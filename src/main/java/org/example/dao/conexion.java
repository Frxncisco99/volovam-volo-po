package org.example.dao;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/pos_panaderia";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";  // Tu contraseña de MySQL

    public static Connection getConexion() {
        try {
            Connection con = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("Conexión exitosa a la base de datos");
            return con;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Error de conexión :(((: " + e.getMessage());

            return null;
        }
    }
}