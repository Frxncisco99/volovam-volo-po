package org.example.controlador;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.servicio.ReportePDFService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CorteCajaController {

    // ── Corte Actual ─────────────────────────────────────────────────────────
    @FXML private Label lblFechaHoy;
    @FXML private Label lblIdCaja;
    @FXML private Label lblCajero;
    @FXML private Label lblHoraApertura;
    @FXML private Label lblFondoInicial;
    @FXML private Label lblNumTickets;
    @FXML private Label lblTotalEfectivo;
    @FXML private Label lblTotalEntradas;
    @FXML private Label lblTotalSalidas;
    @FXML private Label lblDineroEsperado;
    @FXML private Label lblDiferencia;
    @FXML private Label lblEstadoDiferencia;
    @FXML private Label lblObsRequerida;
    @FXML private TextField txtDineroContado;
    @FXML private TextArea txtObservaciones;

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    // ── Tab Cajeros ───────────────────────────────────────────────────────────
    @FXML private TableView<String[]> tablaCajeros;
    @FXML private TableColumn<String[], String> colCajeroNombre;
    @FXML private TableColumn<String[], String> colCajeroTickets;
    @FXML private TableColumn<String[], String> colCajeroTotal;
    @FXML private TableColumn<String[], String> colCajeroPromedio;
    @FXML private Label lblTotalCombinado;

    // ── Tab Métodos de Pago ───────────────────────────────────────────────────
    @FXML private VBox vboxMetodosPago;
    @FXML private Label lblSinMetodos;
    @FXML private Label lblTotalMetodos;

    // ── Tab Historial ─────────────────────────────────────────────────────────
    @FXML private TableView<String[]> tablaHistorial;
    @FXML private TableColumn<String[], String> colHisId;
    @FXML private TableColumn<String[], String> colHisCajero;
    @FXML private TableColumn<String[], String> colHisCaja;
    @FXML private TableColumn<String[], String> colHisApertura;
    @FXML private TableColumn<String[], String> colHisCierre;
    @FXML private TableColumn<String[], String> colHisVentas;
    @FXML private TableColumn<String[], String> colHisEsperado;
    @FXML private TableColumn<String[], String> colHisReal;
    @FXML private TableColumn<String[], String> colHisDiferencia;
    @FXML private Label lblTotalCortes;

    // ── Estado interno ────────────────────────────────────────────────────────
    private double fondoInicial = 0;
    private double totalEfectivo = 0;
    private double totalEntradas = 0;
    private double totalSalidas = 0;
    private double dineroEsperado = 0;
    private final ReportePDFService pdf = new ReportePDFService();

    // Etiquetas de métodos de pago legibles
    private static final Map<String, String> LABELS_METODO = new LinkedHashMap<>();
    static {
        LABELS_METODO.put("EFECTIVO",      "Efectivo");
        LABELS_METODO.put("TARJETA",       "Tarjeta");
        LABELS_METODO.put("TRANSFERENCIA", "Transferencia");
        LABELS_METODO.put("MIXTO",         "Mixto");
        LABELS_METODO.put("FIADO",         "Fiado");
        LABELS_METODO.put("DOLARES",       "Dólares");
        LABELS_METODO.put("MIXTO_USD",     "Mixto USD");
    }

    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
        String fecha = LocalDateTime.now().format(fmt);
        lblFechaHoy.setText(fecha.substring(0, 1).toUpperCase() + fecha.substring(1));

        lblIdCaja.setText("Caja #" + sesion.getIdCaja());
        lblCajero.setText(sesion.getNombre());

        configurarTablasCajeros();
        configurarTablaHistorial();

        cargarResumen();
        cargarVentasPorCajero();
        cargarMetodosPago();
        cargarHistorial10();

        txtDineroContado.textProperty().addListener((obs, old, nuevo) -> calcularDiferencia(nuevo));
        lblObsRequerida.setVisible(false);
    }

    // ─── Configuración de columnas ─────────────────────────────────────────
    private void configurarTablasCajeros() {
        colCajeroNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        colCajeroTickets.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        colCajeroTotal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        colCajeroPromedio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));

        // Colorear diferencia positiva/negativa en tabla de cajeros
        tablaCajeros.setRowFactory(tv -> new TableRow<>());
    }

    private void configurarTablaHistorial() {
        colHisId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        colHisCajero.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        colHisCaja.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        colHisApertura.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        colHisCierre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));
        colHisVentas.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[5]));
        colHisEsperado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[6]));
        colHisReal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[7]));
        colHisDiferencia.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[8]));

        // Color rojo/verde en celda de diferencia
        colHisDiferencia.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.startsWith("-")) {
                    setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                } else if (item.equals("$0.00")) {
                    setStyle("-fx-text-fill: #3B6D11; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #1A6DB5; -fx-font-weight: bold;");
                }
            }
        });
    }

    // ─── Resumen principal ─────────────────────────────────────────────────
    private void cargarResumen() {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();
        try (Connection con = ConexionDB.getConexion()) {

            // Info de apertura
            String sqlCaja = "SELECT monto_inicial, fecha_apertura FROM caja WHERE id_caja = ?";
            PreparedStatement psCaja = con.prepareStatement(sqlCaja);
            psCaja.setInt(1, idCaja);
            ResultSet rsCaja = psCaja.executeQuery();
            if (rsCaja.next()) {
                fondoInicial = rsCaja.getDouble("monto_inicial");
                String horaApertura = rsCaja.getTimestamp("fecha_apertura")
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                lblFondoInicial.setText("$" + String.format("%.2f", fondoInicial));
                lblHoraApertura.setText(horaApertura);
            }

            // Total ventas (todos los métodos)
            String sqlVentas = "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM ventas WHERE id_caja = ?";
            PreparedStatement psVentas = con.prepareStatement(sqlVentas);
            psVentas.setInt(1, idCaja);
            ResultSet rsVentas = psVentas.executeQuery();
            if (rsVentas.next()) {
                lblNumTickets.setText(String.valueOf(rsVentas.getInt(1)));
                totalEfectivo = rsVentas.getDouble(2);
                lblTotalEfectivo.setText("$" + String.format("%.2f", totalEfectivo));
            }

            // Movimientos
            PreparedStatement psEnt = con.prepareStatement(
                    "SELECT COALESCE(SUM(monto), 0) FROM movimientos_caja WHERE id_caja = ? AND tipo = 'INGRESO'");
            psEnt.setInt(1, idCaja);
            ResultSet rsEnt = psEnt.executeQuery();
            if (rsEnt.next()) { totalEntradas = rsEnt.getDouble(1); lblTotalEntradas.setText("$" + String.format("%.2f", totalEntradas)); }

            PreparedStatement psSal = con.prepareStatement(
                    "SELECT COALESCE(SUM(monto), 0) FROM movimientos_caja WHERE id_caja = ? AND tipo = 'RETIRO'");
            psSal.setInt(1, idCaja);
            ResultSet rsSal = psSal.executeQuery();
            if (rsSal.next()) { totalSalidas = rsSal.getDouble(1); lblTotalSalidas.setText("$" + String.format("%.2f", totalSalidas)); }

            dineroEsperado = fondoInicial + totalEfectivo + totalEntradas - totalSalidas;
            lblDineroEsperado.setText("$" + String.format("%.2f", dineroEsperado));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Ventas por cajero ─────────────────────────────────────────────────
    private void cargarVentasPorCajero() {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();
        ObservableList<String[]> datos = FXCollections.observableArrayList();
        double totalGlobal = 0;

        try (Connection con = ConexionDB.getConexion()) {
            String sql = """
                SELECT u.nombre,
                       COUNT(v.id_venta)           AS tickets,
                       COALESCE(SUM(v.total), 0)   AS total
                FROM ventas v
                JOIN usuarios u ON v.id_usuario = u.id_usuario
                WHERE v.id_caja = ?
                GROUP BY u.id_usuario, u.nombre
                ORDER BY total DESC
                """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int tickets = rs.getInt("tickets");
                double total = rs.getDouble("total");
                double promedio = tickets > 0 ? total / tickets : 0;
                totalGlobal += total;
                datos.add(new String[]{
                        nombre,
                        String.valueOf(tickets),
                        "$" + String.format("%.2f", total),
                        "$" + String.format("%.2f", promedio)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tablaCajeros.setItems(datos);
        lblTotalCombinado.setText("$" + String.format("%.2f", totalGlobal));
    }

    // ─── Métodos de pago ───────────────────────────────────────────────────
    private void cargarMetodosPago() {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();
        vboxMetodosPago.getChildren().clear();
        double totalGeneral = 0;
        boolean hayDatos = false;

        try (Connection con = ConexionDB.getConexion()) {
            String sql = """
                SELECT p.tipo_pago,
                       COUNT(v.id_venta)           AS cantidad,
                       COALESCE(SUM(v.total), 0)   AS total
                FROM ventas v
                JOIN pagos p ON p.id_venta = v.id_venta
                WHERE v.id_caja = ?
                GROUP BY p.tipo_pago
                ORDER BY total DESC
                """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idCaja);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                hayDatos = true;
                String tipo = rs.getString("tipo_pago");
                int cantidad = rs.getInt("cantidad");
                double total = rs.getDouble("total");
                totalGeneral += total;

                String etiqueta = LABELS_METODO.getOrDefault(tipo, tipo);
                HBox fila = crearFilaMetodo(etiqueta, cantidad, total);
                vboxMetodosPago.getChildren().add(fila);
                vboxMetodosPago.getChildren().add(new Separator());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!hayDatos) {
            lblSinMetodos.setVisible(true);
            lblSinMetodos.setManaged(true);
        }
        lblTotalMetodos.setText("$" + String.format("%.2f", totalGeneral));
    }

    private HBox crearFilaMetodo(String etiqueta, int cantidad, double total) {
        HBox fila = new HBox();
        fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fila.setSpacing(8);

        // Ícono de punto
        Label punto = new Label("●");
        punto.setStyle("-fx-text-fill: #D4A843; -fx-font-size: 10px;");

        Label lblNombre = new Label(etiqueta);
        lblNombre.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 13px; -fx-min-width: 160;");

        Label lblCantidad = new Label(cantidad + " venta" + (cantidad != 1 ? "s" : ""));
        lblCantidad.setStyle("-fx-text-fill: #A0856A; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label lblMonto = new Label("$" + String.format("%.2f", total));
        lblMonto.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #6B4226;");

        fila.getChildren().addAll(punto, lblNombre, lblCantidad, spacer, lblMonto);
        return fila;
    }

    // ─── Historial de cortes ───────────────────────────────────────────────
    @FXML
    public void cargarHistorial10() { cargarHistorial(10); }
    @FXML
    public void cargarHistorial30() { cargarHistorial(30); }
    @FXML
    public void cargarHistorialTodos() { cargarHistorial(0); }

    private void cargarHistorial(int limite) {
        ObservableList<String[]> datos = FXCollections.observableArrayList();
        String sql = """
                SELECT cc.id_corte,
                       u.nombre         AS cajero,
                       cc.id_caja,
                       cc.fecha_apertura,
                       cc.fecha_cierre,
                       cc.total_ventas,
                       cc.dinero_esperado,
                       cc.dinero_real,
                       cc.diferencia
                FROM corte_caja cc
                JOIN usuarios u ON u.id_usuario = cc.id_usuario
                ORDER BY cc.fecha_cierre DESC
                """ + (limite > 0 ? " LIMIT " + limite : "");

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

            while (rs.next()) {
                double dif = rs.getDouble("diferencia");
                String difStr = (dif >= 0 ? "$" : "-$") + String.format("%.2f", Math.abs(dif));
                if (dif > 0) difStr = "+$" + String.format("%.2f", dif);

                datos.add(new String[]{
                        String.valueOf(rs.getInt("id_corte")),
                        rs.getString("cajero"),
                        "Caja #" + rs.getInt("id_caja"),
                        rs.getTimestamp("fecha_apertura").toLocalDateTime().format(fmt),
                        rs.getTimestamp("fecha_cierre").toLocalDateTime().format(fmt),
                        "$" + String.format("%.2f", rs.getDouble("total_ventas")),
                        "$" + String.format("%.2f", rs.getDouble("dinero_esperado")),
                        "$" + String.format("%.2f", rs.getDouble("dinero_real")),
                        difStr
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tablaHistorial.setItems(datos);
        lblTotalCortes.setText(datos.size() + " corte(s) encontrados");
    }

    // ─── Diferencia en tiempo real ─────────────────────────────────────────
    private void calcularDiferencia(String texto) {
        try {
            double contado = Double.parseDouble(texto);
            double diferencia = contado - dineroEsperado;
            lblDiferencia.setText("$" + String.format("%.2f", diferencia));

            if (diferencia == 0) {
                lblDiferencia.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #3B6D11;");
                lblEstadoDiferencia.setText("✓ Todo correcto");
                lblEstadoDiferencia.setStyle("-fx-text-fill: #3B6D11; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblObsRequerida.setVisible(false);
            } else if (diferencia < 0) {
                lblDiferencia.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #C0392B;");
                lblEstadoDiferencia.setText("⚠ Falta dinero");
                lblEstadoDiferencia.setStyle("-fx-text-fill: #C0392B; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblObsRequerida.setVisible(true);
            } else {
                lblDiferencia.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A6DB5;");
                lblEstadoDiferencia.setText("ℹ Sobra dinero");
                lblEstadoDiferencia.setStyle("-fx-text-fill: #1A6DB5; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblObsRequerida.setVisible(true);
            }
        } catch (NumberFormatException e) {
            lblDiferencia.setText("$0.00");
            lblEstadoDiferencia.setText("");
        }
    }

    // ─── Cerrar caja ───────────────────────────────────────────────────────
    @FXML
    public void handleCerrarCaja() {
        String textoContado = txtDineroContado.getText().trim();
        if (textoContado.isEmpty()) { mostrarAlerta("Campo vacío", "Ingresa el dinero contado."); return; }

        double contado;
        try { contado = Double.parseDouble(textoContado); }
        catch (NumberFormatException e) { mostrarAlerta("Error", "Ingresa un número válido."); return; }

        double diferencia = contado - dineroEsperado;

        if (diferencia != 0 && txtObservaciones.getText().trim().isEmpty()) {
            mostrarAlerta("Observaciones requeridas",
                    "Hay una diferencia de $" + String.format("%.2f", diferencia) + ". Debes escribir una observacion.");
            txtObservaciones.requestFocus();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar caja");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Seguro que deseas cerrar la caja? Esta accion no se puede deshacer.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) cerrarCajaEnBD(contado, diferencia);
        });
    }

    private void cerrarCajaEnBD(double montoReal, double diferencia) {
        int idCaja = SesionUsuario.getInstancia().getIdCaja();
        int idUsuario = SesionUsuario.getInstancia().getIdUsuario();

        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);

            PreparedStatement psCerrar = con.prepareStatement(
                    "UPDATE caja SET estado = 'cerrada', fecha_cierre = NOW(), monto_final = ? WHERE id_caja = ?");
            psCerrar.setDouble(1, montoReal);
            psCerrar.setInt(2, idCaja);
            psCerrar.executeUpdate();

            PreparedStatement psCorte = con.prepareStatement(
                    "INSERT INTO corte_caja (id_caja, id_usuario, fecha_apertura, fecha_cierre, fondo_inicial, total_ventas, total_entradas, total_salidas, dinero_esperado, dinero_real, diferencia, observaciones) " +
                            "VALUES (?, ?, (SELECT fecha_apertura FROM caja WHERE id_caja = ?), NOW(), ?, ?, ?, ?, ?, ?, ?, ?)");
            psCorte.setInt(1, idCaja);
            psCorte.setInt(2, idUsuario);
            psCorte.setInt(3, idCaja);
            psCorte.setDouble(4, fondoInicial);
            psCorte.setDouble(5, totalEfectivo);
            psCorte.setDouble(6, totalEntradas);
            psCorte.setDouble(7, totalSalidas);
            psCorte.setDouble(8, dineroEsperado);
            psCorte.setDouble(9, montoReal);
            psCorte.setDouble(10, diferencia);
            psCorte.setString(11, txtObservaciones.getText().trim());
            psCorte.executeUpdate();
            con.commit();

            // PDF
            String fechaHoy = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Guardar corte de caja");
            fileChooser.setInitialFileName("Corte_Caja_" + fechaHoy + ".pdf");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
            java.io.File docFolder = new java.io.File(System.getProperty("user.home") + "/Documents");
            fileChooser.setInitialDirectory(docFolder.exists() ? docFolder : new java.io.File(System.getProperty("user.home")));

            Stage stage = (Stage) lblFechaHoy.getScene().getWindow();
            java.io.File destino = fileChooser.showSaveDialog(stage);

            if (destino != null) {
                SesionUsuario sesion = SesionUsuario.getInstancia();
                pdf.generarCorteCaja(
                        sesion.getNombre(), "Caja #" + sesion.getIdCaja(),
                        lblHoraApertura.getText(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        fondoInicial, totalEfectivo, totalEntradas, totalSalidas,
                        dineroEsperado, montoReal, diferencia,
                        Integer.parseInt(lblNumTickets.getText()),
                        txtObservaciones.getText().trim(),
                        destino.getAbsolutePath());
            }

            SesionUsuario.getInstancia().setIdCaja(0);
            mostrarInfo("Caja cerrada", "El corte se registro correctamente." +
                    (destino != null ? "\nPDF guardado en: " + destino.getName() : "\nPDF no guardado."));
            Platform.exit();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cerrar la caja.");
        }
    }

    // ─── Exportar PDF (sin cerrar) ─────────────────────────────────────────
    @FXML
    private void exportarCortePDF() {
        try {
            String fechaHoy = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Guardar corte de caja");
            fileChooser.setInitialFileName("Reporte_Corte_Caja_" + fechaHoy + ".pdf");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
            java.io.File docFolder = new java.io.File(System.getProperty("user.home") + "/Documents");
            fileChooser.setInitialDirectory(docFolder.exists() ? docFolder : new java.io.File(System.getProperty("user.home")));

            Stage stage = (Stage) lblFechaHoy.getScene().getWindow();
            java.io.File destino = fileChooser.showSaveDialog(stage);
            if (destino == null) return;

            double contado = 0;
            try { contado = Double.parseDouble(txtDineroContado.getText().trim()); } catch (Exception ignored) {}

            SesionUsuario sesion = SesionUsuario.getInstancia();
            pdf.generarCorteCaja(
                    sesion.getNombre(), "Caja #" + sesion.getIdCaja(),
                    lblHoraApertura.getText(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    fondoInicial, totalEfectivo, totalEntradas, totalSalidas,
                    dineroEsperado, contado, contado - dineroEsperado,
                    Integer.parseInt(lblNumTickets.getText()),
                    txtObservaciones.getText().trim(),
                    destino.getAbsolutePath());

            mostrarInfo("PDF generado", "Corte guardado en:\n" + destino.getName());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el PDF.");
        }
    }

    // ─── Navegación ────────────────────────────────────────────────────────
    @FXML public void irADashboard() { navegar("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML public void irAVentas()    { navegar("/org/example/vista/Ventas.fxml"); }
    @FXML public void irAEmpleados() { navegar("/org/example/vista/Empleados.fxml"); }

    @FXML
    public void btnCerrar() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Salir"); alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas salir?");
        alerta.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) Platform.exit(); });
    }

    private void navegar(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) lblFechaHoy.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void irAReportes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Reportes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void irAInventario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Inventario.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void irAClientes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Clientes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── Alertas ───────────────────────────────────────────────────────────
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }


}
