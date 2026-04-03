    package org.example.controlador;

    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Node;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.Button;

    import javafx.stage.Stage;

    import java.io.IOException;


    public class LoginController {

        @FXML private Button btnLogin;





        @FXML
        private void handleIniciarSesion(ActionEvent event) throws IOException {
            // (Opcional) Validar usuario y contraseña aquí

            // Cargar el Dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/MenuPrincipal.fxml"));
            Parent root = loader.load();

            // Obtener el Stage actual desde cualquier nodo de la vista
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Crear y mostrar la nueva escena
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();
        }
    }