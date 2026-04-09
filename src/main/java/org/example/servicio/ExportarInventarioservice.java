package org.example.servicio;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.modelo.Producto;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportarInventarioservice {

    // Paleta Volovan Volo
    private static final BaseColor COLOR_HEADER     = new BaseColor(61,  28,  2);   // #3D1C02
    private static final BaseColor COLOR_ACENTO     = new BaseColor(201, 168, 76);  // #C9A84C
    private static final BaseColor COLOR_FILA_PAR   = new BaseColor(245, 239, 230); // #F5EFE6
    private static final BaseColor COLOR_FILA_IMPAR = new BaseColor(237, 232, 220); // #EDE8DC
    private static final BaseColor COLOR_BAJO_STOCK = new BaseColor(163, 45,  45);  // #A32D2D
    private static final BaseColor COLOR_TEXTO_OSC  = new BaseColor(61,  26,  10);  // #3D1A0A
    private static final BaseColor COLOR_BLANCO     = BaseColor.WHITE;

    private static final Font FONT_TITULO    = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   COLOR_BLANCO);
    private static final Font FONT_SUBTITULO = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(160, 120, 72));
    private static final Font FONT_COL_HEAD  = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD,   COLOR_BLANCO);
    private static final Font FONT_CELDA     = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, COLOR_TEXTO_OSC);
    private static final Font FONT_CELDA_ID  = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, new BaseColor(139, 74, 90));
    private static final Font FONT_CELDA_RED = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD,   COLOR_BAJO_STOCK);
    private static final Font FONT_RESUMEN_L = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, new BaseColor(139, 74, 90));
    private static final Font FONT_RESUMEN_V = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   new BaseColor(107, 18, 40));
    private static final Font FONT_FOOTER    = new Font(Font.FontFamily.HELVETICA, 8,  Font.NORMAL, new BaseColor(160, 120, 72));

    /**
     * Genera el PDF.
     *
     * @param productos  lista de productos a incluir (filtrada o completa según elección del usuario)
     * @param rutaSalida ruta completa del archivo destino, ej. "/home/user/inventario.pdf"
     */
    public void exportar(List<Producto> productos, String rutaSalida) throws Exception {

        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(rutaSalida));

        // Footer con número de página
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                PdfContentByte cb = w.getDirectContent();
                String texto = "Volovan Volo · Inventario exportado el "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        + "   |   Página " + w.getPageNumber();
                Phrase pie = new Phrase(texto, FONT_FOOTER);
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, pie,
                        (d.left() + d.right()) / 2, d.bottom() - 15, 0);
            }
        });

        doc.open();

        // ── HEADER ──────────────────────────────────────────────────────────
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(COLOR_HEADER);
        hCell.setPadding(14);
        hCell.setBorder(Rectangle.NO_BORDER);

        Paragraph titulo = new Paragraph("Volovan Volo", FONT_TITULO);
        titulo.setSpacingAfter(2);
        Paragraph subtitulo = new Paragraph(
                "Reporte de Inventario · " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy, HH:mm")),
                FONT_SUBTITULO);

        hCell.addElement(titulo);
        hCell.addElement(subtitulo);
        header.addCell(hCell);
        header.setSpacingAfter(16);
        doc.add(header);

        // ── TARJETAS RESUMEN ─────────────────────────────────────────────────
        int total   = productos.size();
        int bajo    = (int) productos.stream().filter(p -> p.getStock() < 5).count();
        double valor = productos.stream().mapToDouble(p -> p.getPrecio() * p.getStock()).sum();

        PdfPTable resumen = new PdfPTable(3);
        resumen.setWidthPercentage(60);
        resumen.setHorizontalAlignment(Element.ALIGN_LEFT);
        resumen.setSpacingAfter(18);

        agregarTarjetaResumen(resumen, "TOTAL PRODUCTOS", String.valueOf(total), false);
        agregarTarjetaResumen(resumen, "STOCK BAJO",      String.valueOf(bajo),  bajo > 0);
        agregarTarjetaResumen(resumen, "VALOR TOTAL",     String.format("$%.2f", valor), false);
        doc.add(resumen);

        // ── TABLA DE PRODUCTOS ───────────────────────────────────────────────
        PdfPTable tabla = new PdfPTable(new float[]{6, 20, 13, 10, 8, 11});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);

        String[] cabeceras = {"ID", "NOMBRE", "CATEGORÍA", "PRECIO", "STOCK", "ESTADO"};
        for (String cab : cabeceras) {
            PdfPCell c = new PdfPCell(new Phrase(cab, FONT_COL_HEAD));
            c.setBackgroundColor(COLOR_HEADER);
            c.setPadding(8);
            c.setBorder(Rectangle.NO_BORDER);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(c);
        }

        for (int i = 0; i < productos.size(); i++) {
            Producto p   = productos.get(i);
            BaseColor bg = (i % 2 == 0) ? COLOR_FILA_PAR : COLOR_FILA_IMPAR;
            boolean bajo_stock = p.getStock() < 5;

            agregarCelda(tabla, "#" + String.format("%03d", p.getIdProducto()),
                    FONT_CELDA_ID, bg, Element.ALIGN_CENTER);
            agregarCelda(tabla, p.getNombre(),
                    new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_TEXTO_OSC), bg, Element.ALIGN_LEFT);
            agregarCelda(tabla, p.getCategoria(),   FONT_CELDA, bg, Element.ALIGN_CENTER);
            agregarCelda(tabla, String.format("$%.2f", p.getPrecio()), FONT_CELDA, bg, Element.ALIGN_RIGHT);
            agregarCelda(tabla, String.valueOf(p.getStock()),
                    bajo_stock ? FONT_CELDA_RED : FONT_CELDA, bg, Element.ALIGN_CENTER);

            // Badge de estado
            PdfPCell estadoCell = new PdfPCell();
            estadoCell.setBackgroundColor(bg);
            estadoCell.setBorder(Rectangle.NO_BORDER);
            estadoCell.setPadding(6);
            estadoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            estadoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPTable badge = new PdfPTable(1);
            badge.setWidthPercentage(90);
            PdfPCell badgeCell = new PdfPCell(new Phrase(
                    bajo_stock ? "⚠ Bajo Stock" : "Stock OK",
                    new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD,
                            bajo_stock ? COLOR_BAJO_STOCK : new BaseColor(26, 92, 46))));
            badgeCell.setBackgroundColor(bajo_stock
                    ? new BaseColor(247, 224, 224)
                    : new BaseColor(212, 237, 218));
            badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            badgeCell.setPadding(4);
            badgeCell.setBorder(Rectangle.NO_BORDER);
            badge.addCell(badgeCell);
            estadoCell.addElement(badge);
            tabla.addCell(estadoCell);
        }

        doc.add(tabla);
        doc.close();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void agregarTarjetaResumen(PdfPTable tabla, String etiqueta, String valor, boolean alerta) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(COLOR_ACENTO);
        cell.setBorderWidth(0.5f);
        cell.setBackgroundColor(new BaseColor(245, 239, 230));
        cell.setPadding(10);

        Paragraph lbl = new Paragraph(etiqueta, FONT_RESUMEN_L);
        lbl.setSpacingAfter(4);
        Font fv = alerta
                ? new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, COLOR_BAJO_STOCK)
                : FONT_RESUMEN_V;
        Paragraph val = new Paragraph(valor, fv);

        cell.addElement(lbl);
        cell.addElement(val);
        tabla.addCell(cell);
    }

    private void agregarCelda(PdfPTable tabla, String texto, Font font,
                              BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(7);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tabla.addCell(cell);
    }
}