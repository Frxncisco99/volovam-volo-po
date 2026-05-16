package org.example.servicio;

import org.example.modelo.Ticket;
import java.time.format.DateTimeFormatter;

/**
 * TicketRenderer — Fuente única de verdad del layout del ticket.
 *
 * Genera el String canónico en formato monoespaciado.
 * La vista previa (TextArea) y la impresora ESC/POS consumen
 * exactamente este mismo String → fidelidad 1:1 garantizada.
 *
 * Ubicación: src/main/java/org/example/servicio/TicketRenderer.java
 */
public final class TicketRenderer {

    public static final int ANCHO_58MM = 32;
    public static final int ANCHO_80MM = 42;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    private TicketRenderer() {}

    // ─────────────────────────────────────────────────────────────────────────
    //  API pública
    // ─────────────────────────────────────────────────────────────────────────

    public static String generar(Ticket ticket,
                                 String nombre,     String giro,
                                 String direccion,  String ciudad,  String telefono,
                                 String encabezado, String pie,     String aviso,
                                 boolean mostrarLogo,     boolean mostrarFolio,
                                 boolean mostrarDesglose, boolean mostrarQR,
                                 boolean mostrarFecha,    boolean mostrarCajero,
                                 int ancho) {

        // Separadores
        final String SEP2 = rep('=', ancho);           // ════  sección principal
        final String SEP1 = rep('-', ancho);           // ────  sección secundaria
        final String BLK  = "";                        // línea vacía

        // Columnas de productos
        // 58 mm (32): nombre(19) + sp(1) + cant(3) + sp(1) + importe(8)  = 32
        // 80 mm (42): nombre(19) + sp(1) + cant(3) + sp(1) + pu(8) + sp(1) + sub(9) = 42
        final boolean tresCol = (ancho <= ANCHO_58MM);
        final int NW = tresCol ? 19 : 19;   // nombre siempre 19 chars

        StringBuilder sb = new StringBuilder();


        // ── 2. ENCABEZADO NEGOCIO ─────────────────────────────────────────────
        sb.append(centro(safe(nombre, "MI NEGOCIO").toUpperCase(), ancho)).append('\n');
        if (!blank(giro))      sb.append(centro(giro,          ancho)).append('\n');
        if (!blank(direccion)) sb.append(centro(direccion,     ancho)).append('\n');
        if (!blank(ciudad))    sb.append(centro(ciudad,        ancho)).append('\n');
        if (!blank(telefono))  sb.append(centro("Tel: " + telefono, ancho)).append('\n');

        sb.append(SEP2).append('\n');

        // Mensaje de encabezado personalizado
        if (!blank(encabezado)) {
            for (String l : encabezado.split("\\r?\\n"))
                if (!blank(l)) sb.append(centro(l.trim(), ancho)).append('\n');
            sb.append(SEP1).append('\n');
        }

        // ── 3. DATOS DE VENTA ─────────────────────────────────────────────────
        if (mostrarFolio)
            sb.append(par("Folio", String.format("#%06d", ticket.getIdVenta()), ancho)).append('\n');
        if (mostrarFecha)
            sb.append(par("Fecha", ticket.getFechaHora().format(FMT), ancho)).append('\n');
        if (mostrarCajero)
            sb.append(par("Cajero", ticket.getNombreCajero(), ancho)).append('\n');
        sb.append(par("Caja", String.valueOf(ticket.getNumeroCaja()), ancho)).append('\n');

        sb.append(SEP1).append('\n');

        // ── 4. TABLA DE PRODUCTOS ─────────────────────────────────────────────
        if (tresCol) {
            // Cabecera: nombre(19) cant(3) importe(8) con 1 esp entre cols = 32
            sb.append(String.format("%-19s %3s %8s\n", "DESCRIPCION", "QTY", "IMPORTE"));
        } else {
            sb.append(String.format("%-19s %3s %8s %9s\n",
                    "DESCRIPCION", "QTY", "P.U.", "TOTAL"));
        }
        sb.append(SEP1).append('\n');

        for (Ticket.LineaTicket l : ticket.getLineas()) {
            String prod = l.getNombreProducto();

            if (tresCol) {
                if (prod.length() > NW) {
                    // Línea larga: nombre en primera fila, valores en segunda
                    sb.append(String.format("%-19s\n", cortar(prod, 19)));
                    sb.append(String.format("  %-17s %3d %8.2f\n",
                            cortar(prod.substring(Math.min(19, prod.length())), 17),
                            l.getCantidad(), l.getSubtotal()));
                } else {
                    sb.append(String.format("%-19s %3d %8.2f\n",
                            prod, l.getCantidad(), l.getSubtotal()));
                }
            } else {
                if (prod.length() > NW) {
                    sb.append(String.format("%-19s\n", cortar(prod, 19)));
                    sb.append(String.format("  %-17s %3d %8.2f %9.2f\n",
                            cortar(prod.substring(Math.min(19, prod.length())), 17),
                            l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
                } else {
                    sb.append(String.format("%-19s %3d %8.2f %9.2f\n",
                            prod, l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
                }
            }
        }

        // ── 5. TOTALES ────────────────────────────────────────────────────────
        int totalArt = ticket.getLineas().stream()
                .mapToInt(Ticket.LineaTicket::getCantidad).sum();

        sb.append(SEP1).append('\n');
        sb.append(par("Articulos", totalArt + " pza(s)", ancho)).append('\n');
        sb.append(SEP2).append('\n');

        if (mostrarDesglose)
            sb.append(derechaFmt("Subtotal:", ticket.getSubtotal(), ancho)).append('\n');

        // Línea TOTAL destacada con espacio arriba
        sb.append(derechaFmt("TOTAL:", ticket.getTotal(), ancho)).append('\n');
        sb.append(SEP2).append('\n');
        sb.append('\n');

        // Pago
        sb.append(derechaFmt("Efectivo:", ticket.getMontoRecibido(), ancho)).append('\n');
        sb.append(derechaFmt("Cambio:",   ticket.getCambio(),        ancho)).append('\n');


        // ── 6. SEPARADOR ──────────────────────────────────────────────────────
        sb.append('\n');
        sb.append(SEP1).append('\n');

        // ── 7. AVISO FISCAL ───────────────────────────────────────────────────
        if (!blank(aviso)) {
            sb.append('\n');

            for (String l : aviso.split("\\r?\\n")) {
                if (!blank(l)) {
                    sb.append(centro(l.trim(), ancho)).append('\n');
                }
            }
        }

        // ── 8. PIE ────────────────────────────────────────────────────────────
        if (!blank(pie)) {
            sb.append('\n');
            sb.append(SEP1).append('\n');
            for (String l : pie.split("\n"))
                if (!blank(l)) sb.append(centro(l.trim(), ancho)).append('\n');
        }

        // Avance de papel antes del corte
        sb.append('\n').append('\n').append('\n');
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers de formato
    // ─────────────────────────────────────────────────────────────────────────

    /** Centra texto en 'ancho' columnas. */
    public static String centro(String texto, int ancho) {
        if (blank(texto)) return "";
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int pad = (ancho - texto.length()) / 2;
        return " ".repeat(pad) + texto;
    }

    /**
     * Dos columnas: etiqueta a la izquierda, valor a la derecha.
     * "Fecha" + "01/01/2025 10:00" → "Fecha    01/01/2025 10:00"
     */
    public static String par(String izq, String der, int ancho) {
        int espacios = ancho - izq.length() - der.length();
        if (espacios < 1) espacios = 1;
        return izq + " ".repeat(espacios) + der;
    }

    /**
     * Etiqueta a la izquierda, monto formateado a la derecha.
     * "TOTAL:" + 141.00 → "TOTAL:              $141.00"
     */
    public static String derechaFmt(String etiqueta, double valor, int ancho) {
        String montoStr = String.format("$%8.2f", valor);
        return par(etiqueta, montoStr, ancho);
    }

    /** Repite un carácter n veces. */
    public static String rep(char c, int n) {
        return String.valueOf(c).repeat(Math.max(0, n));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Privados
    // ─────────────────────────────────────────────────────────────────────────

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