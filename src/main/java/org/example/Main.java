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

        primaryStage.setTitle("Volovan Volo — Sistema de Gestion");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();



    }

    public static void main(String[] args) {
        launch(args);
    }
}