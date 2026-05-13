package org.example.controlador;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.dao.ReporteAvanzadoDAO;
import org.example.modelo.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AuditoriaController {

    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;
    @FXML private Label lblAcceso;
    @FXML private Label lblTotalAcciones;
    @FXML private Label lblLogins;
    @FXML private Label lblCancelaciones;
    @FXML private Label lblAjustes;
    @FXML private Label lblConteo;

    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    @FXML private TextField txtFiltroUsuario;
    @FXML private ComboBox<String> cmbFiltroAccion;

    @FXML private TableView<Map<String, Object>>           tablaAuditoria;
    @FXML private TableColumn<Map<String, Object>, String> colFecha;
    @FXML private TableColumn<Map<String, Object>, String> colUsuario;
    @FXML private TableColumn<Map<String, Object>, String> colAccion;
    @FXML private TableColumn<Map<String, Object>, String> colTabla;
    @FXML private TableColumn<Map<String, Object>, String> colDetalle;

    private final ReporteAvanzadoDAO dao = new ReporteAvanzadoDAO();

    @FXML
    public void initialize() {
        // Sesión
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);

        // Solo admin
        boolean esAdmin = "admin".equalsIgnoreCase(sesion.getRol());
        if (!esAdmin) {
            lblAcceso.setText("⛔ Sin acceso");
            lblAcceso.setStyle("-fx-background-color: #fde8e8; -fx-text-fill: #C0392B; -fx-background-radius: 10; -fx-padding: 4 12; -fx-font-size: 11px;");
            tablaAuditoria.setPlaceholder(new Label("⛔ Solo el administrador puede ver la auditoría."));
            return;
        }

        // Filtro acciones
        cmbFiltroAccion.getItems().addAll(
                "Todas", "LOGIN", "LOGOUT", "VENTA", "CANCELACION",
                "DEVOLUCION", "AJUSTE_STOCK");
        cmbFiltroAccion.setValue("Todas");

        // Fechas por defecto — hoy
        dateInicio.setValue(LocalDate.now());
        dateFin.setValue(LocalDate.now());

        // Columnas
        configurarColumnas();

        // Cargar stats y datos del día
        cargarStats();
        buscar();
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(d ->
                new SimpleStringProperty((String) d.getValue().get("fecha")));
        colUsuario.setCellValueFactory(d ->
                new SimpleStringProperty((String) d.getValue().get("usuario")));
        colTabla.setCellValueFactory(d ->
                new SimpleStringProperty((String) d.getValue().get("tabla")));
        colDetalle.setCellValueFactory(d ->
                new SimpleStringProperty((String) d.getValue().get("detalle")));

        // Acción con color
        colAccion.setCellValueFactory(d ->
                new SimpleStringProperty((String) d.getValue().get("accion")));
        colAccion.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                String style;
                if      ("LOGIN".equals(v))        style = "-fx-text-fill: #1a6fa8; -fx-font-weight: bold;";
                else if ("VENTA".equals(v))        style = "-fx-text-fill: #1e7d3e; -fx-font-weight: bold;";
                else if ("CANCELACION".equals(v))  style = "-fx-text-fill: #C0392B; -fx-font-weight: bold;";
                else if ("DEVOLUCION".equals(v))   style = "-fx-text-fill: #e88c1a; -fx-font-weight: bold;";
                else if ("AJUSTE_STOCK".equals(v)) style = "-fx-text-fill: #6a2fa0; -fx-font-weight: bold;";
                else if ("LOGOUT".equals(v)) style = "-fx-text-fill: #6a96b8; -fx-font-weight: bold;";
                else                               style = "-fx-text-fill: #6a96b8;";
                setStyle(style);
            }
        });

        // Zebra striping
        tablaAuditoria.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: #f5f9ff;"
                        : "-fx-background-color: #eaf2fa;");
            }
        });
    }

    private void cargarStats() {
        String hoy = LocalDate.now().toString();
        String sql = """
            SELECT 
                COUNT(*)                                          AS total,
                SUM(CASE WHEN accion = 'LOGIN'       THEN 1 ELSE 0 END) AS logins,
                SUM(CASE WHEN accion = 'CANCELACION' THEN 1 ELSE 0 END) AS cancelaciones,
                SUM(CASE WHEN accion = 'AJUSTE_STOCK'THEN 1 ELSE 0 END) AS ajustes
            FROM auditoria
            WHERE DATE(fecha) = CURDATE()
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblTotalAcciones.setText(String.valueOf(rs.getInt("total")));
                lblLogins.setText(String.valueOf(rs.getInt("logins")));
                lblCancelaciones.setText(String.valueOf(rs.getInt("cancelaciones")));
                lblAjustes.setText(String.valueOf(rs.getInt("ajustes")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void buscar() {
        if (dateInicio.getValue() == null || dateFin.getValue() == null) return;

        String ini = dateInicio.getValue().atStartOfDay()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String fin = dateFin.getValue().atTime(23, 59, 59)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String usuario = txtFiltroUsuario.getText().trim();
        String accion  = cmbFiltroAccion.getValue() != null
                && !cmbFiltroAccion.getValue().equals("Todas")
                ? cmbFiltroAccion.getValue() : "";

        List<Map<String, Object>> datos = dao.obtenerAuditoria(ini, fin, usuario, accion);
        tablaAuditoria.getItems().setAll(datos);
        lblConteo.setText(datos.size() + " registro(s) encontrados");
    }

    @FXML
    public void limpiar() {
        dateInicio.setValue(LocalDate.now());
        dateFin.setValue(LocalDate.now());
        txtFiltroUsuario.clear();
        cmbFiltroAccion.setValue("Todas");
        buscar();
    }

    // Navegación
    @FXML public void irADashboard()    { navegar("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML public void irAVentas()       { navegar("/org/example/vista/Ventas.fxml"); }
    @FXML public void irAInventario()   { navegar("/org/example/vista/Inventario.fxml"); }
    @FXML public void irAEmpleados()    { navegar("/org/example/vista/Empleados.fxml"); }
    @FXML public void irAClientes()     { navegar("/org/example/vista/Clientes.fxml"); }
    @FXML public void irAReportes()     { navegar("/org/example/vista/Reportes.fxml"); }
    @FXML public void irACorteCaja()    { navegar("/org/example/vista/CorteCaja.fxml"); }
    @FXML public void irAConfiguracion(){ navegar("/org/example/vista/Configuracion.fxml"); }

    @FXML
    public void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Salir"); a.setHeaderText(null);
        a.setContentText("¿Seguro que deseas salir?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                registrarLogout(); // ← agrega esto
                Platform.exit();
            }
        });
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

    private void navegar(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) tablaAuditoria.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}