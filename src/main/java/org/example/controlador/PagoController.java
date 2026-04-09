package org.example.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;           // ← NUEVO
import javafx.scene.Parent;              // ← NUEVO
import javafx.scene.Scene;              // ← NUEVO
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.modelo.Ticket;        // ← NUEVO
import org.example.servicio.TicketService; // ← NUEVO

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class PagoController {

    @FXML private Label lblTotalPagar;
    @FXML private TextField txtDineroRecibido;
    @FXML private Label lblCambio;

    private double total;
    private Map<Integer, Object[]> carrito;
    private VentasController ventasController;

    private final TicketService ticketService = new TicketService(); // ← NUEVO

    public void setDatos(double total, Map<Integer, Object[]> carrito, VentasController ventasController) {
        this.total = total;
        this.carrito = carrito;
        this.ventasController = ventasController;
        lblTotalPagar.setText("$" + String.format("%.2f", total));


        // Calcular cambio en tiempo real
        txtDineroRecibido.textProperty().addListener((obs, old, nuevo) -> {
            try {
                double recibido = Double.parseDouble(nuevo);
                double cambio = recibido - total;
                if (cambio >= 0) {
                    lblCambio.setText("$" + String.format("%.2f", cambio));
                    lblCambio.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #3B6D11;");
                } else {
                    lblCambio.setText("Insuficiente");
                    lblCambio.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #C0392B;");
                }
            } catch (NumberFormatException e) {
                lblCambio.setText("$0.00");
            }
        });
        txtDineroRecibido.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleConfirmar();
            }
        });
    }

    @FXML
    public void handleConfirmar() {
        String textoRecibido = txtDineroRecibido.getText().trim();
        if (textoRecibido.isEmpty()) {
            mostrarAlerta("Campo vacío", "Ingresa el dinero recibido.");
            return;
        }

        double recibido;
        try {
            recibido = Double.parseDouble(textoRecibido);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Ingresa un número válido.");
            return;
        }

        if (recibido < total) {
            mostrarAlerta("Pago insuficiente", "El dinero recibido es menor al total.");
            return;
        }

        double cambio = recibido - total;

        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);

            // 1. Insertar venta — SIN CAMBIOS
            String sqlVenta = "INSERT INTO ventas (total, id_usuario, id_caja) VALUES (?, ?, ?)";
            PreparedStatement psVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            psVenta.setDouble(1, total);
            psVenta.setInt(2, SesionUsuario.getInstancia().getIdUsuario());
            psVenta.setInt(3, SesionUsuario.getInstancia().getIdCaja());
            psVenta.executeUpdate();

            ResultSet rs = psVenta.getGeneratedKeys();
            rs.next();
            int idVenta = rs.getInt(1);

            // 2. Insertar detalle y descontar stock — SIN CAMBIOS
            String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            String sqlStock = "UPDATE productos SET stock = stock - ? WHERE id_producto = ?";

            for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
                int idProducto = entry.getKey();
                double precio = (double) entry.getValue()[1];
                int cantidad = (int) entry.getValue()[2];
                double subtotal = precio * cantidad;

                PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                psDetalle.setInt(1, idVenta);
                psDetalle.setInt(2, idProducto);
                psDetalle.setInt(3, cantidad);
                psDetalle.setDouble(4, precio);
                psDetalle.setDouble(5, subtotal);
                psDetalle.executeUpdate();

                PreparedStatement psStock = con.prepareStatement(sqlStock);
                psStock.setInt(1, cantidad);
                psStock.setInt(2, idProducto);
                psStock.executeUpdate();
            }

            // 3. Insertar pago — SIN CAMBIOS
            String sqlPago = "INSERT INTO pagos (id_venta, monto_recibido, cambio) VALUES (?, ?, ?)";
            PreparedStatement psPago = con.prepareStatement(sqlPago);
            psPago.setInt(1, idVenta);
            psPago.setDouble(2, recibido);
            psPago.setDouble(3, cambio);
            psPago.executeUpdate();

            con.commit();

            // ── NUEVO: Generar ticket en memoria (sin tocar BD, usa datos ya disponibles)
            Ticket ticket = ticketService.generarDesdeMemoria(
                    idVenta,
                    carrito,
                    total,
                    recibido,
                    cambio,
                    SesionUsuario.getInstancia().getNombre(),
                    SesionUsuario.getInstancia().getIdCaja()
            );

            // Cerrar ventana de pago — SIN CAMBIOS
            Stage stage = (Stage) txtDineroRecibido.getScene().getWindow();
            stage.close();

            // Limpiar carrito — SIN CAMBIOS
            ventasController.ventaCompletada();

            // ── NUEVO: Abrir vista previa del ticket
            abrirVistaTicket(ticket);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar la venta.");
        }
    }

    // ── NUEVO: Abre la ventana de vista previa del ticket ───────────────────
    private void abrirVistaTicket(Ticket ticket) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/Ticket.fxml")
            );
            Parent root = loader.load();

            TicketController ticketController = loader.getController();
            ticketController.setTicket(ticket);

            Stage stageTicket = new Stage();
            stageTicket.setTitle("Ticket de Venta #" + ticket.getIdVenta());
            stageTicket.setScene(new Scene(root));
            stageTicket.setResizable(false);
            stageTicket.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stageTicket.show();
        } catch (Exception e) {
            e.printStackTrace();
            // La venta ya quedó guardada — el ticket no es crítico
            mostrarAlerta("Aviso", "La venta se guardó correctamente pero no se pudo abrir el ticket.");
        }
    }

    // ── SIN CAMBIOS ──────────────────────────────────────────────────────────

    @FXML
    public void handleCancelar() {
        Stage stage = (Stage) txtDineroRecibido.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}