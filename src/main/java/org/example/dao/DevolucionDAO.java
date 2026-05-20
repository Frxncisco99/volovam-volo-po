package org.example.dao;

import org.example.modelo.DevolucionLinea;
import org.example.servicio.AuditoriaService;
import org.example.servicio.FolioService;
import org.example.servicio.InventarioMovimientoService;
import org.example.servicio.PermisoService;

import java.sql.*;
import java.util.*;

public class DevolucionDAO {

    /**
     * Devuelve las líneas de una venta con cuánto ya se devolvió y cuánto queda.
     */
    public List<DevolucionLinea> obtenerLineasDisponibles(int idVenta) {
        List<DevolucionLinea> lista = new ArrayList<>();
        String sql = """
            SELECT 
                dv.id_detalle,
                dv.id_producto,
                p.nombre,
                dv.cantidad                                   AS vendido,
                COALESCE(SUM(dd.cantidad), 0)                 AS ya_devuelto,
                dv.cantidad - COALESCE(SUM(dd.cantidad), 0)   AS disponible,
                dv.precio_unitario
            FROM detalle_venta dv
            JOIN productos p ON dv.id_producto = p.id_producto
            LEFT JOIN detalle_devolucion dd ON dd.id_detalle_venta = dv.id_detalle
            WHERE dv.id_venta = ?
            GROUP BY dv.id_detalle, dv.id_producto, p.nombre, dv.cantidad, dv.precio_unitario
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new DevolucionLinea(
                        rs.getInt("id_detalle"),
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getInt("vendido"),
                        rs.getInt("ya_devuelto"),
                        rs.getInt("disponible"),
                        rs.getDouble("precio_unitario")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * Registra una devolución validando que no exceda lo permitido.
     * Lanza IllegalStateException si alguna línea excede el disponible.
     */
    public void registrarDevolucion(int idVenta, int idUsuario,
                                    Map<Integer, Integer> devolucionesPorDetalle,
                                    String tipoReembolso, String notas) throws Exception {

        if (!PermisoService.requerirPermisoOAutorizacionAdmin(
                PermisoService.VENTAS_DEVOLVER,
                "Procesar devolucion de venta " + FolioService.venta(idVenta))) {
            throw new SecurityException("No se autorizo la devolucion.");
        }

        if (devolucionesPorDetalle == null || devolucionesPorDetalle.values().stream().noneMatch(c -> c != null && c > 0)) {
            throw new IllegalArgumentException("Selecciona al menos una cantidad valida para devolver.");
        }

        Connection con = ConexionDB.getConexion();
        if (con == null) throw new Exception("Sin conexión a la base de datos.");

        try {
            con.setAutoCommit(false);
            validarVentaDevolvible(con, idVenta);
            validarLimites(con, idVenta, devolucionesPorDetalle);

            // Calcular monto total a devolver
            double montoTotal = calcularMonto(con, devolucionesPorDetalle);
            if (montoTotal <= 0) {
                throw new IllegalStateException("El monto de devolucion debe ser mayor a cero.");
            }

            // Insertar en devoluciones
            int idDevolucion;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO devoluciones (id_venta, id_usuario, monto_devuelto, tipo_reembolso, notas) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idVenta);
                ps.setInt(2, idUsuario);
                ps.setDouble(3, montoTotal);
                ps.setString(4, tipoReembolso);
                ps.setString(5, notas);
                ps.executeUpdate();
                ResultSet rk = ps.getGeneratedKeys();
                rk.next();
                idDevolucion = rk.getInt(1);
            }

            // Insertar detalle y actualizar stock
            for (Map.Entry<Integer, Integer> entry : devolucionesPorDetalle.entrySet()) {
                int idDetalle  = entry.getKey();
                int cantDevuelta = entry.getValue();
                if (cantDevuelta <= 0) continue;

                // Insertar línea de devolución
                int idProducto = obtenerIdProducto(con, idDetalle);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO detalle_devolucion (id_devolucion, id_detalle_venta, id_producto, cantidad) " +
                                "VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, idDevolucion);
                    ps.setInt(2, idDetalle);
                    ps.setInt(3, idProducto);
                    ps.setInt(4, cantDevuelta);
                    ps.executeUpdate();
                }

                // Devolver stock
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE productos SET stock = stock + ? WHERE id_producto = ?")) {
                    ps.setInt(1, cantDevuelta);
                    ps.setInt(2, idProducto);
                    ps.executeUpdate();
                }

                // Registrar movimiento de inventario
                InventarioMovimientoService.get().registrar(con, idProducto,
                        InventarioMovimientoService.TipoMovimiento.DEVOLUCION,
                        cantDevuelta, idDevolucion, "DEVOLUCION",
                        "Devolución " + FolioService.devolucion(idDevolucion));
            }

            // Actualizar estado de la venta
            actualizarEstadoVenta(con, idVenta);


            con.commit();

            // Auditoría (fuera de la transacción, no rompe el flujo si falla)
            AuditoriaService.get().registrar(
                    idUsuario, "DEVOLUCION", "devoluciones", idDevolucion,
                    String.format("Devolución %s de venta %s — Monto: $%.2f — Tipo: %s",
                            FolioService.devolucion(idDevolucion),
                            FolioService.venta(idVenta),
                            montoTotal, tipoReembolso)
            );

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
            con.close();
        }
    }

    private void validarLimites(Connection con, int idVenta, Map<Integer, Integer> devolucionesPorDetalle) throws Exception {
        String sql = """
            SELECT dv.id_detalle,
                   dv.cantidad - COALESCE((
                       SELECT SUM(dd.cantidad)
                       FROM detalle_devolucion dd
                       WHERE dd.id_detalle_venta = dv.id_detalle
                   ), 0) AS disponible
            FROM detalle_venta dv
            WHERE dv.id_detalle = ?
              AND dv.id_venta = ?
            FOR UPDATE
        """;
        for (Map.Entry<Integer, Integer> entry : devolucionesPorDetalle.entrySet()) {
            int cantidad = entry.getValue() == null ? 0 : entry.getValue();
            if (cantidad <= 0) continue;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, entry.getKey());
                ps.setInt(2, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("El detalle #" + entry.getKey() + " no pertenece a la venta seleccionada.");
                    }
                    int disponible = rs.getInt("disponible");
                    if (cantidad > disponible) {
                        throw new IllegalStateException(
                                "Intento de devolver " + cantidad +
                                        " unidades, pero solo hay " + disponible +
                                        " disponibles para el detalle #" + entry.getKey()
                        );
                    }
                }
            }
        }
    }

    private void validarVentaDevolvible(Connection con, int idVenta) throws Exception {
        String sql = "SELECT COALESCE(estado, 'COMPLETADA') AS estado FROM ventas WHERE id_venta = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("La venta no existe.");
                String estado = rs.getString("estado");
                if ("CANCELADA".equalsIgnoreCase(estado)) {
                    throw new IllegalStateException("Una venta cancelada no puede tener devoluciones.");
                }
                if ("DEVUELTA".equalsIgnoreCase(estado)) {
                    throw new IllegalStateException("Esta venta ya fue devuelta por completo.");
                }
            }
        }
    }

    private double calcularMonto(Connection con, Map<Integer, Integer> devs) throws SQLException {
        double total = 0;
        String sql = "SELECT precio_unitario FROM detalle_venta WHERE id_detalle = ?";
        for (Map.Entry<Integer, Integer> entry : devs.entrySet()) {
            if (entry.getValue() <= 0) continue;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, entry.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("No se encontro el detalle #" + entry.getKey());
                    }
                    total += rs.getDouble(1) * entry.getValue();
                }
            }
        }
        return total;
    }

    private int obtenerIdProducto(Connection con, int idDetalle) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_producto FROM detalle_venta WHERE id_detalle = ?")) {
            ps.setInt(1, idDetalle);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("No se encontró el detalle #" + idDetalle);
    }

    /**
     * Recalcula el estado de la venta basándose en las devoluciones existentes.
     */
    private void actualizarEstadoVenta(Connection con, int idVenta) throws SQLException {
        String sqlTotales = """
            SELECT 
                SUM(dv.cantidad)               AS total_vendido,
                COALESCE(SUM(dd_sum.devuelto), 0) AS total_devuelto
            FROM detalle_venta dv
            LEFT JOIN (
                SELECT id_detalle_venta, SUM(cantidad) AS devuelto
                FROM detalle_devolucion dd
                JOIN devoluciones dev ON dev.id_devolucion = dd.id_devolucion
                GROUP BY id_detalle_venta
            ) dd_sum ON dd_sum.id_detalle_venta = dv.id_detalle
            WHERE dv.id_venta = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sqlTotales)) {
            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int vendido   = rs.getInt("total_vendido");
                int devuelto  = rs.getInt("total_devuelto");

                String nuevoEstado;
                if (devuelto == 0)             nuevoEstado = "COMPLETADA";
                else if (devuelto >= vendido)  nuevoEstado = "DEVUELTA";
                else                           nuevoEstado = "PARCIALMENTE_DEVUELTA";

                try (PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE ventas SET estado = ? WHERE id_venta = ?")) {
                    psUpd.setString(1, nuevoEstado);
                    psUpd.setInt(2, idVenta);
                    psUpd.executeUpdate();
                }
            }
        }
    }

    /**
     * Últimas ventas para mostrar en la pantalla de devoluciones.
     */
    public List<Map<String, Object>> obtenerVentasRecientes(String filtro, int limite) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT v.id_venta, 
                   DATE_FORMAT(v.fecha,'%d/%m/%Y %H:%i') AS fecha,
                   COALESCE(c.nombre,'Publico General')   AS cliente,
                   v.total,
                   COALESCE(v.estado,'COMPLETADA')        AS estado,
                   COALESCE(SUM(dev.monto_devuelto), 0)   AS total_devuelto
            FROM ventas v
            LEFT JOIN clientes c ON v.id_cliente = c.id_cliente
            LEFT JOIN devoluciones dev ON dev.id_venta = v.id_venta
            WHERE COALESCE(v.estado, 'COMPLETADA') NOT IN ('DEVUELTA', 'CANCELADA')
              AND (? = '' OR CAST(v.id_venta AS CHAR) LIKE ? 
                          OR c.nombre LIKE ?)
            GROUP BY v.id_venta, v.fecha, c.nombre, v.total, v.estado
            ORDER BY v.fecha DESC
            LIMIT ?
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + filtro + "%";
            ps.setString(1, filtro);
            ps.setString(2, patron);
            ps.setString(3, patron);
            ps.setInt(4, limite);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id_venta",       rs.getInt("id_venta"));
                row.put("fecha",          rs.getString("fecha"));
                row.put("cliente",        rs.getString("cliente"));
                row.put("total",          rs.getDouble("total"));
                row.put("estado",         rs.getString("estado"));
                row.put("total_devuelto", rs.getDouble("total_devuelto"));
                lista.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}
