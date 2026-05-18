package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.servicio.AppExitService;
import org.example.servicio.SessionTimeoutService;

public class Main extends Application {

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/vista/Login.fxml")
        );

        Scene scene = new Scene(root);

        // ── Ícono en barra de tareas / alt-tab ───────────────────────────────
        javafx.scene.image.Image icono = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/org/example/Imagenes/logo_volovan.png")
        );
        primaryStage.getIcons().add(icono);

        primaryStage.setTitle("Volovan Volo — Sistema de Gestion");
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        primaryStage.setScene(scene);
        SessionTimeoutService.instalar(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            if (AppExitService.confirmarSalida(scene.getRoot())) {
                AppExitService.registrarSalida();
                org.example.dao.ConexionDB.cerrarPool();
                javafx.application.Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.show();
    }
}
