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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import java.awt.Desktop;
import javafx.stage.FileChooser;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReporteController {

    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> cbTipoReporte;

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

    @FXML private Button btnCantidad;
    @FXML private Button btnIngresos;

    @FXML private Button btnHoy;
    @FXML private Button btnAyer;
    @FXML private Button btnSemana;
    @FXML private Button btnMes;
    @FXML private Button btnAnio;

    @FXML private Button btnTabla;
    @FXML private TableView<FilaReporte> tablaReporte;
    @FXML private TableColumn<FilaReporte, String>  colTablaProducto;
    @FXML private TableColumn<FilaReporte, Integer> colTablaCantidad;
    @FXML private TableColumn<FilaReporte, Double>  colTablaIngresos;



    @FXML private ComboBox<String> cbMesSel;
    @FXML private ComboBox<Integer> cbAnioSel;


    @FXML private BarChart<String, Number> chartVentas;

    // Estilos
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


    private String ultimaRutaPDF = null;
    private Map<String, Integer> ultimoTop = null;
    private List<Ticket> ultimosTickets = null;

    @FXML private BarChart<String, Number> graficaProductos;

    private ReporteService service = new ReporteService();
    private ReportePDFService pdf = new ReportePDFService();

    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    // Navegación
    @FXML private void irADashboard()  { cambiarEscena("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML private void irAVentas() {
        cambiarEscena("/org/example/vista/Ventas.fxml");
    }
    @FXML private void irAInventario() {
        cambiarEscena("/org/example/vista/Inventario.fxml");
    }
    @FXML private void irAEmpleados() {
        cambiarEscena("/org/example/vista/Empleados.fxml");
    }
    @FXML private void irAClientes() {cambiarEscena ("/org/example/vista/Clientes.fxml"); }
    @FXML private void irACorteCaja() {cambiarEscena("/org/example/vista/CorteCaja.fxml");}
    @FXML private void irAConfiguracion() {cambiarEscena("/org/example/vista/Configuracion.fxml");}

    private void cambiarEscena(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) lblTotal.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Init
    @FXML
    public void initialize() {
        cargarDatosUsuario();

        cbTipoReporte.getItems().addAll(
                "Ventas",
                "Productos más vendidos",
                "Bajo stock"
        );

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

        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm:ss");

        javafx.animation.Timeline reloj = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    lblHora.setText(LocalDateTime.now().format(fmtHora));
                })
        );
        reloj.setCycleCount(javafx.animation.Animation.INDEFINITE);
        reloj.play();
    }

    private void cargarDatosUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        String nombre = sesion.getNombre();
        String rol = sesion.getRol();
        lblNombreUsuario.setText(nombre);
        lblRolUsuario.setText(rol);
        String iniciales = nombre.length() >= 2
                ? nombre.substring(0, 2).toUpperCase()
                : nombre.toUpperCase();
        lblAvatarIniciales.setText(iniciales);
    }

    // Genera reporte
    @FXML
    private void generarReporte() {

        try {

            if (dateInicio.getValue() == null || dateFin.getValue() == null) {
                alerta("Selecciona fechas");
                return;
            }

            if (cbTipoReporte.getValue() == null) {
                alerta("Selecciona tipo de reporte");
                return;
            }

            LocalDateTime inicio = dateInicio.getValue().atStartOfDay();
            LocalDateTime fin = dateFin.getValue().atTime(23, 59);

            List<Ticket> tickets = service.obtenerTickets(inicio, fin);

            double total = service.calcularTotal(tickets);
            int cantidad = service.contarTickets(tickets);
            double promedio = service.calcularPromedio(tickets);
            Map<String, Integer> top = service.topProductos(tickets);
            ultimoTop = top;
            ultimosTickets = tickets;

            // UI
            lblTotal.setText("$" + df.format(total));
            lblTickets.setText(String.valueOf(cantidad));
            lblPromedio.setText("$" + df.format(promedio));

            // Producto top
            if (!top.isEmpty()) {
                Map.Entry<String, Integer> primero = top.entrySet().iterator().next();
                lblProductoTop.setText(primero.getKey());
                lblProductoTopCantidad.setText(primero.getValue() + " unidades vendidas");
            } else {
                lblProductoTop.setText("Sin datos");
                lblProductoTopCantidad.setText("No hay ventas en el periodo");
            }

            // Grafica
            chartVentas.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Productos");

            for (Map.Entry<String, Integer> e : top.entrySet()) {
                series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
            }

            chartVentas.getData().add(series);

            alerta("Vista previa generada ✔");

        } catch (Exception e) {
            e.printStackTrace();
            alerta("Error al generar vista previa");
        }
    }

    @FXML
    private void guardarPDF() {
        try {
            // Regenerar datos
            LocalDateTime inicio = dateInicio.getValue().atStartOfDay();
            LocalDateTime fin = dateFin.getValue().atTime(23, 59);

            List<Ticket> tickets = service.obtenerTickets(inicio, fin);
            double total = service.calcularTotal(tickets);
            int cantidad = service.contarTickets(tickets);
            double promedio = service.calcularPromedio(tickets);
            Map<String, Integer> top = service.topProductos(tickets);

            // Nombre automatico
            String tipoLimpio = cbTipoReporte.getValue()
                    .replace(" ", "_")
                    .replace("á","a").replace("é","e")
                    .replace("í","i").replace("ó","o")
                    .replace("ú","u");
            String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nombreSugerido = "Reporte_" + tipoLimpio + "_" + fechaHoy + ".pdf";

            // Abrir explorador de archivos
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar reporte PDF");
            fileChooser.setInitialFileName(nombreSugerido);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf")
            );

            // Directorio inicial
            File docFolder = new File(System.getProperty("user.home") + "/Documents");
            fileChooser.setInitialDirectory(docFolder.exists() ? docFolder
                    : new File(System.getProperty("user.home")));

            // Obtener la ventana actual
            Stage stage = (Stage) cbTipoReporte.getScene().getWindow();
            File archivoDestino = fileChooser.showSaveDialog(stage);

            if (archivoDestino == null) return;

            ultimaRutaPDF = archivoDestino.getAbsolutePath();

            // Generar el PDF en la ruta elegida
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

            alerta("PDF guardado ✔  →  " + archivoDestino.getName());

        } catch (Exception e) {
            e.printStackTrace();
            alerta("Error al guardar PDF");
        }
    }

    // Grafica
    private void cargarGrafica(Map<String, Integer> datos) {
        if (graficaProductos == null) return;

        graficaProductos.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Productos más vendidos");

        for (Map.Entry<String, Integer> e : datos.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }

        graficaProductos.getData().add(series);
    }

    // Abrir pdf
    @FXML
    private void abrirPDF() {
        try {
            if (ultimaRutaPDF == null) {
                alerta("Primero genera un PDF");
                return;
            }

            Desktop.getDesktop().open(new File(ultimaRutaPDF));

        } catch (Exception e) {
            e.printStackTrace();
            alerta("No se pudo abrir el PDF");
        }
    }

    // Generar alerta
    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }


    public void btnCerrar(ActionEvent actionEvent) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Salir");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas salir?");
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    @FXML
    private void verGraficaCantidad() {
        if (ultimoTop == null) { alerta("Genera un reporte primero"); return; }

        chartVentas.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad vendida");

        for (Map.Entry<String, Integer> e : ultimoTop.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chartVentas.getData().add(series);

        // Estado de botones
        btnCantidad.setStyle("-fx-background-color: #6B1228; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-width: 0;");
        btnIngresos.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B5A3A; -fx-border-color: #D4C9B0; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand;");
        chartVentas.setVisible(true);
        chartVentas.setManaged(true);
        tablaReporte.setVisible(false);
        tablaReporte.setManaged(false);
    }

    @FXML
    private void verGraficaIngresos() {
        if (ultimosTickets == null) { alerta("Genera un reporte primero"); return; }

        // Calcular ingresos por producto
        Map<String, Double> ingresosPorProducto = new java.util.LinkedHashMap<>();
        for (Ticket t : ultimosTickets) {
            if (t.getLineas() == null) continue;
            for (Ticket.LineaTicket linea : t.getLineas()) {
                ingresosPorProducto.merge(
                        linea.getNombreProducto(),
                        linea.getSubtotal(),
                        Double::sum
                );
            }
        }

        // Ordenar de mayor a menor
        ingresosPorProducto = ingresosPorProducto.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, java.util.LinkedHashMap::new));

        chartVentas.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ingresos ($)");

        for (Map.Entry<String, Double> e : ingresosPorProducto.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chartVentas.getData().add(series);

        // Estado de botones
        btnIngresos.setStyle("-fx-background-color: #6B1228; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-width: 0;");
        btnCantidad.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B5A3A; -fx-border-color: #D4C9B0; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand;");
        chartVentas.setVisible(true);
        chartVentas.setManaged(true);
        tablaReporte.setVisible(false);
        tablaReporte.setManaged(false);
    }

    @FXML
    private void filtroRapido(ActionEvent e) {
        LocalDate hoy = LocalDate.now();
        Button origen = (Button) e.getSource();

        LocalDate inicio;
        LocalDate fin = hoy;

        if (origen == btnHoy) {
            inicio = hoy;

        } else if (origen == btnAyer) {
            inicio = hoy.minusDays(1);
            fin    = hoy.minusDays(1);

        } else if (origen == btnSemana) {

            inicio = hoy.with(java.time.DayOfWeek.MONDAY);

        } else if (origen == btnMes) {
            inicio = hoy.withDayOfMonth(1);

        } else {   // btnAño
            inicio = hoy.withDayOfYear(1);
        }

        // Rellenar los DatePicker
        dateInicio.setValue(inicio);
        dateFin.setValue(fin);

        // Resaltar el botón pulsado y apagar los demás
        for (Button b : new Button[]{ btnHoy, btnAyer, btnSemana, btnMes, btnAnio }) {
            b.setStyle(b == origen ? ESTILO_BTN_ACTIVO : ESTILO_BTN_INACTIVO);
        }

        // Lanzar el reporte automáticamente
        if (cbTipoReporte.getValue() == null) {
            cbTipoReporte.setValue("Ventas");
        }
        generarReporte();
    }

    @FXML
    private void filtroMesEspecifico() {
        String mesStr = cbMesSel.getValue();
        Integer anio  = cbAnioSel.getValue();

        if (mesStr == null || anio == null) {
            alerta("Selecciona mes y año");
            return;
        }

        String[] nombres = {
                "Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
        };

        int mes = -1;
        for (int i = 0; i < nombres.length; i++) {
            if (nombres[i].equals(mesStr)) {
                mes = i + 1;
                break;
            }
        }

        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin    = inicio.withDayOfMonth(inicio.lengthOfMonth());

        dateInicio.setValue(inicio);
        dateFin.setValue(fin);

        for (Button b : new Button[]{ btnHoy, btnAyer, btnSemana, btnMes, btnAnio }) {
            b.setStyle(ESTILO_BTN_INACTIVO);
        }

        if (cbTipoReporte.getValue() == null) {
            cbTipoReporte.setValue("Ventas");
        }
        generarReporte();
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
                for (Ticket.LineaTicket linea : t.getLineas()) {
                    ingresosPorProducto.merge(linea.getNombreProducto(), linea.getSubtotal(), Double::sum);
                }
            }
        }

        ObservableList<FilaReporte> filas = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> e : ultimoTop.entrySet()) {
            double ing = ingresosPorProducto.getOrDefault(e.getKey(), 0.0);
            filas.add(new FilaReporte(e.getKey(), e.getValue(), ing));
        }
        tablaReporte.setItems(filas);

        chartVentas.setVisible(false);
        chartVentas.setManaged(false);
        tablaReporte.setVisible(true);
        tablaReporte.setManaged(true);

        btnTabla.setStyle("-fx-background-color: #6B1228; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-width: 0;");
        btnCantidad.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B5A3A; -fx-border-color: #D4C9B0; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand;");
        btnIngresos.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B5A3A; -fx-border-color: #D4C9B0; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand;");
    }

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


}