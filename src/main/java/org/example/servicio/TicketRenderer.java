package org.example.servicio;

import org.example.modelo.Ticket;

import java.time.format.DateTimeFormatter;

/**
 * ──────────────────────────────────────────────────────────────────────
 *  TicketRenderer  —  Fuente única de verdad para el layout del ticket.
 * ──────────────────────────────────────────────────────────────────────
 *
 * Genera el texto canónico del ticket en formato monoespaciado.
 * TANTO la vista previa (TextArea) COMO la impresión ESC/POS consumen
 * este mismo String → garantiza que lo que se ve = lo que sale impreso.
 *
 * Ubicación: src/main/java/org/example/servicio/TicketRenderer.java
 *
 * Callers:
 *   · TicketPreviewController.configurar()  → preview modal
 *   · ConfiguracionController.actualizarVistaPrevia()  → mini-preview
 *   · TicketImpresora.imprimirConRenderer()  → impresión real
 */
public final class TicketRenderer {

    /** Caracteres por línea para papel de 58mm (32 cols a 12 CPI). */
    public static final int ANCHO_58MM = 32;

    /** Caracteres por línea para papel de 80mm (42 cols a 12 CPI). */
    public static final int ANCHO_80MM = 42;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    // ── Constructor privado — clase utilitaria estática ───────────────────

    private TicketRenderer() {}

    // ── API pública ───────────────────────────────────────────────────────

