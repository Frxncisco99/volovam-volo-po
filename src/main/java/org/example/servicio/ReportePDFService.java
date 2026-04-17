package org.example.servicio;

// ── iText — imports explícitos (evita conflicto con java.util.List) ──────────
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.example.modelo.Producto;
import org.example.modelo.Ticket;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportePDFService {

    // ── Paleta ──────────────────────────────────────────────
    private static final BaseColor COLOR_PRIMARIO   = new BaseColor(107, 18,  40);
    private static final BaseColor COLOR_DORADO     = new BaseColor(201,168, 76);
    private static final BaseColor COLOR_VERDE      = new BaseColor(46, 125, 50);
    private static final BaseColor COLOR_ROJO       = new BaseColor(192, 57, 43);
    private static final BaseColor COLOR_FILA_PAR   = new BaseColor(245,239,230);
    private static final BaseColor COLOR_FILA_IMPAR = BaseColor.WHITE;
    private static final BaseColor COLOR_HEADER_TBL = new BaseColor(61, 26, 10);
    private static final BaseColor COLOR_TEXTO      = new BaseColor(61, 26, 10);
    private static final BaseColor COLOR_GRIS       = new BaseColor(120,100, 85);
    private static final BaseColor COLOR_BARRA      = new BaseColor(107, 18,  40);
    private static final BaseColor COLOR_BARRA_BG   = new BaseColor(245,239,230);

    // ── Fuentes ─────────────────────────────────────────────
    private static final Font FONT_TITULO    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  22, COLOR_PRIMARIO);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA,       10, COLOR_GRIS);
    private static final Font FONT_SECCION   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  11, COLOR_PRIMARIO);
    private static final Font FONT_TH        = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, BaseColor.WHITE);
    private static final Font FONT_TD        = FontFactory.getFont(FontFactory.HELVETICA,        9, COLOR_TEXTO);
    private static final Font FONT_CARD_LBL  = FontFactory.getFont(FontFactory.HELVETICA,        8, COLOR_GRIS);
    private static final Font FONT_CARD_VAL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  16, COLOR_PRIMARIO);
    private static final Font FONT_CARD_VAL2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  16, COLOR_VERDE);
    private static final Font FONT_FOOTER    = FontFactory.getFont(FontFactory.HELVETICA,         8, COLOR_GRIS);
    private static final Font FONT_BARRA_LBL = FontFactory.getFont(FontFactory.HELVETICA,         7, COLOR_TEXTO);
    private static final Font FONT_BARRA_VAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    7, COLOR_PRIMARIO);

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

        // ── RESUMEN EJECUTIVO ────────────────────────────────
        doc.add(etiquetaSeccion("Resumen ejecutivo"));

        double costoTotal = 0;
        Map<String, double[]> detallePorProducto = new LinkedHashMap<>();
        // double[] = { unidades, ingresos, costoUnitario }

        for (Ticket t : tickets) {
            if (t.getLineas() == null) continue;
            for (Ticket.LineaTicket l : t.getLineas()) {
                String nombre   = l.getNombreProducto();
                double subtotal = l.getSubtotal();
                double costo    = l.getCostoUnitario() * l.getCantidad();
                costoTotal += costo;

                detallePorProducto.merge(
                        nombre,
                        new double[]{ l.getCantidad(), subtotal, l.getCostoUnitario() },
                        (a, b) -> new double[]{ a[0] + b[0], a[1] + b[1], b[2] }
                );
            }
        }
        double gananciaTotal = total - costoTotal;
        double margen = total > 0 ? (gananciaTotal / total * 100) : 0;

        PdfPTable cards = new PdfPTable(3);
        cards.setWidthPercentage(100);
        cards.setSpacingBefore(8);
        cards.setSpacingAfter(4);
        cards.addCell(crearCard("TOTAL VENTAS",     String.format("$%,.2f", total),    false));
        cards.addCell(crearCard("TICKETS EMITIDOS", String.valueOf(cantidad),           false));
        cards.addCell(crearCard("TICKET PROMEDIO",  String.format("$%,.2f", promedio), false));
        doc.add(cards);

        PdfPTable cards2 = new PdfPTable(3);
        cards2.setWidthPercentage(100);
        cards2.setSpacingBefore(4);
        cards2.setSpacingAfter(14);
        cards2.addCell(crearCard("COSTO TOTAL",        String.format("$%,.2f", costoTotal),    false));
        cards2.addCell(crearCard("GANANCIA NETA",      String.format("$%,.2f", gananciaTotal), true));
        cards2.addCell(crearCard("MARGEN DE GANANCIA", String.format("%.1f%%",  margen),       true));
        doc.add(cards2);

        // ── GRÁFICA ──────────────────────────────────────────
        doc.add(etiquetaSeccion("Gráfica — Productos más vendidos (unidades)"));
        doc.add(construirGraficaBarras(topProductos, writer));

        // ── DESGLOSE POR PRODUCTO ────────────────────────────
        doc.add(etiquetaSeccion("Desglose por producto"));

        PdfPTable tDesglose = new PdfPTable(new float[]{30, 12, 16, 16, 14, 12});
        tDesglose.setWidthPercentage(100);
        tDesglose.setSpacingBefore(6);
        tDesglose.setSpacingAfter(20);
        agregarHeaderTabla(tDesglose,
                "Producto", "Unids.", "Ingresos", "Costo total", "Ganancia", "% Total");

        List<Map.Entry<String, double[]>> listaDesglose = new ArrayList<>(detallePorProducto.entrySet());
        listaDesglose.sort((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1]));

        int filaD = 0;
        for (Map.Entry<String, double[]> e : listaDesglose) {
            BaseColor bg  = (filaD++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
            double[] v    = e.getValue();
            int    unids  = (int) v[0];
            double ing    = v[1];
            double costoP = v[2] * unids;
            double ganP   = ing - costoP;
            double pct    = total > 0 ? (ing / total * 100) : 0;

            tDesglose.addCell(celdaTD(e.getKey(),                      bg, Element.ALIGN_LEFT));
            tDesglose.addCell(celdaTD(String.valueOf(unids),           bg, Element.ALIGN_CENTER));
            tDesglose.addCell(celdaTD(String.format("$%,.2f", ing),    bg, Element.ALIGN_RIGHT));
            tDesglose.addCell(celdaTD(String.format("$%,.2f", costoP), bg, Element.ALIGN_RIGHT));
            tDesglose.addCell(celdaGanancia(ganP,                      bg));
            tDesglose.addCell(celdaTD(String.format("%.1f%%", pct),    bg, Element.ALIGN_CENTER));
        }
        doc.add(tDesglose);

        // ── TABLA DE TICKETS ─────────────────────────────────
        doc.add(etiquetaSeccion("Detalle de tickets"));

        PdfPTable tTickets = new PdfPTable(new float[]{18, 38, 22, 22});
        tTickets.setWidthPercentage(100);
        tTickets.setSpacingBefore(6);
        tTickets.setSpacingAfter(14);
        agregarHeaderTabla(tTickets, "Folio", "Fecha", "Productos", "Total");

        int filaT = 0;
        for (Ticket t : tickets) {
            BaseColor bg     = (filaT++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
            int numProductos = t.getLineas() != null ? t.getLineas().size() : 0;
            tTickets.addCell(celdaTD(String.valueOf(t.getIdVenta()),         bg, Element.ALIGN_CENTER));
            tTickets.addCell(celdaTD(t.getFechaHora().format(FMT),          bg, Element.ALIGN_LEFT));
            tTickets.addCell(celdaTD(numProductos + " artículo(s)",         bg, Element.ALIGN_CENTER));
            tTickets.addCell(celdaTD(String.format("$%,.2f", t.getTotal()), bg, Element.ALIGN_RIGHT));
        }
        doc.add(tTickets);

        // ── TOTALES AL PIE ───────────────────────────────────
        PdfPTable tTotales = new PdfPTable(new float[]{56, 22, 22});
        tTotales.setWidthPercentage(100);
        tTotales.setSpacingBefore(0);
        tTotales.setSpacingAfter(14);

        Font fTot = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);

        PdfPCell cVacio = new PdfPCell(new Phrase("TOTALES", fTot));
        cVacio.setBackgroundColor(COLOR_PRIMARIO);
        cVacio.setPadding(6);
        cVacio.setBorderColor(COLOR_PRIMARIO);
        tTotales.addCell(cVacio);

        PdfPCell cCantTot = new PdfPCell(new Phrase(cantidad + " tickets", fTot));
        cCantTot.setBackgroundColor(COLOR_PRIMARIO);
        cCantTot.setHorizontalAlignment(Element.ALIGN_CENTER);
        cCantTot.setPadding(6);
        cCantTot.setBorderColor(COLOR_PRIMARIO);
        tTotales.addCell(cCantTot);

        PdfPCell cTotalTot = new PdfPCell(new Phrase(String.format("$%,.2f", total), fTot));
        cTotalTot.setBackgroundColor(COLOR_PRIMARIO);
        cTotalTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cTotalTot.setPadding(6);
        cTotalTot.setBorderColor(COLOR_PRIMARIO);
        tTotales.addCell(cTotalTot);

        doc.add(tTotales);
        doc.close();
    }

    // ════════════════════════════════════════════════════════
    //  GRÁFICA DE BARRAS HORIZONTAL
    // ════════════════════════════════════════════════════════
    private PdfPTable construirGraficaBarras(Map<String, Integer> datos, PdfWriter writer) throws Exception {

        List<Map.Entry<String, Integer>> lista = new ArrayList<>(datos.entrySet());
        if (lista.size() > 8) lista = lista.subList(0, 8);

        int maxVal = lista.stream().mapToInt(Map.Entry::getValue).max().orElse(1);

        PdfPTable grafica = new PdfPTable(new float[]{22, 64, 14});
        grafica.setWidthPercentage(96);
        grafica.setSpacingBefore(6);
        grafica.setSpacingAfter(16);

        for (Map.Entry<String, Integer> e : lista) {
            String nombre = e.getKey();
            int    valor  = e.getValue();
            float  pct    = (float) valor / maxVal;

            String label = nombre.length() > 20 ? nombre.substring(0, 18) + "…" : nombre;
            PdfPCell cLabel = new PdfPCell(new Phrase(label, FONT_BARRA_LBL));
            cLabel.setBorder(Rectangle.NO_BORDER);
            cLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cLabel.setPaddingRight(4);
            cLabel.setPaddingBottom(5);
            grafica.addCell(cLabel);

            PdfPTable barraInterna = new PdfPTable(new float[]{pct, 1f - pct + 0.001f});
            barraInterna.setWidthPercentage(100);

            PdfPCell relleno = new PdfPCell(new Phrase(" "));
            relleno.setBackgroundColor(COLOR_BARRA);
            relleno.setBorder(Rectangle.NO_BORDER);
            relleno.setFixedHeight(12);
            barraInterna.addCell(relleno);

            PdfPCell vacio = new PdfPCell(new Phrase(" "));
            vacio.setBackgroundColor(COLOR_BARRA_BG);
            vacio.setBorder(Rectangle.NO_BORDER);
            vacio.setFixedHeight(12);
            barraInterna.addCell(vacio);

            PdfPCell cBarra = new PdfPCell(barraInterna);
            cBarra.setBorder(Rectangle.NO_BORDER);
            cBarra.setPaddingBottom(5);
            cBarra.setVerticalAlignment(Element.ALIGN_MIDDLE);
            grafica.addCell(cBarra);

            PdfPCell cVal = new PdfPCell(new Phrase(String.valueOf(valor), FONT_BARRA_VAL));
            cVal.setBorder(Rectangle.NO_BORDER);
            cVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cVal.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cVal.setPaddingBottom(5);
            grafica.addCell(cVal);
        }

        return grafica;
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

        doc.add(etiquetaSeccion("Gráfica de barras"));
        doc.add(construirGraficaBarras(top, writer));

        doc.add(etiquetaSeccion("Ranking por cantidad vendida"));

        PdfPTable tabla = new PdfPTable(new float[]{10, 65, 25});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        agregarHeaderTabla(tabla, "#", "Producto", "Cantidad");

        int pos = 1, fila = 0;
        for (Map.Entry<String, Integer> e : top.entrySet()) {
            BaseColor bg = (fila++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
            tabla.addCell(celdaTD(String.valueOf(pos++),        bg, Element.ALIGN_CENTER));
            tabla.addCell(celdaTD(e.getKey(),                   bg, Element.ALIGN_LEFT));
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
            BaseColor bg       = (fila++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
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
    //  CORTE DE CAJA
    // ════════════════════════════════════════════════════════
    public void generarCorteCaja(
            String cajero, String numeroCaja,
            String horaApertura, String fechaCierre,
            double fondoInicial, double totalVentas,
            double totalEntradas, double totalSalidas,
            double dineroEsperado, double dineroContado,
            double diferencia, int numTickets,
            String observaciones, String ruta) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        writer.setPageEvent(new FooterEvent("Corte de Caja"));
        doc.open();

        agregarHeader(doc, writer, "CORTE DE CAJA");
        agregarFecha(doc);
        doc.add(separador());

        PdfPTable infoTable = new PdfPTable(new float[]{50, 50});
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(14);
        infoTable.addCell(crearCard("CAJERO",        cajero));
        infoTable.addCell(crearCard("CAJA",          numeroCaja));
        infoTable.addCell(crearCard("HORA APERTURA", horaApertura));
        infoTable.addCell(crearCard("HORA CIERRE",   fechaCierre));
        doc.add(infoTable);

        doc.add(etiquetaSeccion("Resumen financiero"));

        PdfPTable resumen = new PdfPTable(new float[]{70, 30});
        resumen.setWidthPercentage(100);
        resumen.setSpacingBefore(6);
        resumen.setSpacingAfter(14);
        agregarHeaderTabla(resumen, "Concepto", "Monto");

        Object[][] filasData = {
                { "Fondo inicial",         fondoInicial  },
                { "Total ventas efectivo", totalVentas   },
                { "Entradas a caja",       totalEntradas },
                { "Salidas de caja",       totalSalidas  },
        };

        int f = 0;
        for (Object[] fila : filasData) {
            BaseColor bg = (f++ % 2 == 0) ? COLOR_FILA_IMPAR : COLOR_FILA_PAR;
            resumen.addCell(celdaTD((String) fila[0],                          bg, Element.ALIGN_LEFT));
            resumen.addCell(celdaTD(String.format("$%,.2f", (double) fila[1]), bg, Element.ALIGN_RIGHT));
        }
        doc.add(resumen);

        PdfPTable cards = new PdfPTable(3);
        cards.setWidthPercentage(100);
        cards.setSpacingBefore(6);
        cards.setSpacingAfter(14);
        cards.addCell(crearCard("NO. TICKETS",     String.valueOf(numTickets)));
        cards.addCell(crearCard("DINERO ESPERADO", String.format("$%,.2f", dineroEsperado)));
        cards.addCell(crearCard("DINERO CONTADO",  String.format("$%,.2f", dineroContado)));
        doc.add(cards);

        doc.add(etiquetaSeccion("Resultado del corte"));

        PdfPTable tDif = new PdfPTable(new float[]{70, 30});
        tDif.setWidthPercentage(100);
        tDif.setSpacingBefore(6);
        tDif.setSpacingAfter(14);

        BaseColor colorDif = diferencia == 0
                ? new BaseColor(46, 125, 50)
                : diferencia < 0
                ? new BaseColor(192, 57, 43)
                : new BaseColor(26, 109, 181);

        String estadoTexto = diferencia == 0 ? "✔ Todo correcto"
                : diferencia < 0             ? "⚠ Falta dinero"
                :                              "⚠ Sobra dinero";

        Font fDif = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, colorDif);

        PdfPCell cConcepto = new PdfPCell(new Phrase("Diferencia", FONT_TD));
        cConcepto.setBackgroundColor(COLOR_FILA_PAR);
        cConcepto.setPadding(8);
        cConcepto.setBorderColor(new BaseColor(220, 210, 195));

        PdfPCell cValor = new PdfPCell(
                new Phrase(String.format("$%,.2f  %s", diferencia, estadoTexto), fDif));
        cValor.setBackgroundColor(COLOR_FILA_PAR);
        cValor.setPadding(8);
        cValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cValor.setBorderColor(new BaseColor(220, 210, 195));

        tDif.addCell(cConcepto);
        tDif.addCell(cValor);
        doc.add(tDif);

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

    // ════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ════════════════════════════════════════════════════════

    private void agregarHeader(Document doc, PdfWriter writer, String titulo) throws Exception {
        PdfContentByte cb = writer.getDirectContent();
        float w   = doc.getPageSize().getWidth();
        float top = doc.getPageSize().getHeight() - 30;

        cb.setColorFill(COLOR_PRIMARIO);
        cb.rectangle(0, top - 54, w, 84);
        cb.fill();

        cb.setColorFill(COLOR_DORADO);
        cb.rectangle(0, top - 54, w, 4);
        cb.fill();

        Font fEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("VOLOVAN VOLO  ·  Sistema de Punto de Venta", fEmpresa),
                36, top + 24, 0);

        Font fTit = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.WHITE);
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase(titulo, fTit),
                36, top - 12, 0);

        doc.add(new Paragraph("\n\n\n"));
    }

    private void agregarFecha(Document doc) throws Exception {
        Paragraph p = new Paragraph(
                "Generado el " + LocalDateTime.now().format(FMT), FONT_SUBTITULO);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private Paragraph separador() {
        Paragraph sep = new Paragraph(" ");
        sep.setSpacingBefore(2);
        sep.setSpacingAfter(2);
        return sep;
    }

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

    private PdfPCell crearCard(String etiqueta, String valor, boolean isGanancia) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        PdfPCell cLbl = new PdfPCell(new Phrase(etiqueta, FONT_CARD_LBL));
        cLbl.setBorder(Rectangle.NO_BORDER);
        cLbl.setPaddingTop(10);
        cLbl.setPaddingLeft(10);
        inner.addCell(cLbl);

        Font valFont = isGanancia ? FONT_CARD_VAL2 : FONT_CARD_VAL;
        PdfPCell cVal = new PdfPCell(new Phrase(valor, valFont));
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

    private PdfPCell crearCard(String etiqueta, String valor) {
        return crearCard(etiqueta, valor, false);
    }

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

    private PdfPCell celdaTD(String texto, BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_TD));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setPadding(6);
        cell.setBorderColor(new BaseColor(220, 210, 195));
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell celdaGanancia(double ganancia, BaseColor bg) {
        BaseColor color = ganancia >= 0 ? COLOR_VERDE : COLOR_ROJO;
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, color);
        PdfPCell cell = new PdfPCell(new Phrase(String.format("$%,.2f", ganancia), f));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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
}