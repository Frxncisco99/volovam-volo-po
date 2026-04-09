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

    private final ChangeListener<String> filtroCategoriaListener = (o, a, b) -> aplicarFiltros();
    private final ChangeListener<String> filtroEstadoListener    = (o, a, b) -> aplicarFiltros();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Header de tabla en vino oscuro via CSS de JavaFX
        tablaProductos.setStyle(
                "-fx-background-color: #F5EFE6; " +
                        "-fx-border-color: #D4C9B0; " +
                        "-fx-border-width: 0.5;"
        );

        // Columna stock — rojo si bajo
        colStock.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null); setStyle("");
                } else if (stock < 5) {
                    setText(String.valueOf(stock));
                    setStyle("-fx-text-fill: #A32D2D; -fx-font-weight: bold;");
                } else {
                    setText(String.valueOf(stock));
                    setStyle("-fx-text-fill: #6B4226;");
                }
            }
        });

        // Columna estado — badges de colores
        colEstado.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStock() < 5 ? "Bajo" : "Normal")
        );
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null); setStyle("");
                } else if (estado.equals("Bajo")) {
                    setText("Bajo");
                    setStyle("-fx-background-color: #F7E0E0; -fx-text-fill: #6B1228; " +
                            "-fx-font-weight: bold; -fx-background-radius: 20; -fx-alignment: CENTER;");
                } else {
                    setText("Normal");
                    setStyle("-fx-background-color: #F0EAD0; -fx-text-fill: #7A5E1A; " +
                            "-fx-background-radius: 20; -fx-alignment: CENTER;");
                }
            }
        });

        // Columna acciones — dorado + vino
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox   caja        = new HBox(6, btnEditar, btnEliminar);

            {
                btnEditar.setStyle(
                        "-fx-background-color: #C9A84C; -fx-text-fill: #3D1A0A; " +
                                "-fx-background-radius: 6; -fx-padding: 4 12; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;"
                );
                btnEliminar.setStyle(
                        "-fx-background-color: #6B1228; -fx-text-fill: #F5EFE0; " +
                                "-fx-background-radius: 6; -fx-padding: 4 12; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;"
                );
                caja.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 2 0;");

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

        // Filas alternadas
        tablaProductos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else if (getIndex() % 2 == 0) {
                    setStyle("-fx-background-color: #F5EFE6;");
                } else {
                    setStyle("-fx-background-color: #EDE8DC;");
                }
            }
        });

        cargarProductos();
        cargarFiltros();

        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltros());
        cbCategoria.valueProperty().addListener(filtroCategoriaListener);
        cbEstado.valueProperty().addListener(filtroEstadoListener);
    }

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
            String ep = p.getStock() < 5 ? "Bajo" : "Normal";
            boolean coincideEstado = estado == null || ep.equalsIgnoreCase(estado);
            return coincideTexto && coincideCategoria && coincideEstado;
        });
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
        aplicarFiltros();
    }

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
        int total = listaProductos.size();
        int bajo  = 0;
        double valor = 0;
        for (Producto p : listaProductos) {
            if (p.getStock() < 5) bajo++;
            valor += p.getPrecio() * p.getStock();
        }
        lblTotalProductos.setText(String.valueOf(total));
        lblStockBajo.setText(String.valueOf(bajo));
        lblValorTotal.setText(String.format("$%.2f", valor));
    }

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