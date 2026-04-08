package org.example.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.CategoriaDAO;
import org.example.dao.ProductoDAO;
import org.example.modelo.Categoria;
import org.example.modelo.Producto;

import java.util.List;

public class EditarProductoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCosto;
    @FXML private TextField txtStock;
    @FXML private ComboBox<Categoria> cbCategoria;

    private Producto producto;
    private final ProductoDAO dao = new ProductoDAO();

    // Llamado desde InventarioController antes de mostrar el stage
    public void setProducto(Producto p) {
        this.producto = p;

        // Cargar categorías en el ComboBox
        List<Categoria> categorias = new CategoriaDAO().obtenerCategorias();
        cbCategoria.setItems(FXCollections.observableArrayList(categorias));

        // Mostrar el nombre de la categoría en el ComboBox
        cbCategoria.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Categoria c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNombre());
            }
        });
        cbCategoria.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Categoria c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNombre());
            }
        });

        // Pre-llenar los campos con los datos actuales
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtCosto.setText(String.valueOf(p.getCosto()));
        txtStock.setText(String.valueOf(p.getStock()));

        // Seleccionar la categoría actual
        categorias.stream()
                .filter(c -> c.getIdCategoria() == p.getIdCategoria())
                .findFirst()
                .ifPresent(cbCategoria::setValue);
    }

    @FXML
    private void guardarCambios() {
        // Validación básica
        if (txtNombre.getText().isBlank()) {
            mostrarAlerta("El nombre no puede estar vacío.");
            return;
        }

        double precio, costo;
        int stock;

        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
            costo  = Double.parseDouble(txtCosto.getText().trim());
            stock  = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("Precio, costo y stock deben ser números válidos.");
            return;
        }

        if (cbCategoria.getValue() == null) {
            mostrarAlerta("Selecciona una categoría.");
            return;
        }

        // Actualizar el objeto
        producto.setNombre(txtNombre.getText().trim());
        producto.setPrecio(precio);
        producto.setCosto(costo);
        producto.setStock(stock);
        producto.setIdCategoria(cbCategoria.getValue().getIdCategoria());

        // Guardar en BD (actualiza nombre, precio, costo, stock, id_categoria)
        dao.actualizarProducto(producto);

        cerrarVentana();
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}