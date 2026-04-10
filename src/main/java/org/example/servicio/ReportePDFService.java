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

    public void generarReporteVentas(
            List<Ticket> tickets,
            double total,
            int cantidad,
            double promedio,
            Map<String, Integer> topProductos,
            String ruta
    ) throws Exception {

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        doc.open();

        // 🧾 Título
        doc.add(new Paragraph("REPORTE DE VENTAS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        doc.add(new Paragraph("Fecha: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        doc.add(new Paragraph(" "));

        // 📊 Resumen
        doc.add(new Paragraph("Total vendido: $" + total));
        doc.add(new Paragraph("Tickets: " + cantidad));
        doc.add(new Paragraph("Promedio: $" + promedio));
        doc.add(new Paragraph(" "));

        // 🔝 Top productos
        doc.add(new Paragraph("Productos más vendidos:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

        PdfPTable tabla = new PdfPTable(2);
        tabla.addCell("Producto");
        tabla.addCell("Cantidad");

        for (Map.Entry<String, Integer> e : topProductos.entrySet()) {
            tabla.addCell(e.getKey());
            tabla.addCell(String.valueOf(e.getValue()));
        }

        doc.add(tabla);

        doc.close();
    }

    public void generarTopProductos(Map<String, Integer> top, String ruta) throws Exception {
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        doc.open();

        doc.add(new Paragraph("TOP PRODUCTOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        doc.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(2);
        tabla.addCell("Producto");
        tabla.addCell("Cantidad");

        for (var e : top.entrySet()) {
            tabla.addCell(e.getKey());
            tabla.addCell(String.valueOf(e.getValue()));
        }

        doc.add(tabla);
        doc.close();
    }

    public void generarBajoStock(List<Producto> productos, String ruta) throws Exception {
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        doc.open();

        doc.add(new Paragraph("PRODUCTOS CON BAJO STOCK", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        doc.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(2);
        tabla.addCell("Producto");
        tabla.addCell("Stock");

        for (Producto p : productos) {
            tabla.addCell(p.getNombre());
            tabla.addCell(String.valueOf(p.getStock()));
        }

        doc.add(tabla);
        doc.close();
    }
}