package org.example.controlador;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.dao.CorteCajaDAO;
import org.example.modelo.CorteCajaReporte;
import org.example.modelo.SesionUsuario;
import org.example.servicio.FolioService;
import org.example.servicio.ReportePDFService;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CorteCajaController {

    @FXML private Label lblFechaHoy;
    @FXML private Label lblFolio;
    @FXML private Label lblIdCaja;
    @FXML private Label lblEstadoCorte;
    @FXML private Label lblCajero;
    @FXML private Label lblHoraApertura;
    @FXML private Label lblHoraCierre;
    @FXML private Label lblFondoInicial;
    @FXML private Label lblTotalVendido;
    @FXML private Label lblNumTickets;
    @FXML private Label lblPromedioTicket;
    @FXML private Label lblTotalEntradas;
    @FXML private Label lblTotalSalidas;
    @FXML private Label lblDineroEsperado;
    @FXML private Label lblDiferencia;
    @FXML private Label lblEstadoDiferencia;
    @FXML private Label lblObsRequerida;
    @FXML private Label lblTotalCancelado;
    @FXML private Label lblCantidadCancelaciones;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotalImpuestos;
    @FXML private Label lblIngresos;
    @FXML private Label lblCostos;
    @FXML private Label lblUtilidad;
    @FXML private TextField txtDineroContado;
    @FXML private TextArea txtObservaciones;

    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    @FXML private VBox vboxMetodosPago;

    @FXML private TableView<CorteCajaReporte.MovimientoCaja> tablaMovimientos;
    @FXML private TableColumn<CorteCajaReporte.MovimientoCaja, String> colMovTipo;
    @FXML private TableColumn<CorteCajaReporte.MovimientoCaja, String> colMovConcepto;
    @FXML private TableColumn<CorteCajaReporte.MovimientoCaja, String> colMovMonto;
    @FXML private TableColumn<CorteCajaReporte.MovimientoCaja, String> colMovFecha;
    @FXML private TableColumn<CorteCajaReporte.MovimientoCaja, String> colMovUsuario;

    @FXML private TableView<CorteCajaReporte.CancelacionDevolucion> tablaCancelaciones;
    @FXML private TableColumn<CorteCajaReporte.CancelacionDevolucion, String> colCanTipo;
    @FXML private TableColumn<CorteCajaReporte.CancelacionDevolucion, String> colCanFolio;
    @FXML private TableColumn<CorteCajaReporte.CancelacionDevolucion, String> colCanTotal;
    @FXML private TableColumn<CorteCajaReporte.CancelacionDevolucion, String> colCanMotivo;
    @FXML private TableColumn<CorteCajaReporte.CancelacionDevolucion, String> colCanUsuario;

    @FXML private TableView<CorteCajaReporte.ProductoVendido> tablaProductos;
    @FXML private TableColumn<CorteCajaReporte.ProductoVendido, String> colProdNombre;
    @FXML private TableColumn<CorteCajaReporte.ProductoVendido, String> colProdCantidad;
    @FXML private TableColumn<CorteCajaReporte.ProductoVendido, String> colProdIngresos;
    @FXML private TableColumn<CorteCajaReporte.ProductoVendido, String> colProdUtilidad;

    @FXML private TableView<CorteCajaReporte.HistorialCorte> tablaHistorial;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisFolio;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisCajero;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisCaja;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisApertura;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisCierre;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisVentas;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisEsperado;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisReal;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisDiferencia;
    @FXML private TableColumn<CorteCajaReporte.HistorialCorte, String> colHisEstado;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private Label lblTotalCortes;

    private final CorteCajaDAO dao = new CorteCajaDAO();
    private final ReportePDFService pdf = new ReportePDFService();
    private CorteCajaReporte reporteActual;

    private static final DateTimeFormatter FECHA_LARGA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    @FXML
    public void initialize() {
        configurarSesion();
        configurarTablas();
        cargarDatos();
        txtDineroContado.textProperty().addListener((obs, old, nuevo) -> actualizarConteoFisico(nuevo));
        lblObsRequerida.setVisible(false);
    }

    private void configurarSesion() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String nombre = sesion.getNombre() == null ? "US" : sesion.getNombre().trim();
        lblAvatarIniciales.setText(nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase());
        String fecha = LocalDateTime.now().format(FECHA_LARGA);
        lblFechaHoy.setText(fecha.substring(0, 1).toUpperCase() + fecha.substring(1));
        lblIdCaja.setText("Caja #" + sesion.getIdCaja());
    }

    private void configurarTablas() {
        colMovTipo.setCellValueFactory(d -> texto(d.getValue().getTipo()));
        colMovConcepto.setCellValueFactory(d -> texto(d.getValue().getConcepto()));
        colMovMonto.setCellValueFactory(d -> texto(moneda(d.getValue().getMonto())));
        colMovFecha.setCellValueFactory(d -> texto(fecha(d.getValue().getFecha(), FECHA_CORTA)));
        colMovUsuario.setCellValueFactory(d -> texto(d.getValue().getUsuario()));

        colCanTipo.setCellValueFactory(d -> texto(d.getValue().getTipo()));
        colCanFolio.setCellValueFactory(d -> texto(d.getValue().getFolio()));
        colCanTotal.setCellValueFactory(d -> texto(moneda(d.getValue().getTotal())));
        colCanMotivo.setCellValueFactory(d -> texto(d.getValue().getMotivo()));
        colCanUsuario.setCellValueFactory(d -> texto(d.getValue().getUsuarioAutorizo()));

        colProdNombre.setCellValueFactory(d -> texto(d.getValue().getProducto()));
        colProdCantidad.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getCantidad())));
        colProdIngresos.setCellValueFactory(d -> texto(moneda(d.getValue().getIngresos())));
        colProdUtilidad.setCellValueFactory(d -> texto(moneda(d.getValue().getUtilidad())));

        colHisFolio.setCellValueFactory(d -> texto(d.getValue().getFolio()));
        colHisCajero.setCellValueFactory(d -> texto(d.getValue().getCajero()));
        colHisCaja.setCellValueFactory(d -> texto("Caja #" + d.getValue().getIdCaja()));
        colHisApertura.setCellValueFactory(d -> texto(fecha(d.getValue().getApertura(), FECHA_CORTA)));
        colHisCierre.setCellValueFactory(d -> texto(fecha(d.getValue().getCierre(), FECHA_CORTA)));
        colHisVentas.setCellValueFactory(d -> texto(moneda(d.getValue().getVentas())));
        colHisEsperado.setCellValueFactory(d -> texto(moneda(d.getValue().getEsperado())));
        colHisReal.setCellValueFactory(d -> texto(moneda(d.getValue().getContado())));
        colHisDiferencia.setCellValueFactory(d -> texto(monedaConSigno(d.getValue().getDiferencia())));
        colHisEstado.setCellValueFactory(d -> texto(d.getValue().getEstado()));
        colHisDiferencia.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if (item.startsWith("-")) setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                else if (item.equals("$0.00")) setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                else setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
            }
        });
        tablaHistorial.setRowFactory(tv -> new TableRow<>());
    }

    private SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor == null || valor.isBlank() ? "-" : valor);
    }

    private void cargarDatos() {
        try {
            SesionUsuario sesion = SesionUsuario.getInstancia();
            reporteActual = dao.obtenerCorteActual(sesion.getIdCaja(), sesion.getNombre());
            pintarReporte(reporteActual);
            cargarHistorial10();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el corte de caja.");
        }
    }

    private void pintarReporte(CorteCajaReporte r) {
        lblFolio.setText(r.getFolio());
        lblCajero.setText(r.getCajero());
        lblHoraApertura.setText(fecha(r.getFechaApertura(), FECHA_HORA));
        lblHoraCierre.setText(fecha(r.getFechaCierre(), FECHA_HORA));
        lblEstadoCorte.setText(normalizarEstado(r.getEstado()));
        lblFondoInicial.setText(moneda(r.getFondoInicial()));
        lblTotalVendido.setText(moneda(r.getTotalVendido()));
        lblNumTickets.setText(String.valueOf(r.getCantidadTickets()));
        lblPromedioTicket.setText(moneda(r.getPromedioTicket()));
        lblTotalEntradas.setText(moneda(r.getTotalEntradas()));
        lblTotalSalidas.setText(moneda(r.getTotalSalidas()));
        lblDineroEsperado.setText(moneda(r.getEfectivoEsperado()));
        lblTotalCancelado.setText(moneda(r.getTotalCancelado()));
        lblCantidadCancelaciones.setText(String.valueOf(r.getCantidadCancelaciones()));
        lblSubtotal.setText(moneda(r.getSubtotal()));
        lblIva.setText(moneda(r.getIva()));
        lblTotalImpuestos.setText(moneda(r.getTotalConImpuestos()));
        lblIngresos.setText(moneda(r.getIngresos()));
        lblCostos.setText(moneda(r.getCostos()));
        lblUtilidad.setText(moneda(r.getUtilidad()));
        lblUtilidad.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " +
                (r.getUtilidad() >= 0 ? "#15803d;" : "#dc2626;"));

        tablaMovimientos.setItems(FXCollections.observableArrayList(r.getMovimientos()));
        tablaCancelaciones.setItems(FXCollections.observableArrayList(r.getCancelacionesDevoluciones()));
        tablaProductos.setItems(FXCollections.observableArrayList(r.getProductosMasVendidos()));
        pintarMetodosPago(r);
        actualizarConteoFisico(txtDineroContado.getText());
    }

    private void pintarMetodosPago(CorteCajaReporte r) {
        vboxMetodosPago.getChildren().clear();
        for (CorteCajaReporte.MetodoPago metodo : r.getMetodosPago()) {
            vboxMetodosPago.getChildren().add(crearFilaMetodo(metodo));
        }
    }

    private HBox crearFilaMetodo(CorteCajaReporte.MetodoPago metodo) {
        HBox fila = new HBox(10);
        fila.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #dbe7f3; -fx-border-radius: 10; -fx-padding: 12;");

        Label icono = new Label(iconoMetodo(metodo.getMetodo()));
        icono.setStyle("-fx-min-width: 34; -fx-min-height: 34; -fx-alignment: center; -fx-background-radius: 17; -fx-background-color: #eaf2ff; -fx-text-fill: #1d4ed8; -fx-font-family: 'Font Awesome 5 Free Solid';");

        VBox textos = new VBox(2);
        Label nombre = new Label(metodo.getMetodo());
        nombre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #172033;");
        Label tickets = new Label(metodo.getCantidad() + " ticket(s)");
        tickets.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        textos.getChildren().addAll(nombre, tickets);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label monto = new Label(moneda(metodo.getTotal()));
        monto.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        fila.getChildren().addAll(icono, textos, spacer, monto);
        return fila;
    }

    private String iconoMetodo(String metodo) {
        return switch (metodo) {
            case "Efectivo" -> "\uf3d1";
            case "Tarjeta" -> "\uf09d";
            case "Transferencia" -> "\uf1d8";
            case "QR" -> "\uf029";
            case "Credito" -> "\uf555";
            default -> "\uf155";
        };
    }

    private void actualizarConteoFisico(String texto) {
        if (reporteActual == null) return;
        double contado = 0;
        try {
            if (texto != null && !texto.trim().isEmpty()) contado = Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            lblDiferencia.setText("$0.00");
            lblEstadoDiferencia.setText("Ingresa un numero valido");
            return;
        }

        double diferencia = contado - reporteActual.getEfectivoEsperado();
        reporteActual.setEfectivoContado(contado);
        reporteActual.setDiferencia(diferencia);
        lblDiferencia.setText(monedaConSigno(diferencia));

        if (diferencia == 0) {
            lblDiferencia.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #15803d;");
            lblEstadoDiferencia.setText("Cuadre correcto");
            lblEstadoDiferencia.setStyle("-fx-text-fill: #15803d; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblObsRequerida.setVisible(false);
        } else if (diferencia < 0) {
            lblDiferencia.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
            lblEstadoDiferencia.setText("Faltante de efectivo");
            lblEstadoDiferencia.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblObsRequerida.setVisible(true);
        } else {
            lblDiferencia.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
            lblEstadoDiferencia.setText("Sobrante de efectivo");
            lblEstadoDiferencia.setStyle("-fx-text-fill: #2563eb; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblObsRequerida.setVisible(true);
        }
    }

    @FXML
    public void handleCerrarCaja() {
        if (reporteActual == null) return;
        if (txtDineroContado.getText().trim().isEmpty()) {
            mostrarAlerta("Campo requerido", "Ingresa el efectivo contado.");
            return;
        }
        actualizarConteoFisico(txtDineroContado.getText());

        if (reporteActual.getDiferencia() != 0 && txtObservaciones.getText().trim().isEmpty()) {
            mostrarAlerta("Observaciones requeridas", "Hay diferencia de efectivo. Captura una observacion antes de cerrar.");
            txtObservaciones.requestFocus();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar caja");
        confirmacion.setHeaderText("Confirmar corte de caja");
        confirmacion.setContentText("Se cerrara la caja y se registrara el corte. Esta accion no se puede deshacer.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) cerrarCaja();
        });
    }

    private void cerrarCaja() {
        try {
            SesionUsuario sesion = SesionUsuario.getInstancia();
            int idCorte = dao.registrarCorte(sesion.getIdCaja(), sesion.getIdUsuario(), reporteActual, txtObservaciones.getText().trim());
            reporteActual.setFolio(FolioService.corte(idCorte));
            reporteActual.setEstado("CERRADO");
            reporteActual.setFechaCierre(LocalDateTime.now());

            File destino = elegirArchivoPDF("Corte_Caja_" + reporteActual.getFolio() + ".pdf");
            if (destino != null) {
                pdf.generarCorteCaja(reporteActual, txtObservaciones.getText().trim(), destino.getAbsolutePath());
            }

            sesion.setIdCaja(0);
            mostrarInfo("Caja cerrada", "El corte " + reporteActual.getFolio() + " se registro correctamente.");
            Platform.exit();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cerrar la caja.");
        }
    }

    @FXML
    private void exportarCortePDF() {
        if (reporteActual == null) return;
        try {
            actualizarConteoFisico(txtDineroContado.getText());
            File destino = elegirArchivoPDF("Reporte_Corte_Caja_" + LocalDate.now() + ".pdf");
            if (destino == null) return;
            pdf.generarCorteCaja(reporteActual, txtObservaciones.getText().trim(), destino.getAbsolutePath());
            mostrarInfo("PDF generado", "Corte guardado en:\n" + destino.getName());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el PDF.");
        }
    }

    private File elegirArchivoPDF(String nombre) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar corte de caja");
        chooser.setInitialFileName(nombre);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        File docs = new File(System.getProperty("user.home"), "Documents");
        chooser.setInitialDirectory(docs.exists() ? docs : new File(System.getProperty("user.home")));
        return chooser.showSaveDialog((Stage) lblFechaHoy.getScene().getWindow());
    }

    @FXML public void cargarHistorial10() { cargarHistorial(new CorteCajaReporte.FiltroHistorial(null, null, 10)); }
    @FXML public void cargarHistorial30() { cargarHistorial(new CorteCajaReporte.FiltroHistorial(null, null, 30)); }
    @FXML public void cargarHistorialTodos() { cargarHistorial(new CorteCajaReporte.FiltroHistorial(null, null, 0)); }

    @FXML
    public void filtrarHistorial() {
        cargarHistorial(new CorteCajaReporte.FiltroHistorial(dpInicio.getValue(), dpFin.getValue(), 0));
    }

    @FXML
    public void limpiarFiltrosHistorial() {
        dpInicio.setValue(null);
        dpFin.setValue(null);
        cargarHistorial10();
    }

    private void cargarHistorial(CorteCajaReporte.FiltroHistorial filtro) {
        try {
            var datos = dao.obtenerHistorial(filtro);
            tablaHistorial.setItems(FXCollections.observableArrayList(datos));
            lblTotalCortes.setText(datos.size() + " corte(s) encontrados");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el historial de cortes.");
        }
    }

    @FXML public void irADashboard() { navegar("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML public void irAVentas() { navegar("/org/example/vista/Ventas.fxml"); }
    @FXML private void irAInventario() { navegar("/org/example/vista/Inventario.fxml"); }
    @FXML private void irAClientes() { navegar("/org/example/vista/Clientes.fxml"); }
    @FXML private void irAReportes() { navegar("/org/example/vista/Reportes.fxml"); }
    @FXML public void irAEmpleados() { navegar("/org/example/vista/Empleados.fxml"); }
    @FXML private void irAAuditoria() { navegar("/org/example/vista/Auditoria.fxml"); }
    @FXML private void irAConfiguracion() { navegar("/org/example/vista/Configuracion.fxml"); }

    @FXML
    public void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Salir");
        a.setHeaderText(null);
        a.setContentText("Seguro que deseas salir?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) Platform.exit();
        });
    }

    private void navegar(String ruta) {
        try {
            Parent root = new FXMLLoader(getClass().getResource(ruta)).load();
            Stage stage = (Stage) lblFechaHoy.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fecha(LocalDateTime fecha, DateTimeFormatter formato) {
        return fecha == null ? "-" : fecha.format(formato);
    }

    private String moneda(double valor) {
        return "$" + String.format("%,.2f", valor);
    }

    private String monedaConSigno(double valor) {
        if (valor > 0) return "+$" + String.format("%,.2f", valor);
        if (valor < 0) return "-$" + String.format("%,.2f", Math.abs(valor));
        return "$0.00";
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) return "ABIERTO";
        return estado.toUpperCase(Locale.ROOT);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
