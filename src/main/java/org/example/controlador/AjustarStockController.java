package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;
import org.example.modelo.SesionUsuario;
import org.example.servicio.AuditoriaService;
import org.example.servicio.InventarioMovimientoService;
import org.example.servicio.PermisoService;
import org.kordamp.ikonli.javafx.FontIcon;

public class AjustarStockController {

    private static final String MODO_SUMAR_ACTIVO = "-fx-background-color: #1a6fa8; -fx-text-fill: white;"
            + "-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 12px;"
            + "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-width: 1; -fx-border-color: #1a6fa8;";
    private static final String MODO_SUMAR_INACTIVO = "-fx-background-color: white; -fx-text-fill: #1a6fa8;"
            + "-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 12px;"
            + "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-width: 1; -fx-border-color: #c8dff0;";
    private static final String MODO_QUITAR_ACTIVO = "-fx-background-color: #C0392B; -fx-text-fill: white;"
            + "-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 12px;"
            + "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-width: 1; -fx-border-color: #C0392B;";
    private static final String MODO_QUITAR_INACTIVO = "-fx-background-color: white; -fx-text-fill: #C0392B;"
            + "-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 12px;"
            + "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-width: 1; -fx-border-color: #c8dff0;";

    private static final String CONFIRMAR_SUMAR = "-fx-background-color: #1a6fa8; -fx-text-fill: white;"
            + "-fx-background-radius: 7; -fx-border-radius: 7; -fx-font-size: 13px;"
            + "-fx-font-weight: bold; -fx-padding: 0 20; -fx-cursor: hand;"
            + "-fx-effect: dropshadow(gaussian, #1a6fa840, 8, 0, 0, 2);";
    private static final String CONFIRMAR_QUITAR = "-fx-background-color: #C0392B; -fx-text-fill: white;"
            + "-fx-background-radius: 7; -fx-border-radius: 7; -fx-font-size: 13px;"
            + "-fx-font-weight: bold; -fx-padding: 0 20; -fx-cursor: hand;";

    private static final String PREVIEW_SUMAR = "-fx-background-color: #e0f5e9; -fx-background-radius: 10;"
            + "-fx-text-fill: #1e7d3e; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 12;";
    private static final String PREVIEW_QUITAR = "-fx-background-color: #fde8e8; -fx-background-radius: 10;"
            + "-fx-text-fill: #C0392B; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 12;";
    private static final String PREVIEW_ERROR = "-fx-background-color: #fde8e8; -fx-background-radius: 10;"
            + "-fx-text-fill: #C0392B; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 12;";

    @FXML private Label lblNombreProducto;
    @FXML private Label lblStockActual;
    @FXML private Label lblPreview;
    @FXML private TextField txtCantidad;
    @FXML private ToggleButton btnModoSumar;
    @FXML private ToggleButton btnModoQuitar;
    @FXML private ToggleGroup grupoOperacion;
    @FXML private Button btnConfirmar;
    @FXML private FontIcon iconConfirmar;

    private Producto producto;
    private boolean ajusteConfirmado;
    private final ProductoDAO dao = new ProductoDAO();

    @FXML
    private void initialize() {
        txtCantidad.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
        txtCantidad.textProperty().addListener((obs, anterior, actual) -> actualizarPreview());
        grupoOperacion.selectedToggleProperty().addListener((obs, anterior, actual) -> {
            if (actual == null && anterior != null) {
                anterior.setSelected(true);
                return;
            }
            actualizarOperacionVisual();
            actualizarPreview();
        });
        actualizarOperacionVisual();
    }

    public void setProducto(Producto p) {
        this.producto = p;
        lblNombreProducto.setText(p.getNombre());
        lblStockActual.setText(String.valueOf(p.getStock()));
        actualizarPreview();
    }

    public boolean isAjusteConfirmado() {
        return ajusteConfirmado;
    }

    @FXML
    private void sumar() {
        cambiarCantidad(1);
    }

    @FXML
    private void restar() {
        cambiarCantidad(-1);
    }

    @FXML
    private void confirmar() {
        ejecutarAjuste(esOperacionSumar());
    }

    private void cambiarCantidad(int delta) {
        Integer actual = leerCantidad();
        int base = actual == null ? 0 : actual;
        txtCantidad.setText(String.valueOf(Math.max(1, base + delta)));
    }

    private void actualizarOperacionVisual() {
        boolean sumar = esOperacionSumar();
        aplicarEstilo(btnModoSumar, sumar ? MODO_SUMAR_ACTIVO : MODO_SUMAR_INACTIVO,
                sumar ? "white" : "#1a6fa8");
        aplicarEstilo(btnModoQuitar, sumar ? MODO_QUITAR_INACTIVO : MODO_QUITAR_ACTIVO,
                sumar ? "#C0392B" : "white");

        btnConfirmar.setText(sumar ? "Sumar stock" : "Quitar stock");
        btnConfirmar.setStyle(sumar ? CONFIRMAR_SUMAR : CONFIRMAR_QUITAR);
        iconConfirmar.setIconLiteral(sumar ? "fas-plus" : "fas-minus");
        iconConfirmar.setIconColor(Paint.valueOf("white"));
    }

    private void aplicarEstilo(ToggleButton boton, String estilo, String colorIcono) {
        boton.setStyle(estilo);
        if (boton.getGraphic() instanceof FontIcon icono) {
            icono.setIconColor(Paint.valueOf(colorIcono));
        }
    }

