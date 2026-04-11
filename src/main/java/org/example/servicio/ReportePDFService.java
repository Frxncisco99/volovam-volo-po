package org.example.servicio;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.modelo.Producto;
import org.example.modelo.Ticket;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportePDFService {

    // ── Paleta ──────────────────────────────────────────────
    private static final BaseColor COLOR_PRIMARIO   = new BaseColor(107, 18,  40);  // #6B1228
    private static final BaseColor COLOR_DORADO     = new BaseColor(201,168, 76);   // #C9A84C
    private static final BaseColor COLOR_FILA_PAR   = new BaseColor(245,239,230);   // #F5EFE6
    private static final BaseColor COLOR_FILA_IMPAR = BaseColor.WHITE;
    private static final BaseColor COLOR_HEADER_TBL = new BaseColor(61, 26, 10);    // #3D1A0A
    private static final BaseColor COLOR_TEXTO      = new BaseColor(61, 26, 10);
    private static final BaseColor COLOR_GRIS       = new BaseColor(120,100, 85);

    // ── Fuentes ─────────────────────────────────────────────
    private static final Font FONT_TITULO    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  22, COLOR_PRIMARIO);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA,       10, COLOR_GRIS);
    private static final Font FONT_SECCION   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  11, COLOR_PRIMARIO);
    private static final Font FONT_TH        = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, BaseColor.WHITE);
    private static final Font FONT_TD        = FontFactory.getFont(FontFactory.HELVETICA,        9, COLOR_TEXTO);
    private static final Font FONT_CARD_LBL  = FontFactory.getFont(FontFactory.HELVETICA,        8, COLOR_GRIS);
    private static final Font FONT_CARD_VAL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  18, COLOR_PRIMARIO);
    private static final Font FONT_FOOTER    = FontFactory.getFont(FontFactory.HELVETICA,         8, COLOR_GRIS);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ════════════════════════════════════════════════════════
    //  REPORTE DE VENTAS
    // ════════════════════════════════════════════════════════
    public void generarReporteVentas(
            List<Ticket> tickets,
            double total, int cantidad, double promedio,
            Map<String, Integer> topProductos,
            String ruta) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        writer.setPageEvent(new FooterEvent("Reporte de Ventas"));
        doc.open();

        agregarHeader(doc, writer, "REPORTE DE VENTAS");
        agregarFecha(doc);

        // ── Tarjetas de resumen ──────────────────────────────
        doc.add(separador());

        PdfPTable cards = new PdfPTable(3);
        cards.setWidthPercentage(100);
        cards.setSpacingBefore(10);
        cards.setSpacingAfter(14);

        cards.addCell(crearCard("TOTAL VENDIDO",    String.format("$%,.2f", total)));
        cards.addCell(crearCard("TICKETS EMITIDOS", String.valueOf(cantidad)));
        cards.addCell(crearCard("TICKET PROMEDIO",  String.format("$%,.2f", promedio)));
        doc.add(cards);

        // ── Top productos ────────────────────────────────────
        doc.add(etiquetaSeccion("Productos más vendidos"));

        PdfPTable tabla = new PdfPTable(new float[]{70, 30});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(6);
        tabla.setSpacingAfter(20);

        agregarHeaderTabla(tabla, "Producto", "Cantidad");

        int fila = 0;
        for (Map.Entry<String, Integer> e : topProductos.entrySet()) {
            BaseColor bg = (fila++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
            tabla.addCell(celdaTD(e.getKey(),              bg, Element.ALIGN_LEFT));
            tabla.addCell(celdaTD(String.valueOf(e.getValue()), bg, Element.ALIGN_CENTER));
        }
        doc.add(tabla);

        // ── Lista de tickets ─────────────────────────────────
        doc.add(etiquetaSeccion("Detalle de tickets"));

        PdfPTable tTickets = new PdfPTable(new float[]{25, 45, 30});
        tTickets.setWidthPercentage(100);
        tTickets.setSpacingBefore(6);

        agregarHeaderTabla(tTickets, "Folio", "Fecha", "Total");

        fila = 0;
        for (Ticket t : tickets) {
            BaseColor bg = (fila++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
            tTickets.addCell(celdaTD(String.valueOf(t.getIdVenta()),         bg, Element.ALIGN_CENTER));
            tTickets.addCell(celdaTD(t.getFechaHora().format(FMT),           bg, Element.ALIGN_LEFT));
            tTickets.addCell(celdaTD(String.format("$%,.2f", t.getTotal()),  bg, Element.ALIGN_RIGHT));
        }
        doc.add(tTickets);

        doc.close();
    }

    // ════════════════════════════════════════════════════════
    //  TOP PRODUCTOS
    // ════════════════════════════════════════════════════════
    public void generarTopProductos(Map<String, Integer> top, String ruta) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        writer.setPageEvent(new FooterEvent("Top Productos"));
        doc.open();

        agregarHeader(doc, writer, "TOP PRODUCTOS");
        agregarFecha(doc);
        doc.add(separador());

        doc.add(etiquetaSeccion("Ranking por cantidad vendida"));

        PdfPTable tabla = new PdfPTable(new float[]{10, 65, 25});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);

        agregarHeaderTabla(tabla, "#", "Producto", "Cantidad");

        int pos = 1, fila = 0;
        for (Map.Entry<String, Integer> e : top.entrySet()) {
            BaseColor bg = (fila++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
            tabla.addCell(celdaTD(String.valueOf(pos++),       bg, Element.ALIGN_CENTER));
            tabla.addCell(celdaTD(e.getKey(),                  bg, Element.ALIGN_LEFT));
            tabla.addCell(celdaTD(String.valueOf(e.getValue()), bg, Element.ALIGN_CENTER));
        }

        doc.add(tabla);
        doc.close();
    }

    // ════════════════════════════════════════════════════════
    //  BAJO STOCK
    // ════════════════════════════════════════════════════════
    public void generarBajoStock(List<Producto> productos, String ruta) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        writer.setPageEvent(new FooterEvent("Bajo Stock"));
        doc.open();

        agregarHeader(doc, writer, "ALERTA: BAJO STOCK");
        agregarFecha(doc);
        doc.add(separador());

        doc.add(etiquetaSeccion("Productos que requieren reabastecimiento"));

        PdfPTable tabla = new PdfPTable(new float[]{70, 30});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);

        agregarHeaderTabla(tabla, "Producto", "Stock actual");

        int fila = 0;
        for (Producto p : productos) {
            BaseColor bg = (fila++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
            // Stock crítico resaltado en rojo suave
            BaseColor txtStock = p.getStock() <= 3
                    ? new BaseColor(180, 0, 0)
                    : COLOR_TEXTO;
            Font fStock = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, txtStock);

            tabla.addCell(celdaTD(p.getNombre(), bg, Element.ALIGN_LEFT));

            PdfPCell cStock = new PdfPCell(new Phrase(String.valueOf(p.getStock()), fStock));
            cStock.setBackgroundColor(bg);
            cStock.setHorizontalAlignment(Element.ALIGN_CENTER);
            cStock.setPadding(6);
            cStock.setBorderColor(new BaseColor(220, 210, 195));
            tabla.addCell(cStock);
        }

        doc.add(tabla);
        doc.close();
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ════════════════════════════════════════════════════════

    /** Banda superior con nombre de la empresa y título del reporte */
    private void agregarHeader(Document doc, PdfWriter writer, String titulo) throws Exception {
        PdfContentByte cb = writer.getDirectContent();

        // Banda roja de fondo
        float w = doc.getPageSize().getWidth();
        float top = doc.getPageSize().getHeight() - 30;
        cb.setColorFill(COLOR_PRIMARIO);
        cb.rectangle(0, top - 54, w, 84);
        cb.fill();

        // Línea dorada inferior de la banda
        cb.setColorFill(COLOR_DORADO);
        cb.rectangle(0, top - 54, w, 4);
        cb.fill();

        // Nombre empresa (blanco)
        Font fEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("VOLOVAN VOLO  ·  Sistema de Punto de Venta", fEmpresa),
                36, top + 24, 0);

        // Título
        Font fTit = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.WHITE);
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase(titulo, fTit),
                36, top - 12, 0);

        // Espacio para que el contenido no tape la banda
        doc.add(new Paragraph("\n\n\n"));
    }

    /** Línea con la fecha de generación */
    private void agregarFecha(Document doc) throws Exception {
        Paragraph p = new Paragraph(
                "Generado el " + LocalDateTime.now().format(FMT),
                FONT_SUBTITULO);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    /** Línea separadora dorada */
    private Paragraph separador() {
        Paragraph sep = new Paragraph(" ");
        sep.setSpacingBefore(2);
        sep.setSpacingAfter(2);
        return sep;  // el footer event dibuja la línea real si quieres; aquí es espaciado
    }

    /** Etiqueta de sección con barra izquierda de color */
    private PdfPTable etiquetaSeccion(String texto) throws Exception {
        PdfPTable t = new PdfPTable(new float[]{2, 98});
        t.setWidthPercentage(100);
        t.setSpacingBefore(14);
        t.setSpacingAfter(4);

        PdfPCell barra = new PdfPCell();
        barra.setBackgroundColor(COLOR_DORADO);
        barra.setBorder(Rectangle.NO_BORDER);
        barra.setFixedHeight(18);
        t.addCell(barra);

        PdfPCell lbl = new PdfPCell(new Phrase(" " + texto, FONT_SECCION));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setVerticalAlignment(Element.ALIGN_MIDDLE);
        lbl.setPaddingBottom(3);
        t.addCell(lbl);

        return t;
    }

    /** Tarjeta de resumen (para los 3 KPIs) */
    private PdfPCell crearCard(String etiqueta, String valor) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        PdfPCell cLbl = new PdfPCell(new Phrase(etiqueta, FONT_CARD_LBL));
        cLbl.setBorder(Rectangle.NO_BORDER);
        cLbl.setPaddingTop(10);
        cLbl.setPaddingLeft(10);
        inner.addCell(cLbl);

        PdfPCell cVal = new PdfPCell(new Phrase(valor, FONT_CARD_VAL));
        cVal.setBorder(Rectangle.NO_BORDER);
        cVal.setPaddingLeft(10);
        cVal.setPaddingBottom(10);
        inner.addCell(cVal);

        PdfPCell card = new PdfPCell(inner);
        card.setBackgroundColor(COLOR_FILA_PAR);
        card.setBorderColor(COLOR_DORADO);
        card.setBorderWidth(0.8f);
        card.setPadding(2);
        return card;
    }

    /** Fila de encabezado de tabla */
    private void agregarHeaderTabla(PdfPTable tabla, String... cols) {
        for (String col : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(col, FONT_TH));
            cell.setBackgroundColor(COLOR_HEADER_TBL);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(7);
            cell.setBorderColor(COLOR_PRIMARIO);
            tabla.addCell(cell);
        }
    }

    /** Celda de dato normal */
    private PdfPCell celdaTD(String texto, BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_TD));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setPadding(6);
        cell.setBorderColor(new BaseColor(220, 210, 195));
        cell.setBorderWidth(0.5f);
        return cell;
    }

    // ════════════════════════════════════════════════════════
    //  FOOTER CON NÚMERO DE PÁGINA
    // ════════════════════════════════════════════════════════
    static class FooterEvent extends PdfPageEventHelper {
        private final String titulo;
        FooterEvent(String titulo) { this.titulo = titulo; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float y = document.bottom() - 20;
            float w = document.getPageSize().getWidth();

            // Línea
            cb.setColorStroke(new BaseColor(201, 168, 76));
            cb.setLineWidth(0.5f);
            cb.moveTo(36, y + 10);
            cb.lineTo(w - 36, y + 10);
            cb.stroke();

            Font f = FontFactory.getFont(FontFactory.HELVETICA, 7, new BaseColor(120, 100, 85));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("Volovan Volo  ·  " + titulo, f), 36, y, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), f), w - 36, y, 0);
        }


    }

    public void generarCorteCaja(
            String cajero,
            String numeroCaja,
            String horaApertura,
            String fechaCierre,
            double fondoInicial,
            double totalVentas,
            double totalEntradas,
            double totalSalidas,
            double dineroEsperado,
            double dineroContado,
            double diferencia,
            int numTickets,
            String observaciones,
            String ruta
    ) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        writer.setPageEvent(new FooterEvent("Corte de Caja"));
        doc.open();

        agregarHeader(doc, writer, "CORTE DE CAJA");
        agregarFecha(doc);
        doc.add(separador());

        // ── Info de la caja ──────────────────────────────────
        PdfPTable infoTable = new PdfPTable(new float[]{50, 50});
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(14);

        infoTable.addCell(crearCard("CAJERO",       cajero));
        infoTable.addCell(crearCard("CAJA",         numeroCaja));
        infoTable.addCell(crearCard("HORA APERTURA", horaApertura));
        infoTable.addCell(crearCard("HORA CIERRE",   fechaCierre));
        doc.add(infoTable);

        // ── Resumen financiero ───────────────────────────────
        doc.add(etiquetaSeccion("Resumen financiero"));

        PdfPTable resumen = new PdfPTable(new float[]{70, 30});
        resumen.setWidthPercentage(100);
        resumen.setSpacingBefore(6);
        resumen.setSpacingAfter(14);

        agregarHeaderTabla(resumen, "Concepto", "Monto");

        Object[][] filas = {
                { "Fondo inicial",         fondoInicial },
                { "Total ventas efectivo", totalVentas  },
                { "Entradas a caja",       totalEntradas},
                { "Salidas de caja",       totalSalidas },
        };

        int f = 0;
        for (Object[] fila : filas) {
            BaseColor bg = (f++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
            resumen.addCell(celdaTD((String) fila[0],                          bg, Element.ALIGN_LEFT));
            resumen.addCell(celdaTD(String.format("$%,.2f", (double) fila[1]), bg, Element.ALIGN_RIGHT));
        }
        doc.add(resumen);

        // ── Cards de cierre ──────────────────────────────────
        PdfPTable cards = new PdfPTable(3);
        cards.setWidthPercentage(100);
        cards.setSpacingBefore(6);
        cards.setSpacingAfter(14);

        cards.addCell(crearCard("NO. TICKETS",      String.valueOf(numTickets)));
        cards.addCell(crearCard("DINERO ESPERADO",  String.format("$%,.2f", dineroEsperado)));
        cards.addCell(crearCard("DINERO CONTADO",   String.format("$%,.2f", dineroContado)));
        doc.add(cards);

        // ── Diferencia con color ─────────────────────────────
        doc.add(etiquetaSeccion("Resultado del corte"));

        PdfPTable tDif = new PdfPTable(new float[]{70, 30});
        tDif.setWidthPercentage(100);
        tDif.setSpacingBefore(6);
        tDif.setSpacingAfter(14);

        BaseColor colorDif = diferencia == 0
                ? new BaseColor(46, 125, 50)   // verde
                : diferencia < 0
                ? new BaseColor(192, 57, 43)  // rojo
                : new BaseColor(26, 109, 181); // azul

        String estadoTexto = diferencia == 0 ? "✔ Todo correcto"
                : diferencia < 0  ? "⚠ Falta dinero"
                : "⚠ Sobra dinero";

        Font fDif = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, colorDif);

        PdfPCell cConcepto = new PdfPCell(new Phrase("Diferencia", FONT_TD));
        cConcepto.setBackgroundColor(COLOR_FILA_PAR);
        cConcepto.setPadding(8);
        cConcepto.setBorderColor(new BaseColor(220, 210, 195));

        PdfPCell cValor = new PdfPCell(new Phrase(
                String.format("$%,.2f  %s", diferencia, estadoTexto), fDif));
        cValor.setBackgroundColor(COLOR_FILA_PAR);
        cValor.setPadding(8);
        cValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cValor.setBorderColor(new BaseColor(220, 210, 195));

        tDif.addCell(cConcepto);
        tDif.addCell(cValor);
        doc.add(tDif);

        // ── Observaciones ────────────────────────────────────
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            doc.add(etiquetaSeccion("Observaciones"));

            PdfPTable tObs = new PdfPTable(1);
            tObs.setWidthPercentage(100);
            tObs.setSpacingBefore(6);

            PdfPCell cObs = new PdfPCell(new Phrase(observaciones, FONT_TD));
            cObs.setBackgroundColor(COLOR_FILA_PAR);
            cObs.setPadding(10);
            cObs.setBorderColor(new BaseColor(220, 210, 195));
            tObs.addCell(cObs);
            doc.add(tObs);
        }

        doc.close();
    }
}