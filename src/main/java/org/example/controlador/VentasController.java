package org.example.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
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
    @FXML private FlowPane panelCategorias;
    @FXML private Label lblClienteSeleccionado;
    @FXML private Label lblCreditoDisponible;

    private int idClienteSeleccionado = 1; // 1 = Público General por defecto
    private String nombreClienteSeleccionado = "Publico General";
    private double limiteCredito = 0;
    private double saldoCliente = 0;
    private HBox primerProducto = null;
    private String categoriaSeleccionada = "Todas";
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
                cargarProductos(nuevo, categoriaSeleccionada));

        // ENTER en el buscador: si el texto coincide EXACTAMENTE con un código
        // de barras → agrega al carrito y limpia (flujo para lector físico)
        txtBuscar.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String texto = txtBuscar.getText().trim();
                if (!texto.isEmpty() && agregarPorCodigoExacto(texto)) {
                    e.consume(); // evita que el handler global de ENTER vuelva a dispararse
                }
            }
        });

        Platform.runLater(() -> {
            Scene scene = lblTotal.getScene();
            if (scene != null) {

                // Foco en buscar
                txtBuscar.requestFocus();

                scene.setOnKeyPressed(e -> {

                    if (e.getCode() == KeyCode.ENTER) {

                        // ENTER en buscar → agrega primer producto
                        if (scene.getFocusOwner() == txtBuscar) {
                            if (primerProducto != null) {
                                primerProducto.fireEvent(
                                        new javafx.scene.input.MouseEvent(
                                                javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                                                0,0,0,0,
                                                javafx.scene.input.MouseButton.PRIMARY,
                                                1,
                                                true,true,true,true,
                                                true,true,true,true,true,true,
                                                null
                                        )
                                );
                            }
                            return;
                        }

                        // ENTER normal → cobrar
                        if (!(scene.getFocusOwner() instanceof Button) ||
                                scene.getFocusOwner() == btnCobrar) {
                            handleCobrar();
                        }
                    }

                    if (e.getCode() == KeyCode.F2) handleCobrar();
                    if (e.getCode() == KeyCode.F3) txtBuscar.requestFocus();
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
        panelCategorias.getChildren().clear();

        // Botón "Todas"
        Button btnTodas = crearBotonCategoria("Todas", true);
        panelCategorias.getChildren().add(btnTodas);

        String sql = "SELECT nombre FROM categorias";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Button btn = crearBotonCategoria(rs.getString("nombre"), false);
                panelCategorias.getChildren().add(btn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Button crearBotonCategoria(String nombre, boolean activo) {
        Button btn = new Button(nombre);
        actualizarEstiloCategoria(btn, activo);
        btn.setOnAction(e -> {
            categoriaSeleccionada = nombre;
            // Actualizar estilos de todos los botones
            panelCategorias.getChildren().forEach(node -> {
                if (node instanceof Button b) {
                    actualizarEstiloCategoria(b, b.getText().equals(nombre));
                }
            });
            cargarProductos(txtBuscar.getText(), nombre);
        });
        return btn;
    }

    private void actualizarEstiloCategoria(Button btn, boolean activo) {
        if (activo) {
            btn.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: #6B4226; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand; -fx-font-size: 12px; -fx-border-color: #6B4226; -fx-border-radius: 20; -fx-border-width: 1;");
        }
    }

    private void cargarProductos(String filtro, String categoria) {
        listaProductos.getChildren().clear();

        String sql;
        if (categoria == null || categoria.equals("Todas")) {
            sql = "SELECT p.id_producto, p.nombre, p.codigo_barras, p.precio, p.stock " +
                    "FROM productos p " +
                    "WHERE p.activo = 1 AND (p.nombre LIKE ? OR p.codigo_barras LIKE ?)";
        } else {
            sql = "SELECT p.id_producto, p.nombre, p.codigo_barras, p.precio, p.stock " +
                    "FROM productos p JOIN categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.activo = 1 AND (p.nombre LIKE ? OR p.codigo_barras LIKE ?) AND c.nombre = ?";
        }

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String patron = "%" + (filtro == null ? "" : filtro) + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            if (categoria != null && !categoria.equals("Todas")) {
                ps.setString(3, categoria);
            }

            ResultSet rs = ps.executeQuery();
            primerProducto = null;
            while (rs.next()) {
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                String codigo = rs.getString("codigo_barras");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");

                // Card principal
                HBox card = new HBox(12);
                card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12 16; -fx-effect: dropshadow(gaussian, #00000010, 6, 0, 0, 1);");

                if (primerProducto == null && stock > 0) {
                    primerProducto = card;
                    card.setStyle(card.getStyle() + "; -fx-border-color: #6B4226; -fx-border-width: 2;");
                }

                String colorStock;
                String textoStock;
                if (stock == 0) {
                    colorStock = "#C0392B"; textoStock = "Sin stock";
                } else if (stock <= 5) {
                    colorStock = "#E67E22"; textoStock = "Stock: " + stock;
                } else {
                    colorStock = "#27AE60"; textoStock = "Stock: " + stock;
                }

                javafx.scene.layout.StackPane indicador = new javafx.scene.layout.StackPane();
                indicador.setMinWidth(4); indicador.setMaxWidth(4);
                indicador.setMinHeight(40);
                indicador.setStyle("-fx-background-color: " + colorStock + "; -fx-background-radius: 2;");

                VBox info = new VBox(3);
                HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

                Label lblNombre = new Label(nombre);
                lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-font-size: 13px;");

                HBox filaBaja = new HBox(8);
                filaBaja.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label lblPrecio = new Label("$" + String.format("%.2f", precio));
                lblPrecio.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-font-size: 14px;");

                Label lblStockLabel = new Label(textoStock);
                lblStockLabel.setStyle("-fx-text-fill: " + colorStock + "; -fx-font-size: 11px; -fx-background-color: " + colorStock + "22; -fx-background-radius: 6; -fx-padding: 2 8;");

                filaBaja.getChildren().addAll(lblPrecio, lblStockLabel);

                // Mostrar código de barras si existe
                if (codigo != null && !codigo.isBlank()) {
                    Label lblCodigo = new Label("↳ " + codigo);
                    lblCodigo.setStyle("-fx-text-fill: #A0856A; -fx-font-size: 10px;");
                    info.getChildren().addAll(lblNombre, filaBaja, lblCodigo);
                } else {
                    info.getChildren().addAll(lblNombre, filaBaja);
                }

                Button btnAgregar = new Button("+");
                btnAgregar.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36; -fx-cursor: hand; -fx-padding: 0; -fx-alignment: center;");
                if (stock > 0) {
                    btnAgregar.setOnAction(e -> agregarAlCarrito(id, nombre, precio, stock));
                    card.setOnMouseClicked(e -> agregarAlCarrito(id, nombre, precio, stock));
                } else {
                    btnAgregar.setDisable(true);
                    card.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 12; -fx-padding: 12 16; -fx-opacity: 0.6;");
                }

                card.getChildren().addAll(indicador, info, btnAgregar);
                listaProductos.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Busca un producto por código de barras EXACTO y lo agrega al carrito.
     * Retorna true si encontró y agregó el producto (para que ENTER no haga
     * otra cosa), false si no había coincidencia exacta.
     */
    private boolean agregarPorCodigoExacto(String codigo) {
        String sql = "SELECT id_producto, nombre, precio, stock FROM productos " +
                "WHERE activo = 1 AND codigo_barras = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id      = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                int stock   = rs.getInt("stock");
                if (stock <= 0) {
                    mostrarAlerta("Sin stock", "\"" + nombre + "\" no tiene unidades disponibles.");
                    return true; // sí lo encontramos aunque no lo agreguemos
                }
                agregarAlCarrito(id, nombre, precio, stock);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Nuevo metodo para obtener stock real desde BD
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
        Platform.runLater(() -> {
            txtBuscar.clear();
            txtBuscar.requestFocus();
        });
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
    public void seleccionarCliente() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar cliente");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 16; -fx-min-width: 400;");

        TextField txtBuscarCliente = new TextField();
        txtBuscarCliente.setPromptText("Buscar cliente...");
        txtBuscarCliente.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-padding: 8;");

        ListView<String> listaClientes = new ListView<>();
        listaClientes.setPrefHeight(250);

        // Cargar clientes
        java.util.List<int[]> idsClientes = new java.util.ArrayList<>();
        java.util.List<double[]> creditosClientes = new java.util.ArrayList<>();

        Runnable cargarLista = () -> {
            listaClientes.getItems().clear();
            idsClientes.clear();
            creditosClientes.clear();

            // Siempre mostrar Público General primero
            listaClientes.getItems().add("Publico General");
            idsClientes.add(new int[]{1});
            creditosClientes.add(new double[]{0, 0});

            String sql = "SELECT id_cliente, nombre, limite_credito, saldo_actual FROM clientes WHERE activo = 1 AND nombre != 'Publico General' AND nombre LIKE ?";
            try (Connection con = ConexionDB.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, "%" + txtBuscarCliente.getText().trim() + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    double limite = rs.getDouble("limite_credito");
                    double saldo = rs.getDouble("saldo_actual");
                    double disponible = limite - saldo;
                    String texto = rs.getString("nombre") +
                            (limite > 0 ? " — Disponible: $" + String.format("%.2f", disponible) : "");
                    listaClientes.getItems().add(texto);
                    idsClientes.add(new int[]{rs.getInt("id_cliente")});
                    creditosClientes.add(new double[]{limite, saldo});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        cargarLista.run();
        txtBuscarCliente.textProperty().addListener((obs, old, nuevo) -> cargarLista.run());

        contenido.getChildren().addAll(txtBuscarCliente, listaClientes);
        dialog.getDialogPane().setContent(contenido);

        dialog.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                int index = listaClientes.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    idClienteSeleccionado = idsClientes.get(index)[0];
                    limiteCredito = creditosClientes.get(index)[0];
                    saldoCliente = creditosClientes.get(index)[1];

                    // Obtener nombre limpio sin el crédito disponible
                    String itemSeleccionado = listaClientes.getItems().get(index);
                    nombreClienteSeleccionado = itemSeleccionado.contains(" — ")
                            ? itemSeleccionado.split(" — ")[0]
                            : itemSeleccionado;

                    lblClienteSeleccionado.setText(nombreClienteSeleccionado);

                    if (limiteCredito > 0) {
                        double disponible = limiteCredito - saldoCliente;
                        lblCreditoDisponible.setText("Credito disponible: $" + String.format("%.2f", disponible));
                        lblCreditoDisponible.setStyle("-fx-font-size: 11px; -fx-text-fill: " +
                                (disponible <= 0 ? "#C0392B" : "#3B6D11") + ";");
                    } else {
                        lblCreditoDisponible.setText("");
                    }
                }
            }
        });
    }
    @FXML
    public void handleCobrar() {
        if (carrito.isEmpty()) {
            mostrarAlerta("Carrito vacío", "Agrega productos antes de cobrar.");
            return;
        }

        // Validar límite de crédito si es cliente con fiado
        if (limiteCredito > 0) {
            double disponible = limiteCredito - saldoCliente;
            if (total > disponible) {
                mostrarAlerta("Credito insuficiente",
                        nombreClienteSeleccionado + " solo tiene $" +
                                String.format("%.2f", disponible) + " de credito disponible.");
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Pago.fxml"));
            Parent root = loader.load();
            PagoController pagoController = loader.getController();
            pagoController.setDatos(total, carrito, this, idClienteSeleccionado,
                    nombreClienteSeleccionado, limiteCredito, saldoCliente);
            Stage stagePago = new Stage();
            stagePago.setTitle("Cobro");
            stagePago.setScene(new Scene(root));
            stagePago.setResizable(false);
            stagePago.initModality(javafx.stage.Modality.APPLICATION_MODAL);          // ← bloquea la ventana de atrás
            stagePago.initOwner(lblTotal.getScene().getWindow());                      // ← la ata a la ventana principal
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
    // ─── HISTORIAL DE VENTAS ────────────────────────────────────────────────────

    @FXML
    public void abrirHistorial() {
        Stage stage = new Stage();
        stage.setTitle("Historial de ventas del día");

        TableView<Map<String, Object>> tabla = new TableView<>();
        tabla.setPrefWidth(780);

        TableColumn<Map<String, Object>, String> colFolio = new TableColumn<>("Folio");
        colFolio.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("#%04d", (int) d.getValue().get("folio"))));
        colFolio.setPrefWidth(80);

        TableColumn<Map<String, Object>, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("hora")));
        colHora.setPrefWidth(120);

        // NUEVO: CLIENTE
        TableColumn<Map<String, Object>, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        (String) d.getValue().get("cliente")));
        colCliente.setPrefWidth(180);

        TableColumn<Map<String, Object>, String> colCajero = new TableColumn<>("Cajero");
        colCajero.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("cajero")));
        colCajero.setPrefWidth(160);

        TableColumn<Map<String, Object>, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        "$" + String.format("%.2f", (double) d.getValue().get("total"))));
        colTotal.setPrefWidth(100);

        tabla.getColumns().addAll(colFolio, colHora, colCliente, colCajero, colTotal);

        //  QUERY CON CLIENTE
        String sql = """
        SELECT v.id_venta,
               DATE_FORMAT(v.fecha, '%H:%i:%s') AS hora,
               u.nombre AS cajero,
               c.nombre AS cliente,
               v.total
        FROM ventas v
        JOIN usuarios u ON v.id_usuario = u.id_usuario
        JOIN clientes c ON v.id_cliente = c.id_cliente
        WHERE DATE(v.fecha) = CURDATE()
        ORDER BY v.fecha DESC
    """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("folio", rs.getInt("id_venta"));
                fila.put("hora", rs.getString("hora"));
                fila.put("cajero", rs.getString("cajero"));
                fila.put("cliente", rs.getString("cliente")); // 🔥 CLAVE
                fila.put("total", rs.getDouble("total"));

                tabla.getItems().add(fila);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Doble clic → detalle
        tabla.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tabla.getSelectionModel().getSelectedItem() != null) {
                Map<String, Object> fila = tabla.getSelectionModel().getSelectedItem();
                int idVenta = (int) fila.get("folio");
                mostrarDetalleVenta(idVenta);
            }
        });

        Label hint = new Label("Doble clic para ver detalle");
        hint.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 11px;");

        VBox layout = new VBox(10, tabla, hint);
        layout.setStyle("-fx-padding: 16; -fx-background-color: #F5EFE6;");

        stage.setScene(new Scene(layout, 800, 480));
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void mostrarDetalleVenta(int idVenta) {
        Stage stage = new Stage();
        stage.setTitle("Detalle de venta #" + String.format("%04d", idVenta));

        TableView<Map<String, Object>> tabla = new TableView<>();
        tabla.setPrefWidth(480);

        TableColumn<Map<String, Object>, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("producto")));
        colProd.setPrefWidth(200);

        TableColumn<Map<String, Object>, String> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf((int) d.getValue().get("cantidad"))));
        colCant.setPrefWidth(70);

        TableColumn<Map<String, Object>, String> colPrecio = new TableColumn<>("Precio unit.");
        colPrecio.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        "$" + String.format("%.2f", (double) d.getValue().get("precio"))));
        colPrecio.setPrefWidth(100);

        TableColumn<Map<String, Object>, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        "$" + String.format("%.2f", (double) d.getValue().get("subtotal"))));
        colSub.setPrefWidth(100);

        tabla.getColumns().addAll(colProd, colCant, colPrecio, colSub);

        String sql = """
        SELECT p.nombre AS producto, dv.cantidad,
               dv.precio_unitario AS precio,
               (dv.cantidad * dv.precio_unitario) AS subtotal
        FROM detalle_venta dv
        JOIN productos p ON dv.id_producto = p.id_producto
        WHERE dv.id_venta = ?
    """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("producto", rs.getString("producto"));
                fila.put("cantidad", rs.getInt("cantidad"));
                fila.put("precio", rs.getDouble("precio"));
                fila.put("subtotal", rs.getDouble("subtotal"));
                tabla.getItems().add(fila);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox layout = new VBox(10, tabla);
        layout.setStyle("-fx-padding: 16; -fx-background-color: #F5EFE6;");

        stage.setScene(new Scene(layout, 520, 360));
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.show();
    }

