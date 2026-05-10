package org.example.servicio;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.example.modelo.Producto;
import org.example.modelo.Ticket;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Genera reportes Excel (.xlsx) con la misma estructura y nivel de detalle
 * que ReportePDFService. Mismas secciones, mismos datos, misma paleta de colores.
 *
 * Métodos públicos (espejo exacto del PDF):
 *   generarReporteVentas(...)   → 4 hojas: Resumen, Desglose, Tickets, Top Productos
 *   generarTopProductos(...)    → 1 hoja: ranking con gráfica de barras ASCII
 *   generarBajoStock(...)       → 1 hoja: alertas con colores por criticidad
 *   generarCorteCaja(...)       → 1 hoja: resumen financiero del corte
 */
public class ReporteExcelService {

    // ── Paleta — mismos valores RGB que ReportePDFService ───────────────────
    private static final byte[] RGB_PRIMARIO   = { (byte)107, (byte)18,  (byte)40  };
    private static final byte[] RGB_DORADO     = { (byte)201, (byte)168, (byte)76  };
    private static final byte[] RGB_HEADER_TBL = { (byte)61,  (byte)26,  (byte)10  };
    private static final byte[] RGB_FILA_PAR   = { (byte)245, (byte)239, (byte)230 };
    private static final byte[] RGB_FILA_IMPAR = { (byte)255, (byte)255, (byte)255 };
    private static final byte[] RGB_VERDE      = { (byte)46,  (byte)125, (byte)50  };
    private static final byte[] RGB_ROJO       = { (byte)192, (byte)57,  (byte)43  };
    private static final byte[] RGB_AZUL       = { (byte)26,  (byte)111, (byte)168 };
    private static final byte[] RGB_GRIS       = { (byte)120, (byte)100, (byte)85  };
    private static final byte[] RGB_TEXTO      = { (byte)61,  (byte)26,  (byte)10  };
    private static final byte[] RGB_BLANCO     = { (byte)255, (byte)255, (byte)255 };
    private static final byte[] RGB_CARD_BG    = { (byte)245, (byte)239, (byte)230 };

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ════════════════════════════════════════════════════════════════════════
    //  1. REPORTE DE VENTAS
    //     Espejo de: ReportePDFService.generarReporteVentas()
    //     Secciones: Resumen ejecutivo · Gráfica barras · Desglose por producto
    //                · Detalle de tickets · Totales al pie
    // ════════════════════════════════════════════════════════════════════════
    public void generarReporteVentas(
            List<Ticket> tickets,
            double total, int cantidad, double promedio,
            Map<String, Integer> topProductos,
            String ruta) throws Exception {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // ── Calcular costo/ganancia (igual que en el PDF) ───────────────
            double costoTotal = 0;
            Map<String, double[]> detallePorProducto = new LinkedHashMap<>();
            // double[] = { unidades, ingresos, costoUnitario }
            for (Ticket t : tickets) {
                if (t.getLineas() == null) continue;
                for (Ticket.LineaTicket l : t.getLineas()) {
                    double costo = l.getCostoUnitario() * l.getCantidad();
                    costoTotal += costo;
                    detallePorProducto.merge(
                            l.getNombreProducto(),
                            new double[]{ l.getCantidad(), l.getSubtotal(), l.getCostoUnitario() },
                            (a, b) -> new double[]{ a[0]+b[0], a[1]+b[1], b[2] }
                    );
                }
            }
            double gananciaTotal = total - costoTotal;
            double margen = total > 0 ? (gananciaTotal / total * 100) : 0;

            // ── HOJA 1: Resumen ejecutivo ────────────────────────────────────
            XSSFSheet hResumen = wb.createSheet("Resumen Ejecutivo");
            hResumen.setColumnWidth(0, 30 * 256);
            hResumen.setColumnWidth(1, 22 * 256);
            hResumen.setColumnWidth(2, 22 * 256);

            int r = 0;
            r = filaHeader(wb, hResumen, r, "REPORTE DE VENTAS", 3);
            r = filaEmpresa(wb, hResumen, r, 3);
            r = filaFecha(wb, hResumen, r, 3);
            r++;

            r = filaSectionLabel(wb, hResumen, r, "RESUMEN EJECUTIVO", 3);
            r++;

            // Cards: 2 filas de 3 (igual que el PDF)
            r = filaCardTriple(wb, hResumen, r,
                    "TOTAL VENTAS",     String.format("$%,.2f", total),    false,
                    "TICKETS EMITIDOS", String.valueOf(cantidad),           false,
                    "TICKET PROMEDIO",  String.format("$%,.2f", promedio),  false);
            r = filaCardTriple(wb, hResumen, r,
                    "COSTO TOTAL",        String.format("$%,.2f", costoTotal),    false,
                    "GANANCIA NETA",      String.format("$%,.2f", gananciaTotal), true,
                    "MARGEN DE GANANCIA", String.format("%.1f%%",  margen),        true);
            r++;

            // Gráfica de barras horizontal (ASCII — misma idea que el PDF)
            r = filaSectionLabel(wb, hResumen, r, "GRÁFICA — PRODUCTOS MÁS VENDIDOS (unidades)", 3);
            r++;
            r = filaBarChart(wb, hResumen, r, topProductos);
            r++;

            // ── HOJA 2: Desglose por producto ───────────────────────────────
            // Espejo de la tabla "Desglose por producto" del PDF
            XSSFSheet hDesglose = wb.createSheet("Desglose por Producto");
            hDesglose.setColumnWidth(0, 36 * 256);
            hDesglose.setColumnWidth(1, 12 * 256);
            hDesglose.setColumnWidth(2, 16 * 256);
            hDesglose.setColumnWidth(3, 16 * 256);
            hDesglose.setColumnWidth(4, 16 * 256);
            hDesglose.setColumnWidth(5, 14 * 256);

            int d = 0;
            d = filaHeader(wb, hDesglose, d, "DESGLOSE POR PRODUCTO", 6);
            d = filaEmpresa(wb, hDesglose, d, 6);
            d = filaFecha(wb, hDesglose, d, 6);
            d++;
            d = filaSectionLabel(wb, hDesglose, d, "DESGLOSE POR PRODUCTO", 6);
            d++;
            d = filaHeaderTabla(wb, hDesglose, d,
                    "Producto", "Unidades", "Ingresos", "Costo total", "Ganancia", "% Total");

            List<Map.Entry<String, double[]>> listaDesglose =
                    new ArrayList<>(detallePorProducto.entrySet());
            listaDesglose.sort((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1]));

