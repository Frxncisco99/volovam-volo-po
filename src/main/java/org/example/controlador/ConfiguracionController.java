package org.example.controlador;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.modelo.SesionUsuario;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // ── Panel Fiscal — Impuestos ──
    @FXML private ToggleButton tglIVA;
    @FXML private TextField    txtTasaIVA;
    @FXML private ComboBox<String> cmbPreciosIVA;
    @FXML private TextField    txtRFCFiscal;
    @FXML private ToggleButton tglCFDI;

    // ── Panel Fiscal — Impresión ──
    @FXML private ToggleButton tglLogoTicket;
    @FXML private ToggleButton tglFolioTicket;
    @FXML private ToggleButton tglDesglose;
    @FXML private ToggleButton tglQR;
    @FXML private ComboBox<String> cmbImpresora;
    @FXML private ComboBox<String> cmbAnchoPapel;

    // ── Panel Fiscal — Textos del ticket (NUEVOS) ──
    @FXML private TextField txtTicketNombre;
    @FXML private TextField txtTicketGiro;
    @FXML private TextField txtTicketDireccion;
    @FXML private TextField txtTicketCiudad;
    @FXML private TextField txtTicketTelefono;
    @FXML private TextArea  txtMensajeEncabezado;
    @FXML private TextArea  txtMensajePie;
    @FXML private TextField txtAvisoFiscal;

    // ── Vista previa del ticket (NUEVO) ──
    @FXML private TextArea txtVistaTicket;

    // ── Panel POS ──
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private ToggleButton tglDescuentos;
    @FXML private ToggleButton tglPropinas;
    @FXML private TextField    txtMaxDescuento;

    // ── Estilos de pestañas ──
    private static final String STYLE_TAB_ACTIVE =
            "-fx-background-color: #6B4226; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand;";
    private static final String STYLE_TAB_INACTIVE =
            "-fx-background-color: white; -fx-text-fill: #6B4226; " +
                    "-fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand; " +
                    "-fx-border-color: #D4C5B0; -fx-border-radius: 20;";

    // ─────────────────────────────────────────────
    @FXML
    public void initialize() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String nombre = sesion.getNombre();
        lblAvatarIniciales.setText(
                nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase()
        );

        // ── ComboBoxes ──
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

        cmbImpresora.getItems().addAll(
                "EPSON TM-T20III", "EPSON TM-T88V", "Star TSP100", "Genérica"
        );
        cmbImpresora.setValue("EPSON TM-T20III");

        cmbAnchoPapel.getItems().addAll("58 mm", "80 mm");
        cmbAnchoPapel.setValue("80 mm");

        cmbMetodoPago.getItems().addAll("Efectivo", "Efectivo y Tarjeta", "Todos los métodos");
        cmbMetodoPago.setValue("Efectivo y Tarjeta");

        mostrarTab("negocio");
        cargarPreferencias();
    }

    // ── Navegación entre pestañas ──
    @FXML private void tabNegocio()       { mostrarTab("negocio"); }
    @FXML private void tabPOS()           { mostrarTab("pos"); }
    @FXML private void tabUsuarios()      { mostrarTab("usuarios"); }
    @FXML private void tabFiscal()        { mostrarTab("fiscal"); actualizarVistaPrevia(); }
    @FXML private void tabIntegraciones() { mostrarTab("integraciones"); }
    @FXML private void tabApariencia()    { mostrarTab("apariencia"); }
    @FXML private void tabBaseDatos()     { mostrarTab("basedatos"); }

    private void mostrarTab(String tab) {
        panelNegocio.setVisible(false);      panelNegocio.setManaged(false);
        panelPOS.setVisible(false);          panelPOS.setManaged(false);
        panelUsuarios.setVisible(false);     panelUsuarios.setManaged(false);
        panelFiscal.setVisible(false);       panelFiscal.setManaged(false);
        panelIntegraciones.setVisible(false);panelIntegraciones.setManaged(false);
        panelApariencia.setVisible(false);   panelApariencia.setManaged(false);
        panelBaseDatos.setVisible(false);    panelBaseDatos.setManaged(false);

        btnTabNegocio.setStyle(STYLE_TAB_INACTIVE);
        btnTabPOS.setStyle(STYLE_TAB_INACTIVE);
        btnTabUsuarios.setStyle(STYLE_TAB_INACTIVE);
        btnTabFiscal.setStyle(STYLE_TAB_INACTIVE);
        btnTabIntegraciones.setStyle(STYLE_TAB_INACTIVE);
        btnTabApariencia.setStyle(STYLE_TAB_INACTIVE);
        btnTabBaseDatos.setStyle(STYLE_TAB_INACTIVE);

        switch (tab) {
            case "negocio"       -> { panelNegocio.setVisible(true);       panelNegocio.setManaged(true);       btnTabNegocio.setStyle(STYLE_TAB_ACTIVE); }
            case "pos"           -> { panelPOS.setVisible(true);           panelPOS.setManaged(true);           btnTabPOS.setStyle(STYLE_TAB_ACTIVE); }
            case "usuarios"      -> { panelUsuarios.setVisible(true);      panelUsuarios.setManaged(true);      btnTabUsuarios.setStyle(STYLE_TAB_ACTIVE); }
            case "fiscal"        -> { panelFiscal.setVisible(true);        panelFiscal.setManaged(true);        btnTabFiscal.setStyle(STYLE_TAB_ACTIVE); }
            case "integraciones" -> { panelIntegraciones.setVisible(true); panelIntegraciones.setManaged(true); btnTabIntegraciones.setStyle(STYLE_TAB_ACTIVE); }
            case "apariencia"    -> { panelApariencia.setVisible(true);    panelApariencia.setManaged(true);    btnTabApariencia.setStyle(STYLE_TAB_ACTIVE); }
            case "basedatos"     -> { panelBaseDatos.setVisible(true);     panelBaseDatos.setManaged(true);     btnTabBaseDatos.setStyle(STYLE_TAB_ACTIVE); }
        }
    }

    // ─────────────────────────────────────────────
    // GUARDAR
    // ─────────────────────────────────────────────
    @FXML
    public void guardarConfiguracion() {
        Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);

        // Negocio
        prefs.put("negocio_nombre",    txtNombreNegocio.getText().trim());
        prefs.put("negocio_telefono",  txtTelefono.getText().trim());
        prefs.put("negocio_direccion", txtDireccion.getText().trim());
        prefs.put("negocio_correo",    txtCorreo.getText().trim());
        prefs.put("negocio_web",       txtSitioWeb.getText().trim());
        prefs.put("negocio_rfc",       txtRFC.getText().trim());

        // Fiscal
        prefs.put("fiscal_iva",    txtTasaIVA.getText().trim());
        prefs.put("fiscal_rfc",    txtRFCFiscal.getText().trim());
        prefs.putBoolean("fiscal_aplica_iva", tglIVA.isSelected());
        prefs.putBoolean("fiscal_cfdi",       tglCFDI.isSelected());
        prefs.putBoolean("ticket_logo",       tglLogoTicket.isSelected());
        prefs.putBoolean("ticket_folio",      tglFolioTicket.isSelected());
        prefs.putBoolean("ticket_desglose",   tglDesglose.isSelected());
        prefs.putBoolean("ticket_qr",         tglQR.isSelected());

        // Textos del ticket (NUEVOS)
        prefs.put("ticket_nombre",      txtTicketNombre.getText().trim());
        prefs.put("ticket_giro",        txtTicketGiro.getText().trim());
        prefs.put("ticket_direccion",   txtTicketDireccion.getText().trim());
        prefs.put("ticket_ciudad",      txtTicketCiudad.getText().trim());
        prefs.put("ticket_telefono",    txtTicketTelefono.getText().trim());
        prefs.put("ticket_encabezado",  txtMensajeEncabezado.getText().trim());
        prefs.put("ticket_pie",         txtMensajePie.getText().trim());
        prefs.put("ticket_aviso",       txtAvisoFiscal.getText().trim());

        // Actualizar vista previa después de guardar
        actualizarVistaPrevia();

        mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado", "Configuración guardada correctamente.");
    }

    // ─────────────────────────────────────────────
    // RESTABLECER
    // ─────────────────────────────────────────────
    @FXML
    public void restablecerConfiguracion() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Restablecer");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Restablecer todos los ajustes a los valores predeterminados?");
        alerta.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                // Negocio
                txtNombreNegocio.clear(); txtTelefono.clear();
                txtDireccion.clear();     txtCorreo.clear();
                txtSitioWeb.clear();      txtRFC.clear();

                // Fiscal
                txtTasaIVA.setText("16"); txtRFCFiscal.clear();
                tglIVA.setSelected(true); tglCFDI.setSelected(false);
                tglLogoTicket.setSelected(true); tglFolioTicket.setSelected(true);
                tglDesglose.setSelected(true);   tglQR.setSelected(false);
                cmbMoneda.setValue("MXN - Peso Mexicano");
                cmbImpresora.setValue("EPSON TM-T20III");
                cmbAnchoPapel.setValue("80 mm");

                // Textos del ticket
                txtTicketNombre.setText("Volovan Volo");
                txtTicketGiro.setText("Panadería y Repostería");
                txtTicketDireccion.clear();
                txtTicketCiudad.clear();
                txtTicketTelefono.clear();
                txtMensajeEncabezado.setText("¡Bienvenido! Gracias por visitarnos.");
                txtMensajePie.setText("¡Gracias por su compra!\nVuelva pronto.");
                txtAvisoFiscal.setText("Este ticket no es comprobante fiscal");

                actualizarVistaPrevia();
            }
        });
    }

    // ─────────────────────────────────────────────
    // CARGAR PREFERENCIAS
    // ─────────────────────────────────────────────
    private void cargarPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);

        txtNombreNegocio.setText(prefs.get("negocio_nombre",    ""));
        txtTelefono.setText(     prefs.get("negocio_telefono",  ""));
        txtDireccion.setText(    prefs.get("negocio_direccion", ""));
        txtCorreo.setText(       prefs.get("negocio_correo",    ""));
        txtSitioWeb.setText(     prefs.get("negocio_web",       ""));
        txtRFC.setText(          prefs.get("negocio_rfc",       ""));
        txtTasaIVA.setText(      prefs.get("fiscal_iva",        "16"));
        txtRFCFiscal.setText(    prefs.get("fiscal_rfc",        ""));

        tglIVA.setSelected(        prefs.getBoolean("fiscal_aplica_iva", true));
        tglCFDI.setSelected(       prefs.getBoolean("fiscal_cfdi",       false));
        tglLogoTicket.setSelected( prefs.getBoolean("ticket_logo",       true));
        tglFolioTicket.setSelected(prefs.getBoolean("ticket_folio",      true));
        tglDesglose.setSelected(   prefs.getBoolean("ticket_desglose",   true));
        tglQR.setSelected(         prefs.getBoolean("ticket_qr",         false));

        // Textos del ticket
        txtTicketNombre.setText(    prefs.get("ticket_nombre",    "Volovan Volo"));
        txtTicketGiro.setText(      prefs.get("ticket_giro",      "Panadería y Repostería"));
        txtTicketDireccion.setText( prefs.get("ticket_direccion", ""));
        txtTicketCiudad.setText(    prefs.get("ticket_ciudad",    ""));
        txtTicketTelefono.setText(  prefs.get("ticket_telefono",  ""));
        txtMensajeEncabezado.setText(prefs.get("ticket_encabezado",
                "¡Bienvenido! Gracias por visitarnos."));
        txtMensajePie.setText(      prefs.get("ticket_pie",
                "¡Gracias por su compra!\nVuelva pronto."));
        txtAvisoFiscal.setText(     prefs.get("ticket_aviso",
                "Este ticket no es comprobante fiscal"));
    }

    // ─────────────────────────────────────────────
    // VISTA PREVIA DEL TICKET  ← MÉTODO PRINCIPAL NUEVO
    // ─────────────────────────────────────────────
    private void actualizarVistaPrevia() {
        // Determinar ancho en caracteres según el papel seleccionado
        int ancho = "58 mm".equals(cmbAnchoPapel.getValue()) ? 32 : 42;

        String nombre    = txtTicketNombre.getText().trim();
        String giro      = txtTicketGiro.getText().trim();
        String direccion = txtTicketDireccion.getText().trim();
        String ciudad    = txtTicketCiudad.getText().trim();
        String tel       = txtTicketTelefono.getText().trim();
        String encabezado= txtMensajeEncabezado.getText().trim();
        String pie       = txtMensajePie.getText().trim();
        String aviso     = txtAvisoFiscal.getText().trim();
        String tasa      = txtTasaIVA.getText().trim();
        String rfc       = txtRFCFiscal.getText().trim();

        String linea  = "=".repeat(ancho);
        String lineaS = "-".repeat(ancho);
        String fecha  = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm"));

        StringBuilder sb = new StringBuilder();

        // ── ENCABEZADO DEL NEGOCIO ──
        if (tglLogoTicket.isSelected()) {
            sb.append(centrar("[LOGO]", ancho)).append("\n");
        }
        sb.append(centrar(nombre.isEmpty()  ? "NOMBRE DEL NEGOCIO" : nombre,  ancho)).append("\n");
        if (!giro.isEmpty())      sb.append(centrar(giro,      ancho)).append("\n");
        if (!direccion.isEmpty()) sb.append(centrar(direccion, ancho)).append("\n");
        if (!ciudad.isEmpty())    sb.append(centrar(ciudad,    ancho)).append("\n");
        if (!tel.isEmpty())       sb.append(centrar("Tel: " + tel, ancho)).append("\n");
        if (!rfc.isEmpty())       sb.append(centrar("RFC: " + rfc, ancho)).append("\n");
        if (!aviso.isEmpty())     sb.append(centrar(aviso, ancho)).append("\n");
        sb.append(linea).append("\n");

        // ── MENSAJE DE ENCABEZADO ──
        if (!encabezado.isEmpty()) {
            for (String lineasEnc : encabezado.split("\n")) {
                sb.append(centrar(lineasEnc.trim(), ancho)).append("\n");
            }
            sb.append(lineaS).append("\n");
        }

        // ── INFO DE VENTA ──
        if (tglFolioTicket.isSelected()) {
            sb.append(izq("Folio: #001234", ancho)).append("\n");
        }
        sb.append(izq("Fecha: " + fecha, ancho)).append("\n");
        sb.append(izq("Cajero: " + SesionUsuario.getInstancia().getNombre(), ancho)).append("\n");
        sb.append(lineaS).append("\n");

        // ── ARTÍCULOS DE EJEMPLO ──
        sb.append(columnas("DESCRIPCIÓN", "IMPORTE", ancho)).append("\n");
        sb.append(lineaS).append("\n");
        sb.append(columnas("2x Croissant mantequilla", "$48.00", ancho)).append("\n");
        sb.append(columnas("1x Pan de chocolate",      "$22.00", ancho)).append("\n");
        sb.append(columnas("3x Cuerno azucarado",      "$36.00", ancho)).append("\n");
        sb.append(columnas("1x Café americano",        "$35.00", ancho)).append("\n");
        sb.append(lineaS).append("\n");

        // ── TOTALES ──
        sb.append(columnas("Subtotal:", "$141.00", ancho)).append("\n");
        if (tglDesglose.isSelected() && tglIVA.isSelected()) {
            sb.append(columnas("IVA (" + tasa + "%):", "$22.56", ancho)).append("\n");
        }
        sb.append(linea).append("\n");
        sb.append(columnas("TOTAL:", "$163.56", ancho)).append("\n");
        sb.append(linea).append("\n");
        sb.append(columnas("Efectivo:", "$200.00", ancho)).append("\n");
        sb.append(columnas("Cambio:", "$ 36.44", ancho)).append("\n");
        sb.append(lineaS).append("\n");

        // ── QR / CÓDIGO DE BARRAS ──
        if (tglQR.isSelected()) {
            sb.append("\n").append(centrar("[■■ QR ■■]", ancho)).append("\n");
        }

        // ── MENSAJE DE PIE ──
        if (!pie.isEmpty()) {
            sb.append(lineaS).append("\n");
            for (String lineaPie : pie.split("\n")) {
                sb.append(centrar(lineaPie.trim(), ancho)).append("\n");
            }
        }

        sb.append("\n\n");  // margen inferior de corte

        txtVistaTicket.setText(sb.toString());
    }

    // ── Helpers de formato ──

    /** Centra el texto en `ancho` caracteres; trunca si es más largo. */
    private String centrar(String texto, int ancho) {
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int pad = (ancho - texto.length()) / 2;
        return " ".repeat(pad) + texto;
    }

    /** Alinea texto a la izquierda. */
    private String izq(String texto, int ancho) {
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        return texto;
    }

    /**
     * Dos columnas: texto izquierda y valor a la derecha.
     * Si el conjunto excede el ancho, trunca la descripción.
     */
    private String columnas(String izquierda, String derecha, int ancho) {
        int espacioDesc = ancho - derecha.length() - 1;
        if (espacioDesc < 1) espacioDesc = 1;
        String desc = izquierda.length() > espacioDesc
                ? izquierda.substring(0, espacioDesc - 1) + "…"
                : izquierda;
        int espacios = ancho - desc.length() - derecha.length();
        if (espacios < 1) espacios = 1;
        return desc + " ".repeat(espacios) + derecha;
    }

    // ─────────────────────────────────────────────
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
    private void irAClientes() {
        navegar ("/org/example/vista/Clientes.fxml");
    }


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