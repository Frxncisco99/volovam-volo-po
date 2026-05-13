package org.example.servicio;

import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import org.example.dao.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AutorizacionAdminService {

    public static boolean solicitar(String accion) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Autorizacion requerida");
        dialog.setHeaderText("Se requiere usuario administrador");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuario administrador");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contrasena");

        Label lblAccion = new Label(accion);
        lblAccion.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        VBox contenido = new VBox(10, lblAccion, txtUsuario, txtPassword);
        contenido.setStyle("-fx-padding: 14; -fx-min-width: 320;");
        dialog.getDialogPane().setContent(contenido);

        return dialog.showAndWait()
                .filter(ButtonType.OK::equals)
                .map(r -> validarAdmin(txtUsuario.getText().trim(), txtPassword.getText().trim()))
                .orElse(false);
    }

    private static boolean validarAdmin(String usuario, String password) {
        if (usuario.isEmpty() || password.isEmpty()) return false;
        String sql = """
                SELECT 1
                FROM usuarios u
                JOIN roles r ON r.id_rol = u.id_rol
                WHERE u.usuario = ?
                  AND u.contrasena = ?
                  AND u.activo = 1
                  AND LOWER(r.nombre) = 'admin'
                LIMIT 1
                """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
