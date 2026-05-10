package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.example.modelo.Ticket;
import org.example.servicio.TicketImpresora;
import org.example.servicio.TicketRenderer;

/**
 * Controlador del modal "Vista Previa del Ticket".
 *
 * Diseño: muestra el mismo String que TicketRenderer.generar() produce,
 * en una TextArea monoespaciada que simula el papel térmico.
 * La impresora real también consume ese mismo String →
 * lo que se ve en pantalla = lo que sale impreso.
 *
 * Ubicación: src/main/java/org/example/controlador/TicketPreviewController.java
 * FXML:      src/main/resources/org/example/vista/TicketPreview.fxml
 */
public class TicketPreviewController {

    @FXML private TextArea txtTicketRender;

    private final TicketImpresora impresora = new TicketImpresora();

    // ── Estado capturado al abrir la ventana ──────────────────────────────────
    private Ticket  ticket;
    private int     ancho;
    private String  nombre, giro, direccion, ciudad, telefono, encabezado, pie, aviso;
    private boolean mostrarLogo, mostrarFolio, mostrarDesglose,
            mostrarQR,   mostrarFecha, mostrarCajero;

    @FXML
    public void initialize() {}

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Único punto de entrada. Llamado desde ConfiguracionController.abrirVistaPrevia().
     * Almacena todos los parámetros y renderiza inmediatamente.
     */
    public void configurar(Ticket ticket,
                           String nombre, String giro,
                           String direccion, String ciudad, String telefono,
                           String encabezado, String pie, String aviso,
                           boolean mostrarLogo, boolean mostrarFolio,
                           boolean mostrarDesglose, boolean mostrarQR,
                           boolean mostrarFecha, boolean mostrarCajero,
                           int ancho) {

        this.ticket          = ticket;
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
        this.ancho           = ancho;

        // ── UN solo render, compartido con la impresora real ─────────────────
        txtTicketRender.setText(TicketRenderer.generar(
                ticket,
                nombre, giro, direccion, ciudad, telefono,
                encabezado, pie, aviso,
                mostrarLogo, mostrarFolio, mostrarDesglose, mostrarQR,
                mostrarFecha, mostrarCajero,
                ancho));
    }

    // ── Botones ───────────────────────────────────────────────────────────────

    @FXML
    public void handleImprimir() {
        if (ticket == null) return;
        try {
            impresora.imprimirConRenderer(
                    ticket,
                    nombre, giro, direccion, ciudad, telefono,
                    encabezado, pie, aviso,
                    mostrarLogo, mostrarFolio, mostrarDesglose, mostrarQR,
                    mostrarFecha, mostrarCajero,
                    ancho);
            alerta(Alert.AlertType.INFORMATION,
                    "Ticket enviado", "El ticket fue enviado a la impresora.");
        } catch (Exception e) {
            e.printStackTrace();
            alerta(Alert.AlertType.ERROR,
                    "Error de impresión",
                    "No se pudo imprimir el ticket.\n" +
                            "Verifica que la impresora esté encendida y conectada.\n\n" +
                            e.getMessage());
        }
    }

    @FXML
    public void handleCerrar() {
        ((Stage) txtTicketRender.getScene().getWindow()).close();
    }

    // ── Utilidad privada ──────────────────────────────────────────────────────

    private void alerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}