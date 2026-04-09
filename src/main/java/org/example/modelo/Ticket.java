package org.example.modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo que representa un ticket de venta imprimible.
 * Se construye a partir de los datos ya guardados por PagoController.
 */
public class Ticket {

    private int idVenta;
    private LocalDateTime fechaHora;
    private String nombreCajero;
    private List<LineaTicket> lineas;
    private double subtotal;
    private double total;
    private double montoRecibido;
    private double cambio;
    private int numeroCaja;

    public Ticket() {}

    public Ticket(int idVenta, LocalDateTime fechaHora, String nombreCajero,
                  List<LineaTicket> lineas, double total,
                  double montoRecibido, double cambio, int numeroCaja) {
        this.idVenta       = idVenta;
        this.fechaHora     = fechaHora;
        this.nombreCajero  = nombreCajero;
        this.lineas        = lineas;
        this.total         = total;
        this.montoRecibido = montoRecibido;
        this.cambio        = cambio;
        this.numeroCaja    = numeroCaja;

        // Calcular subtotal sumando líneas
        this.subtotal = lineas.stream()
                .mapToDouble(LineaTicket::getSubtotal)
                .sum();
    }

    // ─── Getters y Setters ───────────────────────────────────────────────────

    public int getIdVenta()                  { return idVenta; }
    public void setIdVenta(int idVenta)      { this.idVenta = idVenta; }

    public LocalDateTime getFechaHora()                     { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora)       { this.fechaHora = fechaHora; }

    public String getNombreCajero()                         { return nombreCajero; }
    public void setNombreCajero(String nombreCajero)        { this.nombreCajero = nombreCajero; }

    public List<LineaTicket> getLineas()                    { return lineas; }
    public void setLineas(List<LineaTicket> lineas)         { this.lineas = lineas; }

    public double getSubtotal()                             { return subtotal; }
    public void setSubtotal(double subtotal)                { this.subtotal = subtotal; }

    public double getTotal()                                { return total; }
    public void setTotal(double total)                      { this.total = total; }

    public double getMontoRecibido()                        { return montoRecibido; }
    public void setMontoRecibido(double montoRecibido)      { this.montoRecibido = montoRecibido; }

    public double getCambio()                               { return cambio; }
    public void setCambio(double cambio)                    { this.cambio = cambio; }

    public int getNumeroCaja()                              { return numeroCaja; }
    public void setNumeroCaja(int numeroCaja)               { this.numeroCaja = numeroCaja; }

    // ─── Clase interna: una línea del ticket ────────────────────────────────

    public static class LineaTicket {

        private String nombreProducto;
        private int cantidad;
        private double precioUnitario;
        private double subtotal;

        public LineaTicket() {}

        public LineaTicket(String nombreProducto, int cantidad, double precioUnitario) {
            this.nombreProducto = nombreProducto;
            this.cantidad       = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal       = precioUnitario * cantidad;
        }

        public String getNombreProducto()                          { return nombreProducto; }
        public void setNombreProducto(String nombreProducto)       { this.nombreProducto = nombreProducto; }

        public int getCantidad()                                   { return cantidad; }
        public void setCantidad(int cantidad)                      { this.cantidad = cantidad; }

        public double getPrecioUnitario()                          { return precioUnitario; }
        public void setPrecioUnitario(double precioUnitario)       { this.precioUnitario = precioUnitario; }

        public double getSubtotal()                                { return subtotal; }
        public void setSubtotal(double subtotal)                   { this.subtotal = subtotal; }
    }
}