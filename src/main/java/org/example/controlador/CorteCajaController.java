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
import java.util.Locale;

public class CorteCajaController {

    @FXML private Label lblFechaHoy;
    @FXML private Label lblIdCaja;
    @FXML private Label lblCajero;
    @FXML private Label lblHoraApertura;
    @FXML private Label lblFondoInicial;
    @FXML private Label lblNumTickets;
    @FXML private Label lblTotalEfectivo;
    @FXML private Label lblTotalEntradas;
    @FXML private Label lblTotalSalidas;
    @FXML private Label lblDineroEsperado;
    @FXML private Label lblDiferencia;
    @FXML private Label lblEstadoDiferencia;
    @FXML private Label lblObsRequerida;
    @FXML private TextField txtDineroContado;
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    private double fondoInicial = 0;
    private double totalEfectivo = 0;
    private double totalEntradas = 0;
    private double totalSalidas = 0;
    private double dineroEsperado = 0;

    @FXML
    public void initialize() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
        String fecha = LocalDateTime.now().format(fmt);
        lblFechaHoy.setText(fecha.substring(0, 1).toUpperCase() + fecha.substring(1));

        lblIdCaja.setText("Caja #" + sesion.getIdCaja());
        lblCajero.setText(sesion.getNombre());

        cargarResumen();

        // Diferencia en tiempo real
        txtDineroContado.textProperty().addListener((obs, old, nuevo) -> calcularDiferencia(nuevo));

