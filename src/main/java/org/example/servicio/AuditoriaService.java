package org.example.servicio;

import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
            registrarDetalle(idUsuario, null, accion, tabla, tabla, idRegistro, detalle, null, null);
        } catch (Exception e) {
            e.printStackTrace(); // auditoría nunca debe romper el flujo principal
        }
    }

    public void registrarDetalle(int idUsuario, Integer idAdminAutorizo, String accion, String modulo,
                                 String tabla, int idRegistro, String descripcion,
                                 String datosAntes, String datosDespues) {
        String sql = """
            INSERT INTO auditoria_detalle (
                id_usuario, id_admin_autorizo, accion, modulo, tabla_afectada,
                id_registro, descripcion, datos_antes, datos_despues
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null || !tablaExiste(con, "auditoria_detalle")) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                if (idAdminAutorizo == null || idAdminAutorizo <= 0) ps.setNull(2, java.sql.Types.INTEGER);
                else ps.setInt(2, idAdminAutorizo);
                ps.setString(3, accion);
                ps.setString(4, modulo);
                ps.setString(5, tabla);
                ps.setInt(6, idRegistro);
                ps.setString(7, descripcion);
                ps.setString(8, datosAntes);
                ps.setString(9, datosDespues);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean tablaExiste(Connection con, String tabla) throws Exception {
        DatabaseMetaData meta = con.getMetaData();
        try (ResultSet rs = meta.getTables(con.getCatalog(), null, tabla, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
}
