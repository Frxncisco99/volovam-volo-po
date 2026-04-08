package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.CategoriaDAO;
import org.example.dao.ProductoDAO;
import org.example.modelo.Categoria;
import org.example.modelo.Producto;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class AgregarProductoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCosto;
    @FXML private TextField txtStock;

    private ProductoDAO productoDAO = new ProductoDAO();

    @FXML
    private void guardarProducto() {
        System.out.println("CLICK GUARDAR");

        try {

            String nombre = txtNombre.getText();

            if (nombre.isEmpty()) {
                mostrarAlerta("El nombre es obligatorio");
                return;
            }

            double precio;
            double costo;
            int stock;

            try {
                precio = Double.parseDouble(txtPrecio.getText());
                costo = Double.parseDouble(txtCosto.getText());
                stock = Integer.parseInt(txtStock.getText());
            } catch (Exception e) {
                mostrarAlerta("Precio, costo o stock inválido");
                return;
            }

            Categoria cat = cbCategoria.getValue();

            if (cat == null) {
                mostrarAlerta("Selecciona una categoría");
                return;
            }

            Producto p = new Producto();
            p.setNombre(nombre);
            p.setPrecio(precio);
            p.setCosto(costo);
            p.setStock(stock);
            p.setIdCategoria(cat.getIdCategoria());

            System.out.println("Insertando: " + nombre);

            // 🔥 AQUÍ FALTABA TODO
            productoDAO.insertarProducto(p);

            mostrarAlerta("Producto guardado correctamente ✅");

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
    @FXML
    private void cancelar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void nuevaCategoria() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Categoría");
        dialog.setHeaderText(null);
        dialog.setContentText("Nombre de la categoría:");

        dialog.showAndWait().ifPresent(nombre -> {

            if (!nombre.isEmpty()) {

                CategoriaDAO dao = new CategoriaDAO();
                dao.insertarCategoria(nombre);

                // 🔄 Recargar ComboBox
                cbCategoria.getItems().clear();
                cbCategoria.getItems().addAll(dao.obtenerCategorias());
            }
        });
    }

    @FXML private ComboBox<Categoria> cbCategoria;
    private CategoriaDAO categoriaDAO = new CategoriaDAO();

    @FXML
    public void initialize() {
        cbCategoria.getItems().addAll(categoriaDAO.obtenerCategorias());
    }
}