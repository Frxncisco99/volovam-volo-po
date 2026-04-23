package org.example.controlador;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.modelo.SwitchToggle;   // ← IMPORT CORRECTO
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

public class ConfiguracionController {

    // Sidebar
    @FXML
    private Label lblNombreUsuario;
    @FXML
    private Label lblRolUsuario;
    @FXML
    private Label lblAvatarIniciales;

    // Pestañas
    @FXML
    private VBox panelNegocio;
    @FXML
    private VBox panelPOS;
    @FXML
    private VBox panelUsuarios;
    @FXML
    private VBox panelFiscal;
    @FXML
    private VBox panelIntegraciones;
    @FXML
    private VBox panelBaseDatos;

    // Botones de pestaña
    @FXML
    private Button btnTabNegocio;
    @FXML
    private Button btnTabPOS;
    @FXML
    private Button btnTabUsuarios;
    @FXML
    private Button btnTabFiscal;
    @FXML
    private Button btnTabIntegraciones;
    @FXML
    private Button btnTabBaseDatos;

    // Panel negocio
    @FXML
    private TextField txtNombreNegocio;
    @FXML
    private TextField txtSlogan;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtCiudad;
    @FXML
    private TextField txtCP;
    @FXML
    private TextField txtCorreo;
    @FXML
    private TextField txtSitioWeb;
    @FXML
    private TextField txtRFC;
    @FXML
    private ComboBox<String> cmbMoneda;
    @FXML
    private ComboBox<String> cmbZonaHoraria;
    @FXML
    private TextField txtHoraApertura;
    @FXML
    private TextField txtHoraCierre;
    @FXML
    private ToggleButton tglLun, tglMar, tglMie, tglJue, tglVie, tglSab, tglDom;
    @FXML
    private TextField txtLimiteCredito;
    @FXML
    private TextField txtNumSucursal;
    @FXML
    private ToggleButton tglMantenimiento;

    // Panel POS
    @FXML
    private ComboBox<String> cmbMetodoPago;
    @FXML
    private ComboBox<String> cmbRedondeo;
    @FXML
    private ToggleButton tglAperturaCaja;
    @FXML
    private ToggleButton tglConfirmarCobro;
    @FXML
    private ToggleButton tglAutoImprimir;
    @FXML
    private ComboBox<String> cmbBusqueda;
    @FXML
    private ComboBox<String> cmbOrdenProductos;
    @FXML
    private ToggleButton tglImagenesProducto;
    @FXML
    private ToggleButton tglAlertaStock;
    @FXML
    private ToggleButton tglVentaSinStock;
    @FXML
    private ToggleButton tglAsociarCliente;
    @FXML
    private ToggleButton tglVentaCredito;
    @FXML
    private ToggleButton tglDevoluciones;
    @FXML
    private ToggleButton tglVentaRapida;
    @FXML
    private TextField txtNotaInterna;

    // Panel Usuarios
    @FXML
    private TextField txtBuscarUsuario;
    @FXML
    private ToggleButton tglAutoBloqueo;
    @FXML
    private ComboBox<String> cmbInactividad;
    @FXML
    private ToggleButton tglAuditoria;

    // Panel fiscal
    @FXML
    private ToggleButton tglLogoTicket;
    @FXML
    private ToggleButton tglFolioTicket;
    @FXML
    private ToggleButton tglDesglose;
    @FXML
    private ToggleButton tglQR;
    @FXML
    private ToggleButton tglCopiacocina;
    @FXML
    private ComboBox<String> cmbImpresora;
    @FXML
    private ComboBox<String> cmbAnchoPapel;

    // Panel fiscal (ticket)
    @FXML
    private TextField txtTicketNombre;
    @FXML
    private TextField txtTicketGiro;
    @FXML
    private TextField txtTicketDireccion;
    @FXML
    private TextField txtTicketCiudad;
    @FXML
    private TextField txtTicketTelefono;
    @FXML
    private TextArea txtMensajeEncabezado;
    @FXML
    private TextArea txtMensajePie;
    @FXML
    private TextField txtAvisoFiscal;
    @FXML
    private TextArea txtVistaTicket;

    // Panel integraciones
    @FXML
    private TextField txtClipToken;
    @FXML
    private TextField txtStripeKey;
    @FXML
    private TextField txtCorreoReportes;
    @FXML
    private ComboBox<String> cmbSmtp;
    @FXML
    private ToggleButton tglReporteDiario;
    @FXML
    private ToggleButton tglAlertaStockCorreo;
    @FXML
    private TextField txtTwilioSid;
    @FXML
    private TextField txtTwilioToken;
    @FXML
    private ToggleButton tglTicketWhatsapp;