            boolean par = false;
            for (Map.Entry<String, double[]> e : listaDesglose) {
                double[] v    = e.getValue();
                int    unids  = (int) v[0];
                double ing    = v[1];
                double costoP = v[2] * unids;
                double ganP   = ing - costoP;
                double pct    = total > 0 ? (ing / total * 100) : 0;
                byte[] bg     = par ? RGB_FILA_PAR : RGB_FILA_IMPAR;
                par = !par;

                XSSFRow row = hDesglose.createRow(d++);
                celdaTexto(wb, row, 0, e.getKey(),                       bg, false, HorizontalAlignment.LEFT);
                celdaTexto(wb, row, 1, String.valueOf(unids),             bg, false, HorizontalAlignment.CENTER);
                celdaMoneda(wb, row, 2, ing,                              bg, false);
                celdaMoneda(wb, row, 3, costoP,                           bg, false);
                celdaMonedaColor(wb, row, 4, ganP,                        bg, ganP >= 0 ? RGB_VERDE : RGB_ROJO);
                celdaTexto(wb, row, 5, String.format("%.1f%%", pct),      bg, false, HorizontalAlignment.CENTER);
            }

            // Fila de totales al pie (igual que el PDF)
            d++;
            XSSFRow rowTot = hDesglose.createRow(d++);
            celdaTexto(wb, rowTot, 0, "TOTALES",                          RGB_PRIMARIO, true,  HorizontalAlignment.LEFT);
            celdaTexto(wb, rowTot, 1, "",                                  RGB_PRIMARIO, false, HorizontalAlignment.CENTER);
            celdaMonedaColor(wb, rowTot, 2, total,                         RGB_PRIMARIO, RGB_BLANCO);
            celdaMonedaColor(wb, rowTot, 3, costoTotal,                    RGB_PRIMARIO, RGB_BLANCO);
            celdaMonedaColor(wb, rowTot, 4, gananciaTotal,                 RGB_PRIMARIO, RGB_BLANCO);
            celdaTexto(wb, rowTot, 5, "100.0%",                            RGB_PRIMARIO, true,  HorizontalAlignment.CENTER);

            // ── HOJA 3: Detalle de tickets ───────────────────────────────────
            // Espejo de la tabla "Detalle de tickets" del PDF
            XSSFSheet hTickets = wb.createSheet("Detalle de Tickets");
            hTickets.setColumnWidth(0, 12 * 256);
            hTickets.setColumnWidth(1, 24 * 256);
            hTickets.setColumnWidth(2, 14 * 256);
            hTickets.setColumnWidth(3, 16 * 256);

