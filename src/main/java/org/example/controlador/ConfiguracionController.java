package org.example.controlador;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.modelo.SesionUsuario;

import java.io.IOException;
import java.util.prefs.Preferences;

public class ConfiguracionController {

    // ── Sidebar ──
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    // ── Pestañas (paneles) ──
    @FXML private VBox panelNegocio;
    @FXML private VBox panelPOS;
    @FXML private VBox panelUsuarios;
    @FXML private VBox panelFiscal;
    @FXML private VBox panelIntegraciones;
    @FXML private VBox panelApariencia;
    @FXML private VBox panelBaseDatos;

    // Botones de pestaña
    @FXML private Button btnTabNegocio;
    @FXML private Button btnTabPOS;
    @FXML private Button btnTabUsuarios;
    @FXML private Button btnTabFiscal;
    @FXML private Button btnTabIntegraciones;
    @FXML private Button btnTabApariencia;
    @FXML private Button btnTabBaseDatos;

    // ── Panel Negocio ──
    @FXML private TextField txtNombreNegocio;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtSitioWeb;
    @FXML private TextField txtRFC;
    @FXML private ComboBox<String> cmbMoneda;
    @FXML private ComboBox<String> cmbZonaHoraria;

    // ── Panel Fiscal ──
    @FXML private ToggleButton tglIVA;
    @FXML private TextField txtTasaIVA;
    @FXML private ComboBox<String> cmbPreciosIVA;
    @FXML private TextField txtRFCFiscal;
    @FXML private ToggleButton tglCFDI;
    @FXML private ToggleButton tglLogoTicket;
    @FXML private ToggleButton tglFolioTicket;
    @FXML private ToggleButton tglDesglose;
    @FXML private ToggleButton tglQR;
    @FXML private ComboBox<String> cmbImpresora;
    @FXML private ComboBox<String> cmbAnchoPapel;

    // ── Panel POS ──
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private ToggleButton tglDescuentos;
    @FXML private ToggleButton tglPropinas;
    @FXML private TextField txtMaxDescuento;

    private static final String STYLE_TAB_ACTIVE =
            "-fx-background-color: #6B4226; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand;";
    private static final String STYLE_TAB_INACTIVE =
            "-fx-background-color: white; -fx-text-fill: #6B4226; " +
                    "-fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand; " +
                    "-fx-border-color: #D4C5B0; -fx-border-radius: 20;";

    @FXML
    public void initialize() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String nombre = sesion.getNombre();
        lblAvatarIniciales.setText(
                nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase()
        );

        // Poblar ComboBoxes
        cmbMoneda.getItems().addAll("MXN - Peso Mexicano", "USD - Dólar", "EUR - Euro");
        cmbMoneda.setValue("MXN - Peso Mexicano");

        cmbZonaHoraria.getItems().addAll(
                "America/Monterrey (CST)", "America/Mexico_City (CST)",
                "America/Tijuana (PST)", "America/Cancun (EST)"
        );
        cmbZonaHoraria.setValue("America/Monterrey (CST)");

        cmbPreciosIVA.getItems().addAll(
                "Sí (precio ya incluye IVA)", "No (IVA se agrega al precio)"
        );
        cmbPreciosIVA.setValue("Sí (precio ya incluye IVA)");

        cmbImpresora.getItems().addAll("EPSON TM-T20III", "EPSON TM-T88V", "Star TSP100", "Genérica");
        cmbImpresora.setValue("EPSON TM-T20III");

        cmbAnchoPapel.getItems().addAll("58 mm", "80 mm");
        cmbAnchoPapel.setValue("80 mm");

        cmbMetodoPago.getItems().addAll("Efectivo", "Efectivo y Tarjeta", "Todos los métodos");
        cmbMetodoPago.setValue("Efectivo y Tarjeta");

