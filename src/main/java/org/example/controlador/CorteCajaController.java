package org.example.controlador;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CorteCajaController {

    @FXML private Label lblFecha;
    @FXML private Label lblMontoInicial;
    @FXML private Label lblTotalVendido;
    @FXML private Label lblTotalEsperado;
    @FXML private Label lblNumVentas;
    @FXML private Label lblDiferencia;
    @FXML private TextField txtDineroContado;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    private double montoInicial = 0;
    private double totalVendido = 0;
    private double totalEsperado = 0;

    @FXML
    public void initialize() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new java.util.Locale("es", "MX"));
        String fecha = LocalDateTime.now().format(formatter);
        lblFecha.setText(fecha.substring(0, 1).toUpperCase() + fecha.substring(1));

        cargarResumen();

        // Calcular diferencia en tiempo real
        txtDineroContado.textProperty().addListener((obs, old, nuevo) -> {
            try {
                double contado = Double.parseDouble(nuevo);
                double diferencia = contado - totalEsperado;
                lblDiferencia.setText("$" + String.format("%.2f", diferencia));
                lblDiferencia.setStyle(diferencia >= 0
                        ? "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3B6D11;"
                        : "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #C0392B;");
            } catch (NumberFormatException e) {
                lblDiferencia.setText("$0.00");
            }
        });
    }

    private void cargarResumen() {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();

        String sqlCaja = "SELECT monto_inicial FROM caja WHERE id_caja = ?";
        String sqlVentas = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM ventas WHERE id_caja = ?";

        try (Connection con = ConexionDB.getConexion()) {
            // Monto inicial
            PreparedStatement psCaja = con.prepareStatement(sqlCaja);
            psCaja.setInt(1, idCaja);
            ResultSet rsCaja = psCaja.executeQuery();
            if (rsCaja.next()) {
                montoInicial = rsCaja.getDouble(1);
                lblMontoInicial.setText("$" + String.format("%.2f", montoInicial));
            }

            // Ventas del día
            PreparedStatement psVentas = con.prepareStatement(sqlVentas);
            psVentas.setInt(1, idCaja);
            ResultSet rsVentas = psVentas.executeQuery();
            if (rsVentas.next()) {
                int numVentas = rsVentas.getInt(1);
                totalVendido = rsVentas.getDouble(2);
                totalEsperado = montoInicial + totalVendido;

                lblNumVentas.setText(String.valueOf(numVentas));
                lblTotalVendido.setText("$" + String.format("%.2f", totalVendido));
                lblTotalEsperado.setText("$" + String.format("%.2f", totalEsperado));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCerrarCaja() {
        String textoContado = txtDineroContado.getText().trim();
        if (textoContado.isEmpty()) {
            mostrarAlerta("Campo vacío", "Ingresa el dinero contado.");
            return;
        }

        double contado;
        try {
            contado = Double.parseDouble(textoContado);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Ingresa un número válido.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar caja");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Seguro que deseas cerrar la caja? Esta accion no se puede deshacer.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                cerrarCajaEnBD(contado);
            }
        });
    }

    private void cerrarCajaEnBD(double montoFinal) {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();
        String sql = "UPDATE caja SET estado = 'cerrada', fecha_cierre = NOW(), monto_final = ? WHERE id_caja = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, montoFinal);
            ps.setInt(2, idCaja);
            ps.executeUpdate();

            // Limpiar caja de sesión
            SesionUsuario.getInstancia().setIdCaja(0);

            mostrarInfo("Caja cerrada", "La caja se cerro correctamente. El sistema se cerrara.");
            Platform.exit();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cerrar la caja.");
        }
    }

    @FXML
    public void irADashboard() {
        navegar("/org/example/vista/MenuPrincipal.fxml");
    }

    @FXML
    public void irAVentas() {
        navegar("/org/example/vista/Ventas.fxml");
    }

    @FXML
    public void irAEmpleados() {
        navegar("/org/example/vista/Empleados.fxml");
    }

    @FXML
    public void btnCerrar() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Salir");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas salir?");
        alerta.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) Platform.exit();
        });
    }

    private void navegar(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) lblFecha.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}