package org.example.servicio;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

public class AppExitService {

    public static boolean confirmarSalida(Node owner) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Salir del sistema");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("Seguro que deseas salir del sistema?");
        if (owner != null && owner.getScene() != null) {
            confirmacion.initOwner(owner.getScene().getWindow());
        }
        return confirmacion.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    public static void salir(Node owner) {
        if (!confirmarSalida(owner)) return;
        registrarSalida();
        ConexionDB.cerrarPool();
        Platform.exit();
        System.exit(0);
    }

    public static void registrarSalida() {
        try {
            SesionUsuario sesion = SesionUsuario.getInstancia();
            if (sesion.getIdUsuario() > 0) {
                AuditoriaService.get().registrar(
                        sesion.getIdUsuario(),
                        "SALIDA_SISTEMA",
                        "sistema",
                        sesion.getIdUsuario(),
                        "Salida del sistema: " + sesion.getNombre()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
