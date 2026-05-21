package org.example.dao;

import java.sql.*;
import java.util.*;

public class ReporteAvanzadoDAO {

    // -- Ventas netas (descuenta devoluciones) -----------------------------
    public Map<String, Double> resumenNeto(String fechaInicio, String fechaFin) {
        Map<String, Double> resultado = new LinkedHashMap<>();
        String sql = """
            SELECT 
                COALESCE(SUM(venta_bruta), 0)   AS bruto,
                COALESCE(SUM(total_devuelto), 0) AS devuelto,
                COALESCE(SUM(venta_neta), 0)     AS neto,
                COUNT(*)                          AS tickets
            FROM vista_ventas_netas
            WHERE fecha BETWEEN ? AND ?
              AND estado != 'CANCELADA'
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("bruto",    rs.getDouble("bruto"));
                resultado.put("devuelto", rs.getDouble("devuelto"));
                resultado.put("neto",     rs.getDouble("neto"));
                resultado.put("tickets",  rs.getDouble("tickets"));
            }
        } catch (Exception e) { org.example.servicio.LogService.error("Error no controlado", e); }
        return resultado;
    }

    // -- Ventas por cajero -------------------------------------------------
    public List<Map<String, Object>> ventasPorCajero(String fechaInicio, String fechaFin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT 
                cajero,
                COUNT(*)                          AS tickets,
                COALESCE(SUM(venta_bruta), 0)    AS bruto,
                COALESCE(SUM(venta_neta), 0)     AS neto,
                COALESCE(AVG(venta_neta), 0)     AS promedio,
                COUNT(CASE WHEN estado IN 
                    ('DEVUELTA','PARCIALMENTE_DEVUELTA') 
                    THEN 1 END)                   AS con_devolucion
            FROM vista_ventas_netas
            WHERE fecha BETWEEN ? AND ?
              AND estado != 'CANCELADA'
            GROUP BY cajero
            ORDER BY neto DESC
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("cajero",          rs.getString("cajero"));
                fila.put("tickets",         rs.getInt("tickets"));
                fila.put("bruto",           rs.getDouble("bruto"));
                fila.put("neto",            rs.getDouble("neto"));
                fila.put("promedio",        rs.getDouble("promedio"));
                fila.put("con_devolucion",  rs.getInt("con_devolucion"));
                lista.add(fila);
            }
        } catch (Exception e) { org.example.servicio.LogService.error("Error no controlado", e); }
        return lista;
    }

    // -- Ventas por hora ---------------------------------------------------
    public List<Map<String, Object>> ventasPorHora(String fechaInicio, String fechaFin) {
        List<Map<String, Object>> lista = new ArrayList<>();

        // Inicializar las 24 horas en 0
        for (int h = 0; h < 24; h++) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("hora",    h);
            fila.put("tickets", 0);
            fila.put("total",   0.0);
            lista.add(fila);
        }

        String sql = """
            SELECT 
                hora_venta,
                COUNT(*)                       AS tickets,
                COALESCE(SUM(venta_neta), 0)  AS total
            FROM vista_ventas_netas
            WHERE fecha BETWEEN ? AND ?
              AND estado != 'CANCELADA'
            GROUP BY hora_venta
            ORDER BY hora_venta
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int hora = rs.getInt("hora_venta");
                lista.get(hora).put("tickets", rs.getInt("tickets"));
                lista.get(hora).put("total",   rs.getDouble("total"));
            }
        } catch (Exception e) { org.example.servicio.LogService.error("Error no controlado", e); }
        return lista;
    }

    // -- Rentabilidad por producto -----------------------------------------
    public List<Map<String, Object>> rentabilidadProductos(String fechaInicio, String fechaFin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT 
                p.nombre,
                SUM(dv.cantidad)                              AS unidades,
                SUM(dv.cantidad * dv.precio_unitario)         AS ingresos,
                SUM(dv.cantidad * p.costo)                    AS costos,
                SUM(dv.cantidad * (dv.precio_unitario - p.costo)) AS ganancia,
                CASE 
                    WHEN SUM(dv.cantidad * dv.precio_unitario) > 0
                    THEN ROUND(
                        SUM(dv.cantidad * (dv.precio_unitario - p.costo)) 
                        / SUM(dv.cantidad * dv.precio_unitario) * 100, 1)
                    ELSE 0
                END AS margen
            FROM detalle_venta dv
            JOIN productos p ON dv.id_producto = p.id_producto
            JOIN ventas v ON dv.id_venta = v.id_venta
            WHERE v.fecha BETWEEN ? AND ?
              AND (v.estado IS NULL OR v.estado != 'CANCELADA')
            GROUP BY p.id_producto, p.nombre
            ORDER BY ganancia DESC
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("nombre",   rs.getString("nombre"));
                fila.put("unidades", rs.getInt("unidades"));
                fila.put("ingresos", rs.getDouble("ingresos"));
                fila.put("costos",   rs.getDouble("costos"));
                fila.put("ganancia", rs.getDouble("ganancia"));
                fila.put("margen",   rs.getDouble("margen"));
                lista.add(fila);
            }
        } catch (Exception e) { org.example.servicio.LogService.error("Error no controlado", e); }
        return lista;
    }

    // -- Clientes con crédito ----------------------------------------------
    public List<Map<String, Object>> clientesConCredito() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT 
                c.nombre,
                c.telefono,
                c.limite_credito,
                c.saldo_actual,
                c.limite_credito - c.saldo_actual  AS disponible,
                COUNT(v.id_venta)                  AS total_compras,
                COALESCE(SUM(v.total), 0)          AS monto_total_compras,
                MAX(v.fecha)                        AS ultima_compra
            FROM clientes c
            LEFT JOIN ventas v ON v.id_cliente = c.id_cliente
              AND (v.estado IS NULL OR v.estado != 'CANCELADA')
            WHERE c.activo = 1
              AND c.nombre != 'Publico General'
            GROUP BY c.id_cliente, c.nombre, c.telefono,
                     c.limite_credito, c.saldo_actual
            ORDER BY c.saldo_actual DESC
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("nombre",               rs.getString("nombre"));
                fila.put("telefono",             rs.getString("telefono"));
                fila.put("limite",               rs.getDouble("limite_credito"));
                fila.put("saldo",                rs.getDouble("saldo_actual"));
                fila.put("disponible",           rs.getDouble("disponible"));
                fila.put("total_compras",        rs.getInt("total_compras"));
                fila.put("monto_total_compras",  rs.getDouble("monto_total_compras"));
                Timestamp uc = rs.getTimestamp("ultima_compra");
                fila.put("ultima_compra", uc != null ? uc.toLocalDateTime()
                                                       .format(java.time.format.DateTimeFormatter
                                                               .ofPattern("dd/MM/yyyy")) : "-");
                lista.add(fila);
            }
        } catch (Exception e) { org.example.servicio.LogService.error("Error no controlado", e); }
        return lista;
    }

    public List<Map<String, Object>> obtenerAuditoria(
            String fechaInicio, String fechaFin, String usuario, String accion) {
        List<Map<String, Object>> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
        SELECT 
            DATE_FORMAT(a.fecha, '%d/%m/%Y %H:%i:%s') AS fecha,
            u.nombre    AS usuario,
            a.accion,
            a.tabla_afectada AS tabla,
            a.detalle
        FROM auditoria a
        JOIN usuarios u ON a.id_usuario = u.id_usuario
        WHERE a.fecha BETWEEN ? AND ?
    """);

        if (!usuario.isEmpty()) sql.append(" AND u.nombre LIKE ?");
        if (!accion.isEmpty())  sql.append(" AND a.accion = ?");
        sql.append(" ORDER BY a.fecha DESC LIMIT 500");

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            int idx = 3;
            if (!usuario.isEmpty()) ps.setString(idx++, "%" + usuario + "%");
            if (!accion.isEmpty())  ps.setString(idx,   accion);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("fecha",   rs.getString("fecha"));
                fila.put("usuario", rs.getString("usuario"));
                fila.put("accion",  rs.getString("accion"));
                fila.put("tabla",   rs.getString("tabla"));
                fila.put("detalle", rs.getString("detalle"));
                lista.add(fila);
            }
        } catch (Exception e) { org.example.servicio.LogService.error("Error no controlado", e); }
        return lista;
    }

    // -- Movimientos de inventario -----------------------------------------
    public List<Map<String, Object>> movimientosInventario(
            String fechaInicio, String fechaFin, int idProducto) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT 
                mi.fecha,
                p.nombre        AS producto,
                mi.tipo,
                mi.cantidad,
                mi.stock_anterior,
                mi.stock_nuevo,
                mi.referencia_tipo,
                mi.referencia_id,
                u.nombre        AS usuario,
                mi.notas
            FROM movimientos_inventario mi
            JOIN productos p ON mi.id_producto = p.id_producto
            JOIN usuarios u  ON mi.id_usuario  = u.id_usuario
            WHERE mi.fecha BETWEEN ? AND ?
        """ + (idProducto > 0 ? " AND mi.id_producto = ?" : "") + """
            ORDER BY mi.fecha DESC
            LIMIT 200
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            if (idProducto > 0) ps.setInt(3, idProducto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("fecha",     rs.getTimestamp("fecha").toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter
                                .ofPattern("dd/MM/yyyy HH:mm")));
                fila.put("producto",  rs.getString("producto"));
                fila.put("tipo",      rs.getString("tipo"));
                fila.put("cantidad",  rs.getInt("cantidad"));
                fila.put("anterior",  rs.getInt("stock_anterior"));
                fila.put("nuevo",     rs.getInt("stock_nuevo"));
                fila.put("referencia",rs.getString("referencia_tipo") +
                        (rs.getInt("referencia_id") > 0
                                ? " #" + rs.getInt("referencia_id") : ""));
                fila.put("usuario",   rs.getString("usuario"));
                fila.put("notas",     rs.getString("notas"));
                lista.add(fila);
            }
        } catch (Exception e) { org.example.servicio.LogService.error("Error no controlado", e); }
        return lista;

    }
}