package org.example.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class MenuPrincipal implements Initializable {

    public Button btnDashboard;
    public Button btnVentas;
    public ProgressBar barCroissant;
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

    private void mostrarFecha() {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
        String fechaHoy = LocalDate.now().format(formatter);
        // Capitaliza la primera letra
        lblFecha.setText(fechaHoy.substring(0, 1).toUpperCase() + fechaHoy.substring(1));
    }

    private void cargarDatosUsuario() {
        // TODO: Reemplaza con el usuario real de tu sesión
        String nombre = "Pancho";
        String rol = "Gerente";

        lblNombreUsuario.setText(nombre);
        lblRolUsuario.setText(rol);

        // Iniciales del avatar
        String iniciales = nombre.length() >= 2
                ? nombre.substring(0, 2).toUpperCase()
                : nombre.toUpperCase();
        lblAvatarIniciales.setText(iniciales);
    }

    private void cargarResumenDia() {
        // TODO: Conecta estos valores a tu base de datos
        // Por ahora son valores de ejemplo

        lblVentasHoy.setText("$1,840");
        lblVentasDelta.setText("+12% vs ayer");
        lblVentasDelta.setStyle(lblVentasDelta.getStyle() + "; -fx-text-fill: #3B6D11;");

        lblPedidosHoy.setText("34");
        lblProductosBajos.setText("6");
        lblEmpleadosActivos.setText("4");
    }

    // ===== Navegación del sidebar =====

    @FXML
    private void mostrarDashboard() {
        // Ya estamos en dashboard, puedes refrescar datos si quieres
        cargarResumenDia();
    }

    @FXML
    private void mostrarVentas() {
        // TODO: Cargar Vista Ventas.fxml en el área central
        System.out.println("Navegar a Ventas");
    }

    @FXML
    private void mostrarInventario() {
        // TODO: Cargar Inventario.fxml
        System.out.println("Navegar a Inventario");
    }

    @FXML
    private void mostrarEmpleados() {
        // TODO: Cargar Empleados.fxml
        System.out.println("Navegar a Empleados");
    }

    @FXML
    private void mostrarReportes() {
        // TODO: Cargar Reportes.fxml
        System.out.println("Navegar a Reportes");
    }

    @FXML
    private void mostrarConfiguracion() {
        // TODO: Cargar Configuracion.fxml
        System.out.println("Navegar a Configuración");
    }

    @FXML
    private void btnCerrar(ActionEvent event) throws IOException {

        JOptionPane.showMessageDialog(null,"Saliendo del Programa","Alerta",JOptionPane.INFORMATION_MESSAGE);
        //System.out.println("Cerrando");


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
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
