package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;

public class AjustarStockController {

    @FXML private Label        lblNombreProducto;
    @FXML private Label        lblStockActual;
    @FXML private Label        lblPreview;
    @FXML private Label        lblModo;
    @FXML private TextField    txtCantidad;
    @FXML private ToggleButton btnModoAnadir;
    @FXML private ToggleButton btnModoRestar;
    @FXML private Button       btnConfirmar;
    @FXML private Rectangle    barraIndicadora;

    private Producto       producto;
    private boolean        modoAnadir = true;   // estado del selector
    private final ProductoDAO dao = new ProductoDAO();

    // ── Estilos ──────────────────────────────────────────────────────────────

    private static final String BASE_PILL =
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 5 16;" +
                    "-fx-border-radius: 20; -fx-border-width: 1;";

    private static final String PILL_ADD =
            BASE_PILL + "-fx-text-fill: #1A5C36;" +
                    "-fx-background-color: #C8EDD8; -fx-border-color: #6DBF96;";

    private static final String PILL_SUB =
            BASE_PILL + "-fx-text-fill: #6B1228;" +
                    "-fx-background-color: #F2C4CE; -fx-border-color: #D4708A;";

    private static final String PILL_ERROR =
            BASE_PILL + "-fx-text-fill: #A32D2D;" +
                    "-fx-background-color: #FAE8EC; -fx-border-color: #D4708A;";

    private static final String BASE_MODO =
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 3 12;" +
                    "-fx-border-radius: 20; -fx-border-width: 1;";

    private static final String MODO_ADD =
            BASE_MODO + "-fx-text-fill: #1A5C36;" +
                    "-fx-background-color: #C8EDD8; -fx-border-color: #6DBF96;";

    private static final String MODO_SUB =
            BASE_MODO + "-fx-text-fill: #6B1228;" +
                    "-fx-background-color: #F2C4CE; -fx-border-color: #D4708A;";

    // Botón Añadir activo / inactivo
    private static final String BTN_ANADIR_ON =
            "-fx-background-color: #2E7D50; -fx-text-fill: white;" +
                    "-fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 8 0 0 8; -fx-cursor: hand;";

    private static final String BTN_ANADIR_OFF =
            "-fx-background-color: #E8F5EE; -fx-text-fill: #5A9A78;" +
                    "-fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 8 0 0 8;" +
                    "-fx-border-color: #6DBF96; -fx-border-width: 1.5; -fx-cursor: hand;";

    // Botón Restar activo / inactivo
    private static final String BTN_RESTAR_ON =
            "-fx-background-color: #6B1228; -fx-text-fill: white;" +
                    "-fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 0 8 8 0; -fx-cursor: hand;";

    private static final String BTN_RESTAR_OFF =
            "-fx-background-color: #F2C4CE; -fx-text-fill: #9A2A3A;" +
                    "-fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 0 8 8 0;" +
                    "-fx-border-color: #D4708A; -fx-border-width: 1.5; -fx-cursor: hand;";

    // ── Inicialización ───────────────────────────────────────────────────────

    public void setProducto(Producto p) {
        this.producto = p;
        lblNombreProducto.setText(p.getNombre());
        lblStockActual.setText(String.valueOf(p.getStock()));

        aplicarModo(true);   // arranca en modo añadir

        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtCantidad.setText(newVal.replaceAll("[^\\d]", ""));
                return;
            }
            actualizarPreview();
        });
    }

    // ── Selector de modo ─────────────────────────────────────────────────────

    @FXML
    private void seleccionarModoAnadir() {
        aplicarModo(true);
    }

    @FXML
    private void seleccionarModoRestar() {
        aplicarModo(false);
    }

    /**
     * Cambia el modo activo y actualiza todos los elementos visuales de una vez:
     * selector, barra lateral, píldora lblModo, preview y botón confirmar.
     */
    private void aplicarModo(boolean anadir) {
        modoAnadir = anadir;

        // Selector — resaltar el activo
        btnModoAnadir.setSelected(anadir);
        btnModoRestar.setSelected(!anadir);
        btnModoAnadir.setStyle(anadir ? BTN_ANADIR_ON : BTN_ANADIR_OFF);
        btnModoRestar.setStyle(anadir ? BTN_RESTAR_OFF : BTN_RESTAR_ON);

        // Barra lateral de la sección cantidad
        barraIndicadora.setStyle(anadir
                ? "-fx-fill: #2E7D50;"
                : "-fx-fill: #6B1228;");

        // Píldora de modo
        lblModo.setText(anadir ? "▲  AÑADIENDO" : "▼  RESTANDO");
        lblModo.setStyle(anadir ? MODO_ADD : MODO_SUB);

        // Color del botón confirmar
        btnConfirmar.setStyle(
                (anadir
                        ? "-fx-background-color: #2E7D50;"
                        : "-fx-background-color: #6B1228;") +
                        "-fx-text-fill: white; -fx-background-radius: 8;" +
                        "-fx-border-radius: 8; -fx-font-size: 12px;" +
                        "-fx-font-weight: bold; -fx-padding: 0 28; -fx-cursor: hand;");

        actualizarPreview();
    }

    // ── Botones +/- ──────────────────────────────────────────────────────────

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
            txtCantidad.setText(String.valueOf(Math.max(1, actual + delta)));
        } catch (NumberFormatException e) {
            txtCantidad.setText("1");
        }
        actualizarPreview();
    }

    // ── Preview ──────────────────────────────────────────────────────────────

    private void actualizarPreview() {
        String raw = txtCantidad.getText().trim();

        if (raw.isEmpty()) {
            lblPreview.setText("Ingresa una cantidad");
            lblPreview.setStyle(PILL_ERROR);
            return;
        }

        try {
            int cantidad = Integer.parseInt(raw);
            if (cantidad <= 0) throw new NumberFormatException();

            int stock = producto.getStock();

            if (modoAnadir) {
                lblPreview.setText("▲  Stock resultante: " + (stock + cantidad));
                lblPreview.setStyle(PILL_ADD);
            } else {
                int resultado = stock - cantidad;
                if (resultado < 0) {
                    lblPreview.setText("⚠  Sin stock suficiente (" + stock + " disponibles)");
                    lblPreview.setStyle(PILL_ERROR);
                } else {
                    lblPreview.setText("▼  Stock resultante: " + resultado);
                    lblPreview.setStyle(PILL_SUB);
                }
            }

        } catch (NumberFormatException e) {
            lblPreview.setText("Cantidad inválida");
            lblPreview.setStyle(PILL_ERROR);
        }
    }

    // ── Confirmación única ───────────────────────────────────────────────────

    @FXML
    private void confirmar() {
        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Cantidad inválida",
                    "Ingresa un número entero mayor a 0.");
            return;
        }

        int ajuste    = modoAnadir ? cantidad : -cantidad;
        int resultado = producto.getStock() + ajuste;

        if (resultado < 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Stock insuficiente",
                    "No puedes restar más de lo disponible.\n" +
                            "Stock actual: " + producto.getStock() + " unidades.");
            return;
        }

        dao.ajustarStock(producto.getIdProducto(), ajuste);
        cerrar();
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) lblStockActual.getScene().getWindow()).close();
    }
}