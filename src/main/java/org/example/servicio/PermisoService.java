package org.example.servicio;

import javafx.scene.control.Alert;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PermisoService {

    public static final String VENTAS_ACCEDER = "VENTAS_ACCEDER";
    public static final String VENTAS_CANCELAR = "VENTAS_CANCELAR";
    public static final String VENTAS_ELIMINAR_PRODUCTO_CARRITO = "VENTAS_ELIMINAR_PRODUCTO_CARRITO";
    public static final String VENTAS_APLICAR_DESCUENTO = "VENTAS_APLICAR_DESCUENTO";
    public static final String VENTAS_COBRAR = "VENTAS_COBRAR";
    public static final String VENTAS_DEVOLVER = "VENTAS_DEVOLVER";
    public static final String PRODUCTOS_ACCEDER = "PRODUCTOS_ACCEDER";
    public static final String PRODUCTOS_CREAR = "PRODUCTOS_CREAR";
    public static final String PRODUCTOS_EDITAR = "PRODUCTOS_EDITAR";
    public static final String PRODUCTOS_ELIMINAR = "PRODUCTOS_ELIMINAR";
    public static final String INVENTARIO_AJUSTAR = "INVENTARIO_AJUSTAR";
    public static final String INVENTARIO_VER_COSTOS = "INVENTARIO_VER_COSTOS";
    public static final String CAJA_ACCEDER = "CAJA_ACCEDER";
    public static final String CAJA_ABRIR = "CAJA_ABRIR";
    public static final String CAJA_CERRAR = "CAJA_CERRAR";
    public static final String CAJA_HACER_RETIRO = "CAJA_HACER_RETIRO";
    public static final String CAJA_HACER_INGRESO = "CAJA_HACER_INGRESO";
    public static final String REPORTES_ACCEDER = "REPORTES_ACCEDER";
    public static final String REPORTES_EXPORTAR_PDF = "REPORTES_EXPORTAR_PDF";
    public static final String CLIENTES_ACCEDER = "CLIENTES_ACCEDER";
    public static final String CLIENTES_CREAR = "CLIENTES_CREAR";
    public static final String CLIENTES_EDITAR = "CLIENTES_EDITAR";
    public static final String CLIENTES_CREDITO = "CLIENTES_CREDITO";
    public static final String USUARIOS_ACCEDER = "USUARIOS_ACCEDER";
    public static final String USUARIOS_CREAR = "USUARIOS_CREAR";
    public static final String USUARIOS_EDITAR = "USUARIOS_EDITAR";
    public static final String USUARIOS_DESACTIVAR = "USUARIOS_DESACTIVAR";
    public static final String PERMISOS_GESTIONAR = "PERMISOS_GESTIONAR";
    public static final String CONFIGURACION_ACCEDER = "CONFIGURACION_ACCEDER";

    public enum Accion {
        CANCELAR_VENTA,
        PROCESAR_DEVOLUCION,
        AJUSTAR_INVENTARIO,
        VER_REPORTES,
        MODIFICAR_PRECIOS,
        GESTIONAR_EMPLEADOS,
        VER_CORTE_CAJA,
        CERRAR_CAJA,
        ACCEDER_VENTAS,
        ACCEDER_CLIENTES,
        ACCEDER_INVENTARIO,
        ACCEDER_CONFIGURACION,
        ACCEDER_AUDITORIA,
        QUITAR_PRODUCTO_CARRITO,
        CANCELAR_CARRITO
    }

    private static final Map<Accion, String> LEGACY_MAP = Map.ofEntries(
            Map.entry(Accion.CANCELAR_VENTA, VENTAS_CANCELAR),
            Map.entry(Accion.PROCESAR_DEVOLUCION, VENTAS_DEVOLVER),
            Map.entry(Accion.AJUSTAR_INVENTARIO, INVENTARIO_AJUSTAR),
            Map.entry(Accion.VER_REPORTES, REPORTES_ACCEDER),
            Map.entry(Accion.MODIFICAR_PRECIOS, PRODUCTOS_EDITAR),
            Map.entry(Accion.GESTIONAR_EMPLEADOS, USUARIOS_ACCEDER),
            Map.entry(Accion.VER_CORTE_CAJA, CAJA_ACCEDER),
            Map.entry(Accion.CERRAR_CAJA, CAJA_CERRAR),
            Map.entry(Accion.ACCEDER_VENTAS, VENTAS_ACCEDER),
            Map.entry(Accion.ACCEDER_CLIENTES, CLIENTES_ACCEDER),
            Map.entry(Accion.ACCEDER_INVENTARIO, PRODUCTOS_ACCEDER),
            Map.entry(Accion.ACCEDER_CONFIGURACION, CONFIGURACION_ACCEDER),
            Map.entry(Accion.ACCEDER_AUDITORIA, PERMISOS_GESTIONAR),
            Map.entry(Accion.QUITAR_PRODUCTO_CARRITO, VENTAS_ELIMINAR_PRODUCTO_CARRITO),
            Map.entry(Accion.CANCELAR_CARRITO, VENTAS_CANCELAR)
    );

    public static boolean puede(Accion accion) {
        return tienePermiso(LEGACY_MAP.getOrDefault(accion, ""));
    }

    public static boolean tienePermiso(String permiso) {
        return tienePermiso(SesionUsuario.getInstancia().getIdUsuario(), permiso);
    }

    public static boolean tienePermiso(int idUsuario, String permiso) {
        if (permiso == null || permiso.isBlank()) return false;
        String sql = """
                SELECT 1
                FROM usuarios u
                LEFT JOIN usuario_permisos up ON up.id_usuario = u.id_usuario
                LEFT JOIN permisos pu ON pu.id_permiso = up.id_permiso AND pu.activo = 1
                LEFT JOIN rol_permisos rp ON rp.id_rol = u.id_rol
                LEFT JOIN permisos pr ON pr.id_permiso = rp.id_permiso AND pr.activo = 1
                WHERE u.id_usuario = ?
                  AND u.activo = 1
                  AND (pu.codigo = ? OR pr.codigo = ? OR u.es_admin_principal = 1)
                LIMIT 1
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return fallbackPorRol(SesionUsuario.getInstancia().getRol(), permiso);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, permiso);
                ps.setString(3, permiso);
                ResultSet rs = ps.executeQuery();
                return rs.next();
            }
        } catch (Exception e) {
            return fallbackPorRol(SesionUsuario.getInstancia().getRol(), permiso);
        }
    }

    public static void verificar(Accion accion) throws SecurityException {
        validarPermiso(LEGACY_MAP.getOrDefault(accion, ""));
    }

    public static void validarPermiso(String permiso) throws SecurityException {
        if (!tienePermiso(permiso)) {
            throw new SecurityException("No tienes permiso para realizar esta accion: " + permiso);
        }
    }

    public static boolean requerirPermisoOAutorizacionAdmin(String permiso, String accion) {
        if (tienePermiso(permiso)) return true;
        AutorizacionAdminService.Autorizacion autorizacion = AutorizacionAdminService.solicitarAutorizacion(accion, permiso);
        if (!autorizacion.autorizado()) {
            mostrarDenegado();
            return false;
        }
        AuditoriaService.get().registrar(
                SesionUsuario.getInstancia().getIdUsuario(),
                "AUTORIZACION_ADMIN",
                "permisos",
                autorizacion.idAdmin(),
                "Accion autorizada: " + accion + " | permiso: " + permiso + " | admin: " + autorizacion.nombreAdmin()
        );
        AuditoriaService.get().registrarDetalle(
                SesionUsuario.getInstancia().getIdUsuario(),
                autorizacion.idAdmin(),
                "AUTORIZACION_ADMIN",
                "Permisos",
                "permisos",
                autorizacion.idAdmin(),
                "Accion autorizada: " + accion + " | permiso: " + permiso,
                null,
                "Admin: " + autorizacion.nombreAdmin()
        );
        return true;
    }

    public static List<PermisoInfo> listarPermisos() {
        List<PermisoInfo> permisos = new ArrayList<>();
        String sql = "SELECT id_permiso, codigo, nombre, modulo, descripcion FROM permisos WHERE activo = 1 ORDER BY modulo, codigo";
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return permisos;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(new PermisoInfo(
                            rs.getInt("id_permiso"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("modulo"),
                            rs.getString("descripcion")
                    ));
                }
            }
        } catch (Exception ignored) {
        }
        return permisos;
    }

    public static Map<String, Boolean> permisosUsuario(int idUsuario) {
        Map<String, Boolean> permisos = new LinkedHashMap<>();
        for (PermisoInfo permiso : listarPermisos()) {
            permisos.put(permiso.codigo(), tienePermiso(idUsuario, permiso.codigo()));
        }
        return permisos;
    }

    public static void guardarPermisosUsuario(int idUsuario, List<String> codigos) throws Exception {
        UsuarioSeguridadService seguridad = new UsuarioSeguridadService();
        for (String codigo : permisosUsuario(idUsuario).keySet()) {
            if (!codigos.contains(codigo) && !seguridad.puedeQuitarPermiso(idUsuario, codigo)) {
                throw new SecurityException("No se pueden quitar permisos criticos al administrador principal.");
            }
        }

        String delete = "DELETE FROM usuario_permisos WHERE id_usuario = ?";
        String insert = """
                INSERT IGNORE INTO usuario_permisos (id_usuario, id_permiso, otorgado_por)
                SELECT ?, id_permiso, ?
                FROM permisos
                WHERE codigo = ?
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) throw new IllegalStateException("Sin conexion a la base de datos.");
            con.setAutoCommit(false);
            try (PreparedStatement psDelete = con.prepareStatement(delete);
                 PreparedStatement psInsert = con.prepareStatement(insert)) {
                psDelete.setInt(1, idUsuario);
                psDelete.executeUpdate();
                for (String codigo : codigos) {
                    psInsert.setInt(1, idUsuario);
                    psInsert.setInt(2, SesionUsuario.getInstancia().getIdUsuario());
                    psInsert.setString(3, codigo);
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public static boolean esCajeroActual() {
        return "cajero".equalsIgnoreCase(SesionUsuario.getInstancia().getRol());
    }

    private static boolean fallbackPorRol(String rol, String permiso) {
        if ("admin".equalsIgnoreCase(rol)) return true;
        if ("supervisor".equalsIgnoreCase(rol)) {
            return !List.of(PERMISOS_GESTIONAR, USUARIOS_DESACTIVAR, USUARIOS_CREAR).contains(permiso);
        }
        return List.of(VENTAS_ACCEDER, VENTAS_COBRAR, CLIENTES_ACCEDER).contains(permiso);
    }

    private static void mostrarDenegado() {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Acceso denegado");
        alerta.setHeaderText(null);
        alerta.setContentText("No se autorizo la accion.");
        alerta.showAndWait();
    }

    public record PermisoInfo(int idPermiso, String codigo, String nombre, String modulo, String descripcion) {}
}
