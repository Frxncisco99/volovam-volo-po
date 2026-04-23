package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;

public class AjustarStockController {

    @FXML private Label     lblNombreProducto;
    @FXML private Label     lblStockActual;
    @FXML private Label     lblPreview;
    @FXML private TextField txtCantidad;

    private Producto    producto;
    private final ProductoDAO dao = new ProductoDAO();

    public void setProducto(Producto p) {
        this.producto = p;
        lblNombreProducto.setText(p.getNombre());
        lblStockActual.setText(String.valueOf(p.getStock()));
        actualizarPreview();

        // Actualizar preview
        txtCantidad.textProperty().addListener((o, a, b) -> actualizarPreview());
    }

    @FXML
    private void sumar() {
        cambiarCantidad(1);
    }

    @FXML
    private void restar() {
        cambiarCantidad(-1);
    }

    private void cambiarCantidad(int delta) {
        try {
            int actual = Integer.parseInt(txtCantidad.getText().trim());
            int nuevo  = Math.max(1, actual + delta);
            txtCantidad.setText(String.valueOf(nuevo));
        } catch (NumberFormatException e) {
            txtCantidad.setText("1");
        }
        actualizarPreview();
    }

    private void actualizarPreview() {
        try {
            int cantidad  = Integer.parseInt(txtCantidad.getText().trim());
            int stockNuevo = producto.getStock() + cantidad;
            lblPreview.setText("Al añadir → Stock resultante: " + stockNuevo);
            lblPreview.setStyle("-fx-font-size: 12px; -fx-text-fill: #2E7D50; " +
                    "-fx-font-weight: bold; -fx-alignment: center;");
        } catch (NumberFormatException e) {
            lblPreview.setText("Ingresa una cantidad válida");
            lblPreview.setStyle("-fx-font-size: 12px; -fx-text-fill: #A32D2D; " +
                    "-fx-font-weight: bold; -fx-alignment: center;");
        }
    }

    @FXML
    private void confirmarAnadir() {
        ejecutarAjuste(true);
    }

    @FXML
    private void confirmarRestar() {
        ejecutarAjuste(false);
    }

    private void ejecutarAjuste(boolean esAdicion) {
        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Alert err = new Alert(Alert.AlertType.WARNING);
            err.setTitle("Cantidad inválida");
            err.setHeaderText(null);
            err.setContentText("Ingresa un número entero mayor a 0.");
            err.showAndWait();
            return;
        }

        int ajuste    = esAdicion ? cantidad : -cantidad;
        int resultado = producto.getStock() + ajuste;

        if (resultado < 0) {
            Alert err = new Alert(Alert.AlertType.WARNING);
            err.setTitle("Stock insuficiente");
            err.setHeaderText(null);
            err.setContentText("No puedes restar más de lo que hay en stock.\n" +
                    "Stock actual: " + producto.getStock());
            err.showAndWait();
            return;
        }

        dao.ajustarStock(producto.getIdProducto(), ajuste);
        cerrar();
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) lblStockActual.getScene().getWindow()).close();
    }
}