package org.example.controlador;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.OperacionesDAO;
import org.example.dao.ProductoDAO;
import org.example.modelo.Producto;
import org.example.modelo.SesionUsuario;
import org.example.servicio.MarcaService;
import org.example.servicio.PermisoService;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OperacionesController {

    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    @FXML private TableView<Map<String, Object>> tablaPrefacturas;
    @FXML private TableView<Map<String, Object>> tablaPromociones;
    @FXML private TableView<Map<String, Object>> tablaProveedores;
    @FXML private TableView<Map<String, Object>> tablaProductoProveedor;
    @FXML private TableView<Map<String, Object>> tablaCompras;
    @FXML private TableView<Map<String, Object>> tablaTurnos;
    @FXML private TableView<Map<String, Object>> tablaMotivos;

    @FXML private TextField txtPromoNombre;
    @FXML private ComboBox<String> cmbPromoTipo;
    @FXML private TextField txtPromoValor;
    @FXML private DatePicker datePromoInicio;
    @FXML private DatePicker datePromoFin;
    @FXML private CheckBox chkPromoActiva;

    @FXML private TextField txtProveedorNombre;
    @FXML private TextField txtProveedorRfc;
    @FXML private TextField txtProveedorTelefono;
    @FXML private TextField txtProveedorEmail;
    @FXML private TextField txtProveedorContacto;

    @FXML private ComboBox<Opcion> cmbVinculoProveedor;
    @FXML private ComboBox<Opcion> cmbVinculoProducto;
    @FXML private TextField txtVinculoCosto;
    @FXML private CheckBox chkVinculoPrincipal;

    @FXML private ComboBox<Opcion> cmbCompraProveedor;
    @FXML private ComboBox<Opcion> cmbCompraProducto;
    @FXML private TextField txtCompraFolio;
    @FXML private TextField txtCompraDescripcion;
    @FXML private TextField txtCompraCantidad;
    @FXML private TextField txtCompraCosto;

    @FXML private TextField txtTurnoFondo;
    @FXML private TextField txtTurnoContado;
    @FXML private TextField txtTurnoObs;

    @FXML private TextField txtMotivoClave;
    @FXML private TextField txtMotivoDescripcion;

    private final OperacionesDAO dao = new OperacionesDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final DecimalFormat dinero = new DecimalFormat("$#,##0.00");

    @FXML
    public void initialize() {
        cargarUsuario();
        configurarTablas();
        cmbPromoTipo.setItems(FXCollections.observableArrayList("PORCENTAJE", "MONTO"));
        cmbPromoTipo.getSelectionModel().selectFirst();
        chkPromoActiva.setSelected(true);
        recargarTodo();
    }

    @FXML private void recargarTodo() {
        tablaPrefacturas.setItems(FXCollections.observableArrayList(dao.listarPrefacturas()));
        tablaPromociones.setItems(FXCollections.observableArrayList(dao.listarPromociones()));
        tablaProveedores.setItems(FXCollections.observableArrayList(dao.listarProveedores()));
        tablaProductoProveedor.setItems(FXCollections.observableArrayList(dao.listarProductoProveedor()));
        tablaCompras.setItems(FXCollections.observableArrayList(dao.listarCompras()));
        tablaTurnos.setItems(FXCollections.observableArrayList(dao.listarTurnos()));
        tablaMotivos.setItems(FXCollections.observableArrayList(dao.listarMotivosCancelacion()));
        cargarCombosCompra();
    }

    @FXML private void guardarPromocion() {
        try {
            String nombre = requerido(txtPromoNombre, "Nombre de promoción");
            double valor = numero(txtPromoValor, "Valor");
            dao.guardarPromocion(nombre, cmbPromoTipo.getValue(), valor,
                    datePromoInicio.getValue(), datePromoFin.getValue(), chkPromoActiva.isSelected());
            limpiarPromo();
            recargarTodo();
        } catch (Exception e) {
            alerta("Promoción", e.getMessage());
        }
    }

    @FXML private void alternarPromocion() {
        Map<String, Object> fila = tablaPromociones.getSelectionModel().getSelectedItem();
        if (fila == null) return;
        try {
            boolean activo = !booleano(fila.get("activo"));
            dao.cambiarActivoPromocion(entero(fila.get("id_promocion")), activo);
            recargarTodo();
        } catch (Exception e) {
            alerta("Promoción", e.getMessage());
        }
    }

    @FXML private void verDetallePrefactura() {
        Map<String, Object> fila = tablaPrefacturas.getSelectionModel().getSelectedItem();
        if (fila == null) {
            alerta("Prefactura", "Selecciona una prefactura.");
            return;
        }

        TableView<Map<String, Object>> tablaDetalle = new TableView<>();
        columnas(tablaDetalle,
                col("Descripcion", "descripcion"), col("Cant.", "cantidad"), col("Precio", "precio_unitario"),
                col("Subtotal", "subtotal_sin_impuesto"), col("Desc.", "descuento"),
                col("Impuesto", "impuesto_importe"), col("Total", "total_linea"));
        tablaDetalle.setItems(FXCollections.observableArrayList(
                dao.listarDetallePrefactura(entero(fila.get("id_factura")))));

        Label titulo = new Label("Prefactura " + formatear(fila.get("folio_interno")));
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0d3d5e;");
        Label total = new Label("Total: " + formatear(fila.get("total")) + " | Estado: " + formatear(fila.get("estado")));
        total.setStyle("-fx-text-fill: #6a96b8;");

        VBox root = new VBox(10, titulo, total, tablaDetalle);
        root.setStyle("-fx-padding: 16; -fx-background-color: #f0f7ff;");
        VBox.setVgrow(tablaDetalle, javafx.scene.layout.Priority.ALWAYS);

        Stage stage = new Stage();
        org.example.servicio.VentanaEmergenteService.preparar(stage);
        stage.setTitle("Detalle de prefactura");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(tablaPrefacturas.getScene().getWindow());
        stage.setScene(new Scene(root, 760, 460));
        stage.show();
    }

    @FXML private void cancelarPrefactura() {
        Map<String, Object> fila = tablaPrefacturas.getSelectionModel().getSelectedItem();
        if (fila == null) {
            alerta("Prefactura", "Selecciona una prefactura.");
            return;
        }
        if ("CANCELADA".equalsIgnoreCase(String.valueOf(fila.get("estado")))) {
            alerta("Prefactura", "La prefactura ya está cancelada.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar prefactura");
        dialog.setHeaderText("Cancelar " + formatear(fila.get("folio_interno")));
        dialog.setContentText("Motivo:");
        org.example.servicio.DialogService.preparar(dialog, lblNombreUsuario);
        dialog.showAndWait().ifPresent(motivo -> {
            try {
                dao.cancelarPrefactura(entero(fila.get("id_factura")), motivo);
                recargarTodo();
            } catch (Exception e) {
                alerta("Prefactura", e.getMessage());
            }
        });
    }

    @FXML private void guardarProveedor() {
        try {
            dao.guardarProveedor(
                    requerido(txtProveedorNombre, "Nombre del proveedor"),
                    txtProveedorRfc.getText(),
                    txtProveedorTelefono.getText(),
                    txtProveedorEmail.getText(),
                    txtProveedorContacto.getText()
            );
            txtProveedorNombre.clear();
            txtProveedorRfc.clear();
            txtProveedorTelefono.clear();
            txtProveedorEmail.clear();
            txtProveedorContacto.clear();
            recargarTodo();
        } catch (Exception e) {
            alerta("Proveedor", e.getMessage());
        }
    }

    @FXML private void vincularProveedorProducto() {
        try {
            Opcion proveedor = cmbVinculoProveedor.getValue();
            Opcion producto = cmbVinculoProducto.getValue();
            if (proveedor == null || producto == null) {
                throw new IllegalArgumentException("Selecciona proveedor y producto.");
            }
            double costo = numero(txtVinculoCosto, "Costo ultimo");
            dao.vincularProductoProveedor(producto.id(), proveedor.id(), costo, chkVinculoPrincipal.isSelected());
            txtVinculoCosto.clear();
            chkVinculoPrincipal.setSelected(true);
            recargarTodo();
        } catch (Exception e) {
            alerta("Proveedor-producto", e.getMessage());
        }
    }

    @FXML private void alternarVinculoProveedorProducto() {
        Map<String, Object> fila = tablaProductoProveedor.getSelectionModel().getSelectedItem();
        if (fila == null) {
            alerta("Proveedor-producto", "Selecciona un enlace.");
            return;
        }
        try {
            boolean activo = !booleano(fila.get("activo"));
            dao.cambiarActivoProductoProveedor(
                    entero(fila.get("id_producto")),
                    entero(fila.get("id_proveedor")),
                    activo
            );
            recargarTodo();
        } catch (Exception e) {
            alerta("Proveedor-producto", e.getMessage());
        }
    }

    @FXML private void registrarCompra() {
        try {
            Opcion proveedor = cmbCompraProveedor.getValue();
            Opcion producto = cmbCompraProducto.getValue();
            String descripcion = txtCompraDescripcion.getText();
            if ((descripcion == null || descripcion.isBlank()) && producto != null) {
                descripcion = producto.texto();
            }
            dao.registrarCompra(
                    proveedor == null ? 0 : proveedor.id(),
                    producto == null ? 0 : producto.id(),
                    descripcion == null || descripcion.isBlank() ? "Compra sin descripcion" : descripcion.trim(),
                    numero(txtCompraCantidad, "Cantidad"),
                    numero(txtCompraCosto, "Costo unitario"),
                    txtCompraFolio.getText()
            );
            txtCompraFolio.clear();
            txtCompraDescripcion.clear();
            txtCompraCantidad.clear();
            txtCompraCosto.clear();
            recargarTodo();
        } catch (Exception e) {
            alerta("Compra", e.getMessage());
        }
    }

    @FXML private void abrirTurno() {
        try {
            dao.abrirTurno(numero(txtTurnoFondo, "Fondo inicial"), txtTurnoObs.getText());
            txtTurnoFondo.clear();
            txtTurnoObs.clear();
            recargarTodo();
        } catch (Exception e) {
            alerta("Turno", e.getMessage());
        }
    }

    @FXML private void cerrarTurno() {
        Map<String, Object> fila = tablaTurnos.getSelectionModel().getSelectedItem();
        if (fila == null) {
            alerta("Turno", "Selecciona un turno abierto.");
            return;
        }
        try {
            dao.cerrarTurno(entero(fila.get("id_turno")),
                    numero(txtTurnoContado, "Efectivo contado"),
                    txtTurnoObs.getText());
            txtTurnoContado.clear();
            txtTurnoObs.clear();
            recargarTodo();
        } catch (Exception e) {
            alerta("Turno", e.getMessage());
        }
    }

    @FXML private void guardarMotivo() {
        try {
            dao.guardarMotivoCancelacion(
                    requerido(txtMotivoClave, "Clave").toUpperCase().replace(" ", "_"),
                    requerido(txtMotivoDescripcion, "Descripcion")
            );
            txtMotivoClave.clear();
            txtMotivoDescripcion.clear();
            recargarTodo();
        } catch (Exception e) {
            alerta("Motivo", e.getMessage());
        }
    }

    @FXML private void alternarMotivo() {
        Map<String, Object> fila = tablaMotivos.getSelectionModel().getSelectedItem();
        if (fila == null) return;
        try {
            dao.cambiarActivoMotivo(entero(fila.get("id_motivo")), !booleano(fila.get("activo")));
            recargarTodo();
        } catch (Exception e) {
            alerta("Motivo", e.getMessage());
        }
    }

    @FXML private void irADashboard() { navegar("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML private void irAVentas() { navegarConPermiso(PermisoService.Accion.ACCEDER_VENTAS, "/org/example/vista/Ventas.fxml"); }
    @FXML private void irAInventario() { navegarConPermiso(PermisoService.Accion.ACCEDER_INVENTARIO, "/org/example/vista/Inventario.fxml"); }
    @FXML private void irAClientes() { navegarConPermiso(PermisoService.Accion.ACCEDER_CLIENTES, "/org/example/vista/Clientes.fxml"); }
    @FXML private void irAReportes() { navegarConPermiso(PermisoService.Accion.VER_REPORTES, "/org/example/vista/Reportes.fxml"); }
    @FXML private void irACorteCaja() { navegarConPermiso(PermisoService.Accion.VER_CORTE_CAJA, "/org/example/vista/CorteCaja.fxml"); }
    @FXML private void irAConfiguracion() { navegarConPermiso(PermisoService.Accion.ACCEDER_CONFIGURACION, "/org/example/vista/Configuracion.fxml"); }

    private void cargarUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String nombre = sesion.getNombre() == null ? "US" : sesion.getNombre();
        lblAvatarIniciales.setText(nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase());
    }

    private void configurarTablas() {
        columnas(tablaPrefacturas,
                col("Folio", "folio_interno"), col("Fecha", "fecha"), col("Cliente", "cliente"),
                col("RFC", "rfc_receptor"), col("Estado", "estado"), col("Total", "total"));
        columnas(tablaPromociones,
                col("Nombre", "nombre"), col("Tipo", "tipo"), col("Valor", "valor"),
                col("Inicio", "fecha_inicio"), col("Fin", "fecha_fin"), col("Activa", "activo"));
        columnas(tablaProveedores,
                col("Nombre", "nombre"), col("RFC", "rfc"), col("Teléfono", "telefono"),
                col("Email", "email"), col("Contacto", "contacto"), col("Activo", "activo"));
        columnas(tablaProductoProveedor,
                col("Producto", "producto"), col("Proveedor", "proveedor"), col("Costo", "costo_ultimo"),
                col("Principal", "proveedor_principal"), col("Activo", "activo"), col("Actualizado", "actualizado_en"));
        columnas(tablaCompras,
                col("Folio", "folio"), col("Fecha", "fecha_hora"), col("Proveedor", "proveedor"),
                col("Subtotal", "subtotal"), col("Total", "total"), col("Estado", "estado"));
        columnas(tablaTurnos,
                col("ID", "id_turno"), col("Usuario", "usuario"), col("Caja", "id_caja"),
                col("Apertura", "fecha_apertura"), col("Cierre", "fecha_cierre"),
                col("Contado", "efectivo_contado"), col("Dif.", "diferencia"), col("Estado", "estado"));
        columnas(tablaMotivos,
                col("Clave", "clave"), col("Descripcion", "descripcion"), col("Activo", "activo"));
    }

    @SafeVarargs
    private final void columnas(TableView<Map<String, Object>> tabla, TableColumn<Map<String, Object>, String>... cols) {
        tabla.getColumns().setAll(cols);
    }

    private TableColumn<Map<String, Object>, String> col(String titulo, String llave) {
        TableColumn<Map<String, Object>, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(switch (titulo) {
            case "Cliente", "Nombre", "Proveedor", "Descripcion", "Producto" -> 180;
            case "Fecha", "Apertura", "Cierre", "Actualizado" -> 145;
            default -> 105;
        });
        col.setCellValueFactory(data -> new SimpleStringProperty(formatear(data.getValue().get(llave))));
        return col;
    }

    private void cargarCombosCompra() {
        List<Opcion> proveedores = dao.listarProveedores().stream()
                .filter(m -> booleano(m.get("activo")))
                .map(m -> new Opcion(entero(m.get("id_proveedor")), String.valueOf(m.get("nombre"))))
                .collect(Collectors.toList());
        cmbCompraProveedor.setItems(FXCollections.observableArrayList(proveedores));
        cmbVinculoProveedor.setItems(FXCollections.observableArrayList(proveedores));

        List<Opcion> productos = productoDAO.obtenerProductos().stream()
                .map(p -> new Opcion(p.getIdProducto(), p.getNombre()))
                .collect(Collectors.toList());
        cmbCompraProducto.setItems(FXCollections.observableArrayList(productos));
        cmbVinculoProducto.setItems(FXCollections.observableArrayList(productos));
    }

    private String formatear(Object valor) {
        if (valor == null) return "";
        if (valor instanceof Number n && !(valor instanceof Integer) && !(valor instanceof Long)) {
            return dinero.format(n.doubleValue());
        }
        return String.valueOf(valor);
    }

    private String requerido(TextField field, String nombre) {
        String value = field.getText();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(nombre + " es requerido.");
        }
        return value.trim();
    }

    private double numero(TextField field, String nombre) {
        try {
            return Double.parseDouble(requerido(field, nombre));
        } catch (NumberFormatException e) {
        throw new IllegalArgumentException(nombre + " debe ser numérico.");
        }
    }

    private int entero(Object value) {
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    private boolean booleano(Object value) {
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private void limpiarPromo() {
        txtPromoNombre.clear();
        txtPromoValor.clear();
        datePromoInicio.setValue(null);
        datePromoFin.setValue(null);
        chkPromoActiva.setSelected(true);
    }

    private void navegarConPermiso(PermisoService.Accion accion, String ruta) {
        if (!PermisoService.puede(accion)) {
            alerta("Acceso denegado", "No tienes permiso para acceder a este módulo.");
            return;
        }
        navegar(ruta);
    }

    private void navegar(String ruta) {
        org.example.servicio.NavigationService.cambiarEscena(lblNombreUsuario, ruta);
    }

    private void alerta(String titulo, String mensaje) {
        org.example.servicio.DialogService.info(lblNombreUsuario, titulo, mensaje);
    }

    @FXML
    private void btnCerrar() {
        org.example.servicio.NavigationService.cambiarSesion(lblNombreUsuario);
    }

    @FXML
    private void salirAplicacion() {
        org.example.servicio.AppExitService.salir(lblNombreUsuario);
    }

    public record Opcion(int id, String texto) {
        @Override public String toString() { return texto; }
    }
}