            int t2 = 0;
            t2 = filaHeader(wb, hTickets, t2, "DETALLE DE TICKETS", 4);
            t2 = filaEmpresa(wb, hTickets, t2, 4);
            t2 = filaFecha(wb, hTickets, t2, 4);
            t2++;
            t2 = filaSectionLabel(wb, hTickets, t2, "DETALLE DE TICKETS", 4);
            t2++;
            t2 = filaHeaderTabla(wb, hTickets, t2, "Folio", "Fecha", "Artículos", "Total");

            boolean parT = false;
            double sumTotal = 0;
            for (Ticket t : tickets) {
                byte[] bg     = parT ? RGB_FILA_PAR : RGB_FILA_IMPAR;
                parT = !parT;
                int arts = t.getLineas() != null ? t.getLineas().size() : 0;
                sumTotal += t.getTotal();

                XSSFRow row = hTickets.createRow(t2++);
                celdaTexto(wb, row, 0, String.valueOf(t.getIdVenta()),    bg, false, HorizontalAlignment.CENTER);
                celdaTexto(wb, row, 1, t.getFechaHora().format(FMT),     bg, false, HorizontalAlignment.LEFT);
                celdaTexto(wb, row, 2, arts + " artículo(s)",             bg, false, HorizontalAlignment.CENTER);
                celdaMoneda(wb, row, 3, t.getTotal(),                     bg, false);
            }

            // Fila totales al pie (igual que el PDF)
            t2++;
            XSSFRow rowTotT = hTickets.createRow(t2++);
            celdaTexto(wb, rowTotT, 0, "TOTALES",                         RGB_PRIMARIO, true,  HorizontalAlignment.LEFT);
            celdaTexto(wb, rowTotT, 1, cantidad + " tickets",             RGB_PRIMARIO, true,  HorizontalAlignment.CENTER);
            celdaTexto(wb, rowTotT, 2, "",                                RGB_PRIMARIO, false, HorizontalAlignment.CENTER);
            celdaMonedaColor(wb, rowTotT, 3, sumTotal,                    RGB_PRIMARIO, RGB_BLANCO);

            // ── HOJA 4: Top productos ────────────────────────────────────────
            // Espejo de generarTopProductos() del PDF
            XSSFSheet hTop = wb.createSheet("Top Productos");
            hTop.setColumnWidth(0,  9 * 256);
            hTop.setColumnWidth(1, 42 * 256);
            hTop.setColumnWidth(2, 14 * 256);

            int p = 0;
            p = filaHeader(wb, hTop, p, "TOP PRODUCTOS MÁS VENDIDOS", 3);
            p = filaEmpresa(wb, hTop, p, 3);
            p = filaFecha(wb, hTop, p, 3);
            p++;
            p = filaSectionLabel(wb, hTop, p, "GRÁFICA DE BARRAS", 3);
            p++;
            p = filaBarChart(wb, hTop, p, topProductos);
            p++;
            p = filaSectionLabel(wb, hTop, p, "RANKING POR CANTIDAD VENDIDA", 3);
            p++;
            p = filaHeaderTabla(wb, hTop, p, "#", "Producto", "Cantidad");

            int pos = 1; boolean parP = false;
            for (Map.Entry<String, Integer> e : topProductos.entrySet()) {
                byte[] bg = parP ? RGB_FILA_PAR : RGB_FILA_IMPAR;
                parP = !parP;
                XSSFRow row = hTop.createRow(p++);
                celdaTexto(wb, row, 0, String.valueOf(pos++),             bg, false, HorizontalAlignment.CENTER);
                celdaTexto(wb, row, 1, e.getKey(),                        bg, false, HorizontalAlignment.LEFT);
                celdaTexto(wb, row, 2, String.valueOf(e.getValue()),       bg, true,  HorizontalAlignment.CENTER);
            }

            grabar(wb, ruta);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  2. TOP PRODUCTOS (reporte independiente)
    //     Espejo de: ReportePDFService.generarTopProductos()
    // ════════════════════════════════════════════════════════════════════════
    public void generarTopProductos(Map<String, Integer> top, String ruta) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet hoja = wb.createSheet("Top Productos");
            hoja.setColumnWidth(0,  9 * 256);
            hoja.setColumnWidth(1, 42 * 256);
            hoja.setColumnWidth(2, 14 * 256);

            int r = 0;
            r = filaHeader(wb, hoja, r, "TOP PRODUCTOS", 3);
            r = filaEmpresa(wb, hoja, r, 3);
            r = filaFecha(wb, hoja, r, 3);
            r++;
            r = filaSectionLabel(wb, hoja, r, "GRÁFICA DE BARRAS", 3);
            r++;
            r = filaBarChart(wb, hoja, r, top);
            r++;
            r = filaSectionLabel(wb, hoja, r, "RANKING POR CANTIDAD VENDIDA", 3);
            r++;
            r = filaHeaderTabla(wb, hoja, r, "#", "Producto", "Cantidad vendida");

