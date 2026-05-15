package org.example.dao;

import org.example.modelo.CorteCajaReporte;
import org.example.servicio.FolioService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.DatabaseMetaData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class CorteCajaDAO {

    public CorteCajaReporte obtenerCorteActual(int idCaja, String cajeroActual) throws Exception {
        CorteCajaReporte reporte = new CorteCajaReporte();
        reporte.setIdCaja(idCaja);
        reporte.setFolio("COR-PENDIENTE");
        reporte.setFechaCierre(LocalDateTime.now());
        reporte.setCajero(cajeroActual);

        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) throw new IllegalStateException("Sin conexion a la base de datos.");

            cargarCaja(con, idCaja, reporte);
            cargarResumenVentas(con, idCaja, reporte);
            cargarMetodosPago(con, idCaja, reporte);
            cargarMovimientos(con, idCaja, reporte);
            cargarCancelacionesYDevoluciones(con, idCaja, reporte);
            cargarProductosMasVendidos(con, idCaja, reporte);
            calcularTotalesDerivados(reporte);
        }

        return reporte;
    }

    public java.util.List<CorteCajaReporte.HistorialCorte> obtenerHistorial(CorteCajaReporte.FiltroHistorial filtro) throws Exception {
        java.util.List<CorteCajaReporte.HistorialCorte> historial = new java.util.ArrayList<>();
        try (Connection con = ConexionDB.getConexion()) {
            boolean tieneEstado = columnaExiste(con, "corte_caja", "estado");
            StringBuilder sql = new StringBuilder("""
                SELECT cc.id_corte,
                       COALESCE(u.nombre, 'Usuario no disponible') AS cajero,
                       cc.id_caja,
                       cc.fecha_apertura,
                       cc.fecha_cierre,
                       cc.total_ventas,
                       cc.dinero_esperado,
                       cc.dinero_real,
                       cc.diferencia,
                       """);
            sql.append(tieneEstado ? "COALESCE(cc.estado, 'CERRADO')" : "'CERRADO'");
            sql.append("""
                       AS estado
                FROM corte_caja cc
                LEFT JOIN usuarios u ON u.id_usuario = cc.id_usuario
                WHERE 1 = 1
                """);
            if (filtro.getInicio() != null) sql.append(" AND DATE(cc.fecha_cierre) >= ? ");
            if (filtro.getFin() != null) sql.append(" AND DATE(cc.fecha_cierre) <= ? ");
            sql.append(" ORDER BY cc.fecha_cierre DESC ");
            if (filtro.getLimite() > 0) sql.append(" LIMIT ").append(filtro.getLimite());

            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int i = 1;
                if (filtro.getInicio() != null) ps.setDate(i++, java.sql.Date.valueOf(filtro.getInicio()));
                if (filtro.getFin() != null) ps.setDate(i, java.sql.Date.valueOf(filtro.getFin()));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int idCorte = rs.getInt("id_corte");
                    historial.add(new CorteCajaReporte.HistorialCorte(
                            FolioService.corte(idCorte),
                            rs.getString("cajero"),
                            rs.getInt("id_caja"),
                            toLocalDateTime(rs.getTimestamp("fecha_apertura")),
                            toLocalDateTime(rs.getTimestamp("fecha_cierre")),
                            rs.getDouble("total_ventas"),
                            rs.getDouble("dinero_esperado"),
                            rs.getDouble("dinero_real"),
                            rs.getDouble("diferencia"),
                            rs.getString("estado")
                    ));
                }
            }
        }
        return historial;
    }

    public int registrarCorte(int idCaja, int idUsuario, CorteCajaReporte reporte, String observaciones) throws Exception {
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) throw new IllegalStateException("Sin conexion a la base de datos.");
            con.setAutoCommit(false);
            try {
                try (PreparedStatement psCerrar = con.prepareStatement(
                        "UPDATE caja SET estado = 'cerrada', fecha_cierre = NOW(), monto_final = ? WHERE id_caja = ?")) {
                    psCerrar.setDouble(1, reporte.getEfectivoContado());
                    psCerrar.setInt(2, idCaja);
                    psCerrar.executeUpdate();
                }

                boolean tieneEstado = columnaExiste(con, "corte_caja", "estado");
                int idCorte;
                String sqlInsert = tieneEstado ? """
                        INSERT INTO corte_caja (
                            id_caja, id_usuario, fecha_apertura, fecha_cierre, fondo_inicial,
                            total_ventas, num_tickets, total_entradas, total_salidas, dinero_esperado,
                            dinero_real, diferencia, observaciones, estado
                        )
                        VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CERRADO')
                        """ : """
                        INSERT INTO corte_caja (
                            id_caja, id_usuario, fecha_apertura, fecha_cierre, fondo_inicial,
                            total_ventas, num_tickets, total_entradas, total_salidas, dinero_esperado,
                            dinero_real, diferencia, observaciones
                        )
                        VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement psCorte = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    psCorte.setInt(1, idCaja);
                    psCorte.setInt(2, idUsuario);
                    psCorte.setTimestamp(3, Timestamp.valueOf(reporte.getFechaApertura()));
                    psCorte.setDouble(4, reporte.getFondoInicial());
                    psCorte.setDouble(5, reporte.getTotalVendido());
                    psCorte.setInt(6, reporte.getCantidadTickets());
                    psCorte.setDouble(7, reporte.getTotalEntradas());
                    psCorte.setDouble(8, reporte.getTotalSalidas());
                    psCorte.setDouble(9, reporte.getEfectivoEsperado());
                    psCorte.setDouble(10, reporte.getEfectivoContado());
                    psCorte.setDouble(11, reporte.getDiferencia());
                    psCorte.setString(12, observaciones);
                    psCorte.executeUpdate();
                    ResultSet rs = psCorte.getGeneratedKeys();
                    rs.next();
                    idCorte = rs.getInt(1);
                }

                guardarConteoDenominaciones(con, idCorte, reporte.getConteoDenominaciones());
                con.commit();
                return idCorte;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void cargarCaja(Connection con, int idCaja, CorteCajaReporte reporte) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT c.monto_inicial,
                       c.fecha_apertura,
                       c.fecha_cierre,
                       COALESCE(c.estado, 'abierta') AS estado,
                       COALESCE(u.nombre, ?) AS cajero
                FROM caja c
                LEFT JOIN usuarios u ON u.id_usuario = c.id_usuario
                WHERE c.id_caja = ?
                """)) {
            ps.setString(1, reporte.getCajero());
            ps.setInt(2, idCaja);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                reporte.setFondoInicial(rs.getDouble("monto_inicial"));
                reporte.setFechaApertura(toLocalDateTime(rs.getTimestamp("fecha_apertura")));
                reporte.setFechaCierre(toLocalDateTime(rs.getTimestamp("fecha_cierre")));
                if (reporte.getFechaCierre() == null) reporte.setFechaCierre(LocalDateTime.now());
                reporte.setEstado(rs.getString("estado"));
                reporte.setCajero(rs.getString("cajero"));
            }
        }
    }

    private void cargarResumenVentas(Connection con, int idCaja, CorteCajaReporte reporte) throws Exception {
        if (tablaExiste(con, "detalle_pago")) {
            cargarResumenVentasConDetallePago(con, idCaja, reporte);
            return;
        }

        try (PreparedStatement ps = con.prepareStatement("""
                SELECT ventas_resumen.tickets,
                       ventas_resumen.total_ventas,
                       ventas_resumen.efectivo_real,
                       COALESCE(costos.costo_total, 0) AS costo_total
                FROM (
                    SELECT COUNT(v.id_venta) AS tickets,
                           COALESCE(SUM(v.total), 0) AS total_ventas,
                           COALESCE(SUM(CASE
                               WHEN COALESCE(pg.tipo_pago, v.metodo_pago) IN ('EFECTIVO', 'DOLARES') THEN v.total
                               WHEN COALESCE(pg.tipo_pago, v.metodo_pago) IN ('MIXTO', 'MIXTO_USD') THEN GREATEST(pg.monto_recibido - pg.cambio, 0)
                               ELSE 0
                           END), 0) AS efectivo_real
                    FROM ventas v
                    LEFT JOIN pagos pg ON pg.id_venta = v.id_venta
                    WHERE v.id_caja = ?
                      AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
                ) ventas_resumen
                CROSS JOIN (
                    SELECT COALESCE(SUM(dv.cantidad * COALESCE(p.costo, 0)), 0) AS costo_total
                    FROM detalle_venta dv
                    JOIN ventas v ON v.id_venta = dv.id_venta
                    LEFT JOIN productos p ON p.id_producto = dv.id_producto
                    WHERE v.id_caja = ?
                      AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
                ) costos
                """)) {
            ps.setInt(1, idCaja);
            ps.setInt(2, idCaja);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int tickets = rs.getInt("tickets");
                double total = rs.getDouble("total_ventas");
                reporte.setCantidadTickets(tickets);
                reporte.setTotalVendido(total);
                reporte.setPromedioTicket(tickets > 0 ? total / tickets : 0);
                reporte.setCostos(rs.getDouble("costo_total"));
                reporte.setIngresos(total);
                reporte.setEfectivoEsperado(reporte.getFondoInicial() + rs.getDouble("efectivo_real") - totalDevolucionesEfectivo(con, idCaja));
            }
        }
    }

    private void cargarMetodosPago(Connection con, int idCaja, CorteCajaReporte reporte) throws Exception {
        if (tablaExiste(con, "detalle_pago")) {
            cargarMetodosPagoConDetallePago(con, idCaja, reporte);
            return;
        }

        Map<String, CorteCajaReporte.MetodoPago> base = new LinkedHashMap<>();
        base.put("Efectivo", new CorteCajaReporte.MetodoPago("Efectivo", 0, 0));
        base.put("Tarjeta", new CorteCajaReporte.MetodoPago("Tarjeta", 0, 0));
        base.put("Transferencia", new CorteCajaReporte.MetodoPago("Transferencia", 0, 0));
        base.put("Credito", new CorteCajaReporte.MetodoPago("Credito", 0, 0));

        try (PreparedStatement ps = con.prepareStatement("""
                SELECT CASE
                           WHEN COALESCE(pg.tipo_pago, v.metodo_pago) IN ('EFECTIVO', 'DOLARES', 'MIXTO', 'MIXTO_USD') THEN 'Efectivo'
                           WHEN COALESCE(pg.tipo_pago, v.metodo_pago) = 'TARJETA' THEN 'Tarjeta'
                           WHEN COALESCE(pg.tipo_pago, v.metodo_pago) = 'TRANSFERENCIA' THEN 'Transferencia'
                           WHEN COALESCE(pg.tipo_pago, v.metodo_pago) IN ('FIADO', 'CREDITO') THEN 'Credito'
                           ELSE COALESCE(pg.tipo_pago, v.metodo_pago, 'Otro')
                       END AS metodo,
                       COUNT(v.id_venta) AS cantidad,
                       COALESCE(SUM(v.total), 0) AS total
                FROM ventas v
                LEFT JOIN pagos pg ON pg.id_venta = v.id_venta
                WHERE v.id_caja = ?
                  AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
                GROUP BY metodo
                ORDER BY total DESC
                """)) {
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                base.put(rs.getString("metodo"), new CorteCajaReporte.MetodoPago(
                        rs.getString("metodo"),
                        rs.getInt("cantidad"),
                        rs.getDouble("total")
                ));
            }
        }
        reporte.getMetodosPago().addAll(base.values());
    }

    private void cargarResumenVentasConDetallePago(Connection con, int idCaja, CorteCajaReporte reporte) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT COUNT(v.id_venta) AS tickets,
                       COALESCE(SUM(v.total), 0) AS total_ventas
                FROM ventas v
                WHERE v.id_caja = ?
                  AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
                """)) {
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int tickets = rs.getInt("tickets");
                double total = rs.getDouble("total_ventas");
                reporte.setCantidadTickets(tickets);
                reporte.setTotalVendido(total);
                reporte.setPromedioTicket(tickets > 0 ? total / tickets : 0);
                reporte.setIngresos(total);
            }
        }

        try (PreparedStatement ps = con.prepareStatement("""
                SELECT COALESCE(SUM(dv.cantidad * COALESCE(p.costo, 0)), 0) AS costo_total
                FROM detalle_venta dv
                JOIN ventas v ON v.id_venta = dv.id_venta
                LEFT JOIN productos p ON p.id_producto = dv.id_producto
                WHERE v.id_caja = ?
                  AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
                """)) {
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) reporte.setCostos(rs.getDouble("costo_total"));
        }

        double efectivoReal = 0;
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT COALESCE(SUM(dp.monto), 0) AS efectivo_real
                FROM detalle_pago dp
                JOIN ventas v ON v.id_venta = dp.id_venta
                WHERE v.id_caja = ?
                  AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
                  AND UPPER(COALESCE(dp.metodo_pago, '')) IN ('EFECTIVO', 'DOLARES')
                """)) {
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) efectivoReal = rs.getDouble("efectivo_real");
        }

        reporte.setEfectivoEsperado(reporte.getFondoInicial() + efectivoReal - totalDevolucionesEfectivo(con, idCaja));
    }

    private void cargarMetodosPagoConDetallePago(Connection con, int idCaja, CorteCajaReporte reporte) throws Exception {
        Map<String, CorteCajaReporte.MetodoPago> base = new LinkedHashMap<>();
        base.put("Efectivo", new CorteCajaReporte.MetodoPago("Efectivo", 0, 0));
        base.put("Tarjeta", new CorteCajaReporte.MetodoPago("Tarjeta", 0, 0));
        base.put("Transferencia", new CorteCajaReporte.MetodoPago("Transferencia", 0, 0));
        base.put("Credito", new CorteCajaReporte.MetodoPago("Credito", 0, 0));

        try (PreparedStatement ps = con.prepareStatement("""
                SELECT CASE
                           WHEN UPPER(COALESCE(dp.metodo_pago, '')) IN ('EFECTIVO', 'DOLARES') THEN 'Efectivo'
                           WHEN UPPER(COALESCE(dp.metodo_pago, '')) = 'TARJETA' THEN 'Tarjeta'
                           WHEN UPPER(COALESCE(dp.metodo_pago, '')) = 'TRANSFERENCIA' THEN 'Transferencia'
                           WHEN UPPER(COALESCE(dp.metodo_pago, '')) IN ('FIADO', 'CREDITO') THEN 'Credito'
                           ELSE COALESCE(dp.metodo_pago, 'Otro')
                       END AS metodo,
                       COUNT(DISTINCT dp.id_venta) AS cantidad,
                       COALESCE(SUM(dp.monto), 0) AS total
                FROM detalle_pago dp
                JOIN ventas v ON v.id_venta = dp.id_venta
                WHERE v.id_caja = ?
                  AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
                GROUP BY metodo
                ORDER BY total DESC
                """)) {
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                base.put(rs.getString("metodo"), new CorteCajaReporte.MetodoPago(
                        rs.getString("metodo"),
                        rs.getInt("cantidad"),
                        rs.getDouble("total")
                ));
            }
        }

        try (PreparedStatement ps = con.prepareStatement("""
                SELECT COUNT(v.id_venta) AS cantidad,
                       COALESCE(SUM(v.total), 0) AS total
                FROM ventas v
                WHERE v.id_caja = ?
                  AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
                  AND UPPER(COALESCE(v.metodo_pago, '')) IN ('FIADO', 'CREDITO')
                  AND NOT EXISTS (SELECT 1 FROM detalle_pago dp WHERE dp.id_venta = v.id_venta)
                """)) {
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("cantidad") > 0) {
                base.put("Credito", new CorteCajaReporte.MetodoPago(
                        "Credito",
                        rs.getInt("cantidad"),
                        rs.getDouble("total")
                ));
            }
        }

        reporte.getMetodosPago().addAll(base.values());
    }

    private void cargarMovimientos(Connection con, int idCaja, CorteCajaReporte reporte) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT mc.tipo,
                       COALESCE(mc.motivo, '') AS concepto,
                       mc.monto,
                       mc.fecha,
                       COALESCE(u.nombre, 'Sistema') AS usuario
                FROM movimientos_caja mc
                LEFT JOIN usuarios u ON u.id_usuario = mc.id_usuario
                WHERE mc.id_caja = ?
                ORDER BY mc.fecha DESC
                """)) {
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tipo = rs.getString("tipo");
                double monto = rs.getDouble("monto");
                if ("INGRESO".equalsIgnoreCase(tipo) || "ENTRADA".equalsIgnoreCase(tipo)) {
                    reporte.setTotalEntradas(reporte.getTotalEntradas() + monto);
                } else if ("RETIRO".equalsIgnoreCase(tipo) || "SALIDA".equalsIgnoreCase(tipo)) {
                    reporte.setTotalSalidas(reporte.getTotalSalidas() + monto);
                }
                reporte.getMovimientos().add(new CorteCajaReporte.MovimientoCaja(
                        tipo,
                        rs.getString("concepto"),
                        monto,
                        toLocalDateTime(rs.getTimestamp("fecha")),
                        rs.getString("usuario")
                ));
            }
        }
        reporte.setEfectivoEsperado(reporte.getEfectivoEsperado() + reporte.getTotalEntradas() - reporte.getTotalSalidas());
    }

    private void cargarCancelacionesYDevoluciones(Connection con, int idCaja, CorteCajaReporte reporte) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT 'Cancelacion' AS tipo,
                       c.id_cancelacion AS id_mov,
                       v.total AS total,
                       COALESCE(c.motivo, '') AS motivo,
                       COALESCE(u.nombre, 'Usuario no disponible') AS usuario
                FROM cancelaciones c
                JOIN ventas v ON v.id_venta = c.id_venta
                LEFT JOIN usuarios u ON u.id_usuario = c.id_usuario
                WHERE v.id_caja = ?
                UNION ALL
                SELECT 'Devolucion' AS tipo,
                       d.id_devolucion AS id_mov,
                       d.monto_devuelto AS total,
                       COALESCE(d.notas, '') AS motivo,
                       COALESCE(u.nombre, 'Usuario no disponible') AS usuario
                FROM devoluciones d
                JOIN ventas v ON v.id_venta = d.id_venta
                LEFT JOIN usuarios u ON u.id_usuario = d.id_usuario
                WHERE v.id_caja = ?
                """)) {
            ps.setInt(1, idCaja);
            ps.setInt(2, idCaja);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tipo = rs.getString("tipo");
                int id = rs.getInt("id_mov");
                double total = rs.getDouble("total");
                reporte.setTotalCancelado(reporte.getTotalCancelado() + total);
                reporte.setCantidadCancelaciones(reporte.getCantidadCancelaciones() + 1);
                reporte.getCancelacionesDevoluciones().add(new CorteCajaReporte.CancelacionDevolucion(
                        tipo,
                        "Cancelacion".equals(tipo) ? FolioService.cancelacion(id) : FolioService.devolucion(id),
                        total,
                        rs.getString("motivo"),
                        rs.getString("usuario")
                ));
            }
        }
    }

    private void cargarProductosMasVendidos(Connection con, int idCaja, CorteCajaReporte reporte) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT p.nombre,
                       SUM(dv.cantidad) AS cantidad,
                       SUM(dv.subtotal) AS ingresos,
                       SUM(dv.cantidad * COALESCE(p.costo, 0)) AS costo
                FROM detalle_venta dv
                JOIN ventas v ON v.id_venta = dv.id_venta
                JOIN productos p ON p.id_producto = dv.id_producto
                WHERE v.id_caja = ?
                  AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
                GROUP BY p.id_producto, p.nombre
                ORDER BY cantidad DESC, ingresos DESC
                LIMIT 10
                """)) {
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double ingresos = rs.getDouble("ingresos");
                double costo = rs.getDouble("costo");
                reporte.getProductosMasVendidos().add(new CorteCajaReporte.ProductoVendido(
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        ingresos,
                        costo,
                        ingresos - costo
                ));
            }
        }
    }

    private void calcularTotalesDerivados(CorteCajaReporte reporte) {
        double tasa = tasaFiscalPredeterminada();
        double subtotal = tasa > 0 ? reporte.getTotalVendido() / (1 + tasa) : reporte.getTotalVendido();
        reporte.setSubtotal(subtotal);
        reporte.setIva(reporte.getTotalVendido() - subtotal);
        reporte.setTotalConImpuestos(reporte.getTotalVendido());
        reporte.setUtilidad(reporte.getIngresos() - reporte.getCostos());
    }

    private double tasaFiscalPredeterminada() {
        try {
            return new FiscalDAO().obtenerImpuestoPredeterminado().getTasa().doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private double totalDevolucionesEfectivo(Connection con, int idCaja) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT COALESCE(SUM(d.monto_devuelto), 0) AS total
                FROM devoluciones d
                JOIN ventas v ON v.id_venta = d.id_venta
                WHERE v.id_caja = ?
                  AND COALESCE(d.tipo_reembolso, 'EFECTIVO') = 'EFECTIVO'
                """)) {
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("total") : 0;
        }
    }

    private void guardarConteoDenominaciones(Connection con, int idCorte, String conteoDenominaciones) throws Exception {
        if (conteoDenominaciones == null || conteoDenominaciones.isBlank()) return;
        if (!columnaExiste(con, "corte_caja", "conteo_denominaciones")) return;
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE corte_caja SET conteo_denominaciones = ? WHERE id_corte = ?")) {
            ps.setString(1, conteoDenominaciones);
            ps.setInt(2, idCorte);
            ps.executeUpdate();
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private boolean columnaExiste(Connection con, String tabla, String columna) throws Exception {
        DatabaseMetaData metaData = con.getMetaData();
        try (ResultSet rs = metaData.getColumns(con.getCatalog(), null, tabla, columna)) {
            return rs.next();
        }
    }

    private boolean tablaExiste(Connection con, String tabla) throws Exception {
        DatabaseMetaData metaData = con.getMetaData();
        try (ResultSet rs = metaData.getTables(con.getCatalog(), null, tabla, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
}
