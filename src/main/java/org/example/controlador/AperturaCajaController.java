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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AperturaCajaController {

    @FXML private Label lblFecha;
    @FXML private Label lblUsuario;
    @FXML private TextField txtMontoInicial;



    @FXML
    public void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblFecha.setText(LocalDateTime.now().format(formatter));
        lblUsuario.setText("Cajero: " + SesionUsuario.getInstancia().getNombre());
    }

    @FXML
    public void handleAbrirCaja(ActionEvent event) {
        String montoTexto = txtMontoInicial.getText().trim();

        if (montoTexto.isEmpty()) {
            mostrarAlerta("Campo vacío", "Ingresa el monto inicial.");
            return;
        }

        try {
            double monto = Double.parseDouble(montoTexto);

            String sql = "INSERT INTO caja (fecha_apertura, monto_inicial, estado, id_usuario) VALUES (NOW(), ?, 'abierta', ?)";
            try (Connection con = ConexionDB.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setDouble(1, monto);
                ps.setInt(2, SesionUsuario.getInstancia().getIdUsuario());
                ps.executeUpdate();

                // Guardar el id_caja en sesión
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    SesionUsuario.getInstancia().setIdCaja(rs.getInt(1));
                }
            }

            // Ir al MenuPrincipal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Ingresa un número válido.");
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