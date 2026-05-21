package org.example.servicio;

import org.example.dao.FiscalDAO;
import org.example.modelo.ConfiguracionFiscal;
import org.example.modelo.Impuesto;
import org.example.modelo.LineaCalculoFiscal;
import org.example.modelo.ResumenCalculoFiscal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class ImpuestoService {

    private static final BigDecimal CIEN = new BigDecimal("100");
    private final FiscalDAO fiscalDAO = new FiscalDAO();

    public ResumenCalculoFiscal calcularCarrito(Map<Integer, Object[]> carrito) {
        ResumenCalculoFiscal resumen = new ResumenCalculoFiscal();
        if (carrito == null || carrito.isEmpty()) return resumen;

        for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
            Object[] item = entry.getValue();
            String descripcion = item[0] == null ? "Producto" : item[0].toString();
            BigDecimal precio = toBigDecimal(item[1]);
            BigDecimal cantidad = toBigDecimal(item[2]);
            BigDecimal descuento = item.length > 3 ? toBigDecimal(item[3]) : BigDecimal.ZERO;
            resumen.agregarLinea(calcularLinea(entry.getKey(), descripcion, cantidad, precio, descuento));
        }
        return resumen;
    }

    public LineaCalculoFiscal calcularLinea(int idProducto, String descripcion, BigDecimal cantidad,
                                            BigDecimal precioUnitario, BigDecimal descuento) {
        ConfiguracionFiscal config = fiscalDAO.obtenerConfiguracionFiscal();
        Impuesto impuesto = resolverImpuesto(idProducto, config);

        BigDecimal cant = valor(cantidad);
        BigDecimal precio = valor(precioUnitario);
        BigDecimal desc = valor(descuento);
        BigDecimal bruto = precio.multiply(cant);
        BigDecimal neto = bruto.subtract(desc).max(BigDecimal.ZERO);
        BigDecimal tasa = impuesto.getTasa();
        boolean grava = esImpuestoTrasladado(impuesto) && tasa.compareTo(BigDecimal.ZERO) > 0;

        BigDecimal subtotal;
        BigDecimal impuestoImporte;
        BigDecimal totalLinea;

        if (!grava) {
            subtotal = neto;
            impuestoImporte = BigDecimal.ZERO;
            totalLinea = neto;
        } else if (config.isPrecioIncluyeImpuesto()) {
            subtotal = neto.divide(BigDecimal.ONE.add(tasa), 6, RoundingMode.HALF_UP);
            impuestoImporte = neto.subtract(subtotal);
            totalLinea = neto;
        } else {
            subtotal = neto;
            impuestoImporte = subtotal.multiply(tasa);
            totalLinea = subtotal.add(impuestoImporte);
        }

        LineaCalculoFiscal linea = new LineaCalculoFiscal();
        linea.setIdProducto(idProducto);
        linea.setDescripcion(descripcion);
        linea.setCantidad(cant.setScale(3, RoundingMode.HALF_UP));
        linea.setPrecioUnitario(precio.setScale(2, RoundingMode.HALF_UP));
        linea.setSubtotalSinImpuesto(moneda(subtotal));
        linea.setDescuento(moneda(desc));
        linea.setImpuestoImporte(moneda(impuestoImporte));
        linea.setTotalLinea(moneda(totalLinea));
        linea.setImpuesto(impuesto);
        return linea;
    }

    public BigDecimal tasaComoPorcentaje(Impuesto impuesto) {
        if (impuesto == null) return BigDecimal.ZERO;
        return impuesto.getTasa().multiply(CIEN).setScale(2, RoundingMode.HALF_UP);
    }

    private Impuesto resolverImpuesto(int idProducto, ConfiguracionFiscal config) {
        if (config.isImpuestoPorProducto()) {
            return fiscalDAO.obtenerImpuestoProducto(idProducto)
                    .orElseGet(() -> fiscalDAO.obtenerImpuestoPorClave(config.getImpuestoPredeterminadoClave())
                            .orElseGet(fiscalDAO::obtenerImpuestoPredeterminado));
        }
        return fiscalDAO.obtenerImpuestoPorClave(config.getImpuestoPredeterminadoClave())
                .orElseGet(fiscalDAO::obtenerImpuestoPredeterminado);
    }

    private boolean esImpuestoTrasladado(Impuesto impuesto) {
        String tipo = impuesto == null || impuesto.getTipo() == null ? "" : impuesto.getTipo();
        return "IVA".equalsIgnoreCase(tipo) || "IEPS".equalsIgnoreCase(tipo) || "PERSONALIZADO".equalsIgnoreCase(tipo);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        try {
            return new BigDecimal(value.toString().trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal valor(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private BigDecimal moneda(BigDecimal valor) {
        return valor(valor).setScale(2, RoundingMode.HALF_UP);
    }
}
