package org.example.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Muestra el historial de movimientos de inventario para un producto.
 * Necesita la tabla: movimientos_inventario (id, id_producto, tipo, cantidad,
 *                    motivo, id_usuario, fecha)
 * Si la tabla no existe aún, muestra mensaje informativo sin crash.
 */
public class HistorialMovimientosController {

    // ── Header ─────────────────────────────────────────────────────────────
    @FXML private Label lblNombreProducto;
    @FXML private Label lblStockActual;

    // ── Tabla ───────────────────────────────────────────────────────────────
    @FXML private TableView<MovimientoRow>          tablaHistorial;
    @FXML private TableColumn<MovimientoRow, String> colFecha;
    @FXML private TableColumn<MovimientoRow, String> colTipo;
    @FXML private TableColumn<MovimientoRow, String> colCantidad;
    @FXML private TableColumn<MovimientoRow, String> colMotivo;
    @FXML private TableColumn<MovimientoRow, String> colUsuario;

    // ── Totales rápidos ─────────────────────────────────────────────────────
    @FXML private Label lblTotalEntradas;
    @FXML private Label lblTotalSalidas;
    @FXML private Label lblTotalVentas;

    private Producto producto;
    private final ObservableList<MovimientoRow> datos = FXCollections.observableArrayList();

    // ── API ─────────────────────────────────────────────────────────────────

    /** Llamado desde InventarioController justo antes de mostrar el stage. */
    public void setProducto(Producto p) {
        this.producto = p;
        lblNombreProducto.setText(p.getNombre());
        lblStockActual.setText("Stock actual: " + p.getStock() + " unidades");
        cargarHistorial();
    }

    @FXML
    public void initialize() {
        // Columnas
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));

        // Badge de color en la columna Tipo
        colTipo.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) { setGraphic(null); return; }
                switch (tipo) {
                    case "ENTRADA"  -> badge.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #1a5c2e; " +
                            "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                    case "VENTA"    -> badge.setStyle("-fx-background-color: #fde8e8; -fx-text-fill: #a83232; " +
                            "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                    case "AJUSTE"   -> badge.setStyle("-fx-background-color: #e8f3fb; -fx-text-fill: #1a6fa8; " +
                            "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                    default         -> badge.setStyle("-fx-background-color: #e8f0f8; -fx-text-fill: #3a5a7a; " +
                            "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px;");
                }
                badge.setText(tipo);
                setGraphic(badge);
            }
        });

        // Cantidad con + / -
        colCantidad.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String cant, boolean empty) {
                super.updateItem(cant, empty);
                if (empty || cant == null) { setText(null); setStyle(""); return; }
                setText(cant);
                String color = cant.startsWith("+") ? "#1a5c2e" : "#a83232";
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;");
            }
        });

        tablaHistorial.setItems(datos);
    }

    // ── Carga ────────────────────────────────────────────────────────────────

    private void cargarHistorial() {
        datos.clear();
        int entradas = 0, salidas = 0, ventas = 0;

        // ── Movimientos de inventario (ajustes manuales, entradas) ──────────
        String sqlMov = """
            SELECT
                DATE_FORMAT(m.fecha,'%d/%m/%Y %H:%i') AS fecha,
                m.tipo,
                m.cantidad,
                COALESCE(m.motivo, '—') AS motivo,
                COALESCE(u.nombre, 'Sistema') AS usuario
            FROM movimientos_inventario m
            LEFT JOIN usuarios u ON m.id_usuario = u.id_usuario
            WHERE m.id_producto = ?
            ORDER BY m.fecha DESC
            LIMIT 200
            """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlMov)) {
            ps.setInt(1, producto.getIdProducto());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tipo = rs.getString("tipo");
                int cant    = rs.getInt("cantidad");
                datos.add(new MovimientoRow(
                        rs.getString("fecha"),
                        tipo,
                        (tipo.equals("ENTRADA") || tipo.equals("AJUSTE_ALTA") ? "+" : "-") + cant,
                        rs.getString("motivo"),
                        rs.getString("usuario")
                ));
                if (tipo.equals("ENTRADA") || tipo.equals("AJUSTE_ALTA")) entradas += cant;
                else salidas += cant;
            }
        } catch (Exception e) {
            // La tabla puede no existir aún — no crashear
        }

        // ── Ventas (desde detalle_venta) ────────────────────────────────────
        String sqlVentas = """
            SELECT
                DATE_FORMAT(v.fecha,'%d/%m/%Y %H:%i') AS fecha,
                dv.cantidad,
                COALESCE(u.nombre, 'Cajero') AS usuario,
                CONCAT('Venta #', LPAD(v.id_venta,4,'0')) AS motivo
            FROM detalle_venta dv
            JOIN ventas v ON dv.id_venta = v.id_venta
            LEFT JOIN usuarios u ON v.id_usuario = u.id_usuario
            WHERE dv.id_producto = ?
            ORDER BY v.fecha DESC
            LIMIT 100
            """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlVentas)) {
            ps.setInt(1, producto.getIdProducto());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int cant = rs.getInt("cantidad");
                ventas  += cant;
                datos.add(new MovimientoRow(
                        rs.getString("fecha"),
                        "VENTA",
                        "-" + cant,
                        rs.getString("motivo"),
                        rs.getString("usuario")
                ));
            }
        } catch (Exception e) {
            // continuar sin ventas
        }

        // Ordenar por fecha desc (mezcla de las dos consultas)
        datos.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));

        // Totales
        lblTotalEntradas.setText("+" + entradas);
        lblTotalSalidas.setText("-" + (salidas));
        lblTotalVentas.setText("-" + ventas);

        if (datos.isEmpty()) {
            tablaHistorial.setPlaceholder(new Label("Sin movimientos registrados para este producto"));
        }
    }

    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) tablaHistorial.getScene().getWindow();
        stage.close();
    }

    // ── DTO ──────────────────────────────────────────────────────────────────

    /** Fila de la tabla — JavaBean simple para PropertyValueFactory. */
    public static class MovimientoRow {
        private final String fecha, tipo, cantidad, motivo, usuario;
        public MovimientoRow(String fecha, String tipo, String cantidad,
                             String motivo, String usuario) {
            this.fecha    = fecha;
            this.tipo     = tipo;
            this.cantidad = cantidad;
            this.motivo   = motivo;
            this.usuario  = usuario;
        }
        public String getFecha()    { return fecha;    }
        public String getTipo()     { return tipo;     }
        public String getCantidad() { return cantidad; }
        public String getMotivo()   { return motivo;   }
        public String getUsuario()  { return usuario;  }
    }
}