    private boolean esOperacionSumar() {
        return btnModoSumar == null || btnModoSumar.isSelected();
    }

    private void actualizarPreview() {
        if (producto == null || lblPreview == null || btnConfirmar == null) {
            return;
        }

        Integer cantidad = leerCantidad();
        if (cantidad == null) {
            mostrarPreview("Ingresa una cantidad mayor a 0", PREVIEW_ERROR, false);
            return;
        }

        boolean sumar = esOperacionSumar();
        long resultado = sumar
                ? (long) producto.getStock() + cantidad
                : (long) producto.getStock() - cantidad;

        if (resultado > Integer.MAX_VALUE) {
            mostrarPreview("La cantidad es demasiado grande", PREVIEW_ERROR, false);
            return;
        }

        if (resultado < 0) {
            mostrarPreview("No hay stock suficiente. Disponible: " + producto.getStock(), PREVIEW_ERROR, false);
            return;
        }

        mostrarPreview("Stock resultante: " + resultado, sumar ? PREVIEW_SUMAR : PREVIEW_QUITAR, true);
    }

    private void mostrarPreview(String mensaje, String estilo, boolean puedeConfirmar) {
        lblPreview.setText(mensaje);
        lblPreview.setStyle(estilo);
        btnConfirmar.setDisable(!puedeConfirmar);
        btnConfirmar.setOpacity(puedeConfirmar ? 1.0 : 0.55);
    }

    private Integer leerCantidad() {
        String texto = txtCantidad.getText() == null ? "" : txtCantidad.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            int cantidad = Integer.parseInt(texto);
            return cantidad > 0 ? cantidad : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void ejecutarAjuste(boolean esAdicion) {
        Integer cantidad = leerCantidad();
        if (cantidad == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cantidad invalida",
                    "Ingresa un numero entero mayor a 0.");
            return;
        }

        int ajuste = esAdicion ? cantidad : -cantidad;
        long resultadoCalculado = (long) producto.getStock() + ajuste;

        if (resultadoCalculado < 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Stock insuficiente",
                    "No puedes quitar mas de lo que hay en stock.\nStock actual: " + producto.getStock());
            return;
        }

        if (resultadoCalculado > Integer.MAX_VALUE) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cantidad invalida",
                    "La cantidad es demasiado grande.");
            return;
        }

        if (!PermisoService.requerirPermisoOAutorizacionAdmin(
                PermisoService.INVENTARIO_AJUSTAR,
                "Ajuste manual de stock")) {
            return;
        }

        int resultado = (int) resultadoCalculado;
        try {
            aplicarAjusteTransaccional(esAdicion, cantidad, ajuste);
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo ajustar stock", mensajeError(e));
            return;
        }

        producto.setStock(resultado);
        lblStockActual.setText(String.valueOf(resultado));
        ajusteConfirmado = true;

        AuditoriaService.get().registrar(
                "AJUSTE_STOCK", "productos", producto.getIdProducto(),
                String.format("Producto: %s - %s %d unidades - Stock resultante: %d",
                        producto.getNombre(),
                        esAdicion ? "Entrada" : "Salida",
                        cantidad, resultado)
        );

        cerrar();
    }

    private void aplicarAjusteTransaccional(boolean esAdicion, int cantidad, int ajuste) throws Exception {
        java.sql.Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);
            registrarMovimientoInventario(con, esAdicion, cantidad);
            if (!dao.ajustarStock(con, producto.getIdProducto(), ajuste)) {
                throw new IllegalStateException("Stock insuficiente o producto no encontrado.");
            }
            con.commit();
        } catch (Exception e) {
            rollbackSilencioso(con);
            throw e;
        } finally {
            cerrarConexion(con);
        }
    }

    private void registrarMovimientoInventario(java.sql.Connection con, boolean esAdicion, int cantidad) throws Exception {
        InventarioMovimientoService.TipoMovimiento tipo = esAdicion
                ? InventarioMovimientoService.TipoMovimiento.AJUSTE_ENTRADA
                : InventarioMovimientoService.TipoMovimiento.AJUSTE_SALIDA;

        InventarioMovimientoService.get().registrar(
                con, producto.getIdProducto(), tipo, cantidad,
                0, "AJUSTE_MANUAL",
                "Ajuste manual de stock por " + SesionUsuario.getInstancia().getNombre()
        );
    }

    private void rollbackSilencioso(java.sql.Connection con) {
        if (con == null) return;
        try {
            con.rollback();
        } catch (Exception ignored) {
        }
    }

    private void cerrarConexion(java.sql.Connection con) {
        if (con == null) return;
        try {
            con.setAutoCommit(true);
        } catch (Exception ignored) {
        }
        try {
            con.close();
        } catch (Exception ignored) {
        }
    }

    private String mensajeError(Exception e) {
        return e.getMessage() == null || e.getMessage().isBlank()
                ? "Ocurrio un error al actualizar el inventario."
                : e.getMessage();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        if (tipo == Alert.AlertType.ERROR) {
            org.example.servicio.DialogService.error(lblStockActual, titulo, mensaje);
        } else if (tipo == Alert.AlertType.INFORMATION) {
            org.example.servicio.DialogService.info(lblStockActual, titulo, mensaje);
        } else {
            org.example.servicio.DialogService.advertencia(lblStockActual, titulo, mensaje);
        }
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) lblStockActual.getScene().getWindow()).close();
    }
}
