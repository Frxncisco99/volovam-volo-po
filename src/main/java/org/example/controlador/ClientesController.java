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
import java.sql.DatabaseMetaData;
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
        boolean fiscales = columnasFiscalesClientes();
        String sql = fiscales ? """
                SELECT id_cliente, nombre, telefono, limite_credito, saldo_actual,
                       rfc, razon_social, regimen_fiscal, uso_cfdi_default,
                       codigo_postal_fiscal, correo_facturacion
                FROM clientes
                WHERE activo = 1 AND nombre LIKE ? AND nombre != 'Publico General'
                ORDER BY nombre
                """ : """
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
                        rs.getDouble("saldo_actual"),
                        fiscales ? rs.getString("rfc") : "",
                        fiscales ? rs.getString("razon_social") : "",
                        fiscales ? rs.getString("regimen_fiscal") : "",
                        fiscales ? rs.getString("uso_cfdi_default") : "",
                        fiscales ? rs.getString("codigo_postal_fiscal") : "",
                        fiscales ? rs.getString("correo_facturacion") : ""));
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
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
                        || (c.telefono() != null && c.telefono().toLowerCase().contains(filtro))
                        || (c.rfc() != null && c.rfc().toLowerCase().contains(filtro)))
                .toList();
        lblConteoActivos.setText(filtrados.size() + " clientes activos");
        flowClientes.getChildren().clear();
        for (ClienteRow cliente : filtrados) {
            flowClientes.getChildren().add(crearCard(cliente));
        }
        if (filtrados.isEmpty()) {
            Label vacio = new Label("No hay clientes para mostrar");
            vacio.getStyleClass().add("clientes-empty");
            flowClientes.getChildren().add(vacio);
        }
    }

    private VBox crearCard(ClienteRow cliente) {
        double disponible = cliente.limite() - cliente.saldo();
        String estado = estadoCliente(cliente);
        VBox card = new VBox();
        card.setPrefWidth(295);
        card.setMaxWidth(295);
        card.getStyleClass().add("cliente-card");

        StackPane header = new StackPane();
        header.setPrefHeight(82);
        header.getStyleClass().add("cliente-card-header");

        Label badge = new Label(estado);
        badge.getStyleClass().add(estadoClase(cliente));
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10));

        Label avatar = new Label(iniciales(cliente.nombre()));
        avatar.setPrefSize(56, 56);
        avatar.setMaxSize(56, 56);
        avatar.getStyleClass().add("cliente-avatar");
        StackPane.setAlignment(avatar, Pos.BOTTOM_LEFT);
        StackPane.setMargin(avatar, new Insets(0, 0, -24, 16));
        header.getChildren().addAll(badge, avatar);

        VBox body = new VBox(8);
        body.getStyleClass().add("cliente-card-body");

        Label nombre = new Label(cliente.nombre());
        nombre.setWrapText(true);
        nombre.getStyleClass().add("cliente-name");

        HBox telefono = new HBox(7);
        telefono.setAlignment(Pos.CENTER_LEFT);
        FontIcon phone = crearIcono("fas-phone", "cliente-inline-icon");
        Label tel = new Label(cliente.telefono() == null || cliente.telefono().isBlank() ? "Sin teléfono" : cliente.telefono());
        tel.getStyleClass().add("cliente-phone-text");
        telefono.getChildren().addAll(phone, tel);

        Label fiscal = new Label(cliente.rfc() == null || cliente.rfc().isBlank()
                ? "Sin RFC fiscal"
                : "RFC: " + cliente.rfc());
        fiscal.getStyleClass().add("cliente-fiscal-text");

        VBox credito = new VBox(4);
        credito.getStyleClass().add("cliente-credit-box");
        credito.getChildren().addAll(
                lineaDato("Limite", "$" + String.format("%.2f", cliente.limite()), "cliente-data-value-primary"),
                lineaDato("Saldo", "$" + String.format("%.2f", cliente.saldo()), cliente.saldo() > 0 ? "cliente-data-value-danger" : "cliente-data-value-success"),
                lineaDato("Disponible", "$" + String.format("%.2f", disponible), disponible <= 0 && cliente.limite() > 0 ? "cliente-data-value-danger" : "cliente-data-value-success")
        );

        HBox acciones1 = new HBox(8);
        Button btnEditar = boton("Editar", "cliente-action-primary");
        Button btnHistorial = boton("Historial", "cliente-action-secondary");
        btnEditar.setOnAction(e -> mostrarDialogoCliente(cliente));
        btnHistorial.setOnAction(e -> mostrarHistorial(cliente));
        HBox.setHgrow(btnEditar, Priority.ALWAYS);
        HBox.setHgrow(btnHistorial, Priority.ALWAYS);
        btnEditar.setMaxWidth(Double.MAX_VALUE);
        btnHistorial.setMaxWidth(Double.MAX_VALUE);
        acciones1.getChildren().addAll(btnEditar, btnHistorial);

        HBox acciones2 = new HBox(8);
        Button btnPago = boton("Registrar pago", "cliente-action-success");
        Button btnDesactivar = boton("Desactivar", "cliente-action-danger");
        btnPago.setOnAction(e -> mostrarPago(cliente));
        btnPago.setDisable(cliente.saldo() <= 0);
        btnDesactivar.setOnAction(e -> desactivarCliente(cliente));
        HBox.setHgrow(btnPago, Priority.ALWAYS);
        HBox.setHgrow(btnDesactivar, Priority.ALWAYS);
        btnPago.setMaxWidth(Double.MAX_VALUE);
        btnDesactivar.setMaxWidth(Double.MAX_VALUE);
        acciones2.getChildren().addAll(btnPago, btnDesactivar);

        body.getChildren().addAll(nombre, telefono, fiscal, credito, acciones1, acciones2);
        card.getChildren().addAll(header, body);
        return card;
    }

    private HBox lineaDato(String etiqueta, String valor, String valorStyleClass) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label left = new Label(etiqueta);
        left.getStyleClass().add("cliente-data-label");
        Label right = new Label(valor);
        right.getStyleClass().add(valorStyleClass);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(left, spacer, right);
        return row;
    }

    private Button boton(String texto, String estilo) {
        Button btn = new Button(texto);
        btn.setPrefHeight(32);
        btn.getStyleClass().addAll("cliente-action-button", estilo);
        return btn;
    }

    private FontIcon crearIcono(String iconLiteral, String estilo) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(12);
        icon.getStyleClass().add(estilo);
        return icon;
    }

    @FXML
    public void handleNuevoCliente() {
        if (!org.example.servicio.PermisoService.tienePermiso(org.example.servicio.PermisoService.CLIENTES_CREAR)) {
            mostrarAlerta("Acceso denegado", "No tienes permiso para crear clientes.");
            return;
        }
        mostrarDialogoCliente(null);
    }

    private void mostrarDialogoCliente(ClienteRow cliente) {
        boolean nuevo = cliente == null;
        if (!nuevo && !org.example.servicio.PermisoService.tienePermiso(org.example.servicio.PermisoService.CLIENTES_EDITAR)) {
            mostrarAlerta("Acceso denegado", "No tienes permiso para editar clientes.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(nuevo ? "Nuevo cliente" : "Editar cliente");
        aplicarEstilosDialogo(dialog);

        VBox contenido = new VBox(10);
        contenido.setMinWidth(380);
        contenido.getStyleClass().add("cliente-dialog-content");

        TextField txtNombre = new TextField(nuevo ? "" : cliente.nombre());
        TextField txtTelefono = new TextField(nuevo ? "" : cliente.telefono());
        TextField txtLimite = new TextField(nuevo || cliente.limite() <= 0 ? "" : String.valueOf(cliente.limite()));
        TextField txtRfc = new TextField(nuevo ? "" : cliente.rfc());
        TextField txtRazonSocial = new TextField(nuevo ? "" : cliente.razonSocial());
        TextField txtRegimen = new TextField(nuevo ? "" : cliente.regimenFiscal());
        TextField txtUsoCfdi = new TextField(nuevo ? "" : cliente.usoCfdi());
        TextField txtCpFiscal = new TextField(nuevo ? "" : cliente.cpFiscal());
        TextField txtCorreoFacturacion = new TextField(nuevo ? "" : cliente.correoFacturacion());

        txtNombre.setPromptText("Nombre completo");
        txtTelefono.setPromptText("Teléfono a 10 dígitos");
        txtLimite.setPromptText("Límite de crédito");
        txtRfc.setPromptText("RFC");
        txtRazonSocial.setPromptText("Razón social");
        txtRegimen.setPromptText("Régimen fiscal");
        txtUsoCfdi.setPromptText("Uso CFDI");
        txtCpFiscal.setPromptText("Código postal fiscal");
        txtCorreoFacturacion.setPromptText("Correo de facturación");
        aplicarEstiloCampo(txtNombre);
        aplicarEstiloCampo(txtTelefono);
        aplicarEstiloCampo(txtLimite);
        aplicarEstiloCampo(txtRfc);
        aplicarEstiloCampo(txtRazonSocial);
        aplicarEstiloCampo(txtRegimen);
        aplicarEstiloCampo(txtUsoCfdi);
        aplicarEstiloCampo(txtCpFiscal);
        aplicarEstiloCampo(txtCorreoFacturacion);

        txtTelefono.textProperty().addListener((obs, old, nuevoTexto) -> {
            if (!nuevoTexto.matches("\\d{0,10}")) txtTelefono.setText(old);
        });

        contenido.getChildren().addAll(
                new Label("Nombre"), txtNombre,
                new Label("Teléfono"), txtTelefono,
                new Label("Límite de crédito"), txtLimite,
                new Label("Datos fiscales"), txtRfc, txtRazonSocial, txtRegimen,
                txtUsoCfdi, txtCpFiscal, txtCorreoFacturacion
        );
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        org.example.servicio.DialogService.preparar(dialog, flowClientes);
        dialog.showAndWait().ifPresent(respuesta -> {
            if (respuesta != ButtonType.OK) return;
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            double limite;
            try {
                limite = txtLimite.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtLimite.getText().trim());
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El límite de crédito debe ser numérico.");
                return;
            }
            if (nombre.isEmpty()) {
                mostrarAlerta("Error", "El nombre es obligatorio.");
                return;
            }
            if (limite < 0) {
                mostrarAlerta("Error", "El límite de crédito debe ser mayor o igual a 0.");
                return;
            }
            ClienteFiscal fiscal = new ClienteFiscal(
                    txtRfc.getText().trim(),
                    txtRazonSocial.getText().trim(),
                    txtRegimen.getText().trim(),
                    txtUsoCfdi.getText().trim(),
                    txtCpFiscal.getText().trim(),
                    txtCorreoFacturacion.getText().trim()
            );
            if (nuevo) insertarCliente(nombre, telefono, limite, fiscal);
            else actualizarCliente(cliente.id(), nombre, telefono, limite, fiscal);
        });
    }

    private void aplicarEstiloCampo(TextField field) {
        field.getStyleClass().add("cliente-dialog-input");
    }

    private void aplicarEstilosDialogo(Dialog<?> dialog) {
        java.net.URL css = getClass().getResource("/org/example/vista/menuPrincipal.css");
        if (css != null) {
            dialog.getDialogPane().getStylesheets().add(css.toExternalForm());
        }
        dialog.getDialogPane().getStyleClass().add("cliente-dialog");
    }

    private void insertarCliente(String nombre, String telefono, double limite, ClienteFiscal fiscal) {
        boolean fiscales = columnasFiscalesClientes();
        String sql = fiscales
                ? "INSERT INTO clientes (nombre, telefono, limite_credito, rfc, razon_social, regimen_fiscal, uso_cfdi_default, codigo_postal_fiscal, correo_facturacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                : "INSERT INTO clientes (nombre, telefono, limite_credito) VALUES (?, ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setDouble(3, limite);
            if (fiscales) {
                ps.setString(4, fiscal.rfc());
                ps.setString(5, fiscal.razonSocial());
                ps.setString(6, fiscal.regimenFiscal());
                ps.setString(7, fiscal.usoCfdi());
                ps.setString(8, fiscal.cpFiscal());
                ps.setString(9, fiscal.correoFacturacion());
            }
            ps.executeUpdate();
            cargarClientes();
            mostrarInfo("Exito", "Cliente registrado correctamente.");
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
            mostrarAlerta("Error", "No se pudo registrar el cliente.");
        }
    }

    private void actualizarCliente(int id, String nombre, String telefono, double limite, ClienteFiscal fiscal) {
        boolean fiscales = columnasFiscalesClientes();
        String sql = fiscales
                ? "UPDATE clientes SET nombre = ?, telefono = ?, limite_credito = ?, rfc = ?, razon_social = ?, regimen_fiscal = ?, uso_cfdi_default = ?, codigo_postal_fiscal = ?, correo_facturacion = ? WHERE id_cliente = ?"
                : "UPDATE clientes SET nombre = ?, telefono = ?, limite_credito = ? WHERE id_cliente = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setDouble(3, limite);
            if (fiscales) {
                ps.setString(4, fiscal.rfc());
                ps.setString(5, fiscal.razonSocial());
                ps.setString(6, fiscal.regimenFiscal());
                ps.setString(7, fiscal.usoCfdi());
                ps.setString(8, fiscal.cpFiscal());
                ps.setString(9, fiscal.correoFacturacion());
                ps.setInt(10, id);
            } else {
                ps.setInt(4, id);
            }
            ps.executeUpdate();
            cargarClientes();
            mostrarInfo("Exito", "Cliente actualizado correctamente.");
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
            mostrarAlerta("Error", "No se pudo actualizar el cliente.");
        }
    }

    private void mostrarPago(ClienteRow cliente) {
        if (!org.example.servicio.PermisoService.requerirPermisoOAutorizacionAdmin(
                org.example.servicio.PermisoService.CLIENTES_CREDITO,
                "Registrar abono de cliente")) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Registrar pago");
        aplicarEstilosDialogo(dialog);
        VBox contenido = new VBox(10);
        contenido.setMinWidth(360);
        contenido.getStyleClass().add("cliente-dialog-content");
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
        org.example.servicio.DialogService.preparar(dialog, flowClientes);
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
            org.example.servicio.LogService.error("Error no controlado", e);
            mostrarAlerta("Error", "No se pudo registrar el pago.");
        }
    }

    private void mostrarHistorial(ClienteRow cliente) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Historial de pagos - " + cliente.nombre());
        aplicarEstilosDialogo(dialog);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<PagoRow> tabla = new TableView<>();
        tabla.getStyleClass().add("cliente-history-table");
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
        org.example.servicio.DialogService.preparar(dialog, flowClientes);
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
            org.example.servicio.LogService.error("Error no controlado", e);
        }
        return pagos;
    }

    private void desactivarCliente(ClienteRow cliente) {
        if (!org.example.servicio.PermisoService.tienePermiso(org.example.servicio.PermisoService.CLIENTES_EDITAR)) {
            mostrarAlerta("Acceso denegado", "No tienes permiso para desactivar clientes.");
            return;
        }
        if (cliente.saldo() > 0) {
            mostrarAlerta("No permitido", "No se puede desactivar un cliente con adeudo pendiente.");
            return;
        }
        if (org.example.servicio.DialogService.confirmar(
                flowClientes,
                "Desactivar cliente",
                "Seguro que deseas desactivar a " + cliente.nombre() + "?"
        ).orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        String sql = "UPDATE clientes SET activo = 0 WHERE id_cliente = ? AND nombre != 'Publico General'";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cliente.id());
            ps.executeUpdate();
            cargarClientes();
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
            mostrarAlerta("Error", "No se pudo desactivar el cliente.");
        }
    }

    private String estadoCliente(ClienteRow cliente) {
        if (cliente.limite() > 0 && cliente.saldo() >= cliente.limite()) return "Limite al maximo";
        if (cliente.saldo() > 0) return "Con adeudo";
        return "Sin adeudo";
    }

    private String estadoClase(ClienteRow cliente) {
        if (cliente.limite() > 0 && cliente.saldo() >= cliente.limite()) return "cliente-status-danger";
        if (cliente.saldo() > 0) return "cliente-status-warning";
        return "cliente-status-ok";
    }

    private String iniciales(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) return "CL";
        String[] partes = limpio.split("\\s+");
        if (partes.length > 1) return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
        return limpio.substring(0, Math.min(2, limpio.length())).toUpperCase();
    }

    private boolean columnasFiscalesClientes() {
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return false;
            DatabaseMetaData meta = con.getMetaData();
            try (ResultSet rs = meta.getColumns(con.getCatalog(), null, "clientes", "rfc")) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void registrarLogout() {
        String sql = "INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, detalle) VALUES (?, 'LOGOUT', 'usuarios', ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int idUsuario = SesionUsuario.getInstancia().getIdUsuario();
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setString(3, "Cierre de sesión: " + SesionUsuario.getInstancia().getNombre());
            ps.executeUpdate();
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    @FXML public void irADashboard() { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_REPORTES, "/org/example/vista/MenuPrincipal.fxml"); }
    @FXML public void irAVentas() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_VENTAS, "/org/example/vista/Ventas.fxml"); }
    @FXML private void irAInventario() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_INVENTARIO, "/org/example/vista/Inventario.fxml"); }
    @FXML public void irAEmpleados() { navegarConPermiso(org.example.servicio.PermisoService.Accion.GESTIONAR_EMPLEADOS, "/org/example/vista/Empleados.fxml"); }
    @FXML public void irAReportes() { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_REPORTES, "/org/example/vista/Reportes.fxml"); }
    @FXML public void irACorteCaja() { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_CORTE_CAJA, "/org/example/vista/CorteCaja.fxml"); }
    @FXML private void irAAuditoria() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_AUDITORIA, "/org/example/vista/Auditoria.fxml"); }
    @FXML private void irAConfiguracion() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_CONFIGURACION, "/org/example/vista/Configuracion.fxml"); }

    @FXML
    public void btnCerrar() {
        org.example.servicio.NavigationService.cambiarSesion(flowClientes);
    }

    @FXML
    public void salirAplicacion() {
        org.example.servicio.AppExitService.salir(flowClientes);
    }

    private void navegar(String ruta) {
        org.example.servicio.NavigationService.cambiarEscena(flowClientes, ruta);
    }

    private void navegarConPermiso(org.example.servicio.PermisoService.Accion accion, String ruta) {
        if (!org.example.servicio.PermisoService.puede(accion)) {
            mostrarAlerta("Acceso denegado", "No tienes permiso para acceder a este módulo.");
            return;
        }
        navegar(ruta);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        org.example.servicio.DialogService.advertencia(flowClientes, titulo, mensaje);
    }

    private void mostrarInfo(String titulo, String mensaje) {
        org.example.servicio.DialogService.info(flowClientes, titulo, mensaje);
    }

    private record ClienteRow(int id, String nombre, String telefono, double limite, double saldo,
                              String rfc, String razonSocial, String regimenFiscal, String usoCfdi,
                              String cpFiscal, String correoFacturacion) {}

    private record ClienteFiscal(String rfc, String razonSocial, String regimenFiscal, String usoCfdi,
                                 String cpFiscal, String correoFacturacion) {}

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
