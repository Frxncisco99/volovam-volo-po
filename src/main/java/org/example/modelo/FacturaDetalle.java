package org.example.modelo;

import java.math.BigDecimal;

public class FacturaDetalle {

    private int idDetalle;
    private int idFactura;
    private Integer idProducto;
    private String descripcion;
    private BigDecimal cantidad = BigDecimal.ZERO;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal descuento = BigDecimal.ZERO;
    private String impuestoClave;
    private String impuestoTipo;
    private BigDecimal impuestoTasa = BigDecimal.ZERO;
    private BigDecimal impuestoImporte = BigDecimal.ZERO;
    private BigDecimal totalLinea = BigDecimal.ZERO;

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal == null ? BigDecimal.ZERO : subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento == null ? BigDecimal.ZERO : descuento;
    }

    public String getImpuestoClave() {
        return impuestoClave;
    }

    public void setImpuestoClave(String impuestoClave) {
        this.impuestoClave = impuestoClave;
    }

    public String getImpuestoTipo() {
        return impuestoTipo;
    }

    public void setImpuestoTipo(String impuestoTipo) {
        this.impuestoTipo = impuestoTipo;
    }

    public BigDecimal getImpuestoTasa() {
        return impuestoTasa;
    }

    public void setImpuestoTasa(BigDecimal impuestoTasa) {
        this.impuestoTasa = impuestoTasa == null ? BigDecimal.ZERO : impuestoTasa;
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
}
