package org.example.dao;

import org.example.modelo.Factura;
import org.example.modelo.FacturaDetalle;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FacturacionDAO {

    public int obtenerSiguienteFolio(Connection con, String serie, int folioInicial) throws Exception {
        String sql = "SELECT COALESCE(MAX(folio), ?) + 1 FROM facturas WHERE serie = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, folioInicial - 1));
            ps.setString(2, serie);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return Math.max(1, folioInicial);
    }

    public int crearPrefactura(Factura factura) throws Exception {
        String sqlFactura = """
                INSERT INTO facturas (
                    id_venta, serie, folio, folio_interno, estado, modo, uuid,
                    rfc_emisor, razon_social_emisor, regimen_fiscal_emisor, codigo_postal_emisor,
                    rfc_receptor, razon_social_receptor, regimen_fiscal_receptor, codigo_postal_receptor,
                    uso_cfdi, metodo_pago_sat, forma_pago_sat,
                    subtotal, descuento, iva, ieps, impuestos, total_gravado, total_exento,
                    total_tasa0, total
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) throw new IllegalStateException("No hay conexion a la base de datos.");
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, factura.getIdVenta());
                ps.setString(2, factura.getSerie());
                ps.setInt(3, factura.getFolio());
                ps.setString(4, factura.getFolioInterno());
                ps.setString(5, factura.getEstado());
                ps.setString(6, factura.getModo());
                ps.setString(7, factura.getUuid());
                ps.setString(8, factura.getRfcEmisor());
                ps.setString(9, factura.getRazonSocialEmisor());
                ps.setString(10, factura.getRegimenFiscalEmisor());
                ps.setString(11, factura.getCodigoPostalEmisor());
                ps.setString(12, factura.getRfcReceptor());
                ps.setString(13, factura.getRazonSocialReceptor());
                ps.setString(14, factura.getRegimenFiscalReceptor());
                ps.setString(15, factura.getCodigoPostalReceptor());
                ps.setString(16, factura.getUsoCfdi());
                ps.setString(17, factura.getMetodoPagoSat());
                ps.setString(18, factura.getFormaPagoSat());
                ps.setBigDecimal(19, factura.getSubtotal());
                ps.setBigDecimal(20, factura.getDescuento());
                ps.setBigDecimal(21, factura.getIva());
                ps.setBigDecimal(22, factura.getIeps());
                ps.setBigDecimal(23, factura.getImpuestos());
                ps.setBigDecimal(24, factura.getTotalGravado());
                ps.setBigDecimal(25, factura.getTotalExento());
                ps.setBigDecimal(26, factura.getTotalTasa0());
                ps.setBigDecimal(27, factura.getTotal());
                ps.executeUpdate();

                int idFactura;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new IllegalStateException("No se genero id_factura.");
                    idFactura = keys.getInt(1);
                }

                insertarDetalles(con, idFactura, factura);
                actualizarVentaFacturada(con, factura.getIdVenta(), idFactura);
                con.commit();
                return idFactura;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void insertarDetalles(Connection con, int idFactura, Factura factura) throws Exception {
        String sql = """
                INSERT INTO factura_detalle (
                    id_factura, id_producto, descripcion, cantidad, precio_unitario, subtotal,
                    impuesto_clave, impuesto_tipo, impuesto_tasa, impuesto_importe, total_linea
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (FacturaDetalle detalle : factura.getDetalles()) {
                ps.setInt(1, idFactura);
                if (detalle.getIdProducto() == null) ps.setNull(2, java.sql.Types.INTEGER);
                else ps.setInt(2, detalle.getIdProducto());
                ps.setString(3, detalle.getDescripcion());
                ps.setBigDecimal(4, detalle.getCantidad());
                ps.setBigDecimal(5, detalle.getPrecioUnitario());
                ps.setBigDecimal(6, detalle.getSubtotal());
                ps.setString(7, detalle.getImpuestoClave());
                ps.setString(8, detalle.getImpuestoTipo());
                ps.setBigDecimal(9, detalle.getImpuestoTasa());
                ps.setBigDecimal(10, detalle.getImpuestoImporte());
                ps.setBigDecimal(11, detalle.getTotalLinea());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void actualizarVentaFacturada(Connection con, int idVenta, int idFactura) throws Exception {
        if (!columnaExiste(con, "ventas", "factura_id") || !columnaExiste(con, "ventas", "estado_facturacion")) {
            return;
        }
        String sql = "UPDATE ventas SET estado_facturacion = 'FACTURADA', factura_id = ? WHERE id_venta = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            ps.setInt(2, idVenta);
            ps.executeUpdate();
        }
    }

    private boolean columnaExiste(Connection con, String tabla, String columna) throws Exception {
        DatabaseMetaData meta = con.getMetaData();
        try (ResultSet rs = meta.getColumns(con.getCatalog(), null, tabla, columna)) {
            return rs.next();
        }
    }
}
