package org.example.servicio;

import org.example.modelo.Ticket;
import org.example.modelo.Ticket.LineaTicket;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TicketImpresora {

    // Comandos ESC/POS
    private static final byte ESC = 0x1B;
    private static final byte GS  = 0x1D;

    private static final byte[] INIT         = { ESC, '@' };
    private static final byte[] ALIGN_LEFT   = { ESC, 'a', 0 };
    private static final byte[] ALIGN_CENTER = { ESC, 'a', 1 };
    private static final byte[] BOLD_ON      = { ESC, 'E', 1 };
    private static final byte[] BOLD_OFF     = { ESC, 'E', 0 };
    private static final byte[] FONT_NORMAL  = { GS,  '!', 0 };
    private static final byte[] FONT_DOUBLE  = { GS,  '!', 0x11 };
    private static final byte[] CUT          = { GS,  'V', 66, 0 };

    // 48 chars para papel 80mm — cambia a 32 para papel 58mm
    private static final int ANCHO = 48;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss", new Locale("es", "MX"));

    // ── API pública ──────────────────────────────────────────────────────────

    public void imprimir(Ticket ticket) throws Exception {
        byte[] datos = construirDatos(ticket);
        PrintService servicio = buscarImpresoraTermica();
        enviarAImpresora(servicio, datos);
    }

    public String generarTextoPlano(Ticket ticket) {
        int totalArticulos = ticket.getLineas().stream()
                .mapToInt(LineaTicket::getCantidad).sum();

        StringBuilder sb = new StringBuilder();

        // Encabezado
        sb.append(centrar("VOLOVAN VOLO")).append("\n");
        sb.append(centrar("Panaderia y Reposteria")).append("\n");
        sb.append(centrar("C. Francisco I Madero #1515")).append("\n");
        sb.append(centrar("Col. Nogales, C.P. 26236")).append("\n");
        sb.append(linea()).append("\n");

        // Folio y datos
        sb.append(String.format("Folio : %06d\n", ticket.getIdVenta()));
        sb.append("Fecha : ").append(ticket.getFechaHora().format(FMT)).append("\n");
        sb.append("Cajero: ").append(ticket.getNombreCajero()).append("\n");
        sb.append("Caja  : ").append(ticket.getNumeroCaja()).append("\n");
        sb.append(linea()).append("\n");

        // Columnas
        sb.append(String.format("%-22s %4s %9s %9s\n", "PRODUCTO", "CANT", "P.U.", "SUBT."));
        sb.append(linea()).append("\n");

        // Productos
        for (LineaTicket l : ticket.getLineas()) {
            String nombre = l.getNombreProducto();
            if (nombre.length() > 22) {
                sb.append(nombre, 0, 22).append("\n");
                nombre = "  " + nombre.substring(22);
            }
            sb.append(String.format("%-22s %4d %9.2f %9.2f\n",
                    nombre, l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
        }

        // Totales
        sb.append(linea()).append("\n");
        sb.append(String.format("%-28s %4d pza(s)\n", "Total articulos:", totalArticulos));
        sb.append(linea()).append("\n");
        sb.append(String.format("%-34s %10.2f\n", "TOTAL   $", ticket.getTotal()));
        sb.append(String.format("%-34s %10.2f\n", "RECIBIDO $", ticket.getMontoRecibido()));
        sb.append(String.format("%-34s %10.2f\n", "CAMBIO  $", ticket.getCambio()));
        sb.append(linea()).append("\n");

        // Pie
        sb.append(centrar("Este ticket no es comprobante fiscal")).append("\n");
        sb.append(linea()).append("\n");
        sb.append(centrar("* Gracias por su compra *")).append("\n");
        sb.append(centrar("Vuelva pronto")).append("\n");
        sb.append(centrar("facebook.com/VolovanVolo")).append("\n");
        sb.append("\n\n\n");

        return sb.toString();
    }

    // ── Construcción ESC/POS ─────────────────────────────────────────────────

    private byte[] construirDatos(Ticket ticket) throws Exception {
        int totalArticulos = ticket.getLineas().stream()
                .mapToInt(LineaTicket::getCantidad).sum();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escribe(out, INIT);

        // Encabezado centrado
        escribe(out, ALIGN_CENTER);
        escribe(out, FONT_DOUBLE);
        escribe(out, BOLD_ON);
        escribeTxt(out, "VOLOVAN VOLO\n");
        escribe(out, FONT_NORMAL);
        escribe(out, BOLD_OFF);
        escribeTxt(out, "Panaderia y Reposteria\n");
        escribeTxt(out, "C. Francisco I Madero #1515\n");
        escribeTxt(out, "Col. Nogales, C.P. 26236\n");
        escribeTxt(out, linea() + "\n");

        // Datos de la venta
        escribe(out, ALIGN_LEFT);
        escribeTxt(out, String.format("Folio : %06d\n", ticket.getIdVenta()));
        escribeTxt(out, "Fecha : " + ticket.getFechaHora().format(FMT) + "\n");
        escribeTxt(out, "Cajero: " + ticket.getNombreCajero() + "\n");
        escribeTxt(out, "Caja  : " + ticket.getNumeroCaja() + "\n");
        escribeTxt(out, linea() + "\n");

        // Columnas
        escribe(out, BOLD_ON);
        escribeTxt(out, String.format("%-22s %4s %9s %9s\n", "PRODUCTO", "CANT", "P.U.", "SUBT."));
        escribe(out, BOLD_OFF);
        escribeTxt(out, linea() + "\n");

        // Productos
        for (LineaTicket l : ticket.getLineas()) {
            String nombre = l.getNombreProducto();
            if (nombre.length() > 22) {
                escribeTxt(out, nombre.substring(0, 22) + "\n");
                nombre = "  " + nombre.substring(22);
            }
            escribeTxt(out, String.format("%-22s %4d %9.2f %9.2f\n",
                    nombre, l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
        }

        // Totales
        escribeTxt(out, linea() + "\n");
        escribeTxt(out, String.format("%-28s %4d pza(s)\n", "Total articulos:", totalArticulos));
        escribeTxt(out, linea() + "\n");
        escribe(out, BOLD_ON);
        escribeTxt(out, String.format("%-34s %10.2f\n", "TOTAL   $", ticket.getTotal()));
        escribe(out, BOLD_OFF);
        escribeTxt(out, String.format("%-34s %10.2f\n", "RECIBIDO $", ticket.getMontoRecibido()));
        escribeTxt(out, String.format("%-34s %10.2f\n", "CAMBIO  $", ticket.getCambio()));

        // Pie
        escribe(out, ALIGN_CENTER);
        escribeTxt(out, linea() + "\n");
        escribeTxt(out, "Este ticket no es comprobante fiscal\n");
        escribeTxt(out, linea() + "\n");
        escribe(out, BOLD_ON);
        escribeTxt(out, "* Gracias por su compra *\n");
        escribe(out, BOLD_OFF);
        escribeTxt(out, "Vuelva pronto\n");
        escribeTxt(out, "facebook.com/VolovanVolo\n");
        escribeTxt(out, "\n\n\n");

        escribe(out, CUT);
        return out.toByteArray();
    }

    // ── Impresora ────────────────────────────────────────────────────────────

    private PrintService buscarImpresoraTermica() throws Exception {
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintService[] servicios = PrintServiceLookup.lookupPrintServices(flavor, null);
        if (servicios == null || servicios.length == 0)
            throw new Exception("No se encontro ninguna impresora disponible.");
        for (PrintService s : servicios) {
            String n = s.getName().toLowerCase();
            if (n.contains("thermal") || n.contains("receipt") || n.contains("pos")
                    || n.contains("epson") || n.contains("star") || n.contains("bixolon"))
                return s;
        }
        return servicios[0];
    }

    private void enviarAImpresora(PrintService servicio, byte[] datos) throws Exception {
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(datos, flavor, null);
        PrintRequestAttributeSet atts = new HashPrintRequestAttributeSet();
        servicio.createPrintJob().print(doc, atts);
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private void escribe(ByteArrayOutputStream out, byte[] bytes) throws Exception {
        out.write(bytes);
    }

    private void escribeTxt(ByteArrayOutputStream out, String texto) throws Exception {
        out.write(texto.getBytes("CP850"));
    }

    private String linea() { return "-".repeat(ANCHO); }

    private String centrar(String texto) {
        if (texto.length() >= ANCHO) return texto;
        int pad = (ANCHO - texto.length()) / 2;
        return " ".repeat(pad) + texto;
    }
}