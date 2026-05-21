package org.example.servicio;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

import java.util.Optional;

public final class DialogService {

    private DialogService() {
    }

    public static Optional<ButtonType> confirmar(Node owner, String titulo, String mensaje) {
        return mostrar(new Alert(Alert.AlertType.CONFIRMATION), owner, titulo, mensaje);
    }

    public static Optional<ButtonType> confirmar(Node owner, String titulo, String mensaje, ButtonType... botones) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        if (botones != null && botones.length > 0) {
            alerta.getButtonTypes().setAll(botones);
        }
        return mostrar(alerta, owner, titulo, mensaje);
    }

    public static Optional<ButtonType> info(Node owner, String titulo, String mensaje) {
        return mostrar(new Alert(Alert.AlertType.INFORMATION), owner, titulo, mensaje);
    }

    public static Optional<ButtonType> advertencia(Node owner, String titulo, String mensaje) {
        return mostrar(new Alert(Alert.AlertType.WARNING), owner, titulo, mensaje);
    }

    public static Optional<ButtonType> error(Node owner, String titulo, String mensaje) {
        return mostrar(new Alert(Alert.AlertType.ERROR), owner, titulo, mensaje);
    }

    private static Optional<ButtonType> mostrar(Alert alerta, Node owner, String titulo, String mensaje) {
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        preparar(alerta, owner);
        return alerta.showAndWait();
    }

    public static void preparar(Dialog<?> alerta, Node owner) {
        Window ownerWindow = obtenerVentana(owner);
        if (ownerWindow != null) {
            alerta.initOwner(ownerWindow);
        }

        DialogPane pane = alerta.getDialogPane();
        pane.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #b8d4ea;
                -fx-border-width: 1;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                """);
        alerta.setOnShown(event -> centrar(alerta.getDialogPane().getScene().getWindow(), ownerWindow));
    }

    private static Window obtenerVentana(Node owner) {
        return owner != null && owner.getScene() != null ? owner.getScene().getWindow() : null;
    }

    private static void centrar(Window dialog, Window owner) {
        if (dialog == null) {
            return;
        }
        if (owner != null && owner.isShowing()) {
            dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
            dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
        } else {
            dialog.centerOnScreen();
        }
    }
}
