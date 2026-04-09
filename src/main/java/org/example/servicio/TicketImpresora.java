package org.example.servicio;

import org.example.modelo.Ticket;
import org.example.modelo.Ticket.LineaTicket;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Maneja la impresión física en impresoras térmicas mediante comandos ESC/POS.
 *
 * ESC/POS es el protocolo estándar de Epson usado por casi todas las
 * impresoras térmicas de punto de venta (80mm y 58mm).
 *
 * Dependencia requerida en pom.xml / build.gradle: NINGUNA.
 * Usa únicamente javax.print de la JDK estándar.
 */
public class TicketImpresora {

    // ── Constantes ESC/POS ──────────────────────────────────────────────────
    private static final byte ESC  = 0x1B;
    private static final byte GS   = 0x1D;

    // Inicializar impresora
    private static final byte[] INIT          = { ESC, '@' };
    // Alinear: izquierda=0, centro=1, derecha=2
    private static final byte[] ALIGN_LEFT    = { ESC, 'a', 0 };
    private static final byte[] ALIGN_CENTER  = { ESC, 'a', 1 };
    // Negrita ON / OFF
    private static final byte[] BOLD_ON       = { ESC, 'E', 1 };
    private static final byte[] BOLD_OFF      = { ESC, 'E', 0 };
    // Tamaño de fuente normal / doble alto+ancho
    private static final byte[] FONT_NORMAL   = { GS, '!', 0 };
    private static final byte[] FONT_DOUBLE   = { GS, '!', 0x11 };
    // Cortar papel
    private static final byte[] CUT           = { GS, 'V', 66, 0 };

    // Ancho de impresión en caracteres (80mm ≈ 48 chars, 58mm ≈ 32 chars)
    private static final int ANCHO = 48;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss", new Locale("es", "MX"));

    // ── API pública ─────────────────────────────────────────────────────────

    /**
     * Imprime el ticket en la impresora térmica predeterminada del sistema.
     * Si no hay impresora térmica, lanza una excepción descriptiva.
     *
     * @param ticket El ticket ya construido con todos sus datos.
     * @throws Exception Si no hay impresora disponible o falla la impresión.
     */
    public void imprimir(Ticket ticket) throws Exception {
        byte[] datos = construirDatos(ticket);
        PrintService servicio = buscarImpresoraTermica();
        enviarAImpresora(servicio, datos);
    }

