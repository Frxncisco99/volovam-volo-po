package org.example.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AperturaCajaController {

    @FXML private Label lblFecha;
    @FXML private Label lblUsuario;
    @FXML private Label lblEjemplo;
    @FXML private TextField txtMontoInicial;
    @FXML private TextField txtTipoCambio;

    @FXML
    public void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblFecha.setText(LocalDateTime.now().format(formatter));
        lblUsuario.setText("Cajero: " + SesionUsuario.getInstancia().getNombre());

        // Actualizar ejemplo
        txtTipoCambio.textProperty().addListener((obs, old, nuevo) -> {
            try {
                double tc = Double.parseDouble(nuevo);
                lblEjemplo.setText(String.format("Ej: $1 USD = $%.2f MXN", tc));
            } catch (NumberFormatException e) {
                lblEjemplo.setText("Ej: $1 USD = $0.00 MXN");
            }
        });
    }

    @FXML
    public void handleAbrirCaja(ActionEvent event) {
        String montoTexto = txtMontoInicial.getText().trim();
        String tipoCambioTexto = txtTipoCambio.getText().trim();

        if (montoTexto.isEmpty()) {
            mostrarAlerta("Campo vacío", "Ingresa el monto inicial.");
            return;
        }
        if (tipoCambioTexto.isEmpty()) {
            mostrarAlerta("Campo vacío", "Ingresa el tipo de cambio del dólar.");
            return;
        }

        try {
            double monto = Double.parseDouble(montoTexto);
            double tipoCambio = Double.parseDouble(tipoCambioTexto);

            if (tipoCambio <= 0) {
                mostrarAlerta("Error", "El tipo de cambio debe ser mayor a 0.");
                return;
            }

            String sql = "INSERT INTO caja (fecha_apertura, monto_inicial, estado, id_usuario, tipo_cambio_dolar) " +
                    "VALUES (NOW(), ?, 'abierta', ?, ?)";

            try (Connection con = ConexionDB.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setDouble(1, monto);
                ps.setInt(2, SesionUsuario.getInstancia().getIdUsuario());
                ps.setDouble(3, tipoCambio);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    SesionUsuario.getInstancia().setIdCaja(rs.getInt(1));
                    SesionUsuario.getInstancia().setTipoCambioDolar(tipoCambio);
                }
            }

            // Ir a MenuPrincipal
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Ingresa números válidos.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la caja.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}