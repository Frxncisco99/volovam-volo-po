package org.example.modelo;

import java.math.BigDecimal;

public class LineaCalculoFiscal {

    private int idProducto;
    private String descripcion;
    private BigDecimal cantidad = BigDecimal.ZERO;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private BigDecimal subtotalSinImpuesto = BigDecimal.ZERO;
    private BigDecimal descuento = BigDecimal.ZERO;
    private BigDecimal impuestoImporte = BigDecimal.ZERO;
    private BigDecimal totalLinea = BigDecimal.ZERO;
    private Impuesto impuesto = Impuesto.sinImpuesto();

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad == null ? BigDecimal.ZERO : cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario == null ? BigDecimal.ZERO : precioUnitario;
    }

    public BigDecimal getSubtotalSinImpuesto() {
        return subtotalSinImpuesto;
    }

    public void setSubtotalSinImpuesto(BigDecimal subtotalSinImpuesto) {
        this.subtotalSinImpuesto = subtotalSinImpuesto == null ? BigDecimal.ZERO : subtotalSinImpuesto;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento == null ? BigDecimal.ZERO : descuento;
    }

    public BigDecimal getImpuestoImporte() {
        return impuestoImporte;
    }

    public void setImpuestoImporte(BigDecimal impuestoImporte) {
        this.impuestoImporte = impuestoImporte == null ? BigDecimal.ZERO : impuestoImporte;
    }

    public BigDecimal getTotalLinea() {
        return totalLinea;
    }

    public void setTotalLinea(BigDecimal totalLinea) {
        this.totalLinea = totalLinea == null ? BigDecimal.ZERO : totalLinea;
    }

    public Impuesto getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(Impuesto impuesto) {
        this.impuesto = impuesto == null ? Impuesto.sinImpuesto() : impuesto;
    }
}
