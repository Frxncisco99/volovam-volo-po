package org.example.controlador;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.modelo.Ticket;
import org.example.servicio.TicketImpresora;
import org.example.servicio.TicketService;

import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

/**
 * Controlador del modal de vista previa del ticket.
 * Renderiza el ticket como componentes JavaFX styled,
 * simulando fielmente un ticket térmico de 58mm.
 *
 * Flujo: ConfiguracionController.abrirVistaPrevia()
 *   → carga TicketPreview.fxml
 *   → llama configurar(...)
 *   → renderizar() construye dinámicamente los nodos
 */
public class TicketPreviewController {

    @FXML private VBox contenedorTicket;

    private final TicketService    ticketService = new TicketService();
    private final TicketImpresora  impresora     = new TicketImpresora();

    // Config del ticket — se guardan en configurar() para usarlos en handleImprimir()
    private Ticket ticket;
    private String logoRuta;
    private String nombre, giro, direccion, ciudad, telefono;
    private String encabezado, pie, aviso;
    private boolean mostrarLogo, mostrarFolio, mostrarDesglose;
    private boolean mostrarQR, mostrarFecha, mostrarCajero;

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Punto de entrada. Llamado desde ConfiguracionController justo después de load().
     */
    public void configurar(Ticket ticket, String logoRuta,
                           String nombre, String giro, String direccion,
                           String ciudad, String telefono,
                           String encabezado, String pie, String aviso,
                           boolean mostrarLogo, boolean mostrarFolio,
                           boolean mostrarDesglose, boolean mostrarQR,
                           boolean mostrarFecha, boolean mostrarCajero) {
        this.ticket          = ticket;
        this.logoRuta        = logoRuta;
        this.nombre          = nombre;
        this.giro            = giro;
        this.direccion       = direccion;
        this.ciudad          = ciudad;
        this.telefono        = telefono;
        this.encabezado      = encabezado;
        this.pie             = pie;
        this.aviso           = aviso;
        this.mostrarLogo     = mostrarLogo;
        this.mostrarFolio    = mostrarFolio;
        this.mostrarDesglose = mostrarDesglose;
        this.mostrarQR       = mostrarQR;
        this.mostrarFecha    = mostrarFecha;
        this.mostrarCajero   = mostrarCajero;
        renderizar();
    }

    @FXML
    public void initialize() {}

    // ── Renderizado ───────────────────────────────────────────────────────────