        // Mostrar pestaña inicial
        mostrarTab("negocio");
        cargarPreferencias();
    }

    // ── Navegación entre pestañas ──
    @FXML private void tabNegocio()      { mostrarTab("negocio"); }
    @FXML private void tabPOS()          { mostrarTab("pos"); }
    @FXML private void tabUsuarios()     { mostrarTab("usuarios"); }
    @FXML private void tabFiscal()       { mostrarTab("fiscal"); }
    @FXML private void tabIntegraciones(){ mostrarTab("integraciones"); }
    @FXML private void tabApariencia()   { mostrarTab("apariencia"); }
    @FXML private void tabBaseDatos()    { mostrarTab("basedatos"); }

    private void mostrarTab(String tab) {
        // Ocultar todos
        panelNegocio.setVisible(false);     panelNegocio.setManaged(false);
        panelPOS.setVisible(false);         panelPOS.setManaged(false);
        panelUsuarios.setVisible(false);    panelUsuarios.setManaged(false);
        panelFiscal.setVisible(false);      panelFiscal.setManaged(false);
        panelIntegraciones.setVisible(false);panelIntegraciones.setManaged(false);
        panelApariencia.setVisible(false);  panelApariencia.setManaged(false);
        panelBaseDatos.setVisible(false);   panelBaseDatos.setManaged(false);

        // Resetear estilos de pestañas
        btnTabNegocio.setStyle(STYLE_TAB_INACTIVE);
        btnTabPOS.setStyle(STYLE_TAB_INACTIVE);
        btnTabUsuarios.setStyle(STYLE_TAB_INACTIVE);
        btnTabFiscal.setStyle(STYLE_TAB_INACTIVE);
        btnTabIntegraciones.setStyle(STYLE_TAB_INACTIVE);
        btnTabApariencia.setStyle(STYLE_TAB_INACTIVE);
        btnTabBaseDatos.setStyle(STYLE_TAB_INACTIVE);

        // Mostrar el panel activo y resaltar su pestaña
        switch (tab) {
            case "negocio"      -> { panelNegocio.setVisible(true);      panelNegocio.setManaged(true);      btnTabNegocio.setStyle(STYLE_TAB_ACTIVE); }
            case "pos"          -> { panelPOS.setVisible(true);          panelPOS.setManaged(true);          btnTabPOS.setStyle(STYLE_TAB_ACTIVE); }
            case "usuarios"     -> { panelUsuarios.setVisible(true);     panelUsuarios.setManaged(true);     btnTabUsuarios.setStyle(STYLE_TAB_ACTIVE); }
            case "fiscal"       -> { panelFiscal.setVisible(true);       panelFiscal.setManaged(true);       btnTabFiscal.setStyle(STYLE_TAB_ACTIVE); }
            case "integraciones"-> { panelIntegraciones.setVisible(true);panelIntegraciones.setManaged(true);btnTabIntegraciones.setStyle(STYLE_TAB_ACTIVE); }
            case "apariencia"   -> { panelApariencia.setVisible(true);   panelApariencia.setManaged(true);   btnTabApariencia.setStyle(STYLE_TAB_ACTIVE); }
            case "basedatos"    -> { panelBaseDatos.setVisible(true);    panelBaseDatos.setManaged(true);    btnTabBaseDatos.setStyle(STYLE_TAB_ACTIVE); }
        }
    }

    @FXML
    public void guardarConfiguracion() {
        Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);
        prefs.put("negocio_nombre",    txtNombreNegocio.getText().trim());
        prefs.put("negocio_telefono",  txtTelefono.getText().trim());
        prefs.put("negocio_direccion", txtDireccion.getText().trim());
        prefs.put("negocio_correo",    txtCorreo.getText().trim());
        prefs.put("negocio_web",       txtSitioWeb.getText().trim());
        prefs.put("negocio_rfc",       txtRFC.getText().trim());
        prefs.put("fiscal_iva",        txtTasaIVA.getText().trim());
        prefs.put("fiscal_rfc",        txtRFCFiscal.getText().trim());
        prefs.putBoolean("fiscal_aplica_iva",  tglIVA.isSelected());
        prefs.putBoolean("fiscal_cfdi",        tglCFDI.isSelected());
        prefs.putBoolean("ticket_logo",        tglLogoTicket.isSelected());
        prefs.putBoolean("ticket_folio",       tglFolioTicket.isSelected());
        prefs.putBoolean("ticket_desglose",    tglDesglose.isSelected());
        prefs.putBoolean("ticket_qr",          tglQR.isSelected());

        mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado", "Configuración guardada correctamente.");
    }

    @FXML
    public void restablecerConfiguracion() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Restablecer");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Restablecer todos los ajustes a los valores predeterminados?");
        alerta.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                txtNombreNegocio.clear(); txtTelefono.clear();
                txtDireccion.clear();     txtCorreo.clear();
                txtSitioWeb.clear();      txtRFC.clear();
                txtTasaIVA.setText("16"); txtRFCFiscal.clear();
                tglIVA.setSelected(true); tglCFDI.setSelected(false);
                tglLogoTicket.setSelected(true); tglFolioTicket.setSelected(true);
                tglDesglose.setSelected(true);   tglQR.setSelected(false);
                cmbMoneda.setValue("MXN - Peso Mexicano");
                cmbImpresora.setValue("EPSON TM-T20III");
                cmbAnchoPapel.setValue("80 mm");
            }
        });
    }

    private void cargarPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);
        txtNombreNegocio.setText(prefs.get("negocio_nombre", ""));
        txtTelefono.setText(prefs.get("negocio_telefono", ""));
        txtDireccion.setText(prefs.get("negocio_direccion", ""));
        txtCorreo.setText(prefs.get("negocio_correo", ""));
        txtSitioWeb.setText(prefs.get("negocio_web", ""));
        txtRFC.setText(prefs.get("negocio_rfc", ""));
        txtTasaIVA.setText(prefs.get("fiscal_iva", "16"));
        txtRFCFiscal.setText(prefs.get("fiscal_rfc", ""));
        tglIVA.setSelected(prefs.getBoolean("fiscal_aplica_iva", true));
        tglCFDI.setSelected(prefs.getBoolean("fiscal_cfdi", false));
        tglLogoTicket.setSelected(prefs.getBoolean("ticket_logo", true));
        tglFolioTicket.setSelected(prefs.getBoolean("ticket_folio", true));
        tglDesglose.setSelected(prefs.getBoolean("ticket_desglose", true));
        tglQR.setSelected(prefs.getBoolean("ticket_qr", false));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje);
        a.showAndWait();
    }

    // ── Navegación Sidebar ──
    @FXML private void irADashboard()  { navegar("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML private void irAVentas()     { navegar("/org/example/vista/Ventas.fxml"); }
    @FXML private void irAInventario() { navegar("/org/example/vista/Inventario.fxml"); }
    @FXML private void irAEmpleados()  { navegar("/org/example/vista/Empleados.fxml"); }
    @FXML private void irAReportes()   { navegar("/org/example/vista/Reportes.fxml"); }
    @FXML private void irACorteCaja()  { navegar("/org/example/vista/CorteCaja.fxml"); }


    @FXML
    private void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Cerrar Sesión"); a.setHeaderText(null);
        a.setContentText("¿Estás seguro de que deseas salir?");
        a.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) Platform.exit(); });
    }

    private void navegar(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) lblNombreUsuario.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}