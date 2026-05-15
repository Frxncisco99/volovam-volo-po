package org.example.servicio;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import java.util.prefs.Preferences;

public final class MarcaService {

    private static final String MARCA_DEFAULT = "Volovan Volo";

    private MarcaService() {}

    public static String nombreNegocio() {
        Preferences prefs = Preferences.userNodeForPackage(
                org.example.controlador.ConfiguracionController.class);
        String nombre = prefs.get("negocio_nombre", MARCA_DEFAULT);
        return nombre == null || nombre.isBlank() ? MARCA_DEFAULT : nombre.trim();
    }

    public static void aplicar(Parent root) {
        if (root == null) return;
        aplicarNodo(root, nombreNegocio());
    }

    private static void aplicarNodo(Node node, String nombre) {
        if (node instanceof Label label) {
            String texto = label.getText();
            if (texto != null && texto.contains(MARCA_DEFAULT)) {
                label.setText(texto.replace(MARCA_DEFAULT, nombre));
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                aplicarNodo(child, nombre);
            }
        }
    }
}
