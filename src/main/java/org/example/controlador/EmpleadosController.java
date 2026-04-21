package org.example.controlador;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmpleadosController {

    // ── Nuevos fx:id para tarjetas ──
    @FXML private FlowPane flowTarjetas;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroRol;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private Label lblConteo;
    @FXML private Label lblStatTotal;
    @FXML private Label lblStatActivos;
    @FXML private Label lblStatInactivos;
    @FXML private Label lblStatRoles;

    // ── Sidebar labels (sin cambios) ──
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    // Cache de datos cargados de BD
    private List<ObservableList<String>> todosLosEmpleados = new ArrayList<>();



    // ─────────────────────────────────────────────
    @FXML
    public void initialize() {

        // Sesión (sin cambios)
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);

        // Filtros fijos de estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));

        // Listeners en tiempo real
        txtBuscar.textProperty().addListener((o, ov, nv) -> renderTarjetas());
        cmbFiltroRol.valueProperty().addListener((o, ov, nv) -> renderTarjetas());
        cmbFiltroEstado.valueProperty().addListener((o, ov, nv) -> renderTarjetas());

        cargarEmpleados();
    }


    // ─────────────────────────────────────────────
    // CARGA DESDE BD (lógica original intacta, solo guarda en cache)
    // ─────────────────────────────────────────────
    private void cargarEmpleados() {
        todosLosEmpleados.clear();
        Set<String> roles = new HashSet<>();

        String sql = "SELECT u.id_usuario, u.nombre, u.usuario, r.nombre, u.activo " +
                "FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(String.valueOf(rs.getInt(1)));   // 0 id
                fila.add(rs.getString(2));                // 1 nombre
                fila.add(rs.getString(3));                // 2 usuario
                fila.add(rs.getString(4));                // 3 rol
                fila.add(rs.getInt(5) == 1 ? "Activo" : "Inactivo"); // 4 estado
                todosLosEmpleados.add(fila);
                roles.add(rs.getString(4));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Poblar combo de roles con los que existen en BD
        cmbFiltroRol.setItems(FXCollections.observableArrayList(roles));

        actualizarStats();
        renderTarjetas();
    }

    // ─────────────────────────────────────────────
    // STATS BAR
    // ─────────────────────────────────────────────
    private void actualizarStats() {
        long total    = todosLosEmpleados.size();
        long activos  = todosLosEmpleados.stream().filter(f -> "Activo".equals(f.get(4))).count();
        long inactivos= total - activos;
        long roles    = todosLosEmpleados.stream().map(f -> f.get(3)).distinct().count();

        lblStatTotal.setText(String.valueOf(total));
        lblStatActivos.setText(String.valueOf(activos));
        lblStatInactivos.setText(String.valueOf(inactivos));
        lblStatRoles.setText(String.valueOf(roles));
    }

    // ─────────────────────────────────────────────
    // RENDER DE TARJETAS
    // ─────────────────────────────────────────────
    private void renderTarjetas() {
        String buscar  = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();
        String filtRol = cmbFiltroRol.getValue();
        String filtEst = cmbFiltroEstado.getValue();

        List<ObservableList<String>> filtrados = todosLosEmpleados.stream()
                .filter(f -> buscar.isEmpty()
                        || f.get(1).toLowerCase().contains(buscar)
                        || f.get(2).toLowerCase().contains(buscar))
                .filter(f -> filtRol == null || filtRol.isEmpty() || f.get(3).equals(filtRol))
                .filter(f -> filtEst == null || filtEst.isEmpty() || f.get(4).equals(filtEst))
                .toList();

        lblConteo.setText("Mostrando " + filtrados.size() +
                " empleado" + (filtrados.size() != 1 ? "s" : ""));

        flowTarjetas.getChildren().clear();
        for (ObservableList<String> fila : filtrados) {
            flowTarjetas.getChildren().add(crearTarjeta(fila));
        }
    }

    // ─────────────────────────────────────────────
    // CONSTRUCCIÓN DE UNA TARJETA
    // ─────────────────────────────────────────────
    private VBox crearTarjeta(ObservableList<String> fila) {
        String id     = fila.get(0);
        String nombre = fila.get(1);
        String usuario= fila.get(2);
        String rol    = fila.get(3);
        String estado = fila.get(4);
        boolean activo = "Activo".equals(estado);

        // ── Contenedor principal ──
        VBox card = new VBox();
        card.setPrefWidth(260);
        card.setMaxWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #E8DDD0; -fx-border-width: 1; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian,rgba(61,31,13,0.10),12,0,0,3);");

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(
                "dropshadow(gaussian,rgba(61,31,13,0.10),12,0,0,3)",
                "dropshadow(gaussian,rgba(61,31,13,0.20),24,0,0,6)")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace(
                "dropshadow(gaussian,rgba(61,31,13,0.20),24,0,0,6)",
                "dropshadow(gaussian,rgba(61,31,13,0.10),12,0,0,3)")));

        // ── HEADER con gradiente ──
        StackPane header = new StackPane();
        header.setPrefHeight(90);
        header.setStyle("-fx-background-color: linear-gradient(to bottom right, #3D1F0D, #6B4226); " +
                "-fx-background-radius: 16 16 0 0;");

        // Badge rol (esquina superior derecha)
        Label rolBadge = new Label(rol);
        rolBadge.setStyle("-fx-background-color: rgba(212,168,67,0.22); -fx-text-fill: #D4A843; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 20; " +
                "-fx-border-color: rgba(212,168,67,0.35); -fx-border-width: 1; -fx-border-radius: 20; " +
                "-fx-padding: 3 10;");
        StackPane.setAlignment(rolBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(rolBadge, new Insets(10, 10, 0, 0));

        // Avatar con iniciales (esquina inferior izquierda)
        String ini = nombre.trim().isEmpty() ? "??" :
                nombre.trim().split("\\s+").length >= 2
                        ? String.valueOf(nombre.trim().charAt(0)).toUpperCase() +
                        String.valueOf(nombre.trim().split("\\s+")[1].charAt(0)).toUpperCase()
                        : nombre.substring(0, Math.min(2, nombre.length())).toUpperCase();

        Label avatar = new Label(ini);
        avatar.setPrefSize(54, 54);
        avatar.setMaxSize(54, 54);
        avatar.setStyle("-fx-background-color: #D4A843; -fx-text-fill: #6B4226; " +
                "-fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 27; " +
                "-fx-border-color: rgba(255,255,255,0.25); -fx-border-width: 2.5; -fx-border-radius: 27; " +
                "-fx-alignment: center; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.25),8,0,0,2);");
        StackPane.setAlignment(avatar, Pos.BOTTOM_LEFT);
        StackPane.setMargin(avatar, new Insets(0, 0, -20, 16));

        // Badge estado (esquina inferior derecha)
        Label estadoBadge = new Label(activo ? "● Activo" : "○ Inactivo");
        estadoBadge.setStyle(activo
                ? "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 3 10;"
                : "-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 3 10;");
        StackPane.setAlignment(estadoBadge, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(estadoBadge, new Insets(0, 10, 8, 0));

        header.getChildren().addAll(rolBadge, avatar, estadoBadge);

        // ── BODY ──
        VBox body = new VBox(4);
        body.setPadding(new Insets(28, 16, 8, 16));

        Label lblNombre = new Label(nombre);
        lblNombre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C1A0E;");
        lblNombre.setWrapText(true);

        Label lblUsuario = new Label("@" + usuario);
        lblUsuario.setStyle("-fx-font-size: 12px; -fx-text-fill: #7A5535;");

        body.getChildren().addAll(lblNombre, lblUsuario);

        // ── META CHIPS ──
        HBox chips = new HBox(8);
        chips.setPadding(new Insets(8, 16, 8, 16));
        chips.setStyle("-fx-border-color: #F0E8DC; -fx-border-width: 1 0 0 0;");

        Label chipId  = crearChip("# ID " + id);
        Label chipRol = crearChip("🎭 " + rol);
        chips.getChildren().addAll(chipId, chipRol);

        // ── ACCIONES ──
        HBox acciones = new HBox(8);
        acciones.setPadding(new Insets(8, 16, 14, 16));
        acciones.setAlignment(Pos.CENTER_LEFT);

        Button btnEditar = new Button("✏  Editar");
        btnEditar.setPrefHeight(32);
        btnEditar.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B4226; " +
                "-fx-border-color: #6B4226; -fx-border-width: 1.5; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
        HBox.setHgrow(btnEditar, Priority.ALWAYS);
        btnEditar.setMaxWidth(Double.MAX_VALUE);
        btnEditar.setOnMouseEntered(e -> btnEditar.setStyle(btnEditar.getStyle()
                .replace("-fx-background-color: transparent;", "-fx-background-color: #6B4226;")
                .replace("-fx-text-fill: #6B4226;", "-fx-text-fill: white;")));
        btnEditar.setOnMouseExited(e -> btnEditar.setStyle(btnEditar.getStyle()
                .replace("-fx-background-color: #6B4226;", "-fx-background-color: transparent;")
                .replace("-fx-text-fill: white;", "-fx-text-fill: #6B4226;")));
        btnEditar.setOnAction(e -> mostrarDialogoEmpleado(Integer.parseInt(id), nombre, usuario, rol));

        // Botón activar/desactivar
        Button btnToggle = new Button(activo ? "⏸" : "▶");
        btnToggle.setPrefSize(32, 32);
        btnToggle.setStyle("-fx-background-color: transparent; -fx-border-color: #D4C9B0; " +
                "-fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-font-size: 13px; -fx-cursor: hand;");
        btnToggle.setTooltip(new Tooltip(activo ? "Desactivar empleado" : "Activar empleado"));
        btnToggle.setOnAction(e -> toggleEstado(Integer.parseInt(id), nombre, activo));

        // Botón eliminar
        Button btnEliminar = new Button("🗑");
        btnEliminar.setPrefSize(32, 32);
        btnEliminar.setStyle("-fx-background-color: transparent; -fx-border-color: #D4C9B0; " +
                "-fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-font-size: 13px; -fx-cursor: hand;");
        btnEliminar.setTooltip(new Tooltip("Eliminar empleado"));
        btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle(btnEliminar.getStyle()
                .replace("-fx-border-color: #D4C9B0;", "-fx-border-color: #C0392B;")
                .replace("-fx-background-color: transparent;", "-fx-background-color: #FFF5F5;")));
        btnEliminar.setOnMouseExited(e -> btnEliminar.setStyle(btnEliminar.getStyle()
                .replace("-fx-border-color: #C0392B;", "-fx-border-color: #D4C9B0;")
                .replace("-fx-background-color: #FFF5F5;", "-fx-background-color: transparent;")));
        btnEliminar.setOnAction(e -> eliminarEmpleado(Integer.parseInt(id), nombre));

        acciones.getChildren().addAll(btnEditar, btnToggle, btnEliminar);

        card.getChildren().addAll(header, body, chips, acciones);
        return card;
    }

    private Label crearChip(String texto) {
        Label chip = new Label(texto);
        chip.setStyle("-fx-background-color: #F5EFE6; -fx-text-fill: #7A5535; -fx-font-size: 11px; " +
                "-fx-background-radius: 6; -fx-border-color: #E8DDD0; -fx-border-width: 1; " +
                "-fx-border-radius: 6; -fx-padding: 3 10;");
        return chip;
    }

    // ─────────────────────────────────────────────
    // TOGGLE ACTIVO / INACTIVO
    // ─────────────────────────────────────────────
    private void toggleEstado(int id, String nombre, boolean estaActivo) {
        int nuevoValor = estaActivo ? 0 : 1;
        String sql = "UPDATE usuarios SET activo = ? WHERE id_usuario = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoValor);
            ps.setInt(2, id);
            ps.executeUpdate();
            cargarEmpleados();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cambiar el estado del empleado.");
        }
    }

    // ─────────────────────────────────────────────
    // FILTRO LIMPIAR
    // ─────────────────────────────────────────────
    @FXML
    public void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroRol.setValue(null);
        cmbFiltroEstado.setValue(null);
    }

    // ─────────────────────────────────────────────
    // CRUD — lógica original sin cambios
    // ─────────────────────────────────────────────
    @FXML
    public void handleNuevoEmpleado() {
        mostrarDialogoEmpleado(0, "", "", "cajero");
    }

    private void mostrarDialogoEmpleado(int id, String nombre, String usuario, String rol) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(id == 0 ? "Nuevo empleado" : "Editar empleado");

        VBox contenido = new VBox(12);
        contenido.setStyle("-fx-padding: 20;");

        TextField txtNombre = new TextField(nombre);
        txtNombre.setPromptText("Nombre completo");
        txtNombre.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-padding: 8;");

        TextField txtUsuario = new TextField(usuario);
        txtUsuario.setPromptText("Usuario");
        txtUsuario.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-padding: 8;");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText(id == 0 ? "Contraseña" : "Nueva contraseña (dejar vacío para no cambiar)");
        txtPassword.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6B4226; -fx-padding: 8;");

        ComboBox<String> cmbRol = new ComboBox<>();
        cmbRol.getItems().addAll("admin", "cajero");
        cmbRol.setValue(rol);
        cmbRol.setStyle("-fx-background-radius: 8;");

        contenido.getChildren().addAll(
                new Label("Nombre:"), txtNombre,
                new Label("Usuario:"), txtUsuario,
                new Label("Contraseña:"), txtPassword,
                new Label("Rol:"), cmbRol
        );

        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                String n = txtNombre.getText().trim();
                String u = txtUsuario.getText().trim();
                String p = txtPassword.getText().trim();
                String r = cmbRol.getValue();

                if (n.isEmpty() || u.isEmpty() || (id == 0 && p.isEmpty())) {
                    mostrarAlerta("Error", "Nombre, usuario y contraseña son obligatorios.");
                    return;
                }

                if (id == 0) {
                    insertarEmpleado(n, u, p, r);
                } else {
                    actualizarEmpleado(id, n, u, p, r);
                }
            }
        });
    }

    private void insertarEmpleado(String nombre, String usuario, String password, String rol) {
        String sqlRol    = "SELECT id_rol FROM roles WHERE nombre = ?";
        String sqlInsert = "INSERT INTO usuarios (nombre, usuario, contrasena, id_rol) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConexion()) {
            PreparedStatement psRol = con.prepareStatement(sqlRol);
            psRol.setString(1, rol);
            ResultSet rs = psRol.executeQuery();
            if (rs.next()) {
                int idRol = rs.getInt(1);
                PreparedStatement ps = con.prepareStatement(sqlInsert);
                ps.setString(1, nombre);
                ps.setString(2, usuario);
                ps.setString(3, password);
                ps.setInt(4, idRol);
                ps.executeUpdate();
                cargarEmpleados();
                mostrarAlerta("Exito", "Empleado creado correctamente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo crear el empleado.");
        }
    }

    private void actualizarEmpleado(int id, String nombre, String usuario, String password, String rol) {
        String sqlRol = "SELECT id_rol FROM roles WHERE nombre = ?";
        try (Connection con = ConexionDB.getConexion()) {
            PreparedStatement psRol = con.prepareStatement(sqlRol);
            psRol.setString(1, rol);
            ResultSet rs = psRol.executeQuery();
            if (rs.next()) {
                int idRol = rs.getInt(1);
                String sql;
                PreparedStatement ps;
                if (password.isEmpty()) {
                    sql = "UPDATE usuarios SET nombre = ?, usuario = ?, id_rol = ? WHERE id_usuario = ?";
                    ps  = con.prepareStatement(sql);
                    ps.setString(1, nombre);
                    ps.setString(2, usuario);
                    ps.setInt(3, idRol);
                    ps.setInt(4, id);
                } else {
                    sql = "UPDATE usuarios SET nombre = ?, usuario = ?, contrasena = ?, id_rol = ? WHERE id_usuario = ?";
                    ps  = con.prepareStatement(sql);
                    ps.setString(1, nombre);
                    ps.setString(2, usuario);
                    ps.setString(3, password);
                    ps.setInt(4, idRol);
                    ps.setInt(5, id);
                }
                ps.executeUpdate();
                cargarEmpleados();
                mostrarAlerta("Exito", "Empleado actualizado correctamente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo actualizar el empleado.");
        }
    }

    private void eliminarEmpleado(int id, String nombre) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Eliminar empleado");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Seguro que deseas eliminar a " + nombre + "?");
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                String sql = "UPDATE usuarios SET activo = 0 WHERE id_usuario = ?";
                try (Connection con = ConexionDB.getConexion();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    cargarEmpleados();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ─────────────────────────────────────────────
    // NAVEGACIÓN — sin cambios
    // ─────────────────────────────────────────────
    @FXML
    public void irADashboard() {
        navegar("/org/example/vista/MenuPrincipal.fxml");
    }

    @FXML
    public void irAVentas() {
        navegar("/org/example/vista/Ventas.fxml");
    }

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

    @FXML
    public void irAReportes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Reportes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void irACorteCaja(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/CorteCaja.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAInventario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/Inventario.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navegar(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) flowTarjetas.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }


    public void irAClientes(ActionEvent actionEvent) {
        navegar("/org/example/vista/Clientes.fxml");
    }
}
