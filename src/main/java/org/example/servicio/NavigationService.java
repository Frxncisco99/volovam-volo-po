package org.example.servicio;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.modelo.SesionUsuario;

public final class NavigationService {

    public static final String LOGIN = "/org/example/vista/Login.fxml";
    public static final String APERTURA_CAJA = "/org/example/vista/AperturaCaja.fxml";
    public static final String MENU_PRINCIPAL = "/org/example/vista/MenuPrincipal.fxml";

    private static final double LOGIN_WIDTH = 479;
    private static final double LOGIN_HEIGHT = 527;
    private static final double APERTURA_WIDTH = 500;
    private static final double APERTURA_HEIGHT = 430;

    private NavigationService() {
    }

    public static void cambiarEscena(Node owner, String ruta) {
        try {
            Parent root = cargar(ruta);
            Stage stage = obtenerStage(owner);
            if (stage == null) {
                return;
            }
            aplicarRoot(stage, root);
            ajustarVentana(stage, ruta);
        } catch (Exception e) {
            LogService.error("No se pudo abrir la pantalla " + ruta, e);
            DialogService.error(owner, "Navegacion", "No se pudo abrir la pantalla solicitada.");
        }
    }

    public static void abrirMenuPrincipal(Node owner) {
        cambiarEscena(owner, MENU_PRINCIPAL);
    }

    public static void abrirAperturaCaja(Node owner) {
        cambiarEscena(owner, APERTURA_CAJA);
    }

    public static void mostrarLogin(Scene scene) {
        try {
            Parent root = cargar(LOGIN);
            scene.setRoot(root);
            scene.setFill(Color.TRANSPARENT);
            if (scene.getWindow() instanceof Stage stage) {
                ajustarVentana(stage, LOGIN);
            }
        } catch (Exception e) {
            LogService.error("No se pudo mostrar el login", e);
        }
    }

    public static void cambiarSesion(Node owner) {
        boolean confirmado = DialogService.confirmar(
                owner,
                "Cambiar sesión",
                "¿Seguro que deseas cambiar de sesión?"
        ).filter(ButtonType.OK::equals).isPresent();

        if (!confirmado) {
            return;
        }

        registrarLogout();
        SesionUsuario.cerrarSesion();
        cambiarEscena(owner, LOGIN);
    }

    public static void prepararLogin(Stage stage) {
        ajustarVentana(stage, LOGIN);
    }

    public static void registrarLogout() {
        try {
            SesionUsuario sesion = SesionUsuario.getInstancia();
            int idUsuario = sesion.getIdUsuario();
            if (idUsuario <= 0) {
                return;
            }
            AuditoriaService.get().registrar(
                    idUsuario,
                    "LOGOUT",
                    "usuarios",
                    idUsuario,
                    "Cambio de sesión: " + sesion.getNombre()
            );
        } catch (Exception e) {
            LogService.warn("No se pudo registrar el cierre de sesión", e);
        }
    }

    private static Parent cargar(String ruta) throws Exception {
        FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource(ruta));
        Parent root = loader.load();
        if (!LOGIN.equals(ruta)) {
            MarcaService.aplicar(root);
        }
        return root;
    }

    private static Stage obtenerStage(Node owner) {
        if (owner == null || owner.getScene() == null || !(owner.getScene().getWindow() instanceof Stage stage)) {
            return null;
        }
        return stage;
    }

    private static void aplicarRoot(Stage stage, Parent root) {
        if (stage.getScene() == null) {
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
            stage.getScene().setFill(Color.TRANSPARENT);
        }
    }

    private static void ajustarVentana(Stage stage, String ruta) {
        if (LOGIN.equals(ruta)) {
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setWidth(LOGIN_WIDTH);
            stage.setHeight(LOGIN_HEIGHT);
            stage.centerOnScreen();
            return;
        }

        if (APERTURA_CAJA.equals(ruta)) {
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setWidth(APERTURA_WIDTH);
            stage.setHeight(APERTURA_HEIGHT);
            stage.centerOnScreen();
            return;
        }

        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }
}
