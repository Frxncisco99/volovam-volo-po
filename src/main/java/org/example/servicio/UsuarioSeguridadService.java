package org.example.servicio;

import org.example.dao.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioSeguridadService {

    public boolean esAdminPrincipal(int idUsuario) {
        String sql = "SELECT es_admin_principal, usuario FROM usuarios WHERE id_usuario = ?";
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return idUsuario == 1;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return idUsuario == 1;
                return rs.getBoolean("es_admin_principal") || idUsuario == 1 || "admin".equalsIgnoreCase(rs.getString("usuario"));
            }
        } catch (Exception e) {
            return idUsuario == 1;
        }
    }

    public boolean puedeDesactivarUsuario(int idUsuario) {
        if (esAdminPrincipal(idUsuario)) return false;
        return !esUltimoAdminActivo(idUsuario);
    }

    public boolean puedeCambiarRol(int idUsuario, String nuevoRol) {
        if (esAdminPrincipal(idUsuario) && !"admin".equalsIgnoreCase(nuevoRol)) return false;
        return !"admin".equalsIgnoreCase(rolActual(idUsuario)) || "admin".equalsIgnoreCase(nuevoRol) || !esUltimoAdminActivo(idUsuario);
    }

    public boolean puedeQuitarPermiso(int idUsuario, String permiso) {
        if (!esAdminPrincipal(idUsuario)) return true;
        return !esPermisoCritico(permiso);
    }

    public String mensajeProteccionAdmin(int idUsuario) {
        if (esAdminPrincipal(idUsuario)) {
            return "No se puede desactivar el administrador principal del sistema.";
        }
        return "No puedes dejar el sistema sin un usuario administrador activo.";
    }

    private boolean esUltimoAdminActivo(int idUsuarioExcluido) {
        String sql = """
                SELECT COUNT(*)
                FROM usuarios u
                JOIN roles r ON r.id_rol = u.id_rol
                WHERE u.activo = 1
                  AND LOWER(r.nombre) = 'admin'
                  AND u.id_usuario <> ?
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuarioExcluido);
                ResultSet rs = ps.executeQuery();
                return rs.next() && rs.getInt(1) == 0;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String rolActual(int idUsuario) {
        String sql = """
                SELECT r.nombre
                FROM usuarios u
                JOIN roles r ON r.id_rol = u.id_rol
                WHERE u.id_usuario = ?
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return "";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean esPermisoCritico(String permiso) {
        return "PERMISOS_GESTIONAR".equals(permiso)
                || "USUARIOS_ACCEDER".equals(permiso)
                || "USUARIOS_EDITAR".equals(permiso)
                || "USUARIOS_DESACTIVAR".equals(permiso)
                || "CONFIGURACION_ACCEDER".equals(permiso);
    }
}
