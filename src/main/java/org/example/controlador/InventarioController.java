package org.example.controlador;

import javafx.beans.property.SimpleStringProperty;
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
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;
import org.example.modelo.Categoria;

public class InventarioController {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String> colEstado;
    @FXML private TableColumn<Producto, Void> colAcciones;

    @FXML private Label lblTotalProductos;
    @FXML private Label lblStockBajo;
    @FXML private Label lblValorTotal;

    @FXML private TextField txtBuscar;

    private ObservableList<Producto> listaProductos;
    private ProductoDAO dao = new ProductoDAO();

    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbFiltroEstado;

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();

        if (cbCategoria != null)
            cbCategoria.getSelectionModel().clearSelection();

        if (cbFiltroEstado != null)
            cbFiltroEstado.getSelectionModel().clearSelection();
    }


    @FXML
    public void initialize() {

        // 🔹 Columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // 🔹 Estado dinámico
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

        // 🔹 Acciones (EDITAR / ELIMINAR)
        colAcciones.setCellFactory(col -> new TableCell<>() {

            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox caja = new HBox(5, btnEditar, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white;");
                btnEliminar.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");

                // ✏️ EDITAR
                btnEditar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    abrirFormularioEditar(p);
                });

                // 🗑️ ELIMINAR (lógico)
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

        // 🔥 CARGAR DESDE BD
        cargarProductos();

        // 🔍 FILTRO
        FilteredList<Producto> filtro = new FilteredList<>(listaProductos, p -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            filtro.setPredicate(p -> {
                if (newVal == null || newVal.isEmpty()) return true;

                String lower = newVal.toLowerCase();
                return p.getNombre().toLowerCase().contains(lower);
            });
        });

        SortedList<Producto> sorted = new SortedList<>(filtro);
        sorted.comparatorProperty().bind(tablaProductos.comparatorProperty());

        tablaProductos.setItems(sorted);
    }

    // 🔥 MÉTODO CLAVE (BD)
    private void cargarProductos() {
        listaProductos = FXCollections.observableArrayList(dao.obtenerProductos());
        tablaProductos.setItems(listaProductos);
        actualizarResumen();
    }

    private void abrirFormularioEditar(Producto p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/EditarProducto.fxml"));
            Parent root = loader.load();

            // Pasar el producto al controlador del modal
            EditarProductoController ctrl = loader.getController();
            ctrl.setProducto(p);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Editar Producto");
            stage.showAndWait();

            cargarProductos(); // refresca la tabla al cerrar

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void abrirFormularioNuevo() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/vista/AgregarProducto.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            stage.initModality(Modality.APPLICATION_MODAL); // 🔥 BLOQUEA TODO
            stage.setResizable(false);
            stage.setTitle("Agregar Producto");

            stage.showAndWait(); // 🔥 NO permite interactuar atrás

            cargarProductos(); // refresca tabla al cerrar

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarResumen() {
        int total = listaProductos.size();
        int bajo = 0;
        double valor = 0;

        for (Producto p : listaProductos) {
            if (p.getStock() < 5) bajo++;
            valor += p.getPrecio() * p.getStock();
        }

        lblTotalProductos.setText(String.valueOf(total));
        lblStockBajo.setText(String.valueOf(bajo));
        lblValorTotal.setText(String.format("%.2f", valor));
    }


}