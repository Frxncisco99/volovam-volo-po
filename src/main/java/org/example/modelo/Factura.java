package org.example.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Factura {

    private int idFactura;
    private int idVenta;
    private String serie = "A";
    private int folio;
    private String folioInterno;
    private String estado = "PENDIENTE";
    private String modo = "PREFACTURA";
    private String uuid;
    private String rfcEmisor = "";
    private String razonSocialEmisor = "";
    private String regimenFiscalEmisor = "";
    private String codigoPostalEmisor = "";
    private String rfcReceptor = "XAXX010101000";
    private String razonSocialReceptor = "Publico General";
    private String regimenFiscalReceptor = "";
    private String codigoPostalReceptor = "";
    private String usoCfdi = "";
    private String metodoPagoSat = "";
    private String formaPagoSat = "";
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal descuento = BigDecimal.ZERO;
    private BigDecimal iva = BigDecimal.ZERO;
    private BigDecimal ieps = BigDecimal.ZERO;
    private BigDecimal impuestos = BigDecimal.ZERO;
    private BigDecimal totalGravado = BigDecimal.ZERO;
    private BigDecimal totalExento = BigDecimal.ZERO;
    private BigDecimal totalTasa0 = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private LocalDateTime fecha;
    private final List<FacturaDetalle> detalles = new ArrayList<>();

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = limpiar(serie);
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public String getFolioInterno() {
        return folioInterno;
    }

    public void setFolioInterno(String folioInterno) {
        this.folioInterno = limpiar(folioInterno);
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = limpiar(estado);
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = limpiar(modo);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRfcEmisor() {
        return rfcEmisor;
    }

    public void setRfcEmisor(String rfcEmisor) {
        this.rfcEmisor = limpiar(rfcEmisor);
    }

    public String getRazonSocialEmisor() {
        return razonSocialEmisor;
    }

    public void setRazonSocialEmisor(String razonSocialEmisor) {
        this.razonSocialEmisor = limpiar(razonSocialEmisor);
    }

    public String getRegimenFiscalEmisor() {
        return regimenFiscalEmisor;
    }

    public void setRegimenFiscalEmisor(String regimenFiscalEmisor) {
        this.regimenFiscalEmisor = limpiar(regimenFiscalEmisor);
    }

    public String getCodigoPostalEmisor() {
        return codigoPostalEmisor;
    }

    public void setCodigoPostalEmisor(String codigoPostalEmisor) {
        this.codigoPostalEmisor = limpiar(codigoPostalEmisor);
    }

    public String getRfcReceptor() {
        return rfcReceptor;
    }

    public void setRfcReceptor(String rfcReceptor) {
        this.rfcReceptor = limpiar(rfcReceptor);
    }

    public String getRazonSocialReceptor() {
        return razonSocialReceptor;
    }

    public void setRazonSocialReceptor(String razonSocialReceptor) {
        this.razonSocialReceptor = limpiar(razonSocialReceptor);
    }

    public String getRegimenFiscalReceptor() {
        return regimenFiscalReceptor;
    }

    public void setRegimenFiscalReceptor(String regimenFiscalReceptor) {
        this.regimenFiscalReceptor = limpiar(regimenFiscalReceptor);
    }

    public String getCodigoPostalReceptor() {
        return codigoPostalReceptor;
    }

    public void setCodigoPostalReceptor(String codigoPostalReceptor) {
        this.codigoPostalReceptor = limpiar(codigoPostalReceptor);
    }

    public String getUsoCfdi() {
        return usoCfdi;
    }

    public void setUsoCfdi(String usoCfdi) {
        this.usoCfdi = limpiar(usoCfdi);
    }

    public String getMetodoPagoSat() {
        return metodoPagoSat;
    }

    public void setMetodoPagoSat(String metodoPagoSat) {
        this.metodoPagoSat = limpiar(metodoPagoSat);
    }

    public String getFormaPagoSat() {
        return formaPagoSat;
    }

    public void setFormaPagoSat(String formaPagoSat) {
        this.formaPagoSat = limpiar(formaPagoSat);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = valor(subtotal);
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = valor(descuento);
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = valor(iva);
    }

    public BigDecimal getIeps() {
        return ieps;
    }

    public void setIeps(BigDecimal ieps) {
        this.ieps = valor(ieps);
    }

    public BigDecimal getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(BigDecimal impuestos) {
        this.impuestos = valor(impuestos);
    }

    public BigDecimal getTotalGravado() {
        return totalGravado;
    }

    public void setTotalGravado(BigDecimal totalGravado) {
        this.totalGravado = valor(totalGravado);
    }

    public BigDecimal getTotalExento() {
        return totalExento;
    }

    public void setTotalExento(BigDecimal totalExento) {
        this.totalExento = valor(totalExento);
    }

    public BigDecimal getTotalTasa0() {
        return totalTasa0;
    }

    public void setTotalTasa0(BigDecimal totalTasa0) {
        this.totalTasa0 = valor(totalTasa0);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = valor(total);
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<FacturaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<FacturaDetalle> detalles) {
        this.detalles.clear();
        if (detalles != null) this.detalles.addAll(detalles);
    }

    private BigDecimal valor(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
