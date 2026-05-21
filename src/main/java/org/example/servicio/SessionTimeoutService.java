package org.example.servicio;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.example.controlador.ConfiguracionController;
import org.example.modelo.SesionUsuario;

import java.util.prefs.Preferences;

public final class SessionTimeoutService {

    private static final String KEY_ACTIVO = "seguridad_bloqueo_inactividad_activo";
    private static final String KEY_MINUTOS = "seguridad_bloqueo_minutos";

    private SessionTimeoutService() {
    }

    public static void instalar(Scene scene) {
        PauseTransition timer = new PauseTransition(Duration.minutes(obtenerMinutos()));
        timer.setOnFinished(event -> bloquearPorInactividad(scene));

        Runnable reiniciar = () -> {
            if (haySesionActiva() && estaActivo()) {
                timer.setDuration(Duration.minutes(obtenerMinutos()));
                timer.playFromStart();
            } else {
                timer.stop();
            }
        };

        scene.addEventFilter(MouseEvent.ANY, event -> reiniciar.run());
        scene.addEventFilter(KeyEvent.ANY, event -> reiniciar.run());
        reiniciar.run();
    }

    private static void bloquearPorInactividad(Scene scene) {
        if (!haySesionActiva() || !estaActivo()) {
            return;
        }
        Platform.runLater(() -> {
            try {
                AuditoriaService.get().registrar("BLOQUEO_INACTIVIDAD", "sesion", 0,
                "Sesión cerrada automáticamente por inactividad.");
                SesionUsuario.cerrarSesion();
                NavigationService.mostrarLogin(scene);
            } catch (Exception e) {
                org.example.servicio.LogService.error("Error no controlado", e);
            }
        });
    }

    private static boolean haySesionActiva() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        return sesion.getIdUsuario() > 0;
    }

    private static boolean estaActivo() {
        return prefs().getBoolean(KEY_ACTIVO, true);
    }

    private static int obtenerMinutos() {
        int minutos = prefs().getInt(KEY_MINUTOS, 30);
        return Math.max(1, minutos);
    }

    private static Preferences prefs() {
        return Preferences.userNodeForPackage(ConfiguracionController.class);
    }
}
