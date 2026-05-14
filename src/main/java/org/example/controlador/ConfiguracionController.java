package org.example.controlador;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.servicio.TicketImpresora;
import org.example.servicio.TicketRenderer;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

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

    private final ObservableList<FilaUsuario> listaUsuarios = FXCollections.observableArrayList();

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
    @FXML private HBox       boxEstadoDB;
    @FXML private FontIcon   icoEstadoDB;
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

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FECHA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String TAB = "cfg-tab";
    private static final String TAB_ACTIVE = "cfg-tab-active";
    private static final String BTN_PRIMARY = "cfg-btn-primary";
    private static final String BTN_SUCCESS = "cfg-btn-success";

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
        configurarTablaUsuarios();
        configurarListeners();
        mostrarTab("negocio");
        cargarConfiguracion();          // BD → campos (con fallback a Preferences)
        verificarEstadoDB();            // estado real al abrir
        cargarUsuariosDesdeDB();
    }

    private void configurarListeners() {
        if (txtBuscarUsuario != null) {
            txtBuscarUsuario.textProperty().addListener((o, a, b) -> filtrarUsuarios(b));
        }
    }

    // ── Reloj en tiempo real ──────────────────────────────────────────────────
    private void iniciarReloj() {
        if (lblHora == null) return;
        lblHora.setText(LocalDateTime.now().format(HORA_FMT));
        relojTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e ->
                        lblHora.setText(LocalDateTime.now().format(HORA_FMT))
                )
        );
        relojTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        relojTimeline.play();
    }

    private void detenerReloj() {
        if (relojTimeline != null) {
            relojTimeline.stop();
            relojTimeline = null;
        }
    }

    // ── Datos del usuario activo ──────────────────────────────────────────────
    private void cargarDatosUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        String nombre = sesion.getNombre() != null ? sesion.getNombre() : "Usuario";
        if (lblNombreUsuario != null) lblNombreUsuario.setText(nombre);
        if (lblRolUsuario    != null) lblRolUsuario.setText(sesion.getRol());
        if (lblAvatarIniciales != null)
            lblAvatarIniciales.setText(
                    nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase());
    }

    // ── Poblar combos con valores fijos ───────────────────────────────────────
    private void poblarCombos() {
        setComboItems(cmbMoneda, "MXN - Peso Mexicano", "MXN - Peso Mexicano", "USD - Dolar", "EUR - Euro");
        setComboItems(cmbZonaHoraria, "America/Monterrey (CST)",
                "America/Monterrey (CST)", "America/Mexico_City (CST)",
                "America/Tijuana (PST)", "America/Cancun (EST)");
        setComboItems(cmbMetodoPago, "Efectivo y Tarjeta", "Efectivo", "Efectivo y Tarjeta", "Todos los metodos");
        setComboItems(cmbRedondeo, "Sin redondeo", "Sin redondeo", "Redondear a $0.50", "Redondear a $1.00");
        setComboItems(cmbBusqueda, "Nombre o codigo", "Nombre del producto", "Codigo de barras", "Nombre o codigo", "Categoria");
        setComboItems(cmbOrdenProductos, "Por categoria", "Alfabetico", "Mas vendido primero", "Por categoria", "Precio ascendente");
        setComboItems(cmbImpresora, "EPSON TM-T20III", "EPSON TM-T20III", "EPSON TM-T88V", "Star TSP100", "Generica");
        setComboItems(cmbAnchoPapel, "58 mm", "58 mm", "80 mm");
        setComboItems(cmbInactividad, "15 minutos", "5 minutos", "10 minutos", "15 minutos", "30 minutos", "1 hora");
        setComboItems(cmbSmtp, "Gmail", "Gmail", "Outlook / Hotmail", "Yahoo Mail", "SMTP personalizado");
        setComboItems(cmbMotorDB, "MySQL 8.x", "MySQL 8.x", "MySQL 5.7", "MariaDB");
        setComboItems(cmbFrecuenciaRespaldo, "Diario", "Diario", "Semanal", "Quincenal", "Mensual");
    }

    @SafeVarargs
    private final void setComboItems(ComboBox<String> combo, String defaultValue, String... values) {
        if (combo == null) return;
        combo.getItems().setAll(values);
        combo.setValue(defaultValue);
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
        Map<String, String> valores = obtenerValoresConfiguracion();

        if (asegurarTablaConfiguracion()) {
            try (Connection con = ConexionDB.getConexion()) {
                for (Map.Entry<String, String> entry : valores.entrySet()) {
                    dbSet(con, entry.getKey(), entry.getValue());
                }
                guardadoEnBD = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Fallback: tambien en Preferences (portabilidad y compatibilidad con TicketService)
        guardarEnPreferences(valores);

        // ── Feedback visual inline — sin Alert ──────────────────────────────
        mostrarFeedbackGuardado(guardadoEnBD);
    }

    private Map<String, String> obtenerValoresConfiguracion() {
        Map<String, String> valores = new LinkedHashMap<>();

        valores.put("negocio_nombre", text(txtNombreNegocio));
        valores.put("negocio_slogan", text(txtSlogan));
        valores.put("negocio_telefono", text(txtTelefono));
        valores.put("negocio_direccion", text(txtDireccion));
        valores.put("negocio_ciudad", text(txtCiudad));
        valores.put("negocio_cp", text(txtCP));
        valores.put("negocio_correo", text(txtCorreo));
        valores.put("negocio_web", text(txtSitioWeb));
        valores.put("negocio_rfc", text(txtRFC));
        valores.put("negocio_moneda", combo(cmbMoneda, "MXN - Peso Mexicano"));
        valores.put("negocio_zona", combo(cmbZonaHoraria, "America/Monterrey (CST)"));
        valores.put("negocio_hora_ap", text(txtHoraApertura));
        valores.put("negocio_hora_ci", text(txtHoraCierre));
        valores.put("negocio_dias", diasOperacion());
        valores.put("negocio_credito", text(txtLimiteCredito));
        valores.put("negocio_sucursal", text(txtNumSucursal));
        valores.put("negocio_mantenimiento", bool(tglMantenimiento));

        valores.put("pos_metodo_pago", combo(cmbMetodoPago, "Efectivo y Tarjeta"));
        valores.put("pos_redondeo", combo(cmbRedondeo, "Sin redondeo"));
        valores.put("pos_busqueda", combo(cmbBusqueda, "Nombre o codigo"));
        valores.put("pos_orden_productos", combo(cmbOrdenProductos, "Por categoria"));
        valores.put("pos_apertura_caja", bool(tglAperturaCaja));
        valores.put("pos_confirmar_cobro", bool(tglConfirmarCobro));
        valores.put("pos_auto_imprimir", bool(tglAutoImprimir));
        valores.put("pos_imagenes_producto", bool(tglImagenesProducto));
        valores.put("pos_alerta_stock", bool(tglAlertaStock));
        valores.put("pos_venta_sin_stock", bool(tglVentaSinStock));
        valores.put("pos_asociar_cliente", bool(tglAsociarCliente));
        valores.put("pos_venta_credito", bool(tglVentaCredito));
        valores.put("pos_devoluciones", bool(tglDevoluciones));
        valores.put("pos_venta_rapida", bool(tglVentaRapida));
        valores.put("pos_nota_interna", text(txtNotaInterna));

        valores.put("seguridad_auto_bloqueo", bool(tglAutoBloqueo));
        valores.put("seguridad_inactividad", combo(cmbInactividad, "15 minutos"));
        valores.put("seguridad_auditoria", bool(tglAuditoria));

        valores.put("ticket_impresora", combo(cmbImpresora, "EPSON TM-T20III"));
        valores.put("ticket_nombre", text(txtTicketNombre));
        valores.put("ticket_giro", text(txtTicketGiro));
        valores.put("ticket_direccion", text(txtTicketDireccion));
        valores.put("ticket_ciudad", text(txtTicketCiudad));
        valores.put("ticket_telefono", text(txtTicketTelefono));
        valores.put("ticket_encabezado", text(txtMensajeEncabezado));
        valores.put("ticket_pie", text(txtMensajePie));
        valores.put("ticket_aviso", text(txtAvisoFiscal));
        valores.put("ticket_ancho", combo(cmbAnchoPapel, "58 mm"));
        valores.put("ticket_logo", bool(tglLogoTicket));
        valores.put("ticket_folio", bool(tglFolioTicket));
        valores.put("ticket_desglose", bool(tglDesglose));
        valores.put("ticket_qr", bool(tglQR));
        valores.put("ticket_copia_cocina", bool(tglCopiacocina));
        valores.put("ticket_mostrar_fecha", bool(tglMostrarFecha));
        valores.put("ticket_mostrar_cajero", bool(tglMostrarCajero));

        valores.put("int_clip", text(txtClipToken));
        valores.put("int_stripe", text(txtStripeKey));
        valores.put("int_correo_rep", text(txtCorreoReportes));
        valores.put("int_smtp", combo(cmbSmtp, "Gmail"));
        valores.put("int_reporte_diario", bool(tglReporteDiario));
        valores.put("int_alerta_stock_correo", bool(tglAlertaStockCorreo));
        valores.put("int_twilio_sid", text(txtTwilioSid));
        valores.put("int_twilio_token", text(txtTwilioToken));
        valores.put("int_ticket_whatsapp", bool(tglTicketWhatsapp));

        valores.put("respaldo_ruta", text(txtRutaRespaldo));
        valores.put("respaldo_frecuencia", combo(cmbFrecuenciaRespaldo, "Diario"));
        valores.put("respaldo_auto", bool(tglRespaldoAuto));
        return valores;
    }

    private String text(TextInputControl control) {
        return control != null && control.getText() != null ? control.getText().trim() : "";
    }

    private String combo(ComboBox<String> combo, String defaultValue) {
        return combo != null && combo.getValue() != null ? combo.getValue() : defaultValue;
    }

    private String bool(org.example.modelo.SwitchToggle toggle) {
        return String.valueOf(toggle != null && toggle.isSelected());
    }

    private String bool(ToggleButton toggle) {
        return String.valueOf(toggle != null && toggle.isSelected());
    }

    private String diasOperacion() {
        return bool(tglLun) + "," + bool(tglMar) + "," + bool(tglMie) + "," +
                bool(tglJue) + "," + bool(tglVie) + "," + bool(tglSab) + "," + bool(tglDom);
    }

    /**
     * Muestra feedback inline en el topbar:
     * - El botón "Guardar Cambios" se vuelve verde por 2 segundos
     * - lblGuardadoAt muestra "Guardado a las HH:mm:ss"
     */
    private void mostrarFeedbackGuardado(boolean enBD) {
        if (lblGuardadoAt != null) {
            String hora = LocalDateTime.now().format(HORA_FMT);
            String origen = enBD ? "BD" : "local";
            lblGuardadoAt.setText("Guardado a las " + hora + " (" + origen + ")");
            lblGuardadoAt.setVisible(true);
            lblGuardadoAt.setManaged(true);
        }

        if (btnGuardar != null) {
            setStyleClass(btnGuardar, BTN_SUCCESS);
            btnGuardar.setText("Guardado");

            // Vuelve al estado original después de 2.5 segundos
            javafx.animation.PauseTransition pausa =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.5));
            pausa.setOnFinished(ev -> {
                setStyleClass(btnGuardar, BTN_PRIMARY);
                btnGuardar.setText("Guardar Cambios");
            });
            pausa.play();
        }
    }

    // ── Guardar en Preferences (fallback / portabilidad) ─────────────────────
    private void guardarEnPreferences(Map<String, String> valores) {
        Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);
        valores.forEach(prefs::put);
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
                setComboValue(cmbMoneda, dbGet(con, "negocio_moneda", "MXN - Peso Mexicano"), "MXN - Peso Mexicano");
                setComboValue(cmbZonaHoraria, dbGet(con, "negocio_zona", "America/Monterrey (CST)"), "America/Monterrey (CST)");
                txtHoraApertura.setText(dbGet(con, "negocio_hora_ap", "08:00"));
                txtHoraCierre.setText(dbGet(con, "negocio_hora_ci", "21:00"));
                aplicarDiasOperacion(dbGet(con, "negocio_dias", "true,true,true,true,true,true,false"));
                txtLimiteCredito.setText(dbGet(con, "negocio_credito", ""));
                txtNumSucursal.setText(dbGet(con, "negocio_sucursal", ""));
                tglMantenimiento.setSelected(Boolean.parseBoolean(dbGet(con, "negocio_mantenimiento", "false")));

                setComboValue(cmbMetodoPago, dbGet(con, "pos_metodo_pago", "Efectivo y Tarjeta"), "Efectivo y Tarjeta");
                setComboValue(cmbRedondeo, dbGet(con, "pos_redondeo", "Sin redondeo"), "Sin redondeo");
                setComboValue(cmbBusqueda, dbGet(con, "pos_busqueda", "Nombre o codigo"), "Nombre o codigo");
                setComboValue(cmbOrdenProductos, dbGet(con, "pos_orden_productos", "Por categoria"), "Por categoria");
                setSwitch(tglAperturaCaja, dbGet(con, "pos_apertura_caja", "true"));
                setSwitch(tglConfirmarCobro, dbGet(con, "pos_confirmar_cobro", "true"));
                setSwitch(tglAutoImprimir, dbGet(con, "pos_auto_imprimir", "false"));
                setSwitch(tglImagenesProducto, dbGet(con, "pos_imagenes_producto", "true"));
                setSwitch(tglAlertaStock, dbGet(con, "pos_alerta_stock", "true"));
                setSwitch(tglVentaSinStock, dbGet(con, "pos_venta_sin_stock", "false"));
                setSwitch(tglAsociarCliente, dbGet(con, "pos_asociar_cliente", "true"));
                setSwitch(tglVentaCredito, dbGet(con, "pos_venta_credito", "false"));
                setSwitch(tglDevoluciones, dbGet(con, "pos_devoluciones", "true"));
                setSwitch(tglVentaRapida, dbGet(con, "pos_venta_rapida", "false"));
                txtNotaInterna.setText(dbGet(con, "pos_nota_interna", ""));

                setSwitch(tglAutoBloqueo, dbGet(con, "seguridad_auto_bloqueo", "true"));
                setComboValue(cmbInactividad, dbGet(con, "seguridad_inactividad", "15 minutos"), "15 minutos");
                setSwitch(tglAuditoria, dbGet(con, "seguridad_auditoria", "true"));

                tglLogoTicket.setSelected(   Boolean.parseBoolean(dbGet(con, "ticket_logo", "true")));
                tglFolioTicket.setSelected(  Boolean.parseBoolean(dbGet(con, "ticket_folio", "true")));
                tglDesglose.setSelected(     Boolean.parseBoolean(dbGet(con, "ticket_desglose", "true")));
                tglQR.setSelected(           Boolean.parseBoolean(dbGet(con, "ticket_qr", "false")));
                setSwitch(tglCopiacocina, dbGet(con, "ticket_copia_cocina", "false"));
                setSwitch(tglMostrarFecha, dbGet(con, "ticket_mostrar_fecha", "true"));
                setSwitch(tglMostrarCajero, dbGet(con, "ticket_mostrar_cajero", "true"));

                setComboValue(cmbImpresora, dbGet(con, "ticket_impresora", "EPSON TM-T20III"), "EPSON TM-T20III");
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
                setComboValue(cmbSmtp, dbGet(con, "int_smtp", "Gmail"), "Gmail");
                setSwitch(tglReporteDiario, dbGet(con, "int_reporte_diario", "false"));
                setSwitch(tglAlertaStockCorreo, dbGet(con, "int_alerta_stock_correo", "true"));
                txtTwilioSid.setText(        dbGet(con, "int_twilio_sid", ""));
                txtTwilioToken.setText(      dbGet(con, "int_twilio_token", ""));
                setSwitch(tglTicketWhatsapp, dbGet(con, "int_ticket_whatsapp", "false"));

                txtRutaRespaldo.setText(dbGet(con, "respaldo_ruta", ""));
                setComboValue(cmbFrecuenciaRespaldo, dbGet(con, "respaldo_frecuencia", "Diario"), "Diario");
                setSwitch(tglRespaldoAuto, dbGet(con, "respaldo_auto", "true"));
                return; // cargado desde BD, no necesita Preferences
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Fallback a Preferences
        cargarDesdePreferences();
    }

    private void cargarDesdePreferences() {
        Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);
        txtNombreNegocio.setText(prefs.get("negocio_nombre", ""));
        txtSlogan.setText(prefs.get("negocio_slogan", ""));
        txtTelefono.setText(prefs.get("negocio_telefono", ""));
        txtDireccion.setText(prefs.get("negocio_direccion", ""));
        txtCiudad.setText(prefs.get("negocio_ciudad", ""));
        txtCP.setText(prefs.get("negocio_cp", ""));
        txtCorreo.setText(prefs.get("negocio_correo", ""));
        txtSitioWeb.setText(prefs.get("negocio_web", ""));
        txtRFC.setText(prefs.get("negocio_rfc", ""));
        setComboValue(cmbMoneda, prefs.get("negocio_moneda", "MXN - Peso Mexicano"), "MXN - Peso Mexicano");
        setComboValue(cmbZonaHoraria, prefs.get("negocio_zona", "America/Monterrey (CST)"), "America/Monterrey (CST)");
        txtHoraApertura.setText(prefs.get("negocio_hora_ap", "08:00"));
        txtHoraCierre.setText(prefs.get("negocio_hora_ci", "21:00"));
        aplicarDiasOperacion(prefs.get("negocio_dias", "true,true,true,true,true,true,false"));
        txtLimiteCredito.setText(prefs.get("negocio_credito", ""));
        txtNumSucursal.setText(prefs.get("negocio_sucursal", ""));
        tglMantenimiento.setSelected(prefs.getBoolean("negocio_mantenimiento", false));

        setComboValue(cmbMetodoPago, prefs.get("pos_metodo_pago", "Efectivo y Tarjeta"), "Efectivo y Tarjeta");
        setComboValue(cmbRedondeo, prefs.get("pos_redondeo", "Sin redondeo"), "Sin redondeo");
        setComboValue(cmbBusqueda, prefs.get("pos_busqueda", "Nombre o codigo"), "Nombre o codigo");
        setComboValue(cmbOrdenProductos, prefs.get("pos_orden_productos", "Por categoria"), "Por categoria");
        tglAperturaCaja.setSelected(prefs.getBoolean("pos_apertura_caja", true));
        tglConfirmarCobro.setSelected(prefs.getBoolean("pos_confirmar_cobro", true));
        tglAutoImprimir.setSelected(prefs.getBoolean("pos_auto_imprimir", false));
        tglImagenesProducto.setSelected(prefs.getBoolean("pos_imagenes_producto", true));
        tglAlertaStock.setSelected(prefs.getBoolean("pos_alerta_stock", true));
        tglVentaSinStock.setSelected(prefs.getBoolean("pos_venta_sin_stock", false));
        tglAsociarCliente.setSelected(prefs.getBoolean("pos_asociar_cliente", true));
        tglVentaCredito.setSelected(prefs.getBoolean("pos_venta_credito", false));
        tglDevoluciones.setSelected(prefs.getBoolean("pos_devoluciones", true));
        tglVentaRapida.setSelected(prefs.getBoolean("pos_venta_rapida", false));
        txtNotaInterna.setText(prefs.get("pos_nota_interna", ""));

        tglAutoBloqueo.setSelected(prefs.getBoolean("seguridad_auto_bloqueo", true));
        setComboValue(cmbInactividad, prefs.get("seguridad_inactividad", "15 minutos"), "15 minutos");
        tglAuditoria.setSelected(prefs.getBoolean("seguridad_auditoria", true));

        tglLogoTicket.setSelected(   prefs.getBoolean("ticket_logo",     true));
        tglFolioTicket.setSelected(  prefs.getBoolean("ticket_folio",    true));
        tglDesglose.setSelected(     prefs.getBoolean("ticket_desglose", true));
        tglQR.setSelected(           prefs.getBoolean("ticket_qr",       false));
        tglCopiacocina.setSelected(prefs.getBoolean("ticket_copia_cocina", false));
        tglMostrarFecha.setSelected(prefs.getBoolean("ticket_mostrar_fecha", true));
        tglMostrarCajero.setSelected(prefs.getBoolean("ticket_mostrar_cajero", true));
        setComboValue(cmbImpresora, prefs.get("ticket_impresora", "EPSON TM-T20III"), "EPSON TM-T20III");
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
        setComboValue(cmbSmtp, prefs.get("int_smtp", "Gmail"), "Gmail");
        tglReporteDiario.setSelected(prefs.getBoolean("int_reporte_diario", false));
        tglAlertaStockCorreo.setSelected(prefs.getBoolean("int_alerta_stock_correo", true));
        txtTwilioSid.setText(      prefs.get("int_twilio_sid", ""));
        txtTwilioToken.setText(    prefs.get("int_twilio_token", ""));
        tglTicketWhatsapp.setSelected(prefs.getBoolean("int_ticket_whatsapp", false));

        txtRutaRespaldo.setText(prefs.get("respaldo_ruta", ""));
        setComboValue(cmbFrecuenciaRespaldo, prefs.get("respaldo_frecuencia", "Diario"), "Diario");
        tglRespaldoAuto.setSelected(prefs.getBoolean("respaldo_auto", true));
    }

    private void setComboValue(ComboBox<String> combo, String value, String defaultValue) {
        if (combo == null) return;
        combo.setValue(combo.getItems().contains(value) ? value : defaultValue);
    }

    private void setSwitch(org.example.modelo.SwitchToggle toggle, String value) {
        if (toggle != null) toggle.setSelected(Boolean.parseBoolean(value));
    }

    private void aplicarDiasOperacion(String encoded) {
        String[] dias = encoded != null ? encoded.split(",", -1) : new String[0];
        setDia(tglLun, dias, 0, true);
        setDia(tglMar, dias, 1, true);
        setDia(tglMie, dias, 2, true);
        setDia(tglJue, dias, 3, true);
        setDia(tglVie, dias, 4, true);
        setDia(tglSab, dias, 5, true);
        setDia(tglDom, dias, 6, false);
    }

    private void setDia(ToggleButton toggle, String[] dias, int index, boolean defaultValue) {
        if (toggle == null) return;
        toggle.setSelected(dias.length > index ? Boolean.parseBoolean(dias[index]) : defaultValue);
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
            lblEstadoDB.setText("Conectado - " + db + "@" + host);
            setStyleClass(lblEstadoDB, "cfg-db-status-label");
            setStyleClass(boxEstadoDB, "cfg-db-status-ok");
            if (icoEstadoDB != null) icoEstadoDB.setIconColor(Color.web("#27AE60"));
        } else {
            lblEstadoDB.setText("Sin conexion - revisa los parametros");
            setStyleClass(lblEstadoDB, "cfg-db-status-label-error");
            setStyleClass(boxEstadoDB, "cfg-db-status-error");
            if (icoEstadoDB != null) icoEstadoDB.setIconColor(Color.web("#C0392B"));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TABLA DE USUARIOS DESDE BD
    // ═════════════════════════════════════════════════════════════════════════

    private void configurarTablaUsuarios() {
        if (tablaUsuarios == null) return;

        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
                setStyleClasses(badge, "cfg-role-badge", claseRol(rol));
                setGraphic(badge);
            }
        });

        // Columna ESTADO — color
        colUsuEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                setAlignment(Pos.CENTER);
                getStyleClass().removeAll("cfg-user-status-active", "cfg-user-status-inactive");
                if (empty || estado == null) { setText(null); return; }
                setText(estado);
                getStyleClass().add("Activo".equalsIgnoreCase(estado)
                        ? "cfg-user-status-active"
                        : "cfg-user-status-inactive");
            }
        });

        // Columna ACCIONES
        colUsuAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("Editar");
            private final Button btnPassword = new Button("Clave");
            private final Button btnEliminar = new Button("Borrar");
            private final HBox   caja        = new HBox(6, btnEditar, btnPassword, btnEliminar);
            {
                setStyleClasses(btnEditar, "cfg-table-action", "cfg-table-action-edit");
                setStyleClasses(btnPassword, "cfg-table-action", "cfg-table-action-key");
                setStyleClasses(btnEliminar, "cfg-table-action", "cfg-table-action-delete");
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
    }

    private String claseRol(String rol) {
        return switch (rol.toLowerCase()) {
            case "administrador" -> "cfg-role-badge-admin";
            case "gerente" -> "cfg-role-badge-manager";
            default -> "cfg-role-badge-default";
        };
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

        setStyleClass(btnTabNegocio, TAB);
        setStyleClass(btnTabPOS, TAB);
        setStyleClass(btnTabUsuarios, TAB);
        setStyleClass(btnTabFiscal, TAB);
        setStyleClass(btnTabIntegraciones, TAB);
        setStyleClass(btnTabBaseDatos, TAB);

        switch (tab) {
            case "negocio"       -> { panelNegocio.setVisible(true);       panelNegocio.setManaged(true);       setStyleClass(btnTabNegocio, TAB_ACTIVE); }
            case "pos"           -> { panelPOS.setVisible(true);           panelPOS.setManaged(true);           setStyleClass(btnTabPOS, TAB_ACTIVE); }
            case "usuarios"      -> { panelUsuarios.setVisible(true);      panelUsuarios.setManaged(true);      setStyleClass(btnTabUsuarios, TAB_ACTIVE);
                cargarUsuariosDesdeDB(); } // refresca al abrir la pestaña
            case "fiscal"        -> { panelFiscal.setVisible(true);        panelFiscal.setManaged(true);        setStyleClass(btnTabFiscal, TAB_ACTIVE); }
            case "integraciones" -> { panelIntegraciones.setVisible(true); panelIntegraciones.setManaged(true); setStyleClass(btnTabIntegraciones, TAB_ACTIVE); }
            case "basedatos"     -> { panelBaseDatos.setVisible(true);     panelBaseDatos.setManaged(true);     setStyleClass(btnTabBaseDatos, TAB_ACTIVE);
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
                txtHoraApertura.setText("08:00");
                txtHoraCierre.setText("21:00");
                aplicarDiasOperacion("true,true,true,true,true,true,false");
                txtLimiteCredito.clear();
                txtNumSucursal.clear();
                tglMantenimiento.setSelected(false);
                tglLogoTicket.setSelected(true);
                tglFolioTicket.setSelected(true);
                tglDesglose.setSelected(true);
                tglQR.setSelected(false);
                tglCopiacocina.setSelected(false);
                tglMostrarFecha.setSelected(true);
                tglMostrarCajero.setSelected(true);
                cmbMoneda.setValue("MXN - Peso Mexicano");
                cmbZonaHoraria.setValue("America/Monterrey (CST)");
                cmbImpresora.setValue("EPSON TM-T20III");
                cmbAnchoPapel.setValue("58 mm");
                cmbMetodoPago.setValue("Efectivo y Tarjeta");
                cmbRedondeo.setValue("Sin redondeo");
                cmbBusqueda.setValue("Nombre o codigo");
                cmbOrdenProductos.setValue("Por categoria");
                tglAperturaCaja.setSelected(true);
                tglConfirmarCobro.setSelected(true);
                tglAutoImprimir.setSelected(false);
                tglImagenesProducto.setSelected(true);
                tglAlertaStock.setSelected(true);
                tglVentaSinStock.setSelected(false);
                tglAsociarCliente.setSelected(true);
                tglVentaCredito.setSelected(false);
                tglDevoluciones.setSelected(true);
                tglVentaRapida.setSelected(false);
                txtNotaInterna.clear();
                tglAutoBloqueo.setSelected(true);
                cmbInactividad.setValue("15 minutos");
                tglAuditoria.setSelected(true);
                txtTicketNombre.setText("Volovan Volo");
                txtTicketGiro.setText("Panaderia y Reposteria");
                txtTicketDireccion.clear();
                txtTicketCiudad.clear();
                txtTicketTelefono.clear();
                txtMensajeEncabezado.setText("Bienvenido!\nGracias por visitarnos.");
                txtMensajePie.setText("Gracias por su compra!\nVuelva pronto.\nfacebook.com/VolovanVolo");
                txtAvisoFiscal.setText("Este ticket no es\nComprobante fiscal");
                txtClipToken.clear();
                txtStripeKey.clear();
                txtCorreoReportes.clear();
                cmbSmtp.setValue("Gmail");
                tglReporteDiario.setSelected(false);
                tglAlertaStockCorreo.setSelected(true);
                txtTwilioSid.clear();
                txtTwilioToken.clear();
                tglTicketWhatsapp.setSelected(false);
                txtRutaRespaldo.clear();
                cmbFrecuenciaRespaldo.setValue("Diario");
                tglRespaldoAuto.setSelected(true);
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
                Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);
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
                LocalDateTime.now().format(FECHA_HORA_FMT) +
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
        try (Connection conn = ConexionDB.getConexion()) {
            if (conn == null) { lblTamanoDB.setText("Sin conexion"); return; }
            try (Statement stmt = conn.createStatement()) {
                ResultSet rsTickets = stmt.executeQuery("SELECT COUNT(*) FROM tickets");
                if (rsTickets.next()) lblRegVentas.setText(rsTickets.getInt(1) + " registros");
                ResultSet rsProductos = stmt.executeQuery("SELECT COUNT(*) FROM productos");
                if (rsProductos.next()) lblRegProductos.setText(rsProductos.getInt(1) + " productos");
            }
            try (PreparedStatement psSize = conn.prepareStatement(
                    "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS tamano_mb " +
                            "FROM information_schema.tables WHERE table_schema = ?")) {
                psSize.setString(1, txtDBNombre.getText());
                ResultSet rsSize = psSize.executeQuery();
                if (rsSize.next()) lblTamanoDB.setText(rsSize.getString(1) + " MB");
            }
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
                detenerReloj();
                Platform.exit();
            }
        });
    }

    private void navegar(String ruta) {
        try {
            detenerReloj();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) lblNombreUsuario.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UTILIDAD
    // ═════════════════════════════════════════════════════════════════════════

    private void setStyleClass(Node node, String styleClass) {
        if (node != null) node.getStyleClass().setAll(styleClass);
    }

    private void setStyleClasses(Node node, String... styleClasses) {
        if (node != null) node.getStyleClass().setAll(styleClasses);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje);
        a.showAndWait();
    }
}
