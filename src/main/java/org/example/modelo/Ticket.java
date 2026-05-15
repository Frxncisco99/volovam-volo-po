package org.example.modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo que representa un ticket de venta imprimible.
 *
 * FASE 1 — campos agregados:
 *   metodoPago    → "Efectivo" / "Tarjeta" / "Transferencia" / etc.
 *   estado        → "Completada" / "Cancelada"
 *   nombreCliente → nombre del cliente si aplica, null = público general
 *   descuento     → monto descontado en la venta (0.0 si no hay)
 *
 * Compatibilidad: los constructores originales NO se modificaron.
 * Los campos nuevos se inicializan con valores seguros por defecto.
 */
public class Ticket {

    private int           idVenta;
    private LocalDateTime fechaHora;
    private String        nombreCajero;
    private List<LineaTicket> lineas;
    private double        subtotal;
    private double        total;
    private double        montoRecibido;
    private double        cambio;
    private int           numeroCaja;

    // ── Campos nuevos (FASE 1) ─────────────────────────────────────────────────
    private String metodoPago    = "Efectivo";   // default seguro
    private String estado        = "Completada"; // default seguro
    private String nombreCliente = null;         // null = público general
    private double descuento     = 0.0;
    private double iva           = 0.0;
    private double ieps          = 0.0;
    private double impuestos     = 0.0;
    private double totalGravado  = 0.0;
    private double totalExento   = 0.0;
    private double totalTasa0    = 0.0;

    // ── Constructores ──────────────────────────────────────────────────────────

    public Ticket() {}

    /**
     * Constructor original — no modificado para no romper código existente.
     * metodoPago, estado, nombreCliente y descuento quedan en sus defaults.
     */
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

