package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.example.dao.CategoriaDAO;
import org.example.dao.ProductoDAO;
import org.example.modelo.Categoria;
import org.example.modelo.Producto;

public class AgregarProductoController {

    @FXML private TextField          txtNombre;
    @FXML private TextField          txtPrecio;
    @FXML private TextField          txtCosto;
    @FXML private TextField          txtStock;
    @FXML private ComboBox<Categoria> cbCategoria;

    private final ProductoDAO  productoDAO  = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @FXML
    public void initialize() {
        cbCategoria.getItems().addAll(categoriaDAO.obtenerCategorias());

        // Mostrar nombre en el ComboBox
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

        // Solo números en precio, costo y stock
        permitirSoloNumeros(txtPrecio, true);
        permitirSoloNumeros(txtCosto,  true);
        permitirSoloNumeros(txtStock,  false);
    }

    // ─── GUARDAR ─────────────────────────────────────────────────────────────

    @FXML
    private void guardarProducto() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            error("El nombre del producto es obligatorio."); return;
        }
        if (nombre.length() < 2) {
            error("El nombre debe tener al menos 2 caracteres."); return;
        }

        double precio, costo;
        int stock;

        try { precio = Double.parseDouble(txtPrecio.getText().trim()); }
        catch (NumberFormatException e) { error("El precio no es válido."); return; }

        try { costo = Double.parseDouble(txtCosto.getText().trim()); }
        catch (NumberFormatException e) { error("El costo no es válido."); return; }

        try { stock = Integer.parseInt(txtStock.getText().trim()); }
        catch (NumberFormatException e) { error("El stock no es válido."); return; }

        if (precio <= 0) { error("El precio debe ser mayor a 0."); return; }
        if (costo  <= 0) { error("El costo debe ser mayor a 0.");  return; }
        if (precio < costo) {
            error("El precio de venta no puede ser menor al costo."); return;
        }
        if (stock < 0)  { error("El stock no puede ser negativo."); return; }

        Categoria cat = cbCategoria.getValue();
        if (cat == null) { error("Selecciona una categoría."); return; }

        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setCosto(costo);
        p.setStock(stock);
        p.setIdCategoria(cat.getIdCategoria());

        productoDAO.insertarProducto(p);

        info("Producto guardado correctamente.");
        cerrar();
    }

    // ─── NUEVA CATEGORÍA ─────────────────────────────────────────────────────

    @FXML
    private void nuevaCategoria() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Categoría");
        dialog.setHeaderText(null);
        dialog.setContentText("Nombre de la categoría:");

        dialog.showAndWait().ifPresent(nombre -> {
            nombre = nombre.trim();
            if (!nombre.isEmpty()) {
                categoriaDAO.insertarCategoria(nombre);
                cbCategoria.getItems().clear();
                cbCategoria.getItems().addAll(categoriaDAO.obtenerCategorias());
            }
        });
    }

    // ─── CANCELAR ────────────────────────────────────────────────────────────

    @FXML
    private void cancelar(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    // ─── UTILIDADES ──────────────────────────────────────────────────────────

    /** Restringe el TextField a solo dígitos (y punto decimal si allowDecimal=true) */
    private void permitirSoloNumeros(TextField tf, boolean allowDecimal) {
        tf.textProperty().addListener((obs, oldVal, newVal) -> {
            String regex = allowDecimal ? "[^\\d.]" : "[^\\d]";
            String limpio = newVal.replaceAll(regex, "");
            // evitar más de un punto
            if (allowDecimal) {
                int puntos = limpio.length() - limpio.replace(".", "").length();
                if (puntos > 1) limpio = oldVal;
            }
            if (!limpio.equals(newVal)) tf.setText(limpio);
        });
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Validación");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Éxito");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
}