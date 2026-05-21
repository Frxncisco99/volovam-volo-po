package org.example.controlador;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.servicio.AppExitService;
import org.example.servicio.MarcaService;
import org.example.servicio.PermisoService;


import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class MenuPrincipal implements Initializable {

    @FXML private Label lblFecha;
    @FXML private Label lblHora;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;
    @FXML private Label lblVentasHoy;
    @FXML private Label lblVentasDelta;
    @FXML private Label lblPedidosHoy;
    @FXML private Label lblProductosVendidos;
    @FXML private Label lblProductosBajos;
    @FXML private TableView<DashboardVenta> tablaUltimasVentas;
    @FXML private TableColumn<DashboardVenta, String> colUltFolio;
    @FXML private TableColumn<DashboardVenta, String> colUltFechaHora;
    @FXML private TableColumn<DashboardVenta, String> colUltTotal;
    @FXML private TableColumn<DashboardVenta, String> colUltMetodo;
    @FXML private TableColumn<DashboardVenta, String> colUltCajero;
    @FXML private TableColumn<DashboardVenta, String> colUltProductos;
    @FXML private TableColumn<DashboardVenta, String> colUltEstado;
    @FXML private Label lblSinVentas;
    @FXML private Label lblSinTop;
    @FXML private PieChart graficaVentas;

    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mostrarFecha();
        iniciarReloj();
        cargarDatosUsuario();
        configurarTablaUltimasVentas();
        cargarDashboard();

    }

    private void mostrarFecha() {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
        String fechaHoy = LocalDate.now().format(formatter);
        lblFecha.setText(fechaHoy.substring(0, 1).toUpperCase() + fechaHoy.substring(1));
    }

    private void iniciarReloj() {
        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm:ss");
        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                lblHora.setText(LocalTime.now().format(fmtHora))
        ));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
        lblHora.setText(LocalTime.now().format(fmtHora));
    }

    private void cargarDatosUsuario() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);
        if (PermisoService.esCajeroActual()) {
            Platform.runLater(() -> cambiarEscena("/org/example/vista/Ventas.fxml"));
        }
    }

    private void configurarTablaUltimasVentas() {
        if (tablaUltimasVentas == null) return;

        colUltFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        colUltFechaHora.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaHora()));
        colUltTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotal()));
        colUltMetodo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMetodoPago()));
        colUltCajero.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCajero()));
        colUltProductos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductos()));
        colUltEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));

        colUltTotal.setStyle("-fx-alignment: CENTER_RIGHT;");
        colUltProductos.setStyle("-fx-alignment: CENTER;");

        colUltEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String estado = item.toLowerCase(Locale.ROOT);
                String color = switch (estado) {
                    case "completada" -> "#1e7d3e";
                    case "cancelada" -> "#c0392b";
                    case "devuelta", "parcial" -> "#d97706";
                    default -> "#1a6fa8";
                };
                String fondo = switch (estado) {
                    case "completada" -> "#e6f4ec";
                    case "cancelada" -> "#fde8e8";
                    case "devuelta", "parcial" -> "#fff3e0";
                    default -> "#e8f2fb";
                };
                setStyle("-fx-text-fill: " + color + "; -fx-background-color: " + fondo + "; " +
                        "-fx-background-radius: 12; -fx-padding: 3 8; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        tablaUltimasVentas.setFixedCellSize(54);
        tablaUltimasVentas.setRowFactory(tv -> {
            TableRow<DashboardVenta> row = new TableRow<>();
            Tooltip tooltip = new Tooltip();
            tooltip.setStyle("-fx-background-color: #091e4e; -fx-text-fill: white; -fx-font-size: 11px;");
            row.itemProperty().addListener((obs, anterior, venta) -> {
                if (venta == null) {
                    row.setTooltip(null);
                } else {
                    tooltip.setText(venta.getFolio() + "\n" +
                            venta.getFechaHora() + " - " + venta.getCajero() + "\n" +
                            venta.getMetodoPago() + " - " + venta.getProductos() + " - " + venta.getTotal());
                    row.setTooltip(tooltip);
                }
            });
            return row;
        });
    }

    private void cargarDashboard() {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();

        try (Connection con = ConexionDB.getConexion()) {

            // Ventas del dia
            String sqlVentas = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM ventas WHERE id_caja = ? AND DATE(fecha) = CURDATE()";
            PreparedStatement psVentas = con.prepareStatement(sqlVentas);
            psVentas.setInt(1, idCaja);
            ResultSet rsVentas = psVentas.executeQuery();
            if (rsVentas.next()) {
                int numVentas = rsVentas.getInt(1);
                double totalVentas = rsVentas.getDouble(2);
                lblPedidosHoy.setText(String.valueOf(numVentas));
                lblVentasHoy.setText(formatoMoneda.format(totalVentas));
                lblVentasDelta.setText(numVentas == 0 ? "sin ventas aun" : numVentas + " tickets registrados");
            }

            // Productos vendidos hoy
            String sqlProductos = "SELECT COALESCE(SUM(dv.cantidad), 0) FROM detalle_venta dv JOIN ventas v ON dv.id_venta = v.id_venta WHERE v.id_caja = ? AND DATE(v.fecha) = CURDATE()";
            PreparedStatement psProductos = con.prepareStatement(sqlProductos);
            psProductos.setInt(1, idCaja);
            ResultSet rsProductos = psProductos.executeQuery();
            if (rsProductos.next()) {
                lblProductosVendidos.setText(String.valueOf(rsProductos.getInt(1)));
            }

            // Stock bajo
            String sqlStock = "SELECT COUNT(*) FROM productos WHERE stock <= stock_minimo AND activo = 1";
            PreparedStatement psStock = con.prepareStatement(sqlStock);
            ResultSet rsStock = psStock.executeQuery();
            if (rsStock.next()) {
                lblProductosBajos.setText(String.valueOf(rsStock.getInt(1)));
            }

            boolean tieneFechaHora = columnaExiste(con, "ventas", "fecha_hora");
            boolean tieneMetodoPago = columnaExiste(con, "ventas", "metodo_pago");
            boolean tieneEstado = columnaExiste(con, "ventas", "estado");
            boolean tienePagos = tablaExiste(con, "pagos");

            String colFechaVenta = tieneFechaHora ? "COALESCE(v.fecha_hora, v.fecha)" : "v.fecha";
            String colMetodoVenta = tieneMetodoPago ? "v.metodo_pago" : "NULL";
            String exprMetodo = tienePagos
                    ? "COALESCE(p.tipo_pago, " + colMetodoVenta + ", 'Efectivo')"
                    : "COALESCE(" + colMetodoVenta + ", 'Efectivo')";
            String exprEstado = tieneEstado ? "COALESCE(v.estado, 'COMPLETADA')" : "'COMPLETADA'";
            String joinPagos = tienePagos ? "LEFT JOIN pagos p ON p.id_venta = v.id_venta " : "";

            // Ultimas ventas
            String sqlUltimas =
                    "SELECT v.id_venta, " + colFechaVenta + " AS fecha_venta, v.total, " +
                            exprMetodo + " AS metodo_pago, " +
                            "COALESCE(u.nombre, 'Sin usuario') AS cajero, " +
                            "COALESCE(dv.total_productos, 0) AS total_productos, " +
                            "COALESCE(dv.tipos_productos, 0) AS tipos_productos, " +
                            exprEstado + " AS estado " +
                    "FROM ventas v " +
                    "JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                    joinPagos +
                    "LEFT JOIN ( " +
                    "   SELECT id_venta, SUM(cantidad) AS total_productos, COUNT(DISTINCT id_producto) AS tipos_productos " +
                    "   FROM detalle_venta GROUP BY id_venta " +
                    ") dv ON dv.id_venta = v.id_venta " +
                    "WHERE v.id_caja = ? AND DATE(" + colFechaVenta + ") = CURDATE() " +
                    "ORDER BY " + colFechaVenta + " DESC LIMIT 15";
            PreparedStatement psUltimas = con.prepareStatement(sqlUltimas);
            psUltimas.setInt(1, idCaja);
            ResultSet rsUltimas = psUltimas.executeQuery();

            ObservableList<DashboardVenta> ventasRecientes = FXCollections.observableArrayList();

            while (rsUltimas.next()) {
                int idVenta = rsUltimas.getInt("id_venta");
                String fechaHora = rsUltimas.getTimestamp("fecha_venta").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                ventasRecientes.add(new DashboardVenta(
                        String.format("#%04d", idVenta),
                        fechaHora,
                        formatoMoneda.format(rsUltimas.getDouble("total")),
                        formatearMetodoPago(rsUltimas.getString("metodo_pago")),
                        rsUltimas.getString("cajero"),
                        formatearProductos(rsUltimas.getInt("total_productos"), rsUltimas.getInt("tipos_productos")),
                        formatearEstadoVenta(rsUltimas.getString("estado"))
                ));
            }

            tablaUltimasVentas.setItems(ventasRecientes);

            // Gráfica de productos
            String sqlTop = "SELECT p.nombre, SUM(dv.cantidad) as total FROM detalle_venta dv JOIN ventas v ON dv.id_venta = v.id_venta JOIN productos p ON dv.id_producto = p.id_producto WHERE v.id_caja = ? AND DATE(v.fecha) = CURDATE() GROUP BY p.id_producto ORDER BY total DESC LIMIT 5";
            PreparedStatement psTop = con.prepareStatement(sqlTop);
            psTop.setInt(1, idCaja);
            ResultSet rsTop = psTop.executeQuery();

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            boolean hayTop = false;

            while (rsTop.next()) {
                hayTop = true;
                String nombre = rsTop.getString("nombre");
                int cantidad = rsTop.getInt("total");
                pieData.add(new PieChart.Data(nombre + " (" + cantidad + ")", cantidad));
            }

            graficaVentas.setData(pieData);
            graficaVentas.setVisible(hayTop);
            lblSinTop.setVisible(!hayTop);


        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    private boolean columnaExiste(Connection con, String tabla, String columna) {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tabla);
            ps.setString(2, columna);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tablaExiste(Connection con, String tabla) {
        String sql = "SELECT COUNT(*) FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tabla);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String formatearMetodoPago(String metodo) {
        if (metodo == null || metodo.isBlank()) return "Efectivo";
        return switch (metodo.trim().toUpperCase(Locale.ROOT)) {
            case "EFECTIVO" -> "Efectivo";
            case "TARJETA" -> "Tarjeta";
            case "TRANSFERENCIA" -> "Transferencia";
            case "MIXTO" -> "Mixto";
            case "MIXTO_USD" -> "Mixto USD";
            case "DOLARES" -> "Dolares";
            case "FIADO", "CREDITO" -> "Credito";
            default -> metodo.trim();
        };
    }

    private String formatearEstadoVenta(String estado) {
        if (estado == null || estado.isBlank()) return "Completada";
        return switch (estado.trim().toUpperCase(Locale.ROOT)) {
            case "COMPLETADA" -> "Completada";
            case "CANCELADA" -> "Cancelada";
            case "DEVUELTA" -> "Devuelta";
            case "PARCIALMENTE_DEVUELTA" -> "Parcial";
            default -> estado.trim();
        };
    }

    private String formatearProductos(int totalProductos, int tiposProductos) {
        if (totalProductos <= 0) return "0 prod.";
        String unidades = totalProductos == 1 ? "1 pza" : totalProductos + " pzas";
        String tipos = tiposProductos == 1 ? "1 tipo" : tiposProductos + " tipos";
        return unidades + " / " + tipos;
    }

    private static class DashboardVenta {
        private final String folio;
        private final String fechaHora;
        private final String total;
        private final String metodoPago;
        private final String cajero;
        private final String productos;
        private final String estado;

        private DashboardVenta(String folio, String fechaHora, String total, String metodoPago,
                               String cajero, String productos, String estado) {
            this.folio = folio;
            this.fechaHora = fechaHora;
            this.total = total;
            this.metodoPago = metodoPago;
            this.cajero = cajero;
            this.productos = productos;
            this.estado = estado;
        }

        public String getFolio() { return folio; }
        public String getFechaHora() { return fechaHora; }
        public String getTotal() { return total; }
        public String getMetodoPago() { return metodoPago; }
        public String getCajero() { return cajero; }
        public String getProductos() { return productos; }
        public String getEstado() { return estado; }
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
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    // Navegación
    @FXML private void irAVentas() { navegarConPermiso(PermisoService.Accion.ACCEDER_VENTAS, "/org/example/vista/Ventas.fxml"); }
    @FXML private void abrirInventario() { navegarConPermiso(PermisoService.Accion.ACCEDER_INVENTARIO, "/org/example/vista/Inventario.fxml");}
    @FXML private void irAEmpleados()  { navegarConPermiso(PermisoService.Accion.GESTIONAR_EMPLEADOS, "/org/example/vista/Empleados.fxml"); }
    @FXML private void irAClientes() {
        navegarConPermiso(PermisoService.Accion.ACCEDER_CLIENTES, "/org/example/vista/Clientes.fxml");
    }
    @FXML public void irAReportes() {
        navegarConPermiso(PermisoService.Accion.VER_REPORTES, "/org/example/vista/Reportes.fxml");
    }
    @FXML private void irACorteCaja() {
        navegarConPermiso(PermisoService.Accion.VER_CORTE_CAJA, "/org/example/vista/CorteCaja.fxml");
    }
    @FXML private void irAAuditoria() {
        navegarConPermiso(PermisoService.Accion.ACCEDER_AUDITORIA, "/org/example/vista/Auditoria.fxml");
    }
    @FXML private void irAOperaciones() {
        navegarConPermiso(PermisoService.Accion.ACCEDER_CONFIGURACION, "/org/example/vista/Operaciones.fxml");
    }
    @FXML private void irAConfiguracion() { navegarConPermiso(PermisoService.Accion.ACCEDER_CONFIGURACION, "/org/example/vista/Configuracion.fxml");}

    private void cambiarEscena(String fxmlPath) {
        org.example.servicio.NavigationService.cambiarEscena(lblFecha, fxmlPath);
    }

    @FXML
    public void btnCerrar() {
        org.example.servicio.NavigationService.cambiarSesion(lblFecha);
    }

    @FXML
    public void salirAplicacion() {
        AppExitService.salir(lblFecha);
    }

    public void abrirInventarioDesdeAfuera() {
        navegarConPermiso(PermisoService.Accion.ACCEDER_INVENTARIO, "/org/example/vista/Inventario.fxml");
    }
    private void navegarConPermiso(PermisoService.Accion accion, String ruta) {
        if (!PermisoService.puede(accion)) {
            mostrarAlerta("Acceso denegado", "No tienes permiso para acceder a este módulo.");
            return;
        }
        cambiarEscena(ruta);
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        org.example.servicio.DialogService.info(lblFecha, titulo, mensaje);
    }
}
