package org.example.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import org.example.dao.ConexionDB;
import org.example.dao.ReporteAvanzadoDAO;
import org.example.modelo.SesionUsuario;
import org.example.modelo.Ticket;
import org.example.servicio.MarcaService;
import org.example.servicio.ReporteService;
import org.example.servicio.ReportePDFService;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.chart.XYChart;
import java.awt.Desktop;
import javafx.stage.FileChooser;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReporteController {

    // ── Filtros ──
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> cbTipoReporte;

    // ── Cards métricas ──
    @FXML private Label lblTotal;
    @FXML private Label lblTickets;
    @FXML private Label lblPromedio;
    @FXML private Label lblFecha;
    @FXML private Label lblHora;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;
    @FXML private Label lblProductoTop;
    @FXML private Label lblProductoTopCantidad;

    // ── Botones filtros rápidos ──
    @FXML private Button btnCantidad;
    @FXML private Button btnIngresos;
    @FXML private Button btnHoy;
    @FXML private Button btnAyer;
    @FXML private Button btnSemana;
    @FXML private Button btnMes;
    @FXML private Button btnAnio;
    @FXML private Button btnTabla;

    // ── Tab 1: Gráficas/Productos ──
    @FXML private TableView<FilaReporte>            tablaReporte;
    @FXML private TableColumn<FilaReporte, String>  colTablaProducto;
    @FXML private TableColumn<FilaReporte, Integer> colTablaCantidad;
    @FXML private TableColumn<FilaReporte, Double>  colTablaIngresos;
    @FXML private ComboBox<String>  cbMesSel;
    @FXML private ComboBox<Integer> cbAnioSel;
    @FXML private BarChart<String, Number> chartVentas;
    // graficaProductos eliminado — no existe en el FXML actual

    // ── Tab 2: Ventas detalladas (dentro de Tab → inyección lazy) ──
    @FXML private TableView<FilaVenta>           tablaVentas;
    @FXML private TableColumn<FilaVenta, String> colVentaFolio;
    @FXML private TableColumn<FilaVenta, String> colVentaFecha;
    @FXML private TableColumn<FilaVenta, String> colVentaHora;
    @FXML private TableColumn<FilaVenta, String> colVentaCliente;
    @FXML private TableColumn<FilaVenta, String> colVentaCajero;
    @FXML private TableColumn<FilaVenta, String> colVentaMetodo;
    @FXML private TableColumn<FilaVenta, String> colVentaArticulos;
    @FXML private TableColumn<FilaVenta, String> colVentaTotal;
    @FXML private TableColumn<FilaVenta, String> colVentaEstado;
    @FXML private TableColumn<FilaVenta, String> colVentaAccion;
    @FXML private Label lblResumenVentas;

    // ── Estilos filtros rápidos — paleta azul ──
    private static final String ESTILO_BTN_ACTIVO =
            "-fx-background-color: #1a6fa8; -fx-text-fill: white; " +
                    "-fx-border-color: #1a6fa8; -fx-border-width: 1; " +
                    "-fx-border-radius: 20; -fx-background-radius: 20; " +
                    "-fx-padding: 5 18; -fx-cursor: hand; " +
                    "-fx-font-size: 11px; -fx-font-weight: bold;";

    private static final String ESTILO_BTN_INACTIVO =
            "-fx-background-color: #f0f7ff; -fx-text-fill: #1a6fa8; " +
                    "-fx-border-color: #1a6fa8; -fx-border-width: 1; " +
                    "-fx-border-radius: 20; -fx-background-radius: 20; " +
                    "-fx-padding: 5 18; -fx-cursor: hand; " +
                    "-fx-font-size: 11px; -fx-font-weight: bold;";

    // ── Estado interno ──
    private String ultimaRutaPDF = null;
    private Map<String, Integer> ultimoTop = null;
    private List<Ticket> ultimosTickets = null;
    private List<FilaVenta> ultimasFilasVenta = new ArrayList<>();

    private final ReporteService    service = new ReporteService();
    private final ReportePDFService pdf     = new ReportePDFService();
    private final DecimalFormat     df      = new DecimalFormat("#,##0.00");

    // ── Reportes avanzados ────────────────────────────────────────────────
    private final ReporteAvanzadoDAO daoAvanzado = new ReporteAvanzadoDAO();

    // Tab Cajeros
    @FXML private TableView<Map<String, Object>>           tablaCajeros;
    @FXML private TableColumn<Map<String, Object>, String> colCajNombre;
    @FXML private TableColumn<Map<String, Object>, String> colCajTickets;
    @FXML private TableColumn<Map<String, Object>, String> colCajBruto;
    @FXML private TableColumn<Map<String, Object>, String> colCajNeto;
    @FXML private TableColumn<Map<String, Object>, String> colCajPromedio;
    @FXML private TableColumn<Map<String, Object>, String> colCajDev;

    // Tab Hora
    @FXML private TableView<Map<String, Object>>           tablaHoras;
    @FXML private TableColumn<Map<String, Object>, String> colHoraHora;
    @FXML private TableColumn<Map<String, Object>, String> colHoraTickets;
    @FXML private TableColumn<Map<String, Object>, String> colHoraTotal;

    // Tab Rentabilidad
    @FXML private TableView<Map<String, Object>>           tablaRentabilidad;
    @FXML private TableColumn<Map<String, Object>, String> colRentNombre;
    @FXML private TableColumn<Map<String, Object>, String> colRentUnidades;
    @FXML private TableColumn<Map<String, Object>, String> colRentIngresos;
    @FXML private TableColumn<Map<String, Object>, String> colRentCostos;
    @FXML private TableColumn<Map<String, Object>, String> colRentGanancia;
    @FXML private TableColumn<Map<String, Object>, String> colRentMargen;

    // Tab Clientes
    @FXML private TableView<Map<String, Object>>           tablaClientesCredito;
    @FXML private TableColumn<Map<String, Object>, String> colCliNombre;
    @FXML private TableColumn<Map<String, Object>, String> colCliSaldo;
    @FXML private TableColumn<Map<String, Object>, String> colCliDisponible;
    @FXML private TableColumn<Map<String, Object>, String> colCliLimite;
    @FXML private TableColumn<Map<String, Object>, String> colCliCompras;
    @FXML private TableColumn<Map<String, Object>, String> colCliUltima;

    // Cards resumen neto
    @FXML private Label lblVentasBrutas;
    @FXML private Label lblTotalDevoluciones;
    @FXML private Label lblVentasNetas;
    @FXML private TableView<Map<String, Object>> tablaFiscal;
    @FXML private TableColumn<Map<String, Object>, String> colFiscalConcepto;
    @FXML private TableColumn<Map<String, Object>, String> colFiscalMonto;
    // ─────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        cargarDatosUsuario();

        cbTipoReporte.getItems().addAll("Ventas", "Productos más vendidos", "Bajo stock");

        cbMesSel.getItems().addAll(
                "Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre");
        cbMesSel.setValue("Enero");

        int anioActual = LocalDate.now().getYear();
        for (int a = anioActual; a >= anioActual - 5; a--) cbAnioSel.getItems().add(a);
        cbAnioSel.setValue(anioActual);

        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm:ss");
        javafx.animation.Timeline reloj = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1),
                        e -> lblHora.setText(java.time.LocalDateTime.now().format(fmtHora))));
        reloj.setCycleCount(javafx.animation.Animation.INDEFINITE);
        reloj.play();

        configurarTablaVentas();
    }

    private void cargarDatosUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);
    }

    // ─────────────────────────────────────────────────────────────
    // CONFIGURAR TABLA DE VENTAS DETALLADAS
    // ─────────────────────────────────────────────────────────────
    private void configurarTablaVentas() {
        colVentaFolio    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        colVentaFecha    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha()));
        colVentaHora     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHora()));
        colVentaCliente  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente()));
        colVentaCajero   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCajero()));
        colVentaMetodo   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMetodoPago()));
        colVentaArticulos.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getTotalArticulos())));
        colVentaTotal    .setCellValueFactory(c -> new SimpleStringProperty("$" + df.format(c.getValue().getTotal())));
        colVentaEstado   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));

        // Columna Estado — color según valor
        colVentaEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = switch (item.toLowerCase()) {
                    case "completada" -> "#1e7d3e";
                    case "cancelada"  -> "#c0392b";
                    default           -> "#6a96b8";
                };
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 11px;");
            }
        });

        // Columna Detalle — botón "Ver"
        colVentaAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ver");
            {
                btn.setStyle("-fx-background-color: #1a6fa8; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-border-radius: 5; " +
                        "-fx-font-size: 10px; -fx-padding: 3 10; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    FilaVenta fila = getTableView().getItems().get(getIndex());
                    mostrarDetalleVenta(fila);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setText(null);
            }
        });

        // Doble clic también abre detalle
        tablaVentas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tablaVentas.getSelectionModel().getSelectedItem() != null) {
                mostrarDetalleVenta(tablaVentas.getSelectionModel().getSelectedItem());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // GENERAR REPORTE (mismo flujo, ahora también llena Tab 2)
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void generarReporte() {
        try {
            if (dateInicio.getValue() == null || dateFin.getValue() == null) { alerta("Selecciona fechas"); return; }
            if (cbTipoReporte.getValue() == null) { alerta("Selecciona tipo de reporte"); return; }

            LocalDateTime inicio = dateInicio.getValue().atStartOfDay();
            LocalDateTime fin    = dateFin.getValue().atTime(23, 59);

            List<Ticket> tickets = service.obtenerTickets(inicio, fin);
            double total    = service.calcularTotal(tickets);
            int    cantidad = service.contarTickets(tickets);
            double promedio = service.calcularPromedio(tickets);
            Map<String, Integer> top = service.topProductos(tickets);

            ultimoTop      = top;
            ultimosTickets = tickets;

            // Cards
            lblTotal.setText("$" + df.format(total));
            lblTickets.setText(String.valueOf(cantidad));
            lblPromedio.setText("$" + df.format(promedio));

            if (!top.isEmpty()) {
                Map.Entry<String, Integer> primero = top.entrySet().iterator().next();
                lblProductoTop.setText(primero.getKey());
                lblProductoTopCantidad.setText(primero.getValue() + " unidades vendidas");
            } else {
                lblProductoTop.setText("Sin datos");
                lblProductoTopCantidad.setText("No hay ventas en el periodo");
            }

            // Gráfica (Tab 1)
            chartVentas.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Productos");
            for (Map.Entry<String, Integer> e : top.entrySet())
                series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
            chartVentas.getData().add(series);

            // Ventas detalladas (Tab 2)
            cargarVentasDetalladas(inicio, fin);

            alerta("Vista previa generada ✔");
            cargarReportesAvanzados();

        } catch (Exception e) {
            e.printStackTrace();
            alerta("Error al generar vista previa");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CARGAR VENTAS DETALLADAS — SQL defensivo
    // ─────────────────────────────────────────────────────────────
    private void cargarVentasDetalladas(LocalDateTime inicio, LocalDateTime fin) {
        ultimasFilasVenta.clear();

        // Detectar qué columnas opcionales existen en la tabla ventas
        boolean tieneMetodoPago = columnaExiste("ventas", "metodo_pago");
        boolean tieneEstado     = columnaExiste("ventas", "estado");

        String colEstado = tieneEstado     ? "COALESCE(v.estado, 'completada')"    : "'completada'";

        String sql =
                "SELECT " +
                        "  v.id_venta, " +
                        "  DATE_FORMAT(v.fecha, '%d/%m/%Y') AS fecha, " +
                        "  DATE_FORMAT(v.fecha, '%H:%i')    AS hora, " +
                        "  COALESCE(c.nombre, 'Publico General') AS cliente, " +
                        "  u.nombre AS cajero, " +
                        "  COALESCE(p.tipo_pago, v.metodo_pago, 'Efectivo') AS metodo_pago, " +
                        "  (SELECT COALESCE(SUM(dv2.cantidad),0) " +
                        "     FROM detalle_venta dv2 WHERE dv2.id_venta = v.id_venta) AS total_articulos, " +
                        "  v.total, " +
                        "  COALESCE(v.estado, 'completada') AS estado " +
                        "FROM ventas v " +
                        "JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                        "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
                        "LEFT JOIN pagos p ON p.id_venta = v.id_venta " +
                        "WHERE v.fecha BETWEEN ? AND ? " +
                        "ORDER BY v.fecha DESC";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Usar Timestamp para máxima compatibilidad con drivers JDBC/MySQL
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(inicio));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(fin));

            ResultSet rs = ps.executeQuery();

            ObservableList<FilaVenta> filas = FXCollections.observableArrayList();
            while (rs.next()) {
                FilaVenta fv = new FilaVenta(
                        String.format("#%04d", rs.getInt("id_venta")),
                        rs.getString("fecha"),
                        rs.getString("hora"),
                        rs.getString("cliente"),
                        rs.getString("cajero"),
                        rs.getString("metodo_pago"),
                        rs.getInt("total_articulos"),
                        rs.getDouble("total"),
                        rs.getString("estado"),
                        rs.getInt("id_venta")
                );
                filas.add(fv);
                ultimasFilasVenta.add(fv);
            }

            tablaVentas.setItems(filas);

            if (lblResumenVentas != null)
                lblResumenVentas.setText(filas.size() + " venta(s) encontradas");

        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Error al cargar ventas detalladas:\n" + ex.getMessage());
        }
    }

    /** Verifica si una columna existe en una tabla sin lanzar excepción */
    private boolean columnaExiste(String tabla, String columna) {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tabla);
            ps.setString(2, columna);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MODAL DETALLE DE VENTA
    // ─────────────────────────────────────────────────────────────
    private void mostrarDetalleVenta(FilaVenta fila) {
        Stage stage = new Stage();
        stage.setTitle("Detalle — Venta " + fila.getFolio());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        // ── Encabezado ──
        VBox header = new VBox(4);
        header.setStyle("-fx-background-color: #1a6fa8; -fx-padding: 16 20;");
        Label lblTitulo = new Label("Detalle de Venta " + fila.getFolio());
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label lblSub = new Label(fila.getFecha() + "  ·  " + fila.getHora()
                + "  ·  " + fila.getCajero());
        lblSub.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
        header.getChildren().addAll(lblTitulo, lblSub);

        // ── Info general ──
        HBox infoRow = new HBox(16);
        infoRow.setStyle("-fx-padding: 12 20 8 20; -fx-background-color: #f0f7ff;");
        infoRow.getChildren().addAll(
                infoChip("Cliente",      fila.getCliente()),
                infoChip("Método pago",  fila.getMetodoPago()),
                infoChip("Artículos",    String.valueOf(fila.getTotalArticulos())),
                infoChip("Estado",       fila.getEstado()),
                infoChip("Total",        "$" + df.format(fila.getTotal()))
        );

        // ── Tabla de productos ──
        TableView<Map<String, Object>> tablaDetalle = new TableView<>();
        tablaDetalle.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0 16 16 16;");
        tablaDetalle.setPrefHeight(260);

        TableColumn<Map<String, Object>, String> cProd  = col("Producto",  220, o -> (String)  o.get("producto"));
        TableColumn<Map<String, Object>, String> cCant  = col("Cant.",      70, o -> String.valueOf((int) o.get("cantidad")));
        TableColumn<Map<String, Object>, String> cPrecio = col("Precio",   100, o -> "$" + df.format((double) o.get("precio")));
        TableColumn<Map<String, Object>, String> cSub   = col("Subtotal",  110, o -> "$" + df.format((double) o.get("subtotal")));
        tablaDetalle.getColumns().addAll(cProd, cCant, cPrecio, cSub);

        // Consulta detalle
        String sqlDet = """
            SELECT p.nombre AS producto, dv.cantidad,
                   dv.precio_unitario AS precio,
                   (dv.cantidad * dv.precio_unitario) AS subtotal
            FROM detalle_venta dv
            JOIN productos p ON dv.id_producto = p.id_producto
            WHERE dv.id_venta = ?
            """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlDet)) {
            ps.setInt(1, fila.getIdVenta());
            ResultSet rs = ps.executeQuery();
            ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();
            while (rs.next()) {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("producto", rs.getString("producto"));
                m.put("cantidad", rs.getInt("cantidad"));
                m.put("precio",   rs.getDouble("precio"));
                m.put("subtotal", rs.getDouble("subtotal"));
                rows.add(m);
            }
            tablaDetalle.setItems(rows);
        } catch (Exception ex) { ex.printStackTrace(); }

        // ── Footer totales ──
        VBox footer = new VBox(6);
        footer.setStyle("-fx-padding: 10 20 16 20; -fx-background-color: #f0f7ff;" +
                "-fx-border-color: #d0e4f4 transparent transparent transparent; -fx-border-width: 1;");

        double[] fiscal = obtenerTotalesFiscalesVenta(fila.getIdVenta(), fila.getTotal());

        footer.getChildren().addAll(
                filaTotal("Subtotal:",            "$" + df.format(fiscal[0]), false),
                filaTotal("IVA:",                 "$" + df.format(fiscal[1]), false),
                filaTotal("IEPS:",                "$" + df.format(fiscal[2]), false),
                filaTotal("Impuestos:",           "$" + df.format(fiscal[3]), false),
                filaTotal("TOTAL:",                "$" + df.format(fila.getTotal()), true)
        );

        VBox layout = new VBox(header, infoRow, tablaDetalle, footer);
        stage.setScene(new Scene(layout, 560, 480));
        stage.show();
    }

    // helpers para el modal de detalle
    private double[] obtenerTotalesFiscalesVenta(int idVenta, double totalFallback) {
        String sql = "SELECT subtotal, iva, ieps, impuestos FROM ventas WHERE id_venta = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            org.example.servicio.FiscalSchemaService.asegurarEstructura(con);
            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new double[]{
                        rs.getDouble("subtotal"),
                        rs.getDouble("iva"),
                        rs.getDouble("ieps"),
                        rs.getDouble("impuestos")
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{totalFallback, 0, 0, 0};
    }

    private VBox infoChip(String etiq, String valor) {
        VBox v = new VBox(2);
        v.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8 12;" +
                "-fx-border-color: #d0e4f4; -fx-border-width: 1; -fx-border-radius: 8;");
        Label le = new Label(etiq); le.setStyle("-fx-text-fill: #6a96b8; -fx-font-size: 9px; -fx-font-weight: bold;");
        Label lv = new Label(valor); lv.setStyle("-fx-text-fill: #0d3d5e; -fx-font-size: 12px; -fx-font-weight: bold;");
        v.getChildren().addAll(le, lv);
        return v;
    }

    private <T> TableColumn<T, String> col(String titulo, double ancho,
                                           java.util.function.Function<T, String> extractor) {
        TableColumn<T, String> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(cd -> new SimpleStringProperty(extractor.apply(cd.getValue())));
        return c;
    }

    private HBox filaTotal(String etiq, String valor, boolean resaltar) {
        HBox h = new HBox();
        Label le = new Label(etiq);
        le.setStyle("-fx-text-fill: #6a96b8; -fx-font-size: " + (resaltar ? "13" : "11") + "px;");
        Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        Label lv = new Label(valor);
        lv.setStyle("-fx-text-fill: #0d3d5e; -fx-font-weight: bold; -fx-font-size: " + (resaltar ? "16" : "12") + "px;");
        h.getChildren().addAll(le, sp, lv);
        return h;
    }

    // ─────────────────────────────────────────────────────────────
    // EXPORTAR CSV
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void exportarVentasCSV() {
        if (ultimasFilasVenta.isEmpty()) { alerta("Genera un reporte primero."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar ventas como CSV");
        String nombre = "Ventas_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";
        fc.setInitialFileName(nombre);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File destino = fc.showSaveDialog(tablaVentas.getScene().getWindow());
        if (destino == null) return;

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(destino), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.println("Folio,Fecha,Hora,Cliente,Cajero,Metodo Pago,Articulos,Total,Estado");
            for (FilaVenta f : ultimasFilasVenta) {
                pw.printf("%s,%s,%s,\"%s\",\"%s\",%s,%d,%.2f,%s%n",
                        f.getFolio(), f.getFecha(), f.getHora(),
                        f.getCliente(), f.getCajero(), f.getMetodoPago(),
                        f.getTotalArticulos(), f.getTotal(), f.getEstado());
            }
            alerta("CSV exportado ✔  →  " + destino.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Error al exportar CSV: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GUARDAR PDF (sin cambios)
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void guardarPDF() {
        try {
            LocalDateTime inicio = dateInicio.getValue().atStartOfDay();
            LocalDateTime fin    = dateFin.getValue().atTime(23, 59);
            List<Ticket> tickets = service.obtenerTickets(inicio, fin);
            double total    = service.calcularTotal(tickets);
            int    cantidad = service.contarTickets(tickets);
            double promedio = service.calcularPromedio(tickets);
            Map<String, Integer> top = service.topProductos(tickets);

            String tipoLimpio = cbTipoReporte.getValue()
                    .replace(" ", "_")
                    .replace("á","a").replace("é","e")
                    .replace("í","i").replace("ó","o").replace("ú","u");
            String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar reporte PDF");
            fc.setInitialFileName("Reporte_" + tipoLimpio + "_" + fechaHoy + ".pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File docFolder = new File(System.getProperty("user.home") + "/Documents");
            fc.setInitialDirectory(docFolder.exists() ? docFolder : new File(System.getProperty("user.home")));

            File archivoDestino = fc.showSaveDialog((Stage) cbTipoReporte.getScene().getWindow());
            if (archivoDestino == null) return;
            ultimaRutaPDF = archivoDestino.getAbsolutePath();

            switch (cbTipoReporte.getValue()) {
                case "Ventas"                  -> pdf.generarReporteVentas(tickets, total, cantidad, promedio, top, ultimaRutaPDF);
                case "Productos más vendidos"   -> pdf.generarTopProductos(top, ultimaRutaPDF);
                case "Bajo stock"              -> pdf.generarBajoStock(service.obtenerBajoStock(), ultimaRutaPDF);
            }
            alerta("PDF guardado ✔  →  " + archivoDestino.getName());
        } catch (Exception e) { e.printStackTrace(); alerta("Error al guardar PDF"); }
    }

    @FXML
    private void abrirPDF() {
        try {
            if (ultimaRutaPDF == null) { alerta("Primero genera un PDF"); return; }
            Desktop.getDesktop().open(new File(ultimaRutaPDF));
        } catch (Exception e) { e.printStackTrace(); alerta("No se pudo abrir el PDF"); }
    }

    // ─────────────────────────────────────────────────────────────
    // GRÁFICAS — Tab 1 (sin cambios de lógica, colores actualizados)
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void verGraficaCantidad() {
        if (ultimoTop == null) { alerta("Genera un reporte primero"); return; }
        chartVentas.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad vendida");
        for (Map.Entry<String, Integer> e : ultimoTop.entrySet())
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        chartVentas.getData().add(series);
        setEstiloBotones(btnCantidad);
        mostrarGrafica();
    }

    @FXML
    private void verGraficaIngresos() {
        if (ultimosTickets == null) { alerta("Genera un reporte primero"); return; }
        Map<String, Double> ingresos = new java.util.LinkedHashMap<>();
        for (Ticket t : ultimosTickets) {
            if (t.getLineas() == null) continue;
            for (Ticket.LineaTicket l : t.getLineas())
                ingresos.merge(l.getNombreProducto(), l.getSubtotal(), Double::sum);
        }
        ingresos = ingresos.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, java.util.LinkedHashMap::new));

        chartVentas.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ingresos ($)");
        for (Map.Entry<String, Double> e : ingresos.entrySet())
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        chartVentas.getData().add(series);
        setEstiloBotones(btnIngresos);
        mostrarGrafica();
    }

    @FXML
    private void verTabla() {
        if (ultimoTop == null) { alerta("Genera un reporte primero"); return; }

        colTablaProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProducto()));
        colTablaCantidad.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCantidad()).asObject());
        colTablaIngresos.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getIngresos()).asObject());

        Map<String, Double> ingresosPorProducto = new java.util.LinkedHashMap<>();
        if (ultimosTickets != null) {
            for (Ticket t : ultimosTickets) {
                if (t.getLineas() == null) continue;
                for (Ticket.LineaTicket l : t.getLineas())
                    ingresosPorProducto.merge(l.getNombreProducto(), l.getSubtotal(), Double::sum);
            }
        }
        ObservableList<FilaReporte> filas = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> e : ultimoTop.entrySet()) {
            double ing = ingresosPorProducto.getOrDefault(e.getKey(), 0.0);
            filas.add(new FilaReporte(e.getKey(), e.getValue(), ing));
        }
        tablaReporte.setItems(filas);
        setEstiloBotones(btnTabla);

        chartVentas.setVisible(false);  chartVentas.setManaged(false);
        tablaReporte.setVisible(true);  tablaReporte.setManaged(true);
    }

    private void mostrarGrafica() {
        chartVentas.setVisible(true);  chartVentas.setManaged(true);
        tablaReporte.setVisible(false); tablaReporte.setManaged(false);
    }

    /** Marca activo el botón pulsado y pone los demás en inactivo */
    private void setEstiloBotones(Button activo) {
        for (Button b : new Button[]{ btnCantidad, btnIngresos, btnTabla }) {
            b.setStyle(b == activo ? ESTILO_BTN_ACTIVO.replace("20", "5").replace("18", "10") :
                    "-fx-background-color: transparent; -fx-text-fill: #1a6fa8; " +
                            "-fx-border-color: #b8d4ea; -fx-border-width: 1; " +
                            "-fx-background-radius: 5; -fx-border-radius: 5; " +
                            "-fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand;");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // FILTROS RÁPIDOS (lógica sin cambios)
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void filtroRapido(ActionEvent e) {
        LocalDate hoy = LocalDate.now();
        Button origen = (Button) e.getSource();
        LocalDate inicio;
        LocalDate fin = hoy;

        if      (origen == btnHoy)    { inicio = hoy; }
        else if (origen == btnAyer)   { inicio = hoy.minusDays(1); fin = hoy.minusDays(1); }
        else if (origen == btnSemana) { inicio = hoy.with(java.time.DayOfWeek.MONDAY); }
        else if (origen == btnMes)    { inicio = hoy.withDayOfMonth(1); }
        else                          { inicio = hoy.withDayOfYear(1); }

        dateInicio.setValue(inicio);
        dateFin.setValue(fin);

        for (Button b : new Button[]{ btnHoy, btnAyer, btnSemana, btnMes, btnAnio })
            b.setStyle(b == origen ? ESTILO_BTN_ACTIVO : ESTILO_BTN_INACTIVO);

        if (cbTipoReporte.getValue() == null) cbTipoReporte.setValue("Ventas");
        generarReporte();
        cargarReportesAvanzados();
    }

    @FXML
    private void filtroMesEspecifico() {
        String mesStr = cbMesSel.getValue();
        Integer anio  = cbAnioSel.getValue();
        if (mesStr == null || anio == null) { alerta("Selecciona mes y año"); return; }

        String[] nombres = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        int mes = -1;
        for (int i = 0; i < nombres.length; i++) if (nombres[i].equals(mesStr)) { mes = i + 1; break; }

        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin    = inicio.withDayOfMonth(inicio.lengthOfMonth());
        dateInicio.setValue(inicio);
        dateFin.setValue(fin);

        for (Button b : new Button[]{ btnHoy, btnAyer, btnSemana, btnMes, btnAnio }) b.setStyle(ESTILO_BTN_INACTIVO);
        if (cbTipoReporte.getValue() == null) cbTipoReporte.setValue("Ventas");
        generarReporte();
        cargarReportesAvanzados();
    }

    // ─────────────────────────────────────────────────────────────
    // NAVEGACIÓN
    // ─────────────────────────────────────────────────────────────
    @FXML private void irADashboard()    { cambiarEscena("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML private void irAVentas()       { cambiarEscena("/org/example/vista/Ventas.fxml"); }
    @FXML private void irAInventario()   { cambiarEscena("/org/example/vista/Inventario.fxml"); }
    @FXML private void irAEmpleados()    { cambiarEscena("/org/example/vista/Empleados.fxml"); }
    @FXML private void irAClientes()     { cambiarEscena("/org/example/vista/Clientes.fxml"); }
    @FXML private void irACorteCaja()    { cambiarEscena("/org/example/vista/CorteCaja.fxml"); }
    @FXML private void irAAuditoria() {
        cambiarEscena("/org/example/vista/Auditoria.fxml");
    }
    private void registrarLogout() {
        String sql = "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, detalle) " +
                "VALUES (?, 'LOGOUT', 'usuarios', ?, ?)";
        try (java.sql.Connection con = org.example.dao.ConexionDB.getConexion();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            int idUsuario = org.example.modelo.SesionUsuario.getInstancia().getIdUsuario();
            String nombre = org.example.modelo.SesionUsuario.getInstancia().getNombre();
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setString(3, "Cierre de sesión: " + nombre);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private void irAConfiguracion(){ cambiarEscena("/org/example/vista/Configuracion.fxml"); }

    private void cambiarEscena(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            MarcaService.aplicar(root);
            Stage stage = (Stage) lblTotal.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Cambiar sesion"); a.setHeaderText(null);
        a.setContentText("Seguro que deseas cambiar de sesion?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                registrarLogout();
                cambiarEscena("/org/example/vista/Login.fxml");
            }
        });
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────
    // MODELOS INTERNOS
    // ─────────────────────────────────────────────────────────────

    /** Fila para Tab 1 (productos) — sin cambios */
    public static class FilaReporte {
        private final SimpleStringProperty  producto;
        private final SimpleIntegerProperty cantidad;
        private final SimpleDoubleProperty  ingresos;

        public FilaReporte(String producto, int cantidad, double ingresos) {
            this.producto = new SimpleStringProperty(producto);
            this.cantidad = new SimpleIntegerProperty(cantidad);
            this.ingresos = new SimpleDoubleProperty(ingresos);
        }
        public String  getProducto() { return producto.get(); }
        public int     getCantidad() { return cantidad.get(); }
        public double  getIngresos() { return ingresos.get(); }
    }

    /** Fila para Tab 2 (ventas detalladas) */
    public static class FilaVenta {
        private final String folio;
        private final String fecha;
        private final String hora;
        private final String cliente;
        private final String cajero;
        private final String metodoPago;
        private final int    totalArticulos;
        private final double total;
        private final String estado;
        private final int    idVenta;

        public FilaVenta(String folio, String fecha, String hora, String cliente,
                         String cajero, String metodoPago, int totalArticulos,
                         double total, String estado, int idVenta) {
            this.folio          = folio;
            this.fecha          = fecha;
            this.hora           = hora;
            this.cliente        = cliente;
            this.cajero         = cajero;
            this.metodoPago     = metodoPago;
            this.totalArticulos = totalArticulos;
            this.total          = total;
            this.estado         = estado;
            this.idVenta        = idVenta;
        }

        public String getFolio()          { return folio; }
        public String getFecha()          { return fecha; }
        public String getHora()           { return hora; }
        public String getCliente()        { return cliente; }
        public String getCajero()         { return cajero; }
        public String getMetodoPago()     { return metodoPago; }
        public int    getTotalArticulos() { return totalArticulos; }
        public double getTotal()          { return total; }
        public String getEstado()         { return estado; }
        public int    getIdVenta()        { return idVenta; }
    }
    // ── Cargar reportes avanzados ─────────────────────────────────────────

    private void cargarReportesAvanzados() {
        if (dateInicio.getValue() == null || dateFin.getValue() == null) return;

        String ini = dateInicio.getValue().atStartOfDay()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String fin = dateFin.getValue().atTime(23, 59, 59)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        cargarResumenNeto(ini, fin);
        cargarTablaCajeros(ini, fin);
        cargarTablaHoras(ini, fin);
        cargarTablaRentabilidad(ini, fin);
        cargarTablaClientes();
        cargarTablaFiscal(ini, fin);
    }

    private void cargarResumenNeto(String ini, String fin) {
        Map<String, Double> r = daoAvanzado.resumenNeto(ini, fin);
        if (lblVentasBrutas    != null)
            lblVentasBrutas.setText("$" + df.format(r.getOrDefault("bruto", 0.0)));
        if (lblTotalDevoluciones != null)
            lblTotalDevoluciones.setText("-$" + df.format(r.getOrDefault("devuelto", 0.0)));
        if (lblVentasNetas     != null)
            lblVentasNetas.setText("$" + df.format(r.getOrDefault("neto", 0.0)));
    }

    private void cargarTablaCajeros(String ini, String fin) {
        if (tablaCajeros == null) return;
        configurarColumnaStr(colCajNombre,   r -> (String) r.get("cajero"));
        configurarColumnaStr(colCajTickets,  r -> String.valueOf((int)(double) r.get("tickets")));
        configurarColumnaStr(colCajBruto,    r -> "$" + df.format((double) r.get("bruto")));
        configurarColumnaStr(colCajNeto,     r -> "$" + df.format((double) r.get("neto")));
        configurarColumnaStr(colCajPromedio, r -> "$" + df.format((double) r.get("promedio")));
        configurarColumnaStr(colCajDev,      r -> String.valueOf((int)(double)(
                r.get("con_devolucion") instanceof Integer
                        ? (double)((Integer)r.get("con_devolucion")).intValue()
                        : r.get("con_devolucion"))));

        tablaCajeros.getItems().setAll(daoAvanzado.ventasPorCajero(ini, fin));
    }

    private void cargarTablaHoras(String ini, String fin) {
        if (tablaHoras == null) return;
        configurarColumnaStr(colHoraHora,    r -> {
            int h = (int) r.get("hora");
            return String.format("%02d:00 - %02d:59", h, h);
        });
        configurarColumnaStr(colHoraTickets, r -> String.valueOf(r.get("tickets")));
        configurarColumnaStr(colHoraTotal,   r -> "$" + df.format((double) r.get("total")));

        tablaHoras.getItems().setAll(daoAvanzado.ventasPorHora(ini, fin));
    }

    private void cargarTablaRentabilidad(String ini, String fin) {
        if (tablaRentabilidad == null) return;
        configurarColumnaStr(colRentNombre,   r -> (String) r.get("nombre"));
        configurarColumnaStr(colRentUnidades, r -> String.valueOf(r.get("unidades")));
        configurarColumnaStr(colRentIngresos, r -> "$" + df.format((double) r.get("ingresos")));
        configurarColumnaStr(colRentCostos,   r -> "$" + df.format((double) r.get("costos")));
        configurarColumnaStr(colRentGanancia, r -> "$" + df.format((double) r.get("ganancia")));
        configurarColumnaStr(colRentMargen,   r -> r.get("margen") + "%");

        // Color en ganancia
        if (colRentGanancia != null) {
            colRentGanancia.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || v == null) { setText(null); setStyle(""); return; }
                    setText(v);
                    boolean negativo = v.startsWith("-");
                    setStyle(negativo
                            ? "-fx-text-fill: #C0392B; -fx-font-weight: bold;"
                            : "-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                }
            });
        }

        tablaRentabilidad.getItems().setAll(daoAvanzado.rentabilidadProductos(ini, fin));
    }

    private void cargarTablaClientes() {
        if (tablaClientesCredito == null) return;
        configurarColumnaStr(colCliNombre,     r -> (String) r.get("nombre"));
        configurarColumnaStr(colCliSaldo,      r -> "$" + df.format((double) r.get("saldo")));
        configurarColumnaStr(colCliDisponible, r -> "$" + df.format((double) r.get("disponible")));
        configurarColumnaStr(colCliLimite,     r -> "$" + df.format((double) r.get("limite")));
        configurarColumnaStr(colCliCompras,    r -> String.valueOf(r.get("total_compras")));
        configurarColumnaStr(colCliUltima,     r -> (String) r.get("ultima_compra"));

        // Color saldo
        if (colCliSaldo != null) {
            colCliSaldo.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || v == null) { setText(null); setStyle(""); return; }
                    setText(v);
                    setStyle(v.equals("$0.00")
                            ? "-fx-text-fill: #27AE60;"
                            : "-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                }
            });
        }

        tablaClientesCredito.getItems().setAll(daoAvanzado.clientesConCredito());
    }

    private void cargarTablaFiscal(String ini, String fin) {
        if (tablaFiscal == null) return;
        configurarColumnaStr(colFiscalConcepto, r -> (String) r.get("concepto"));
        configurarColumnaStr(colFiscalMonto, r -> "$" + df.format((double) r.get("monto")));
        tablaFiscal.getItems().setAll(daoAvanzado.reporteFiscal(ini, fin));
    }

    // Helper para no repetir setCellValueFactory
    private void configurarColumnaStr(
            TableColumn<Map<String, Object>, String> col,
            java.util.function.Function<Map<String, Object>, String> extractor) {
        if (col == null) return;
        col.setCellValueFactory(d -> {
            try { return new SimpleStringProperty(extractor.apply(d.getValue())); }
            catch (Exception e) { return new SimpleStringProperty("—"); }
        });
    }
}
