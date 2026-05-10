package org.example.servicio;

import org.example.modelo.SesionUsuario;

public class PermisoService {

    public enum Accion {
        CANCELAR_VENTA,
        PROCESAR_DEVOLUCION,
        AJUSTAR_INVENTARIO,
        VER_REPORTES,
        MODIFICAR_PRECIOS,
        GESTIONAR_EMPLEADOS,
        VER_CORTE_CAJA,
        CERRAR_CAJA
    }

    public static boolean puede(Accion accion) {
        String rol = SesionUsuario.getInstancia().getRol();
        if (rol == null) return false;

        return switch (accion) {
            case CANCELAR_VENTA       -> esAdmin(rol) || esSupervisor(rol);
            case PROCESAR_DEVOLUCION  -> esAdmin(rol) || esSupervisor(rol) || esCajero(rol);
            case AJUSTAR_INVENTARIO   -> esAdmin(rol) || esSupervisor(rol);
            case VER_REPORTES         -> esAdmin(rol) || esSupervisor(rol);
            case MODIFICAR_PRECIOS    -> esAdmin(rol);
            case GESTIONAR_EMPLEADOS  -> esAdmin(rol);
            case VER_CORTE_CAJA       -> esAdmin(rol) || esSupervisor(rol) || esCajero(rol);
            case CERRAR_CAJA          -> esAdmin(rol) || esSupervisor(rol) || esCajero(rol);
        };
    }

    public static void verificar(Accion accion) throws SecurityException {
        if (!puede(accion)) {
            throw new SecurityException(
                    "No tienes permiso para realizar esta acción: " + accion.name()
            );
        }
    }

    private static boolean esAdmin(String rol)      { return "admin".equalsIgnoreCase(rol); }
    private static boolean esSupervisor(String rol) { return "supervisor".equalsIgnoreCase(rol); }
    private static boolean esCajero(String rol)     { return "cajero".equalsIgnoreCase(rol); }
}