package org.example.servicio;

import org.example.modelo.Ticket;
import java.time.format.DateTimeFormatter;

/**
 * ══════════════════════════════════════════════════════════════════════
 *  TicketRenderer  ·  Fuente única de verdad del layout del ticket
 * ══════════════════════════════════════════════════════════════════════
 *
 *  Genera el String canónico del ticket en formato monoespaciado.
 *  Tanto la vista previa (TextArea) como la impresión ESC/POS consumen
 *  este mismo String → lo que se ve en pantalla = lo que sale impreso.
 *
 *  Ubicación: src/main/java/org/example/servicio/TicketRenderer.java
 *
 *  Callers:
 *    TicketPreviewController  → preview modal
 *    ConfiguracionController  → mini-preview inline
 *    TicketImpresora          → impresión real ESC/POS
 *    TicketService            → textoPlano() para TicketController
 */
public final class TicketRenderer {

    /** Columnas para papel 58 mm (32 chars a 12 CPI). */
    public static final int ANCHO_58MM = 32;

    /** Columnas para papel 80 mm (42 chars a 12 CPI). */
    public static final int ANCHO_80MM = 42;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    private TicketRenderer() {}

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Genera el texto completo del ticket.
     *
     * @param ticket          Datos de la venta (nunca null).
     * @param nombre          Nombre del negocio en ticket.
     * @param giro            Giro/descripción.
     * @param direccion       Dirección física.
     * @param ciudad          Ciudad / C.P.
     * @param telefono        Teléfono de contacto.
     * @param encabezado      Mensaje de bienvenida (multilinea \n permitido).
     * @param pie             Mensaje de despedida (multilinea \n permitido).
     * @param aviso           Aviso fiscal (ej. "No es comprobante fiscal").
     * @param mostrarLogo     Imprime marcador [LOGO] para el logo gráfico.
     * @param mostrarFolio    Imprime número de folio.
     * @param mostrarDesglose Imprime línea de subtotal.
     * @param mostrarQR       Imprime marcador [QR].
     * @param mostrarFecha    Imprime fecha y hora.
     * @param mostrarCajero   Imprime nombre del cajero.
     * @param ancho           Usar ANCHO_58MM (32) o ANCHO_80MM (42).
     */
    public static String generar(Ticket ticket,
                                 String nombre,    String giro,
                                 String direccion, String ciudad, String telefono,
                                 String encabezado, String pie,  String aviso,
                                 boolean mostrarLogo,   boolean mostrarFolio,
                                 boolean mostrarDesglose, boolean mostrarQR,
                                 boolean mostrarFecha, boolean mostrarCajero,
                                 int ancho) {

        final String SEP  = rep('=', ancho);
        final String SEPM = rep('-', ancho);

        // Para 58 mm: 3 columnas (nombre|cant|importe)
        // Para 80 mm: 4 columnas (nombre|cant|p.u.|subt.)
        final boolean tresCol = (ancho <= ANCHO_58MM);
        // nameWidth fijo para que las columnas numéricas siempre cuadren
        final int nw = tresCol ? (ancho - 13) : (ancho - 23);  // 19 / 19

        StringBuilder sb = new StringBuilder();

        // ── Encabezado negocio ────────────────────────────────────────────────
        if (mostrarLogo)
            sb.append(centro("[  LOGO  ]", ancho)).append('\n');

        sb.append(centro(safe(nombre, "NEGOCIO").toUpperCase(), ancho)).append('\n');
        if (!blank(giro))      sb.append(centro(giro,         ancho)).append('\n');
        if (!blank(direccion)) sb.append(centro(direccion,    ancho)).append('\n');
        if (!blank(ciudad))    sb.append(centro(ciudad,       ancho)).append('\n');
        if (!blank(telefono))  sb.append(centro("Tel: " + telefono, ancho)).append('\n');
        if (!blank(aviso))     sb.append(centro(aviso,        ancho)).append('\n');
        sb.append(SEP).append('\n');

        if (!blank(encabezado)) {
            for (String l : encabezado.split("\n"))
                sb.append(centro(l.trim(), ancho)).append('\n');
            sb.append(SEPM).append('\n');
        }

        // ── Datos de la venta ─────────────────────────────────────────────────
        if (mostrarFolio)
            sb.append("Folio : #").append(String.format("%06d", ticket.getIdVenta()))
                    .append('\n');
        if (mostrarFecha)
            sb.append("Fecha : ").append(ticket.getFechaHora().format(FMT)).append('\n');
        if (mostrarCajero)
            sb.append("Cajero: ").append(ticket.getNombreCajero()).append('\n');
        sb.append("Caja  : ").append(ticket.getNumeroCaja()).append('\n');
        sb.append(SEPM).append('\n');

        // ── Cabecera de columnas ──────────────────────────────────────────────
        if (tresCol) {
            sb.append(String.format("%-" + nw + "s %4s %7s\n",
                    "PRODUCTO", "CANT", "IMPORTE"));
        } else {
            sb.append(String.format("%-" + nw + "s %4s %8s %8s\n",
                    "PRODUCTO", "CANT", "P.U.", "SUBT."));
        }
        sb.append(SEPM).append('\n');

        // ── Líneas de producto ────────────────────────────────────────────────
        for (Ticket.LineaTicket l : ticket.getLineas()) {
            String prod  = l.getNombreProducto();
            boolean larga = prod.length() > nw;

            if (tresCol) {
                if (larga) {
                    sb.append(String.format("%-" + nw + "s\n", cortar(prod, nw)));
                    sb.append(String.format("%-" + nw + "s %4d %7.2f\n",
                            cortar("  " + prod.substring(nw), nw),
                            l.getCantidad(), l.getSubtotal()));
                } else {
                    sb.append(String.format("%-" + nw + "s %4d %7.2f\n",
                            prod, l.getCantidad(), l.getSubtotal()));
                }
            } else {
                if (larga) {
                    sb.append(String.format("%-" + nw + "s\n", cortar(prod, nw)));
                    sb.append(String.format("%-" + nw + "s %4d %8.2f %8.2f\n",
                            cortar("  " + prod.substring(nw), nw),
                            l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
                } else {
                    sb.append(String.format("%-" + nw + "s %4d %8.2f %8.2f\n",
                            prod, l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
                }
            }
        }

        // ── Totales ───────────────────────────────────────────────────────────
        int totalArt = ticket.getLineas().stream()
                .mapToInt(Ticket.LineaTicket::getCantidad).sum();
        sb.append(SEPM).append('\n');
        sb.append("Articulos: ").append(totalArt).append(" pza(s)\n");
        sb.append(SEP).append('\n');

        if (mostrarDesglose)
            sb.append(derecha("Subtotal:", monto(ticket.getSubtotal()), ancho)).append('\n');

        sb.append(derecha("TOTAL:", monto(ticket.getTotal()), ancho)).append('\n');
        sb.append(SEP).append('\n');
        sb.append(derecha("Efectivo:", monto(ticket.getMontoRecibido()), ancho)).append('\n');
        sb.append(derecha("Cambio:",   monto(ticket.getCambio()),        ancho)).append('\n');

        // ── QR ────────────────────────────────────────────────────────────────
        if (mostrarQR) {
            sb.append(SEPM).append('\n');
            sb.append(centro("[ QR ]", ancho)).append('\n');
        }

        // ── Pie ───────────────────────────────────────────────────────────────
        if (!blank(pie)) {
            sb.append(SEPM).append('\n');
            for (String l : pie.split("\n"))
                sb.append(centro(l.trim(), ancho)).append('\n');
        }

        sb.append('\n').append('\n').append('\n');
        return sb.toString();
    }

    // ── Helpers de formato (acceso paquete para tests) ────────────────────────

    static String centro(String texto, int ancho) {
        if (texto == null || texto.isEmpty()) return "";
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int pad = (ancho - texto.length()) / 2;
        return " ".repeat(pad) + texto;
    }

    static String derecha(String izq, String der, int ancho) {
        int espacios = ancho - izq.length() - der.length();
        if (espacios < 1) espacios = 1;
        return izq + " ".repeat(espacios) + der;
    }

    static String rep(char c, int n) {
        return String.valueOf(c).repeat(Math.max(0, n));
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private static String monto(double v) {
        return String.format("$%7.2f", v);
    }

    private static String cortar(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String safe(String s, String def) {
        return blank(s) ? def : s;
    }
}