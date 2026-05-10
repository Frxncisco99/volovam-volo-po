package org.example.servicio;

import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuditoriaService {

    private static final AuditoriaService INSTANCE = new AuditoriaService();
    public static AuditoriaService get() { return INSTANCE; }

    public void registrar(String accion, String tabla, int idRegistro, String detalle) {
        registrar(SesionUsuario.getInstancia().getIdUsuario(), accion, tabla, idRegistro, detalle);
    }

    public void registrar(int idUsuario, String accion, String tabla,
                          int idRegistro, String detalle) {
        String sql = """
            INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, detalle)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, accion);
            ps.setString(3, tabla);
            ps.setInt(4, idRegistro);
            ps.setString(5, detalle);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace(); // auditoría nunca debe romper el flujo principal
        }
    }
}