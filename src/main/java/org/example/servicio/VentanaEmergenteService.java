package org.example.servicio;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class VentanaEmergenteService {

    private VentanaEmergenteService() {
    }

    public static void preparar(Stage stage) {
        if (stage == null) return;
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
    }
}
