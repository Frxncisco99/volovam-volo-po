package org.example.controlador;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.CategoriaDAO;
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;
import org.example.modelo.SesionUsuario;
import org.example.servicio.ExportarInventarioservice;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class InventarioController {

    // ── Tabla ──────────────────────────────────────────────────────────────────
    @FXML private TableView<Producto>        tablaProductos;

    @FXML private TableColumn<Producto, String>  colSku;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String>  colNombre;
    @FXML private TableColumn<Producto, Double>  colPrecio;
    @FXML private TableColumn<Producto, String>  colCategoria;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String>  colEstado;
    @FXML private TableColumn<Producto, Void>    colAcciones;

    // ── Cards métricas ─────────────────────────────────────────────────────────
    @FXML private Label lblTotalProductos;
    @FXML private Label lblStockBajo;
    @FXML private Label lblAgotados;           // NUEVA
    @FXML private Label lblValorTotal;
    @FXML private Label lblCategoriaDominante; // NUEVA
    @FXML private Label lblCategoriaDominanteCount; // NUEVA

    // ── Filtros ────────────────────────────────────────────────────────────────
    @FXML private TextField        txtBuscar;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbEstado;

    // ── Paginación ─────────────────────────────────────────────────────────────
    @FXML private Label               lblConteo;
    @FXML private Label               lblPagina;
    @FXML private ComboBox<Integer>   cbPorPagina;
    @FXML private Button              btnPrimera;
    @FXML private Button              btnAnterior;
    @FXML private Button              btnSiguiente;
    @FXML private Button              btnUltima;

    // ── Toast ──────────────────────────────────────────────────────────────────
    @FXML private HBox    toastBox;
    @FXML private Label   lblToast;
    @FXML private FontIcon toastIcon;

    // ── Sidebar ────────────────────────────────────────────────────────────────
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;
    @FXML private Label lblHora;

    // ── Estado interno ─────────────────────────────────────────────────────────
    private ObservableList<Producto>  listaProductos  = FXCollections.observableArrayList();
    private FilteredList<Producto>    filtro;

    /** Lista filtrada + ordenada (sin paginación). Fuente de verdad para la paginación. */
    private SortedList<Producto>      listaOrdenada;

    /** Lo que realmente ve la tabla (subconjunto paginado). */
    private final ObservableList<Producto> paginaActual = FXCollections.observableArrayList();

    private int paginaIdx    = 0;   // 0-based
    private int porPagina    = 15;  // valor por defecto

    private final ProductoDAO dao  = new ProductoDAO();

    private final ChangeListener<String>  filtroCategoriaListener = (o, a, b) -> { paginaIdx = 0; aplicarFiltros(); };
    private final ChangeListener<String>  filtroEstadoListener    = (o, a, b) -> { paginaIdx = 0; aplicarFiltros(); };

    // ── Caché de imágenes (ruta → Image) para no recargar en cada scroll ──────


    // ────────────────────────────────────────────────────────────────────────────
    //  INITIALIZE
    // ────────────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {

        // Reloj
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> lblHora.setText(LocalDateTime.now().format(fmt))));
        reloj.setCycleCount(Animation.INDEFINITE);
        reloj.play();

        // Sesión
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String ini = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(ini);

        // ComboBox por página
        cbPorPagina.getItems().addAll(10, 15, 25, 50);
        cbPorPagina.setValue(porPagina);
        cbPorPagina.valueProperty().addListener((o, a, b) -> {
            if (b != null) { porPagina = b; paginaIdx = 0; refrescarPagina(); }
        });

        configurarColumnas();
        cargarProductos();
        cargarFiltros();

        txtBuscar.textProperty().addListener((o, a, b) -> { paginaIdx = 0; aplicarFiltros(); });
        cbCategoria.valueProperty().addListener(filtroCategoriaListener);
        cbEstado.valueProperty().addListener(filtroEstadoListener);
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  CONFIGURAR COLUMNAS
    // ────────────────────────────────────────────────────────────────────────────
    private void configurarColumnas() {



        // ── SKU ───────────────────────────────────────────────────────────────
        colSku.setCellValueFactory(c -> {
            Producto p = c.getValue();
            // Prioridad: código de barras real → SKU generado
            if (p.getCodigoBarras() != null && !p.getCodigoBarras().isBlank()) {
                return new SimpleStringProperty(p.getCodigoBarras());
            }
            String cat = p.getCategoria() != null && p.getCategoria().length() >= 3
                    ? p.getCategoria().substring(0, 3).toUpperCase()
                    : "VOL";
            return new SimpleStringProperty(cat + "-" + String.format("%04d", p.getIdProducto()));
        });
        colSku.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String sku, boolean empty) {
                super.updateItem(sku, empty);
                setAlignment(Pos.CENTER);
                if (empty || sku == null) { setText(null); setStyle(""); return; }
                setText(sku);
                setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 11px; " +
                        "-fx-text-fill: #1a6fa8; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        // ── ID ────────────────────────────────────────────────────────────────
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                setAlignment(Pos.CENTER);
                if (empty || id == null) { setText(null); setStyle(""); return; }
                setText("#" + String.format("%03d", id));
                setStyle("-fx-text-fill: #9ab8d4; -fx-font-size: 11px; -fx-alignment: CENTER;");
            }
        });

        // ── Nombre ────────────────────────────────────────────────────────────
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String nombre, boolean empty) {
                super.updateItem(nombre, empty);
                setAlignment(Pos.CENTER_LEFT);
                if (empty || nombre == null) { setText(null); setStyle(""); return; }
                setText(nombre);
                setStyle("-fx-text-fill: #0d3d5e; -fx-font-weight: bold; " +
                        "-fx-font-size: 12px; -fx-padding: 0 6;");
            }
        });

        // ── Categoría (badge) ─────────────────────────────────────────────────
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCategoria.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.setStyle("-fx-background-color: #e8f3fb; -fx-text-fill: #1a6fa8; " +
                        "-fx-background-radius: 20; -fx-padding: 3 10; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; " +
                        "-fx-effect: dropshadow(gaussian, #1a6fa818, 4, 0, 0, 1);");
            }
            @Override protected void updateItem(String cat, boolean empty) {
                super.updateItem(cat, empty);
                setAlignment(Pos.CENTER);
                if (empty || cat == null) { setGraphic(null); return; }
                badge.setText(cat); setGraphic(badge);
            }
        });

        // ── Precio ────────────────────────────────────────────────────────────
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                setAlignment(Pos.CENTER);
                if (empty || precio == null) { setText(null); setStyle(""); return; }
                setText(String.format("$%.2f", precio));
                setStyle("-fx-text-fill: #0d3d5e; -fx-font-weight: bold; " +
                        "-fx-font-size: 12px; -fx-alignment: CENTER;");
            }
        });

        // ── Stock con barra visual ────────────────────────────────────────────
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar barra  = new ProgressBar(0);
            private final Label       numero = new Label();
            private final VBox        caja   = new VBox(2, numero, barra);
            {
                barra.setPrefWidth(70); barra.setPrefHeight(5);
                numero.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                caja.setAlignment(Pos.CENTER);
            }
            @Override protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                setAlignment(Pos.CENTER);
                if (empty || stock == null) { setGraphic(null); return; }

                Producto p = getTableView().getItems().get(getIndex());
                int minimo = p.getStockMinimo() > 0 ? p.getStockMinimo() : 5;
                double nivel = minimo > 0 ? Math.min(1.0, (double) stock / (minimo * 3)) : 1.0;

                String colorBarra, colorTexto;
                if (stock == 0)            { colorBarra = "#e05252"; colorTexto = "#a83232"; }
                else if (p.isBajoStock())  { colorBarra = "#f0a830"; colorTexto = "#9a6010"; }
                else                       { colorBarra = "#3aaa6e"; colorTexto = "#1a5e3e"; }

                numero.setText(String.valueOf(stock));
                numero.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + colorTexto + ";");
                barra.setProgress(nivel);
                barra.setStyle("-fx-accent: " + colorBarra + "; -fx-background-color: #e8f0f8; " +
                        "-fx-background-radius: 4; -fx-border-radius: 4;");
                setGraphic(caja);
            }
        });

        // ── Estado (badge) ────────────────────────────────────────────────────
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getStock() == 0 ? "Agotado"
                                : c.getValue().isBajoStock() ? "Bajo" : "Normal"));
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                setAlignment(Pos.CENTER);
                if (empty || estado == null) { setGraphic(null); return; }
                switch (estado) {
                    case "Agotado" -> {
                        badge.setText("✕ Agotado");
                        badge.setStyle("-fx-background-color: #fde8e8; -fx-text-fill: #a83232; " +
                                "-fx-background-radius: 20; -fx-padding: 4 10; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                    case "Bajo" -> {
                        badge.setText("⚠ Bajo Stock");
                        badge.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #8a6020; " +
                                "-fx-background-radius: 20; -fx-padding: 4 10; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                    default -> {
                        badge.setText("✓ Normal");
                        badge.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #1a5c2e; " +
                                "-fx-background-radius: 20; -fx-padding: 4 10; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                }
                setGraphic(badge);
            }
        });

        // ── Acciones: botón Editar + menú ⋮ ──────────────────────────────────
        colAcciones.setCellFactory(col -> new TableCell<>() {

            private final Button btnEditar = new Button("Editar");
            private final Button btnMenu   = new Button("⋮");
            private final HBox   caja      = new HBox(6, btnEditar, btnMenu);

            {
                btnEditar.setStyle("-fx-background-color: #1a6fa8; -fx-text-fill: white; " +
                        "-fx-background-radius: 7; -fx-padding: 6 14; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
                btnMenu.setStyle("-fx-background-color: transparent; -fx-text-fill: #6a96b8; " +
                        "-fx-border-color: #c8dff0; -fx-border-width: 1; " +
                        "-fx-background-radius: 7; -fx-border-radius: 7; " +
                        "-fx-padding: 5 10; -fx-font-size: 15px; -fx-cursor: hand;");
                caja.setAlignment(Pos.CENTER);

                btnEditar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    abrirFormularioEditar(p);
                });

                btnMenu.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    mostrarMenuContextual(p, btnMenu);
                });

                // Hover sutil en btnEditar
                btnEditar.setOnMouseEntered(e ->
                        btnEditar.setStyle("-fx-background-color: #155d8e; -fx-text-fill: white; " +
                                "-fx-background-radius: 7; -fx-padding: 6 14; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;"));
                btnEditar.setOnMouseExited(e ->
                        btnEditar.setStyle("-fx-background-color: #1a6fa8; -fx-text-fill: white; " +
                                "-fx-background-radius: 7; -fx-padding: 6 14; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;"));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : caja);
            }
        });

        // ── Hover en filas ────────────────────────────────────────────────────
        tablaProductos.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.hoverProperty().addListener((obs, wasHover, isHover) -> {
                if (!row.isEmpty()) {
                    row.setStyle(isHover
                            ? "-fx-background-color: #d6eaf8;"
                            : (row.getIndex() % 2 == 0 ? "-fx-background-color: #f5f9ff;" : "-fx-background-color: white;"));
                }
            });
            row.itemProperty().addListener((obs, old, item) -> {
                if (item == null) row.setStyle("-fx-background-color: transparent;");
                else row.setStyle(row.getIndex() % 2 == 0 ? "-fx-background-color: #f5f9ff;" : "-fx-background-color: white;");
            });
            return row;
        });
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  MENÚ CONTEXTUAL (⋮)
    // ────────────────────────────────────────────────────────────────────────────
    private void mostrarMenuContextual(Producto p, Button anchor) {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, #00000030, 12, 0, 0, 4);");

        MenuItem itemStock     = new MenuItem("⇅  Ajustar stock");
        MenuItem itemHistorial = new MenuItem("📋  Ver historial");
        MenuItem itemEliminar  = new MenuItem("🗑  Eliminar producto");

        itemStock.setStyle("-fx-font-size: 12px;");
        itemHistorial.setStyle("-fx-font-size: 12px;");
        itemEliminar.setStyle("-fx-font-size: 12px; -fx-text-fill: #a83232;");

        itemStock.setOnAction(e -> abrirAjusteStock(p));
        itemHistorial.setOnAction(e -> abrirHistorial(p));
        itemEliminar.setOnAction(e -> confirmarEliminar(p));

        menu.getItems().addAll(itemStock, itemHistorial, new SeparatorMenuItem(), itemEliminar);
        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 4);
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  TOAST NOTIFICATION
    // ────────────────────────────────────────────────────────────────────────────
    private Timeline toastTimeline;

    /**
     * @param tipo  "success" | "error" | "warning" | "info"
     */
    private void toast(String mensaje, String tipo) {
        if (toastTimeline != null) toastTimeline.stop();

        String color, icono;
        switch (tipo) {
            case "error"   -> { color = "#a83232"; icono = "fas-times-circle"; }
            case "warning" -> { color = "#9a6010"; icono = "fas-exclamation-triangle"; }
            case "info"    -> { color = "#1a5c8a"; icono = "fas-info-circle"; }
            default        -> { color = "#1a5e3e"; icono = "fas-check-circle"; }
        }

        toastBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 30; " +
                "-fx-padding: 12 28; -fx-effect: dropshadow(gaussian, #00000050, 16, 0, 0, 4);");
        toastIcon.setIconLiteral(icono);
        lblToast.setText(mensaje);

        toastBox.setOpacity(0);
        toastBox.setVisible(true);
        toastBox.setManaged(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toastBox);
        fadeIn.setToValue(1);
        fadeIn.play();

        toastTimeline = new Timeline(new KeyFrame(Duration.seconds(2.8), e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastBox);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> { toastBox.setVisible(false); toastBox.setManaged(false); });
            fadeOut.play();
        }));
        toastTimeline.play();
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  PAGINACIÓN
    // ────────────────────────────────────────────────────────────────────────────
    private void refrescarPagina() {
        if (listaOrdenada == null) return;

        int total   = listaOrdenada.size();
        int paginas = total == 0 ? 1 : (int) Math.ceil((double) total / porPagina);

        // Guardar el clamp
        paginaIdx = Math.max(0, Math.min(paginaIdx, paginas - 1));

        int desde = paginaIdx * porPagina;
        int hasta = Math.min(desde + porPagina, total);

        paginaActual.setAll(listaOrdenada.subList(desde, hasta));

        lblConteo.setText("Mostrando " + (total == 0 ? 0 : desde + 1) + "–" + hasta + " de " + total + " productos");
        lblPagina.setText("Página " + (paginaIdx + 1) + " de " + paginas);

        btnPrimera.setDisable(paginaIdx == 0);
        btnAnterior.setDisable(paginaIdx == 0);
        btnSiguiente.setDisable(paginaIdx >= paginas - 1);
        btnUltima.setDisable(paginaIdx >= paginas - 1);
    }

    @FXML private void irPrimeraPagina()  { paginaIdx = 0;                                  refrescarPagina(); }
    @FXML private void paginaAnterior()   { if (paginaIdx > 0) paginaIdx--;                 refrescarPagina(); }
    @FXML private void paginaSiguiente()  { paginaIdx++;                                     refrescarPagina(); }
    @FXML private void irUltimaPagina()   {
        if (listaOrdenada != null) {
            paginaIdx = Math.max(0, (int) Math.ceil((double) listaOrdenada.size() / porPagina) - 1);
        }
        refrescarPagina();
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  CARGAR PRODUCTOS
    // ────────────────────────────────────────────────────────────────────────────
    private void cargarProductos() {
        listaProductos.setAll(dao.obtenerProductos());

        if (filtro == null) {
            filtro       = new FilteredList<>(listaProductos, p -> true);
            listaOrdenada = new SortedList<>(filtro);
            listaOrdenada.comparatorProperty().bind(tablaProductos.comparatorProperty());
            tablaProductos.setItems(paginaActual);
        }

        actualizarResumen();
        refrescarPagina();
    }

    private void actualizarResumen() {
        int total   = listaProductos.size();
        int bajo    = 0, agotados = 0;
        double valor = 0;
        Map<String, Long> porCategoria = new LinkedHashMap<>();

        for (Producto p : listaProductos) {
            if (p.getStock() == 0) agotados++;
            else if (p.isBajoStock()) bajo++;
            valor += p.getPrecio() * p.getStock();
            porCategoria.merge(
                    p.getCategoria() != null ? p.getCategoria() : "Sin cat.",
                    1L, Long::sum);
        }

        lblTotalProductos.setText(String.valueOf(total));
        lblStockBajo.setText(String.valueOf(bajo));
        lblAgotados.setText(String.valueOf(agotados));
        lblValorTotal.setText(String.format("$%.2f", valor));

        if (!porCategoria.isEmpty()) {
            Map.Entry<String, Long> top = porCategoria.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).orElseThrow();
            lblCategoriaDominante.setText(top.getKey());
            lblCategoriaDominanteCount.setText(top.getValue() + " productos");
        } else {
            lblCategoriaDominante.setText("—");
            lblCategoriaDominanteCount.setText("Sin datos");
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  FILTROS
    // ────────────────────────────────────────────────────────────────────────────
    private void cargarFiltros() {
        cbCategoria.getItems().clear();
        new CategoriaDAO().obtenerCategorias().forEach(c -> cbCategoria.getItems().add(c.getNombre()));
        cbCategoria.getSelectionModel().clearSelection();
        cbEstado.getItems().setAll("Normal", "Bajo", "Agotado");
        cbEstado.getSelectionModel().clearSelection();
    }

    private void aplicarFiltros() {
        if (filtro == null) return;
        String texto     = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();
        String categoria = cbCategoria.getValue();
        String estado    = cbEstado.getValue();

        filtro.setPredicate(p -> {
            // Búsqueda multi-campo: nombre, categoría, SKU generado, código de barras
            String skuGen = (p.getCategoria() != null && p.getCategoria().length() >= 3
                    ? p.getCategoria().substring(0, 3).toUpperCase()
                    : "VOL") + "-" + String.format("%04d", p.getIdProducto());

            boolean textoOk = texto.isEmpty()
                    || p.getNombre().toLowerCase().contains(texto)
                    || p.getCategoria().toLowerCase().contains(texto)
                    || skuGen.toLowerCase().contains(texto)
                    || (p.getCodigoBarras() != null && p.getCodigoBarras().toLowerCase().contains(texto));

            boolean catOk = categoria == null || p.getCategoria().equalsIgnoreCase(categoria);

            boolean estadoOk = estado == null || switch (estado) {
                case "Agotado" -> p.getStock() == 0;
                case "Bajo"    -> p.isBajoStock() && p.getStock() > 0;
                default        -> !p.isBajoStock() && p.getStock() > 0;
            };

            return textoOk && catOk && estadoOk;
        });

        refrescarPagina();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cbCategoria.valueProperty().removeListener(filtroCategoriaListener);
        cbEstado.valueProperty().removeListener(filtroEstadoListener);
        cbCategoria.getSelectionModel().clearSelection();
        cbEstado.getSelectionModel().clearSelection();
        cbCategoria.valueProperty().addListener(filtroCategoriaListener);
        cbEstado.valueProperty().addListener(filtroEstadoListener);
        paginaIdx = 0;
        aplicarFiltros();
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  ACCIONES DE FILA
    // ────────────────────────────────────────────────────────────────────────────
    @FXML
    private void abrirFormularioNuevo() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/AgregarProducto.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Nuevo Producto");
            stage.showAndWait();
            cargarProductos();
            cargarFiltros();
            toast("Producto agregado correctamente", "success");
        } catch (Exception e) {
            e.printStackTrace();
            toast("Error al abrir formulario", "error");
        }
    }

    private void abrirFormularioEditar(Producto p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/EditarProducto.fxml"));
            Parent root = loader.load();
            EditarProductoController ctrl = loader.getController();
            ctrl.setProducto(p);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Editar Producto — " + p.getNombre());
            stage.showAndWait();
            cargarProductos();
            toast("Producto actualizado", "success");
        } catch (Exception e) {
            e.printStackTrace();
            toast("Error al abrir editor", "error");
        }
    }

    private void abrirAjusteStock(Producto p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/AjustarStock.fxml"));
            Parent root = loader.load();
            AjustarStockController ctrl = loader.getController();
            ctrl.setProducto(p);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Ajustar Stock — " + p.getNombre());
            stage.showAndWait();
            cargarProductos();
            toast("Stock actualizado para " + p.getNombre(), "success");
        } catch (Exception e) {
            e.printStackTrace();
            toast("Error al ajustar stock", "error");
        }
    }

    /**
     * Abre modal de historial de movimientos.
     * Si aún no existe el FXML, muestra un mensaje informativo.
     */
    private void abrirHistorial(Producto p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/HistorialMovimientos.fxml"));
            Parent root = loader.load();
            // Pasar producto al controller si existe
            Object ctrl = loader.getController();
            if (ctrl instanceof HistorialMovimientosController hCtrl) {
                hCtrl.setProducto(p);
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Historial — " + p.getNombre());
            stage.showAndWait();
        } catch (Exception e) {
            // Módulo aún no implementado; mostrar toast informativo
            toast("Módulo de historial próximamente", "info");
        }
    }

    private void confirmarEliminar(Producto p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar \"" + p.getNombre() + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.getButtonTypes().setAll(
                new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        confirm.showAndWait().ifPresent(resp -> {
            if (resp.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                dao.eliminarLogico(p.getIdProducto());
                org.example.servicio.AuditoriaService.get().registrar(
                        "BAJA_PRODUCTO", "productos", p.getIdProducto(),
                        "Producto eliminado: " + p.getNombre() +
                                " | Categoría: " + p.getCategoria()
                );
                cargarProductos();
                toast("\"" + p.getNombre() + "\" eliminado", "error");
            }
        });
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  EXPORTAR PDF
    // ────────────────────────────────────────────────────────────────────────────
    @FXML
    private void exportarPDF() {
        Alert opcion = new Alert(Alert.AlertType.CONFIRMATION);
        opcion.setTitle("Exportar inventario");
        opcion.setHeaderText("¿Qué productos deseas exportar?");
        opcion.setContentText("Selecciona una opción:");
        ButtonType btnFiltrado = new ButtonType("Solo vista actual");
        ButtonType btnCompleto = new ButtonType("Todo el inventario");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        opcion.getButtonTypes().setAll(btnFiltrado, btnCompleto, btnCancelar);

        Optional<ButtonType> respuesta = opcion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() == btnCancelar) return;

        List<Producto> productosAExportar = respuesta.get() == btnFiltrado
                ? new ArrayList<>(listaOrdenada)
                : listaProductos;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar PDF");
        chooser.setInitialFileName("inventario_volovan_volo.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));

        Stage stage = (Stage) tablaProductos.getScene().getWindow();
        File archivo = chooser.showSaveDialog(stage);
        if (archivo == null) return;

        try {
            new ExportarInventarioservice().exportar(productosAExportar, archivo.getAbsolutePath());
            toast("PDF exportado: " + archivo.getName(), "success");
        } catch (Exception e) {
            e.printStackTrace();
            toast("Error al exportar PDF", "error");
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  NAVEGACIÓN
    // ────────────────────────────────────────────────────────────────────────────
    @FXML private void irADashboard()     { cambiarEscena("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML private void irAVentas()        { cambiarEscena("/org/example/vista/Ventas.fxml"); }
    @FXML private void irAEmpleados()     { cambiarEscena("/org/example/vista/Empleados.fxml"); }
    @FXML private void irAClientes()      { cambiarEscena("/org/example/vista/Clientes.fxml"); }
    @FXML private void irAReportes()      { cambiarEscena("/org/example/vista/Reportes.fxml"); }
    @FXML private void irACorteCaja()     { cambiarEscena("/org/example/vista/CorteCaja.fxml"); }
    @FXML private void irAAuditoria()     { cambiarEscena("/org/example/vista/Auditoria.fxml"); }
    @FXML private void irAConfiguracion() { cambiarEscena("/org/example/vista/Configuracion.fxml"); }

    private void cambiarEscena(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tablaProductos.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  CERRAR SESIÓN
    // ────────────────────────────────────────────────────────────────────────────
    @FXML
    public void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Salir"); a.setHeaderText(null);
        a.setContentText("¿Seguro que deseas salir?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                registrarLogout();
                Platform.exit();
            }
        });
    }

    private void registrarLogout() {
        String sql = "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, detalle) " +
                "VALUES (?, 'LOGOUT', 'usuarios', ?, ?)";
        try (java.sql.Connection con = org.example.dao.ConexionDB.getConexion();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            int id = SesionUsuario.getInstancia().getIdUsuario();
            ps.setInt(1, id); ps.setInt(2, id);
            ps.setString(3, "Cierre de sesión: " + SesionUsuario.getInstancia().getNombre());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}