    /**
     * Genera el String del ticket completo, listo para mostrar y para imprimir.
     *
     * @param ticket          Datos de la venta.
     * @param nombre          Nombre del negocio en el ticket.
     * @param giro            Giro/descripción del negocio.
     * @param direccion       Dirección.
     * @param ciudad          Ciudad / CP.
     * @param telefono        Teléfono.
     * @param encabezado      Mensaje de bienvenida (puede ser multilinea con \n).
     * @param pie             Mensaje de despedida (puede ser multilinea con \n).
     * @param aviso           Aviso fiscal.
     * @param mostrarLogo     Si true, imprime marcador [LOGO] en encabezado.
     * @param mostrarFolio    Si true, imprime el número de folio.
     * @param mostrarDesglose Si true, imprime línea de subtotal.
     * @param mostrarQR       Si true, imprime marcador [QR].
     * @param mostrarFecha    Si true, imprime fecha y hora.
     * @param mostrarCajero   Si true, imprime nombre del cajero.
     * @param ancho           Columnas: usar ANCHO_58MM o ANCHO_80MM.
     * @return String con el layout completo del ticket.
     */
    public static String generar(Ticket ticket,
                                 String nombre, String giro,
                                 String direccion, String ciudad, String telefono,
                                 String encabezado, String pie, String aviso,
                                 boolean mostrarLogo, boolean mostrarFolio,
                                 boolean mostrarDesglose, boolean mostrarQR,
                                 boolean mostrarFecha, boolean mostrarCajero,
                                 int ancho) {

        final String SEP  = rep('=', ancho);
        final String SEPM = rep('-', ancho);

        StringBuilder sb = new StringBuilder();

        // ── Encabezado del negocio ────────────────────────────────────────
        if (mostrarLogo) {
            sb.append(centro("[  LOGO  ]", ancho)).append('\n');
        }
        sb.append(centro(safe(nombre, "NEGOCIO").toUpperCase(), ancho)).append('\n');
        if (!blank(giro))      sb.append(centro(giro, ancho)).append('\n');
        if (!blank(direccion)) sb.append(centro(direccion, ancho)).append('\n');
        if (!blank(ciudad))    sb.append(centro(ciudad, ancho)).append('\n');
        if (!blank(telefono))  sb.append(centro("Tel: " + telefono, ancho)).append('\n');
        if (!blank(aviso))     sb.append(centro(aviso, ancho)).append('\n');
        sb.append(SEP).append('\n');

        // Mensaje de encabezado personalizado
        if (!blank(encabezado)) {
            for (String linea : encabezado.split("\n"))
                sb.append(centro(linea.trim(), ancho)).append('\n');
            sb.append(SEPM).append('\n');
        }

        // ── Datos de la venta ─────────────────────────────────────────────
        if (mostrarFolio)
            sb.append(izq("Folio : #" + String.format("%06d", ticket.getIdVenta()), ancho))
                    .append('\n');
        if (mostrarFecha)
            sb.append(izq("Fecha : " + ticket.getFechaHora().format(FMT), ancho)).append('\n');
        if (mostrarCajero)
            sb.append(izq("Cajero: " + ticket.getNombreCajero(), ancho)).append('\n');
        sb.append(izq("Caja  : " + ticket.getNumeroCaja(), ancho)).append('\n');
        sb.append(SEPM).append('\n');

        // ── Columnas de productos ─────────────────────────────────────────
        // Para 58mm (32): 3 columnas — nombre (19) | cant (4) | importe (7)
        // Para 80mm (42): 4 columnas — nombre (19) | cant (4) | p.u. (8) | subt (8)
        final boolean tresColumnas = (ancho <= 34);

        if (tresColumnas) {
            // Formato 3 col: 19 + 1 + 4 + 1 + 7 = 32
            int nw = ancho - 4 - 7 - 2;            // nameWidth = 19 para 32
            sb.append(String.format("%-" + nw + "s %4s %7s\n", "PRODUCTO", "CANT", "IMPORTE"));
            sb.append(SEPM).append('\n');

            for (Ticket.LineaTicket l : ticket.getLineas()) {
                String prod = cortar(l.getNombreProducto(), nw);
                if (l.getNombreProducto().length() > nw) {
                    // Nombre largo: primera línea cortada, segunda línea indentada
                    sb.append(String.format("%-" + nw + "s %4s %7s\n",
                            prod, "", ""));
                    prod = "  " + l.getNombreProducto().substring(nw);
                    sb.append(String.format("%-" + nw + "s %4d %7.2f\n",
                            cortar(prod, nw), l.getCantidad(), l.getSubtotal()));
                } else {
                    sb.append(String.format("%-" + nw + "s %4d %7.2f\n",
                            prod, l.getCantidad(), l.getSubtotal()));
                }
            }
        } else {
            // Formato 4 col: 19 + 1 + 4 + 1 + 8 + 1 + 8 = 42
            int nw = ancho - 4 - 8 - 8 - 3;        // nameWidth = 19 para 42
            sb.append(String.format("%-" + nw + "s %4s %8s %8s\n",
                    "PRODUCTO", "CANT", "P.U.", "SUBT."));
            sb.append(SEPM).append('\n');

            for (Ticket.LineaTicket l : ticket.getLineas()) {
                String prod = cortar(l.getNombreProducto(), nw);
                if (l.getNombreProducto().length() > nw) {
                    sb.append(String.format("%-" + nw + "s %4s %8s %8s\n", prod, "", "", ""));
                    prod = "  " + l.getNombreProducto().substring(nw);
                    sb.append(String.format("%-" + nw + "s %4d %8.2f %8.2f\n",
                            cortar(prod, nw), l.getCantidad(),
                            l.getPrecioUnitario(), l.getSubtotal()));
                } else {
                    sb.append(String.format("%-" + nw + "s %4d %8.2f %8.2f\n",
                            prod, l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
                }
            }
        }

        // ── Totales ───────────────────────────────────────────────────────
        sb.append(SEPM).append('\n');

        int totalArt = ticket.getLineas().stream()
                .mapToInt(Ticket.LineaTicket::getCantidad).sum();
        sb.append(izq("Articulos: " + totalArt + " pza(s)", ancho)).append('\n');
        sb.append(SEP).append('\n');

        if (mostrarDesglose)
            sb.append(derechaCol("Subtotal:", fmt(ticket.getSubtotal()), ancho)).append('\n');

        sb.append(derechaCol("TOTAL:", fmt(ticket.getTotal()), ancho)).append('\n');
        sb.append(SEP).append('\n');
        sb.append(derechaCol("Efectivo:", fmt(ticket.getMontoRecibido()), ancho)).append('\n');
        sb.append(derechaCol("Cambio:",   fmt(ticket.getCambio()),        ancho)).append('\n');

        // ── QR ────────────────────────────────────────────────────────────
        if (mostrarQR) {
            sb.append(SEPM).append('\n');
            sb.append(centro("[  == QR ==  ]", ancho)).append('\n');
        }

        // ── Pie de página ─────────────────────────────────────────────────
        if (!blank(pie)) {
            sb.append(SEPM).append('\n');
            for (String linea : pie.split("\n"))
                sb.append(centro(linea.trim(), ancho)).append('\n');
        }

        // Avance final de papel antes del corte
        sb.append('\n').append('\n').append('\n');

        return sb.toString();
    }

    // ── Helpers de formato ────────────────────────────────────────────────

    /** Centra texto en 'ancho' caracteres con espacios. */
    public static String centro(String texto, int ancho) {
        if (texto == null || texto.isEmpty()) return "";
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int pad = (ancho - texto.length()) / 2;
        return " ".repeat(pad) + texto;
    }

    /** Texto alineado a la izquierda, truncado si excede 'ancho'. */
    public static String izq(String texto, int ancho) {
        if (texto == null) return "";
        return texto.length() > ancho ? texto.substring(0, ancho) : texto;
    }

    /**
     * Dos columnas: izquierda y derecha alineada al final de línea.
     * "TOTAL:"          "$141.00"  →  "TOTAL:       $141.00"
     */
    public static String derechaCol(String izquierda, String derecha, int ancho) {
        int espacios = ancho - izquierda.length() - derecha.length();
        if (espacios < 1) espacios = 1;
        return izquierda + " ".repeat(espacios) + derecha;
    }

    /** Repite un carácter n veces. */
    public static String rep(char c, int n) {
        return String.valueOf(c).repeat(Math.max(0, n));
    }

    // ── Helpers privados ──────────────────────────────────────────────────

    private static String fmt(double valor) {
        return String.format("$%7.2f", valor);
    }

    private static String cortar(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max) : (s == null ? "" : s);
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String safe(String s, String def) {
        return blank(s) ? def : s;
    }
}