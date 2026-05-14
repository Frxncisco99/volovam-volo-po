package org.example.controlador;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.servicio.MarcaService;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientesController {

    @FXML private FlowPane flowClientes;
    @FXML private TextField txtBuscar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;
    @FXML private Label lblConteoActivos;
    @FXML private Label lblStatTotal;
    @FXML private Label lblStatAdeudo;
    @FXML private Label lblStatConSaldo;

    private final List<ClienteRow> clientes = new ArrayList<>();

    @FXML
    public void initialize() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        String nombre = sesion.getNombre() == null ? "Usuario" : sesion.getNombre();
        lblNombreUsuario.setText(nombre);
        lblRolUsuario.setText(sesion.getRol());
        lblAvatarIniciales.setText(iniciales(nombre));

        txtBuscar.textProperty().addListener((obs, old, nuevo) -> renderClientes());
        cargarClientes();
    }

    private void cargarClientes() {
        clientes.clear();
        String sql = """
                SELECT id_cliente, nombre, telefono, limite_credito, saldo_actual
                FROM clientes
                WHERE activo = 1 AND nombre LIKE ? AND nombre != 'Publico General'
                ORDER BY nombre
                """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                clientes.add(new ClienteRow(
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getDouble("limite_credito"),
                        rs.getDouble("saldo_actual")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        actualizarStats();
        renderClientes();
    }

    private void actualizarStats() {
        double totalAdeudo = clientes.stream().mapToDouble(ClienteRow::saldo).sum();
        long conSaldo = clientes.stream().filter(c -> c.saldo() > 0).count();
        lblStatTotal.setText(String.valueOf(clientes.size()));
        lblStatAdeudo.setText("$" + String.format("%.2f", totalAdeudo));
        lblStatConSaldo.setText(String.valueOf(conSaldo));
    }

    private void renderClientes() {
        String filtro = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        List<ClienteRow> filtrados = clientes.stream()
                .filter(c -> filtro.isEmpty()
                        || c.nombre().toLowerCase().contains(filtro)
                        || (c.telefono() != null && c.telefono().toLowerCase().contains(filtro)))
                .toList();
        lblConteoActivos.setText(filtrados.size() + " clientes activos");
        flowClientes.getChildren().clear();
        for (ClienteRow cliente : filtrados) {
            flowClientes.getChildren().add(crearCard(cliente));
        }
        if (filtrados.isEmpty()) {
            Label vacio = new Label("No hay clientes para mostrar");
            vacio.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 13px; -fx-padding: 20;");
            flowClientes.getChildren().add(vacio);
        }
    }

    private VBox crearCard(ClienteRow cliente) {
        double disponible = cliente.limite() - cliente.saldo();
        String estado = estadoCliente(cliente);
        String estadoColor = switch (estado) {
            case "Limite al maximo" -> "#8B1A1A";
            case "Con adeudo" -> "#C0392B";
            default -> "#2E7D32";
        };

        VBox card = new VBox();
        card.setPrefWidth(295);
        card.setMaxWidth(295);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E8DDD0; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian,rgba(61,31,13,0.10),12,0,0,3);");

        StackPane header = new StackPane();
        header.setPrefHeight(82);
        header.setStyle("-fx-background-color: linear-gradient(to bottom right, #3D1F0D, #6B4226); -fx-background-radius: 8 8 0 0;");

        Label badge = new Label(estado);
        badge.setStyle("-fx-background-color: " + estadoColor + "22; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 14; -fx-padding: 4 10;");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10));

        Label avatar = new Label(iniciales(cliente.nombre()));
        avatar.setPrefSize(56, 56);
        avatar.setMaxSize(56, 56);
        avatar.setStyle("-fx-background-color: " + colorAvatar(cliente.nombre()) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 28; -fx-border-color: rgba(255,255,255,0.35); -fx-border-width: 2; -fx-border-radius: 28; -fx-alignment: center;");
        StackPane.setAlignment(avatar, Pos.BOTTOM_LEFT);
        StackPane.setMargin(avatar, new Insets(0, 0, -24, 16));
        header.getChildren().addAll(badge, avatar);

        VBox body = new VBox(8);
        body.setPadding(new Insets(30, 16, 12, 16));

        Label nombre = new Label(cliente.nombre());
        nombre.setWrapText(true);
        nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2C1A0E;");

        HBox telefono = new HBox(7);
        telefono.setAlignment(Pos.CENTER_LEFT);
        FontIcon phone = new FontIcon("fas-phone");
        phone.setIconSize(12);
        phone.setIconColor(javafx.scene.paint.Color.web("#7A5535"));
        Label tel = new Label(cliente.telefono() == null || cliente.telefono().isBlank() ? "Sin telefono" : cliente.telefono());
        tel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7A5535;");
        telefono.getChildren().addAll(phone, tel);

        VBox credito = new VBox(4);
        credito.setStyle("-fx-background-color: #F5EFE6; -fx-background-radius: 8; -fx-padding: 10;");
        credito.getChildren().addAll(
                lineaDato("Limite", "$" + String.format("%.2f", cliente.limite()), "#0d3d5e"),
                lineaDato("Saldo", "$" + String.format("%.2f", cliente.saldo()), cliente.saldo() > 0 ? "#C0392B" : "#2E7D32"),
                lineaDato("Disponible", "$" + String.format("%.2f", disponible), disponible <= 0 && cliente.limite() > 0 ? "#8B1A1A" : "#2E7D32")
        );

        HBox acciones1 = new HBox(8);
        Button btnEditar = boton("Editar", "#1a6fa8", "white");
        Button btnHistorial = boton("Historial", "#F5ECD7", "#6B4226");
        btnEditar.setOnAction(e -> mostrarDialogoCliente(cliente));
        btnHistorial.setOnAction(e -> mostrarHistorial(cliente));
        HBox.setHgrow(btnEditar, Priority.ALWAYS);
        HBox.setHgrow(btnHistorial, Priority.ALWAYS);
        btnEditar.setMaxWidth(Double.MAX_VALUE);
        btnHistorial.setMaxWidth(Double.MAX_VALUE);
        acciones1.getChildren().addAll(btnEditar, btnHistorial);

        HBox acciones2 = new HBox(8);
        Button btnPago = boton("Registrar pago", "#2E7D32", "white");
        Button btnDesactivar = boton("Desactivar", "#FDE8E8", "#B91C1C");
        btnPago.setOnAction(e -> mostrarPago(cliente));
        btnPago.setDisable(cliente.saldo() <= 0);
        btnDesactivar.setOnAction(e -> desactivarCliente(cliente));
        HBox.setHgrow(btnPago, Priority.ALWAYS);
        HBox.setHgrow(btnDesactivar, Priority.ALWAYS);
        btnPago.setMaxWidth(Double.MAX_VALUE);
        btnDesactivar.setMaxWidth(Double.MAX_VALUE);
        acciones2.getChildren().addAll(btnPago, btnDesactivar);

        body.getChildren().addAll(nombre, telefono, credito, acciones1, acciones2);
        card.getChildren().addAll(header, body);
        return card;
    }

    private HBox lineaDato(String etiqueta, String valor, String color) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label left = new Label(etiqueta);
        left.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 11px;");
        Label right = new Label(valor);
        right.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(left, spacer, right);
        return row;
    }

    private Button boton(String texto, String fondo, String color) {
        Button btn = new Button(texto);
        btn.setPrefHeight(32);
        btn.setStyle("-fx-background-color: " + fondo + "; -fx-text-fill: " + color + "; -fx-background-radius: 8; -fx-padding: 6 10; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: bold;");
        return btn;
    }

    @FXML
    public void handleNuevoCliente() {
        mostrarDialogoCliente(null);
    }

    private void mostrarDialogoCliente(ClienteRow cliente) {
        boolean nuevo = cliente == null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(nuevo ? "Nuevo cliente" : "Editar cliente");

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 20; -fx-min-width: 380;");

        TextField txtNombre = new TextField(nuevo ? "" : cliente.nombre());
        TextField txtTelefono = new TextField(nuevo ? "" : cliente.telefono());
        TextField txtLimite = new TextField(nuevo || cliente.limite() <= 0 ? "" : String.valueOf(cliente.limite()));

        txtNombre.setPromptText("Nombre completo");
        txtTelefono.setPromptText("Telefono a 10 digitos");
        txtLimite.setPromptText("Limite de credito");
        aplicarEstiloCampo(txtNombre);
        aplicarEstiloCampo(txtTelefono);
        aplicarEstiloCampo(txtLimite);

        txtTelefono.textProperty().addListener((obs, old, nuevoTexto) -> {
            if (!nuevoTexto.matches("\\d{0,10}")) txtTelefono.setText(old);
        });

        contenido.getChildren().addAll(
                new Label("Nombre"), txtNombre,
                new Label("Telefono"), txtTelefono,
                new Label("Limite de credito"), txtLimite
        );
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(respuesta -> {
            if (respuesta != ButtonType.OK) return;
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            double limite;
            try {
                limite = txtLimite.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtLimite.getText().trim());
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El limite de credito debe ser numerico.");
                return;
            }
            if (nombre.isEmpty()) {
                mostrarAlerta("Error", "El nombre es obligatorio.");
                return;
            }
            if (limite < 0) {
                mostrarAlerta("Error", "El limite de credito debe ser mayor o igual a 0.");
                return;
            }
            if (nuevo) insertarCliente(nombre, telefono, limite);
            else actualizarCliente(cliente.id(), nombre, telefono, limite);
        });
    }

    private void aplicarEstiloCampo(TextField field) {
        field.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-padding: 8;");
    }

    private void insertarCliente(String nombre, String telefono, double limite) {
        String sql = "INSERT INTO clientes (nombre, telefono, limite_credito) VALUES (?, ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setDouble(3, limite);
            ps.executeUpdate();
            cargarClientes();
            mostrarInfo("Exito", "Cliente registrado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo registrar el cliente.");
        }
    }

    private void actualizarCliente(int id, String nombre, String telefono, double limite) {
        String sql = "UPDATE clientes SET nombre = ?, telefono = ?, limite_credito = ? WHERE id_cliente = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setDouble(3, limite);
            ps.setInt(4, id);
            ps.executeUpdate();
            cargarClientes();
            mostrarInfo("Exito", "Cliente actualizado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo actualizar el cliente.");
        }
    }

    private void mostrarPago(ClienteRow cliente) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Registrar pago");
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 20; -fx-min-width: 360;");
        TextField txtMonto = new TextField();
        TextField txtConcepto = new TextField("Abono manual");
        txtMonto.setPromptText("Monto");
        txtConcepto.setPromptText("Concepto");
        aplicarEstiloCampo(txtMonto);
        aplicarEstiloCampo(txtConcepto);
        contenido.getChildren().addAll(
                new Label("Cliente: " + cliente.nombre()),
                new Label("Saldo actual: $" + String.format("%.2f", cliente.saldo())),
                new Label("Monto"), txtMonto,
                new Label("Concepto"), txtConcepto
        );
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK) return;
            try {
                double monto = Double.parseDouble(txtMonto.getText().trim());
                if (monto <= 0) {
                    mostrarAlerta("Error", "El monto debe ser mayor a 0.");
                    return;
                }
                if (monto > cliente.saldo()) {
                    mostrarAlerta("Error", "El pago no puede ser mayor al saldo.");
                    return;
                }
                registrarPago(cliente, monto, txtConcepto.getText().trim());
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Ingresa un monto valido.");
            }
        });
    }

    private void registrarPago(ClienteRow cliente, double monto, String concepto) {
        String nota = concepto == null || concepto.isBlank() ? "Abono manual" : concepto;
        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement psSaldo = con.prepareStatement("UPDATE clientes SET saldo_actual = saldo_actual - ? WHERE id_cliente = ?");
                 PreparedStatement psPago = con.prepareStatement("INSERT INTO pagos_cliente (id_cliente, monto, tipo, notas) VALUES (?, ?, 'ABONO', ?)");
                 PreparedStatement psMovimiento = con.prepareStatement("INSERT INTO movimientos_caja (id_caja, tipo, monto, motivo, id_usuario) VALUES (?, 'INGRESO', ?, ?, ?)")) {
                psSaldo.setDouble(1, monto);
                psSaldo.setInt(2, cliente.id());
                psSaldo.executeUpdate();

                psPago.setInt(1, cliente.id());
                psPago.setDouble(2, monto);
                psPago.setString(3, nota);
                psPago.executeUpdate();

                psMovimiento.setInt(1, SesionUsuario.getInstancia().getIdCaja());
                psMovimiento.setDouble(2, monto);
                psMovimiento.setString(3, "Pago de cliente: " + cliente.nombre());
                psMovimiento.setInt(4, SesionUsuario.getInstancia().getIdUsuario());
                psMovimiento.executeUpdate();
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
            cargarClientes();
            mostrarInfo("Pago registrado", "Se registro el pago de $" + String.format("%.2f", monto) + ".");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo registrar el pago.");
        }
    }

    private void mostrarHistorial(ClienteRow cliente) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Historial de pagos - " + cliente.nombre());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<PagoRow> tabla = new TableView<>();
        TableColumn<PagoRow, String> colFecha = new TableColumn<>("Fecha");
        TableColumn<PagoRow, String> colTipo = new TableColumn<>("Tipo");
        TableColumn<PagoRow, String> colMonto = new TableColumn<>("Monto");
        TableColumn<PagoRow, String> colNotas = new TableColumn<>("Concepto");
        colFecha.setCellValueFactory(data -> data.getValue().fechaProperty());
        colTipo.setCellValueFactory(data -> data.getValue().tipoProperty());
        colMonto.setCellValueFactory(data -> data.getValue().montoProperty());
        colNotas.setCellValueFactory(data -> data.getValue().notasProperty());
        colFecha.setPrefWidth(145);
        colTipo.setPrefWidth(90);
        colMonto.setPrefWidth(95);
        colNotas.setPrefWidth(220);
        tabla.getColumns().addAll(colFecha, colTipo, colMonto, colNotas);
        tabla.setItems(cargarHistorial(cliente.id()));
        tabla.setPrefHeight(380);
        dialog.getDialogPane().setContent(tabla);
        dialog.showAndWait();
    }

    private ObservableList<PagoRow> cargarHistorial(int idCliente) {
        ObservableList<PagoRow> pagos = FXCollections.observableArrayList();
        String sql = "SELECT tipo, monto, fecha, notas FROM pagos_cliente WHERE id_cliente = ? ORDER BY fecha DESC LIMIT 100";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String fecha = rs.getTimestamp("fecha").toLocalDateTime().format(fmt);
                pagos.add(new PagoRow(fecha, rs.getString("tipo"), "$" + String.format("%.2f", rs.getDouble("monto")), rs.getString("notas")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pagos;
    }

    private void desactivarCliente(ClienteRow cliente) {
        if (cliente.saldo() > 0) {
            mostrarAlerta("No permitido", "No se puede desactivar un cliente con adeudo pendiente.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Desactivar cliente");
        confirm.setHeaderText(null);
        confirm.setContentText("Seguro que deseas desactivar a " + cliente.nombre() + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        String sql = "UPDATE clientes SET activo = 0 WHERE id_cliente = ? AND nombre != 'Publico General'";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cliente.id());
            ps.executeUpdate();
            cargarClientes();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo desactivar el cliente.");
        }
    }

    private String estadoCliente(ClienteRow cliente) {
        if (cliente.limite() > 0 && cliente.saldo() >= cliente.limite()) return "Limite al maximo";
        if (cliente.saldo() > 0) return "Con adeudo";
        return "Sin adeudo";
    }

    private String iniciales(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) return "CL";
        String[] partes = limpio.split("\\s+");
        if (partes.length > 1) return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
        return limpio.substring(0, Math.min(2, limpio.length())).toUpperCase();
    }

    private String colorAvatar(String nombre) {
        String[] colores = {"#1a6fa8", "#6B4226", "#2E7D32", "#8B5CF6", "#D97706", "#C0392B"};
        int index = Math.abs((nombre == null ? "" : nombre).hashCode()) % colores.length;
        return colores[index];
    }

    private void registrarLogout() {
        String sql = "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, detalle) VALUES (?, 'LOGOUT', 'usuarios', ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int idUsuario = SesionUsuario.getInstancia().getIdUsuario();
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setString(3, "Cierre de sesion: " + SesionUsuario.getInstancia().getNombre());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void irADashboard() { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_REPORTES, "/org/example/vista/MenuPrincipal.fxml"); }
    @FXML public void irAVentas() { navegar("/org/example/vista/Ventas.fxml"); }
    @FXML private void irAInventario() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_INVENTARIO, "/org/example/vista/Inventario.fxml"); }
    @FXML public void irAEmpleados() { navegarConPermiso(org.example.servicio.PermisoService.Accion.GESTIONAR_EMPLEADOS, "/org/example/vista/Empleados.fxml"); }
    @FXML public void irAReportes() { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_REPORTES, "/org/example/vista/Reportes.fxml"); }
    @FXML public void irACorteCaja() { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_CORTE_CAJA, "/org/example/vista/CorteCaja.fxml"); }
    @FXML private void irAAuditoria() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_AUDITORIA, "/org/example/vista/Auditoria.fxml"); }
    @FXML private void irAConfiguracion() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_CONFIGURACION, "/org/example/vista/Configuracion.fxml"); }

    @FXML
    public void btnCerrar() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Cambiar sesion");
        a.setHeaderText(null);
        a.setContentText("Seguro que deseas cambiar de sesion?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                registrarLogout();
                navegar("/org/example/vista/Login.fxml");
            }
        });
    }

    private void navegar(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            MarcaService.aplicar(root);
            Stage stage = (Stage) flowClientes.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navegarConPermiso(org.example.servicio.PermisoService.Accion accion, String ruta) {
        if (!org.example.servicio.PermisoService.puede(accion)) {
            mostrarAlerta("Acceso denegado", "El cajero solo puede acceder al modulo de ventas.");
            return;
        }
        navegar(ruta);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private record ClienteRow(int id, String nombre, String telefono, double limite, double saldo) {}

    private static class PagoRow {
        private final javafx.beans.property.SimpleStringProperty fecha;
        private final javafx.beans.property.SimpleStringProperty tipo;
        private final javafx.beans.property.SimpleStringProperty monto;
        private final javafx.beans.property.SimpleStringProperty notas;

        PagoRow(String fecha, String tipo, String monto, String notas) {
            this.fecha = new javafx.beans.property.SimpleStringProperty(fecha);
            this.tipo = new javafx.beans.property.SimpleStringProperty(tipo);
            this.monto = new javafx.beans.property.SimpleStringProperty(monto);
            this.notas = new javafx.beans.property.SimpleStringProperty(notas == null ? "" : notas);
        }

        javafx.beans.property.SimpleStringProperty fechaProperty() { return fecha; }
        javafx.beans.property.SimpleStringProperty tipoProperty() { return tipo; }
        javafx.beans.property.SimpleStringProperty montoProperty() { return monto; }
        javafx.beans.property.SimpleStringProperty notasProperty() { return notas; }
    }
}
