package org.example.modelo;

public class ConfiguracionFiscal {

    private int idConfig = 1;
    private String rfcNegocio = "";
    private String razonSocial = "";
    private String regimenFiscal = "601 - General de Ley Personas Morales";
    private String codigoPostalFiscal = "";
    private String regionFiscal = "GENERAL";
    private boolean precioIncluyeImpuesto = true;
    private boolean impuestoPorProducto = true;
    private boolean mostrarDesgloseTicket = true;
    private String impuestoPredeterminadoClave = "IVA_16";
    private String serieFactura = "A";
    private int folioInicial = 1;
    private String modoFacturacion = "PREFACTURA";
    private String usoCfdiDefault = "G03 - Gastos en general";
    private String metodoPagoSat = "PUE - Pago en una sola exhibicion";
    private String formaPagoSat = "01 - Efectivo";

    public int getIdConfig() {
        return idConfig;
    }

    public void setIdConfig(int idConfig) {
        this.idConfig = idConfig;
    }

    public String getRfcNegocio() {
        return rfcNegocio;
    }

    public void setRfcNegocio(String rfcNegocio) {
        this.rfcNegocio = limpiar(rfcNegocio);
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = limpiar(razonSocial);
    }

    public String getRegimenFiscal() {
        return regimenFiscal;
    }

    public void setRegimenFiscal(String regimenFiscal) {
        this.regimenFiscal = limpiar(regimenFiscal);
    }

    public String getCodigoPostalFiscal() {
        return codigoPostalFiscal;
    }

    public void setCodigoPostalFiscal(String codigoPostalFiscal) {
        this.codigoPostalFiscal = limpiar(codigoPostalFiscal);
    }

    public String getRegionFiscal() {
        return regionFiscal;
    }

    public void setRegionFiscal(String regionFiscal) {
        this.regionFiscal = limpiar(regionFiscal);
    }

    public boolean isPrecioIncluyeImpuesto() {
        return precioIncluyeImpuesto;
    }

    public void setPrecioIncluyeImpuesto(boolean precioIncluyeImpuesto) {
        this.precioIncluyeImpuesto = precioIncluyeImpuesto;
    }

    public boolean isImpuestoPorProducto() {
        return impuestoPorProducto;
    }

    public void setImpuestoPorProducto(boolean impuestoPorProducto) {
        this.impuestoPorProducto = impuestoPorProducto;
    }

    public boolean isMostrarDesgloseTicket() {
        return mostrarDesgloseTicket;
    }

    public void setMostrarDesgloseTicket(boolean mostrarDesgloseTicket) {
        this.mostrarDesgloseTicket = mostrarDesgloseTicket;
    }

    public String getImpuestoPredeterminadoClave() {
        return impuestoPredeterminadoClave;
    }

    public void setImpuestoPredeterminadoClave(String impuestoPredeterminadoClave) {
        this.impuestoPredeterminadoClave = limpiar(impuestoPredeterminadoClave);
    }

    public String getSerieFactura() {
        return serieFactura;
    }

    public void setSerieFactura(String serieFactura) {
        this.serieFactura = limpiar(serieFactura);
    }

    public int getFolioInicial() {
        return folioInicial;
    }

    public void setFolioInicial(int folioInicial) {
        this.folioInicial = Math.max(1, folioInicial);
    }

    public String getModoFacturacion() {
        return modoFacturacion;
    }

    public void setModoFacturacion(String modoFacturacion) {
        this.modoFacturacion = limpiar(modoFacturacion);
    }

    public String getUsoCfdiDefault() {
        return usoCfdiDefault;
    }

    public void setUsoCfdiDefault(String usoCfdiDefault) {
        this.usoCfdiDefault = limpiar(usoCfdiDefault);
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

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
