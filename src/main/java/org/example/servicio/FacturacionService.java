package org.example.servicio;

import org.example.dao.ConexionDB;
import org.example.dao.FacturacionDAO;
import org.example.dao.FiscalDAO;
import org.example.modelo.ConfiguracionFiscal;
import org.example.modelo.Factura;
import org.example.modelo.FacturaDetalle;
import org.example.modelo.LineaCalculoFiscal;
import org.example.modelo.ResumenCalculoFiscal;
import org.example.modelo.SesionUsuario;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FacturacionService {

    private final FacturacionDAO facturacionDAO = new FacturacionDAO();
    private final FiscalDAO fiscalDAO = new FiscalDAO();
    private final ImpuestoService impuestoService = new ImpuestoService();

    public int generarPrefactura(int idVenta) throws Exception {
        Integer existente = facturacionDAO.obtenerFacturaPorVenta(idVenta);
        if (existente != null) return existente;
        ConfiguracionFiscal config = fiscalDAO.obtenerConfiguracionFiscal();
        Factura factura = construirPrefactura(idVenta, config);
        int idFactura = facturacionDAO.crearPrefactura(factura);
        fiscalDAO.registrarAuditoriaFiscal(
                "PREFACTURA_GENERADA",
                "facturas",
                idFactura,
                "Prefactura " + factura.getSerie() + "-" + factura.getFolio() + " ligada a venta " + idVenta,
                SesionUsuario.getInstancia().getIdUsuario()
        );
        return idFactura;
    }

    private Factura construirPrefactura(int idVenta, ConfiguracionFiscal config) throws Exception {
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) throw new IllegalStateException("No hay conexion a la base de datos.");

            Factura factura = cargarVentaBase(con, idVenta, config);
            int folio = facturacionDAO.obtenerSiguienteFolio(con, config.getSerieFactura(), config.getFolioInicial());
            factura.setSerie(config.getSerieFactura());
            factura.setFolio(folio);
            factura.setFolioInterno(String.format("%s-%06d", factura.getSerie(), folio));
            factura.setEstado("PENDIENTE");
            factura.setModo(config.getModoFacturacion().isBlank() ? "PREFACTURA" : config.getModoFacturacion());

            cargarDetalles(con, idVenta, factura);
            aplicarTotales(factura);
            return factura;
        }
    }

    private Factura cargarVentaBase(Connection con, int idVenta, ConfiguracionFiscal config) throws Exception {
        String sql = """
                SELECT v.id_venta, v.total, v.subtotal, v.descuento, v.iva, v.ieps, v.impuestos,
                       v.total_gravado, v.total_exento, v.total_tasa0,
                       c.rfc, c.razon_social, c.regimen_fiscal, c.uso_cfdi_default,
                       c.codigo_postal_fiscal, c.correo_facturacion, c.nombre AS cliente_nombre
                FROM ventas v
                LEFT JOIN clientes c ON c.id_cliente = v.id_cliente
                WHERE v.id_venta = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new IllegalArgumentException("No existe la venta " + idVenta + ".");

            Factura factura = new Factura();
            factura.setIdVenta(idVenta);
            factura.setRfcEmisor(config.getRfcNegocio());
            factura.setRazonSocialEmisor(config.getRazonSocial());
            factura.setRegimenFiscalEmisor(config.getRegimenFiscal());
            factura.setCodigoPostalEmisor(config.getCodigoPostalFiscal());
            factura.setRfcReceptor(valor(rs.getString("rfc"), "XAXX010101000"));
            factura.setRazonSocialReceptor(valor(rs.getString("razon_social"), valor(rs.getString("cliente_nombre"), "Publico General")));
            factura.setRegimenFiscalReceptor(valor(rs.getString("regimen_fiscal"), ""));
            factura.setCodigoPostalReceptor(valor(rs.getString("codigo_postal_fiscal"), ""));
            factura.setUsoCfdi(valor(rs.getString("uso_cfdi_default"), config.getUsoCfdiDefault()));
            factura.setMetodoPagoSat(config.getMetodoPagoSat());
            factura.setFormaPagoSat(config.getFormaPagoSat());
            factura.setSubtotal(rs.getBigDecimal("subtotal"));
            factura.setDescuento(rs.getBigDecimal("descuento"));
            factura.setIva(rs.getBigDecimal("iva"));
            factura.setIeps(rs.getBigDecimal("ieps"));
            factura.setImpuestos(rs.getBigDecimal("impuestos"));
            factura.setTotalGravado(rs.getBigDecimal("total_gravado"));
            factura.setTotalExento(rs.getBigDecimal("total_exento"));
            factura.setTotalTasa0(rs.getBigDecimal("total_tasa0"));
            factura.setTotal(rs.getBigDecimal("total"));
            return factura;
        }
    }

    private void cargarDetalles(Connection con, int idVenta, Factura factura) throws Exception {
        String sql = """
                SELECT dv.id_producto, p.nombre, dv.cantidad, dv.precio_unitario,
                       dv.subtotal, dv.impuesto_clave, dv.impuesto_tipo, dv.impuesto_tasa,
                       dv.impuesto_importe, dv.subtotal_sin_impuesto, dv.total_linea
                FROM detalle_venta dv
                LEFT JOIN productos p ON p.id_producto = dv.id_producto
                WHERE dv.id_venta = ?
                ORDER BY dv.id_detalle
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FacturaDetalle detalle = new FacturaDetalle();
                int idProducto = rs.getInt("id_producto");
                String descripcion = valor(rs.getString("nombre"), "Producto");
                BigDecimal cantidad = rs.getBigDecimal("cantidad");
                BigDecimal precio = rs.getBigDecimal("precio_unitario");
                String clave = rs.getString("impuesto_clave");

                if (clave == null || clave.isBlank()) {
                    LineaCalculoFiscal linea = impuestoService.calcularLinea(idProducto, descripcion, cantidad, precio, BigDecimal.ZERO);
                    detalle.setIdProducto(idProducto);
                    detalle.setDescripcion(descripcion);
                    detalle.setCantidad(linea.getCantidad());
                    detalle.setPrecioUnitario(linea.getPrecioUnitario());
                    detalle.setSubtotal(linea.getSubtotalSinImpuesto());
                    detalle.setImpuestoClave(linea.getImpuesto().getClave());
                    detalle.setImpuestoTipo(linea.getImpuesto().getTipo());
                    detalle.setImpuestoTasa(linea.getImpuesto().getTasa());
                    detalle.setImpuestoImporte(linea.getImpuestoImporte());
                    detalle.setTotalLinea(linea.getTotalLinea());
                } else {
                    detalle.setIdProducto(idProducto);
                    detalle.setDescripcion(descripcion);
                    detalle.setCantidad(cantidad);
                    detalle.setPrecioUnitario(precio);
                    detalle.setSubtotal(rs.getBigDecimal("subtotal_sin_impuesto"));
                    detalle.setImpuestoClave(clave);
                    detalle.setImpuestoTipo(rs.getString("impuesto_tipo"));
                    detalle.setImpuestoTasa(rs.getBigDecimal("impuesto_tasa"));
                    detalle.setImpuestoImporte(rs.getBigDecimal("impuesto_importe"));
                    detalle.setTotalLinea(rs.getBigDecimal("total_linea"));
                }
                factura.getDetalles().add(detalle);
            }
        }
    }

    private void aplicarTotales(Factura factura) {
        if (!factura.getDetalles().isEmpty() && factura.getImpuestos().compareTo(BigDecimal.ZERO) == 0) {
            ResumenCalculoFiscal resumen = new ResumenCalculoFiscal();
            for (FacturaDetalle detalle : factura.getDetalles()) {
                LineaCalculoFiscal linea = new LineaCalculoFiscal();
                linea.setIdProducto(detalle.getIdProducto() == null ? 0 : detalle.getIdProducto());
                linea.setDescripcion(detalle.getDescripcion());
                linea.setCantidad(detalle.getCantidad());
                linea.setPrecioUnitario(detalle.getPrecioUnitario());
                linea.setSubtotalSinImpuesto(detalle.getSubtotal());
                linea.setImpuestoImporte(detalle.getImpuestoImporte());
                linea.setTotalLinea(detalle.getTotalLinea());
                fiscalDAO.obtenerImpuestoPorClave(detalle.getImpuestoClave()).ifPresent(linea::setImpuesto);
                resumen.agregarLinea(linea);
            }
            factura.setSubtotal(resumen.getSubtotal());
            factura.setDescuento(resumen.getDescuento());
            factura.setIva(resumen.getIva());
            factura.setIeps(resumen.getIeps());
            factura.setImpuestos(resumen.getImpuestos());
            factura.setTotalGravado(resumen.getTotalGravado());
            factura.setTotalExento(resumen.getTotalExento());
            factura.setTotalTasa0(resumen.getTotalTasa0());
            factura.setTotal(resumen.getTotal());
        }
    }

    private String valor(String valor, String fallback) {
        return valor == null || valor.isBlank() ? fallback : valor.trim();
    }
}