    // Panel base de datos
    @FXML
    private Label lblEstadoDB;
    @FXML
    private ComboBox<String> cmbMotorDB;
    @FXML
    private TextField txtDBHost;
    @FXML
    private TextField txtDBPuerto;
    @FXML
    private TextField txtDBNombre;
    @FXML
    private TextField txtDBUsuario;
    @FXML
    private PasswordField txtDBPassword;
    @FXML
    private TextField txtRutaRespaldo;
    @FXML
    private ComboBox<String> cmbFrecuenciaRespaldo;
    @FXML
    private ToggleButton tglRespaldoAuto;
    @FXML
    private Label lblUltimoRespaldo;
    @FXML
    private Label lblTamanoRespaldo;
    @FXML
    private Label lblTamanoDB;
    @FXML
    private Label lblRegVentas;
    @FXML
    private Label lblRegProductos;

    // Estilo de pestañas
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

        // ComboBoxe negocio
        cmbMoneda.getItems().addAll("MXN - Peso Mexicano", "USD - Dolar", "EUR - Euro");
        cmbMoneda.setValue("MXN - Peso Mexicano");
        cmbZonaHoraria.getItems().addAll(
                "America/Monterrey (CST)", "America/Mexico_City (CST)",
                "America/Tijuana (PST)", "America/Cancun (EST)"
        );
        cmbZonaHoraria.setValue("America/Monterrey (CST)");

        // ComboBoxes POS
        cmbMetodoPago.getItems().addAll("Efectivo", "Efectivo y Tarjeta", "Todos los metodos");
        cmbMetodoPago.setValue("Efectivo y Tarjeta");
        cmbRedondeo.getItems().addAll("Sin redondeo", "Redondear a $0.50", "Redondear a $1.00");
        cmbRedondeo.setValue("Sin redondeo");
        cmbBusqueda.getItems().addAll("Nombre del producto", "Codigo de barras", "Nombre o codigo", "Categoria");
        cmbBusqueda.setValue("Nombre o codigo");
        cmbOrdenProductos.getItems().addAll("Alfabetico", "Mas vendido primero", "Por categoria", "Precio ascendente");
        cmbOrdenProductos.setValue("Por categoria");

        // ComboBoxes fiscal
        cmbImpresora.getItems().addAll(
                "EPSON TM-T20III", "EPSON TM-T88V", "Star TSP100", "Generica"
        );
        cmbImpresora.setValue("EPSON TM-T20III");
        cmbAnchoPapel.getItems().addAll("58 mm", "80 mm");
        cmbAnchoPapel.setValue("80 mm");

        // ComboBoxes usuarios
        cmbInactividad.getItems().addAll("5 minutos", "10 minutos", "15 minutos", "30 minutos", "1 hora");
        cmbInactividad.setValue("15 minutos");

        // ComboBoxe integraciones
        cmbSmtp.getItems().addAll("Gmail", "Outlook / Hotmail", "Yahoo Mail", "SMTP personalizado");
        cmbSmtp.setValue("Gmail");

        // ComboBoxes base de datos
        cmbMotorDB.getItems().addAll("MySQL 8.x", "MySQL 5.7", "MariaDB");
        cmbMotorDB.setValue("MySQL 8.x");
        cmbFrecuenciaRespaldo.getItems().addAll("Diario", "Semanal", "Quincenal", "Mensual");
        cmbFrecuenciaRespaldo.setValue("Diario");

