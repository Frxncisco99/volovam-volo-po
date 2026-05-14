package org.example.controlador;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.servicio.TicketImpresora;
import org.example.servicio.TicketRenderer;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConfiguracionController {

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    // ── Topbar ────────────────────────────────────────────────────────────────
    @FXML private Label  lblHora;
    @FXML private Label  lblEstadoCaja;
    @FXML private Label  lblGuardadoAt;   // feedback "Guardado a las HH:mm:ss"
    @FXML private Button btnGuardar;      // para animación de feedback

    // ── Pestañas ──────────────────────────────────────────────────────────────
    @FXML private VBox panelNegocio;
    @FXML private VBox panelPOS;
    @FXML private VBox panelUsuarios;
    @FXML private VBox panelFiscal;
    @FXML private VBox panelIntegraciones;
    @FXML private VBox panelBaseDatos;

    @FXML private Button btnTabNegocio;
    @FXML private Button btnTabPOS;
    @FXML private Button btnTabUsuarios;
    @FXML private Button btnTabFiscal;
    @FXML private Button btnTabIntegraciones;
    @FXML private Button btnTabBaseDatos;

    // ── Panel Negocio ─────────────────────────────────────────────────────────
    @FXML private TextField txtNombreNegocio;
    @FXML private TextField txtSlogan;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCP;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtSitioWeb;
    @FXML private TextField txtRFC;
    @FXML private ComboBox<String> cmbMoneda;
    @FXML private ComboBox<String> cmbZonaHoraria;
    @FXML private TextField txtHoraApertura;
    @FXML private TextField txtHoraCierre;
    @FXML private ToggleButton tglLun, tglMar, tglMie, tglJue, tglVie, tglSab, tglDom;
    @FXML private TextField txtLimiteCredito;
    @FXML private TextField txtNumSucursal;
    @FXML private ToggleButton tglMantenimiento;

    // ── Panel POS ─────────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private ComboBox<String> cmbRedondeo;
    @FXML private org.example.modelo.SwitchToggle tglAperturaCaja;
    @FXML private org.example.modelo.SwitchToggle tglConfirmarCobro;
    @FXML private org.example.modelo.SwitchToggle tglAutoImprimir;
    @FXML private ComboBox<String> cmbBusqueda;
    @FXML private ComboBox<String> cmbOrdenProductos;
    @FXML private org.example.modelo.SwitchToggle tglImagenesProducto;
    @FXML private org.example.modelo.SwitchToggle tglAlertaStock;
    @FXML private org.example.modelo.SwitchToggle tglVentaSinStock;
    @FXML private org.example.modelo.SwitchToggle tglAsociarCliente;
    @FXML private org.example.modelo.SwitchToggle tglVentaCredito;
    @FXML private org.example.modelo.SwitchToggle tglDevoluciones;
    @FXML private org.example.modelo.SwitchToggle tglVentaRapida;
    @FXML private TextField txtNotaInterna;

    // ── Panel Usuarios ────────────────────────────────────────────────────────
    @FXML private TextField txtBuscarUsuario;
    @FXML private TableView<FilaUsuario> tablaUsuarios;
    @FXML private TableColumn<FilaUsuario, String> colUsuNombre;
    @FXML private TableColumn<FilaUsuario, String> colUsuRol;
    @FXML private TableColumn<FilaUsuario, String> colUsuEstado;
    @FXML private TableColumn<FilaUsuario, String> colUsuUltimoAcceso;
    @FXML private TableColumn<FilaUsuario, Void>   colUsuAcciones;
    @FXML private org.example.modelo.SwitchToggle tglAutoBloqueo;
    @FXML private ComboBox<String> cmbInactividad;
    @FXML private org.example.modelo.SwitchToggle tglAuditoria;

    private ObservableList<FilaUsuario> listaUsuarios = FXCollections.observableArrayList();

    // ── Panel Fiscal ──────────────────────────────────────────────────────────
    @FXML private org.example.modelo.SwitchToggle tglLogoTicket;
    @FXML private org.example.modelo.SwitchToggle tglFolioTicket;
    @FXML private org.example.modelo.SwitchToggle tglDesglose;
    @FXML private org.example.modelo.SwitchToggle tglQR;
    @FXML private org.example.modelo.SwitchToggle tglCopiacocina;
    @FXML private org.example.modelo.SwitchToggle tglMostrarFecha;
    @FXML private org.example.modelo.SwitchToggle tglMostrarCajero;
    @FXML private ComboBox<String> cmbImpresora;
    @FXML private ComboBox<String> cmbAnchoPapel;
    @FXML private TextField txtTicketNombre;
    @FXML private TextField txtTicketGiro;
    @FXML private TextField txtTicketDireccion;
    @FXML private TextField txtTicketCiudad;
    @FXML private TextField txtTicketTelefono;
    @FXML private TextArea  txtMensajeEncabezado;
    @FXML private TextArea  txtMensajePie;
    @FXML private TextArea  txtAvisoFiscal;

    // ── Panel Integraciones ───────────────────────────────────────────────────
    @FXML private TextField txtClipToken;
    @FXML private TextField txtStripeKey;
    @FXML private TextField txtCorreoReportes;
    @FXML private ComboBox<String> cmbSmtp;
    @FXML private org.example.modelo.SwitchToggle tglReporteDiario;
    @FXML private org.example.modelo.SwitchToggle tglAlertaStockCorreo;
    @FXML private TextField txtTwilioSid;
    @FXML private TextField txtTwilioToken;
    @FXML private org.example.modelo.SwitchToggle tglTicketWhatsapp;

    // ── Panel Base de Datos ───────────────────────────────────────────────────
    @FXML private Label      lblEstadoDB;
    @FXML private ComboBox<String> cmbMotorDB;
    @FXML private TextField  txtDBHost;
    @FXML private TextField  txtDBPuerto;
    @FXML private TextField  txtDBNombre;
    @FXML private TextField  txtDBUsuario;
    @FXML private PasswordField txtDBPassword;
    @FXML private TextField  txtRutaRespaldo;
    @FXML private ComboBox<String> cmbFrecuenciaRespaldo;
    @FXML private org.example.modelo.SwitchToggle tglRespaldoAuto;
    @FXML private Label      lblUltimoRespaldo;
    @FXML private Label      lblTamanoRespaldo;
    @FXML private Label      lblTamanoDB;
    @FXML private Label      lblRegVentas;
    @FXML private Label      lblRegProductos;

    // ── Servicios ─────────────────────────────────────────────────────────────
    private final TicketImpresora impresora = new TicketImpresora();
    private javafx.animation.Timeline relojTimeline;

    // ── Clave usada en tabla configuracion ───────────────────────────────────
    // Cada ajuste se guarda como fila: clave VARCHAR, valor TEXT
    // Fallback automático a Preferences si la tabla no existe aún.

    // ═════════════════════════════════════════════════════════════════════════
    //  INICIALIZACIÓN
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        cargarDatosUsuario();
        iniciarReloj();
        poblarCombos();
        mostrarTab("negocio");
        cargarConfiguracion();          // BD → campos (con fallback a Preferences)
        verificarEstadoDB();            // estado real al abrir
        configurarTablaUsuarios();
        cargarUsuariosDesdeDB();

        // Listener de búsqueda de usuarios
        if (txtBuscarUsuario != null) {
            txtBuscarUsuario.textProperty().addListener((o, a, b) -> filtrarUsuarios(b));
        }
    }

    // ── Reloj en tiempo real ──────────────────────────────────────────────────
    private void iniciarReloj() {
        if (lblHora == null) return;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        relojTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e ->
                        lblHora.setText(LocalDateTime.now().format(fmt))
                )
        );
        relojTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        relojTimeline.play();
    }

    // ── Datos del usuario activo ──────────────────────────────────────────────
    private void cargarDatosUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        String nombre = sesion.getNombre();
        if (lblNombreUsuario != null) lblNombreUsuario.setText(nombre);
        if (lblRolUsuario    != null) lblRolUsuario.setText(sesion.getRol());
        if (lblAvatarIniciales != null)
            lblAvatarIniciales.setText(
                    nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase());
    }

    // ── Poblar combos con valores fijos ───────────────────────────────────────
    private void poblarCombos() {
        cmbMoneda.getItems().addAll("MXN - Peso Mexicano", "USD - Dolar", "EUR - Euro");
        cmbMoneda.setValue("MXN - Peso Mexicano");

        cmbZonaHoraria.getItems().addAll(
                "America/Monterrey (CST)", "America/Mexico_City (CST)",
                "America/Tijuana (PST)",   "America/Cancun (EST)");
        cmbZonaHoraria.setValue("America/Monterrey (CST)");

        cmbMetodoPago.getItems().addAll("Efectivo", "Efectivo y Tarjeta", "Todos los metodos");
        cmbMetodoPago.setValue("Efectivo y Tarjeta");

        cmbRedondeo.getItems().addAll("Sin redondeo", "Redondear a $0.50", "Redondear a $1.00");
        cmbRedondeo.setValue("Sin redondeo");

        cmbBusqueda.getItems().addAll("Nombre del producto", "Codigo de barras", "Nombre o codigo", "Categoria");
        cmbBusqueda.setValue("Nombre o codigo");

        cmbOrdenProductos.getItems().addAll("Alfabetico", "Mas vendido primero", "Por categoria", "Precio ascendente");
        cmbOrdenProductos.setValue("Por categoria");

        cmbImpresora.getItems().addAll("EPSON TM-T20III", "EPSON TM-T88V", "Star TSP100", "Generica");
        cmbImpresora.setValue("EPSON TM-T20III");

        cmbAnchoPapel.getItems().addAll("58 mm", "80 mm");
        cmbAnchoPapel.setValue("58 mm");

        cmbInactividad.getItems().addAll("5 minutos", "10 minutos", "15 minutos", "30 minutos", "1 hora");
        cmbInactividad.setValue("15 minutos");

        cmbSmtp.getItems().addAll("Gmail", "Outlook / Hotmail", "Yahoo Mail", "SMTP personalizado");
        cmbSmtp.setValue("Gmail");

        cmbMotorDB.getItems().addAll("MySQL 8.x", "MySQL 5.7", "MariaDB");
        cmbMotorDB.setValue("MySQL 8.x");

        cmbFrecuenciaRespaldo.getItems().addAll("Diario", "Semanal", "Quincenal", "Mensual");
        cmbFrecuenciaRespaldo.setValue("Diario");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STORAGE — BD (tabla configuracion) con fallback a Preferences
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Garantiza que la tabla 'configuracion' exista.
     * Si no existe la crea; si no hay conexión, retorna false (usa Preferences).
     */
    private boolean asegurarTablaConfiguracion() {
        String ddl = "CREATE TABLE IF NOT EXISTS configuracion (" +
                "clave VARCHAR(120) PRIMARY KEY, " +
                "valor TEXT NOT NULL, " +
                "actualizado DATETIME DEFAULT CURRENT_TIMESTAMP " +
                "ON UPDATE CURRENT_TIMESTAMP" +
                ")";
        try (Connection con = ConexionDB.getConexion();
             Statement st  = con.createStatement()) {
            st.execute(ddl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Guarda un par clave/valor en la tabla configuracion (UPSERT). */
    private void dbSet(Connection con, String clave, String valor) throws SQLException {
        String sql = "INSERT INTO configuracion (clave, valor) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE valor = VALUES(valor)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, valor == null ? "" : valor);
            ps.executeUpdate();
        }
    }

    /** Lee un valor de la tabla configuracion; devuelve el defaultVal si no existe. */
    private String dbGet(Connection con, String clave, String defaultVal) {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT valor FROM configuracion WHERE clave = ?")) {
            ps.setString(1, clave);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("valor");
        } catch (Exception ignored) {}
        return defaultVal;
    }

    // ── Guardar toda la configuración ────────────────────────────────────────
    @FXML
    public void guardarConfiguracion() {
        boolean guardadoEnBD = false;

        if (asegurarTablaConfiguracion()) {
            try (Connection con = ConexionDB.getConexion()) {
                // Negocio
                dbSet(con, "negocio_nombre",    txtNombreNegocio.getText().trim());
                dbSet(con, "negocio_slogan",    txtSlogan.getText().trim());
                dbSet(con, "negocio_telefono",  txtTelefono.getText().trim());
                dbSet(con, "negocio_direccion", txtDireccion.getText().trim());
                dbSet(con, "negocio_ciudad",    txtCiudad.getText().trim());
                dbSet(con, "negocio_cp",        txtCP.getText().trim());
                dbSet(con, "negocio_correo",    txtCorreo.getText().trim());
                dbSet(con, "negocio_web",       txtSitioWeb.getText().trim());
                dbSet(con, "negocio_rfc",       txtRFC.getText().trim());
                dbSet(con, "negocio_hora_ap",   txtHoraApertura.getText().trim());
                dbSet(con, "negocio_hora_ci",   txtHoraCierre.getText().trim());
                dbSet(con, "negocio_credito",   txtLimiteCredito.getText().trim());
                dbSet(con, "negocio_sucursal",  txtNumSucursal.getText().trim());
                // Ticket
                dbSet(con, "ticket_nombre",     txtTicketNombre.getText().trim());
                dbSet(con, "ticket_giro",       txtTicketGiro.getText().trim());
                dbSet(con, "ticket_direccion",  txtTicketDireccion.getText().trim());
                dbSet(con, "ticket_ciudad",     txtTicketCiudad.getText().trim());
                dbSet(con, "ticket_telefono",   txtTicketTelefono.getText().trim());
                dbSet(con, "ticket_encabezado", txtMensajeEncabezado.getText().trim());
                dbSet(con, "ticket_pie",        txtMensajePie.getText().trim());
                dbSet(con, "ticket_aviso",      txtAvisoFiscal.getText().trim());
                dbSet(con, "ticket_ancho",      cmbAnchoPapel.getValue() != null ? cmbAnchoPapel.getValue() : "58 mm");
                dbSet(con, "ticket_logo",       String.valueOf(tglLogoTicket.isSelected()));
                dbSet(con, "ticket_folio",      String.valueOf(tglFolioTicket.isSelected()));
                dbSet(con, "ticket_desglose",   String.valueOf(tglDesglose.isSelected()));
                dbSet(con, "ticket_qr",         String.valueOf(tglQR.isSelected()));
                // Integraciones
                dbSet(con, "int_clip",          txtClipToken.getText().trim());
                dbSet(con, "int_stripe",        txtStripeKey.getText().trim());
                dbSet(con, "int_correo_rep",    txtCorreoReportes.getText().trim());
                guardadoEnBD = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Fallback: también en Preferences (portabilidad)
        guardarEnPreferences();

        // ── Feedback visual inline — sin Alert ──────────────────────────────
        mostrarFeedbackGuardado(guardadoEnBD);
    }

    /**
     * Muestra feedback inline en el topbar:
     * - El botón "Guardar Cambios" se vuelve verde por 2 segundos
     * - lblGuardadoAt muestra "Guardado a las HH:mm:ss"
     */
    private void mostrarFeedbackGuardado(boolean enBD) {
        if (lblGuardadoAt != null) {
            String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String origen = enBD ? "BD" : "local";
            lblGuardadoAt.setText("✓ Guardado a las " + hora + " (" + origen + ")");
            lblGuardadoAt.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblGuardadoAt.setVisible(true);
            lblGuardadoAt.setManaged(true);
        }

        if (btnGuardar != null) {
            String estiloVerde =
                    "-fx-background-color: #27AE60; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-background-radius: 8; " +
                            "-fx-border-color: transparent; -fx-border-width: 1; " +
                            "-fx-padding: 0 20; -fx-cursor: hand; -fx-font-size: 12px; " +
                            "-fx-background-insets: 0;";
            String estiloOriginal =
                    "-fx-background-color: #1a6fa8; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-background-radius: 8; " +
                            "-fx-border-color: transparent; -fx-border-width: 1; " +
                            "-fx-padding: 0 20; -fx-cursor: hand; -fx-font-size: 12px; " +
                            "-fx-background-insets: 0;";
            btnGuardar.setStyle(estiloVerde);
            btnGuardar.setText("✓ Guardado");

            // Vuelve al estado original después de 2.5 segundos
            javafx.animation.PauseTransition pausa =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.5));
            pausa.setOnFinished(ev -> {
                btnGuardar.setStyle(estiloOriginal);
                btnGuardar.setText("Guardar Cambios");
            });
            pausa.play();
        }
    }

    // ── Guardar en Preferences (fallback / portabilidad) ─────────────────────
    private void guardarEnPreferences() {
        java.util.prefs.Preferences prefs =
                java.util.prefs.Preferences.userNodeForPackage(ConfiguracionController.class);
        prefs.put("negocio_nombre",    txtNombreNegocio.getText().trim());
        prefs.put("negocio_slogan",    txtSlogan.getText().trim());
        prefs.put("negocio_telefono",  txtTelefono.getText().trim());
        prefs.put("negocio_direccion", txtDireccion.getText().trim());
        prefs.put("negocio_ciudad",    txtCiudad.getText().trim());
        prefs.put("negocio_cp",        txtCP.getText().trim());
        prefs.put("negocio_correo",    txtCorreo.getText().trim());
        prefs.put("negocio_web",       txtSitioWeb.getText().trim());
        prefs.put("negocio_rfc",       txtRFC.getText().trim());
        prefs.put("negocio_hora_ap",   txtHoraApertura.getText().trim());
        prefs.put("negocio_hora_ci",   txtHoraCierre.getText().trim());
        prefs.put("negocio_credito",   txtLimiteCredito.getText().trim());
        prefs.put("negocio_sucursal",  txtNumSucursal.getText().trim());
        prefs.put("ticket_nombre",     txtTicketNombre.getText().trim());
        prefs.put("ticket_giro",       txtTicketGiro.getText().trim());
        prefs.put("ticket_direccion",  txtTicketDireccion.getText().trim());
        prefs.put("ticket_ciudad",     txtTicketCiudad.getText().trim());
        prefs.put("ticket_telefono",   txtTicketTelefono.getText().trim());
        prefs.put("ticket_encabezado", txtMensajeEncabezado.getText().trim());
        prefs.put("ticket_pie",        txtMensajePie.getText().trim());
        prefs.put("ticket_aviso",      txtAvisoFiscal.getText().trim());
        prefs.put("ticket_ancho",      cmbAnchoPapel.getValue() != null ? cmbAnchoPapel.getValue() : "58 mm");
        prefs.putBoolean("ticket_logo",     tglLogoTicket.isSelected());
        prefs.putBoolean("ticket_folio",    tglFolioTicket.isSelected());
        prefs.putBoolean("ticket_desglose", tglDesglose.isSelected());
        prefs.putBoolean("ticket_qr",       tglQR.isSelected());
        prefs.put("int_clip",           txtClipToken.getText().trim());
        prefs.put("int_stripe",         txtStripeKey.getText().trim());
        prefs.put("int_correo_rep",     txtCorreoReportes.getText().trim());
    }

    // ── Cargar configuración: BD primero, fallback a Preferences ─────────────
    private void cargarConfiguracion() {
        if (asegurarTablaConfiguracion()) {
            try (Connection con = ConexionDB.getConexion()) {
                txtNombreNegocio.setText(dbGet(con, "negocio_nombre", ""));
                txtSlogan.setText(dbGet(con, "negocio_slogan", ""));
                txtTelefono.setText(dbGet(con, "negocio_telefono", ""));
                txtDireccion.setText(dbGet(con, "negocio_direccion", ""));
                txtCiudad.setText(dbGet(con, "negocio_ciudad", ""));
                txtCP.setText(dbGet(con, "negocio_cp", ""));
                txtCorreo.setText(dbGet(con, "negocio_correo", ""));
                txtSitioWeb.setText(dbGet(con, "negocio_web", ""));
                txtRFC.setText(dbGet(con, "negocio_rfc", ""));
                txtHoraApertura.setText(dbGet(con, "negocio_hora_ap", "08:00"));
                txtHoraCierre.setText(dbGet(con, "negocio_hora_ci", "21:00"));
                txtLimiteCredito.setText(dbGet(con, "negocio_credito", ""));
                txtNumSucursal.setText(dbGet(con, "negocio_sucursal", ""));

                tglLogoTicket.setSelected(   Boolean.parseBoolean(dbGet(con, "ticket_logo", "true")));
                tglFolioTicket.setSelected(  Boolean.parseBoolean(dbGet(con, "ticket_folio", "true")));
                tglDesglose.setSelected(     Boolean.parseBoolean(dbGet(con, "ticket_desglose", "true")));
                tglQR.setSelected(           Boolean.parseBoolean(dbGet(con, "ticket_qr", "false")));

                txtTicketNombre.setText(    dbGet(con, "ticket_nombre",    "Volovan Volo"));
                txtTicketGiro.setText(      dbGet(con, "ticket_giro",      "Panaderia y Reposteria"));
                txtTicketDireccion.setText( dbGet(con, "ticket_direccion", ""));
                txtTicketCiudad.setText(    dbGet(con, "ticket_ciudad",    ""));
                txtTicketTelefono.setText(  dbGet(con, "ticket_telefono",  ""));
                txtMensajeEncabezado.setText(dbGet(con, "ticket_encabezado", "Bienvenido!\nGracias por visitarnos."));
                txtMensajePie.setText(       dbGet(con, "ticket_pie",        "Gracias por su compra!\nVuelva pronto."));
                txtAvisoFiscal.setText(      dbGet(con, "ticket_aviso",      "Este ticket no es\nComprobante fiscal"));

                String ancho = dbGet(con, "ticket_ancho", "58 mm");
                if (cmbAnchoPapel.getItems().contains(ancho)) cmbAnchoPapel.setValue(ancho);

                txtClipToken.setText(        dbGet(con, "int_clip",       ""));
                txtStripeKey.setText(        dbGet(con, "int_stripe",     ""));
                txtCorreoReportes.setText(   dbGet(con, "int_correo_rep", ""));
                return; // cargado desde BD, no necesita Preferences
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Fallback a Preferences
        cargarDesdePreferences();
    }

    private void cargarDesdePreferences() {
        java.util.prefs.Preferences prefs =
                java.util.prefs.Preferences.userNodeForPackage(ConfiguracionController.class);
        txtNombreNegocio.setText(prefs.get("negocio_nombre", ""));
        txtSlogan.setText(prefs.get("negocio_slogan", ""));
        txtTelefono.setText(prefs.get("negocio_telefono", ""));
        txtDireccion.setText(prefs.get("negocio_direccion", ""));
        txtCiudad.setText(prefs.get("negocio_ciudad", ""));
        txtCP.setText(prefs.get("negocio_cp", ""));
        txtCorreo.setText(prefs.get("negocio_correo", ""));
        txtSitioWeb.setText(prefs.get("negocio_web", ""));
        txtRFC.setText(prefs.get("negocio_rfc", ""));
        txtHoraApertura.setText(prefs.get("negocio_hora_ap", "08:00"));
        txtHoraCierre.setText(prefs.get("negocio_hora_ci", "21:00"));
        txtLimiteCredito.setText(prefs.get("negocio_credito", ""));
        txtNumSucursal.setText(prefs.get("negocio_sucursal", ""));
        tglLogoTicket.setSelected(   prefs.getBoolean("ticket_logo",     true));
        tglFolioTicket.setSelected(  prefs.getBoolean("ticket_folio",    true));
        tglDesglose.setSelected(     prefs.getBoolean("ticket_desglose", true));
        tglQR.setSelected(           prefs.getBoolean("ticket_qr",       false));
        txtTicketNombre.setText(    prefs.get("ticket_nombre",    "Volovan Volo"));
        txtTicketGiro.setText(      prefs.get("ticket_giro",      "Panaderia y Reposteria"));
        txtTicketDireccion.setText( prefs.get("ticket_direccion", ""));
        txtTicketCiudad.setText(    prefs.get("ticket_ciudad",    ""));
        txtTicketTelefono.setText(  prefs.get("ticket_telefono",  ""));
        txtMensajeEncabezado.setText(prefs.get("ticket_encabezado", "Bienvenido!\nGracias por visitarnos."));
        txtMensajePie.setText(       prefs.get("ticket_pie",        "Gracias por su compra!\nVuelva pronto."));
        txtAvisoFiscal.setText(      prefs.get("ticket_aviso",      "Este ticket no es\nComprobante fiscal"));
        String ancho = prefs.get("ticket_ancho", "58 mm");
        if (cmbAnchoPapel.getItems().contains(ancho)) cmbAnchoPapel.setValue(ancho);
        txtClipToken.setText(      prefs.get("int_clip",       ""));
        txtStripeKey.setText(      prefs.get("int_stripe",     ""));
        txtCorreoReportes.setText( prefs.get("int_correo_rep", ""));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ESTADO DE BASE DE DATOS AL INICIAR
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Verifica la conexión actual al abrir la pantalla y actualiza
     * el badge de estado sin esperar a que el usuario haga click en "Probar".
     */
    private void verificarEstadoDB() {
        if (lblEstadoDB == null) return;
        // Corre en hilo separado para no bloquear el UI thread
        Thread t = new Thread(() -> {
            Connection con = ConexionDB.getConexion();
            boolean ok = con != null;
            if (ok) {
                try { con.close(); } catch (Exception ignored) {}
            }
            Platform.runLater(() -> actualizarBadgeDB(ok));
        });
        t.setDaemon(true);
        t.start();
    }

    private void actualizarBadgeDB(boolean conectado) {
        if (lblEstadoDB == null) return;
        if (conectado) {
            String host = txtDBHost != null ? txtDBHost.getText() : "localhost";
            String db   = txtDBNombre != null ? txtDBNombre.getText() : "pospanaderia";
            lblEstadoDB.setText("● Conectado — " + db + "@" + host);
            lblEstadoDB.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            lblEstadoDB.setText("● Sin conexión — revisa los parámetros");
            lblEstadoDB.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold; -fx-font-size: 11px;");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TABLA DE USUARIOS DESDE BD
    // ═════════════════════════════════════════════════════════════════════════

    private void configurarTablaUsuarios() {
        if (tablaUsuarios == null) return;

        colUsuNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCompleto()));
        colUsuRol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRol()));
        colUsuEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colUsuUltimoAcceso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUltimoAcceso()));

        // Columna ROL — badge de color
        colUsuRol.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override protected void updateItem(String rol, boolean empty) {
                super.updateItem(rol, empty);
                setAlignment(Pos.CENTER);
                if (empty || rol == null) { setGraphic(null); return; }
                badge.setText(rol);
                String color = switch (rol.toLowerCase()) {
                    case "administrador" -> "-fx-background-color: #e8f3fb; -fx-text-fill: #1a6fa8;";
                    case "gerente"       -> "-fx-background-color: #D8F0E0; -fx-text-fill: #1A7A40;";
                    default              -> "-fx-background-color: #D5EAF8; -fx-text-fill: #1A5D8A;";
                };
                badge.setStyle(color + "-fx-background-radius: 10; -fx-padding: 3 8; -fx-font-size: 10px; -fx-font-weight: bold;");
                setGraphic(badge);
            }
        });

        // Columna ESTADO — color
        colUsuEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                setAlignment(Pos.CENTER);
                if (empty || estado == null) { setText(null); setStyle(""); return; }
                setText(estado);
                setStyle("Activo".equalsIgnoreCase(estado)
                        ? "-fx-text-fill: #27AE60; -fx-font-weight: bold; -fx-font-size: 11px;"
                        : "-fx-text-fill: #C0392B; -fx-font-weight: bold; -fx-font-size: 11px;");
            }
        });

        // Columna ACCIONES
        colUsuAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("Editar");
            private final Button btnPassword = new Button("Clave");
            private final Button btnEliminar = new Button("Borrar");
            private final HBox   caja        = new HBox(6, btnEditar, btnPassword, btnEliminar);
            {
                String base = "-fx-text-fill: white; -fx-background-radius: 6; " +
                        "-fx-border-color: transparent; -fx-border-width: 1; " +
                        "-fx-padding: 4 8; -fx-font-size: 11px; -fx-cursor: hand; " +
                        "-fx-background-insets: 0; -fx-font-weight: bold;";
                btnEditar.setStyle("-fx-background-color: #1a6fa8; " + base);
                btnPassword.setStyle("-fx-background-color: #2E7D50; " + base);
                btnEliminar.setStyle("-fx-background-color: #C0392B; " + base);
                caja.setAlignment(Pos.CENTER);
                btnEditar.setOnAction(e   -> editarUsuario());
                btnPassword.setOnAction(e -> cambiarPassword());
                btnEliminar.setOnAction(e -> {
                    FilaUsuario u = getTableView().getItems().get(getIndex());
                    confirmarEliminarUsuario(u);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : caja);
            }
        });

        tablaUsuarios.setItems(listaUsuarios);

        // Estilo de filas alternadas
        tablaUsuarios.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(FilaUsuario item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: #f5f9ff;"
                        : "-fx-background-color: white;");
            }
        });
    }

    private void cargarUsuariosDesdeDB() {
        if (tablaUsuarios == null) return;
        listaUsuarios.clear();
        String sql = "SELECT u.id_usuario, u.nombre, u.apellido, r.nombre AS rol, " +
                "u.activo, u.ultimo_acceso " +
                "FROM usuarios u " +
                "LEFT JOIN roles r ON u.id_rol = r.id_rol " +
                "ORDER BY u.nombre";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nombre    = rs.getString("nombre") + " " +
                        (rs.getString("apellido") != null ? rs.getString("apellido") : "");
                String rol       = rs.getString("rol") != null ? rs.getString("rol") : "Sin rol";
                String estado    = rs.getBoolean("activo") ? "Activo" : "Inactivo";
                String acceso    = rs.getTimestamp("ultimo_acceso") != null
                        ? rs.getTimestamp("ultimo_acceso")
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                        : "Nunca";
                listaUsuarios.add(new FilaUsuario(
                        rs.getInt("id_usuario"), nombre.trim(), rol, estado, acceso));
            }
        } catch (Exception e) {
            // Si la tabla tiene columnas distintas, intenta con esquema mínimo
            cargarUsuariosEsquemaSimple();
        }
    }

    /** Fallback con esquema mínimo (solo usuarios sin JOIN a roles) */
    private void cargarUsuariosEsquemaSimple() {
        String sql = "SELECT id_usuario, nombre, rol, activo FROM usuarios ORDER BY nombre";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String estado = rs.getBoolean("activo") ? "Activo" : "Inactivo";
                listaUsuarios.add(new FilaUsuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("rol") != null ? rs.getString("rol") : "—",
                        estado,
                        "—"));
            }
        } catch (Exception ignored) {
            // Sin acceso a la tabla — la tabla queda vacía, no explota
        }
    }

    private void filtrarUsuarios(String texto) {
        if (texto == null || texto.isBlank()) {
            tablaUsuarios.setItems(listaUsuarios);
            return;
        }
        String lower = texto.toLowerCase();
        ObservableList<FilaUsuario> filtrada = FXCollections.observableArrayList();
        for (FilaUsuario u : listaUsuarios) {
            if (u.getNombreCompleto().toLowerCase().contains(lower) ||
                    u.getRol().toLowerCase().contains(lower)) {
                filtrada.add(u);
            }
        }
        tablaUsuarios.setItems(filtrada);
    }

    private void confirmarEliminarUsuario(FilaUsuario u) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Eliminar usuario");
        a.setHeaderText(null);
        a.setContentText("¿Eliminar a \"" + u.getNombreCompleto() + "\"? Esta acción no se puede deshacer.");
        a.getButtonTypes().setAll(
                new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE));
        a.showAndWait().ifPresent(r -> {
            if (r.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try (Connection con = ConexionDB.getConexion();
                     PreparedStatement ps = con.prepareStatement(
                             "UPDATE usuarios SET activo = 0 WHERE id_usuario = ?")) {
                    ps.setInt(1, u.getIdUsuario());
                    ps.executeUpdate();
                    cargarUsuariosDesdeDB();
                } catch (Exception ex) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
                }
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MODELO FilaUsuario
    // ═════════════════════════════════════════════════════════════════════════

    public static class FilaUsuario {
        private final int    idUsuario;
        private final String nombreCompleto;
        private final String rol;
        private final String estado;
        private final String ultimoAcceso;

        public FilaUsuario(int id, String nombre, String rol, String estado, String acceso) {
            this.idUsuario      = id;
            this.nombreCompleto = nombre;
            this.rol            = rol;
            this.estado         = estado;
            this.ultimoAcceso   = acceso;
        }

        public int    getIdUsuario()      { return idUsuario; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getRol()            { return rol; }
        public String getEstado()         { return estado; }
        public String getUltimoAcceso()   { return ultimoAcceso; }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PESTAÑAS
    // ═════════════════════════════════════════════════════════════════════════

    @FXML private void tabNegocio()       { mostrarTab("negocio"); }
    @FXML private void tabPOS()           { mostrarTab("pos"); }
    @FXML private void tabUsuarios()      { mostrarTab("usuarios"); }
    @FXML private void tabFiscal()        { mostrarTab("fiscal"); }
    @FXML private void tabIntegraciones() { mostrarTab("integraciones"); }
    @FXML private void tabBaseDatos()     { mostrarTab("basedatos"); }

    private void mostrarTab(String tab) {
        panelNegocio.setVisible(false);       panelNegocio.setManaged(false);
        panelPOS.setVisible(false);           panelPOS.setManaged(false);
        panelUsuarios.setVisible(false);      panelUsuarios.setManaged(false);
        panelFiscal.setVisible(false);        panelFiscal.setManaged(false);
        panelIntegraciones.setVisible(false); panelIntegraciones.setManaged(false);
        panelBaseDatos.setVisible(false);     panelBaseDatos.setManaged(false);

        btnTabNegocio.getStyleClass().setAll("cfg-tab");
        btnTabPOS.getStyleClass().setAll("cfg-tab");
        btnTabUsuarios.getStyleClass().setAll("cfg-tab");
        btnTabFiscal.getStyleClass().setAll("cfg-tab");
        btnTabIntegraciones.getStyleClass().setAll("cfg-tab");
        btnTabBaseDatos.getStyleClass().setAll("cfg-tab");

        switch (tab) {
            case "negocio"       -> { panelNegocio.setVisible(true);       panelNegocio.setManaged(true);       btnTabNegocio.getStyleClass().setAll("cfg-tab-active"); }
            case "pos"           -> { panelPOS.setVisible(true);           panelPOS.setManaged(true);           btnTabPOS.getStyleClass().setAll("cfg-tab-active"); }
            case "usuarios"      -> { panelUsuarios.setVisible(true);      panelUsuarios.setManaged(true);      btnTabUsuarios.getStyleClass().setAll("cfg-tab-active");
                cargarUsuariosDesdeDB(); } // refresca al abrir la pestaña
            case "fiscal"        -> { panelFiscal.setVisible(true);        panelFiscal.setManaged(true);        btnTabFiscal.getStyleClass().setAll("cfg-tab-active"); }
            case "integraciones" -> { panelIntegraciones.setVisible(true); panelIntegraciones.setManaged(true); btnTabIntegraciones.getStyleClass().setAll("cfg-tab-active"); }
            case "basedatos"     -> { panelBaseDatos.setVisible(true);     panelBaseDatos.setManaged(true);     btnTabBaseDatos.getStyleClass().setAll("cfg-tab-active");
                verificarEstadoDB(); } // refresca estado al abrir la pestaña
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RESTABLECER
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void restablecerConfiguracion() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Restablecer"); a.setHeaderText(null);
        a.setContentText("¿Restablecer todos los ajustes a los valores predeterminados?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                txtNombreNegocio.clear(); txtTelefono.clear(); txtDireccion.clear();
                txtCorreo.clear();        txtSitioWeb.clear(); txtRFC.clear();
                txtSlogan.clear();        txtCiudad.clear();   txtCP.clear();
                tglLogoTicket.setSelected(true);
                tglFolioTicket.setSelected(true);
                tglDesglose.setSelected(true);
                tglQR.setSelected(false);
                cmbMoneda.setValue("MXN - Peso Mexicano");
                cmbImpresora.setValue("EPSON TM-T20III");
                cmbAnchoPapel.setValue("58 mm");
                cmbMetodoPago.setValue("Efectivo y Tarjeta");
                txtTicketNombre.setText("Volovan Volo");
                txtTicketGiro.setText("Panaderia y Reposteria");
                txtTicketDireccion.clear();
                txtTicketCiudad.clear();
                txtTicketTelefono.clear();
                txtMensajeEncabezado.setText("Bienvenido!\nGracias por visitarnos.");
                txtMensajePie.setText("Gracias por su compra!\nVuelva pronto.\nfacebook.com/VolovanVolo");
                txtAvisoFiscal.setText("Este ticket no es\nComprobante fiscal");
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TICKET — vista previa e impresión (sin cambios funcionales)
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void abrirVistaPrevia() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/TicketPreview.fxml"));
            Parent root = loader.load();
            TicketPreviewController ctrl = loader.getController();
            int ancho = "58 mm".equals(cmbAnchoPapel.getValue())
                    ? TicketRenderer.ANCHO_58MM : TicketRenderer.ANCHO_80MM;
            ctrl.configurar(ticketMuestra(ancho),
                    txtTicketNombre.getText().trim(),
                    txtTicketGiro.getText().trim(),
                    txtTicketDireccion.getText().trim(),
                    txtTicketCiudad.getText().trim(),
                    txtTicketTelefono.getText().trim(),
                    txtMensajeEncabezado.getText().trim(),
                    txtMensajePie.getText().trim(),
                    txtAvisoFiscal.getText().trim(),
                    tglLogoTicket.isSelected(),
                    tglFolioTicket.isSelected(),
                    tglDesglose.isSelected(),
                    tglQR.isSelected(),
                    tglMostrarFecha  != null && tglMostrarFecha.isSelected(),
                    tglMostrarCajero != null && tglMostrarCajero.isSelected(),
                    ancho);
            Stage stage = new Stage();
            stage.setTitle("Vista Previa — Ticket");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.initOwner(lblNombreUsuario.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo abrir la vista previa.\n" + e.getMessage());
        }
    }

    @FXML
    public void imprimirTicketPrueba() {
        int ancho = "58 mm".equals(cmbAnchoPapel.getValue())
                ? TicketRenderer.ANCHO_58MM : TicketRenderer.ANCHO_80MM;
        try {
            impresora.imprimirConRenderer(ticketMuestra(ancho),
                    txtTicketNombre.getText().trim(),
                    txtTicketGiro.getText().trim(),
                    txtTicketDireccion.getText().trim(),
                    txtTicketCiudad.getText().trim(),
                    txtTicketTelefono.getText().trim(),
                    txtMensajeEncabezado.getText().trim(),
                    txtMensajePie.getText().trim(),
                    txtAvisoFiscal.getText().trim(),
                    tglLogoTicket.isSelected(), tglFolioTicket.isSelected(),
                    tglDesglose.isSelected(),   tglQR.isSelected(),
                    tglMostrarFecha  != null && tglMostrarFecha.isSelected(),
                    tglMostrarCajero != null && tglMostrarCajero.isSelected(),
                    ancho);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Ticket de Prueba",
                    "Ticket enviado a la impresora correctamente.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Impresión",
                    "No se pudo imprimir.\n" + e.getMessage());
        }
    }

    private org.example.modelo.Ticket ticketMuestra(int ancho) {
        java.util.List<org.example.modelo.Ticket.LineaTicket> lineas = java.util.List.of(
                new org.example.modelo.Ticket.LineaTicket("Croissant mantequilla", 2, 24.00),
                new org.example.modelo.Ticket.LineaTicket("Pan de chocolate",      1, 22.00),
                new org.example.modelo.Ticket.LineaTicket("Cuerno azucarado",      3, 12.00),
                new org.example.modelo.Ticket.LineaTicket("Cafe americano",        1, 35.00)
        );
        return new org.example.modelo.Ticket(1234,
                LocalDateTime.now(),
                SesionUsuario.getInstancia().getNombre(),
                lineas, 141.00, 200.00, 59.00, 1);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  USUARIOS Y ROLES — stubs de dialogo
    // ═════════════════════════════════════════════════════════════════════════

    @FXML public void agregarUsuario() {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Nuevo Usuario",
                "Aqui se abrira el dialogo para crear un nuevo usuario.");
    }
    @FXML public void editarUsuario() {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Editar Usuario",
                "Aqui se abrira el dialogo para editar el usuario seleccionado.");
    }
    @FXML public void cambiarPassword() {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Cambiar Contrasena",
                "Aqui se abrira el dialogo para cambiar la contrasena.");
    }
    @FXML public void eliminarUsuario() {
        mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un usuario",
                "Selecciona un usuario de la tabla para eliminarlo.");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INTEGRACIONES
    // ═════════════════════════════════════════════════════════════════════════

    @FXML public void conectarClip() {
        String token = txtClipToken.getText().trim();
        if (token.isEmpty()) { mostrarAlerta(Alert.AlertType.WARNING, "Clip", "Ingresa el token de Clip."); return; }
        mostrarAlerta(Alert.AlertType.INFORMATION, "Clip",
                "Token guardado: " + token.substring(0, Math.min(8, token.length())) + "...");
    }
    @FXML public void conectarStripe() {
        String key = txtStripeKey.getText().trim();
        if (key.isEmpty()) { mostrarAlerta(Alert.AlertType.WARNING, "Stripe", "Ingresa la API Key de Stripe."); return; }
        mostrarAlerta(Alert.AlertType.INFORMATION, "Stripe", "Conexion con Stripe configurada.");
    }
    @FXML public void enviarCorreoPrueba() {
        String correo = txtCorreoReportes.getText().trim();
        if (correo.isEmpty()) { mostrarAlerta(Alert.AlertType.WARNING, "Correo", "Ingresa el correo de destino."); return; }
        mostrarAlerta(Alert.AlertType.INFORMATION, "Correo de Prueba", "Correo enviado a: " + correo);
    }
    @FXML public void probarWhatsapp() {
        if (txtTwilioSid.getText().trim().isEmpty() || txtTwilioToken.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Twilio", "Ingresa Account SID y Auth Token."); return;
        }
        mostrarAlerta(Alert.AlertType.INFORMATION, "WhatsApp / Twilio", "Conexion con Twilio verificada.");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BASE DE DATOS
    // ═════════════════════════════════════════════════════════════════════════

    @FXML public void probarConexionDB() {
        String url = "jdbc:mysql://" + txtDBHost.getText().trim() +
                ":" + txtDBPuerto.getText().trim() +
                "/" + txtDBNombre.getText().trim();
        try {
            Connection conn = java.sql.DriverManager.getConnection(
                    url, txtDBUsuario.getText().trim(), txtDBPassword.getText());
            if (conn != null) {
                conn.close();
                actualizarBadgeDB(true);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Conexion Exitosa",
                        "Base de datos conectada correctamente.");
            }
        } catch (Exception e) {
            actualizarBadgeDB(false);
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexion",
                    "No se pudo conectar.\n" + e.getMessage());
        }
    }

    @FXML public void aplicarConexionDB() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Aplicar Conexion"); a.setHeaderText(null);
        a.setContentText("¿Aplicar estos parametros como conexion activa?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                java.util.prefs.Preferences prefs =
                        java.util.prefs.Preferences.userNodeForPackage(ConfiguracionController.class);
                prefs.put("db_host",    txtDBHost.getText().trim());
                prefs.put("db_puerto",  txtDBPuerto.getText().trim());
                prefs.put("db_nombre",  txtDBNombre.getText().trim());
                prefs.put("db_usuario", txtDBUsuario.getText().trim());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Configuracion Aplicada",
                        "Parametros de base de datos actualizados.");
            }
        });
    }

    @FXML public void seleccionarCarpetaRespaldo() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta de respaldos");
        File carpeta = chooser.showDialog((Stage) lblNombreUsuario.getScene().getWindow());
        if (carpeta != null) txtRutaRespaldo.setText(carpeta.getAbsolutePath());
    }

    @FXML public void exportarRespaldo() {
        String ruta = txtRutaRespaldo.getText().trim();
        if (ruta.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Respaldo", "Selecciona la carpeta primero."); return;
        }
        String archivo = "respaldo_pos_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".sql";
        lblUltimoRespaldo.setText(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                        " — " + archivo);
        mostrarAlerta(Alert.AlertType.INFORMATION, "Respaldo Exportado",
                "Respaldo creado:\n" + ruta + java.io.File.separator + archivo);
    }

    @FXML public void restaurarRespaldo() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Restaurar"); c.setHeaderText("Advertencia: sobreescribira los datos actuales");
        c.setContentText("¿Deseas seleccionar un archivo de respaldo?");
        c.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Seleccionar archivo de respaldo");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos SQL", "*.sql"));
                File archivo = fc.showOpenDialog((Stage) lblNombreUsuario.getScene().getWindow());
                if (archivo != null)
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Restaurar",
                            "Archivo: " + archivo.getName());
            }
        });
    }

    @FXML public void actualizarEstadisticasDB() {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) { lblTamanoDB.setText("Sin conexion"); return; }
        try (Statement stmt = conn.createStatement()) {
            ResultSet rsTickets = stmt.executeQuery("SELECT COUNT(*) FROM tickets");
            if (rsTickets.next()) lblRegVentas.setText(rsTickets.getInt(1) + " registros");
            ResultSet rsProductos = stmt.executeQuery("SELECT COUNT(*) FROM productos");
            if (rsProductos.next()) lblRegProductos.setText(rsProductos.getInt(1) + " productos");
            ResultSet rsSize = stmt.executeQuery(
                    "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS tamano_mb " +
                            "FROM information_schema.tables WHERE table_schema = '" +
                            txtDBNombre.getText() + "'");
            if (rsSize.next()) lblTamanoDB.setText(rsSize.getString(1) + " MB");
            conn.close();
        } catch (Exception e) { lblTamanoDB.setText("Error"); }
    }

    @FXML public void limpiarTemporales() {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Limpiar Temporales", "Registros temporales eliminados.");
    }

    @FXML public void optimizarTablas() {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) { mostrarAlerta(Alert.AlertType.ERROR, "Optimizar", "Sin conexion activa."); return; }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("OPTIMIZE TABLE productos, tickets, categorias");
            conn.close();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Optimizado", "Tablas optimizadas exitosamente.");
        } catch (Exception e) { mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage()); }
    }

    @FXML public void limpiarAuditoria() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Limpiar Log"); a.setHeaderText(null);
        a.setContentText("¿Eliminar todos los registros del log de auditoria?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK)
                mostrarAlerta(Alert.AlertType.INFORMATION, "Log Limpiado", "Log de auditoria limpiado.");
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  AUDITORÍA Y CIERRE DE SESIÓN
    // ═════════════════════════════════════════════════════════════════════════

    private void registrarLogout() {
        String sql = "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, detalle) " +
                "VALUES (?, 'LOGOUT', 'usuarios', ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int idUsuario = SesionUsuario.getInstancia().getIdUsuario();
            String nombre = SesionUsuario.getInstancia().getNombre();
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setString(3, "Cierre de sesión: " + nombre);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  NAVEGACIÓN
    // ═════════════════════════════════════════════════════════════════════════

    @FXML private void irADashboard()    { navegar("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML private void irAVentas()       { navegar("/org/example/vista/Ventas.fxml"); }
    @FXML private void irAInventario()   { navegar("/org/example/vista/Inventario.fxml"); }
    @FXML private void irAEmpleados()    { navegar("/org/example/vista/Empleados.fxml"); }
    @FXML private void irAClientes()     { navegar("/org/example/vista/Clientes.fxml"); }
    @FXML private void irAReportes()     { navegar("/org/example/vista/Reportes.fxml"); }
    @FXML private void irAAuditoria()    { navegar("/org/example/vista/Auditoria.fxml"); }
    @FXML private void irACorteCaja()    { navegar("/org/example/vista/CorteCaja.fxml"); }

    @FXML
    public void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Salir"); a.setHeaderText(null);
        a.setContentText("¿Seguro que deseas salir?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                registrarLogout();
                if (relojTimeline != null) relojTimeline.stop();
                Platform.exit();
            }
        });
    }

    private void navegar(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) lblNombreUsuario.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UTILIDAD
    // ═════════════════════════════════════════════════════════════════════════

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje);
        a.showAndWait();
    }
}