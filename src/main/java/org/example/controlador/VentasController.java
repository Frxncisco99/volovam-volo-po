package org.example.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.example.dao.DevolucionDAO;
import org.example.modelo.DevolucionLinea;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import java.util.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.servicio.MarcaService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;
    @FXML private Button btnCobrar;
    @FXML private FlowPane panelCategorias;
    @FXML private Label lblClienteSeleccionado;
    @FXML private Label lblCreditoDisponible;

    // ── Label de ventas en espera (se actualiza dinámicamente) ──
    @FXML private Label lblEnEspera;

    private int idClienteSeleccionado = 1;
    private String nombreClienteSeleccionado = "Publico General";
    private double limiteCredito = 0;
    private double saldoCliente = 0;
    private HBox primerProducto = null;
    private String categoriaSeleccionada = "Todas";
    private Map<Integer, Object[]> carrito = new HashMap<>();
    private double total = 0;

    // ── Ventas en espera: lista de snapshots ──
    // Cada snapshot: [carrito, idCliente, nombreCliente, limiteCredito, saldoCliente, etiqueta]
    private final List<Object[]> ventasEnEspera = new ArrayList<>();

    // ─────────────────────────────────────────────
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
        actualizarBadgeEspera();

        txtBuscar.textProperty().addListener((obs, old, nuevo) ->
                cargarProductos(nuevo, categoriaSeleccionada));

        txtBuscar.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String texto = txtBuscar.getText().trim();
                if (!texto.isEmpty() && agregarPorCodigoExacto(texto)) e.consume();
            }
        });

        Platform.runLater(() -> {
            Scene scene = lblTotal.getScene();
            if (scene != null) {
                txtBuscar.requestFocus();
                scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER && scene.getFocusOwner() != txtBuscar) {
                        e.consume();
                    }
                });
                scene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        if (scene.getFocusOwner() == txtBuscar) {
                            if (primerProducto != null) {
                                primerProducto.fireEvent(new javafx.scene.input.MouseEvent(
                                        javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                                        0,0,0,0, javafx.scene.input.MouseButton.PRIMARY, 1,
                                        true,true,true,true,true,true,true,true,true,true,null));
                            }
                            e.consume();
                        }
                        return;
                    }
                    if (e.getCode() == KeyCode.F2) handleCobrar();
                    if (e.getCode() == KeyCode.F3) txtBuscar.requestFocus();
                });
            }
        });
    }


    // VENTAS EN ESPERA


    /** Pausa la venta actual y la guarda en espera */
    @FXML
    public void ponerEnEspera() {
        if (carrito.isEmpty()) {
            mostrarAlerta("Carrito vacío", "No hay productos en el carrito para poner en espera.");
            return;
        }

        // Pedir etiqueta opcional
        TextInputDialog dialog = new TextInputDialog("Mesa " + (ventasEnEspera.size() + 1));
        dialog.setTitle("Poner en espera");
        dialog.setHeaderText(null);
        dialog.setContentText("Nombre o referencia de esta venta:");
        String etiqueta = dialog.showAndWait().orElse("Venta " + (ventasEnEspera.size() + 1));

        // Guardar copia del carrito
        Map<Integer, Object[]> copia = new HashMap<>();
        for (Map.Entry<Integer, Object[]> e : carrito.entrySet()) {
            Object[] orig = e.getValue();
            copia.put(e.getKey(), new Object[]{orig[0], orig[1], orig[2]});
        }

        ventasEnEspera.add(new Object[]{
                copia,
                idClienteSeleccionado,
                nombreClienteSeleccionado,
                limiteCredito,
                saldoCliente,
                etiqueta
        });

        // Limpiar carrito actual
        carrito.clear();
        actualizarCarrito();
        idClienteSeleccionado = 1;
        nombreClienteSeleccionado = "Publico General";
        limiteCredito = 0;
        saldoCliente = 0;
        lblClienteSeleccionado.setText("Publico General");
        lblCreditoDisponible.setText("");

        actualizarBadgeEspera();
    }

    /** Muestra las ventas en espera para recuperar una */
    @FXML
    @SuppressWarnings("unchecked")
    public void verVentasEnEspera() {
        if (ventasEnEspera.isEmpty()) {
            mostrarAlerta("Sin ventas en espera", "No hay ventas guardadas en espera.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Ventas en espera");
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.initOwner(lblTotal.getScene().getWindow());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-background-color: #F5EFE6;");

        Label titulo = new Label("Selecciona una venta para recuperarla");
        titulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-font-size: 13px;");
        layout.getChildren().add(titulo);

        for (int i = 0; i < ventasEnEspera.size(); i++) {
            final int idx = i;
            Object[] snap = ventasEnEspera.get(i);
            String etiqueta   = (String) snap[5];
            String cliente    = (String) snap[2];
            Map<Integer, Object[]> carritoSnap = (Map<Integer, Object[]>) snap[0];

            // Calcular total del snapshot
            double totalSnap = carritoSnap.values().stream()
                    .mapToDouble(o -> (double) o[1] * (int) o[2]).sum();

            HBox fila = new HBox(12);
            fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            fila.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12 16;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 1);");

            VBox info = new VBox(3);
            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
            Label lblEtiqueta = new Label(etiqueta);
            lblEtiqueta.setStyle("-fx-font-weight: bold; -fx-text-fill: #3D1F0D; -fx-font-size: 13px;");
            Label lblInfo = new Label(cliente + " · " + carritoSnap.size() + " producto(s) · $" + String.format("%.2f", totalSnap));
            lblInfo.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 11px;");
            info.getChildren().addAll(lblEtiqueta, lblInfo);

            Button btnRecuperar = new Button("Recuperar");
            btnRecuperar.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white; -fx-background-radius: 8;" +
                    "-fx-padding: 6 14; -fx-cursor: hand; -fx-font-weight: bold;");
            btnRecuperar.setOnAction(e -> {
                // Si hay carrito activo, preguntar
                if (!carrito.isEmpty()) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Carrito activo");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Tienes productos en el carrito actual. ¿Descartar y recuperar la venta en espera?");
                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
                }
                recuperarVentaEnEspera(idx);
                stage.close();
            });

            Button btnEliminar = new Button("✕");
            btnEliminar.setStyle("-fx-background-color: #FDEDEC; -fx-text-fill: #C0392B; -fx-background-radius: 8;" +
                    "-fx-padding: 6 10; -fx-cursor: hand; -fx-font-weight: bold;");
            btnEliminar.setOnAction(e -> {
                ventasEnEspera.remove(idx);
                actualizarBadgeEspera();
                stage.close();
                if (!ventasEnEspera.isEmpty()) verVentasEnEspera();
            });

            fila.getChildren().addAll(info, btnRecuperar, btnEliminar);
            layout.getChildren().add(fila);
        }

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        stage.setScene(new Scene(scroll, 500, Math.min(400, 100 + ventasEnEspera.size() * 80)));
        stage.setResizable(false);
        stage.show();
    }

    @SuppressWarnings("unchecked")
    private void recuperarVentaEnEspera(int idx) {
        Object[] snap = ventasEnEspera.remove(idx);
        carrito               = new HashMap<>((Map<Integer, Object[]>) snap[0]);
        idClienteSeleccionado = (int)    snap[1];
        nombreClienteSeleccionado = (String) snap[2];
        limiteCredito         = (double) snap[3];
        saldoCliente          = (double) snap[4];

        lblClienteSeleccionado.setText(nombreClienteSeleccionado);
        if (limiteCredito > 0) {
            double disponible = limiteCredito - saldoCliente;
            lblCreditoDisponible.setText("Credito disponible: $" + String.format("%.2f", disponible));
        } else {
            lblCreditoDisponible.setText("");
        }
        actualizarCarrito();
        actualizarBadgeEspera();
    }

    private void actualizarBadgeEspera() {
        if (lblEnEspera != null) {
            int n = ventasEnEspera.size();
            lblEnEspera.setText(n > 0 ? String.valueOf(n) : "");
            lblEnEspera.setVisible(n > 0);
            lblEnEspera.setManaged(n > 0);
        }
    }


    // DEVOLUCIONES


    @FXML
    public void abrirDevoluciones() {
        try {
            Stage stage = new Stage();
            stage.setTitle("Devoluciones");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(lblTotal.getScene().getWindow());

            VBox root = construirPantallaDevoluciones(stage);
            stage.setScene(new Scene(root, 860, 640));
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox construirPantallaDevoluciones(Stage stage) {
        DevolucionDAO dao = new DevolucionDAO();

        // ── Layout principal
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #dddddd;");

        // ── Header
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #091e4e; -fx-padding: 16 20;");
        Label titulo = new Label("Devoluciones");
        titulo.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold;");
        header.getChildren().add(titulo);
        root.getChildren().add(header);

        // ── Contenido en dos columnas
        HBox contenido = new HBox(12);
        contenido.setStyle("-fx-padding: 16;");
        VBox.setVgrow(contenido, Priority.ALWAYS);

        // ── COLUMNA IZQUIERDA: lista de ventas
        VBox colIzq = new VBox(10);
        colIzq.setPrefWidth(400);
        colIzq.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; " +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

        Label lblVentas = new Label("Seleccionar venta");
        lblVentas.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000; -fx-font-size: 13px;");

        TextField txtFiltro = new TextField();
        txtFiltro.setPromptText("Buscar por folio o cliente...");
        txtFiltro.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #D4C9B0; -fx-padding: 8; -fx-font-size: 12px;");

        TableView<Map<String, Object>> tablaVentas = new TableView<>();
        tablaVentas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tablaVentas, Priority.ALWAYS);

        TableColumn<Map<String, Object>, String> cFolio = new TableColumn<>("Folio");
        cFolio.setCellValueFactory(d ->
                new SimpleStringProperty("#" + String.format("%04d", (int) d.getValue().get("id_venta"))));
        cFolio.setPrefWidth(65);

        TableColumn<Map<String, Object>, String> cFecha = new TableColumn<>("Fecha");
        cFecha.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("fecha")));

        TableColumn<Map<String, Object>, String> cTotal = new TableColumn<>("Total");
        cTotal.setCellValueFactory(d ->
                new SimpleStringProperty("$" + String.format("%.2f", (double) d.getValue().get("total"))));
        cTotal.setPrefWidth(80);

        TableColumn<Map<String, Object>, String> cEstado = new TableColumn<>("Estado");
        cEstado.setCellValueFactory(d ->
                new SimpleStringProperty(traducirEstado((String) d.getValue().get("estado"))));
        cEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setStyle(""); return; }
                setText(estado);
                String color = estado.contains("Parcial") ? "#E67E22" : "#27AE60";
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 10px;");
            }
        });

        tablaVentas.getColumns().addAll(cFolio, cFecha, cTotal, cEstado);

        // Cargar ventas iniciales
        Runnable cargarVentas = () -> {
            tablaVentas.getItems().setAll(dao.obtenerVentasRecientes(txtFiltro.getText().trim(), 50));
        };
        cargarVentas.run();
        txtFiltro.textProperty().addListener((obs, o, n) -> cargarVentas.run());

        colIzq.getChildren().addAll(lblVentas, txtFiltro, tablaVentas);

        // ── COLUMNA DERECHA: detalle de la venta seleccionada
        VBox colDer = new VBox(10);
        HBox.setHgrow(colDer, Priority.ALWAYS);
        colDer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; " +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

        Label lblDetalle = new Label("Selecciona una venta para ver el detalle");
        lblDetalle.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 13px;");

        Label lblInfoVenta = new Label("");
        lblInfoVenta.setStyle("-fx-text-fill: #091e4e; -fx-font-size: 11px;");

        TableView<DevolucionLinea> tablaLineas = new TableView<>();
        tablaLineas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tablaLineas, Priority.ALWAYS);

        TableColumn<DevolucionLinea, String>  cProd  = new TableColumn<>("Producto");
        cProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));

        TableColumn<DevolucionLinea, String>  cVend  = new TableColumn<>("Vendido");
        cVend.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getVendido())));
        cVend.setPrefWidth(65);

        TableColumn<DevolucionLinea, String>  cDevuelto = new TableColumn<>("Devuelto");
        cDevuelto.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getYaDevuelto())));
        cDevuelto.setPrefWidth(65);
        cDevuelto.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle(Integer.parseInt(v) > 0 ? "-fx-text-fill: #C0392B; -fx-font-weight: bold;" : "");
            }
        });

        // Columna editable: cantidad a devolver
        TableColumn<DevolucionLinea, String> cCant = new TableColumn<>("A devolver");
        cCant.setPrefWidth(90);
        Map<Integer, Spinner<Integer>> spinners = new LinkedHashMap<>();

        cCant.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                DevolucionLinea linea = (DevolucionLinea) getTableRow().getItem();
                if (linea.getDisponible() <= 0) {
                    Label agotado = new Label("Agotado");
                    agotado.setStyle("-fx-text-fill: #C0392B; -fx-font-size: 10px;");
                    setGraphic(agotado);
                    return;
                }
                Spinner<Integer> sp = spinners.computeIfAbsent(linea.getIdDetalle(),
                        k -> new Spinner<>(0, linea.getDisponible(), 0));
                sp.setPrefWidth(70);
                setGraphic(sp);
            }
        });

        tablaLineas.getColumns().addAll(cProd, cVend, cDevuelto, cCant);

        // Tipo de reembolso y botón confirmar
        HBox filaTipo = new HBox(10);
        filaTipo.setAlignment(Pos.CENTER_LEFT);
        Label lblTipo = new Label("Reembolso:");
        lblTipo.setStyle("-fx-text-fill: #091e4e; -fx-font-size: 12px;");
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("EFECTIVO", "NOTA_CREDITO");
        cmbTipo.setValue("EFECTIVO");
        filaTipo.getChildren().addAll(lblTipo, cmbTipo);

        Button btnConfirmar = new Button("Confirmar devolución");
        btnConfirmar.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-padding: 10 20; -fx-cursor: hand;");
        btnConfirmar.setMaxWidth(Double.MAX_VALUE);
        btnConfirmar.setDisable(true);

        colDer.getChildren().addAll(lblDetalle, lblInfoVenta, tablaLineas, filaTipo, btnConfirmar);

        // ── Listener: al seleccionar venta
        final int[] idVentaSeleccionada = {-1};

        tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo == null) return;
            idVentaSeleccionada[0] = (int) nuevo.get("id_venta");
            spinners.clear();

            List<DevolucionLinea> lineas = dao.obtenerLineasDisponibles(idVentaSeleccionada[0]);
            tablaLineas.getItems().setAll(lineas);
            tablaLineas.refresh();

            String estado = (String) nuevo.get("estado");
            lblInfoVenta.setText(
                    "Folio #" + String.format("%04d", idVentaSeleccionada[0]) +
                            "  ·  " + nuevo.get("fecha") +
                            "  ·  " + nuevo.get("cliente") +
                            "  ·  Total: $" + String.format("%.2f", (double) nuevo.get("total")) +
                            "  ·  " + traducirEstado(estado)
            );
            btnConfirmar.setDisable(lineas.isEmpty());
        });

        // ── Confirmar devolución
        btnConfirmar.setOnAction(e -> {
            if (idVentaSeleccionada[0] < 0) return;

            Map<Integer, Integer> seleccion = new LinkedHashMap<>();
            for (Map.Entry<Integer, Spinner<Integer>> entry : spinners.entrySet()) {
                int cant = entry.getValue().getValue();
                if (cant > 0) seleccion.put(entry.getKey(), cant);
            }

            if (seleccion.isEmpty()) {
                mostrarAlerta("Sin selección", "Elige al menos un producto a devolver.");
                return;
            }

            // Calcular monto visual para confirmación
            double montoVisual = tablaLineas.getItems().stream()
                    .filter(l -> seleccion.containsKey(l.getIdDetalle()))
                    .mapToDouble(l -> l.getPrecioUnitario() * seleccion.get(l.getIdDetalle()))
                    .sum();

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar devolución");
            confirm.setHeaderText(null);
            confirm.setContentText(String.format(
                    "¿Confirmas la devolución de $%.2f via %s?", montoVisual, cmbTipo.getValue()));
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            try {
                dao.registrarDevolucion(
                        idVentaSeleccionada[0],
                        SesionUsuario.getInstancia().getIdUsuario(),
                        seleccion,
                        cmbTipo.getValue(),
                        ""
                );

                mostrarAlerta("Devolución registrada",
                        String.format("Se procesó correctamente la devolución de $%.2f", montoVisual));

                // Recargar lista
                cargarVentas.run();
                tablaLineas.getItems().clear();
                spinners.clear();
                btnConfirmar.setDisable(true);
                lblInfoVenta.setText("");
                idVentaSeleccionada[0] = -1;

                // Recargar productos en caja
                cargarProductos(txtBuscar.getText(), categoriaSeleccionada);

            } catch (IllegalStateException ex) {
                mostrarAlerta("Devolución no permitida", ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error", "No se pudo registrar la devolución: " + ex.getMessage());
            }
        });

        contenido.getChildren().addAll(colIzq, colDer);
        root.getChildren().add(contenido);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        return root;
    }

    private String traducirEstado(String estado) {
        if (estado == null) return "Completada";
        return switch (estado) {
            case "COMPLETADA"            -> "Completada";
            case "PARCIALMENTE_DEVUELTA" -> "Parcial";
            case "DEVUELTA"              -> "Devuelta";
            default -> estado;
        };
    }

    // RESTO DEL CÓDIGO ORIGINAL (sin cambios)


    private void cargarFolio() {
        String sql = "SELECT COUNT(*) + 1 FROM ventas";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) lblFolio.setText(String.format("Folio #%04d", rs.getInt(1)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarCategorias() {
        panelCategorias.getChildren().clear();
        panelCategorias.getChildren().add(crearBotonCategoria("Todas", true));
        String sql = "SELECT nombre FROM categorias";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) panelCategorias.getChildren().add(crearBotonCategoria(rs.getString("nombre"), false));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Button crearBotonCategoria(String nombre, boolean activo) {
        Button btn = new Button(nombre);
        actualizarEstiloCategoria(btn, activo);
        btn.setOnAction(e -> {
            categoriaSeleccionada = nombre;
            panelCategorias.getChildren().forEach(node -> {
                if (node instanceof Button b) actualizarEstiloCategoria(b, b.getText().equals(nombre));
            });
            cargarProductos(txtBuscar.getText(), nombre);
        });
        return btn;
    }

    private void actualizarEstiloCategoria(Button btn, boolean activo) {
        if (activo) btn.setStyle("-fx-background-color: #091e4e; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold;");
        else        btn.setStyle("-fx-background-color: white; -fx-text-fill: #091e4e; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand; -fx-font-size: 12px; -fx-border-color: #091e4e; -fx-border-radius: 20; -fx-border-width: 1;");
    }

    private void cargarProductos(String filtro, String categoria) {
        listaProductos.getChildren().clear();
        String sql = (categoria == null || categoria.equals("Todas"))
                ? "SELECT p.id_producto, p.nombre, p.codigo_barras, p.precio, p.stock FROM productos p WHERE p.activo = 1 AND (p.nombre LIKE ? OR p.codigo_barras LIKE ?)"
                : "SELECT p.id_producto, p.nombre, p.codigo_barras, p.precio, p.stock FROM productos p JOIN categorias c ON p.id_categoria = c.id_categoria WHERE p.activo = 1 AND (p.nombre LIKE ? OR p.codigo_barras LIKE ?) AND c.nombre = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + (filtro == null ? "" : filtro) + "%";
            ps.setString(1, patron); ps.setString(2, patron);
            if (categoria != null && !categoria.equals("Todas")) ps.setString(3, categoria);
            ResultSet rs = ps.executeQuery();
            primerProducto = null;
            while (rs.next()) {
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                String codigo = rs.getString("codigo_barras");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");
                HBox card = new HBox(12);
                card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12 16; -fx-effect: dropshadow(gaussian, #A0856A, 6, 0, 0, 1);");
                if (primerProducto == null && stock > 0) { primerProducto = card; card.setStyle(card.getStyle() + "; -fx-border-color: #091e4e; -fx-border-width: 2;"); }
                String colorStock = stock == 0 ? "#C0392B" : stock <= 5 ? "#E67E22" : "#27AE60";
                String textoStock = stock == 0 ? "Sin stock" : "Stock: " + stock;
                javafx.scene.layout.StackPane indicador = new javafx.scene.layout.StackPane();
                indicador.setMinWidth(4); indicador.setMaxWidth(4); indicador.setMinHeight(40);
                indicador.setStyle("-fx-background-color: " + colorStock + "; -fx-background-radius: 2;");
                VBox info = new VBox(3); HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
                Label lblNombre = new Label(nombre); lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #091e4e; -fx-font-size: 13px;");
                HBox filaBaja = new HBox(8); filaBaja.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                Label lblPrecio = new Label("$" + String.format("%.2f", precio)); lblPrecio.setStyle("-fx-font-weight: bold; -fx-text-fill: #091e4e; -fx-font-size: 14px;");
                Label lblStockLabel = new Label(textoStock); lblStockLabel.setStyle("-fx-text-fill: " + colorStock + "; -fx-font-size: 11px; -fx-background-color: " + colorStock + "22; -fx-background-radius: 6; -fx-padding: 2 8;");
                filaBaja.getChildren().addAll(lblPrecio, lblStockLabel);
                if (codigo != null && !codigo.isBlank()) { Label lblCodigo = new Label("↳ " + codigo); lblCodigo.setStyle("-fx-text-fill: #A0856A; -fx-font-size: 10px;"); info.getChildren().addAll(lblNombre, filaBaja, lblCodigo); }
                else info.getChildren().addAll(lblNombre, filaBaja);
                Button btnAgregar = new Button("+");
                btnAgregar.setStyle("-fx-background-color: #091e4e; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36; -fx-cursor: hand; -fx-padding: 0; -fx-alignment: center;");
                if (stock > 0) { btnAgregar.setOnAction(e -> agregarAlCarrito(id, nombre, precio, stock)); card.setOnMouseClicked(e -> agregarAlCarrito(id, nombre, precio, stock)); }
                else { btnAgregar.setDisable(true); card.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 12; -fx-padding: 12 16; -fx-opacity: 0.6;"); }
                card.getChildren().addAll(indicador, info, btnAgregar);
                listaProductos.getChildren().add(card);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean agregarPorCodigoExacto(String codigo) {
        String sql = "SELECT id_producto, nombre, precio, stock FROM productos WHERE activo = 1 AND codigo_barras = ?";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id_producto"); String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio"); int stock = rs.getInt("stock");
                if (stock <= 0) { mostrarAlerta("Sin stock", "\"" + nombre + "\" no tiene unidades disponibles."); return true; }
                agregarAlCarrito(id, nombre, precio, stock); return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private int obtenerStock(int idProducto) {
        String sql = "SELECT stock FROM productos WHERE id_producto = ?";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stock");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private void agregarAlCarrito(int id, String nombre, double precio, int stock) {
        if (carrito.containsKey(id)) {
            Object[] item = carrito.get(id);
            int cantidad = (int) item[2];
            if (cantidad >= stock) { mostrarAlerta("Sin stock", "No hay mas unidades disponibles."); return; }
            item[2] = cantidad + 1;
        } else { carrito.put(id, new Object[]{nombre, precio, 1}); }
        actualizarCarrito();
        Platform.runLater(() -> { txtBuscar.clear(); txtBuscar.requestFocus(); });
    }

    private void actualizarCarrito() {
        listaCarrito.getChildren().clear();
        total = 0; int totalItems = 0;
        for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
            int id = entry.getKey(); Object[] item = entry.getValue();
            String nombre = (String) item[0]; double precio = (double) item[1]; int cantidad = (int) item[2];
            double subtotal = precio * cantidad; total += subtotal; totalItems += cantidad;
            HBox fila = new HBox(8); fila.getStyleClass().add("carrito-item"); fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label lblNombre = new Label(nombre); lblNombre.setStyle("-fx-text-fill: #091e4e; -fx-font-size: 12px;"); lblNombre.setMaxWidth(130);
            Region spacer = new Region(); HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            Button btnMenos = new Button("-"); btnMenos.setStyle("-fx-background-color: #091e4e; -fx-text-fill: white; -fx-background-radius: 4; -fx-min-width: 24; -fx-min-height: 24; -fx-cursor: hand;");
            btnMenos.setOnAction(e -> {
                if (!autorizarAccionRestringida("Quitar producto del carrito")) return;
                if (cantidad <= 1) carrito.remove(id); else item[2] = cantidad - 1;
                actualizarCarrito();
            });
            TextField tfCantidad = new TextField(String.valueOf(cantidad)); tfCantidad.setStyle("-fx-text-fill: #091e4e; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: center; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #0052cc; -fx-border-width: 1; -fx-pref-width: 50; -fx-max-width: 50;");
            Runnable validarYActualizar = () -> { try { int nueva = Integer.parseInt(tfCantidad.getText().trim()); if (nueva < cantidad && !autorizarAccionRestringida("Reducir cantidad en carrito")) { tfCantidad.setText(String.valueOf(cantidad)); return; } if (nueva <= 0) carrito.remove(id); else { int stockReal = obtenerStock(id); if (nueva > stockReal) { mostrarAlerta("Stock insuficiente", "Solo hay " + stockReal + " unidades disponibles."); item[2] = stockReal; } else item[2] = nueva; } actualizarCarrito(); } catch (NumberFormatException ex) { tfCantidad.setText(String.valueOf(cantidad)); } };
            tfCantidad.setOnAction(e -> validarYActualizar.run()); tfCantidad.focusedProperty().addListener((obs, o, n) -> { if (!n) validarYActualizar.run(); });
            Button btnMas = new Button("+"); btnMas.setStyle("-fx-background-color: #091e4e; -fx-text-fill: white; -fx-background-radius: 4; -fx-min-width: 24; -fx-min-height: 24; -fx-cursor: hand;");
            btnMas.setOnAction(e -> { int stockReal = obtenerStock(id); if (cantidad >= stockReal) { mostrarAlerta("Sin stock", "No hay mas unidades disponibles."); return; } item[2] = cantidad + 1; actualizarCarrito(); });
            Label lblSub = new Label("$" + String.format("%.2f", subtotal)); lblSub.setStyle("-fx-text-fill: #091e4e; -fx-font-weight: bold; -fx-font-size: 12px; -fx-min-width: 60; -fx-alignment: CENTER_RIGHT;");
            fila.getChildren().addAll(lblNombre, spacer, btnMenos, tfCantidad, btnMas, lblSub);
            listaCarrito.getChildren().add(fila);
        }
        double iva = total * 0.16; double subtotalSinIva = total - iva;
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
        VBox contenido = new VBox(10); contenido.setStyle("-fx-padding: 16; -fx-min-width: 400;");
        TextField txtBuscarCliente = new TextField(); txtBuscarCliente.setPromptText("Buscar cliente...");
        txtBuscarCliente.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #091e4e; -fx-padding: 8;");
        ListView<String> listaClientes = new ListView<>(); listaClientes.setPrefHeight(250);
        List<int[]> idsClientes = new ArrayList<>(); List<double[]> creditosClientes = new ArrayList<>();
        Runnable cargarLista = () -> {
            listaClientes.getItems().clear(); idsClientes.clear(); creditosClientes.clear();
            listaClientes.getItems().add("Publico General"); idsClientes.add(new int[]{1}); creditosClientes.add(new double[]{0, 0});
            String sql = "SELECT id_cliente, nombre, limite_credito, saldo_actual FROM clientes WHERE activo = 1 AND nombre != 'Publico General' AND nombre LIKE ?";
            try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, "%" + txtBuscarCliente.getText().trim() + "%"); ResultSet rs = ps.executeQuery();
                while (rs.next()) { double limite = rs.getDouble("limite_credito"); double saldo = rs.getDouble("saldo_actual"); double disponible = limite - saldo; String texto = rs.getString("nombre") + (limite > 0 ? " — Disponible: $" + String.format("%.2f", disponible) : ""); listaClientes.getItems().add(texto); idsClientes.add(new int[]{rs.getInt("id_cliente")}); creditosClientes.add(new double[]{limite, saldo}); }
            } catch (Exception e) { e.printStackTrace(); }
        };
        cargarLista.run(); txtBuscarCliente.textProperty().addListener((obs, old, nuevo) -> cargarLista.run());
        contenido.getChildren().addAll(txtBuscarCliente, listaClientes); dialog.getDialogPane().setContent(contenido);
        dialog.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                int index = listaClientes.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    idClienteSeleccionado = idsClientes.get(index)[0]; limiteCredito = creditosClientes.get(index)[0]; saldoCliente = creditosClientes.get(index)[1];
                    String item = listaClientes.getItems().get(index); nombreClienteSeleccionado = item.contains(" — ") ? item.split(" — ")[0] : item;
                    lblClienteSeleccionado.setText(nombreClienteSeleccionado);
                    if (limiteCredito > 0) { double disponible = limiteCredito - saldoCliente; lblCreditoDisponible.setText("Credito disponible: $" + String.format("%.2f", disponible)); lblCreditoDisponible.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (disponible <= 0 ? "#C0392B" : "#3B6D11") + ";"); }
                    else lblCreditoDisponible.setText("");
                }
            }
        });
    }

    @FXML
    public void handleCobrar() {
        if (carrito.isEmpty()) { mostrarAlerta("Carrito vacío", "Agrega productos antes de cobrar."); return; }
        if (limiteCredito > 0) { double disponible = limiteCredito - saldoCliente; if (total > disponible) { mostrarAlerta("Credito insuficiente", nombreClienteSeleccionado + " solo tiene $" + String.format("%.2f", disponible) + " de credito disponible."); return; } }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Pago.fxml"));
            Parent root = loader.load();
            PagoController pagoController = loader.getController();
            pagoController.setDatos(total, carrito, this, idClienteSeleccionado, nombreClienteSeleccionado, limiteCredito, saldoCliente);
            Stage stagePago = new Stage();
            stagePago.setTitle("Cobro");
            stagePago.setScene(new Scene(root));
            stagePago.setResizable(false);
            stagePago.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stagePago.initOwner(btnCobrar.getScene().getWindow());
            stagePago.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleCancelar() {
        if (carrito.isEmpty()) return;
        if (!autorizarAccionRestringida("Cancelar venta en curso")) return;
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION); alerta.setTitle("Cancelar venta"); alerta.setHeaderText(null); alerta.setContentText("¿Seguro que deseas cancelar la venta?");
        alerta.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { carrito.clear(); actualizarCarrito(); } });
    }

    @FXML
    public void abrirHistorial() {
        Stage stage = new Stage(); stage.setTitle("Historial de ventas del día");
        TableView<Map<String, Object>> tabla = new TableView<>(); tabla.setPrefWidth(780);
        TableColumn<Map<String, Object>, String> colFolio = new TableColumn<>("Folio"); colFolio.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("#%04d", (int) d.getValue().get("folio")))); colFolio.setPrefWidth(80);
        TableColumn<Map<String, Object>, String> colHora  = new TableColumn<>("Hora");  colHora.setCellValueFactory(d  -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("hora")));  colHora.setPrefWidth(120);
        TableColumn<Map<String, Object>, String> colCliente = new TableColumn<>("Cliente"); colCliente.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("cliente"))); colCliente.setPrefWidth(180);
        TableColumn<Map<String, Object>, String> colCajero = new TableColumn<>("Cajero"); colCajero.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("cajero"))); colCajero.setPrefWidth(160);
        TableColumn<Map<String, Object>, String> colTotal  = new TableColumn<>("Total");  colTotal.setCellValueFactory(d  -> new javafx.beans.property.SimpleStringProperty("$" + String.format("%.2f", (double) d.getValue().get("total")))); colTotal.setPrefWidth(100);
        tabla.getColumns().addAll(colFolio, colHora, colCliente, colCajero, colTotal);
        String sql = "SELECT v.id_venta, DATE_FORMAT(v.fecha,'%H:%i:%s') AS hora, u.nombre AS cajero, COALESCE(c.nombre,'Publico General') AS cliente, v.total FROM ventas v JOIN usuarios u ON v.id_usuario = u.id_usuario LEFT JOIN clientes c ON v.id_cliente = c.id_cliente WHERE DATE(v.fecha) = CURDATE() ORDER BY v.fecha DESC";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { Map<String, Object> fila = new HashMap<>(); fila.put("folio", rs.getInt("id_venta")); fila.put("hora", rs.getString("hora")); fila.put("cajero", rs.getString("cajero")); fila.put("cliente", rs.getString("cliente")); fila.put("total", rs.getDouble("total")); tabla.getItems().add(fila); }
        } catch (Exception e) { e.printStackTrace(); }
        tabla.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && tabla.getSelectionModel().getSelectedItem() != null) mostrarDetalleVenta((int) tabla.getSelectionModel().getSelectedItem().get("folio")); });
        Label hint = new Label("Doble clic para ver detalle"); hint.setStyle("-fx-text-fill: #000000; -fx-font-size: 11px;");
        VBox layout = new VBox(10, tabla, hint); layout.setStyle("-fx-padding: 16; -fx-background-color: #F5EFE6;");
        stage.setScene(new Scene(layout, 800, 480)); stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); stage.show();
    }

    private void mostrarDetalleVenta(int idVenta) {
        Stage stage = new Stage();
        stage.setTitle("Detalle venta #" + String.format("%04d", idVenta));

        TableView<Map<String, Object>> tabla = new TableView<>();
        tabla.setPrefWidth(480);

        TableColumn<Map<String, Object>, String> colProd   = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("producto")));
        colProd.setPrefWidth(200);

        TableColumn<Map<String, Object>, String> colCant   = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf((int) d.getValue().get("cantidad"))));
        colCant.setPrefWidth(70);

        TableColumn<Map<String, Object>, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("$" + String.format("%.2f", (double) d.getValue().get("precio"))));
        colPrecio.setPrefWidth(100);

        TableColumn<Map<String, Object>, String> colSub    = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("$" + String.format("%.2f", (double) d.getValue().get("subtotal"))));
        colSub.setPrefWidth(100);

        tabla.getColumns().addAll(colProd, colCant, colPrecio, colSub);

        String sql = "SELECT p.nombre AS producto, dv.cantidad, dv.precio_unitario AS precio, " +
                "(dv.cantidad * dv.precio_unitario) AS subtotal " +
                "FROM detalle_venta dv " +
                "JOIN productos p ON dv.id_producto = p.id_producto " +
                "WHERE dv.id_venta = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("producto", rs.getString("producto"));
                fila.put("cantidad", rs.getInt("cantidad"));
                fila.put("precio",   rs.getDouble("precio"));
                fila.put("subtotal", rs.getDouble("subtotal"));
                tabla.getItems().add(fila);
            }
        } catch (Exception e) { e.printStackTrace(); }

        // ── Botón cancelar venta (solo visible si tiene permiso) ──────────────
        Button btnCancelar = new Button("Cancelar esta venta");
        btnCancelar.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand; " +
                "-fx-font-weight: bold;");
        btnCancelar.setMaxWidth(Double.MAX_VALUE);
        btnCancelar.setOnAction(e -> manejarCancelacion(idVenta, stage::close));

        VBox layout = new VBox(10, tabla, btnCancelar);
        layout.setStyle("-fx-padding: 16; -fx-background-color: #F5EFE6;");
        stage.setScene(new Scene(layout, 520, 400));
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.show();
    }
    private void manejarCancelacion(int idVenta, Runnable onExito) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Cancelar venta");
        dialog.setHeaderText("Cancelar " + org.example.servicio.FolioService.venta(idVenta));
        dialog.setContentText("Motivo de cancelación (obligatorio):");

        dialog.showAndWait().ifPresent(motivo -> {
            if (motivo.trim().isEmpty()) {
                mostrarAlerta("Motivo requerido", "Debes ingresar un motivo para cancelar.");
                return;
            }
            try {
                new org.example.servicio.CancelacionService().cancelarVenta(idVenta, motivo.trim());
                mostrarAlerta("Venta cancelada",
                        org.example.servicio.FolioService.venta(idVenta) +
                                " cancelada correctamente.\nEl stock fue restaurado.");
                if (onExito != null) onExito.run();
                cargarProductos(txtBuscar.getText(), categoriaSeleccionada);
            } catch (IllegalStateException e) {
                mostrarAlerta("No permitido", e.getMessage());
            } catch (SecurityException e) {
                mostrarAlerta("Acceso denegado", e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo cancelar: " + e.getMessage());
            }
        });
    }

    @FXML public void abrirIngreso() { abrirMovimientoCaja("INGRESO"); }
    @FXML public void abrirSalida()  { abrirMovimientoCaja("RETIRO"); }

    private void abrirMovimientoCaja(String tipo) {
        boolean esIngreso = tipo.equals("INGRESO"); Stage stage = new Stage();
        stage.setTitle(esIngreso ? "Registrar ingreso" : "Registrar salida");
        Label lblMonto = new Label("Monto:"); lblMonto.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000;");
        TextField txtMonto = new TextField(); txtMonto.setPromptText("$0.00"); txtMonto.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #000000; -fx-border-width: 1; -fx-padding: 8;");
        Label lblMotivo = new Label("Motivo: *"); lblMotivo.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000;");
        TextField txtMotivo = new TextField(); txtMotivo.setPromptText("Escribe el motivo..."); txtMotivo.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #000000; -fx-border-width: 1; -fx-padding: 8;");
        Button btnGuardar = new Button(esIngreso ? "Registrar ingreso" : "Registrar salida");
        btnGuardar.setStyle("-fx-background-color: " + (esIngreso ? "#2E7D32" : "#C0392B") + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand; -fx-font-weight: bold;"); btnGuardar.setPrefWidth(260);
        btnGuardar.setOnAction(e -> {
            String motivoTxt = txtMotivo.getText().trim(); String montoTxt = txtMonto.getText().trim();
            if (motivoTxt.isEmpty()) { mostrarAlerta("Campo requerido", "El motivo es obligatorio."); return; }
            double monto; try { monto = Double.parseDouble(montoTxt.replace(",", ".").replace("$", "")); if (monto <= 0) throw new NumberFormatException(); } catch (NumberFormatException ex) { mostrarAlerta("Monto inválido", "Ingresa un monto válido mayor a 0."); return; }
            String sql = "INSERT INTO movimientos_caja (id_caja, tipo, monto, motivo, id_usuario) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, SesionUsuario.getInstancia().getIdCaja()); ps.setString(2, tipo); ps.setDouble(3, monto); ps.setString(4, motivoTxt); ps.setInt(5, SesionUsuario.getInstancia().getIdUsuario()); ps.executeUpdate();
                stage.close(); mostrarAlerta("Listo", tipo.equals("INGRESO") ? "Ingreso de $" + String.format("%.2f", monto) + " registrado." : "Salida de $" + String.format("%.2f", monto) + " registrada.");
            } catch (Exception ex) { ex.printStackTrace(); mostrarAlerta("Error", "No se pudo registrar el movimiento."); }
        });
        VBox layout = new VBox(12, lblMonto, txtMonto, lblMotivo, txtMotivo, btnGuardar); layout.setStyle("-fx-padding: 24; -fx-background-color: #F5EFE6;");
        stage.setScene(new Scene(layout, 300, 260)); stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); stage.setResizable(false); stage.show(); txtMonto.requestFocus();
    }

    public void ventaCompletada() {
        carrito.clear(); actualizarCarrito(); cargarProductos("", "Todas"); cargarFolio();
        idClienteSeleccionado = 1; nombreClienteSeleccionado = "Publico General"; limiteCredito = 0; saldoCliente = 0;
        lblClienteSeleccionado.setText("Publico General"); lblCreditoDisponible.setText("");
        mostrarAlerta("Venta completada", "La venta se registro correctamente.");
    }
    private void registrarLogout() {
        String sql = "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, detalle) " +
                "VALUES (?, 'LOGOUT', 'usuarios', ?, ?)";
        try (java.sql.Connection con = org.example.dao.ConexionDB.getConexion();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            int idUsuario = org.example.modelo.SesionUsuario.getInstancia().getIdUsuario();
            String nombre = org.example.modelo.SesionUsuario.getInstancia().getNombre();
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setString(3, "Cierre de sesión: " + nombre);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void irADashboard()  { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_REPORTES, "/org/example/vista/MenuPrincipal.fxml"); }
    @FXML public void irAInventario(ActionEvent event) { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_INVENTARIO, "/org/example/vista/Inventario.fxml"); }
    @FXML public void irAReportes(ActionEvent event)   { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_REPORTES, "/org/example/vista/Reportes.fxml"); }
    @FXML public void irAEmpleados()  { if (!SesionUsuario.getInstancia().getRol().equals("admin")) { mostrarAlerta("Acceso Denegado","Solo el Administrador"); return; } cambiarEscena("/org/example/vista/Empleados.fxml"); }
    @FXML private void irAConfiguracion() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_CONFIGURACION, "/org/example/vista/Configuracion.fxml"); }
    @FXML private void irACorteCaja()     { cambiarEscena("/org/example/vista/CorteCaja.fxml"); }
    @FXML private void irAAuditoria() {
        navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_AUDITORIA, "/org/example/vista/Auditoria.fxml");
    }
    @FXML private void irAClientes()      { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_CLIENTES, "/org/example/vista/Clientes.fxml"); }

    @FXML
    public void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Cambiar sesion"); a.setHeaderText(null);
        a.setContentText("Seguro que deseas cambiar de sesion?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                registrarLogout();
                org.example.modelo.SesionUsuario.cerrarSesion();
                cambiarEscena("/org/example/vista/Login.fxml");
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }

    private boolean autorizarAccionRestringida(String accion) {
        return org.example.servicio.PermisoService.requerirPermisoOAutorizacionAdmin(
                org.example.servicio.PermisoService.VENTAS_ELIMINAR_PRODUCTO_CARRITO,
                accion
        );
    }

    private void navegarConPermiso(org.example.servicio.PermisoService.Accion accion, String ruta) {
        if (!org.example.servicio.PermisoService.puede(accion)) {
            mostrarAlerta("Acceso denegado", "El cajero solo puede acceder al modulo de ventas.");
            return;
        }
        cambiarEscena(ruta);
    }

    private void cambiarEscena(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            MarcaService.aplicar(root);
            ((Stage) lblTotal.getScene().getWindow()).getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