            int pos = 1; boolean par = false;
            for (Map.Entry<String, Integer> e : top.entrySet()) {
                byte[] bg = par ? RGB_FILA_PAR : RGB_FILA_IMPAR;
                par = !par;
                XSSFRow row = hoja.createRow(r++);
                celdaTexto(wb, row, 0, String.valueOf(pos++),   bg, false, HorizontalAlignment.CENTER);
                celdaTexto(wb, row, 1, e.getKey(),              bg, false, HorizontalAlignment.LEFT);
                celdaTexto(wb, row, 2, String.valueOf(e.getValue()), bg, true, HorizontalAlignment.CENTER);
            }

            grabar(wb, ruta);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  3. BAJO STOCK
    //     Espejo de: ReportePDFService.generarBajoStock()
    //     Colores: rojo crítico (≤3), normal para el resto
    // ════════════════════════════════════════════════════════════════════════
    public void generarBajoStock(List<Producto> productos, String ruta) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet hoja = wb.createSheet("Bajo Stock");
            hoja.setColumnWidth(0, 46 * 256);
            hoja.setColumnWidth(1, 16 * 256);

            int r = 0;
            r = filaHeader(wb, hoja, r, "ALERTA: BAJO STOCK", 2);
            r = filaEmpresa(wb, hoja, r, 2);
            r = filaFecha(wb, hoja, r, 2);
            r++;
            r = filaSectionLabel(wb, hoja, r,
                    "PRODUCTOS QUE REQUIEREN REABASTECIMIENTO", 2);
            r++;
            r = filaHeaderTabla(wb, hoja, r, "Producto", "Stock actual");

            boolean par = false;
            for (Producto p : productos) {
                byte[] bg        = par ? RGB_FILA_PAR : RGB_FILA_IMPAR;
                par = !par;
                byte[] colorStock = p.getStock() <= 3 ? RGB_ROJO : RGB_TEXTO;

                XSSFRow row = hoja.createRow(r++);
                celdaTexto(wb, row, 0, p.getNombre(),            bg, false, HorizontalAlignment.LEFT);
                celdaTextoColor(wb, row, 1, String.valueOf(p.getStock()), bg, colorStock, true);
            }

