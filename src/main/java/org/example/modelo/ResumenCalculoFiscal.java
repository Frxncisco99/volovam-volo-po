package org.example.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResumenCalculoFiscal {

    private final List<LineaCalculoFiscal> lineas = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal descuento = BigDecimal.ZERO;
    private BigDecimal iva = BigDecimal.ZERO;
    private BigDecimal ieps = BigDecimal.ZERO;
    private BigDecimal impuestos = BigDecimal.ZERO;
    private BigDecimal totalGravado = BigDecimal.ZERO;
    private BigDecimal totalExento = BigDecimal.ZERO;
    private BigDecimal totalTasa0 = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    public void agregarLinea(LineaCalculoFiscal linea) {
        if (linea == null) return;
        lineas.add(linea);
        subtotal = subtotal.add(linea.getSubtotalSinImpuesto());
        descuento = descuento.add(linea.getDescuento());
        impuestos = impuestos.add(linea.getImpuestoImporte());
        total = total.add(linea.getTotalLinea());

        String tipo = linea.getImpuesto().getTipo();
        if ("IVA".equalsIgnoreCase(tipo)) {
            iva = iva.add(linea.getImpuestoImporte());
            totalGravado = totalGravado.add(linea.getSubtotalSinImpuesto());
        } else if ("IEPS".equalsIgnoreCase(tipo)) {
            ieps = ieps.add(linea.getImpuestoImporte());
            totalGravado = totalGravado.add(linea.getSubtotalSinImpuesto());
        } else if ("TASA_0".equalsIgnoreCase(tipo)) {
            totalTasa0 = totalTasa0.add(linea.getSubtotalSinImpuesto());
        } else {
            totalExento = totalExento.add(linea.getSubtotalSinImpuesto());
        }
    }

    public List<LineaCalculoFiscal> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public BigDecimal getIeps() {
        return ieps;
    }

    public BigDecimal getImpuestos() {
        return impuestos;
    }

    public BigDecimal getTotalGravado() {
        return totalGravado;
    }

    public BigDecimal getTotalExento() {
        return totalExento;
    }

    public BigDecimal getTotalTasa0() {
        return totalTasa0;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