// ─── INGRESO DE EFECTIVO ────────────────────────────────────────────────────

    @FXML
    public void abrirIngreso() {
        abrirMovimientoCaja("INGRESO");
    }

// ─── SALIDA DE EFECTIVO ─────────────────────────────────────────────────────

    @FXML
    public void abrirSalida() {
        abrirMovimientoCaja("RETIRO");
    }

    private void abrirMovimientoCaja(String tipo) {
        boolean esIngreso = tipo.equals("INGRESO");
        Stage stage = new Stage();
        stage.setTitle(esIngreso ? "Registrar ingreso de efectivo" : "Registrar salida de efectivo");

        Label lblMonto = new Label("Monto:");
        lblMonto.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226;");
        TextField txtMonto = new TextField();
        txtMonto.setPromptText("$0.00");
        txtMonto.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-border-width: 1; -fx-padding: 8;");

        Label lblMotivo = new Label("Motivo: *");
        lblMotivo.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226;");
        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText("Escribe el motivo...");
        txtMotivo.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-border-width: 1; -fx-padding: 8;");

        String colorBtn = esIngreso ? "#2E7D32" : "#C0392B";
        Button btnGuardar = new Button(esIngreso ? "Registrar ingreso" : "Registrar salida");
        btnGuardar.setStyle("-fx-background-color: " + colorBtn + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand; -fx-font-weight: bold;");
        btnGuardar.setPrefWidth(260);

        btnGuardar.setOnAction(e -> {
            String motivoTxt = txtMotivo.getText().trim();
            String montoTxt  = txtMonto.getText().trim();

            if (motivoTxt.isEmpty()) {
                mostrarAlerta("Campo requerido", "El motivo es obligatorio.");
                return;
            }
            double monto;
            try {
                monto = Double.parseDouble(montoTxt.replace(",", ".").replace("$", ""));
                if (monto <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                mostrarAlerta("Monto inválido", "Ingresa un monto válido mayor a 0.");
                return;
            }

            String sql = "INSERT INTO movimientos_caja (id_caja, tipo, monto, motivo, id_usuario) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = ConexionDB.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, SesionUsuario.getInstancia().getIdCaja()); // ← dinámico, el que se guardó al abrir caja
                ps.setString(2, tipo);
                ps.setDouble(3, monto);
                ps.setString(4, motivoTxt);
                ps.setInt(5, SesionUsuario.getInstancia().getIdUsuario());
                ps.executeUpdate();
                stage.close();
                mostrarAlerta("Listo", tipo.equals("INGRESO")
                        ? "Ingreso de $" + String.format("%.2f", monto) + " registrado."
                        : "Salida de $" + String.format("%.2f", monto) + " registrada.");
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error", "No se pudo registrar el movimiento.");
            }
        });

        VBox layout = new VBox(12, lblMonto, txtMonto, lblMotivo, txtMotivo, btnGuardar);
        layout.setStyle("-fx-padding: 24; -fx-background-color: #F5EFE6;");

        stage.setScene(new Scene(layout, 300, 260));
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.show();
        txtMonto.requestFocus();
    }

    public void ventaCompletada() {
        carrito.clear();
        actualizarCarrito();
        cargarProductos("", "Todas");
        cargarFolio();
        // Resetear cliente
        idClienteSeleccionado = 1;
        nombreClienteSeleccionado = "Publico General";
        limiteCredito = 0;
        saldoCliente = 0;
        lblClienteSeleccionado.setText("Publico General");
        lblCreditoDisponible.setText("");
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
        if (!SesionUsuario.getInstancia().getRol().equals("admin")){
            mostrarAlerta("Acceso Denegado","Solo el Administrador");
        }else{
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Empleados.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) lblTotal.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @FXML
    private void irAClientes() {
        cambiarEscena("/org/example/vista/Clientes.fxml");
    }

}