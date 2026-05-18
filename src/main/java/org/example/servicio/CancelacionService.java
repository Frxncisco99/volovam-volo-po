package org.example.servicio;

import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CancelacionService {

    /**
     * Cancela una venta: regresa stock, marca estado, registra auditoría y movimientos.
     */
    public void cancelarVenta(int idVenta, String motivo) throws Exception {
        if (motivo == null || motivo.trim().length() < 5) {
            throw new IllegalArgumentException("Indica un motivo de cancelacion valido de al menos 5 caracteres.");
        }
        motivo = motivo.trim();

        if (!PermisoService.requerirPermisoOAutorizacionAdmin(
                PermisoService.VENTAS_CANCELAR,
                "Cancelar venta " + FolioService.venta(idVenta))) {
            throw new SecurityException("No se autorizo la cancelacion.");
        }

        Connection con = ConexionDB.getConexion();
        if (con == null) throw new Exception("Sin conexión a la base de datos.");

        try {
            con.setAutoCommit(false);

            // 1. Verificar que la venta existe y su estado actual
            String sqlEstado = "SELECT estado, COALESCE(fecha_hora, fecha) AS fecha_venta FROM ventas WHERE id_venta = ?";
            String estadoActual;
            LocalDateTime fechaVenta;
            try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                ps.setInt(1, idVenta);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new Exception("Venta #" + idVenta + " no encontrada.");
                estadoActual = rs.getString("estado");
                Timestamp ts = rs.getTimestamp("fecha_venta");
                fechaVenta = ts != null ? ts.toLocalDateTime() : null;
            }

            if ("CANCELADA".equalsIgnoreCase(estadoActual))
                throw new IllegalStateException("Esta venta ya está cancelada.");
            if ("DEVUELTA".equalsIgnoreCase(estadoActual))
                throw new IllegalStateException("No se puede cancelar una venta ya devuelta.");
            if ("PARCIALMENTE_DEVUELTA".equalsIgnoreCase(estadoActual))
                throw new IllegalStateException("No se puede cancelar una venta con devoluciones parciales.");
            validarVentanaCancelacion(con, fechaVenta);

            // 2. Obtener productos de la venta
            String sqlDetalle = """
                SELECT id_producto, cantidad 
                FROM detalle_venta 
                WHERE id_venta = ?
            """;
            List<int[]> productos = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idVenta);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    productos.add(new int[]{
                            rs.getInt("id_producto"),
                            rs.getInt("cantidad")
                    });
                }
            }

            // 3. Regresar stock y registrar movimientos
            InventarioMovimientoService invService = InventarioMovimientoService.get();
            for (int[] prod : productos) {
                // Regresar stock
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE productos SET stock = stock + ? WHERE id_producto = ?")) {
                    ps.setInt(1, prod[1]);
                    ps.setInt(2, prod[0]);
                    ps.executeUpdate();
                }
                // Registrar movimiento
                invService.registrar(con, prod[0],
                        InventarioMovimientoService.TipoMovimiento.CANCELACION,
                        prod[1], idVenta, "VENTA",
                        "Cancelación venta " + FolioService.venta(idVenta));
            }

            // 4. Registrar cancelación
            int idCancelacion;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cancelaciones (id_venta, id_usuario, motivo) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idVenta);
                ps.setInt(2, SesionUsuario.getInstancia().getIdUsuario());
                ps.setString(3, motivo);
                ps.executeUpdate();
                ResultSet rk = ps.getGeneratedKeys();
                rk.next();
                idCancelacion = rk.getInt(1);
            }

            // 5. Actualizar estado de la venta
            boolean tieneFechaCancelacion = columnaExiste(con, "ventas", "fecha_cancelacion");
            boolean tieneMotivoCancelacion = columnaExiste(con, "ventas", "motivo_cancelacion");
            String sqlCancelar = tieneFechaCancelacion && tieneMotivoCancelacion
                    ? "UPDATE ventas SET estado = 'CANCELADA', fecha_cancelacion = NOW(), motivo_cancelacion = ? WHERE id_venta = ?"
                    : "UPDATE ventas SET estado = 'CANCELADA' WHERE id_venta = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlCancelar)) {
                if (tieneFechaCancelacion && tieneMotivoCancelacion) {
                    ps.setString(1, motivo);
                    ps.setInt(2, idVenta);
                } else {
                    ps.setInt(1, idVenta);
                }
                ps.executeUpdate();
            }

            con.commit();

            // 6. Auditoría (fuera de la transacción, no debe romper el flujo)
            AuditoriaService.get().registrar(
                    "CANCELACION", "ventas", idVenta,
                    "Venta " + FolioService.venta(idVenta) +
                            " cancelada. Motivo: " + motivo
            );

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
            con.close();
        }
    }

    private boolean columnaExiste(Connection con, String tabla, String columna) {
        try (ResultSet rs = con.getMetaData().getColumns(con.getCatalog(), null, tabla, columna)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    private void validarVentanaCancelacion(Connection con, LocalDateTime fechaVenta) throws Exception {
        int limiteHoras = obtenerLimiteCancelacionHoras(con);
        if (limiteHoras <= 0 || fechaVenta == null) {
            return;
        }
        long horas = Duration.between(fechaVenta, LocalDateTime.now()).toHours();
        if (horas > limiteHoras) {
            throw new IllegalStateException("La venta supera el limite de " + limiteHoras + " horas para cancelacion.");
        }
    }

    private int obtenerLimiteCancelacionHoras(Connection con) {
        String sql = "SELECT valor FROM configuracion WHERE clave = 'cancelacion_limite_horas' LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Integer.parseInt(rs.getString("valor"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 24;
    }
}
