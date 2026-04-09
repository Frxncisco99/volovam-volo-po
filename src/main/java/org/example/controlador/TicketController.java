package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.example.modelo.Ticket;
import org.example.servicio.TicketService;

public class TicketController {

    @FXML private Label lblNumeroVenta;
    @FXML private Label lblFecha;
    @FXML private Label lblCajero;
    @FXML private Label lblCaja;
    @FXML private Label lblTotal;
    @FXML private Label lblRecibido;
    @FXML private Label lblCambio;
    @FXML private Label lblArticulos;
    @FXML private TextArea txtVistaPrevia;

    private Ticket ticket;
    private final TicketService ticketService = new TicketService();

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        poblarVista();
    }

    @FXML
    public void initialize() {}

    private void poblarVista() {
        if (ticket == null) return;

        // Folio formateado: 000123
        lblNumeroVenta.setText("Folio # " + String.format("%06d", ticket.getIdVenta()));

        lblFecha.setText(ticket.getFechaHora()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm")));
        lblCajero.setText(ticket.getNombreCajero());
        lblCaja.setText(String.valueOf(ticket.getNumeroCaja()));

        lblTotal.setText("$" + String.format("%.2f", ticket.getTotal()));
        lblRecibido.setText("$" + String.format("%.2f", ticket.getMontoRecibido()));
        lblCambio.setText("$" + String.format("%.2f", ticket.getCambio()));

        // Total de artículos (suma de cantidades)
        int totalArticulos = ticket.getLineas().stream()
                .mapToInt(Ticket.LineaTicket::getCantidad)
                .sum();
        lblArticulos.setText(String.valueOf(totalArticulos));

        // Vista previa monoespaciada
        txtVistaPrevia.setText(ticketService.textoPlano(ticket));
        txtVistaPrevia.setEditable(false);
    }

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
                            "Detalle: " + e.getMessage());
        }
    }

    @FXML
    public void handleReimprimir() {
        handleImprimir();
    }

    @FXML
    public void handleCerrar() {
        Stage stage = (Stage) txtVistaPrevia.getScene().getWindow();
        stage.close();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje);
        a.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje);
        a.showAndWait();
    }
}