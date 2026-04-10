package org.example.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class VentasController {

    @FXML private VBox listaProductos;
    @FXML private VBox listaCarrito;
    @FXML private Label lblTotal;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblCantidadItems;
    @FXML private Label lblFolio;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;
    @FXML private Button btnCobrar;
    @FXML private Label lblFecha;

    private Map<Integer, Object[]> carrito = new HashMap<>();
    private double total = 0;

    @FXML
    public void initialize() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);

        cargarCategorias();
        cargarProductos("", "Todas");
        cargarFolio();

        txtBuscar.textProperty().addListener((obs, old, nuevo) ->
                cargarProductos(nuevo, cmbCategoria.getValue()));
        cmbCategoria.valueProperty().addListener((obs, old, nuevo) ->
                cargarProductos(txtBuscar.getText(), nuevo));

        Platform.runLater(() -> {
            Scene scene = lblTotal.getScene();
            if (scene != null) {
                // Poner foco en cobrar al iniciar
                btnCobrar.requestFocus();

                scene.setOnKeyPressed(e -> {
                    // Enter solo si el foco NO está en un botón del sidebar
                    if (e.getCode() == KeyCode.ENTER) {
                        if (!(scene.getFocusOwner() instanceof Button) ||
                                scene.getFocusOwner() == btnCobrar) {
                            handleCobrar();
                        }
                    }
                    if (e.getCode() == KeyCode.ESCAPE) handleCancelar();
                    if (e.getCode() == KeyCode.F2) txtBuscar.requestFocus();
                });
            }
        });
    }

    private void cargarFolio() {
        String sql = "SELECT COUNT(*) + 1 FROM ventas";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblFolio.setText(String.format("Folio #%04d", rs.getInt(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarCategorias() {
        cmbCategoria.getItems().add("Todas");
        String sql = "SELECT nombre FROM categorias";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cmbCategoria.getItems().add(rs.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cmbCategoria.setValue("Todas");
    }

    private void cargarProductos(String filtro, String categoria) {
        listaProductos.getChildren().clear();

        String sql;
        if (categoria == null || categoria.equals("Todas")) {
            sql = "SELECT p.id_producto, p.nombre, p.precio, p.stock FROM productos p WHERE p.activo = 1 AND p.nombre LIKE ?";
        } else {
            sql = "SELECT p.id_producto, p.nombre, p.precio, p.stock FROM productos p JOIN categorias c ON p.id_categoria = c.id_categoria WHERE p.activo = 1 AND p.nombre LIKE ? AND c.nombre = ?";
        }

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + (filtro == null ? "" : filtro) + "%");
            if (categoria != null && !categoria.equals("Todas")) {
                ps.setString(2, categoria);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");

                VBox card = new VBox(4);
                card.getStyleClass().add("producto-card");

                HBox fila = new HBox();
                Label lblNombre = new Label(nombre);
                lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-font-size: 13px;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                Label lblPrecio = new Label("$" + String.format("%.2f", precio));
                lblPrecio.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-font-size: 13px;");
                fila.getChildren().addAll(lblNombre, spacer, lblPrecio);

                Label lblStock = new Label("Stock: " + stock);
                lblStock.setStyle(stock <= 5
                        ? "-fx-text-fill: #C0392B; -fx-font-size: 11px;"
                        : "-fx-text-fill: #7A5535; -fx-font-size: 11px;");

                card.getChildren().addAll(fila, lblStock);

                if (stock > 0) {
                    card.setOnMouseClicked(e -> agregarAlCarrito(id, nombre, precio, stock));
                } else {
                    card.setStyle("-fx-opacity: 0.5;");
                }

                listaProductos.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ← Nuevo método para obtener stock real desde BD
    private int obtenerStock(int idProducto) {
        String sql = "SELECT stock FROM productos WHERE id_producto = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stock");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void agregarAlCarrito(int id, String nombre, double precio, int stock) {
        if (carrito.containsKey(id)) {
            Object[] item = carrito.get(id);
            int cantidad = (int) item[2];
            if (cantidad >= stock) {
                mostrarAlerta("Sin stock", "No hay mas unidades disponibles.");
                return;
            }
            item[2] = cantidad + 1;
        } else {
            carrito.put(id, new Object[]{nombre, precio, 1});
        }
        actualizarCarrito();
    }

    private void actualizarCarrito() {
        listaCarrito.getChildren().clear();
        total = 0;
        int totalItems = 0;

        for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
            int id = entry.getKey();
            Object[] item = entry.getValue();
            String nombre = (String) item[0];
            double precio = (double) item[1];
            int cantidad = (int) item[2];
            double subtotal = precio * cantidad;
            total += subtotal;
            totalItems += cantidad;

            HBox fila = new HBox(8);
            fila.getStyleClass().add("carrito-item");
            fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label lblNombre = new Label(nombre);
            lblNombre.setStyle("-fx-text-fill: #6B4226; -fx-font-size: 12px;");
            lblNombre.setMaxWidth(130);

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Button btnMenos = new Button("-");
            btnMenos.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white; -fx-background-radius: 4; -fx-min-width: 24; -fx-min-height: 24; -fx-cursor: hand;");
            btnMenos.setOnAction(e -> {
                if (cantidad <= 1) carrito.remove(id);
                else item[2] = cantidad - 1;
                actualizarCarrito();
            });

            TextField tfCantidad = new TextField(String.valueOf(cantidad));
            tfCantidad.setStyle("-fx-text-fill: #6B4226; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: center; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #6B4226; -fx-border-width: 1; -fx-pref-width: 50; -fx-max-width: 50;");

            // ← Lógica con validación de stock
            Runnable validarYActualizar = () -> {
                try {
                    int nuevaCantidad = Integer.parseInt(tfCantidad.getText().trim());
                    if (nuevaCantidad <= 0) {
                        carrito.remove(id);
                    } else {
                        int stockReal = obtenerStock(id);
                        if (nuevaCantidad > stockReal) {
                            mostrarAlerta("Stock insuficiente", "Solo hay " + stockReal + " unidades disponibles.");
                            item[2] = stockReal;
                        } else {
                            item[2] = nuevaCantidad;
                        }
                    }
                    actualizarCarrito();
                } catch (NumberFormatException ex) {
                    tfCantidad.setText(String.valueOf(cantidad));
                }
            };

            tfCantidad.setOnAction(e -> validarYActualizar.run());
            tfCantidad.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) validarYActualizar.run();
            });

            Button btnMas = new Button("+");
            btnMas.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white; -fx-background-radius: 4; -fx-min-width: 24; -fx-min-height: 24; -fx-cursor: hand;");
            btnMas.setOnAction(e -> {
                int stockReal = obtenerStock(id);
                if (cantidad >= stockReal) {
                    mostrarAlerta("Sin stock", "No hay mas unidades disponibles.");
                    return;
                }
                item[2] = cantidad + 1;
                actualizarCarrito();
            });

            Label lblSub = new Label("$" + String.format("%.2f", subtotal));
            lblSub.setStyle("-fx-text-fill: #6B4226; -fx-font-weight: bold; -fx-font-size: 12px; -fx-min-width: 60; -fx-alignment: CENTER_RIGHT;");

            fila.getChildren().addAll(lblNombre, spacer, btnMenos, tfCantidad, btnMas, lblSub);
            listaCarrito.getChildren().add(fila);
        }

        double iva = total * 0.16;
        double subtotalSinIva = total - iva;
        lblSubtotal.setText("$" + String.format("%.2f", subtotalSinIva));
        lblIva.setText("$" + String.format("%.2f", iva));
        lblTotal.setText("$" + String.format("%.2f", total));
        lblCantidadItems.setText(totalItems + " items");
    }

    @FXML
    public void handleCobrar() {
        if (carrito.isEmpty()) {
            mostrarAlerta("Carrito vacío", "Agrega productos antes de cobrar.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Pago.fxml"));
            Parent root = loader.load();
            PagoController pagoController = loader.getController();
            pagoController.setDatos(total, carrito, this);
            Stage stagePago = new Stage();
            stagePago.setTitle("Cobro");
            stagePago.setScene(new Scene(root));
            stagePago.setResizable(false);
            stagePago.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancelar() {
        if (carrito.isEmpty()) return;
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cancelar venta");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas cancelar la venta? Se perderan los productos del carrito.");
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                carrito.clear();
                actualizarCarrito();
            }
        });
    }

    public void ventaCompletada() {
        carrito.clear();
        actualizarCarrito();
        cargarProductos("", "Todas");
        cargarFolio();
        mostrarAlerta("Venta completada", "La venta se registro correctamente.");
    }

    @FXML
    public void irADashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblTotal.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAInventario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Inventario.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAReportes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Reportes.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAEmpleados() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Empleados.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblTotal.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void btnCerrar() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Salir");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas salir?");
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) Platform.exit();
        });
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cambiarEscena(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) lblTotal.getScene().getWindow(); // ← FIX
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void irACorteCaja() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/CorteCaja.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblTotal.getScene().getWindow(); // ← FIX
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}