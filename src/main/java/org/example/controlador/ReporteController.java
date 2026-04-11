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
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    @FXML private Label lblProductoTop;
    @FXML private Label lblProductoTopCantidad;

    @FXML private Button btnCantidad;
    @FXML private Button btnIngresos;

    @FXML private BarChart<String, Number> chartVentas;

    private String ultimaRutaPDF = null;
    private Map<String, Integer> ultimoTop = null;
    private List<Ticket> ultimosTickets = null;

    @FXML private BarChart<String, Number> graficaProductos;

    private ReporteService service = new ReporteService();
    private ReportePDFService pdf = new ReportePDFService();

    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    // 🔁 NAVEGACIÓN
    @FXML
    private void irAVentas() {
        cambiarEscena("/org/example/vista/Ventas.fxml");
    }

    @FXML
    private void irAInventario() {
        cambiarEscena("/org/example/vista/Inventario.fxml");
    }

    @FXML
    private void irAEmpleados() {
        cambiarEscena("/org/example/vista/Empleados.fxml");
    }

    @FXML
    private void irACorteCaja() {
        cambiarEscena("/org/example/vista/CorteCaja.fxml");
    }

    private void cambiarEscena(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) lblTotal.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // 🔧 INIT
    @FXML
    public void initialize() {
        cargarDatosUsuario();
        cbTipoReporte.getItems().addAll(
                "Ventas",
                "Productos más vendidos",
                "Bajo stock"
        );
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

    // 📊 GENERAR REPORTE
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

            // 📊 GRAFICA
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

            // Nombre sugerido automático: Reporte_Ventas_2025-01-15.pdf
            String tipoLimpio = cbTipoReporte.getValue()
                    .replace(" ", "_")
                    .replace("á","a").replace("é","e")
                    .replace("í","i").replace("ó","o")
                    .replace("ú","u");
            String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nombreSugerido = "Reporte_" + tipoLimpio + "_" + fechaHoy + ".pdf";

            // Abrir explorador de archivos nativo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar reporte PDF");
            fileChooser.setInitialFileName(nombreSugerido);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf")
            );

            // Directorio inicial: Documents si existe, si no home
            File docFolder = new File(System.getProperty("user.home") + "/Documents");
            fileChooser.setInitialDirectory(docFolder.exists() ? docFolder
                    : new File(System.getProperty("user.home")));

            // Obtener la ventana actual para el diálogo modal
            Stage stage = (Stage) cbTipoReporte.getScene().getWindow();
            File archivoDestino = fileChooser.showSaveDialog(stage);

            if (archivoDestino == null) return; // El usuario canceló

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

    // 📊 GRAFICA
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

    // 📂 ABRIR PDF
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

    // 🔔 ALERTA
    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public void irADashboard(ActionEvent actionEvent) {
        cambiarEscena("/org/example/vista/MenuPrincipal.fxml");
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

        // Estado visual de botones
        btnCantidad.setStyle("-fx-background-color: #6B1228; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-width: 0;");
        btnIngresos.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B5A3A; -fx-border-color: #D4C9B0; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand;");
    }

    @FXML
    private void verGraficaIngresos() {
        if (ultimosTickets == null) { alerta("Genera un reporte primero"); return; }

        // Calcular ingresos por producto sumando subtotales de líneas
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

        // Estado visual de botones
        btnIngresos.setStyle("-fx-background-color: #6B1228; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-width: 0;");
        btnCantidad.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B5A3A; -fx-border-color: #D4C9B0; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5; -fx-font-size: 10px; -fx-padding: 4 10; -fx-cursor: hand;");
    }

}