        // Ocultar label de obs requerida por defecto
        lblObsRequerida.setVisible(false);
    }

    private void cargarResumen() {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();

        try (Connection con = ConexionDB.getConexion()) {

            // Info de apertura
            String sqlCaja = "SELECT monto_inicial, fecha_apertura FROM caja WHERE id_caja = ?";
            PreparedStatement psCaja = con.prepareStatement(sqlCaja);
            psCaja.setInt(1, idCaja);
            ResultSet rsCaja = psCaja.executeQuery();
            if (rsCaja.next()) {
                fondoInicial = rsCaja.getDouble("monto_inicial");
                String horaApertura = rsCaja.getTimestamp("fecha_apertura")
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                lblFondoInicial.setText("$" + String.format("%.2f", fondoInicial));
                lblHoraApertura.setText(horaApertura);
            }

            // Ventas
            String sqlVentas = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM ventas WHERE id_caja = ?";
            PreparedStatement psVentas = con.prepareStatement(sqlVentas);
            psVentas.setInt(1, idCaja);
            ResultSet rsVentas = psVentas.executeQuery();
            if (rsVentas.next()) {
                lblNumTickets.setText(String.valueOf(rsVentas.getInt(1)));
                totalEfectivo = rsVentas.getDouble(2);
                lblTotalEfectivo.setText("$" + String.format("%.2f", totalEfectivo));
            }

            // Movimientos de caja
            String sqlEntradas = "SELECT COALESCE(SUM(monto), 0) FROM movimientos_caja WHERE id_caja = ? AND tipo = 'INGRESO'";
            String sqlSalidas = "SELECT COALESCE(SUM(monto), 0) FROM movimientos_caja WHERE id_caja = ? AND tipo = 'RETIRO'";

            PreparedStatement psEnt = con.prepareStatement(sqlEntradas);
            psEnt.setInt(1, idCaja);
            ResultSet rsEnt = psEnt.executeQuery();
            if (rsEnt.next()) {
                totalEntradas = rsEnt.getDouble(1);
                lblTotalEntradas.setText("$" + String.format("%.2f", totalEntradas));
            }

            PreparedStatement psSal = con.prepareStatement(sqlSalidas);
            psSal.setInt(1, idCaja);
            ResultSet rsSal = psSal.executeQuery();
            if (rsSal.next()) {
                totalSalidas = rsSal.getDouble(1);
                lblTotalSalidas.setText("$" + String.format("%.2f", totalSalidas));
            }

            // Dinero esperado = fondo + ventas + entradas - salidas
            dineroEsperado = fondoInicial + totalEfectivo + totalEntradas - totalSalidas;
            lblDineroEsperado.setText("$" + String.format("%.2f", dineroEsperado));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calcularDiferencia(String texto) {
        try {
            double contado = Double.parseDouble(texto);
            double diferencia = contado - dineroEsperado;
            lblDiferencia.setText("$" + String.format("%.2f", diferencia));

            if (diferencia == 0) {
                lblDiferencia.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #3B6D11;");
                lblEstadoDiferencia.setText("Todo correcto");
                lblEstadoDiferencia.setStyle("-fx-text-fill: #3B6D11; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblObsRequerida.setVisible(false);
            } else if (diferencia < 0) {
                lblDiferencia.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #C0392B;");
                lblEstadoDiferencia.setText("Falta dinero");
                lblEstadoDiferencia.setStyle("-fx-text-fill: #C0392B; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblObsRequerida.setVisible(true);
            } else {
                lblDiferencia.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A6DB5;");
                lblEstadoDiferencia.setText("Sobra dinero");
                lblEstadoDiferencia.setStyle("-fx-text-fill: #1A6DB5; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblObsRequerida.setVisible(true);
            }
        } catch (NumberFormatException e) {
            lblDiferencia.setText("$0.00");
            lblEstadoDiferencia.setText("");
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

        double diferencia = contado - dineroEsperado;

        // Si hay diferencia, observaciones son obligatorias
        if (diferencia != 0 && txtObservaciones.getText().trim().isEmpty()) {
            mostrarAlerta("Observaciones requeridas", "Hay una diferencia de $" + String.format("%.2f", diferencia) + ". Debes escribir una observacion.");
            txtObservaciones.requestFocus();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar caja");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Seguro que deseas cerrar la caja? Esta accion no se puede deshacer.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                cerrarCajaEnBD(contado, diferencia);
            }
        });
    }

    private void cerrarCajaEnBD(double montoReal, double diferencia) {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();
        int idUsuario = SesionUsuario.getInstancia().getIdUsuario();

        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);

            // 1. Cerrar la caja
            String sqlCerrar = "UPDATE caja SET estado = 'cerrada', fecha_cierre = NOW(), monto_final = ? WHERE id_caja = ?";
            PreparedStatement psCerrar = con.prepareStatement(sqlCerrar);
            psCerrar.setDouble(1, montoReal);
            psCerrar.setInt(2, idCaja);
            psCerrar.executeUpdate();

            // 2. Guardar corte
            String sqlCorte = "INSERT INTO corte_caja (id_caja, id_usuario, fecha_apertura, fecha_cierre, fondo_inicial, total_ventas, total_entradas, total_salidas, dinero_esperado, dinero_real, diferencia, observaciones) VALUES (?, ?, (SELECT fecha_apertura FROM caja WHERE id_caja = ?), NOW(), ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psCorte = con.prepareStatement(sqlCorte);
            psCorte.setInt(1, idCaja);
            psCorte.setInt(2, idUsuario);
            psCorte.setInt(3, idCaja);
            psCorte.setDouble(4, fondoInicial);
            psCorte.setDouble(5, totalEfectivo);
            psCorte.setDouble(6, totalEntradas);
            psCorte.setDouble(7, totalSalidas);
            psCorte.setDouble(8, dineroEsperado);
            psCorte.setDouble(9, montoReal);
            psCorte.setDouble(10, diferencia);
            psCorte.setString(11, txtObservaciones.getText().trim());
            psCorte.executeUpdate();

            con.commit();

            SesionUsuario.getInstancia().setIdCaja(0);
            mostrarInfo("Caja cerrada", "El corte se registro correctamente.");
            Platform.exit();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cerrar la caja.");
        }
    }

    @FXML public void irADashboard() { navegar("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML public void irAVentas() { navegar("/org/example/vista/Ventas.fxml"); }
    @FXML public void irAEmpleados() { navegar("/org/example/vista/Empleados.fxml"); }

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
            Stage stage = (Stage) lblFechaHoy.getScene().getWindow();
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