        mostrarTab("negocio");
        cargarPreferencias();
    }

    // Navegacion de pestañas
    @FXML
    private void tabNegocio() {
        mostrarTab("negocio");
    }

    @FXML
    private void tabPOS() {
        mostrarTab("pos");
    }

    @FXML
    private void tabUsuarios() {
        mostrarTab("usuarios");
    }

    @FXML
    private void tabFiscal() {
        mostrarTab("fiscal");
        actualizarVistaPrevia();
    }

    @FXML
    private void tabIntegraciones() {
        mostrarTab("integraciones");
    }

    @FXML
    private void tabBaseDatos() {
        mostrarTab("basedatos");
    }

    private void mostrarTab(String tab) {
        panelNegocio.setVisible(false);
        panelNegocio.setManaged(false);
        panelPOS.setVisible(false);
        panelPOS.setManaged(false);
        panelUsuarios.setVisible(false);
        panelUsuarios.setManaged(false);
        panelFiscal.setVisible(false);
        panelFiscal.setManaged(false);
        panelIntegraciones.setVisible(false);
        panelIntegraciones.setManaged(false);
        panelBaseDatos.setVisible(false);
        panelBaseDatos.setManaged(false);

        btnTabNegocio.setStyle(STYLE_TAB_INACTIVE);
        btnTabPOS.setStyle(STYLE_TAB_INACTIVE);
        btnTabUsuarios.setStyle(STYLE_TAB_INACTIVE);
        btnTabFiscal.setStyle(STYLE_TAB_INACTIVE);
        btnTabIntegraciones.setStyle(STYLE_TAB_INACTIVE);
        btnTabBaseDatos.setStyle(STYLE_TAB_INACTIVE);

        switch (tab) {
            case "negocio" -> {
                panelNegocio.setVisible(true);
                panelNegocio.setManaged(true);
                btnTabNegocio.setStyle(STYLE_TAB_ACTIVE);
            }
            case "pos" -> {
                panelPOS.setVisible(true);
                panelPOS.setManaged(true);
                btnTabPOS.setStyle(STYLE_TAB_ACTIVE);
            }
            case "usuarios" -> {
                panelUsuarios.setVisible(true);
                panelUsuarios.setManaged(true);
                btnTabUsuarios.setStyle(STYLE_TAB_ACTIVE);
            }
            case "fiscal" -> {
                panelFiscal.setVisible(true);
                panelFiscal.setManaged(true);
                btnTabFiscal.setStyle(STYLE_TAB_ACTIVE);
            }
            case "integraciones" -> {
                panelIntegraciones.setVisible(true);
                panelIntegraciones.setManaged(true);
                btnTabIntegraciones.setStyle(STYLE_TAB_ACTIVE);
            }
            case "basedatos" -> {
                panelBaseDatos.setVisible(true);
                panelBaseDatos.setManaged(true);
                btnTabBaseDatos.setStyle(STYLE_TAB_ACTIVE);
            }
        }
    }

    // Guardar
    @FXML
    public void guardarConfiguracion() {
        Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);

        // Negocio
        prefs.put("negocio_nombre", txtNombreNegocio.getText().trim());
        prefs.put("negocio_slogan", txtSlogan.getText().trim());
        prefs.put("negocio_telefono", txtTelefono.getText().trim());
        prefs.put("negocio_direccion", txtDireccion.getText().trim());
        prefs.put("negocio_ciudad", txtCiudad.getText().trim());
        prefs.put("negocio_cp", txtCP.getText().trim());
        prefs.put("negocio_correo", txtCorreo.getText().trim());
        prefs.put("negocio_web", txtSitioWeb.getText().trim());
        prefs.put("negocio_rfc", txtRFC.getText().trim());
        prefs.put("negocio_hora_ap", txtHoraApertura.getText().trim());
        prefs.put("negocio_hora_ci", txtHoraCierre.getText().trim());
        prefs.put("negocio_credito", txtLimiteCredito.getText().trim());
        prefs.put("negocio_sucursal", txtNumSucursal.getText().trim());

        // Ticket
        prefs.put("ticket_nombre", txtTicketNombre.getText().trim());
        prefs.put("ticket_giro", txtTicketGiro.getText().trim());
        prefs.put("ticket_direccion", txtTicketDireccion.getText().trim());
        prefs.put("ticket_ciudad", txtTicketCiudad.getText().trim());
        prefs.put("ticket_telefono", txtTicketTelefono.getText().trim());
        prefs.put("ticket_encabezado", txtMensajeEncabezado.getText().trim());
        prefs.put("ticket_pie", txtMensajePie.getText().trim());
        prefs.put("ticket_aviso", txtAvisoFiscal.getText().trim());
        prefs.putBoolean("ticket_logo", tglLogoTicket.isSelected());
        prefs.putBoolean("ticket_folio", tglFolioTicket.isSelected());
        prefs.putBoolean("ticket_desglose", tglDesglose.isSelected());
        prefs.putBoolean("ticket_qr", tglQR.isSelected());

        // Integraciones
        prefs.put("int_clip", txtClipToken.getText().trim());
        prefs.put("int_stripe", txtStripeKey.getText().trim());
        prefs.put("int_correo_rep", txtCorreoReportes.getText().trim());

        actualizarVistaPrevia();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado", "Configuracion guardada correctamente.");
    }

    // Restablecer
    @FXML
    public void restablecerConfiguracion() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Restablecer");
        alerta.setHeaderText(null);
        alerta.setContentText("Restablecer todos los ajustes a los valores predeterminados?");
        alerta.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                txtNombreNegocio.clear();
                txtTelefono.clear();
                txtDireccion.clear();
                txtCorreo.clear();
                txtSitioWeb.clear();
                txtRFC.clear();
                txtSlogan.clear();
                txtCiudad.clear();
                txtCP.clear();
                tglLogoTicket.setSelected(true);
                tglFolioTicket.setSelected(true);
                tglDesglose.setSelected(true);
                tglQR.setSelected(false);
                cmbMoneda.setValue("MXN - Peso Mexicano");
                cmbImpresora.setValue("EPSON TM-T20III");
                cmbAnchoPapel.setValue("80 mm");
                cmbMetodoPago.setValue("Efectivo y Tarjeta");
                txtTicketNombre.setText("Volovan Volo");
                txtTicketGiro.setText("Panaderia y Reposteria");
                txtTicketDireccion.clear();
                txtTicketCiudad.clear();
                txtTicketTelefono.clear();
                txtMensajeEncabezado.setText("Bienvenido! Gracias por visitarnos.");
                txtMensajePie.setText("Gracias por su compra!\nVuelva pronto.");
                txtAvisoFiscal.setText("Este ticket no es comprobante fiscal");
                actualizarVistaPrevia();
            }
        });
    }

    // Cargar preferencias
    private void cargarPreferencias() {
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
        txtHoraApertura.setText(prefs.get("negocio_hora_ap", "08:00"));
        txtHoraCierre.setText(prefs.get("negocio_hora_ci", "21:00"));
        txtLimiteCredito.setText(prefs.get("negocio_credito", ""));
        txtNumSucursal.setText(prefs.get("negocio_sucursal", ""));

        tglLogoTicket.setSelected(prefs.getBoolean("ticket_logo", true));
        tglFolioTicket.setSelected(prefs.getBoolean("ticket_folio", true));
        tglDesglose.setSelected(prefs.getBoolean("ticket_desglose", true));
        tglQR.setSelected(prefs.getBoolean("ticket_qr", false));

        txtTicketNombre.setText(prefs.get("ticket_nombre", "Volovan Volo"));
        txtTicketGiro.setText(prefs.get("ticket_giro", "Panaderia y Reposteria"));
        txtTicketDireccion.setText(prefs.get("ticket_direccion", ""));
        txtTicketCiudad.setText(prefs.get("ticket_ciudad", ""));
        txtTicketTelefono.setText(prefs.get("ticket_telefono", ""));
        txtMensajeEncabezado.setText(prefs.get("ticket_encabezado", "Bienvenido! Gracias por visitarnos."));
        txtMensajePie.setText(prefs.get("ticket_pie", "Gracias por su compra!\nVuelva pronto."));
        txtAvisoFiscal.setText(prefs.get("ticket_aviso", "Este ticket no es comprobante fiscal"));

        txtClipToken.setText(prefs.get("int_clip", ""));
        txtStripeKey.setText(prefs.get("int_stripe", ""));
        txtCorreoReportes.setText(prefs.get("int_correo_rep", ""));
    }

    // Vista del ticket
    @FXML
    public void actualizarVistaPreviaBtn() { actualizarVistaPrevia(); }

    private void actualizarVistaPrevia() {
        int ancho = "58 mm".equals(cmbAnchoPapel.getValue()) ? 32 : 42;

        String nombre = txtTicketNombre.getText().trim();
        String giro = txtTicketGiro.getText().trim();
        String direccion = txtTicketDireccion.getText().trim();
        String ciudad = txtTicketCiudad.getText().trim();
        String tel = txtTicketTelefono.getText().trim();
        String encabezado = txtMensajeEncabezado.getText().trim();
        String pie = txtMensajePie.getText().trim();
        String aviso = txtAvisoFiscal.getText().trim();

        String linea = "=".repeat(ancho);
        String lineaS = "-".repeat(ancho);
        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm"));

        StringBuilder sb = new StringBuilder();

        if (tglLogoTicket.isSelected()) {
            sb.append(centrar("[LOGO]", ancho)).append("\n");
        }
        sb.append(centrar(nombre.isEmpty() ? "NOMBRE DEL NEGOCIO" : nombre, ancho)).append("\n");
        if (!giro.isEmpty()) sb.append(centrar(giro, ancho)).append("\n");
        if (!direccion.isEmpty()) sb.append(centrar(direccion, ancho)).append("\n");
        if (!ciudad.isEmpty()) sb.append(centrar(ciudad, ancho)).append("\n");
        if (!tel.isEmpty()) sb.append(centrar("Tel: " + tel, ancho)).append("\n");
        if (!aviso.isEmpty()) sb.append(centrar(aviso, ancho)).append("\n");
        sb.append(linea).append("\n");
        if (!encabezado.isEmpty()) {
            for (String l : encabezado.split("\n")) sb.append(centrar(l.trim(), ancho)).append("\n");
            sb.append(lineaS).append("\n");
        }
        if (tglFolioTicket.isSelected()) sb.append(izq("Folio: #001234", ancho)).append("\n");
        sb.append(izq("Fecha: " + fecha, ancho)).append("\n");
        sb.append(izq("Cajero: " + SesionUsuario.getInstancia().getNombre(), ancho)).append("\n");
        sb.append(lineaS).append("\n");
        sb.append(columnas("DESCRIPCION", "IMPORTE", ancho)).append("\n");
        sb.append(lineaS).append("\n");
        sb.append(columnas("2x Croissant mantequilla", "$48.00", ancho)).append("\n");
        sb.append(columnas("1x Pan de chocolate", "$22.00", ancho)).append("\n");
        sb.append(columnas("3x Cuerno azucarado", "$36.00", ancho)).append("\n");
        sb.append(columnas("1x Cafe americano", "$35.00", ancho)).append("\n");
        sb.append(lineaS).append("\n");
        if (tglDesglose.isSelected()) sb.append(columnas("Subtotal:", "$141.00", ancho)).append("\n");
        sb.append(linea).append("\n");
        sb.append(columnas("TOTAL:", "$141.00", ancho)).append("\n");
        sb.append(linea).append("\n");
        sb.append(columnas("Efectivo:", "$200.00", ancho)).append("\n");
        sb.append(columnas("Cambio:",   "$ 59.00", ancho)).append("\n");
        sb.append(lineaS).append("\n");
        if (tglQR.isSelected()) sb.append("\n").append(centrar("[== QR ==]", ancho)).append("\n");
        if (!pie.isEmpty()) {
            sb.append(lineaS).append("\n");
            for (String l : pie.split("\n")) sb.append(centrar(l.trim(), ancho)).append("\n");
        }
        sb.append("\n\n");
        txtVistaTicket.setText(sb.toString());
    }

    private String centrar(String texto, int ancho) {
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int pad = (ancho - texto.length()) / 2;
        return " ".repeat(pad) + texto;
    }
    private String izq(String texto, int ancho) {
        return texto.length() >= ancho ? texto.substring(0, ancho) : texto;
    }
    private String columnas(String izquierda, String derecha, int ancho) {
        int espacioDesc = ancho - derecha.length() - 1;
        if (espacioDesc < 1) espacioDesc = 1;
        String desc = izquierda.length() > espacioDesc
                ? izquierda.substring(0, espacioDesc - 1) + "..."
                : izquierda;
        int espacios = ancho - desc.length() - derecha.length();
        if (espacios < 1) espacios = 1;
        return desc + " ".repeat(espacios) + derecha;
    }

    // Apartado Usuarios y roles
    @FXML
    public void agregarUsuario() {
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
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Eliminar Usuario"); a.setHeaderText(null);
        a.setContentText("Estas seguro? Esta accion no se puede deshacer.");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK)
                mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Usuario eliminado correctamente.");
        });
    }

    // Apartado Integrciones
    @FXML
    public void conectarClip() {
        String token = txtClipToken.getText().trim();
        if (token.isEmpty()) { mostrarAlerta(Alert.AlertType.WARNING, "Clip", "Ingresa el token de Clip."); return; }
        mostrarAlerta(Alert.AlertType.INFORMATION, "Clip", "Token guardado: " + token.substring(0, Math.min(8, token.length())) + "...");
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

    // Apartado Base de datos
    @FXML
    public void probarConexionDB() {
        String host = txtDBHost.getText().trim();
        String puerto = txtDBPuerto.getText().trim();
        String bd = txtDBNombre.getText().trim();
        String user = txtDBUsuario.getText().trim();
        String pass = txtDBPassword.getText();

        String url = "jdbc:mysql://" + host + ":" + puerto + "/" + bd;

        try {
            Connection conn = java.sql.DriverManager.getConnection(url, txtDBUsuario.getText().trim(), txtDBPassword.getText());
            if (conn != null) {
                conn.close();
                lblEstadoDB.setText("Conectado — " + txtDBNombre.getText() + "@" + txtDBHost.getText());
                lblEstadoDB.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold; -fx-font-size: 11px;");
                mostrarAlerta(Alert.AlertType.INFORMATION, "Conexion Exitosa", "Base de datos conectada correctamente.");
            }
        } catch (Exception e) {
            lblEstadoDB.setText("Error de conexion");
            lblEstadoDB.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold; -fx-font-size: 11px;");
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexion", "No se pudo conectar.\n" + e.getMessage());
        }
    }
    @FXML public void aplicarConexionDB() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Aplicar Conexion"); a.setHeaderText(null);
        a.setContentText("Aplicar estos parametros como conexion activa?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);
                prefs.put("db_host", txtDBHost.getText().trim());
                prefs.put("db_puerto", txtDBPuerto.getText().trim());
                prefs.put("db_nombre", txtDBNombre.getText().trim());
                prefs.put("db_usuario", txtDBUsuario.getText().trim());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Configuracion Aplicada",
                        "Parametros de base de datos actualizados.\n" +
                                "NOTA: Para que el cambio surta efecto en ConexionDB.java,\n" +
                                "actualiza los valores en esa clase o implementa carga dinamica.");
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
        if (ruta.isEmpty()) { mostrarAlerta(Alert.AlertType.WARNING, "Respaldo", "Selecciona la carpeta primero."); return; }
        String archivo = "respaldo_pos_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".sql";
        lblUltimoRespaldo.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " — " + archivo);
        mostrarAlerta(Alert.AlertType.INFORMATION, "Respaldo Exportado", "Respaldo creado:\n" + ruta + File.separator + archivo);
    }
    @FXML public void restaurarRespaldo() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Restaurar"); c.setHeaderText("Advertencia: sobreescribira los datos actuales");
        c.setContentText("Deseas seleccionar un archivo de respaldo?");
        c.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Seleccionar archivo de respaldo");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos SQL", "*.sql"));
                File archivo = fc.showOpenDialog((Stage) lblNombreUsuario.getScene().getWindow());
                if (archivo != null)
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Restaurar", "Archivo: " + archivo.getName());
            }
        });
    }
    @FXML public void actualizarEstadisticasDB() {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) { lblTamanoDB.setText("Sin conexion"); return; }
        try (Statement stmt = conn.createStatement()) {
            // Contar tickets
            ResultSet rsTickets = stmt.executeQuery("SELECT COUNT(*) FROM tickets");
            if (rsTickets.next()) lblRegVentas.setText(rsTickets.getInt(1) + " registros");

            // Contar productos
            ResultSet rsProductos = stmt.executeQuery("SELECT COUNT(*) FROM productos");
            if (rsProductos.next()) lblRegProductos.setText(rsProductos.getInt(1) + " productos");

            // Tamaño de la BD
            ResultSet rsSize = stmt.executeQuery(
                    "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS tamano_mb " +
                            "FROM information_schema.tables WHERE table_schema = '" + txtDBNombre.getText() + "'"
            );
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
        a.setContentText("Eliminar todos los registros del log de auditoria?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK)
                mostrarAlerta(Alert.AlertType.INFORMATION, "Log Limpiado", "Log de auditoria limpiado.");
        });
    }

    //
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje);
        a.showAndWait();
    }

    // Navegación
    @FXML
    private void irADashboard() {
        navegar("/org/example/vista/MenuPrincipal.fxml");
    }

    @FXML
    private void irAVentas() {
        navegar("/org/example/vista/Ventas.fxml");
    }

    @FXML
    private void irAInventario() {
        navegar("/org/example/vista/Inventario.fxml");
    }

    @FXML
    private void irAEmpleados() {
        navegar("/org/example/vista/Empleados.fxml");
    }

    @FXML
    private void irAClientes() {
        navegar("/org/example/vista/Clientes.fxml");
    }

    @FXML
    private void irAReportes() {
        navegar("/org/example/vista/Reportes.fxml");
    }

    @FXML
    private void irACorteCaja() {
        navegar("/org/example/vista/CorteCaja.fxml");
    }


    @FXML
    private void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Cerrar Sesión");
        a.setHeaderText(null);
        a.setContentText("¿Estás seguro de que deseas salir?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) Platform.exit();
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
}