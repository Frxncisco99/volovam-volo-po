package org.example.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import org.example.modelo.SesionUsuario;
import org.example.modelo.Ticket;
import org.example.servicio.ReporteService;
import org.example.servicio.ReportePDFService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.io.File;
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

    // ── Filtros ────────────────────────────────────────────────────────────────
    @FXML private DatePicker        dateInicio;
    @FXML private DatePicker        dateFin;
    @FXML private ComboBox<String>  cbTipoReporte;
    @FXML private ComboBox<String>  cbMesSel;
    @FXML private ComboBox<Integer> cbAnioSel;

    // ── Cards ──────────────────────────────────────────────────────────────────
    @FXML private Label lblTotal;
    @FXML private Label lblTickets;
    @FXML private Label lblPromedio;
    @FXML private Label lblProductoTop;
    @FXML private Label lblProductoTopCantidad;

    // ── Topbar ─────────────────────────────────────────────────────────────────
    @FXML private Label lblFecha;
    @FXML private Label lblHora;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    // ── Botones acceso rápido ──────────────────────────────────────────────────
    @FXML private Button btnHoy;
    @FXML private Button btnAyer;
    @FXML private Button btnSemana;
    @FXML private Button btnMes;
    @FXML private Button btnAnio;

    // ── Tab 1: Análisis de productos (sin cambios) ─────────────────────────────
    @FXML private Button btnCantidad;
    @FXML private Button btnIngresos;
    @FXML private Button btnTabla;
    @FXML private BarChart<String, Number>         chartVentas;
    @FXML private TableView<FilaReporte>            tablaReporte;
    @FXML private TableColumn<FilaReporte, String>  colTablaProducto;
    @FXML private TableColumn<FilaReporte, Integer> colTablaCantidad;
    @FXML private TableColumn<FilaReporte, Double>  colTablaIngresos;

    // ── Tab 2: Ventas detalladas (NUEVO) ───────────────────────────────────────
    @FXML private TableView<FilaVenta>            tablaVentas;
    @FXML private TableColumn<FilaVenta, String>  colFolio;
    @FXML private TableColumn<FilaVenta, String>  colFecha;
    @FXML private TableColumn<FilaVenta, String>  colHora;
    @FXML private TableColumn<FilaVenta, String>  colCajero;
    @FXML private TableColumn<FilaVenta, String>  colMetodoPago;
    @FXML private TableColumn<FilaVenta, String>  colCliente;
    @FXML private TableColumn<FilaVenta, Integer> colArticulos;
    @FXML private TableColumn<FilaVenta, String>  colTotalVenta;
    @FXML private TableColumn<FilaVenta, String>  colEstado;

    // ── Estado interno ─────────────────────────────────────────────────────────
    private String               ultimaRutaPDF  = null;
    private Map<String, Integer> ultimoTop      = null;
    private List<Ticket>         ultimosTickets = null;

    private final ReporteService    service = new ReporteService();
    private final ReportePDFService pdf     = new ReportePDFService();
    private final DecimalFormat     df      = new DecimalFormat("#,##0.00");

    private static final String ESTILO_BTN_ACTIVO =
            "-fx-background-color: #6B1228; -fx-text-fill: white; " +
                    "-fx-border-color: #6B1228; -fx-border-width: 1; " +
                    "-fx-border-radius: 20; -fx-background-radius: 20; " +
                    "-fx-padding: 5 18; -fx-cursor: hand; " +
                    "-fx-font-size: 11px; -fx-font-weight: bold;";

    private static final String ESTILO_BTN_INACTIVO =
            "-fx-background-color: #F5EFE6; -fx-text-fill: #6B1228; " +
                    "-fx-border-color: #C9A84C; -fx-border-width: 1; " +
                    "-fx-border-radius: 20; -fx-background-radius: 20; " +
                    "-fx-padding: 5 18; -fx-cursor: hand; " +
                    "-fx-font-size: 11px; -fx-font-weight: bold;";

    // ── Navegación ─────────────────────────────────────────────────────────────
    @FXML private void irADashboard()     { cambiarEscena("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML private void irAVentas()        { cambiarEscena("/org/example/vista/Ventas.fxml"); }
    @FXML private void irAInventario()    { cambiarEscena("/org/example/vista/Inventario.fxml"); }
    @FXML private void irAEmpleados()     { cambiarEscena("/org/example/vista/Empleados.fxml"); }
    @FXML private void irAClientes()      { cambiarEscena("/org/example/vista/Clientes.fxml"); }
    @FXML private void irACorteCaja()     { cambiarEscena("/org/example/vista/CorteCaja.fxml"); }
    @FXML private void irAConfiguracion() { cambiarEscena("/org/example/vista/Configuracion.fxml"); }

    private void cambiarEscena(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage  = (Stage) lblTotal.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Inicialización ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        cargarDatosUsuario();
        inicializarFiltros();
        inicializarTablaVentas();   // NUEVO
        iniciarReloj();
    }

    private void inicializarFiltros() {
        cbTipoReporte.getItems().addAll("Ventas", "Productos más vendidos", "Bajo stock");

        cbMesSel.getItems().addAll(
                "Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
        );
        cbMesSel.setValue("Enero");

        int anioActual = LocalDate.now().getYear();
        for (int a = anioActual; a >= anioActual - 5; a--) {
            cbAnioSel.getItems().add(a);
        }
        cbAnioSel.setValue(anioActual);
    }

    private void iniciarReloj() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        javafx.animation.Timeline reloj = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1),
                        e -> lblHora.setText(LocalDateTime.now().format(fmt)))
        );
        reloj.setCycleCount(javafx.animation.Animation.INDEFINITE);
        reloj.play();
    }

    private void cargarDatosUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        String nombre = sesion.getNombre();
        lblNombreUsuario.setText(nombre);
        lblRolUsuario.setText(sesion.getRol());
        lblAvatarIniciales.setText(
                nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase()
        );
    }

    // ── Tabla ventas detalladas — setup (NUEVO) ────────────────────────────────
    /**
     * Configura cell-value factories de tablaVentas y el doble clic para detalle.
     * Se llama UNA sola vez desde initialize().
     */
    private void inicializarTablaVentas() {
        colFolio     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        colFecha     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha()));
        colHora      .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHora()));
        colCajero    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCajero()));
        colMetodoPago.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMetodoPago()));
        colCliente   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente()));
        colArticulos .setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getArticulos()).asObject());
        colTotalVenta.setCellValueFactory(c -> new SimpleStringProperty("$" + df.format(c.getValue().getTotal())));
        colEstado    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));

        // Color de fila para ventas canceladas (cuando el modelo tenga estado)
        tablaVentas.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(FilaVenta item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if ("Cancelada".equalsIgnoreCase(item.getEstado())) {
                    setStyle("-fx-background-color: #FFF3F3;");
                } else {
                    setStyle("");
                }
            }
        });

        // Doble clic → modal de detalle
        tablaVentas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FilaVenta sel = tablaVentas.getSelectionModel().getSelectedItem();
                if (sel != null) mostrarDetalleVenta(sel.getTicketOriginal());
            }
        });
    }

    // ── Carga tabla ventas detalladas (NUEVO) ──────────────────────────────────
    /**
     * Rellena tablaVentas a partir de ultimosTickets ya cargados.
     * No realiza ninguna query extra a la base de datos.
     *
     * Getters usados del modelo Ticket actual:
     *   getIdVenta(), getFechaHora(), getNombreCajero(), getNumeroCaja(),
     *   getLineas(), getTotal(), getSubtotal(), getMontoRecibido(), getCambio()
     *
     * Campos pendientes (agregar al modelo cuando sea posible):
     *   getMetodoPago()    → String, método de pago (efectivo, tarjeta, etc.)
     *   getNombreCliente() → String, cliente asociado a la venta
     *   getEstado()        → String, "Completada" / "Cancelada" / etc.
     */
    private void cargarTablaVentas() {
        if (ultimosTickets == null) return;

        DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fmtHora  = DateTimeFormatter.ofPattern("HH:mm");

        ObservableList<FilaVenta> filas = FXCollections.observableArrayList();

        for (Ticket t : ultimosTickets) {
            LocalDateTime fecha = t.getFechaHora();

            // Contar artículos sumando cantidades de todas las líneas
            int articulos = 0;
            if (t.getLineas() != null) {
                for (Ticket.LineaTicket l : t.getLineas()) {
                    articulos += l.getCantidad();
                }
            }

            filas.add(new FilaVenta(
                    String.valueOf(t.getIdVenta()),                          // folio
                    fecha != null ? fecha.format(fmtFecha) : "—",           // fecha
                    fecha != null ? fecha.format(fmtHora)  : "—",           // hora
                    t.getNombreCajero() != null ? t.getNombreCajero() : "—",// cajero
                    "—",          // método pago  → reemplazar con t.getMetodoPago() cuando exista
                    "—",          // cliente      → reemplazar con t.getNombreCliente() cuando exista
                    articulos,
                    t.getTotal(),
                    "Completada", // estado       → reemplazar con t.getEstado() cuando exista
                    t
            ));
        }

        tablaVentas.setItems(filas);
    }

    // ── Modal de detalle de venta (NUEVO) ─────────────────────────────────────
    /**
     * Abre un Dialog con el desglose completo de la venta:
     * tabla de productos, subtotal, total, recibido y cambio.
     * Usa ÚNICAMENTE getters que existen en el modelo Ticket actual.
     */
    private void mostrarDetalleVenta(Ticket ticket) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle de venta — Folio #" + ticket.getIdVenta());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(600);

        // ── Encabezado del folio ───────────────────────────────────────────────
        Label lblTitulo = new Label("Folio #" + ticket.getIdVenta()
                + "   ·   Caja " + ticket.getNumeroCaja());
        lblTitulo.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-padding: 0 0 8 0;");

        // ── Tabla de líneas de producto ───────────────────────────────────────
        TableView<Ticket.LineaTicket> tbl = new TableView<>();
        tbl.setPrefHeight(200);
        tbl.setStyle("-fx-border-color: #E0D5C5; -fx-font-size: 12px;");
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Ticket.LineaTicket, String>  cProd = new TableColumn<>("PRODUCTO");
        TableColumn<Ticket.LineaTicket, Integer> cCant = new TableColumn<>("CANT.");
        TableColumn<Ticket.LineaTicket, String>  cPU   = new TableColumn<>("P. UNITARIO");
        TableColumn<Ticket.LineaTicket, String>  cSub  = new TableColumn<>("SUBTOTAL");

        cProd.setPrefWidth(220); cCant.setPrefWidth(55);
        cPU  .setPrefWidth(110); cSub .setPrefWidth(110);

        cProd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreProducto()));
        cCant.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCantidad()).asObject());
        cPU  .setCellValueFactory(c -> new SimpleStringProperty("$" + df.format(c.getValue().getPrecioUnitario())));
        cSub .setCellValueFactory(c -> new SimpleStringProperty("$" + df.format(c.getValue().getSubtotal())));

        tbl.getColumns().addAll(cProd, cCant, cPU, cSub);
        if (ticket.getLineas() != null) {
            tbl.setItems(FXCollections.observableArrayList(ticket.getLineas()));
        }

        // ── Resumen financiero — solo getters reales ──────────────────────────
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(16); grid.setVgap(5);
        grid.setStyle("-fx-padding: 12 4 0 4;");

        // Columna izquierda: cajero y caja
        // Columna derecha: montos
        Object[][] izq = {
                { "Cajero:",   ticket.getNombreCajero() != null ? ticket.getNombreCajero() : "—" },
                { "Caja #:",   String.valueOf(ticket.getNumeroCaja()) },
                // → Cuando el modelo tenga los campos, agrega aquí:
                // { "Método pago:", ticket.getMetodoPago() != null ? ticket.getMetodoPago() : "—" },
                // { "Cliente:",     ticket.getNombreCliente() != null ? ticket.getNombreCliente() : "Público general" },
                // { "Estado:",      ticket.getEstado() != null ? ticket.getEstado() : "Completada" },
        };

        Object[][] der = {
                { "Subtotal:",  "$" + df.format(ticket.getSubtotal()),      false },
                { "Total:",     "$" + df.format(ticket.getTotal()),          true  }, // resaltado
                { "Recibido:",  "$" + df.format(ticket.getMontoRecibido()), false },
                { "Cambio:",    "$" + df.format(ticket.getCambio()),         false },
        };

        // Renderizar columna izquierda (fila 0…n)
        for (int i = 0; i < izq.length; i++) {
            Label k = new Label((String) izq[i][0]);
            Label v = new Label((String) izq[i][1]);
            k.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-font-size: 11px;");
            v.setStyle("-fx-text-fill: #3D1A0A; -fx-font-size: 11px;");
            grid.add(k, 0, i);
            grid.add(v, 1, i);
        }

        // Separador visual entre columnas
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator(
                javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-opacity: 0.3;");
        grid.add(sep, 2, 0, 1, Math.max(izq.length, der.length));

        // Renderizar columna derecha (fila 0…n)
        for (int i = 0; i < der.length; i++) {
            Label k = new Label((String) der[i][0]);
            boolean resaltado = (boolean) der[i][2];
            Label v = new Label((String) der[i][1]);
            k.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-font-size: 11px;");
            v.setStyle(resaltado
                    ? "-fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-font-size: 14px;"
                    : "-fx-text-fill: #3D1A0A; -fx-font-size: 11px;");
            grid.add(k, 3, i);
            grid.add(v, 4, i);
        }

        // ── Contenido final ───────────────────────────────────────────────────
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10,
                lblTitulo, tbl, grid);
        content.setStyle("-fx-padding: 10;");
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    // ── Generar reporte ────────────────────────────────────────────────────────
    @FXML
    private void generarReporte() {
        try {
            if (dateInicio.getValue() == null || dateFin.getValue() == null) {
                alerta("Selecciona fechas"); return;
            }
            if (cbTipoReporte.getValue() == null) {
                alerta("Selecciona tipo de reporte"); return;
            }

            LocalDateTime inicio = dateInicio.getValue().atStartOfDay();
            LocalDateTime fin    = dateFin.getValue().atTime(23, 59);

            List<Ticket> tickets = service.obtenerTickets(inicio, fin);

            double total    = service.calcularTotal(tickets);
            int    cantidad = service.contarTickets(tickets);
            double promedio = service.calcularPromedio(tickets);
            Map<String, Integer> top = service.topProductos(tickets);

            // Guardar estado para reutilizar en tabs y gráficas sin re-query
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

            // Gráfica Tab 1
            chartVentas.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Productos");
            top.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
            chartVentas.getData().add(series);

            // Tabla detallada Tab 2 — reutiliza la misma lista, sin query extra
            cargarTablaVentas();

            alerta("Reporte generado ✔");

        } catch (Exception e) {
            e.printStackTrace();
            alerta("Error al generar reporte");
        }
    }

    // ── Exportar PDF ───────────────────────────────────────────────────────────
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
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
            File docs = new File(System.getProperty("user.home") + "/Documents");
            fc.setInitialDirectory(docs.exists() ? docs : new File(System.getProperty("user.home")));

            Stage stage = (Stage) cbTipoReporte.getScene().getWindow();
            File dest = fc.showSaveDialog(stage);
            if (dest == null) return;

            ultimaRutaPDF = dest.getAbsolutePath();

            switch (cbTipoReporte.getValue()) {
                case "Ventas":
                    pdf.generarReporteVentas(tickets, total, cantidad, promedio, top, ultimaRutaPDF);
                    break;
                case "Productos más vendidos":
                    pdf.generarTopProductos(top, ultimaRutaPDF);
                    break;
                case "Bajo stock":
                    pdf.generarBajoStock(service.obtenerBajoStock(), ultimaRutaPDF);
                    break;
            }

            alerta("PDF guardado ✔  →  " + dest.getName());

        } catch (Exception e) {
            e.printStackTrace();
            alerta("Error al guardar PDF");
        }
    }

    @FXML
    private void abrirPDF() {
        try {
            if (ultimaRutaPDF == null) { alerta("Primero genera un PDF"); return; }
            Desktop.getDesktop().open(new File(ultimaRutaPDF));
        } catch (Exception e) {
            e.printStackTrace();
            alerta("No se pudo abrir el PDF");
        }
    }

    // ── Vistas del Tab 1 ───────────────────────────────────────────────────────
    @FXML
    private void verGraficaCantidad() {
        if (ultimoTop == null) { alerta("Genera un reporte primero"); return; }

        chartVentas.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad vendida");
        ultimoTop.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        chartVentas.getData().add(series);

        activarVistaBtns(btnCantidad);
        chartVentas.setVisible(true);   chartVentas.setManaged(true);
        tablaReporte.setVisible(false); tablaReporte.setManaged(false);
    }

    @FXML
    private void verGraficaIngresos() {
        if (ultimosTickets == null) { alerta("Genera un reporte primero"); return; }

        Map<String, Double> ing = calcularIngresosPorProducto(); // método centralizado

        chartVentas.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ingresos ($)");
        ing.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        chartVentas.getData().add(series);

        activarVistaBtns(btnIngresos);
        chartVentas.setVisible(true);   chartVentas.setManaged(true);
        tablaReporte.setVisible(false); tablaReporte.setManaged(false);
    }

    @FXML
    private void verTabla() {
        if (ultimoTop == null) { alerta("Genera un reporte primero"); return; }

        colTablaProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProducto()));
        colTablaCantidad.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCantidad()).asObject());
        colTablaIngresos.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getIngresos()).asObject());

        Map<String, Double> ing = calcularIngresosPorProducto(); // mismo método, sin duplicar

        ObservableList<FilaReporte> filas = FXCollections.observableArrayList();
        ultimoTop.forEach((prod, cant) ->
                filas.add(new FilaReporte(prod, cant, ing.getOrDefault(prod, 0.0)))
        );
        tablaReporte.setItems(filas);

        activarVistaBtns(btnTabla);
        chartVentas.setVisible(false);  chartVentas.setManaged(false);
        tablaReporte.setVisible(true);  tablaReporte.setManaged(true);
    }

    /**
     * Calcula ingresos por producto a partir de ultimosTickets.
     * Centraliza la lógica que antes estaba DUPLICADA en verGraficaIngresos() y verTabla().
     */
    private Map<String, Double> calcularIngresosPorProducto() {
        Map<String, Double> mapa = new java.util.LinkedHashMap<>();
        if (ultimosTickets == null) return mapa;

        for (Ticket t : ultimosTickets) {
            if (t.getLineas() == null) continue;
            for (Ticket.LineaTicket linea : t.getLineas()) {
                mapa.merge(linea.getNombreProducto(), linea.getSubtotal(), Double::sum);
            }
        }

        return mapa.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, java.util.LinkedHashMap::new
                ));
    }

    /** Aplica estilo activo/inactivo a los 3 botones de vista del Tab 1. */
    private void activarVistaBtns(Button activo) {
        String on  = "-fx-background-color: #6B1228; -fx-text-fill: white; -fx-background-radius: 5; " +
                "-fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-width: 0;";
        String off = "-fx-background-color: transparent; -fx-text-fill: #8B5A3A; -fx-border-color: #D4C9B0; " +
                "-fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5; " +
                "-fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand;";
        for (Button b : new Button[]{ btnCantidad, btnIngresos, btnTabla }) {
            b.setStyle(b == activo ? on : off);
        }
    }

    // ── Filtros rápidos ────────────────────────────────────────────────────────
    @FXML
    private void filtroRapido(ActionEvent e) {
        LocalDate hoy    = LocalDate.now();
        Button    origen = (Button) e.getSource();
        LocalDate inicio, fin = hoy;

        if      (origen == btnHoy)    { inicio = hoy; }
        else if (origen == btnAyer)   { inicio = hoy.minusDays(1); fin = inicio; }
        else if (origen == btnSemana) { inicio = hoy.with(java.time.DayOfWeek.MONDAY); }
        else if (origen == btnMes)    { inicio = hoy.withDayOfMonth(1); }
        else                          { inicio = hoy.withDayOfYear(1); }

        dateInicio.setValue(inicio);
        dateFin.setValue(fin);

        for (Button b : new Button[]{ btnHoy, btnAyer, btnSemana, btnMes, btnAnio }) {
            b.setStyle(b == origen ? ESTILO_BTN_ACTIVO : ESTILO_BTN_INACTIVO);
        }
        if (cbTipoReporte.getValue() == null) cbTipoReporte.setValue("Ventas");
        generarReporte();
    }

    @FXML
    private void filtroMesEspecifico() {
        String  mesStr = cbMesSel.getValue();
        Integer anio   = cbAnioSel.getValue();
        if (mesStr == null || anio == null) { alerta("Selecciona mes y año"); return; }

        String[] nombres = {
                "Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
        };
        int mes = -1;
        for (int i = 0; i < nombres.length; i++) {
            if (nombres[i].equals(mesStr)) { mes = i + 1; break; }
        }

        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin    = inicio.withDayOfMonth(inicio.lengthOfMonth());

        dateInicio.setValue(inicio);
        dateFin.setValue(fin);
        for (Button b : new Button[]{ btnHoy, btnAyer, btnSemana, btnMes, btnAnio }) {
            b.setStyle(ESTILO_BTN_INACTIVO);
        }
        if (cbTipoReporte.getValue() == null) cbTipoReporte.setValue("Ventas");
        generarReporte();
    }

    // ── Sesión ─────────────────────────────────────────────────────────────────
    @FXML
    public void btnCerrar(ActionEvent actionEvent) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Salir");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas salir?");
        alerta.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) Platform.exit(); });
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ── Modelos internos ───────────────────────────────────────────────────────

    /** Fila para Tab 1 — análisis de productos (sin cambios respecto al original). */
    public static class FilaReporte {
        private final SimpleStringProperty  producto;
        private final SimpleIntegerProperty cantidad;
        private final SimpleDoubleProperty  ingresos;

        public FilaReporte(String producto, int cantidad, double ingresos) {
            this.producto = new SimpleStringProperty(producto);
            this.cantidad = new SimpleIntegerProperty(cantidad);
            this.ingresos = new SimpleDoubleProperty(ingresos);
        }
        public String getProducto() { return producto.get(); }
        public int    getCantidad() { return cantidad.get(); }
        public double getIngresos() { return ingresos.get(); }
    }

    /**
     * Fila para Tab 2 — ventas detalladas.
     * Guarda referencia al Ticket original para abrir el modal de detalle.
     *
     * Campos pendientes en el modelo Ticket:
     *   metodoPago  → actualmente "—" hasta que Ticket.getMetodoPago() exista
     *   cliente     → actualmente "—" hasta que Ticket.getNombreCliente() exista
     *   estado      → actualmente "Completada" hasta que Ticket.getEstado() exista
     */
    public static class FilaVenta {
        private final String folio, fecha, hora, cajero, metodoPago, cliente, estado;
        private final int    articulos;
        private final double total;
        private final Ticket ticketOriginal;

        public FilaVenta(String folio, String fecha, String hora, String cajero,
                         String metodoPago, String cliente, int articulos,
                         double total, String estado, Ticket ticketOriginal) {
            this.folio          = folio;
            this.fecha          = fecha;
            this.hora           = hora;
            this.cajero         = cajero;
            this.metodoPago     = metodoPago;
            this.cliente        = cliente;
            this.articulos      = articulos;
            this.total          = total;
            this.estado         = estado;
            this.ticketOriginal = ticketOriginal;
        }

        public String getFolio()          { return folio; }
        public String getFecha()          { return fecha; }
        public String getHora()           { return hora; }
        public String getCajero()         { return cajero; }
        public String getMetodoPago()     { return metodoPago; }
        public String getCliente()        { return cliente; }
        public int    getArticulos()      { return articulos; }
        public double getTotal()          { return total; }
        public String getEstado()         { return estado; }
        public Ticket getTicketOriginal() { return ticketOriginal; }
    }
}