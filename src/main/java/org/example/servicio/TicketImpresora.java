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
        escribeTxt(out, "Este ticket no es \n comprobante fiscal\n");
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

    // ── API configurable (preview + botón de prueba) ─────────────────────────

    /**
     * Genera texto plano usando valores de configuración en lugar de datos hardcodeados.
     * Usado por la preview en ConfiguracionController y por imprimirConConfig().
     */
    public String generarTextoPlanoConConfig(Ticket ticket,
                                             String nombre, String giro, String direccion, String ciudad, String telefono,
                                             String encabezado, String pie, String aviso,
                                             boolean mostrarLogo, boolean mostrarFolio,
                                             boolean mostrarDesglose, boolean mostrarQR,
                                             int ancho) {

        int totalArticulos = ticket.getLineas().stream()
                .mapToInt(LineaTicket::getCantidad).sum();

        String sep  = "=".repeat(ancho);
        String sepS = "-".repeat(ancho);

        // Ancho de columna descripción: ancho - cant(4) - pu(9) - subt(9) - 3 espacios
        int descW = Math.max(ancho - 25, 10);

        StringBuilder sb = new StringBuilder();

        // ── Encabezado ────────────────────────────────────────────────────────
        if (mostrarLogo) {
            sb.append(centrar("[LOGO]", ancho)).append("\n");
        }
        sb.append(centrar(nombre.isEmpty() ? "NOMBRE DEL NEGOCIO" : nombre, ancho)).append("\n");
        if (!giro.isEmpty())      sb.append(centrar(giro, ancho)).append("\n");
        if (!direccion.isEmpty()) sb.append(centrar(direccion, ancho)).append("\n");
        if (!ciudad.isEmpty())    sb.append(centrar(ciudad, ancho)).append("\n");
        if (!telefono.isEmpty())  sb.append(centrar("Tel: " + telefono, ancho)).append("\n");
        if (!aviso.isEmpty())     sb.append(centrar(aviso, ancho)).append("\n");
        sb.append(sep).append("\n");

        if (!encabezado.isEmpty()) {
            for (String l : encabezado.split("\n"))
                sb.append(centrar(l.trim(), ancho)).append("\n");
            sb.append(sepS).append("\n");
        }

        // ── Datos de la venta ─────────────────────────────────────────────────
        if (mostrarFolio) sb.append(String.format("Folio : %06d\n", ticket.getIdVenta()));
        sb.append("Fecha : ").append(ticket.getFechaHora().format(FMT)).append("\n");
        sb.append("Cajero: ").append(ticket.getNombreCajero()).append("\n");
        sb.append("Caja  : ").append(ticket.getNumeroCaja()).append("\n");
        sb.append(sepS).append("\n");

        // ── Columnas de productos ─────────────────────────────────────────────
        sb.append(String.format("%-" + descW + "s %4s %9s %9s\n",
                "PRODUCTO", "CANT", "P.U.", "SUBT."));
        sb.append(sepS).append("\n");

        for (LineaTicket l : ticket.getLineas()) {
            String prod = l.getNombreProducto();
            if (prod.length() > descW) {
                sb.append(prod, 0, descW).append("\n");
                prod = "  " + prod.substring(descW);
            }
            sb.append(String.format("%-" + descW + "s %4d %9.2f %9.2f\n",
                    prod, l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
        }

        // ── Totales ───────────────────────────────────────────────────────────
        sb.append(sepS).append("\n");
        sb.append(String.format("%-" + (ancho - 14) + "s %4d pza(s)\n",
                "Total articulos:", totalArticulos));
        sb.append(sep).append("\n");

        if (mostrarDesglose) {
            sb.append(String.format("%-" + (ancho - 10) + "s %10.2f\n",
                    "Subtotal $", ticket.getSubtotal()));
        }
        sb.append(String.format("%-" + (ancho - 10) + "s %10.2f\n",
                "TOTAL   $", ticket.getTotal()));
        sb.append(sep).append("\n");
        sb.append(String.format("%-" + (ancho - 10) + "s %10.2f\n",
                "RECIBIDO $", ticket.getMontoRecibido()));
        sb.append(String.format("%-" + (ancho - 10) + "s %10.2f\n",
                "CAMBIO  $", ticket.getCambio()));

        if (mostrarQR) {
            sb.append("\n").append(centrar("[== QR ==]", ancho)).append("\n");
        }

        // ── Pie ───────────────────────────────────────────────────────────────
        if (!pie.isEmpty()) {
            sb.append(sepS).append("\n");
            for (String l : pie.split("\n"))
                sb.append(centrar(l.trim(), ancho)).append("\n");
        }
        sb.append("\n\n\n");

        return sb.toString();
    }

    /**
     * Imprime usando ESC/POS con datos de configuración.
     * Mismo flujo que imprimir() pero con header/footer dinámico.
     */
    public void imprimirConConfig(Ticket ticket,
                                  String nombre, String giro, String direccion, String ciudad, String telefono,
                                  String encabezado, String pie, String aviso,
                                  boolean mostrarLogo, boolean mostrarFolio,
                                  boolean mostrarDesglose, boolean mostrarQR,
                                  int ancho) throws Exception {
        byte[] datos = construirDatosConConfig(ticket,
                nombre, giro, direccion, ciudad, telefono,
                encabezado, pie, aviso,
                mostrarLogo, mostrarFolio, mostrarDesglose, mostrarQR, ancho);
        PrintService servicio = buscarImpresoraTermica();
        enviarAImpresora(servicio, datos);
    }

    private byte[] construirDatosConConfig(Ticket ticket,
                                           String nombre, String giro, String direccion, String ciudad, String telefono,
                                           String encabezado, String pie, String aviso,
                                           boolean mostrarLogo, boolean mostrarFolio,
                                           boolean mostrarDesglose, boolean mostrarQR,
                                           int ancho) throws Exception {

        int totalArticulos = ticket.getLineas().stream()
                .mapToInt(LineaTicket::getCantidad).sum();
        int descW = Math.max(ancho - 25, 10);
        String sep  = "=".repeat(ancho);
        String sepS = "-".repeat(ancho);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escribe(out, INIT);
        escribe(out, ALIGN_CENTER);

        // Encabezado
        if (mostrarLogo) { escribe(out, BOLD_ON); escribeTxt(out, ""); escribe(out, BOLD_OFF); }
        escribe(out, FONT_DOUBLE); escribe(out, BOLD_ON);
        escribeTxt(out, centrar(nombre.isEmpty() ? "NEGOCIO" : nombre, ancho) + "\n");
        escribe(out, FONT_NORMAL); escribe(out, BOLD_OFF);
        if (!giro.isEmpty())      escribeTxt(out, centrar(giro, ancho) + "\n");
        if (!direccion.isEmpty()) escribeTxt(out, centrar(direccion, ancho) + "\n");
        if (!ciudad.isEmpty())    escribeTxt(out, centrar(ciudad, ancho) + "\n");
        if (!telefono.isEmpty())  escribeTxt(out, centrar("Tel: " + telefono, ancho) + "\n");
        if (!aviso.isEmpty())     escribeTxt(out, centrar(aviso, ancho) + "\n");
        escribeTxt(out, sep + "\n");

        if (!encabezado.isEmpty()) {
            for (String l : encabezado.split("\n")) escribeTxt(out, centrar(l.trim(), ancho) + "\n");
            escribeTxt(out, sepS + "\n");
        }

        // Datos de la venta
        escribe(out, ALIGN_LEFT);
        if (mostrarFolio) escribeTxt(out, String.format("Folio : %06d\n", ticket.getIdVenta()));
        escribeTxt(out, "Fecha : " + ticket.getFechaHora().format(FMT) + "\n");
        escribeTxt(out, "Cajero: " + ticket.getNombreCajero() + "\n");
        escribeTxt(out, "Caja  : " + ticket.getNumeroCaja() + "\n");
        escribeTxt(out, sepS + "\n");

        // Columnas
        escribe(out, BOLD_ON);
        escribeTxt(out, String.format("%-" + descW + "s %4s %9s %9s\n", "PRODUCTO", "CANT", "P.U.", "SUBT."));
        escribe(out, BOLD_OFF);
        escribeTxt(out, sepS + "\n");

        for (LineaTicket l : ticket.getLineas()) {
            String prod = l.getNombreProducto();
            if (prod.length() > descW) {
                escribeTxt(out, prod.substring(0, descW) + "\n");
                prod = "  " + prod.substring(descW);
            }
            escribeTxt(out, String.format("%-" + descW + "s %4d %9.2f %9.2f\n",
                    prod, l.getCantidad(), l.getPrecioUnitario(), l.getSubtotal()));
        }

        // Totales
        escribeTxt(out, sepS + "\n");
        escribeTxt(out, String.format("%-" + (ancho - 14) + "s %4d pza(s)\n", "Total articulos:", totalArticulos));
        escribeTxt(out, sep + "\n");
        if (mostrarDesglose) {
            escribeTxt(out, String.format("%-" + (ancho - 10) + "s %10.2f\n", "Subtotal $", ticket.getSubtotal()));
        }
        escribe(out, BOLD_ON);
        escribeTxt(out, String.format("%-" + (ancho - 10) + "s %10.2f\n", "TOTAL   $", ticket.getTotal()));
        escribe(out, BOLD_OFF);
        escribeTxt(out, String.format("%-" + (ancho - 10) + "s %10.2f\n", "RECIBIDO $", ticket.getMontoRecibido()));
        escribeTxt(out, String.format("%-" + (ancho - 10) + "s %10.2f\n", "CAMBIO  $", ticket.getCambio()));

        if (mostrarQR) escribeTxt(out, "\n" + centrar("[== QR ==]", ancho) + "\n");

        // Pie
        escribe(out, ALIGN_CENTER);
        if (!pie.isEmpty()) {
            escribeTxt(out, sepS + "\n");
            for (String l : pie.split("\n")) escribeTxt(out, centrar(l.trim(), ancho) + "\n");
        }
        escribeTxt(out, "\n\n\n");

        escribe(out, CUT);
        return out.toByteArray();
    }

    /** Versión de centrar que acepta ancho como parámetro (sin usar la constante ANCHO). */
    private String centrar(String texto, int ancho) {
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int pad = (ancho - texto.length()) / 2;
        return " ".repeat(pad) + texto;
    }

    /**
     * Convierte el logo PNG a bytes ESC/POS (GS v 0) listos para enviar a la impresora.
     * El logo se escala automáticamente al ancho del papel (máx 360 dots para 58mm).
     *
     * Uso: llama a este método antes de construirDatos() e inserta los bytes al inicio.
     *
     * @param logoStream InputStream del PNG del logo
     * @param anchoPapelDots ancho máximo en dots (360 para 58mm, 480 para 80mm)
     */
    public byte[] logoAEscPos(java.io.InputStream logoStream, int anchoPapelDots) throws Exception {
        java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(logoStream);
        if (original == null) throw new Exception("No se pudo leer el logo");

        // Escalar al ancho del papel manteniendo proporción
        int w = Math.min(original.getWidth(), anchoPapelDots);
        int h = (int)((double)original.getHeight() / original.getWidth() * w);

        // Convertir a escala de grises escalada
        java.awt.image.BufferedImage gris = new java.awt.image.BufferedImage(
                w, h, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
        java.awt.Graphics2D g2 = gris.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, w, h, null);
        g2.dispose();

        // Umbralización a 1 bit (< 128 = punto negro)
        int widthBytes = (w + 7) / 8;
        byte[] bitmap  = new byte[widthBytes * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = gris.getRGB(x, y) & 0xFF;
                if (pixel < 128) {
                    bitmap[y * widthBytes + (x / 8)] |= (byte)(0x80 >> (x % 8));
                }
            }
        }

        // Construir comando GS v 0 (raster bit image)
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ALIGN_CENTER);
        out.write(new byte[]{ 0x1D, 0x76, 0x30, 0x00 }); // GS v 0, m=0 (normal density)
        out.write(widthBytes & 0xFF);
        out.write((widthBytes >> 8) & 0xFF);
        out.write(h & 0xFF);
        out.write((h >> 8) & 0xFF);
        out.write(bitmap);
        out.write(ALIGN_LEFT);
        return out.toByteArray();
    }
    /**
     * Imprime usando TicketRenderer como fuente de layout.
     * Mismo texto que la vista previa → fidelidad 1:1 garantizada.
     * Llamado por: TicketPreviewController, PagoController, TicketService.
     */

    /** Convierte el String de TicketRenderer a bytes ESC/POS. */
    private byte[] rendererAEscPos(String texto) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escribe(out, INIT);
        escribe(out, ALIGN_LEFT);
        escribeTxt(out, texto);
        escribe(out, CUT);
        return out.toByteArray();
    }
    /**
     * Imprime usando TicketRenderer como fuente de layout.
     * Mismo String que la vista previa → fidelidad 1:1 garantizada.
     */
    public void imprimirConRenderer(Ticket ticket,
                                    String nombre,     String giro,
                                    String direccion,  String ciudad,    String telefono,
                                    String encabezado, String pie,       String aviso,
                                    boolean mostrarLogo,     boolean mostrarFolio,
                                    boolean mostrarDesglose, boolean mostrarQR,
                                    boolean mostrarFecha,    boolean mostrarCajero,
                                    int ancho) throws Exception {

        String texto = org.example.servicio.TicketRenderer.generar(ticket,
                nombre, giro, direccion, ciudad, telefono,
                encabezado, pie, aviso,
                mostrarLogo, mostrarFolio, mostrarDesglose, mostrarQR,
                mostrarFecha, mostrarCajero, ancho);

        PrintService servicio = buscarImpresoraTermica();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escribe(out, INIT);
        escribe(out, ALIGN_LEFT);
        escribeTxt(out, texto);
        escribe(out, CUT);
        enviarAImpresora(servicio, out.toByteArray());
    }
}