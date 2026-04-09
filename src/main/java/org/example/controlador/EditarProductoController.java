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

    @FXML private TextField           txtNombre;
    @FXML private TextField           txtPrecio;
    @FXML private TextField           txtCosto;
    @FXML private TextField           txtStock;
    @FXML private TextField           txtStockMinimo;
    @FXML private ComboBox<Categoria> cbCategoria;

    private Producto producto;
    private final ProductoDAO dao = new ProductoDAO();

    public void setProducto(Producto p) {
        this.producto = p;

        List<Categoria> categorias = new CategoriaDAO().obtenerCategorias();
        cbCategoria.setItems(FXCollections.observableArrayList(categorias));

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

        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtCosto.setText(String.valueOf(p.getCosto()));
        txtStock.setText(String.valueOf(p.getStock()));
        txtStockMinimo.setText(String.valueOf(p.getStockMinimo()));

        categorias.stream()
                .filter(c -> c.getIdCategoria() == p.getIdCategoria())
                .findFirst()
                .ifPresent(cbCategoria::setValue);
    }

    @FXML
    private void guardarCambios() {
        if (txtNombre.getText().isBlank()) {
            mostrarAlerta("El nombre no puede estar vacío."); return;
        }

        double precio, costo;
        int stock, stockMinimo;

        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
            costo  = Double.parseDouble(txtCosto.getText().trim());
            stock  = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("Precio, costo y stock deben ser números válidos."); return;
        }

        String minTxt = txtStockMinimo.getText().trim();
        try { stockMinimo = minTxt.isEmpty() ? 5 : Integer.parseInt(minTxt); }
        catch (NumberFormatException e) { mostrarAlerta("El stock mínimo no es válido."); return; }

        if (cbCategoria.getValue() == null) {
            mostrarAlerta("Selecciona una categoría."); return;
        }

        producto.setNombre(txtNombre.getText().trim());
        producto.setPrecio(precio);
        producto.setCosto(costo);
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        producto.setIdCategoria(cbCategoria.getValue().getIdCategoria());

        dao.actualizarProducto(producto);
        cerrarVentana();
    }

    @FXML
    private void cancelar() { cerrarVentana(); }

    private void cerrarVentana() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validación"); alert.setHeaderText(null); alert.setContentText(mensaje);
        alert.showAndWait();
    }
}