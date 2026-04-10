package org.example.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.modelo.SesionUsuario;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class MenuPrincipal implements Initializable {

    @FXML private Label lblFecha;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;
    @FXML private Label lblVentasHoy;
    @FXML private Label lblVentasDelta;
    @FXML private Label lblPedidosHoy;
    @FXML private Label lblProductosBajos;
    @FXML private Label lblEmpleadosActivos;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mostrarFecha();
        cargarDatosUsuario();
    }

    @FXML
    private void irAVentas() {
        cambiarEscena("/org/example/vista/Ventas.fxml");
    }

    @FXML
    private void abrirInventario() {
        cambiarEscena("/org/example/vista/Inventario.fxml");
    }

    private void cambiarEscena(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) lblFecha.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarFecha() {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
        String fechaHoy = LocalDate.now().format(formatter);
        lblFecha.setText(fechaHoy.substring(0, 1).toUpperCase() + fechaHoy.substring(1));
    }

    private void cargarDatosUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        String nombre = sesion.getNombre();
        String rol = sesion.getRol();
        lblNombreUsuario.setText(nombre);
        lblRolUsuario.setText(rol);
        String iniciales = nombre.length() >= 2
                ? nombre.substring(0, 2).toUpperCase()
                : nombre.toUpperCase();
        lblAvatarIniciales.setText(iniciales);
    }

    @FXML
    private void btnCerrar(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Salir");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas salir?");
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                Platform.exit();
            }
        });
    }
    public void abrirInventarioDesdeAfuera() {
        cambiarEscena("/org/example/vista/Inventario.fxml");
    }

    @FXML
    private void irAEmpleados() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Empleados.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblFecha.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void irACorteCaja() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/CorteCaja.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblFecha.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblFecha.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void irAReportes(ActionEvent actionEvent) {
        cambiarEscena("/org/example/vista/Reportes.fxml");
    }
}