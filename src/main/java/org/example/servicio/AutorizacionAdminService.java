package org.example.servicio;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.dao.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AutorizacionAdminService {

    private static final PasswordService passwordService = new PasswordService();

    public static boolean solicitar(String accion) {
        return solicitarAutorizacion(accion, PermisoService.PERMISOS_GESTIONAR).autorizado();
    }

    public static Autorizacion solicitarAutorizacion(String accion, String permisoRequerido) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Autorizacion requerida");
        dialog.setHeaderText("Se requiere un administrador autorizado");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuario administrador");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contrasena");
        Label lblAccion = new Label(accion);
        lblAccion.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        VBox contenido = new VBox(10, lblAccion, txtUsuario, txtPassword);
        contenido.setStyle("-fx-padding: 14; -fx-min-width: 340;");
        dialog.getDialogPane().setContent(contenido);

        return dialog.showAndWait()
                .filter(ButtonType.OK::equals)
                .map(r -> validarAdmin(txtUsuario.getText().trim(), txtPassword.getText(), permisoRequerido))
                .orElse(Autorizacion.denegada());
    }

    private static Autorizacion validarAdmin(String usuario, String password, String permisoRequerido) {
        if (usuario.isEmpty() || password.isEmpty()) return Autorizacion.denegada();
        String sql = """
                SELECT u.id_usuario, u.nombre, u.password_hash, u.contrasena, u.es_admin_principal
                FROM usuarios u
                JOIN roles r ON r.id_rol = u.id_rol
                WHERE u.usuario = ?
                  AND u.activo = 1
                  AND LOWER(r.nombre) = 'admin'
                LIMIT 1
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return Autorizacion.denegada();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, usuario);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return Autorizacion.denegada();
                int idAdmin = rs.getInt("id_usuario");
                boolean passwordOk = passwordService.verificarYActualizarSiEsPlano(
                        idAdmin,
                        password,
                        rs.getString("password_hash"),
                        rs.getString("contrasena")
                );
                if (!passwordOk) return Autorizacion.denegada();
                if (!rs.getBoolean("es_admin_principal") && !PermisoService.tienePermiso(idAdmin, permisoRequerido)) {
                    return Autorizacion.denegada();
                }
                return new Autorizacion(true, idAdmin, rs.getString("nombre"));
            }
        } catch (Exception e) {
            return Autorizacion.denegada();
        }
    }

    public record Autorizacion(boolean autorizado, int idAdmin, String nombreAdmin) {
        static Autorizacion denegada() {
            return new Autorizacion(false, 0, "");
        }
    }
}
