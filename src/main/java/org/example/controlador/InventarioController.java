package org.example.controlador;

import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.CategoriaDAO;
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;
import org.example.modelo.SesionUsuario;
import org.example.servicio.ExportarInventarioservice;

import java.io.File;
import java.util.List;
import java.util.Optional;

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

    // ── Sidebar ──────────────────────────────────────────────────────────────
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private FilteredList<Producto>   filtro;
    private final ProductoDAO dao = new ProductoDAO();

    private final ChangeListener<String> filtroCategoriaListener = (o, a, b) -> aplicarFiltros();
    private final ChangeListener<String> filtroEstadoListener    = (o, a, b) -> aplicarFiltros();

    @FXML
    public void initialize() {

        // ── Datos del usuario en sidebar ─────────────────────────────────────
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);

        // ── Columnas ─────────────────────────────────────────────────────────
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); setStyle(""); }
                else {
                    setText("#" + String.format("%03d", id));
                    setStyle("-fx-text-fill: #8B4A5A; -fx-font-size: 12px;");
                }
            }
        });

        colNombre.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String nombre, boolean empty) {
                super.updateItem(nombre, empty);
                if (empty || nombre == null) { setText(null); setStyle(""); }
                else {
                    setText(nombre);
                    setStyle("-fx-text-fill: #3D1A0A; -fx-font-weight: bold;");
                }
            }
        });

        colCategoria.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.setStyle("-fx-background-color: #F0EAD0; -fx-text-fill: #7A5E1A; " +
                        "-fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px;");
            }
            @Override
            protected void updateItem(String cat, boolean empty) {
                super.updateItem(cat, empty);
                if (empty || cat == null) setGraphic(null);
                else { badge.setText(cat); setGraphic(badge); }
            }
        });

        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) { setText(null); setStyle(""); }
                else {
                    setText(String.format("$%.2f", precio));
                    setStyle("-fx-text-fill: #3D1A0A; -fx-font-weight: bold;");
                }
            }
        });

        colStock.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) { setText(null); setStyle(""); }
                else {
                    Producto p = getTableView().getItems().get(getIndex());
                    setText(String.valueOf(stock));
                    setStyle(p.isBajoStock()
                            ? "-fx-text-fill: #A32D2D; -fx-font-weight: bold;"
                            : "-fx-text-fill: #6B4226;");
                }
            }
        });

        colEstado.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().isBajoStock() ? "Bajo" : "Normal")
        );
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                } else if (estado.equals("Bajo")) {
                    badge.setText("⚠ Bajo Stock");
                    badge.setStyle("-fx-background-color: #F7E0E0; -fx-text-fill: #6B1228; " +
                            "-fx-background-radius: 20; -fx-padding: 4 12; " +
                            "-fx-font-size: 11px; -fx-font-weight: bold;");
                    setGraphic(badge);
                } else {
                    badge.setText("Stock OK");
                    badge.setStyle("-fx-background-color: #D4EDDA; -fx-text-fill: #1A5C2E; " +
                            "-fx-background-radius: 20; -fx-padding: 4 12; " +
                            "-fx-font-size: 11px; -fx-font-weight: bold;");
                    setGraphic(badge);
                }
            }
        });

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox   caja        = new HBox(8, btnEditar, btnEliminar);
            {
                btnEditar.setStyle("-fx-background-color: #C9A84C; -fx-text-fill: #3D1A0A; " +
                        "-fx-background-radius: 6; -fx-padding: 5 14; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: #6B1228; -fx-text-fill: #F5EFE0; " +
                        "-fx-background-radius: 6; -fx-padding: 5 14; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
                caja.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 2 0;");

                btnEditar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    abrirFormularioEditar(p);
                });
                btnEliminar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
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
                            cargarProductos();
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : caja);
            }
        });

        tablaProductos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color: transparent;");
                else if (getIndex() % 2 == 0) setStyle("-fx-background-color: #F5EFE6;");
                else setStyle("-fx-background-color: #EDE8DC;");
            }
        });

        cargarProductos();
        cargarFiltros();

        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltros());
        cbCategoria.valueProperty().addListener(filtroCategoriaListener);
        cbEstado.valueProperty().addListener(filtroEstadoListener);
    }

    // ── Navegación ───────────────────────────────────────────────────────────

    @FXML
    private void irADashboard() {
        cambiarEscena("/org/example/vista/MenuPrincipal.fxml");
    }

    @FXML
    private void irAVentas() {
        cambiarEscena("/org/example/vista/Ventas.fxml");
    }

    @FXML
    private  void irAEmpleados(){
        cambiarEscena("/org/example/vista/Empleados.fxml");
    }

    @FXML
    private void btnCerrar() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Salir");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas salir?");
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) Platform.exit();
        });
    }

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

    // ── Exportar PDF ─────────────────────────────────────────────────────────

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
                ? tablaProductos.getItems()
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
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Exportación exitosa");
            ok.setHeaderText(null);
            ok.setContentText("PDF guardado en:\n" + archivo.getAbsolutePath());
            ok.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error al exportar");
            err.setHeaderText("No se pudo generar el PDF");
            err.setContentText(e.getMessage());
            err.showAndWait();
        }
    }

    // ── Filtros ──────────────────────────────────────────────────────────────

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
            boolean coincideEstado = estado == null
                    || p.isBajoStock() == estado.equals("Bajo");
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

    // ── Productos ────────────────────────────────────────────────────────────

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
            if (p.isBajoStock()) bajo++;
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