            grabar(wb, ruta);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  4. CORTE DE CAJA
    //     Espejo de: ReportePDFService.generarCorteCaja()
    //     Secciones: Info cajero · Resumen financiero · Cards · Diferencia · Observaciones
    // ════════════════════════════════════════════════════════════════════════
    public void generarCorteCaja(
            String cajero, String numeroCaja,
            String horaApertura, String fechaCierre,
            double fondoInicial, double totalVentas,
            double totalEntradas, double totalSalidas,
            double dineroEsperado, double dineroContado,
            double diferencia, int numTickets,
            String observaciones, String ruta) throws Exception {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet hoja = wb.createSheet("Corte de Caja");
            hoja.setColumnWidth(0, 32 * 256);
            hoja.setColumnWidth(1, 26 * 256);
            hoja.setColumnWidth(2, 26 * 256);

            int r = 0;
            r = filaHeader(wb, hoja, r, "CORTE DE CAJA", 3);
            r = filaEmpresa(wb, hoja, r, 3);
            r = filaFecha(wb, hoja, r, 3);
            r++;

            // Info cajero — 2×2 (igual que las cards del PDF)
            r = filaCardDoble(wb, hoja, r, "CAJERO",        cajero,       "CAJA",         numeroCaja);
            r = filaCardDoble(wb, hoja, r, "HORA APERTURA", horaApertura, "HORA CIERRE",  fechaCierre);
            r++;

            // Resumen financiero
            r = filaSectionLabel(wb, hoja, r, "RESUMEN FINANCIERO", 3);
            r++;
            r = filaHeaderTabla(wb, hoja, r, "Concepto", "Monto", "");

            Object[][] conceptos = {
                    { "Fondo inicial",         fondoInicial  },
                    { "Total ventas efectivo", totalVentas   },
                    { "Entradas a caja",       totalEntradas },
                    { "Salidas de caja",       totalSalidas  },
            };
            boolean par = false;
            for (Object[] c : conceptos) {
                byte[] bg = par ? RGB_FILA_PAR : RGB_FILA_IMPAR;
                par = !par;
                XSSFRow row = hoja.createRow(r++);
                celdaTexto(wb, row, 0, (String) c[0],                    bg, false, HorizontalAlignment.LEFT);
                celdaMoneda(wb, row, 1, (double) c[1],                   bg, false);
                celdaTexto(wb, row, 2, "",                                bg, false, HorizontalAlignment.LEFT);
            }
            r++;

            // Cards: NO tickets · dinero esperado · dinero contado
            r = filaCardTriple(wb, hoja, r,
                    "NO. TICKETS",     String.valueOf(numTickets),
                    false,
                    "DINERO ESPERADO", String.format("$%,.2f", dineroEsperado),
                    false,
                    "DINERO CONTADO",  String.format("$%,.2f", dineroContado),
                    false);
            r++;

            // Resultado del corte — diferencia con color
            r = filaSectionLabel(wb, hoja, r, "RESULTADO DEL CORTE", 3);
            r++;
            byte[] colorDif = diferencia == 0 ? RGB_VERDE
                    : diferencia < 0           ? RGB_ROJO
                    :                            RGB_AZUL;
            String estadoTexto = diferencia == 0 ? "✔ Todo correcto"
                    : diferencia < 0             ? "⚠ Falta dinero"
                    :                              "⚠ Sobra dinero";

            XSSFRow rowDif = hoja.createRow(r++);
            celdaTexto(wb, rowDif, 0, "Diferencia",             RGB_FILA_PAR, false, HorizontalAlignment.LEFT);
            celdaMonedaColor(wb, rowDif, 1, diferencia,         RGB_FILA_PAR, colorDif);
            celdaTextoColor(wb, rowDif, 2, estadoTexto,         RGB_FILA_PAR, colorDif, true);
            r++;

            // Observaciones
            if (observaciones != null && !observaciones.trim().isEmpty()) {
                r = filaSectionLabel(wb, hoja, r, "OBSERVACIONES", 3);
                r++;
                hoja.addMergedRegion(new CellRangeAddress(r, r, 0, 2));
                XSSFRow rowObs = hoja.createRow(r++);
                celdaTexto(wb, rowObs, 0, observaciones, RGB_FILA_PAR, false, HorizontalAlignment.LEFT);
            }

            grabar(wb, ruta);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  5. VENTAS DETALLADAS (Tab 2 del ReporteController)
    //     Datos: folio, fecha, hora, cliente, cajero, método pago,
    //            artículos, total, estado
    // ════════════════════════════════════════════════════════════════════════
    public void generarVentasDetalladas(List<?> filas, String ruta) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet hoja = wb.createSheet("Ventas Detalladas");
            hoja.setColumnWidth(0,  9 * 256);
            hoja.setColumnWidth(1, 13 * 256);
            hoja.setColumnWidth(2, 10 * 256);
            hoja.setColumnWidth(3, 22 * 256);
            hoja.setColumnWidth(4, 18 * 256);
            hoja.setColumnWidth(5, 16 * 256);
            hoja.setColumnWidth(6,  9 * 256);
            hoja.setColumnWidth(7, 14 * 256);
            hoja.setColumnWidth(8, 14 * 256);

            int r = 0;
            r = filaHeader(wb, hoja, r, "VENTAS DETALLADAS", 9);
            r = filaEmpresa(wb, hoja, r, 9);
            r = filaFecha(wb, hoja, r, 9);
            r++;
            r = filaSectionLabel(wb, hoja, r, "VENTAS DEL PERIODO SELECCIONADO", 9);
            r++;
            r = filaHeaderTabla(wb, hoja, r,
                    "Folio", "Fecha", "Hora", "Cliente", "Cajero",
                    "Método Pago", "Arts.", "Total", "Estado");

            boolean par = false;
            for (Object o : filas) {
                try {
                    String folio = (String) o.getClass().getMethod("getFolio").invoke(o);
                    String fecha = (String) o.getClass().getMethod("getFecha").invoke(o);
                    String hora  = (String) o.getClass().getMethod("getHora").invoke(o);
                    String cli   = (String) o.getClass().getMethod("getCliente").invoke(o);
                    String caj   = (String) o.getClass().getMethod("getCajero").invoke(o);
                    String met   = (String) o.getClass().getMethod("getMetodoPago").invoke(o);
                    int    arts  = (int)    o.getClass().getMethod("getTotalArticulos").invoke(o);
                    double tot   = (double) o.getClass().getMethod("getTotal").invoke(o);
                    String est   = (String) o.getClass().getMethod("getEstado").invoke(o);

                    byte[] bg = par ? RGB_FILA_PAR : RGB_FILA_IMPAR;
                    par = !par;
                    byte[] colorEst = est.equalsIgnoreCase("cancelada") ? RGB_ROJO
                            : est.equalsIgnoreCase("completada")         ? RGB_VERDE
                            :                                              RGB_AZUL;

                    XSSFRow row = hoja.createRow(r++);
                    celdaTexto(wb, row, 0, folio,             bg, false, HorizontalAlignment.CENTER);
                    celdaTexto(wb, row, 1, fecha,             bg, false, HorizontalAlignment.CENTER);
                    celdaTexto(wb, row, 2, hora,              bg, false, HorizontalAlignment.CENTER);
                    celdaTexto(wb, row, 3, cli,               bg, false, HorizontalAlignment.LEFT);
                    celdaTexto(wb, row, 4, caj,               bg, false, HorizontalAlignment.LEFT);
                    celdaTexto(wb, row, 5, met,               bg, false, HorizontalAlignment.CENTER);
                    celdaTexto(wb, row, 6, String.valueOf(arts), bg, false, HorizontalAlignment.CENTER);
                    celdaMoneda(wb, row, 7, tot,              bg, false);
                    celdaTextoColor(wb, row, 8, est,          bg, colorEst, true);
                } catch (Exception ex) { ex.printStackTrace(); }
            }

            grabar(wb, ruta);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS — estructura de filas
    // ════════════════════════════════════════════════════════════════════════

    /** Banda de título principal (fondo primario, texto blanco grande) */
    private int filaHeader(XSSFWorkbook wb, XSSFSheet hoja,
                           int fila, String texto, int cols) {
        hoja.addMergedRegion(new CellRangeAddress(fila, fila, 0, cols - 1));
        XSSFRow row = hoja.createRow(fila);
        row.setHeightInPoints(32);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue(texto);
        XSSFCellStyle st = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 16);
        setFontColor(f, RGB_BLANCO);
        st.setFont(f);
        setFill(st, RGB_PRIMARIO);
        st.setAlignment(HorizontalAlignment.LEFT);
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setCellStyle(st);
        return fila + 1;
    }

