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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
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
    @FXML private VBox listaUltimasVentas;
    @FXML private VBox listaTopProductos;
    @FXML private Label lblSinVentas;
    @FXML private Label lblSinTop;
    @FXML private PieChart graficaVentas;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mostrarFecha();
        iniciarReloj();
        cargarDatosUsuario();
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
    }

    private void cargarDashboard() {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();

        try (Connection con = ConexionDB.getConexion()) {

            // 1. Ventas del día
            String sqlVentas = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM ventas WHERE id_caja = ? AND DATE(fecha) = CURDATE()";
            PreparedStatement psVentas = con.prepareStatement(sqlVentas);
            psVentas.setInt(1, idCaja);
            ResultSet rsVentas = psVentas.executeQuery();
            if (rsVentas.next()) {
                int numVentas = rsVentas.getInt(1);
                double totalVentas = rsVentas.getDouble(2);
                lblPedidosHoy.setText(String.valueOf(numVentas));
                lblVentasHoy.setText("$" + String.format("%.2f", totalVentas));
                lblVentasDelta.setText(numVentas == 0 ? "sin ventas aun" : numVentas + " tickets registrados");
            }

            // 2. Productos vendidos hoy
            String sqlProductos = "SELECT COALESCE(SUM(dv.cantidad), 0) FROM detalle_venta dv JOIN ventas v ON dv.id_venta = v.id_venta WHERE v.id_caja = ? AND DATE(v.fecha) = CURDATE()";
            PreparedStatement psProductos = con.prepareStatement(sqlProductos);
            psProductos.setInt(1, idCaja);
            ResultSet rsProductos = psProductos.executeQuery();
            if (rsProductos.next()) {
                lblProductosVendidos.setText(String.valueOf(rsProductos.getInt(1)));
            }

            // 3. Stock bajo
            String sqlStock = "SELECT COUNT(*) FROM productos WHERE stock <= stock_minimo AND activo = 1";
            PreparedStatement psStock = con.prepareStatement(sqlStock);
            ResultSet rsStock = psStock.executeQuery();
            if (rsStock.next()) {
                lblProductosBajos.setText(String.valueOf(rsStock.getInt(1)));
            }

            // 4. Últimas 5 ventas
            String sqlUltimas = "SELECT v.id_venta, v.fecha, v.total FROM ventas v WHERE v.id_caja = ? AND DATE(v.fecha) = CURDATE() ORDER BY v.fecha DESC LIMIT 15";
            PreparedStatement psUltimas = con.prepareStatement(sqlUltimas);
            psUltimas.setInt(1, idCaja);
            ResultSet rsUltimas = psUltimas.executeQuery();

            listaUltimasVentas.getChildren().clear();
            boolean hayVentas = false;

            while (rsUltimas.next()) {
                hayVentas = true;
                int idVenta = rsUltimas.getInt("id_venta");
                String hora = rsUltimas.getTimestamp("fecha").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm"));
                double total = rsUltimas.getDouble("total");

                HBox fila = new HBox();
                fila.setStyle("-fx-background-color: #FDF5E8; -fx-background-radius: 6; -fx-padding: 8 12;");

                Label lblHoraV = new Label(hora);
                lblHoraV.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 12px; -fx-min-width: 80;");

                Label lblFolio = new Label(String.format("#%04d", idVenta));
                lblFolio.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 12px; -fx-min-width: 60;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                Label lblTotal = new Label("$" + String.format("%.2f", total));
                lblTotal.setStyle("-fx-text-fill: #6B4226; -fx-font-weight: bold; -fx-font-size: 13px;");

                fila.getChildren().addAll(lblHoraV, lblFolio, spacer, lblTotal);
                listaUltimasVentas.getChildren().add(fila);
            }

            lblSinVentas.setVisible(!hayVentas);

            // 5. Gráfica pastel — top productos
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
            e.printStackTrace();
        }
    }

    @FXML
    private void irAVentas() {
        cambiarEscena("/org/example/vista/Ventas.fxml");
    }

    @FXML
    private void abrirInventario() {
        cambiarEscena("/org/example/vista/Inventario.fxml");
    }

    @FXML
    private void irAEmpleados() {
        cambiarEscena("/org/example/vista/Empleados.fxml");
    }

    @FXML
    private void irACorteCaja() {
        cambiarEscena("/org/example/vista/CorteCaja.fxml");
    }

    @FXML
    private void irAConfiguracion() {cambiarEscena("/org/example/vista/Configuracion.fxml");}

    @FXML
    public void irAReportes(ActionEvent actionEvent) {
        cambiarEscena("/org/example/vista/Reportes.fxml");
    }

    @FXML
    public void irAReportes() {
        cambiarEscena("/org/example/vista/Reportes.fxml");
    }

    private void cambiarEscena(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) lblFecha.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnCerrar(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Salir");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas salir?");
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) Platform.exit();
        });
    }

    public void abrirInventarioDesdeAfuera() {
        cambiarEscena("/org/example/vista/Inventario.fxml");
    }
}