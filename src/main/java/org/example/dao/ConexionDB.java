package org.example.dao;

import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL = "jdbc:mysql://localhost:3306/pospanaderia";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    public static Connection getConexion() {
        try {
            Connection con = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("Conexión exitosa ✅");
            return con;
        } catch (SQLException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de conexión");
            alerta.setContentText("No se pudo conectar a la BD: " + e.getMessage());
            alerta.showAndWait();
            return null;
        }
    }
}