    /** Línea "Volovan Volo · Sistema de Punto de Venta" */
    private int filaEmpresa(XSSFWorkbook wb, XSSFSheet hoja, int fila, int cols) {
        hoja.addMergedRegion(new CellRangeAddress(fila, fila, 0, cols - 1));
        XSSFRow row = hoja.createRow(fila);
        row.setHeightInPoints(14);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue("Volovan Volo  ·  Sistema de Punto de Venta");
        XSSFCellStyle st = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setFontHeightInPoints((short) 8);
        setFontColor(f, RGB_GRIS);
        st.setFont(f);
        setFill(st, RGB_DORADO);
        st.setAlignment(HorizontalAlignment.LEFT);
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setCellStyle(st);
        return fila + 1;
    }

    /** "Generado el dd/MM/yyyy HH:mm" */
    private int filaFecha(XSSFWorkbook wb, XSSFSheet hoja, int fila, int cols) {
        hoja.addMergedRegion(new CellRangeAddress(fila, fila, 0, cols - 1));
        XSSFRow row = hoja.createRow(fila);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue("Generado el " + LocalDateTime.now().format(FMT));
        XSSFCellStyle st = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setFontHeightInPoints((short) 9);
        setFontColor(f, RGB_GRIS);
        st.setFont(f);
        st.setAlignment(HorizontalAlignment.LEFT);
        cell.setCellStyle(st);
        return fila + 1;
    }