    private void renderizar() {
        contenedorTicket.getChildren().clear();

        // ── Logo ──────────────────────────────────────────────────────────────
        if (mostrarLogo) {
            try {
                String ruta = (logoRuta != null && !logoRuta.isBlank())
                        ? logoRuta
                        : "/org/example/Imagenes/logo_volovan.png";
                var stream = getClass().getResourceAsStream(ruta);
                if (stream != null) {
                    Image img = new Image(stream);
                    if (!img.isError()) {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(110);
                        iv.setPreserveRatio(true);
                        iv.setSmooth(true);
                        VBox logoBox = new VBox(iv);
                        logoBox.setAlignment(Pos.CENTER);
                        VBox.setMargin(logoBox, new Insets(4, 0, 6, 0));
                        contenedorTicket.getChildren().add(logoBox);
                    }
                }
            } catch (Exception ignored) {}
        }

        // ── Encabezado negocio ────────────────────────────────────────────────
        agregarCentro(nombre.isBlank() ? "NEGOCIO" : nombre.toUpperCase(), true, 12);
        if (!giro.isBlank())      agregarCentro(giro, false, 9);
        if (!direccion.isBlank()) agregarCentro(direccion, false, 9);
        if (!ciudad.isBlank())    agregarCentro(ciudad, false, 9);
        if (!telefono.isBlank())  agregarCentro("Tel: " + telefono, false, 9);
        if (!aviso.isBlank())     agregarCentroItalica(aviso, 8);

        agregarSeparador(true);

        if (!encabezado.isBlank()) {
            for (String l : encabezado.split("\n"))
                agregarCentro(l.trim(), false, 9);
            agregarSeparador(false);
        }

        // ── Datos de la venta ─────────────────────────────────────────────────
        if (mostrarFolio)
            agregarFila("Folio:", String.format("#%06d", ticket.getIdVenta()), false, 9);
        if (mostrarFecha)
            agregarFila("Fecha:", ticket.getFechaHora()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false, 9);
        if (mostrarCajero)
            agregarFila("Cajero:", ticket.getNombreCajero(), false, 9);
        if (mostrarFolio || mostrarFecha || mostrarCajero)
            agregarFila("Caja:", String.valueOf(ticket.getNumeroCaja()), false, 9);

        agregarSeparador(false);

        // ── Cabecera columnas ─────────────────────────────────────────────────
        contenedorTicket.getChildren().add(crearFilaProducto(
                "PRODUCTO", "CANT", "P.U.", "SUBT.", true));
        agregarSeparador(false);

        // ── Líneas de producto ────────────────────────────────────────────────
        for (Ticket.LineaTicket l : ticket.getLineas()) {
            contenedorTicket.getChildren().add(crearFilaProducto(
                    l.getNombreProducto(),
                    String.valueOf(l.getCantidad()),
                    String.format("%.2f", l.getPrecioUnitario()),
                    String.format("%.2f", l.getSubtotal()),
                    false));
        }

        // ── Totales ───────────────────────────────────────────────────────────
        agregarSeparador(false);
        int totalArt = ticket.getLineas().stream()
                .mapToInt(Ticket.LineaTicket::getCantidad).sum();
        agregarFila("Artículos:", totalArt + " pza(s)", false, 9);
        agregarSeparador(true);

        if (mostrarDesglose)
            agregarFila("Subtotal:", String.format("$%.2f", ticket.getSubtotal()), false, 10);

        agregarFilaNegrita("TOTAL:", String.format("$%.2f", ticket.getTotal()), 13);
        agregarSeparador(true);

        agregarFila("Efectivo:", String.format("$%.2f", ticket.getMontoRecibido()), false, 10);
        agregarFila("Cambio:",   String.format("$%.2f", ticket.getCambio()),        false, 10);

        // ── QR ────────────────────────────────────────────────────────────────
        if (mostrarQR) {
            agregarSeparador(false);
            Label qrPlaceholder = new Label("[ ■■■ QR ■■■ ]");
            qrPlaceholder.setMaxWidth(Double.MAX_VALUE);
            qrPlaceholder.setAlignment(Pos.CENTER);
            qrPlaceholder.setStyle("-fx-font-size: 10px; -fx-text-fill: #555; -fx-font-family: monospace;");
            VBox.setMargin(qrPlaceholder, new Insets(4, 0, 4, 0));
            contenedorTicket.getChildren().add(qrPlaceholder);
        }

        // ── Pie de página ─────────────────────────────────────────────────────
        if (!pie.isBlank()) {
            agregarSeparador(false);
            for (String l : pie.split("\n"))
                agregarCentro(l.trim(), false, 9);
        }

        // Espacio final (simula avance de papel antes del corte)
        contenedorTicket.getChildren().add(new Label(""));
        contenedorTicket.getChildren().add(new Label(""));
    }

    // ── Helpers de construcción de nodos ────────────────────────────────────

    private void agregarCentro(String texto, boolean negrita, int size) {
        Label lbl = new Label(texto);
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        lbl.setWrapText(true);
        lbl.setStyle(
                "-fx-font-size: " + size + "px;" +
                        (negrita ? "-fx-font-weight: bold;" : "") +
                        "-fx-text-fill: #1A1A1A;" +
                        "-fx-font-family: 'Arial', sans-serif;"
        );
        contenedorTicket.getChildren().add(lbl);
    }

    private void agregarCentroItalica(String texto, int size) {
        Label lbl = new Label(texto);
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        lbl.setWrapText(true);
        lbl.setStyle(
                "-fx-font-size: " + size + "px;" +
                        "-fx-font-style: italic;" +
                        "-fx-text-fill: #555555;"
        );
        contenedorTicket.getChildren().add(lbl);
    }