    /**
     * Devuelve el texto plano del ticket (útil para vista previa en pantalla).
     */
    public String generarTextoPlano(Ticket ticket) {
        StringBuilder sb = new StringBuilder();
        sb.append(centrar("VOLOVAN VOLO")).append("\n");
        sb.append(centrar("Sistema de Punto de Venta")).append("\n");
        sb.append(linea()).append("\n");
        sb.append("Venta #: ").append(ticket.getIdVenta()).append("\n");
        sb.append("Fecha : ").append(ticket.getFechaHora().format(FMT)).append("\n");
        sb.append("Cajero: ").append(ticket.getNombreCajero()).append("\n");
        sb.append("Caja  : ").append(ticket.getNumeroCaja()).append("\n");
        sb.append(linea()).append("\n");
        sb.append(String.format("%-24s %4s %8s %8s\n", "PRODUCTO", "CANT", "P.U.", "SUBTOT"));
        sb.append(linea()).append("\n");

        for (LineaTicket l : ticket.getLineas()) {
            // Nombre en primera línea si es largo
            String nombre = l.getNombreProducto();
            if (nombre.length() > 22) {
                sb.append(nombre, 0, 22).append("..").append("\n");
                nombre = "";
            }
            sb.append(String.format("%-24s %4d %8.2f %8.2f\n",
                    nombre, l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
        }

        sb.append(linea()).append("\n");
        sb.append(String.format("%-36s %8.2f\n", "TOTAL:", ticket.getTotal()));
        sb.append(String.format("%-36s %8.2f\n", "RECIBIDO:", ticket.getMontoRecibido()));
        sb.append(String.format("%-36s %8.2f\n", "CAMBIO:", ticket.getCambio()));
        sb.append(linea()).append("\n");
        sb.append(centrar("¡Gracias por su compra!")).append("\n");
        sb.append(centrar("Vuelva pronto")).append("\n\n\n");
        return sb.toString();
    }

    // ── Construcción del stream ESC/POS ─────────────────────────────────────

    private byte[] construirDatos(Ticket ticket) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        escribe(out, INIT);

        // ── Encabezado ──────────────────────────────────────────────────────
        escribe(out, ALIGN_CENTER);
        escribe(out, FONT_DOUBLE);
        escribe(out, BOLD_ON);
        escribeTxt(out, "VOLOVAN VOLO\n");
        escribe(out, FONT_NORMAL);
        escribe(out, BOLD_OFF);
        escribeTxt(out, "Sistema de Punto de Venta\n");
        escribeTxt(out, linea() + "\n");

        // ── Datos de la venta ────────────────────────────────────────────────
        escribe(out, ALIGN_LEFT);
        escribeTxt(out, "Venta # : " + ticket.getIdVenta() + "\n");
        escribeTxt(out, "Fecha   : " + ticket.getFechaHora().format(FMT) + "\n");
        escribeTxt(out, "Cajero  : " + ticket.getNombreCajero() + "\n");
        escribeTxt(out, "Caja    : " + ticket.getNumeroCaja() + "\n");
        escribeTxt(out, linea() + "\n");

        // ── Encabezado de columnas ────────────────────────────────────────────
        escribe(out, BOLD_ON);
        escribeTxt(out, String.format("%-22s %4s %9s %9s\n",
                "PRODUCTO", "CANT", "P.U.", "SUBT."));
        escribe(out, BOLD_OFF);
        escribeTxt(out, linea() + "\n");

        // ── Líneas de productos ───────────────────────────────────────────────
        for (LineaTicket l : ticket.getLineas()) {
            String nombre = l.getNombreProducto();
            // Si el nombre es muy largo, lo parte en dos líneas
            if (nombre.length() > 22) {
                escribeTxt(out, nombre.substring(0, 22) + "\n");
                nombre = "  " + nombre.substring(22);
            }
            escribeTxt(out, String.format("%-22s %4d %9.2f %9.2f\n",
                    nombre, l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
        }

        // ── Totales ───────────────────────────────────────────────────────────
        escribeTxt(out, linea() + "\n");
        escribe(out, BOLD_ON);
        escribeTxt(out, String.format("%-34s %10.2f\n", "TOTAL:", ticket.getTotal()));
        escribe(out, BOLD_OFF);
        escribeTxt(out, String.format("%-34s %10.2f\n", "RECIBIDO:", ticket.getMontoRecibido()));
        escribeTxt(out, String.format("%-34s %10.2f\n", "CAMBIO:", ticket.getCambio()));

        // ── Pie ───────────────────────────────────────────────────────────────
        escribe(out, ALIGN_CENTER);
        escribeTxt(out, linea() + "\n");
        escribeTxt(out, "\n");
        escribe(out, BOLD_ON);
        escribeTxt(out, "* Gracias por su compra *\n");
        escribe(out, BOLD_OFF);
        escribeTxt(out, "Vuelva pronto\n");
        escribeTxt(out, "\n\n\n");

        // ── Cortar papel ──────────────────────────────────────────────────────
        escribe(out, CUT);

        return out.toByteArray();
    }

    // ── Búsqueda de impresora ────────────────────────────────────────────────

    /**
     * Busca una impresora que acepte el flavor RAW (bytes directos).
     * Primero busca alguna con "thermal", "receipt" o "pos" en el nombre.
     * Si no, usa la primera disponible que acepte RAW.
     */
    private PrintService buscarImpresoraTermica() throws Exception {
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintService[] servicios = PrintServiceLookup.lookupPrintServices(flavor, null);

        if (servicios == null || servicios.length == 0) {
            throw new Exception("No se encontró ninguna impresora disponible en el sistema.");
        }

        // Preferir impresora térmica por nombre
        for (PrintService s : servicios) {
            String nombre = s.getName().toLowerCase();
            if (nombre.contains("thermal") || nombre.contains("receipt")
                    || nombre.contains("pos") || nombre.contains("epson")
                    || nombre.contains("star") || nombre.contains("bixolon")) {
                return s;
            }
        }

        // Fallback: primera disponible
        return servicios[0];
    }

    /**
     * Envía los bytes directamente a la impresora sin pasar por el sistema
     * de impresión de Java (que agregaría márgenes y formato de página).
     */
    private void enviarAImpresora(PrintService servicio, byte[] datos) throws Exception {
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(datos, flavor, null);
        PrintRequestAttributeSet atts = new HashPrintRequestAttributeSet();

        DocPrintJob job = servicio.createPrintJob();
        job.print(doc, atts);
    }

    // ── Utilidades de texto ──────────────────────────────────────────────────

    private void escribe(ByteArrayOutputStream out, byte[] bytes) throws Exception {
        out.write(bytes);
    }

    private void escribeTxt(ByteArrayOutputStream out, String texto) throws Exception {
        // CP850 es compatible con la mayoría de impresoras térmicas (soporta acentos)
        out.write(texto.getBytes("CP850"));
    }

    private String linea() {
        return "-".repeat(ANCHO);
    }

    private String centrar(String texto) {
        if (texto.length() >= ANCHO) return texto;
        int padding = (ANCHO - texto.length()) / 2;
        return " ".repeat(padding) + texto;
    }
}