    /** Etiqueta de sección (barra dorada + texto bold — igual que etiquetaSeccion del PDF) */
    private int filaSectionLabel(XSSFWorkbook wb, XSSFSheet hoja,
                                 int fila, String texto, int cols) {
        // Celda decorativa dorada (2% del ancho)
        XSSFRow row = hoja.createRow(fila);
        row.setHeightInPoints(20);

        XSSFCell barra = row.createCell(0);
        barra.setCellValue("");
        XSSFCellStyle stBarra = wb.createCellStyle();
        setFill(stBarra, RGB_DORADO);
        barra.setCellStyle(stBarra);

        // Texto de sección en columna 1 (span del resto)
        if (cols > 1) {
            hoja.addMergedRegion(new CellRangeAddress(fila, fila, 1, cols - 1));
        }
        XSSFCell lblCell = row.createCell(1);
        lblCell.setCellValue(" " + texto);
        XSSFCellStyle stLbl = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 10);
        setFontColor(f, RGB_PRIMARIO);
        stLbl.setFont(f);
        stLbl.setAlignment(HorizontalAlignment.LEFT);
        stLbl.setVerticalAlignment(VerticalAlignment.CENTER);
        lblCell.setCellStyle(stLbl);
        return fila + 1;
    }

    /** Card triple (3 columnas) — espejo de PdfPTable cards del PDF */
    private int filaCardTriple(XSSFWorkbook wb, XSSFSheet hoja, int fila,
                               String lbl1, String val1, boolean ganancia1,
                               String lbl2, String val2, boolean ganancia2,
                               String lbl3, String val3, boolean ganancia3) {
        // Fila etiquetas
        XSSFRow rowLbl = hoja.createRow(fila);
        rowLbl.setHeightInPoints(14);
        celdaCardLbl(wb, rowLbl, 0, lbl1);
        celdaCardLbl(wb, rowLbl, 1, lbl2);
        celdaCardLbl(wb, rowLbl, 2, lbl3);

        // Fila valores
        XSSFRow rowVal = hoja.createRow(fila + 1);
        rowVal.setHeightInPoints(24);
        celdaCardVal(wb, rowVal, 0, val1, ganancia1);
        celdaCardVal(wb, rowVal, 1, val2, ganancia2);
        celdaCardVal(wb, rowVal, 2, val3, ganancia3);
        return fila + 2;
    }

    /** Card doble (2 columnas) */
    private int filaCardDoble(XSSFWorkbook wb, XSSFSheet hoja, int fila,
                              String lbl1, String val1,
                              String lbl2, String val2) {
        XSSFRow rowLbl = hoja.createRow(fila);
        rowLbl.setHeightInPoints(14);
        celdaCardLbl(wb, rowLbl, 0, lbl1);
        celdaCardLbl(wb, rowLbl, 1, lbl2);

        XSSFRow rowVal = hoja.createRow(fila + 1);
        rowVal.setHeightInPoints(22);
        celdaCardVal(wb, rowVal, 0, val1, false);
        celdaCardVal(wb, rowVal, 1, val2, false);
        return fila + 2;
    }

    /** Header de tabla (fondo oscuro, texto blanco bold — igual que agregarHeaderTabla del PDF) */
    private int filaHeaderTabla(XSSFWorkbook wb, XSSFSheet hoja, int fila, String... cols) {
        XSSFRow row = hoja.createRow(fila);
        row.setHeightInPoints(18);
        for (int i = 0; i < cols.length; i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellValue(cols[i]);
            XSSFCellStyle st = wb.createCellStyle();
            XSSFFont f = wb.createFont();
            f.setBold(true);
            f.setFontHeightInPoints((short) 10);
            setFontColor(f, RGB_BLANCO);
            st.setFont(f);
            setFill(st, RGB_HEADER_TBL);
            st.setAlignment(HorizontalAlignment.CENTER);
            st.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorde(st);
            cell.setCellStyle(st);
        }
        return fila + 1;
    }

    /**
     * Gráfica de barras horizontal en celdas de Excel.
     * Espejo visual de construirGraficaBarras() del PDF.
     * Columnas: [nombre] [barra ████░░░] [valor]
     */
    private int filaBarChart(XSSFWorkbook wb, XSSFSheet hoja,
                             int fila, Map<String, Integer> datos) {
        if (datos == null || datos.isEmpty()) return fila;

        List<Map.Entry<String, Integer>> lista = new ArrayList<>(datos.entrySet());
        if (lista.size() > 8) lista = lista.subList(0, 8);
        int maxVal = lista.stream().mapToInt(Map.Entry::getValue).max().orElse(1);

        // Encabezado de la gráfica
        XSSFRow rowHead = hoja.createRow(fila++);
        celdaTexto(wb, rowHead, 0, "Producto",  RGB_CARD_BG, true, HorizontalAlignment.LEFT);
        celdaTexto(wb, rowHead, 1, "Barras",    RGB_CARD_BG, true, HorizontalAlignment.CENTER);
        celdaTexto(wb, rowHead, 2, "Cantidad",  RGB_CARD_BG, true, HorizontalAlignment.CENTER);

        final int BAR_WIDTH = 30; // caracteres totales de la barra
        for (Map.Entry<String, Integer> e : lista) {
            String nombre  = e.getKey();
            int    valor   = e.getValue();
            int    llenos  = (int) Math.round((double) valor / maxVal * BAR_WIDTH);
            int    vacios  = BAR_WIDTH - llenos;

            String barra = "█".repeat(Math.max(llenos, 0))
                    + "░".repeat(Math.max(vacios, 0));

            String label = nombre.length() > 28 ? nombre.substring(0, 26) + "…" : nombre;

            XSSFRow row = hoja.createRow(fila++);
            row.setHeightInPoints(16);

            // Celda nombre
            celdaTexto(wb, row, 0, label,             RGB_FILA_PAR, false, HorizontalAlignment.LEFT);

            // Celda barra — color primario en el texto
            XSSFCell cBarra = row.createCell(1);
            cBarra.setCellValue(barra);
            XSSFCellStyle stBarra = wb.createCellStyle();
            XSSFFont fBarra = wb.createFont();
            fBarra.setFontHeightInPoints((short) 9);
            setFontColor(fBarra, RGB_PRIMARIO);
            stBarra.setFont(fBarra);
            setFill(stBarra, RGB_FILA_PAR);
            setBorde(stBarra);
            cBarra.setCellStyle(stBarra);

            // Celda valor
            celdaTexto(wb, row, 2, String.valueOf(valor), RGB_FILA_PAR, true, HorizontalAlignment.CENTER);
        }
        return fila;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS — celdas individuales
    // ════════════════════════════════════════════════════════════════════════

    private void celdaTexto(XSSFWorkbook wb, XSSFRow row, int col,
                            String valor, byte[] bg,
                            boolean bold, HorizontalAlignment align) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(valor);
        XSSFCellStyle st = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) 9);
        setFontColor(f, RGB_TEXTO);
        st.setFont(f);
        setFill(st, bg);
        st.setAlignment(align);
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorde(st);
        cell.setCellStyle(st);
    }

    private void celdaTextoColor(XSSFWorkbook wb, XSSFRow row, int col,
                                 String valor, byte[] bg,
                                 byte[] colorFuente, boolean bold) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(valor);
        XSSFCellStyle st = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) 9);
        setFontColor(f, colorFuente);
        st.setFont(f);
        setFill(st, bg);
        st.setAlignment(HorizontalAlignment.CENTER);
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorde(st);
        cell.setCellStyle(st);
    }

    private void celdaMoneda(XSSFWorkbook wb, XSSFRow row, int col,
                             double valor, byte[] bg, boolean bold) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(valor);
        XSSFCellStyle st = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) 9);
        setFontColor(f, RGB_TEXTO);
        st.setFont(f);
        setFill(st, bg);
        st.setAlignment(HorizontalAlignment.RIGHT);
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorde(st);
        st.setDataFormat(wb.createDataFormat().getFormat("$#,##0.00"));
        cell.setCellStyle(st);
    }

    /** Moneda con color de fuente personalizado (ganancia/pérdida o totales blancos) */
    private void celdaMonedaColor(XSSFWorkbook wb, XSSFRow row, int col,
                                  double valor, byte[] bg, byte[] colorFuente) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(valor);
        XSSFCellStyle st = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 9);
        setFontColor(f, colorFuente);
        st.setFont(f);
        setFill(st, bg);
        st.setAlignment(HorizontalAlignment.RIGHT);
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorde(st);
        st.setDataFormat(wb.createDataFormat().getFormat("$#,##0.00"));
        cell.setCellStyle(st);
    }

    /** Etiqueta de card (texto pequeño gris) */
    private void celdaCardLbl(XSSFWorkbook wb, XSSFRow row, int col, String texto) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(texto);
        XSSFCellStyle st = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setFontHeightInPoints((short) 8);
        setFontColor(f, RGB_GRIS);
        st.setFont(f);
        setFill(st, RGB_CARD_BG);
        st.setAlignment(HorizontalAlignment.LEFT);
        st.setVerticalAlignment(VerticalAlignment.BOTTOM);
        setBorde(st);
        cell.setCellStyle(st);
    }

    /** Valor de card (texto grande primario o verde si es ganancia) */
    private void celdaCardVal(XSSFWorkbook wb, XSSFRow row, int col,
                              String valor, boolean isGanancia) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(valor);
        XSSFCellStyle st = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        setFontColor(f, isGanancia ? RGB_VERDE : RGB_PRIMARIO);
        st.setFont(f);
        setFill(st, RGB_CARD_BG);
        st.setAlignment(HorizontalAlignment.LEFT);
        st.setVerticalAlignment(VerticalAlignment.TOP);
        setBorde(st);
        cell.setCellStyle(st);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILIDADES DE COLOR Y BORDE
    // ════════════════════════════════════════════════════════════════════════

    private void setFontColor(XSSFFont font, byte[] rgb) {
        font.setColor(new XSSFColor(
                new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF),
                new DefaultIndexedColorMap()));
    }

    private void setFill(XSSFCellStyle st, byte[] rgb) {
        st.setFillForegroundColor(new XSSFColor(
                new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF),
                new DefaultIndexedColorMap()));
        st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private void setBorde(XSSFCellStyle st) {
        st.setBorderBottom(BorderStyle.THIN);
        st.setBorderTop(BorderStyle.THIN);
        st.setBorderLeft(BorderStyle.THIN);
        st.setBorderRight(BorderStyle.THIN);
        XSSFColor gris = new XSSFColor(new java.awt.Color(220, 210, 195),
                new DefaultIndexedColorMap());
        st.setBottomBorderColor(gris);
        st.setTopBorderColor(gris);
        st.setLeftBorderColor(gris);
        st.setRightBorderColor(gris);
    }

    private void grabar(XSSFWorkbook wb, String ruta) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(ruta)) {
            wb.write(fos);
        }
    }
}