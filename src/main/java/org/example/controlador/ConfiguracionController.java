package org.example.controlador;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
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

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

public class ConfiguracionController {

    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;
    @FXML private Label lblMarcaNegocio;
    @FXML private Label lblFeedbackGuardado;

    @FXML private VBox panelNegocio;
    @FXML private VBox panelFiscal;
    @FXML private VBox panelTicket;
    @FXML private VBox panelCajon;
    @FXML private VBox panelEmail;
    @FXML private VBox panelUsuarios;
    @FXML private VBox panelBaseDatos;

    @FXML private Button btnTabNegocio;
    @FXML private Button btnTabFiscal;
    @FXML private Button btnTabTicket;
    @FXML private Button btnTabCajon;
    @FXML private Button btnTabEmail;
    @FXML private Button btnTabUsuarios;
    @FXML private Button btnTabBaseDatos;
    @FXML private Button btnGuardarCambios;

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
        prepararColeccionesUI();
        cargarSesion();
        prepararCombos();
        prepararUsuarios();
        prepararInteracciones();
        cargarConfiguracion();
        cargarUsuarios();
        cargarInfoBaseDatos();
        mostrarTab("negocio");
    }

    private void cargarSesion() {
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
        if (cmbImpresora.getItems().isEmpty()) {
            cmbImpresora.getItems().add("Impresora predeterminada");
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

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                UsuarioRow row = getTableView().getItems().get(getIndex());
                btnEstado.setText(row.activo() ? "Desactivar" : "Activar");
                setGraphic(box);
            }
        });
        tablaUsuarios.setItems(usuarios);
        txtBuscarUsuario.textProperty().addListener((obs, old, value) -> cargarUsuarios());
    }

    private Button botonAccion(String texto) {
        Button btn = new Button(texto);
        btn.getStyleClass().add("cfg-action-button");
        return btn;
    }

    private void instalarTooltip(Control control, String texto) {
        Tooltip tooltip = new Tooltip(texto);
        tooltip.setShowDelay(Duration.millis(250));
        control.setTooltip(tooltip);
    }

    private void validarCampo(TextField campo, java.util.function.Predicate<String> invalido) {
        campo.textProperty().addListener((obs, old, value) -> marcarCampoInvalido(campo, invalido.test(value == null ? "" : value.trim())));
        marcarCampoInvalido(campo, invalido.test(campo.getText() == null ? "" : campo.getText().trim()));
    }

    private void marcarCampoInvalido(Control control, boolean invalido) {
        if (invalido) {
            if (!control.getStyleClass().contains("cfg-field-invalid")) {
                control.getStyleClass().add("cfg-field-invalid");
            }
        } else {
            control.getStyleClass().remove("cfg-field-invalid");
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

    private void prepararInteracciones() {
        cmbEmailSmtp.valueProperty().addListener((obs, old, value) -> actualizarVisibilidadSmtp());

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

    private void mostrarTab(String tab) {
        panelesConfiguracion.forEach(panel -> {
            panel.setManaged(false);
            panel.setVisible(false);
        });
        botonesConfiguracion.forEach(boton -> boton.getStyleClass().remove("cfg-tab-active"));

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

    private void activar(VBox panel, Button boton) {
        panel.setManaged(true);
        panel.setVisible(true);
        if (!boton.getStyleClass().contains("cfg-tab-active")) {
            boton.getStyleClass().add("cfg-tab-active");
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

    private void cargarConfiguracion() {
        cargarNegocio();
        cargarFiscal();
        cargarTicket();
        cargarCajon();
        cargarEmail();
        actualizarVisibilidadSmtp();
    }

    private void cargarNegocio() {
        txtNombreNegocio.setText(leerValor("negocio_nombre", ""));
        txtSlogan.setText(leerValor("negocio_slogan", ""));
        txtTelefono.setText(leerValor("negocio_telefono", ""));
        txtDireccion.setText(leerValor("negocio_direccion", ""));
        txtCiudad.setText(leerValor("negocio_ciudad", ""));
        txtCP.setText(leerValor("negocio_cp", ""));
        txtCorreo.setText(leerValor("negocio_correo", ""));
        txtSitioWeb.setText(leerValor("negocio_web", ""));
        txtRFC.setText(leerValor("negocio_rfc", ""));
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

    private void cargarCajon() {
        tglCajonActivo.setSelected(leerBoolean("cajon_activo", false));
        seleccionar(cmbCajonPuerto, leerValor("cajon_puerto", "Via impresora termica (ESC/POS)"), "Via impresora termica (ESC/POS)");
        seleccionar(cmbCajonPulso, leerValor("cajon_pulso", "Pulso 1 (pin 2)"), "Pulso 1 (pin 2)");
    }

    private void cargarEmail() {
        tglEmailActivo.setSelected(leerBoolean("email_activo", false));
        seleccionar(cmbEmailSmtp, leerValor("email_smtp", "Gmail"), "Gmail");
        txtEmailRemitente.setText(leerValor("email_remitente", ""));
        txtEmailPassword.setText(leerValor("email_password", ""));
        txtEmailHost.setText(leerValor("email_host", ""));
        txtEmailPuerto.setText(leerValor("email_puerto", ""));
        tglEmailReporteDiario.setSelected(leerBoolean("email_reporte_diario", false));
    }

    private void guardarValor(String clave, String valor) {
        String limpio = valor == null ? "" : valor.trim();
        prefs.put(clave, limpio);
        dbSet(clave, limpio);
    }

    private void guardarBoolean(String clave, boolean valor) {
        prefs.putBoolean(clave, valor);
        dbSet(clave, Boolean.toString(valor));
    }

    private String leerValor(String clave, String fallback) {
        return dbGet(clave, prefs.get(clave, fallback));
    }

    private boolean leerBoolean(String clave, boolean fallback) {
        return Boolean.parseBoolean(dbGet(clave, Boolean.toString(prefs.getBoolean(clave, fallback))));
    }

    private String dbGet(String clave, String fallback) {
        String sql = "SELECT valor FROM configuracion WHERE clave = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private void dbSet(String clave, String valor) {
        String sql = "INSERT INTO configuracion (clave, valor) VALUES (?, ?) ON DUPLICATE KEY UPDATE valor = VALUES(valor)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, valor);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    @FXML
    public void abrirVistaPrevia() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/TicketPreview.fxml"));
            Parent root = loader.load();
            MarcaService.aplicar(root);
            TicketPreviewController ctrl = loader.getController();
            int ancho = "58 mm".equals(cmbAnchoPapel.getValue()) ? TicketRenderer.ANCHO_58MM : TicketRenderer.ANCHO_80MM;
            ctrl.configurar(ticketMuestra(),
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
                    true,
                    true,
                    ancho);
            Stage stage = new Stage();
            stage.setTitle("Vista previa - Ticket");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.initOwner(lblNombreUsuario.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Vista previa", "No se pudo abrir la vista previa.\n" + e.getMessage());
        }
    }

    private Ticket ticketMuestra() {
        return new Ticket(1234,
                LocalDateTime.now(),
                SesionUsuario.getInstancia().getNombre(),
                List.of(
                        new Ticket.LineaTicket("Croissant mantequilla", 2, 24.00),
                        new Ticket.LineaTicket("Pan de chocolate", 1, 22.00),
                        new Ticket.LineaTicket("Concha vainilla", 3, 12.00),
                        new Ticket.LineaTicket("Cafe americano", 1, 35.00)
                ),
                141.00, 200.00, 59.00, 1);
    }

    private void actualizarVisibilidadSmtp() {
        boolean personalizado = "SMTP personalizado".equals(cmbEmailSmtp.getValue());
        boxSmtpPersonalizado.setVisible(personalizado);
        boxSmtpPersonalizado.setManaged(personalizado);
        if (!personalizado) {
            String provider = valor(cmbEmailSmtp, "Gmail");
            if ("Outlook / Hotmail".equals(provider)) {
                txtEmailHost.setText("smtp.office365.com");
                txtEmailPuerto.setText("587");
            } else {
                txtEmailHost.setText("smtp.gmail.com");
                txtEmailPuerto.setText("587");
            }
        }
    }

    @FXML
    public void probarCorreo() {
        String remitente = txtEmailRemitente.getText().trim();
        String password = txtEmailPassword.getText();
        String host = txtEmailHost.getText().trim();
        String puerto = txtEmailPuerto.getText().trim();
        if (remitente.isEmpty() || password.isEmpty() || host.isEmpty() || puerto.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Correo", "Completa remitente, password, servidor y puerto.");
            return;
        }
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", puerto);
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(remitente, password);
                }
            });
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(remitente));
            message.setSubject("Prueba de correo POS");
            message.setText("La configuracion de correo del POS funciona correctamente.");
            Transport.send(message);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Correo", "Correo de prueba enviado a " + remitente + ".");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Correo", "No se pudo enviar el correo de prueba.\n" + e.getMessage());
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

    private void cargarUsuarios() {
        usuarios.clear();
        String filtro = txtBuscarUsuario == null || txtBuscarUsuario.getText() == null ? "" : txtBuscarUsuario.getText().trim();
        String sql = """
                SELECT u.id_usuario, u.nombre, u.usuario, r.nombre AS rol, u.activo
                FROM usuarios u
                JOIN roles r ON u.id_rol = r.id_rol
                WHERE u.nombre LIKE ? OR u.usuario LIKE ? OR r.nombre LIKE ?
                ORDER BY u.activo DESC, u.nombre
                """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + filtro + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                boolean activo = rs.getInt("activo") == 1;
                usuarios.add(new UsuarioRow(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("usuario"),
                        rs.getString("rol"),
                        activo ? "Activo" : "Inactivo",
                        activo));
            }
        } catch (Exception ignored) {
        }
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
            cargarUsuarios();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Usuarios", "No se pudo actualizar el usuario.\n" + e.getMessage());
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

    private void cargarInfoBaseDatos() {
        lblDBHost.setText("localhost:3306");
        lblDBNombre.setText("pospanaderia");
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) throw new IllegalStateException("Sin conexion");
            DatabaseMetaData meta = con.getMetaData();
            lblEstadoDB.setText("Conectado");
            lblDBVersion.setText(meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
        } catch (Exception e) {
            lblEstadoDB.setText("Sin conexion");
            lblDBVersion.setText("No disponible");
        }
    }

    @FXML
    public void seleccionarCarpetaRespaldo() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta para respaldo");
        File carpeta = chooser.showDialog(lblNombreUsuario.getScene().getWindow());
        if (carpeta != null) txtRutaRespaldo.setText(carpeta.getAbsolutePath());
    }

    @FXML
    public void exportarRespaldo() {
        String carpeta = txtRutaRespaldo.getText().trim();
        if (carpeta.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Respaldo", "Selecciona una carpeta de destino.");
            return;
        }
        File destino = new File(carpeta, "pospanaderia_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql");
        ProcessBuilder pb = new ProcessBuilder("mysqldump", "-h", "localhost", "-u", "root", "pospanaderia");
        pb.redirectOutput(destino);
        pb.redirectErrorStream(true);
        try {
            int code = pb.start().waitFor();
            if (code == 0) mostrarAlerta(Alert.AlertType.INFORMATION, "Respaldo", "Respaldo exportado:\n" + destino.getAbsolutePath());
            else mostrarAlerta(Alert.AlertType.ERROR, "Respaldo", "mysqldump termino con codigo " + code + ".");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Respaldo", "No se pudo exportar el respaldo.\n" + e.getMessage());
        }
    }

    private void feedbackGuardado() {
        if (feedbackFade != null) {
            feedbackFade.stop();
        }
        lblFeedbackGuardado.setOpacity(1);
        lblFeedbackGuardado.setText("Cambios guardados");
        if (!btnGuardarCambios.getStyleClass().contains("primary-button-success")) {
            btnGuardarCambios.getStyleClass().add("primary-button-success");
        }
        feedbackFade = new FadeTransition(Duration.millis(1800), lblFeedbackGuardado);
        feedbackFade.setFromValue(1);
        feedbackFade.setToValue(0);
        feedbackFade.setOnFinished(e -> btnGuardarCambios.getStyleClass().remove("primary-button-success"));
        feedbackFade.play();
    }

    private String valor(ComboBox<String> combo, String fallback) {
        return combo.getValue() == null ? fallback : combo.getValue();
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

    private String iniciales(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) return "US";
        String[] partes = limpio.split("\\s+");
        if (partes.length > 1) return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
        return limpio.substring(0, Math.min(2, limpio.length())).toUpperCase();
    }

    private void registrarLogout() {
        String sql = "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, detalle) VALUES (?, 'LOGOUT', 'usuarios', ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int idUsuario = SesionUsuario.getInstancia().getIdUsuario();
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setString(3, "Cierre de sesion: " + SesionUsuario.getInstancia().getNombre());
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    @FXML private void irADashboard() { navegar("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML private void irAVentas() { navegar("/org/example/vista/Ventas.fxml"); }
    @FXML private void irAInventario() { navegar("/org/example/vista/Inventario.fxml"); }
    @FXML private void irAEmpleados() { navegar("/org/example/vista/Empleados.fxml"); }
    @FXML private void irAClientes() { navegar("/org/example/vista/Clientes.fxml"); }
    @FXML private void irAReportes() { navegar("/org/example/vista/Reportes.fxml"); }
    @FXML private void irAAuditoria() { navegar("/org/example/vista/Auditoria.fxml"); }
    @FXML private void irACorteCaja() { navegar("/org/example/vista/CorteCaja.fxml"); }

    @FXML
    public void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Cambiar sesion");
        a.setHeaderText(null);
        a.setContentText("Seguro que deseas cambiar de sesion?");
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
            Parent root = FXMLLoader.load(getClass().getResource(ruta));
            MarcaService.aplicar(root);
            Stage stage = (Stage) lblNombreUsuario.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Navegacion", "No se pudo abrir la vista.\n" + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    private record UsuarioRow(int id, String nombre, String usuario, String rol, String estado, boolean activo) {}
}