        this.subtotal = lineas != null
                ? lineas.stream().mapToDouble(LineaTicket::getSubtotal).sum()
                : 0.0;
    }

    /**
     * Constructor completo con todos los campos nuevos.
     * Úsalo desde PagoController cuando tengas los datos completos.
     */
    public Ticket(int idVenta, LocalDateTime fechaHora, String nombreCajero,
                  List<LineaTicket> lineas, double total, double montoRecibido,
                  double cambio, int numeroCaja,
                  String metodoPago, String estado,
                  String nombreCliente, double descuento) {
        this(idVenta, fechaHora, nombreCajero, lineas, total, montoRecibido, cambio, numeroCaja);
        this.metodoPago    = metodoPago    != null ? metodoPago    : "Efectivo";
        this.estado        = estado        != null ? estado        : "Completada";
        this.nombreCliente = nombreCliente;
        this.descuento     = descuento;
    }

    // ── Getters y Setters originales (sin cambios) ─────────────────────────────

    public int getIdVenta()                             { return idVenta; }
    public void setIdVenta(int idVenta)                 { this.idVenta = idVenta; }

    public LocalDateTime getFechaHora()                 { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora)   { this.fechaHora = fechaHora; }

    public String getNombreCajero()                     { return nombreCajero; }
    public void setNombreCajero(String n)               { this.nombreCajero = n; }

    public List<LineaTicket> getLineas()                { return lineas; }
    public void setLineas(List<LineaTicket> lineas)     {
        this.lineas = lineas;
        this.subtotal = lineas != null
                ? lineas.stream().mapToDouble(LineaTicket::getSubtotal).sum()
                : 0.0;
    }

    public double getSubtotal()                         { return subtotal; }
    public void setSubtotal(double subtotal)            { this.subtotal = subtotal; }

    public double getTotal()                            { return total; }
    public void setTotal(double total)                  { this.total = total; }

    public double getMontoRecibido()                    { return montoRecibido; }
    public void setMontoRecibido(double m)              { this.montoRecibido = m; }

    public double getCambio()                           { return cambio; }
    public void setCambio(double cambio)                { this.cambio = cambio; }

    public int getNumeroCaja()                          { return numeroCaja; }
    public void setNumeroCaja(int numeroCaja)           { this.numeroCaja = numeroCaja; }

    // ── Getters y Setters nuevos (FASE 1) ─────────────────────────────────────

    public String getMetodoPago()                       { return metodoPago; }
    public void setMetodoPago(String metodoPago)        {
        this.metodoPago = metodoPago != null ? metodoPago : "Efectivo";
    }

    public String getEstado()                           { return estado; }
    public void setEstado(String estado)                {
        this.estado = estado != null ? estado : "Completada";
    }

    /** Null = público general. Verificar con getNombreClienteDisplay() para UI. */
    public String getNombreCliente()                    { return nombreCliente; }
    public void setNombreCliente(String nombreCliente)  { this.nombreCliente = nombreCliente; }

    /** Nunca devuelve null — usa "Público general" como fallback para UI. */
    public String getNombreClienteDisplay() {
        return nombreCliente != null && !nombreCliente.isBlank()
                ? nombreCliente
                : "Público general";
    }

    public double getDescuento()                        { return descuento; }
    public void setDescuento(double descuento)          { this.descuento = Math.max(0, descuento); }

    public double getIva()                              { return iva; }
    public void setIva(double iva)                      { this.iva = Math.max(0, iva); }

    public double getIeps()                             { return ieps; }
    public void setIeps(double ieps)                    { this.ieps = Math.max(0, ieps); }

    public double getImpuestos()                        { return impuestos; }
    public void setImpuestos(double impuestos)          { this.impuestos = Math.max(0, impuestos); }

    public double getTotalGravado()                     { return totalGravado; }
    public void setTotalGravado(double totalGravado)    { this.totalGravado = Math.max(0, totalGravado); }

    public double getTotalExento()                      { return totalExento; }
    public void setTotalExento(double totalExento)      { this.totalExento = Math.max(0, totalExento); }

    public double getTotalTasa0()                       { return totalTasa0; }
    public void setTotalTasa0(double totalTasa0)        { this.totalTasa0 = Math.max(0, totalTasa0); }

    /** true si la venta fue cancelada. Útil para colorear filas en tablas. */
    public boolean isCancelada() {
        return "Cancelada".equalsIgnoreCase(estado);
    }

    // ── Clase interna: una línea del ticket ────────────────────────────────────

    public static class LineaTicket {

        private String nombreProducto;
        private int    cantidad;
        private double precioUnitario;
        private double costoUnitario;
        private double subtotal;
        private String impuestoClave;
        private String impuestoNombre;
        private String impuestoTipo;
        private double impuestoTasa;
        private double impuestoImporte;
        private double subtotalSinImpuesto;

        public LineaTicket() {}

        /** Constructor original — costoUnitario queda en 0.0 para no romper código existente. */
        public LineaTicket(String nombreProducto, int cantidad, double precioUnitario) {
            this(nombreProducto, cantidad, precioUnitario, 0.0);
        }

        /** Constructor completo con costo unitario. */
        public LineaTicket(String nombreProducto, int cantidad,
                           double precioUnitario, double costoUnitario) {
            this.nombreProducto = nombreProducto;
            this.cantidad       = cantidad;
            this.precioUnitario = precioUnitario;
            this.costoUnitario  = costoUnitario;
            this.subtotal       = precioUnitario * cantidad;
        }

        public String getNombreProducto()                    { return nombreProducto; }
        public void setNombreProducto(String n)              { this.nombreProducto = n; }

        public int getCantidad()                             { return cantidad; }
        public void setCantidad(int cantidad)                { this.cantidad = cantidad; }

        public double getPrecioUnitario()                    { return precioUnitario; }
        public void setPrecioUnitario(double precioUnitario) {
            this.precioUnitario = precioUnitario;
            this.subtotal = precioUnitario * cantidad;
        }

        public double getCostoUnitario()                     { return costoUnitario; }
        public void setCostoUnitario(double costoUnitario)   { this.costoUnitario = costoUnitario; }

        public double getSubtotal()                          { return subtotal; }
        public void setSubtotal(double subtotal)             { this.subtotal = subtotal; }

        public String getImpuestoClave()                     { return impuestoClave; }
        public void setImpuestoClave(String impuestoClave)   { this.impuestoClave = impuestoClave; }

        public String getImpuestoNombre()                    { return impuestoNombre; }
        public void setImpuestoNombre(String impuestoNombre) { this.impuestoNombre = impuestoNombre; }

        public String getImpuestoTipo()                      { return impuestoTipo; }
        public void setImpuestoTipo(String impuestoTipo)     { this.impuestoTipo = impuestoTipo; }

        public double getImpuestoTasa()                      { return impuestoTasa; }
        public void setImpuestoTasa(double impuestoTasa)     { this.impuestoTasa = Math.max(0, impuestoTasa); }

        public double getImpuestoImporte()                   { return impuestoImporte; }
        public void setImpuestoImporte(double importe)       { this.impuestoImporte = Math.max(0, importe); }

        public double getSubtotalSinImpuesto()               { return subtotalSinImpuesto; }
        public void setSubtotalSinImpuesto(double subtotal)  { this.subtotalSinImpuesto = Math.max(0, subtotal); }

        public String getImpuestoDisplay() {
            if (impuestoNombre == null || impuestoNombre.isBlank() || "SIN_IMPUESTO".equalsIgnoreCase(impuestoTipo)) {
                return "";
            }
            if (impuestoTasa > 0) {
                return impuestoNombre + " " + String.format("%.2f%%", impuestoTasa * 100);
            }
            return impuestoNombre;
        }

        /**
         * Ganancia bruta de esta línea.
         * Solo es precisa si costoUnitario fue cargado desde BD.
         * Retorna 0 si costoUnitario no está disponible.
         */
        public double getGananciaBruta() {
            return (precioUnitario - costoUnitario) * cantidad;
        }
    }
}
