package org.example.controlador;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private Button btnLogin;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    @FXML
    public void initialize() {
        Connection con = ConexionDB.getConexion();
        if (con != null) {
            System.out.println("BD conectada ✅");
        }
    }

    @FXML
    public void handleIniciarSesion(ActionEvent event) throws IOException {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtPassword.getText().trim();

        // Validar que no esten vacios
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor ingresa usuario y contraseña.");
            return;
        }

        // Validar cen la base de datos
        if (validarCredenciales(usuario, contrasena)) {
            // Verificar si ya hay caja abierta
            if (hayCajaAbierta()) {
                // Ir directo al menú
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/MenuPrincipal.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setMaximized(true);
                stage.show();
            } else {
                // Ir a apertura de caja
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/AperturaCaja.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Solo cambia el root, sin tocar initStyle
                stage.getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
                stage.getScene().setRoot(root);

                stage.setMaximized(false);
                stage.setWidth(520);
                stage.setHeight(600);
                stage.centerOnScreen();

            }
        } else {
            mostrarAlerta("Error", "Usuario o contraseña incorrectos.");
            txtPassword.clear();
        }
    }
    private boolean hayCajaAbierta() {
        String sql = "SELECT id_caja FROM caja WHERE estado = 'abierta' AND id_usuario = ? ORDER BY fecha_apertura DESC LIMIT 1";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, SesionUsuario.getInstancia().getIdUsuario());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Guardar el id_caja en sesión
                SesionUsuario.getInstancia().setIdCaja(rs.getInt("id_caja"));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean validarCredenciales(String usuario, String contrasena) {
        String sql = """
        SELECT u.id_usuario, u.nombre, u.usuario, u.id_rol, r.nombre AS rol
        FROM usuarios u
        JOIN roles r ON u.id_rol = r.id_rol
        WHERE u.usuario = ? AND u.contrasena = ? AND u.activo = 1
    """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, contrasena);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Guardar datos en sesión
                SesionUsuario sesion = SesionUsuario.getInstancia();
                sesion.setIdUsuario(rs.getInt("id_usuario"));
                sesion.setNombre(rs.getString("nombre"));
                sesion.setUsuario(rs.getString("usuario"));
                sesion.setIdRol(rs.getInt("id_rol"));
                sesion.setRol(rs.getString("rol"));
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
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