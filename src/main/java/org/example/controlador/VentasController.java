package org.example.controlador;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class VentasController {

    @FXML private VBox listaProductos;
    @FXML private VBox listaCarrito;
    @FXML private Label lblTotal;
    @FXML private TextField txtBuscar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    // Carrito: id_producto -> {nombre, precio, cantidad}
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
        cargarProductos("");
        txtBuscar.textProperty().addListener((obs, old, nuevo) -> cargarProductos(nuevo));
    }

    private void cargarProductos(String filtro) {
        listaProductos.getChildren().clear();
        String sql = "SELECT id_producto, nombre, precio, stock FROM productos WHERE activo = 1 AND nombre LIKE ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + filtro + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");

                // Crear tarjeta del producto
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
                lblStock.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 11px;");

                card.getChildren().addAll(fila, lblStock);

                // Al hacer clic agregar al carrito
                final int pid = id;
                final String pnombre = nombre;
                final double pprecio = precio;
                final int pstock = stock;
                card.setOnMouseClicked(e -> agregarAlCarrito(pid, pnombre, pprecio, pstock));

                listaProductos.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (stock <= 0) {
                mostrarAlerta("Sin stock", "Este producto no tiene stock.");
                return;
            }
            carrito.put(id, new Object[]{nombre, precio, 1});
        }
        actualizarCarrito();
    }

    private void actualizarCarrito() {
        listaCarrito.getChildren().clear();
        total = 0;

        for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
            int id = entry.getKey();
            Object[] item = entry.getValue();
            String nombre = (String) item[0];
            double precio = (double) item[1];
            int cantidad = (int) item[2];
            double subtotal = precio * cantidad;
            total += subtotal;

            HBox fila = new HBox(8);
            fila.getStyleClass().add("carrito-item");
            fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label lblNombre = new Label(nombre);
            lblNombre.setStyle("-fx-text-fill: #6B4226; -fx-font-size: 12px;");
            lblNombre.setMaxWidth(140);

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            // Botón restar
            javafx.scene.control.Button btnMenos = new javafx.scene.control.Button("-");
            btnMenos.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white; -fx-background-radius: 4; -fx-min-width: 24; -fx-min-height: 24; -fx-cursor: hand;");
            btnMenos.setOnAction(e -> {
                if (cantidad <= 1) carrito.remove(id);
                else item[2] = cantidad - 1;
                actualizarCarrito();
            });

            Label lblCantidad = new Label(String.valueOf(cantidad));
            lblCantidad.setStyle("-fx-text-fill: #6B4226; -fx-font-weight: bold; -fx-font-size: 13px; -fx-min-width: 20; -fx-alignment: center;");

            // Botón sumar
            javafx.scene.control.Button btnMas = new javafx.scene.control.Button("+");
            btnMas.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white; -fx-background-radius: 4; -fx-min-width: 24; -fx-min-height: 24; -fx-cursor: hand;");
            btnMas.setOnAction(e -> {
                item[2] = cantidad + 1;
                actualizarCarrito();
            });

            Label lblSubtotal = new Label("$" + String.format("%.2f", subtotal));
            lblSubtotal.setStyle("-fx-text-fill: #6B4226; -fx-font-weight: bold; -fx-font-size: 12px; -fx-min-width: 60; -fx-alignment: CENTER_RIGHT;");

            fila.getChildren().addAll(lblNombre, spacer, btnMenos, lblCantidad, btnMas, lblSubtotal);
            listaCarrito.getChildren().add(fila);
        }

        lblTotal.setText("$" + String.format("%.2f", total));
    }

    @FXML
    public void handleCobrar() {
        if (carrito.isEmpty()) {
            mostrarAlerta("Carrito vacío", "Agrega productos antes de cobrar.");
            return;
        }
        // TODO: abrir ventana de pago
        System.out.println("Abriendo ventana de pago... Total: " + total);
    }

    @FXML
    public void handleCancelar() {
        carrito.clear();
        actualizarCarrito();
    }

    @FXML
    public void irADashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblTotal.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

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
            if (respuesta == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}