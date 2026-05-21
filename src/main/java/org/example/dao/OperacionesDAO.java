package org.example.dao;

import org.example.modelo.SesionUsuario;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OperacionesDAO {

    public List<Map<String, Object>> listarPrefacturas() {
        String sql = """
                SELECT f.id_factura, f.folio_interno, f.estado, f.modo, f.fecha,
                       f.rfc_receptor, f.razon_social_receptor, f.total, f.id_venta,
                       COALESCE(c.nombre, 'Publico General') AS cliente
                FROM facturas f
                LEFT JOIN ventas v ON v.id_venta = f.id_venta
                LEFT JOIN clientes c ON c.id_cliente = v.id_cliente
                ORDER BY f.fecha DESC, f.id_factura DESC
                LIMIT 300
                """;
        return query(sql);
    }

    public List<Map<String, Object>> listarPromociones() {
        return query("""
                SELECT id_promocion, nombre, tipo, valor, fecha_inicio, fecha_fin, activo
                FROM promociones
                ORDER BY activo DESC, creado_en DESC, id_promocion DESC
                """);
    }

    public List<Map<String, Object>> listarPromocionesActivas() {
        return query("""
                SELECT id_promocion, nombre, tipo, valor, fecha_inicio, fecha_fin, activo
                FROM promociones
                WHERE activo = 1
                  AND (fecha_inicio IS NULL OR DATE(fecha_inicio) <= CURDATE())
                  AND (fecha_fin IS NULL OR DATE(fecha_fin) >= CURDATE())
                ORDER BY nombre
                """);
    }

    public void guardarPromocion(String nombre, String tipo, double valor,
                                 LocalDate inicio, LocalDate fin, boolean activo) throws Exception {
        String sql = """
                INSERT INTO promociones (nombre, tipo, valor, fecha_inicio, fecha_fin, activo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, tipo);
            ps.setDouble(3, valor);
            setFecha(ps, 4, inicio);
            setFecha(ps, 5, fin);
            ps.setBoolean(6, activo);
            ps.executeUpdate();
        }
    }

    public void cambiarActivoPromocion(int idPromocion, boolean activo) throws Exception {
        ejecutarActivo("promociones", "id_promocion", idPromocion, activo);
    }

    public List<Map<String, Object>> listarProveedores() {
        return query("""
                SELECT id_proveedor, nombre, rfc, telefono, email, contacto, activo
                FROM proveedores
                ORDER BY activo DESC, nombre
                """);
    }

    public List<Map<String, Object>> listarProductoProveedor() {
        return query("""
                SELECT pp.id_producto, p.nombre AS producto,
                       pp.id_proveedor, pr.nombre AS proveedor,
                       pp.costo_ultimo, pp.proveedor_principal, pp.activo, pp.actualizado_en
                FROM producto_proveedor pp
                JOIN productos p ON p.id_producto = pp.id_producto
                JOIN proveedores pr ON pr.id_proveedor = pp.id_proveedor
                ORDER BY pp.activo DESC, p.nombre, pp.proveedor_principal DESC, pr.nombre
                """);
    }

    public void guardarProveedor(String nombre, String rfc, String telefono, String email, String contacto) throws Exception {
        String sql = """
                INSERT INTO proveedores (nombre, rfc, telefono, email, contacto)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, vacioANull(rfc));
            ps.setString(3, vacioANull(telefono));
            ps.setString(4, vacioANull(email));
            ps.setString(5, vacioANull(contacto));
            ps.executeUpdate();
        }
    }

    public void vincularProductoProveedor(int idProducto, int idProveedor, double costoUltimo, boolean principal) throws Exception {
        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);
            try {
                if (principal) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE producto_proveedor SET proveedor_principal = 0 WHERE id_producto = ?")) {
                        ps.setInt(1, idProducto);
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("""
                        INSERT INTO producto_proveedor (id_producto, id_proveedor, costo_ultimo, proveedor_principal, activo)
                        VALUES (?, ?, ?, ?, 1)
                        ON DUPLICATE KEY UPDATE costo_ultimo = VALUES(costo_ultimo),
                                                proveedor_principal = VALUES(proveedor_principal),
                                                activo = 1,
                                                actualizado_en = CURRENT_TIMESTAMP
                        """)) {
                    ps.setInt(1, idProducto);
                    ps.setInt(2, idProveedor);
                    ps.setDouble(3, costoUltimo);
                    ps.setBoolean(4, principal);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void cambiarActivoProductoProveedor(int idProducto, int idProveedor, boolean activo) throws Exception {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement("""
                     UPDATE producto_proveedor
                     SET activo = ?, actualizado_en = CURRENT_TIMESTAMP
                     WHERE id_producto = ? AND id_proveedor = ?
                     """)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idProducto);
            ps.setInt(3, idProveedor);
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("No se encontro el enlace producto-proveedor.");
            }
        }
    }

    public List<Map<String, Object>> listarCompras() {
        return query("""
                SELECT c.id_compra, c.folio, c.fecha_hora, COALESCE(p.nombre, 'Sin proveedor') AS proveedor,
                       c.subtotal, c.impuestos, c.total, c.estado
                FROM compras c
                LEFT JOIN proveedores p ON p.id_proveedor = c.id_proveedor
                ORDER BY c.fecha_hora DESC, c.id_compra DESC
                LIMIT 300
                """);
    }

    public void registrarCompra(int idProveedor, int idProducto, String descripcion,
                                double cantidad, double costoUnitario, String folio) throws Exception {
        double subtotal = cantidad * costoUnitario;
        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);
            try {
                int idCompra;
                try (PreparedStatement ps = con.prepareStatement("""
                        INSERT INTO compras (id_proveedor, id_usuario, folio, subtotal, total)
                        VALUES (?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS)) {
                    if (idProveedor > 0) ps.setInt(1, idProveedor); else ps.setNull(1, Types.INTEGER);
                    ps.setInt(2, SesionUsuario.getInstancia().getIdUsuario());
                    ps.setString(3, vacioANull(folio));
                    ps.setDouble(4, subtotal);
                    ps.setDouble(5, subtotal);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new IllegalStateException("No se genero la compra.");
                        idCompra = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("""
                        INSERT INTO compra_detalle (id_compra, id_producto, descripcion, cantidad, costo_unitario, subtotal)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """)) {
                    ps.setInt(1, idCompra);
                    if (idProducto > 0) ps.setInt(2, idProducto); else ps.setNull(2, Types.INTEGER);
                    ps.setString(3, descripcion);
                    ps.setDouble(4, cantidad);
                    ps.setDouble(5, costoUnitario);
                    ps.setDouble(6, subtotal);
                    ps.executeUpdate();
                }

                if (idProducto > 0) {
                    enlazarProductoProveedor(con, idProducto, idProveedor, costoUnitario);
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE productos SET stock = stock + ?, costo = ? WHERE id_producto = ?")) {
                        ps.setInt(1, (int) Math.round(cantidad));
                        ps.setDouble(2, costoUnitario);
                        ps.setInt(3, idProducto);
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void enlazarProductoProveedor(Connection con, int idProducto, int idProveedor, double costoUnitario) throws SQLException {
        if (idProducto <= 0 || idProveedor <= 0 || !tablaExiste(con, "producto_proveedor")) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement("""
                INSERT INTO producto_proveedor (id_producto, id_proveedor, costo_ultimo, proveedor_principal, activo)
                VALUES (?, ?, ?, 1, 1)
                ON DUPLICATE KEY UPDATE costo_ultimo = VALUES(costo_ultimo),
                                        activo = 1,
                                        actualizado_en = CURRENT_TIMESTAMP
                """)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idProveedor);
            ps.setDouble(3, costoUnitario);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> listarTurnos() {
        return query("""
                SELECT t.id_turno, u.nombre AS usuario, t.id_caja, t.fecha_apertura, t.fecha_cierre,
                       t.fondo_inicial, t.efectivo_esperado, t.efectivo_contado, t.diferencia, t.estado
                FROM turnos t
                LEFT JOIN usuarios u ON u.id_usuario = t.id_usuario
                ORDER BY t.fecha_apertura DESC, t.id_turno DESC
                LIMIT 300
                """);
    }

    public void abrirTurno(double fondoInicial, String observaciones) throws Exception {
        String sql = """
                INSERT INTO turnos (id_usuario, id_caja, fondo_inicial, estado, observaciones)
                VALUES (?, ?, ?, 'ABIERTO', ?)
                """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, SesionUsuario.getInstancia().getIdUsuario());
            ps.setInt(2, SesionUsuario.getInstancia().getIdCaja());
            ps.setDouble(3, fondoInicial);
            ps.setString(4, vacioANull(observaciones));
            ps.executeUpdate();
        }
    }

    public void cerrarTurno(int idTurno, double efectivoContado, String observaciones) throws Exception {
        String sql = """
                UPDATE turnos
                SET fecha_cierre = NOW(),
                    efectivo_contado = ?,
                    diferencia = ? - efectivo_esperado,
                    estado = 'CERRADO',
                    observaciones = COALESCE(?, observaciones)
                WHERE id_turno = ? AND estado = 'ABIERTO'
                """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, efectivoContado);
            ps.setDouble(2, efectivoContado);
            ps.setString(3, vacioANull(observaciones));
            ps.setInt(4, idTurno);
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("El turno no esta abierto o no existe.");
            }
        }
    }

    public List<Map<String, Object>> listarMotivosCancelacion() {
        return query("""
                SELECT id_motivo, clave, descripcion, activo
                FROM motivos_cancelacion
                ORDER BY activo DESC, descripcion
                """);
    }

    public void guardarMotivoCancelacion(String clave, String descripcion) throws Exception {
        String sql = """
                INSERT INTO motivos_cancelacion (clave, descripcion)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion), activo = 1
                """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, descripcion);
            ps.executeUpdate();
        }
    }

    public void cambiarActivoMotivo(int idMotivo, boolean activo) throws Exception {
        ejecutarActivo("motivos_cancelacion", "id_motivo", idMotivo, activo);
    }

    public List<Map<String, Object>> listarDetallePrefactura(int idFactura) {
        return query("""
                SELECT descripcion, cantidad, precio_unitario, subtotal AS subtotal_sin_impuesto,
                       descuento, impuesto_importe, total_linea
                FROM factura_detalle
                WHERE id_factura = ?
                ORDER BY id_detalle
                """, idFactura);
    }

    public void cancelarPrefactura(int idFactura, String motivo) throws Exception {
        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE facturas
                        SET estado = 'CANCELADA',
                            fecha_cancelacion = NOW(),
                            motivo_cancelacion = COALESCE(?, motivo_cancelacion)
                        WHERE id_factura = ?
                        """)) {
                    ps.setString(1, vacioANull(motivo));
                    ps.setInt(2, idFactura);
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalStateException("La prefactura no existe.");
                    }
                }

                if (columnaExiste(con, "ventas", "factura_id") && columnaExiste(con, "ventas", "estado_facturacion")) {
                    try (PreparedStatement ps = con.prepareStatement("""
                            UPDATE ventas
                            SET factura_id = NULL,
                                estado_facturacion = 'NO_FACTURADA'
                            WHERE factura_id = ?
                            """)) {
                        ps.setInt(1, idFactura);
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private List<Map<String, Object>> query(String sql) {
        return query(sql, new Object[0]);
    }

    private List<Map<String, Object>> query(String sql, Object... params) {
        List<Map<String, Object>> filas = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    fila.put(md.getColumnLabel(i), rs.getObject(i));
                }
                filas.add(fila);
            }
            }
        } catch (Exception e) {
            org.example.servicio.LogService.warn("Consulta de operaciones omitida", e);
        }
        return filas;
    }

    private boolean columnaExiste(Connection con, String tabla, String columna) throws SQLException {
        try (ResultSet rs = con.getMetaData().getColumns(null, null, tabla, columna)) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = con.getMetaData().getColumns(null, null, tabla.toLowerCase(), columna)) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = con.getMetaData().getColumns(null, null, tabla.toUpperCase(), columna)) {
            return rs.next();
        }
    }

    private boolean tablaExiste(Connection con, String tabla) throws SQLException {
        try (ResultSet rs = con.getMetaData().getTables(null, null, tabla, new String[]{"TABLE"})) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = con.getMetaData().getTables(null, null, tabla.toLowerCase(), new String[]{"TABLE"})) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = con.getMetaData().getTables(null, null, tabla.toUpperCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private void ejecutarActivo(String tabla, String idColumna, int id, boolean activo) throws Exception {
        String sql = "UPDATE " + tabla + " SET activo = ? WHERE " + idColumna + " = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private void setFecha(PreparedStatement ps, int index, LocalDate fecha) throws SQLException {
        if (fecha == null) ps.setNull(index, Types.TIMESTAMP);
        else ps.setTimestamp(index, Timestamp.valueOf(fecha.atStartOfDay()));
    }

    private String vacioANull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
