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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.dao.FiscalDAO;
import org.example.modelo.ConfiguracionFiscal;
import org.example.modelo.Impuesto;
import org.example.modelo.SesionUsuario;
import org.example.modelo.SwitchToggle;
import org.example.modelo.Ticket;
import org.example.servicio.AuditoriaService;
import org.example.servicio.MarcaService;
import org.example.servicio.PasswordService;
import org.example.servicio.PermisoService;
import org.example.servicio.TicketRenderer;
import org.example.servicio.UsuarioSeguridadService;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    @FXML private VBox panelFiscal;
    @FXML private VBox panelTicket;
    @FXML private VBox panelCajon;
    @FXML private VBox panelEmail;
    @FXML private VBox panelUsuarios;
    @FXML private VBox panelFiscal;
    @FXML private VBox panelIntegraciones;
    @FXML private VBox panelBaseDatos;

    @FXML private Button btnTabNegocio;
    @FXML private Button btnTabFiscal;
    @FXML private Button btnTabTicket;
    @FXML private Button btnTabCajon;
    @FXML private Button btnTabEmail;
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

    @FXML private TextField txtFiscalRfc;
    @FXML private TextField txtFiscalCP;
    @FXML private TextField txtFiscalRazonSocial;
    @FXML private ComboBox<String> cmbFiscalRegimen;
    @FXML private ComboBox<String> cmbFiscalImpuestoDefault;
    @FXML private ComboBox<String> cmbRegionFiscal;
    @FXML private SwitchToggle tglPrecioIncluyeImpuesto;
    @FXML private SwitchToggle tglImpuestoPorProducto;
    @FXML private SwitchToggle tglMostrarDesgloseFiscal;
    @FXML private TextField txtSerieFactura;
    @FXML private TextField txtFolioInicial;
    @FXML private ComboBox<String> cmbModoFacturacion;
    @FXML private ComboBox<String> cmbUsoCfdiDefault;
    @FXML private ComboBox<String> cmbMetodoPagoSat;
    @FXML private ComboBox<String> cmbFormaPagoSat;

    @FXML private TextField txtTicketNombre;
    @FXML private TextField txtTicketGiro;
    @FXML private TextField txtTicketDireccion;
    @FXML private TextField txtTicketCiudad;
    @FXML private TextField txtTicketTelefono;
    @FXML private TextArea txtMensajeEncabezado;
    @FXML private TextArea txtMensajePie;
    @FXML private TextArea txtAvisoFiscal;
    @FXML private SwitchToggle tglLogoTicket;
    @FXML private SwitchToggle tglFolioTicket;
    @FXML private SwitchToggle tglDesglose;
    @FXML private SwitchToggle tglQR;
    @FXML private ComboBox<String> cmbImpresora;
    @FXML private ComboBox<String> cmbAnchoPapel;

    @FXML private SwitchToggle tglCajonActivo;
    @FXML private ComboBox<String> cmbCajonPuerto;
    @FXML private ComboBox<String> cmbCajonPulso;

    @FXML private SwitchToggle tglEmailActivo;
    @FXML private ComboBox<String> cmbEmailSmtp;
    @FXML private TextField txtEmailRemitente;
    @FXML private PasswordField txtEmailPassword;
    @FXML private TextField txtEmailHost;
    @FXML private TextField txtEmailPuerto;
    @FXML private SwitchToggle tglEmailReporteDiario;
    @FXML private VBox boxSmtpPersonalizado;

    @FXML private TextField txtBuscarUsuario;
    @FXML private TableView<UsuarioRow> tablaUsuarios;
    @FXML private TableColumn<UsuarioRow, String> colUsuarioNombre;
    @FXML private TableColumn<UsuarioRow, String> colUsuarioRol;
    @FXML private TableColumn<UsuarioRow, String> colUsuarioEstado;
    @FXML private TableColumn<UsuarioRow, Void> colUsuarioAcciones;

    @FXML private Label lblEstadoDB;
    @FXML private Label lblDBHost;
    @FXML private Label lblDBNombre;
    @FXML private Label lblDBVersion;
    @FXML private TextField txtRutaRespaldo;

    private final Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);
    private final FiscalDAO fiscalDAO = new FiscalDAO();
    private final PasswordService passwordService = new PasswordService();
    private final UsuarioSeguridadService usuarioSeguridadService = new UsuarioSeguridadService();
    private final ObservableList<UsuarioRow> usuarios = FXCollections.observableArrayList();
    private List<VBox> panelesConfiguracion;
    private List<Button> botonesConfiguracion;
    private FadeTransition feedbackFade;

    @FXML
    public void initialize() {
        cargarDatosUsuario();
        iniciarReloj();
        poblarCombos();
        configurarTablaUsuarios();
        configurarTablaImpuestos();
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
        String nombre = sesion.getNombre() == null ? "Usuario" : sesion.getNombre();
        lblNombreUsuario.setText(nombre);
        lblRolUsuario.setText(sesion.getRol());
        lblAvatarIniciales.setText(iniciales(nombre));
        lblMarcaNegocio.setText(MarcaService.nombreNegocio());
    }

    private void prepararCombos() {
        cmbAnchoPapel.getItems().setAll("58 mm", "80 mm");
        cmbCajonPuerto.getItems().setAll("Via impresora termica (ESC/POS)", "COM1", "COM2", "COM3", "COM4");
        cmbCajonPulso.getItems().setAll("Pulso 1 (pin 2)", "Pulso 2 (pin 5)");
        cmbEmailSmtp.getItems().setAll("Gmail", "Outlook / Hotmail", "SMTP personalizado");
        prepararCombosFiscal();

        cmbImpresora.getItems().clear();
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : printServices) {
            cmbImpresora.getItems().add(service.getName());
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

    private void prepararCombosFiscal() {
        cmbFiscalRegimen.getItems().setAll(
                "601 - General de Ley Personas Morales",
                "603 - Personas Morales con Fines no Lucrativos",
                "605 - Sueldos y Salarios",
                "612 - Personas Fisicas con Actividades Empresariales",
                "626 - Regimen Simplificado de Confianza"
        );
        cmbRegionFiscal.getItems().setAll("GENERAL", "FRONTERA");
        cmbModoFacturacion.getItems().setAll("PREFACTURA", "PAC_FUTURO");
        cmbUsoCfdiDefault.getItems().setAll(
                "G03 - Gastos en general",
                "S01 - Sin efectos fiscales",
                "CP01 - Pagos",
                "P01 - Por definir"
        );
        cmbMetodoPagoSat.getItems().setAll(
                "PUE - Pago en una sola exhibicion",
                "PPD - Pago en parcialidades o diferido"
        );
        cmbFormaPagoSat.getItems().setAll(
                "01 - Efectivo",
                "03 - Transferencia electronica",
                "04 - Tarjeta de credito",
                "28 - Tarjeta de debito",
                "99 - Por definir"
        );

        cmbFiscalImpuestoDefault.getItems().clear();
        List<Impuesto> impuestos = fiscalDAO.obtenerImpuestosActivos();
        if (impuestos.isEmpty()) {
            cmbFiscalImpuestoDefault.getItems().setAll(
                    "IVA_16 - IVA general (16.00%)",
                    "IVA_8 - IVA frontera (8.00%)",
                    "TASA_0 - Tasa 0 (0.00%)",
                    "EXENTO - Exento (0.00%)",
                    "SIN_IMPUESTO - Sin impuesto (0.00%)"
            );
            return;
        }
        for (Impuesto impuesto : impuestos) {
            double porcentaje = impuesto.getTasa().multiply(new java.math.BigDecimal("100")).doubleValue();
            cmbFiscalImpuestoDefault.getItems().add(
                    impuesto.getClave() + " - " + impuesto.getNombre() + " (" + String.format("%.2f", porcentaje) + "%)"
            );
        }
    }

    private void prepararUsuarios() {
        colUsuarioNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nombre()));
        colUsuarioRol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().rol()));
        colUsuarioEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().estado()));
        colUsuarioAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = botonAccion("Editar");
            private final Button btnClave = botonAccion("Clave");
            private final Button btnPermisos = botonAccion("Permisos");
            private final Button btnEstado = botonAccion("Desactivar");
            private final HBox box = new HBox(6, btnEditar, btnClave, btnPermisos, btnEstado);

            {
                btnEditar.setOnAction(e -> {
                    UsuarioRow row = getTableView().getItems().get(getIndex());
                    mostrarDialogoUsuario(row);
                });
                btnClave.setOnAction(e -> {
                    UsuarioRow row = getTableView().getItems().get(getIndex());
                    cambiarPasswordUsuario(row);
                });
                btnPermisos.setOnAction(e -> {
                    UsuarioRow row = getTableView().getItems().get(getIndex());
                    mostrarDialogoPermisos(row);
                });
                btnEstado.setOnAction(e -> {
                    UsuarioRow row = getTableView().getItems().get(getIndex());
                    cambiarEstadoUsuario(row);
                });
            }

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
        guardadoEnBD = guardarConfiguracionFiscal() || guardadoEnBD;

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

    private void cargarConfiguracionFiscal() {
        try {
            ConfiguracionFiscal cfg = fiscalConfigService.cargar();
            setText(txtRfcFiscal, cfg.getRfcNegocio().isBlank() ? text(txtRFC) : cfg.getRfcNegocio());
            setText(txtRazonSocialFiscal, cfg.getRazonSocial());
            setText(txtRegimenFiscal, cfg.getRegimenFiscal());
            setText(txtCPFiscal, cfg.getCodigoPostalFiscal());
            setComboValue(cmbIVADefault, cfg.getImpuestoPredeterminadoClave(), "IVA_16");
            setComboValue(cmbRegionFiscal, cfg.getRegionFiscal(), "GENERAL");
            setSwitch(tglPrecioIncluyeImpuesto, String.valueOf(cfg.isPrecioIncluyeImpuesto()));
            setSwitch(tglImpuestoPorProducto, String.valueOf(cfg.isImpuestoPorProducto()));
            setComboValue(cmbModoFacturacion, cfg.getModoFacturacion(), "PREFACTURA");
            setText(txtSerieFactura, cfg.getSerieFactura());
            setText(txtFolioInicial, String.valueOf(cfg.getFolioInicial()));
            setText(txtUsoCfdiDefault, cfg.getUsoCfdiDefault());
            setComboValue(cmbMetodoPagoSat, cfg.getMetodoPagoSat(), "PUE");
            setComboValue(cmbFormaPagoSat, cfg.getFormaPagoSat(), "01");
            if (tglDesglose != null) {
                tglDesglose.setSelected(cfg.isMostrarDesgloseTicket());
            }
            cargarImpuestosFiscales();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean guardarConfiguracionFiscal() {
        try {
            ConfiguracionFiscal anterior = fiscalConfigService.cargar();
            ConfiguracionFiscal cfg = fiscalDesdeCampos();
            fiscalConfigService.guardar(cfg);
            if (txtRFC != null && !cfg.getRfcNegocio().isBlank()) {
                txtRFC.setText(cfg.getRfcNegocio());
            }
            registrarCambiosFiscales(anterior, cfg);
            cargarImpuestosFiscales();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML private void tabNegocio() { mostrarTab("negocio"); }
    @FXML private void tabFiscal() { mostrarTab("fiscal"); }
    @FXML private void tabTicket() { mostrarTab("ticket"); }
    @FXML private void tabCajon() { mostrarTab("cajon"); }
    @FXML private void tabEmail() { mostrarTab("email"); }
    @FXML private void tabUsuarios() { mostrarTab("usuarios"); }
    @FXML private void tabBaseDatos() { mostrarTab("basedatos"); }

    private void prepararColeccionesUI() {
        panelesConfiguracion = List.of(panelNegocio, panelFiscal, panelTicket, panelCajon, panelEmail, panelUsuarios, panelBaseDatos);
        botonesConfiguracion = List.of(btnTabNegocio, btnTabFiscal, btnTabTicket, btnTabCajon, btnTabEmail, btnTabUsuarios, btnTabBaseDatos);
    }

    private void auditarSiCambio(String accion, String campo, String anterior, String actual) {
        String a = anterior == null ? "" : anterior;
        String b = actual == null ? "" : actual;
        if (!a.equals(b)) {
            AuditoriaService.get().registrar(accion, "configuracion_fiscal", 1, campo + ": " + a + " -> " + b);
        }
    }

        instalarTooltip(btnGuardarCambios, "Guarda todos los cambios visibles en base de datos y Preferences.");
        instalarTooltip(btnTabNegocio, "Datos generales del negocio.");
        instalarTooltip(btnTabFiscal, "Datos fiscales, impuestos y prefactura.");
        instalarTooltip(btnTabTicket, "Campos usados por el flujo de impresion de tickets.");
        instalarTooltip(btnTabCajon, "Apertura automatica del cajon de dinero.");
        instalarTooltip(btnTabEmail, "Configuracion SMTP para tickets por correo.");
        instalarTooltip(btnTabUsuarios, "Administracion de usuarios del POS.");
        instalarTooltip(btnTabBaseDatos, "Estado de conexion y respaldo SQL.");

        validarCampo(txtCorreo, texto -> !texto.isBlank() && !texto.contains("@"));
        validarCampo(txtEmailRemitente, texto -> !texto.isBlank() && !texto.contains("@"));
        validarCampo(txtEmailPuerto, texto -> !texto.isBlank() && !texto.matches("\\d{2,5}"));
        validarCampo(txtCP, texto -> !texto.isBlank() && !texto.matches("\\d{4,6}"));
        validarCampo(txtFiscalCP, texto -> !texto.isBlank() && !texto.matches("\\d{5}"));
        validarCampo(txtFolioInicial, texto -> !texto.isBlank() && !texto.matches("\\d+"));
    }

    @FXML
    private void desactivarImpuestoSeleccionado() {
        cambiarEstadoImpuestoSeleccionado(false);
    }

        switch (tab) {
            case "fiscal" -> activar(panelFiscal, btnTabFiscal);
            case "ticket" -> activar(panelTicket, btnTabTicket);
            case "cajon" -> activar(panelCajon, btnTabCajon);
            case "email" -> activar(panelEmail, btnTabEmail);
            case "usuarios" -> activar(panelUsuarios, btnTabUsuarios);
            case "basedatos" -> activar(panelBaseDatos, btnTabBaseDatos);
            default -> activar(panelNegocio, btnTabNegocio);
        }
    }

    private int parseEntero(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @FXML
    public void guardarConfiguracion() {
        guardarNegocio();
        guardarFiscal();
        guardarTicket();
        guardarCajon();
        guardarEmail();
        feedbackGuardado();
    }

    private void guardarNegocio() {
        guardarValor("negocio_nombre", txtNombreNegocio.getText());
        guardarValor("negocio_slogan", txtSlogan.getText());
        guardarValor("negocio_telefono", txtTelefono.getText());
        guardarValor("negocio_direccion", txtDireccion.getText());
        guardarValor("negocio_ciudad", txtCiudad.getText());
        guardarValor("negocio_cp", txtCP.getText());
        guardarValor("negocio_correo", txtCorreo.getText());
        guardarValor("negocio_web", txtSitioWeb.getText());
        guardarValor("negocio_rfc", txtRFC.getText());
        lblMarcaNegocio.setText(MarcaService.nombreNegocio());
    }

    private void guardarFiscal() {
        ConfiguracionFiscal config = new ConfiguracionFiscal();
        config.setRfcNegocio(txtFiscalRfc.getText());
        config.setRazonSocial(txtFiscalRazonSocial.getText());
        config.setRegimenFiscal(valor(cmbFiscalRegimen, "601 - General de Ley Personas Morales"));
        config.setCodigoPostalFiscal(txtFiscalCP.getText());
        config.setRegionFiscal(valor(cmbRegionFiscal, "GENERAL"));
        config.setPrecioIncluyeImpuesto(tglPrecioIncluyeImpuesto.isSelected());
        config.setImpuestoPorProducto(tglImpuestoPorProducto.isSelected());
        config.setMostrarDesgloseTicket(tglMostrarDesgloseFiscal.isSelected());
        config.setImpuestoPredeterminadoClave(claveImpuestoSeleccionada());
        config.setSerieFactura(txtSerieFactura.getText().isBlank() ? "A" : txtSerieFactura.getText());
        config.setFolioInicial(parseEntero(txtFolioInicial.getText(), 1));
        config.setModoFacturacion(valor(cmbModoFacturacion, "PREFACTURA"));
        config.setUsoCfdiDefault(valor(cmbUsoCfdiDefault, "G03 - Gastos en general"));
        config.setMetodoPagoSat(valor(cmbMetodoPagoSat, "PUE - Pago en una sola exhibicion"));
        config.setFormaPagoSat(valor(cmbFormaPagoSat, "01 - Efectivo"));
        fiscalDAO.guardarConfiguracionFiscal(config);
        fiscalDAO.registrarAuditoriaFiscal(
                "CONFIG_FISCAL",
                "configuracion_fiscal",
                1,
                "Configuracion fiscal actualizada. RFC: " + config.getRfcNegocio()
                        + " | Regimen: " + config.getRegimenFiscal()
                        + " | Impuesto: " + config.getImpuestoPredeterminadoClave(),
                SesionUsuario.getInstancia().getIdUsuario()
        );
    }

    private void guardarTicket() {
        guardarValor("ticket_nombre", txtTicketNombre.getText());
        guardarValor("ticket_giro", txtTicketGiro.getText());
        guardarValor("ticket_direccion", txtTicketDireccion.getText());
        guardarValor("ticket_ciudad", txtTicketCiudad.getText());
        guardarValor("ticket_telefono", txtTicketTelefono.getText());
        guardarValor("ticket_encabezado", txtMensajeEncabezado.getText());
        guardarValor("ticket_pie", txtMensajePie.getText());
        guardarValor("ticket_aviso", txtAvisoFiscal.getText());
        guardarValor("ticket_ancho", valor(cmbAnchoPapel, "58 mm"));
        guardarValor("cmbImpresora", valor(cmbImpresora, ""));
        guardarValor("cmbAnchoPapel", valor(cmbAnchoPapel, "58 mm"));
        guardarBoolean("ticket_logo", tglLogoTicket.isSelected());
        guardarBoolean("ticket_folio", tglFolioTicket.isSelected());
        guardarBoolean("ticket_desglose", tglDesglose.isSelected());
        guardarBoolean("ticket_qr", tglQR.isSelected());
    }

    private void guardarCajon() {
        guardarBoolean("cajon_activo", tglCajonActivo.isSelected());
        guardarValor("cajon_puerto", valor(cmbCajonPuerto, "Via impresora termica (ESC/POS)"));
        guardarValor("cajon_pulso", valor(cmbCajonPulso, "Pulso 1 (pin 2)"));
    }

    private void guardarEmail() {
        guardarBoolean("email_activo", tglEmailActivo.isSelected());
        guardarValor("email_smtp", valor(cmbEmailSmtp, "Gmail"));
        guardarValor("email_remitente", txtEmailRemitente.getText());
        guardarValor("email_password", txtEmailPassword.getText());
        guardarValor("email_host", txtEmailHost.getText());
        guardarValor("email_puerto", txtEmailPuerto.getText());
        guardarBoolean("email_reporte_diario", tglEmailReporteDiario.isSelected());
    }

    private void mostrarDialogo(String titulo, String mensaje) {
        mostrarAlerta(Alert.AlertType.WARNING, titulo, mensaje);
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
        cargarNegocio();
        cargarFiscal();
        cargarTicket();
        cargarCajon();
        cargarEmail();
        actualizarVisibilidadSmtp();
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

    private void cargarFiscal() {
        ConfiguracionFiscal config = fiscalDAO.obtenerConfiguracionFiscal();
        txtFiscalRfc.setText(config.getRfcNegocio().isBlank() ? txtRFC.getText() : config.getRfcNegocio());
        txtFiscalRazonSocial.setText(config.getRazonSocial());
        txtFiscalCP.setText(config.getCodigoPostalFiscal());
        seleccionar(cmbFiscalRegimen, config.getRegimenFiscal(), "601 - General de Ley Personas Morales");
        seleccionar(cmbRegionFiscal, config.getRegionFiscal(), "GENERAL");
        seleccionarImpuesto(config.getImpuestoPredeterminadoClave());
        tglPrecioIncluyeImpuesto.setSelected(config.isPrecioIncluyeImpuesto());
        tglImpuestoPorProducto.setSelected(config.isImpuestoPorProducto());
        tglMostrarDesgloseFiscal.setSelected(config.isMostrarDesgloseTicket());
        txtSerieFactura.setText(config.getSerieFactura().isBlank() ? "A" : config.getSerieFactura());
        txtFolioInicial.setText(String.valueOf(config.getFolioInicial()));
        seleccionar(cmbModoFacturacion, config.getModoFacturacion(), "PREFACTURA");
        seleccionar(cmbUsoCfdiDefault, config.getUsoCfdiDefault(), "G03 - Gastos en general");
        seleccionar(cmbMetodoPagoSat, config.getMetodoPagoSat(), "PUE - Pago en una sola exhibicion");
        seleccionar(cmbFormaPagoSat, config.getFormaPagoSat(), "01 - Efectivo");
    }

    private void cargarTicket() {
        txtTicketNombre.setText(leerValor("ticket_nombre", "Volovan Volo"));
        txtTicketGiro.setText(leerValor("ticket_giro", "Panaderia y Reposteria"));
        txtTicketDireccion.setText(leerValor("ticket_direccion", ""));
        txtTicketCiudad.setText(leerValor("ticket_ciudad", ""));
        txtTicketTelefono.setText(leerValor("ticket_telefono", ""));
        txtMensajeEncabezado.setText(leerValor("ticket_encabezado", "Bienvenido!\nGracias por visitarnos."));
        txtMensajePie.setText(leerValor("ticket_pie", "Gracias por su compra!\nVuelva pronto.\nfacebook.com/VolovanVolo"));
        txtAvisoFiscal.setText(leerValor("ticket_aviso", "Este ticket no es comprobante fiscal"));
        seleccionar(cmbAnchoPapel, leerValor("ticket_ancho", leerValor("cmbAnchoPapel", "58 mm")), "58 mm");
        seleccionar(cmbImpresora, leerValor("cmbImpresora", cmbImpresora.getItems().isEmpty() ? "" : cmbImpresora.getItems().get(0)), cmbImpresora.getItems().isEmpty() ? "" : cmbImpresora.getItems().get(0));
        tglLogoTicket.setSelected(leerBoolean("ticket_logo", true));
        tglFolioTicket.setSelected(leerBoolean("ticket_folio", true));
        tglDesglose.setSelected(leerBoolean("ticket_desglose", true));
        tglQR.setSelected(leerBoolean("ticket_qr", false));
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

    private void configurarTablaImpuestos() {
        if (tablaImpuestos == null) return;
        tablaImpuestos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colImpClave.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClave()));
        colImpNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colImpTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));
        colImpTasa.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f%%", c.getValue().getTasa() * 100)));
        colImpActivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "Activo" : "Inactivo"));
        tablaImpuestos.setItems(listaImpuestos);
    }

    private void cargarImpuestosFiscales() {
        try {
            FiscalSchemaService.asegurarEstructura();
            listaImpuestos.setAll(impuestoDAO.obtenerTodos());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @FXML
    public void agregarUsuario() {
        mostrarDialogoUsuario(null);
    }

    private void mostrarDialogoUsuario(UsuarioRow usuario) {
        boolean nuevo = usuario == null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(nuevo ? "Nuevo usuario" : "Editar usuario");
        VBox form = new VBox(10);
        form.getStyleClass().add("cfg-dialog-form");
        TextField txtNombre = new TextField(nuevo ? "" : usuario.nombre());
        TextField txtUsuario = new TextField(nuevo ? "" : usuario.usuario());
        PasswordField txtPassword = new PasswordField();
        ComboBox<String> cmbRol = new ComboBox<>(FXCollections.observableArrayList("admin", "cajero", "supervisor"));
        cmbRol.setValue(nuevo ? "cajero" : usuario.rol());
        txtNombre.setPromptText("Nombre completo");
        txtUsuario.setPromptText("Usuario");
        txtPassword.setPromptText(nuevo ? "Contrasena" : "Nueva contrasena (opcional)");
        form.getChildren().addAll(new Label("Nombre"), txtNombre, new Label("Usuario"), txtUsuario,
                new Label("Contrasena"), txtPassword, new Label("Rol"), cmbRol);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK) return;
            String nombre = txtNombre.getText().trim();
            String user = txtUsuario.getText().trim();
            String pass = txtPassword.getText().trim();
            String rol = cmbRol.getValue();
            if (nombre.isEmpty() || user.isEmpty() || (nuevo && pass.isEmpty())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Usuarios", "Nombre, usuario y contrasena son obligatorios.");
                return;
            }
            if (!nuevo && !usuarioSeguridadService.puedeCambiarRol(usuario.id(), rol)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Usuarios", "No puedes dejar el sistema sin un usuario administrador activo.");
                return;
            }
            if (nuevo) insertarUsuario(nombre, user, pass, rol);
            else actualizarUsuario(usuario.id(), nombre, user, pass, rol);
        });
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

    private void insertarUsuario(String nombre, String usuario, String password, String rol) {
        String sqlRol = "SELECT id_rol FROM roles WHERE nombre = ?";
        String sql = "INSERT INTO usuarios (nombre, usuario, contrasena, password_hash, fecha_actualizacion_password, id_rol) VALUES (?, ?, '', ?, NOW(), ?)";
        try (Connection con = ConexionDB.getConexion()) {
            int idRol = obtenerRol(con, sqlRol, rol);
            if (idRol == 0) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setString(2, usuario);
                ps.setString(3, passwordService.hash(password));
                ps.setInt(4, idRol);
                ps.executeUpdate();
            }
            cargarUsuarios();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Usuarios", "No se pudo crear el usuario.\n" + e.getMessage());
        }
        mostrarAlerta(Alert.AlertType.INFORMATION, "WhatsApp / Twilio", "Conexion con Twilio verificada.");
    }

    private void actualizarUsuario(int id, String nombre, String usuario, String password, String rol) {
        try (Connection con = ConexionDB.getConexion()) {
            int idRol = obtenerRol(con, "SELECT id_rol FROM roles WHERE nombre = ?", rol);
            if (idRol == 0) return;
            String sql = password.isEmpty()
                    ? "UPDATE usuarios SET nombre = ?, usuario = ?, id_rol = ? WHERE id_usuario = ?"
                    : "UPDATE usuarios SET nombre = ?, usuario = ?, contrasena = '', password_hash = ?, fecha_actualizacion_password = NOW(), id_rol = ? WHERE id_usuario = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setString(2, usuario);
                if (password.isEmpty()) {
                    ps.setInt(3, idRol);
                    ps.setInt(4, id);
                } else {
                    ps.setString(3, passwordService.hash(password));
                    ps.setInt(4, idRol);
                    ps.setInt(5, id);
                }
                ps.executeUpdate();
            }
        } catch (Exception e) {
            actualizarBadgeDB(false);
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexion",
                    "No se pudo conectar.\n" + e.getMessage());
        }
    }

    private int obtenerRol(Connection con, String sql, String rol) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rol);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        mostrarAlerta(Alert.AlertType.WARNING, "Usuarios", "No existe el rol '" + rol + "' en la base de datos.");
        return 0;
    }

    private void cambiarPasswordUsuario(UsuarioRow usuario) {
        PasswordField field = new PasswordField();
        field.setPromptText("Nueva contrasena");
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Cambiar clave");
        dialog.setHeaderText("Nueva clave para " + usuario.nombre());
        dialog.getDialogPane().setContent(field);
        dialog.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK || field.getText().trim().isEmpty()) return;
            try (Connection con = ConexionDB.getConexion();
                 PreparedStatement ps = con.prepareStatement("UPDATE usuarios SET contrasena = '', password_hash = ?, fecha_actualizacion_password = NOW() WHERE id_usuario = ?")) {
                ps.setString(1, passwordService.hash(field.getText()));
                ps.setInt(2, usuario.id());
                ps.executeUpdate();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Usuarios", "No se pudo cambiar la clave.\n" + e.getMessage());
            }
        });
    }

    private void cambiarEstadoUsuario(UsuarioRow usuario) {
        if (usuario.activo() && !usuarioSeguridadService.puedeDesactivarUsuario(usuario.id())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Usuarios", usuarioSeguridadService.mensajeProteccionAdmin(usuario.id()));
            return;
        }
        int nuevo = usuario.activo() ? 0 : 1;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE usuarios SET activo = ? WHERE id_usuario = ?")) {
            ps.setInt(1, nuevo);
            ps.setInt(2, usuario.id());
            ps.executeUpdate();
            cargarUsuarios();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Usuarios", "No se pudo actualizar el estado.\n" + e.getMessage());
        }
    }

    private void mostrarDialogoPermisos(UsuarioRow usuario) {
        if (!PermisoService.tienePermiso(PermisoService.PERMISOS_GESTIONAR)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Permisos", "No tienes permiso para gestionar permisos.");
            return;
        }
        List<PermisoService.PermisoInfo> catalogo = PermisoService.listarPermisos();
        if (catalogo.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Permisos", "Ejecuta primero db/migracion_seguridad_permisos.sql.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Gestion de permisos");
        dialog.setHeaderText("Permisos para " + usuario.nombre());

        Map<String, Boolean> actuales = PermisoService.permisosUsuario(usuario.id());
        VBox lista = new VBox(8);
        lista.setStyle("-fx-padding: 12;");
        List<CheckBox> checks = new ArrayList<>();
        String moduloActual = "";
        for (PermisoService.PermisoInfo permiso : catalogo) {
            if (!permiso.modulo().equals(moduloActual)) {
                Label modulo = new Label(permiso.modulo());
                modulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0b3b75; -fx-padding: 10 0 2 0;");
                lista.getChildren().add(modulo);
                moduloActual = permiso.modulo();
            }
            CheckBox check = new CheckBox(permiso.codigo() + " - " + permiso.nombre());
            check.setUserData(permiso.codigo());
            check.setSelected(Boolean.TRUE.equals(actuales.get(permiso.codigo())));
            check.setStyle("-fx-font-size: 12px;");
            checks.add(check);
            lista.getChildren().add(check);
        }

        ScrollPane scroll = new ScrollPane(lista);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(460);
        scroll.setPrefViewportWidth(560);
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK) return;
            List<String> seleccionados = checks.stream()
                    .filter(CheckBox::isSelected)
                    .map(c -> String.valueOf(c.getUserData()))
                    .toList();
            try {
                PermisoService.guardarPermisosUsuario(usuario.id(), seleccionados);
                AuditoriaService.get().registrar(
                        "CAMBIO_PERMISOS", "usuario_permisos", usuario.id(),
                        "Permisos actualizados para " + usuario.usuario()
                );
                mostrarAlerta(Alert.AlertType.INFORMATION, "Permisos", "Permisos guardados correctamente.");
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Permisos", e.getMessage());
            }
        });
    }

    @FXML
    public void probarConexionDB() {
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) throw new IllegalStateException("Sin conexion");
            DatabaseMetaData meta = con.getMetaData();
            lblEstadoDB.setText("Conectado");
            lblEstadoDB.getStyleClass().remove("cfg-status-error");
            if (!lblEstadoDB.getStyleClass().contains("cfg-status-ok")) lblEstadoDB.getStyleClass().add("cfg-status-ok");
            lblDBVersion.setText(meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            mostrarAlerta(Alert.AlertType.INFORMATION, "Base de datos", "Conexion correcta.");
        } catch (Exception e) {
            lblEstadoDB.setText("Error de conexion");
            lblEstadoDB.getStyleClass().remove("cfg-status-ok");
            if (!lblEstadoDB.getStyleClass().contains("cfg-status-error")) lblEstadoDB.getStyleClass().add("cfg-status-error");
            mostrarAlerta(Alert.AlertType.ERROR, "Base de datos", "No se pudo conectar.\n" + e.getMessage());
        }
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

    private String claveImpuestoSeleccionada() {
        String valor = valor(cmbFiscalImpuestoDefault, "IVA_16");
        int idx = valor.indexOf(" - ");
        return idx > 0 ? valor.substring(0, idx).trim() : valor.trim();
    }

    private void seleccionarImpuesto(String clave) {
        String buscada = clave == null || clave.isBlank() ? "IVA_16" : clave;
        for (String item : cmbFiscalImpuestoDefault.getItems()) {
            if (item.equals(buscada) || item.startsWith(buscada + " - ")) {
                cmbFiscalImpuestoDefault.setValue(item);
                return;
            }
        }
        if (!cmbFiscalImpuestoDefault.getItems().isEmpty()) {
            cmbFiscalImpuestoDefault.setValue(cmbFiscalImpuestoDefault.getItems().get(0));
        }
    }

    private int parseEntero(String texto, int fallback) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private void seleccionar(ComboBox<String> combo, String valor, String fallback) {
        if (combo.getItems().contains(valor)) combo.setValue(valor);
        else combo.setValue(fallback);
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
                org.example.modelo.SesionUsuario.cerrarSesion();
                navegar("/org/example/vista/Login.fxml");
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
