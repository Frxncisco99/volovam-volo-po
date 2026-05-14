package org.example.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CorteCajaReporte {

    private int idCaja;
    private String folio;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private String cajero;
    private String estado;
    private double fondoInicial;
    private double totalVendido;
    private int cantidadTickets;
    private double promedioTicket;
    private double totalEntradas;
    private double totalSalidas;
    private double efectivoEsperado;
    private double efectivoContado;
    private double diferencia;
    private double totalCancelado;
    private int cantidadCancelaciones;
    private double subtotal;
    private double iva;
    private double totalConImpuestos;
    private double ingresos;
    private double costos;
    private double utilidad;

    private final List<MetodoPago> metodosPago = new ArrayList<>();
    private final List<MovimientoCaja> movimientos = new ArrayList<>();
    private final List<CancelacionDevolucion> cancelacionesDevoluciones = new ArrayList<>();
    private final List<ProductoVendido> productosMasVendidos = new ArrayList<>();
    private final List<HistorialCorte> historial = new ArrayList<>();

    public int getIdCaja() { return idCaja; }
    public void setIdCaja(int idCaja) { this.idCaja = idCaja; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public String getCajero() { return cajero; }
    public void setCajero(String cajero) { this.cajero = cajero; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public double getFondoInicial() { return fondoInicial; }
    public void setFondoInicial(double fondoInicial) { this.fondoInicial = fondoInicial; }
    public double getTotalVendido() { return totalVendido; }
    public void setTotalVendido(double totalVendido) { this.totalVendido = totalVendido; }
    public int getCantidadTickets() { return cantidadTickets; }
    public void setCantidadTickets(int cantidadTickets) { this.cantidadTickets = cantidadTickets; }
    public double getPromedioTicket() { return promedioTicket; }
    public void setPromedioTicket(double promedioTicket) { this.promedioTicket = promedioTicket; }
    public double getTotalEntradas() { return totalEntradas; }
    public void setTotalEntradas(double totalEntradas) { this.totalEntradas = totalEntradas; }
    public double getTotalSalidas() { return totalSalidas; }
    public void setTotalSalidas(double totalSalidas) { this.totalSalidas = totalSalidas; }
    public double getEfectivoEsperado() { return efectivoEsperado; }
    public void setEfectivoEsperado(double efectivoEsperado) { this.efectivoEsperado = efectivoEsperado; }
    public double getEfectivoContado() { return efectivoContado; }
    public void setEfectivoContado(double efectivoContado) { this.efectivoContado = efectivoContado; }
    public double getDiferencia() { return diferencia; }
    public void setDiferencia(double diferencia) { this.diferencia = diferencia; }
    public double getTotalCancelado() { return totalCancelado; }
    public void setTotalCancelado(double totalCancelado) { this.totalCancelado = totalCancelado; }
    public int getCantidadCancelaciones() { return cantidadCancelaciones; }
    public void setCantidadCancelaciones(int cantidadCancelaciones) { this.cantidadCancelaciones = cantidadCancelaciones; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }
    public double getTotalConImpuestos() { return totalConImpuestos; }
    public void setTotalConImpuestos(double totalConImpuestos) { this.totalConImpuestos = totalConImpuestos; }
    public double getIngresos() { return ingresos; }
    public void setIngresos(double ingresos) { this.ingresos = ingresos; }
    public double getCostos() { return costos; }
    public void setCostos(double costos) { this.costos = costos; }
    public double getUtilidad() { return utilidad; }
    public void setUtilidad(double utilidad) { this.utilidad = utilidad; }
    public List<MetodoPago> getMetodosPago() { return metodosPago; }
    public List<MovimientoCaja> getMovimientos() { return movimientos; }
    public List<CancelacionDevolucion> getCancelacionesDevoluciones() { return cancelacionesDevoluciones; }
    public List<ProductoVendido> getProductosMasVendidos() { return productosMasVendidos; }
    public List<HistorialCorte> getHistorial() { return historial; }

    public static class MetodoPago {
        private final String metodo;
        private final int cantidad;
        private final double total;

        public MetodoPago(String metodo, int cantidad, double total) {
            this.metodo = metodo;
            this.cantidad = cantidad;
            this.total = total;
        }

        public String getMetodo() { return metodo; }
        public int getCantidad() { return cantidad; }
        public double getTotal() { return total; }
    }

    public static class MovimientoCaja {
        private final String tipo;
        private final String concepto;
        private final double monto;
        private final LocalDateTime fecha;
        private final String usuario;

        public MovimientoCaja(String tipo, String concepto, double monto, LocalDateTime fecha, String usuario) {
            this.tipo = tipo;
            this.concepto = concepto;
            this.monto = monto;
            this.fecha = fecha;
            this.usuario = usuario;
        }

        public String getTipo() { return tipo; }
        public String getConcepto() { return concepto; }
        public double getMonto() { return monto; }
        public LocalDateTime getFecha() { return fecha; }
        public String getUsuario() { return usuario; }
    }

    public static class CancelacionDevolucion {
        private final String tipo;
        private final String folio;
        private final double total;
        private final String motivo;
        private final String usuarioAutorizo;

        public CancelacionDevolucion(String tipo, String folio, double total, String motivo, String usuarioAutorizo) {
            this.tipo = tipo;
            this.folio = folio;
            this.total = total;
            this.motivo = motivo;
            this.usuarioAutorizo = usuarioAutorizo;
        }

        public String getTipo() { return tipo; }
        public String getFolio() { return folio; }
        public double getTotal() { return total; }
        public String getMotivo() { return motivo; }
        public String getUsuarioAutorizo() { return usuarioAutorizo; }
    }

    public static class ProductoVendido {
        private final String producto;
        private final int cantidad;
        private final double ingresos;
        private final double costo;
        private final double utilidad;

        public ProductoVendido(String producto, int cantidad, double ingresos, double costo, double utilidad) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.ingresos = ingresos;
            this.costo = costo;
            this.utilidad = utilidad;
        }

        public String getProducto() { return producto; }
        public int getCantidad() { return cantidad; }
        public double getIngresos() { return ingresos; }
        public double getCosto() { return costo; }
        public double getUtilidad() { return utilidad; }
    }

    public static class HistorialCorte {
        private final String folio;
        private final String cajero;
        private final int idCaja;
        private final LocalDateTime apertura;
        private final LocalDateTime cierre;
        private final double ventas;
        private final double esperado;
        private final double contado;
        private final double diferencia;
        private final String estado;

        public HistorialCorte(String folio, String cajero, int idCaja, LocalDateTime apertura,
                              LocalDateTime cierre, double ventas, double esperado,
                              double contado, double diferencia, String estado) {
            this.folio = folio;
            this.cajero = cajero;
            this.idCaja = idCaja;
            this.apertura = apertura;
            this.cierre = cierre;
            this.ventas = ventas;
            this.esperado = esperado;
            this.contado = contado;
            this.diferencia = diferencia;
            this.estado = estado;
        }

        public String getFolio() { return folio; }
        public String getCajero() { return cajero; }
        public int getIdCaja() { return idCaja; }
        public LocalDateTime getApertura() { return apertura; }
        public LocalDateTime getCierre() { return cierre; }
        public double getVentas() { return ventas; }
        public double getEsperado() { return esperado; }
        public double getContado() { return contado; }
        public double getDiferencia() { return diferencia; }
        public String getEstado() { return estado; }
    }

    public static class FiltroHistorial {
        private final LocalDate inicio;
        private final LocalDate fin;
        private final int limite;

        public FiltroHistorial(LocalDate inicio, LocalDate fin, int limite) {
            this.inicio = inicio;
            this.fin = fin;
            this.limite = limite;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFin() { return fin; }
        public int getLimite() { return limite; }
    }
}
