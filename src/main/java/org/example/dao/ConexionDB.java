package org.example.dao;

import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL = "jdbc:mysql://localhost:3306/pospanaderia?useSSL=false&serverTimezone=America/Monterrey&useLegacyDatetimeCode=false&useJDBCCompliantTimezoneShift=true";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    public static Connection getConexion() {
        try {
            return DriverManager.getConnection(URL, USUARIO, CLAVE);
        } catch (SQLException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de conexión");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pudo conectar: " + e.getMessage());
            alerta.showAndWait();
            return null;
        }
    }
}