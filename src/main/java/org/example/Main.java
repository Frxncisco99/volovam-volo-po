package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}