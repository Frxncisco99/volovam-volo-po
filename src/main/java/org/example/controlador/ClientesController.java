package org.example.controlador;

import javafx.application.Platform;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClientesController {

    @FXML private VBox listaClientes;
    @FXML private TextField txtBuscar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    @FXML
    public void initialize() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);

        cargarClientes("");
        txtBuscar.textProperty().addListener((obs, old, nuevo) -> cargarClientes(nuevo));
    }

    private void cargarClientes(String filtro) {
        listaClientes.getChildren().clear();
        String sql = "SELECT id_cliente, nombre, telefono, limite_credito, saldo_actual FROM clientes WHERE activo = 1 AND nombre LIKE ? AND nombre != 'Publico General' ORDER BY nombre";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + filtro + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_cliente");
                String nombre = rs.getString("nombre");
                String telefono = rs.getString("telefono");
                double limite = rs.getDouble("limite_credito");
                double saldo = rs.getDouble("saldo_actual");
                double disponible = limite - saldo;

                // Card del cliente
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 14 16; -fx-effect: dropshadow(gaussian, #00000010, 6, 0, 0, 1);");

                // Fila superior
                HBox filaSup = new HBox(10);
                filaSup.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                VBox info = new VBox(3);
                HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

                Label lblNombre = new Label(nombre);
                lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-font-size: 14px;");

                Label lblTel = new Label(telefono != null ? telefono : "Sin teléfono");
                lblTel.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 11px;");

                info.getChildren().addAll(lblNombre, lblTel);

                // Badge de saldo
                String colorSaldo = saldo >= limite && limite > 0 ? "#C0392B" : "#3B6D11";
                Label lblSaldo = new Label(saldo > 0 ? "Adeudo: $" + String.format("%.2f", saldo) : "Sin adeudo");
                lblSaldo.setStyle("-fx-background-color: " + colorSaldo + "22; -fx-text-fill: " + colorSaldo + "; -fx-background-radius: 8; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: bold;");

                filaSup.getChildren().addAll(info, lblSaldo);

                // Fila de crédito
                HBox filaCred = new HBox(8);
                filaCred.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label lblLimite = new Label("Límite: $" + String.format("%.2f", limite));
                lblLimite.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 11px;");

                Label lblDisponible = new Label("Disponible: $" + String.format("%.2f", disponible));
                lblDisponible.setStyle("-fx-text-fill: " + (disponible <= 0 ? "#C0392B" : "#3B6D11") + "; -fx-font-size: 11px; -fx-font-weight: bold;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                // Botones
                Button btnAbonar = new Button("Abonar");
                btnAbonar.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");
                btnAbonar.setOnAction(e -> handleAbonar(id, nombre, saldo));
                btnAbonar.setDisable(saldo <= 0);

                Button btnEditar = new Button("Editar");
                btnEditar.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEditar.setOnAction(e -> handleEditar(id, nombre, telefono, limite));

                Button btnHistorial = new Button("Historial");
                btnHistorial.setStyle("-fx-background-color: transparent; -fx-border-color: #6B4226; -fx-border-radius: 6; -fx-border-width: 1; -fx-text-fill: #6B4226; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");
                btnHistorial.setOnAction(e -> handleHistorial(id, nombre));

                filaCred.getChildren().addAll(lblLimite, lblDisponible, spacer, btnHistorial, btnAbonar, btnEditar);
                card.getChildren().addAll(filaSup, filaCred);
                listaClientes.getChildren().add(card);
            }

            if (listaClientes.getChildren().isEmpty()) {
                Label lblVacio = new Label("No hay clientes registrados");
                lblVacio.setStyle("-fx-text-fill: #C8A97E; -fx-font-size: 13px; -fx-padding: 20 0;");
                listaClientes.getChildren().add(lblVacio);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNuevoCliente() {
        mostrarDialogoCliente(0, "", "", 0);
    }

    private void mostrarDialogoCliente(int id, String nombre, String telefono, double limite) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(id == 0 ? "Nuevo cliente" : "Editar cliente");

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 20; -fx-min-width: 350;");

        TextField txtNombre = new TextField(nombre);
        txtNombre.setPromptText("Nombre completo");
        txtNombre.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-padding: 8;");

        TextField txtTelefono = new TextField(telefono);
        txtTelefono.setPromptText("Telefono");
        txtTelefono.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-padding: 8;");

        TextField txtLimite = new TextField(limite > 0 ? String.valueOf(limite) : "");
        txtLimite.setPromptText("Limite de credito (ej: 500.00)");
        txtLimite.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-padding: 8;");

        contenido.getChildren().addAll(
                new Label("Nombre:"), txtNombre,
                new Label("Telefono:"), txtTelefono,
                new Label("Limite de credito:"), txtLimite
        );

        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                String n = txtNombre.getText().trim();
                String t = txtTelefono.getText().trim();
                double l = 0;
                try { l = Double.parseDouble(txtLimite.getText().trim()); } catch (Exception ignored) {}

                if (n.isEmpty()) {
                    mostrarAlerta("Error", "El nombre es obligatorio.");
                    return;
                }

                if (id == 0) insertarCliente(n, t, l);
                else actualizarCliente(id, n, t, l);
            }
        });
    }

    private void insertarCliente(String nombre, String telefono, double limite) {
        String sql = "INSERT INTO clientes (nombre, telefono, limite_credito) VALUES (?, ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setDouble(3, limite);
            ps.executeUpdate();
            cargarClientes("");
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
            cargarClientes("");
            mostrarInfo("Exito", "Cliente actualizado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleEditar(int id, String nombre, String telefono, double limite) {
        mostrarDialogoCliente(id, nombre, telefono, limite);
    }

    private void handleAbonar(int id, String nombre, double saldoActual) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Abonar a " + nombre);
        dialog.setHeaderText("Saldo actual: $" + String.format("%.2f", saldoActual));
        dialog.setContentText("Monto a abonar:");

        dialog.showAndWait().ifPresent(texto -> {
            try {
                double monto = Double.parseDouble(texto.trim());
                if (monto <= 0) {
                    mostrarAlerta("Error", "El monto debe ser mayor a 0.");
                    return;
                }
                if (monto > saldoActual) {
                    mostrarAlerta("Error", "El abono no puede ser mayor al saldo.");
                    return;
                }

                try (Connection con = ConexionDB.getConexion()) {
                    con.setAutoCommit(false);

                    // Actualizar saldo
                    PreparedStatement psSaldo = con.prepareStatement(
                            "UPDATE clientes SET saldo_actual = saldo_actual - ? WHERE id_cliente = ?");
                    psSaldo.setDouble(1, monto);
                    psSaldo.setInt(2, id);
                    psSaldo.executeUpdate();

                    // Registrar pago
                    PreparedStatement psPago = con.prepareStatement(
                            "INSERT INTO pagos_cliente (id_cliente, monto, tipo, notas) VALUES (?, ?, 'ABONO', 'Abono manual')");
                    psPago.setInt(1, id);
                    psPago.setDouble(2, monto);
                    psPago.executeUpdate();

                    con.commit();
                    cargarClientes("");
                    mostrarInfo("Exito", "Abono de $" + String.format("%.2f", monto) + " registrado.");
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Ingresa un numero valido.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleHistorial(int id, String nombre) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Historial de " + nombre);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox contenido = new VBox(8);
        contenido.setStyle("-fx-padding: 16; -fx-min-width: 500;");

        String sql = "SELECT tipo, monto, fecha, notas FROM pagos_cliente WHERE id_cliente = ? ORDER BY fecha DESC LIMIT 20";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                double monto = rs.getDouble("monto");
                String fecha = rs.getTimestamp("fecha").toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                String notas = rs.getString("notas");

                HBox fila = new HBox(10);
                fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                fila.setStyle("-fx-background-color: " + (tipo.equals("ABONO") ? "#E8F5E9" : "#FDECEA") + "; -fx-background-radius: 6; -fx-padding: 8 12;");

                Label lblTipo = new Label(tipo.equals("ABONO") ? "Abono" : "Cargo");
                lblTipo.setStyle("-fx-text-fill: " + (tipo.equals("ABONO") ? "#3B6D11" : "#C0392B") + "; -fx-font-weight: bold; -fx-font-size: 12px; -fx-min-width: 60;");

                Label lblMonto = new Label("$" + String.format("%.2f", monto));
                lblMonto.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B4226; -fx-font-size: 13px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                Label lblFecha = new Label(fecha);
                lblFecha.setStyle("-fx-text-fill: #7A5535; -fx-font-size: 11px;");

                fila.getChildren().addAll(lblTipo, lblMonto, spacer, lblFecha);
                contenido.getChildren().add(fila);
            }

            if (contenido.getChildren().isEmpty()) {
                contenido.getChildren().add(new Label("Sin movimientos registrados"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
    }

    @FXML public void irADashboard() { navegar("/org/example/vista/MenuPrincipal.fxml"); }
    @FXML public void irAVentas() { navegar("/org/example/vista/Ventas.fxml"); }
    @FXML public void irAEmpleados() { navegar("/org/example/vista/Empleados.fxml"); }
    @FXML public void irAReportes() { navegar("/org/example/vista/Reportes.fxml"); }
    @FXML public void irACorteCaja() { navegar("/org/example/vista/CorteCaja.fxml"); }

    @FXML
    public void btnCerrar() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Salir");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas salir?");
        alerta.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) Platform.exit();
        });
    }

    private void navegar(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) listaClientes.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @FXML
    private void irAConfiguracion() {navegar("/org/example/vista/Configuracion.fxml");}



}