package org.example.controlador;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.CategoriaDAO;
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;

public class InventarioController {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String>  colNombre;
    @FXML private TableColumn<Producto, Double>  colPrecio;
    @FXML private TableColumn<Producto, String>  colCategoria;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String>  colEstado;
    @FXML private TableColumn<Producto, Void>    colAcciones;

    @FXML private Label lblTotalProductos;
    @FXML private Label lblStockBajo;
    @FXML private Label lblValorTotal;

    @FXML private TextField        txtBuscar;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbEstado;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private FilteredList<Producto>   filtro;
    private final ProductoDAO dao = new ProductoDAO();

    // ─── LISTENERS GUARDADOS COMO CAMPOS ─────────────────────────────────────
    private final ChangeListener<String> filtroCategoriaListener = (o, a, b) -> aplicarFiltros();
    private final ChangeListener<String> filtroEstadoListener    = (o, a, b) -> aplicarFiltros();

    // ─── INIT ────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {

        // Columnas básicas
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Columna estado
        colEstado.setCellValueFactory(cell -> {
            int stock = cell.getValue().getStock();
            return new SimpleStringProperty(stock < 5 ? "Bajo" : "Normal");
        });
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null); setStyle("");
                } else if (estado.equals("Bajo")) {
                    setText("Bajo");
                    setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                } else {
                    setText("Normal");
                    setStyle("-fx-text-fill: #3B6D11;");
                }
            }
        });

        // Columna acciones
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox   caja        = new HBox(5, btnEditar, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white;");
                btnEliminar.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");

                btnEditar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    abrirFormularioEditar(p);
                });

                btnEliminar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    dao.eliminarLogico(p.getIdProducto());
                    cargarProductos();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : caja);
            }
        });

        // Cargar datos y filtros
        cargarProductos();
        cargarFiltros();

        // Listeners (una sola vez)
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltros());
        cbCategoria.valueProperty().addListener(filtroCategoriaListener);
        cbEstado.valueProperty().addListener(filtroEstadoListener);
    }

    // ─── FILTROS ─────────────────────────────────────────────────────────────

    private void cargarFiltros() {
        cbCategoria.getItems().clear();
        new CategoriaDAO().obtenerCategorias()
                .forEach(c -> cbCategoria.getItems().add(c.getNombre()));
        cbCategoria.getSelectionModel().clearSelection();

        cbEstado.getItems().clear();
        cbEstado.getItems().addAll("Normal", "Bajo");
        cbEstado.getSelectionModel().clearSelection();
    }

    private void aplicarFiltros() {
        if (filtro == null) return;

        String texto     = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase();
        String categoria = cbCategoria.getValue();
        String estado    = cbEstado.getValue();

        filtro.setPredicate(p -> {
            boolean coincideTexto = texto.isEmpty()
                    || p.getNombre().toLowerCase().contains(texto)
                    || p.getCategoria().toLowerCase().contains(texto);

            boolean coincideCategoria = categoria == null
                    || p.getCategoria().equalsIgnoreCase(categoria);

            String estadoProducto = p.getStock() < 5 ? "Bajo" : "Normal";
            boolean coincideEstado = estado == null
                    || estadoProducto.equalsIgnoreCase(estado);

            return coincideTexto && coincideCategoria && coincideEstado;
        });
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();

        // Desconectar listeners para que clearSelection no dispare aplicarFiltros
        cbCategoria.valueProperty().removeListener(filtroCategoriaListener);
        cbEstado.valueProperty().removeListener(filtroEstadoListener);

        cbCategoria.getSelectionModel().clearSelection();
        cbEstado.getSelectionModel().clearSelection();

        // Reconectar
        cbCategoria.valueProperty().addListener(filtroCategoriaListener);
        cbEstado.valueProperty().addListener(filtroEstadoListener);

        aplicarFiltros();
    }

    // ─── DATOS ───────────────────────────────────────────────────────────────

    private void cargarProductos() {
        listaProductos.setAll(dao.obtenerProductos());

        if (filtro == null) {
            filtro = new FilteredList<>(listaProductos, p -> true);
            SortedList<Producto> sorted = new SortedList<>(filtro);
            sorted.comparatorProperty().bind(tablaProductos.comparatorProperty());
            tablaProductos.setItems(sorted);
        }

        actualizarResumen();
    }

    private void actualizarResumen() {
        int total    = listaProductos.size();
        int bajo     = 0;
        double valor = 0;

        for (Producto p : listaProductos) {
            if (p.getStock() < 5) bajo++;
            valor += p.getPrecio() * p.getStock();
        }

        lblTotalProductos.setText(String.valueOf(total));
        lblStockBajo.setText(String.valueOf(bajo));
        lblValorTotal.setText(String.format("$%.2f", valor));
    }

    // ─── MODALES ─────────────────────────────────────────────────────────────

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

        } catch (Exception e) {
            e.printStackTrace();
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
            stage.setTitle("Editar Producto");
            stage.showAndWait();

            cargarProductos();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}