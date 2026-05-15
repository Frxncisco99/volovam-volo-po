package org.example.servicio;

import org.example.dao.ConexionDB;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PasswordService {

    private static final int BCRYPT_COST = 12;

    public String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contrasena no puede estar vacia.");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    public boolean verificar(String password, String hash) {
        if (password == null || hash == null || hash.isBlank()) return false;
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verificarYActualizarSiEsPlano(int idUsuario, String passwordIngresado,
                                                  String passwordHash, String passwordPlanoActual) {
        if (verificar(passwordIngresado, passwordHash)) return true;
        if (passwordPlanoActual == null || passwordPlanoActual.isBlank()) return false;
        if (!passwordPlanoActual.equals(passwordIngresado)) return false;

        String nuevoHash = hash(passwordIngresado);
        String sql = "UPDATE usuarios SET password_hash = ?, contrasena = '', fecha_actualizacion_password = NOW() WHERE id_usuario = ?";
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return true;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nuevoHash);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    public boolean validarUsuarioActivo(String usuario, String password) {
        String sql = """
                SELECT id_usuario, password_hash, contrasena
                FROM usuarios
                WHERE usuario = ? AND activo = 1
                LIMIT 1
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, usuario);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return false;
                return verificarYActualizarSiEsPlano(
                        rs.getInt("id_usuario"),
                        password,
                        rs.getString("password_hash"),
                        rs.getString("contrasena")
                );
            }
        } catch (Exception e) {
            return false;
        }
    }
}
