package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.example.modelo.Ticket;
import org.example.servicio.TicketService;

/**
 * Controlador de la vista previa del ticket (Ticket.fxml).
 *
 * Flujo de uso:
 *   1. PagoController crea el Stage con Ticket.fxml.
 *   2. Llama a setTicket(ticket) para inyectar los datos.
 *   3. El cajero ve la vista previa y decide si imprimir o solo cerrar.
 */
public class TicketController {

    @FXML private Label lblNumeroVenta;
    @FXML private Label lblFecha;
    @FXML private Label lblCajero;
    @FXML private Label lblTotal;
    @FXML private Label lblRecibido;
    @FXML private Label lblCambio;
    @FXML private TextArea txtVistaPrevia;

    private Ticket ticket;
    private final TicketService ticketService = new TicketService();

    // ── Inyección de datos ───────────────────────────────────────────────────

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        poblarVista();
    }

    // ── Inicialización ───────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // La vista se puebla cuando PagoController llama setTicket()
    }

    private void poblarVista() {
        if (ticket == null) return;

        lblNumeroVenta.setText("Venta # " + ticket.getIdVenta());
        lblFecha.setText(ticket.getFechaHora()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblCajero.setText(ticket.getNombreCajero());
        lblTotal.setText("$" + String.format("%.2f", ticket.getTotal()));
        lblRecibido.setText("$" + String.format("%.2f", ticket.getMontoRecibido()));
        lblCambio.setText("$" + String.format("%.2f", ticket.getCambio()));

        // Vista previa en texto monoespaciado
        txtVistaPrevia.setText(ticketService.textoPlano(ticket));
        txtVistaPrevia.setEditable(false);
    }

    // ── Acciones ─────────────────────────────────────────────────────────────

    @FXML
    public void handleImprimir() {
        if (ticket == null) return;
        try {
            ticketService.imprimir(ticket);
            mostrarInfo("Impresión enviada", "El ticket fue enviado a la impresora correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de impresión",
                    "No se pudo imprimir el ticket.\n\n" +
                            "Verifica que la impresora esté encendida y conectada.\n" +
                            "Detalle técnico: " + e.getMessage());
        }
    }

    @FXML
    public void handleReimprimir() {
        // Igual que imprimir — útil si el papel se atascó
        handleImprimir();
    }

    @FXML
    public void handleCerrar() {
        Stage stage = (Stage) txtVistaPrevia.getScene().getWindow();
        stage.close();
    }

    // ── Alertas ──────────────────────────────────────────────────────────────

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}