    private void agregarFila(String izq, String der, boolean negrita, int size) {
        HBox fila = new HBox();
        fila.setMaxWidth(Double.MAX_VALUE);
        Label lIzq = new Label(izq);
        Label lDer = new Label(der);
        HBox.setHgrow(lIzq, Priority.ALWAYS);
        String estilo = "-fx-font-size: " + size + "px; -fx-text-fill: #1A1A1A;" +
                (negrita ? "-fx-font-weight: bold;" : "");
        lIzq.setStyle(estilo);
        lDer.setStyle(estilo);
        fila.getChildren().addAll(lIzq, lDer);
        contenedorTicket.getChildren().add(fila);
    }

    private void agregarFilaNegrita(String izq, String der, int size) {
        HBox fila = new HBox();
        fila.setMaxWidth(Double.MAX_VALUE);
        Label lIzq = new Label(izq);
        Label lDer = new Label(der);
        HBox.setHgrow(lIzq, Priority.ALWAYS);
        String estilo = "-fx-font-size: " + size + "px; -fx-text-fill: #1A1A1A; -fx-font-weight: bold;";
        lIzq.setStyle(estilo);
        lDer.setStyle(estilo);
        fila.getChildren().addAll(lIzq, lDer);
        VBox.setMargin(fila, new Insets(2, 0, 2, 0));
        contenedorTicket.getChildren().add(fila);
    }

    private HBox crearFilaProducto(String desc, String cant, String pu, String subt, boolean header) {
        HBox fila = new HBox();
        fila.setMaxWidth(Double.MAX_VALUE);

        Label lDesc = new Label(desc);
        Label lCant = new Label(cant);
        Label lPU   = new Label(pu);
        Label lSubt = new Label(subt);

        HBox.setHgrow(lDesc, Priority.ALWAYS);
        lDesc.setWrapText(!header);
        lDesc.setMaxWidth(Double.MAX_VALUE);

        lCant.setMinWidth(26); lCant.setPrefWidth(26); lCant.setAlignment(Pos.CENTER_RIGHT);
        lPU.setMinWidth(38);   lPU.setPrefWidth(38);   lPU.setAlignment(Pos.CENTER_RIGHT);
        lSubt.setMinWidth(42); lSubt.setPrefWidth(42); lSubt.setAlignment(Pos.CENTER_RIGHT);

        String estilo = "-fx-font-size: 9px; -fx-text-fill: #1A1A1A;" +
                (header ? "-fx-font-weight: bold;" : "");
        for (Label l : new Label[]{lDesc, lCant, lPU, lSubt}) l.setStyle(estilo);

        fila.setSpacing(4);
        fila.getChildren().addAll(lDesc, lCant, lPU, lSubt);
        return fila;
    }

    private void agregarSeparador(boolean doble) {
        Separator sep = new Separator();
        sep.setStyle(doble
                ? "-fx-background-color: #333; -fx-pref-height: 1.5;"
                : "-fx-opacity: 0.35;");
        VBox.setMargin(sep, new Insets(3, 0, 3, 0));
        contenedorTicket.getChildren().add(sep);
    }

    // ── Acciones de botones ───────────────────────────────────────────────────

    /**
     * Imprime usando la configuración guardada en Preferences —
     * exactamente los mismos parámetros que se usaron para renderizar
     * la vista previa. Sincronizado con ConfiguracionController y PagoController.
     */
    @FXML
    public void handleImprimir() {
        if (ticket == null) return;
        try {
            // Leer el ancho de papel guardado en Preferences
            Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);
            String anchoPapel = prefs.get("ticket_ancho", "80 mm");
            int ancho = "58 mm".equals(anchoPapel) ? 32 : 48;

            // Imprimir con los mismos parámetros que configurar() recibió
            impresora.imprimirConConfig(
                    ticket,
                    nombre, giro, direccion, ciudad, telefono,
                    encabezado, pie, aviso,
                    mostrarLogo, mostrarFolio, mostrarDesglose, mostrarQR,
                    ancho
            );
            mostrarInfo("Impresión enviada", "El ticket fue enviado a la impresora.");
        } catch (Exception e) {
            mostrarError("Error de impresión",
                    "No se pudo imprimir.\nVerifica que la impresora esté conectada.\n\n"
                            + e.getMessage());
        }
    }

    @FXML
    public void handleCerrar() {
        ((Stage) contenedorTicket.getScene().getWindow()).close();
    }

    private void mostrarInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    private void mostrarError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}