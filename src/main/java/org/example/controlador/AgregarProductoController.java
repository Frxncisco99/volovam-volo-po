package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;

public class AgregarProductoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCosto;
    @FXML private TextField txtStock;

    private ProductoDAO dao = new ProductoDAO();

    @FXML
    private void guardarProducto() {
        System.out.println("CLICK GUARDAR"); // 👈 PRUEBA

        try {
            String nombre = txtNombre.getText();

            if (nombre.isEmpty()) {
                mostrarAlerta("El nombre es obligatorio");
                return;
            }

            double precio;
            int stock;

            try {
                precio = Double.parseDouble(txtPrecio.getText());
                stock = Integer.parseInt(txtStock.getText());
            } catch (Exception e) {
                mostrarAlerta("Precio o stock inválido");
                return;
            }
            double costo;

            try {
                costo = Double.parseDouble(txtCosto.getText());
            } catch (Exception e) {
                mostrarAlerta("Costo inválido");
                return;
            }

            Producto p = new Producto();
            p.setNombre(nombre);
            p.setPrecio(precio);
            p.setCosto(costo);
            p.setStock(stock);

            // 🔥 DEBUG
            System.out.println("Insertando: " + nombre);

            dao.insertarProducto(p);

            mostrarAlerta("Producto guardado correctamente ✅");

            // cerrar
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al guardar");
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}