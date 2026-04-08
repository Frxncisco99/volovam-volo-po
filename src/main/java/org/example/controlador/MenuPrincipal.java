package org.example.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.modelo.SesionUsuario;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class MenuPrincipal implements Initializable {

    public ProgressBar barCroissant;

    @FXML private Button btnInventario;
    @FXML private Button btnDashboard;
    @FXML private Button btnVentas;

    @FXML
    private VBox contenedorCentral;
    // Topbar
    @FXML private Label lblFecha;

    // Usuario en sidebar
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    // Métricas del día
    @FXML private Label lblVentasHoy;
    @FXML private Label lblVentasDelta;
    @FXML private Label lblPedidosHoy;
    @FXML private Label lblProductosBajos;
    @FXML private Label lblEmpleadosActivos;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mostrarFecha();
        cargarDatosUsuario();
        //cargarResumenDia();
    }
    @FXML
    private void irAVentas() {
        activarMenu("ventas");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/Ventas.fxml"));
            Parent vista = loader.load();

            contenedorCentral.getChildren().clear();
            contenedorCentral.getChildren().add(vista);

            resetMenuActivo(); // opcional si ya lo tienes

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void abrirInventario() {
        cargarVista("/org/example/vista/Inventario.fxml");
        activarMenu("inventario");
    } private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxml));
            Parent vista = loader.load();

            contenedorCentral.getChildren().clear();
            contenedorCentral.getChildren().add(vista);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarFecha() {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
        String fechaHoy = LocalDate.now().format(formatter);
        // Capitaliza la primera letra
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

    private void cargarResumenDia() {

        // Por ahora son valores ejemplo

        lblVentasHoy.setText("$1,840");
        lblVentasDelta.setText("+12% vs ayer");
        lblVentasDelta.setStyle(lblVentasDelta.getStyle() + "; -fx-text-fill: #3B6D11;");

        lblPedidosHoy.setText("34");
        lblProductosBajos.setText("6");
        lblEmpleadosActivos.setText("4");
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
    @FXML


        private void cerrarSesion() {
        // TODO: Volver al Login.fxml
        System.out.println("Cerrando sesión...");
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/org/example/vista/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage)
                    lblFecha.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetMenuActivo() {

        btnDashboard.setStyle("-fx-background-color: transparent; -fx-text-fill: #C8A97E;");
        btnVentas.setStyle("-fx-background-color: transparent; -fx-text-fill: #C8A97E;");
        btnInventario.setStyle("-fx-background-color: transparent; -fx-text-fill: #C8A97E;");

        // Opcional: marcar activo el actual (ventas en este caso)
        btnVentas.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white;");
    }

    private void activarMenu(String opcion) {

        btnDashboard.setStyle("-fx-background-color: transparent; -fx-text-fill: #C8A97E;");
        btnVentas.setStyle("-fx-background-color: transparent; -fx-text-fill: #C8A97E;");
        btnInventario.setStyle("-fx-background-color: transparent; -fx-text-fill: #C8A97E;");

        switch (opcion) {
            case "dashboard":
                btnDashboard.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white;");
                break;

            case "ventas":
                btnVentas.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white;");
                break;

            case "inventario":
                btnInventario.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white;");
                break;
        }
    }

}
