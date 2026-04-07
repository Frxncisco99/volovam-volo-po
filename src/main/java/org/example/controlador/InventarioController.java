package org.example.controlador;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;

public class InventarioController {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
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

                    // Ejemplo rápido (luego hacemos formulario)
                    p.setStock(p.getStock() + 1);

                    dao.actualizarProducto(p);
                    cargarProductos();
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

    @FXML
    private void abrirFormularioNuevo() {
        System.out.println("Aquí abriremos formulario real...");
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