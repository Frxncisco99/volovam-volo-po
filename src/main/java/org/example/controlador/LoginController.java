package org.example.controlador;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyCode;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.servicio.AppExitService;
import org.example.servicio.MarcaService;
import org.example.servicio.PasswordService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private Button btnLogin;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    private final PasswordService passwordService = new PasswordService();

    @FXML
    public void initialize() {
        // Solo verificar que conecta, sin dejar conexión abierta
        try (Connection con = ConexionDB.getConexion()) {
            if (con != null) System.out.println("BD lista");
        } catch (Exception e) {
            e.printStackTrace();
        }
        txtPassword.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    handleIniciarSesion(new ActionEvent(btnLogin, btnLogin));
                } catch (IOException ex) {
                    mostrarAlerta("Error", "No se pudo iniciar sesion.");
                }
            }
        });
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

        // Validar la base de datos
        if (validarCredenciales(usuario, contrasena)) {
            // Verificar si hay caja abierta
            if (hayCajaAbierta()) {
                // Ir directo al menú
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/MenuPrincipal.fxml"));
                Parent root = loader.load();
                MarcaService.aplicar(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setMaximized(true);
                stage.show();
            } else {
                // Ir a apertura de caja
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/AperturaCaja.fxml"));
                Parent root = loader.load();
                MarcaService.aplicar(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setMaximized(false);
                stage.setWidth(500);
                stage.setHeight(400);
                stage.centerOnScreen();
                stage.show();
            }
        } else {
            mostrarAlerta("Error", "Usuario o contraseña incorrectos.");
            txtPassword.clear();
        }
    }
    private boolean hayCajaAbierta() {
        String sql = "SELECT id_caja, tipo_cambio_dolar FROM caja WHERE estado = 'abierta' ORDER BY fecha_apertura DESC LIMIT 1";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SesionUsuario.getInstancia().setIdCaja(rs.getInt("id_caja"));
                SesionUsuario.getInstancia().setTipoCambioDolar(rs.getDouble("tipo_cambio_dolar"));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean validarCredenciales(String usuario, String contrasena) {
        String sql = """
        SELECT u.id_usuario, u.nombre, u.usuario, u.id_rol, r.nombre AS rol,
               u.password_hash, u.contrasena
        FROM usuarios u
        JOIN roles r ON u.id_rol = r.id_rol
        WHERE u.usuario = ? AND u.activo = 1
    """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean passwordOk = passwordService.verificarYActualizarSiEsPlano(
                        rs.getInt("id_usuario"),
                        contrasena,
                        rs.getString("password_hash"),
                        rs.getString("contrasena")
                );
                if (!passwordOk) return false;
                SesionUsuario sesion = SesionUsuario.getInstancia();
                sesion.setIdUsuario(rs.getInt("id_usuario"));
                sesion.setNombre(rs.getString("nombre"));
                sesion.setUsuario(rs.getString("usuario"));
                sesion.setIdRol(rs.getInt("id_rol"));
                sesion.setRol(rs.getString("rol"));

                // ── Registrar login en auditoría ──────────────────────────
                registrarLogin(rs.getInt("id_usuario"), rs.getString("nombre"));

                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    public void salirAplicacion() {
        AppExitService.salir(btnLogin);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    private void registrarLogin(int idUsuario, String nombre) {
        String sql = "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, detalle) " +
                "VALUES (?, 'LOGIN', 'usuarios', ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setString(3, "Inicio de sesión: " + nombre +
                    " desde " + obtenerIP());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String obtenerIP() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "desconocida";
        }
    }
}
