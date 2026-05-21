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
import org.kordamp.ikonli.javafx.FontIcon;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.servicio.MarcaService;
import org.example.servicio.PasswordService;
import org.example.servicio.PermisoService;
import org.example.servicio.UsuarioSeguridadService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class  EmpleadosController {

    // id para tarjetas
    @FXML private FlowPane flowTarjetas;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroRol;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private Label lblConteo;
    @FXML private Label lblStatTotal;
    @FXML private Label lblStatActivos;
    @FXML private Label lblStatInactivos;
    @FXML private Label lblStatRoles;

    // Sidebar labels
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblAvatarIniciales;

    // Cache de datos
    private List<ObservableList<String>> todosLosEmpleados = new ArrayList<>();
    private final PasswordService passwordService = new PasswordService();
    private final UsuarioSeguridadService usuarioSeguridadService = new UsuarioSeguridadService();


    @FXML
    public void initialize() {

        // Sesión
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());
        String iniciales = sesion.getNombre().length() >= 2
                ? sesion.getNombre().substring(0, 2).toUpperCase()
                : sesion.getNombre().toUpperCase();
        lblAvatarIniciales.setText(iniciales);

        // Filtros de estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));

        // Listeners
        txtBuscar.textProperty().addListener((o, ov, nv) -> renderTarjetas());
        cmbFiltroRol.valueProperty().addListener((o, ov, nv) -> renderTarjetas());
        cmbFiltroEstado.valueProperty().addListener((o, ov, nv) -> renderTarjetas());

        cargarEmpleados();
    }



    // Cargar desde BD
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
            org.example.servicio.LogService.error("Error no controlado", e);
        }

        // Poblar combo de roles
        cmbFiltroRol.setItems(FXCollections.observableArrayList(roles));

        actualizarStats();
        renderTarjetas();
    }

    // Stats bar
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

    // Render de tarjetas
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

    // Construcción de tarjeta
    private VBox crearTarjeta(ObservableList<String> fila) {
        String id     = fila.get(0);
        String nombre = fila.get(1);
        String usuario= fila.get(2);
        String rol    = fila.get(3);
        String estado = fila.get(4);
        boolean activo = "Activo".equals(estado);

        // Contenedor principal
        VBox card = new VBox();
        card.setPrefWidth(260);
        card.setMaxWidth(260);
        card.getStyleClass().add("empleado-card");

        // Header con gradiente
        StackPane header = new StackPane();
        header.setPrefHeight(90);
        header.getStyleClass().add("empleado-card-header");

        // Badge rol
        Label rolBadge = new Label(rol);
        rolBadge.getStyleClass().add("empleado-role-badge");
        StackPane.setAlignment(rolBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(rolBadge, new Insets(10, 10, 0, 0));

        // Avatar con iniciales
        String ini = nombre.trim().isEmpty() ? "??" :
                nombre.trim().split("\\s+").length >= 2
                        ? String.valueOf(nombre.trim().charAt(0)).toUpperCase() +
                        String.valueOf(nombre.trim().split("\\s+")[1].charAt(0)).toUpperCase()
                        : nombre.substring(0, Math.min(2, nombre.length())).toUpperCase();

        Label avatar = new Label(ini);
        avatar.setPrefSize(54, 54);
        avatar.setMaxSize(54, 54);
        avatar.getStyleClass().add("empleado-avatar");
        StackPane.setAlignment(avatar, Pos.BOTTOM_LEFT);
        StackPane.setMargin(avatar, new Insets(0, 0, -20, 16));

        Label estadoBadge = new Label(activo ? "Activo" : "Inactivo");
        estadoBadge.getStyleClass().add(activo ? "empleado-status-active" : "empleado-status-inactive");
        StackPane.setAlignment(estadoBadge, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(estadoBadge, new Insets(0, 10, 8, 0));

        header.getChildren().addAll(rolBadge, avatar, estadoBadge);

        // Contenido
        VBox body = new VBox(4);
        body.getStyleClass().add("empleado-card-body");

        Label lblNombre = new Label(nombre);
        lblNombre.getStyleClass().add("empleado-name");
        lblNombre.setWrapText(true);

        Label lblUsuario = new Label("@" + usuario);
        lblUsuario.getStyleClass().add("empleado-user");

        body.getChildren().addAll(lblNombre, lblUsuario);

        // Chips
        HBox chips = new HBox(8);
        chips.getStyleClass().add("empleado-chip-row");

        Label chipId  = crearChip("# ID " + id);
        Label chipRol = crearChip("Rol: " + rol);
        chips.getChildren().addAll(chipId, chipRol);

        // Acciones
        HBox acciones = new HBox(8);
        acciones.getStyleClass().add("empleado-actions");
        acciones.setAlignment(Pos.CENTER_LEFT);

        Button btnEditar = new Button("Editar");
        btnEditar.setPrefHeight(32);
        btnEditar.setGraphic(crearIcono("fas-pen"));
        btnEditar.setGraphicTextGap(8);
        btnEditar.getStyleClass().add("empleado-edit-button");
        HBox.setHgrow(btnEditar, Priority.ALWAYS);
        btnEditar.setMaxWidth(Double.MAX_VALUE);
        btnEditar.setOnAction(e -> mostrarDialogoEmpleado(Integer.parseInt(id), nombre, usuario, rol));

        // Boton activar/desactivar
        Button btnToggle = new Button();
        btnToggle.setPrefSize(32, 32);
        btnToggle.setGraphic(crearIcono(activo ? "fas-pause" : "fas-play"));
        btnToggle.getStyleClass().add("empleado-icon-button");
        btnToggle.setTooltip(new Tooltip(activo ? "Desactivar empleado" : "Activar empleado"));
        btnToggle.setOnAction(e -> toggleEstado(Integer.parseInt(id), nombre, activo));

        // Boton eliminar
        Button btnEliminar = new Button();
        btnEliminar.setPrefSize(32, 32);
        btnEliminar.setGraphic(crearIcono("fas-trash-alt"));
        btnEliminar.getStyleClass().add("empleado-danger-button");
        btnEliminar.setTooltip(new Tooltip("Eliminar empleado"));
        btnEliminar.setOnAction(e -> eliminarEmpleado(Integer.parseInt(id), nombre));

        acciones.getChildren().addAll(btnEditar, btnToggle, btnEliminar);

        card.getChildren().addAll(header, body, chips, acciones);
        return card;
    }

    private Label crearChip(String texto) {
        Label chip = new Label(texto);
        chip.getStyleClass().add("empleado-chip");
        return chip;
    }

    private FontIcon crearIcono(String iconLiteral) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(12);
        return icon;
    }

    // Toggle activo/inactivo
    private void toggleEstado(int id, String nombre, boolean estaActivo) {
        String permiso = estaActivo ? PermisoService.USUARIOS_DESACTIVAR : PermisoService.USUARIOS_EDITAR;
        String accion = (estaActivo ? "Desactivar" : "Activar") + " empleado " + nombre;
        if (!requerirPermiso(permiso, accion)) {
            return;
        }
        if (estaActivo && !usuarioSeguridadService.puedeDesactivarUsuario(id)) {
            mostrarAlerta("Acción no permitida", usuarioSeguridadService.mensajeProteccionAdmin(id));
            return;
        }
        int nuevoValor = estaActivo ? 0 : 1;
        String sql = "UPDATE usuarios SET activo = ? WHERE id_usuario = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoValor);
            ps.setInt(2, id);
            ps.executeUpdate();
            cargarEmpleados();
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
            mostrarAlerta("Error", "No se pudo cambiar el estado del empleado.");
        }
    }

    // Filtro limpiar
    @FXML
    public void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroRol.setValue(null);
        cmbFiltroEstado.setValue(null);
    }

    // Crud
    @FXML
    public void handleNuevoEmpleado() {
        mostrarDialogoEmpleado(0, "", "", "cajero");
    }

    private void aplicarEstilosDialogo(Dialog<?> dialog) {
        java.net.URL css = getClass().getResource("/org/example/vista/menuPrincipal.css");
        if (css != null) {
            dialog.getDialogPane().getStylesheets().add(css.toExternalForm());
        }
        dialog.getDialogPane().getStyleClass().add("empleado-dialog");
    }

    private void mostrarDialogoEmpleado(int id, String nombre, String usuario, String rol) {
        boolean nuevo = id == 0;
        String permiso = nuevo ? PermisoService.USUARIOS_CREAR : PermisoService.USUARIOS_EDITAR;
        String accion = nuevo ? "Crear empleado" : "Editar empleado " + nombre;
        if (!requerirPermiso(permiso, accion)) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(nuevo ? "Nuevo empleado" : "Editar empleado");
        aplicarEstilosDialogo(dialog);

        VBox contenido = new VBox(12);
        contenido.getStyleClass().add("empleado-dialog-content");

        TextField txtNombre = new TextField(nombre);
        txtNombre.setPromptText("Nombre completo");
        txtNombre.getStyleClass().add("empleado-dialog-input");

        TextField txtUsuario = new TextField(usuario);
        txtUsuario.setPromptText("Usuario");
        txtUsuario.getStyleClass().add("empleado-dialog-input");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText(id == 0 ? "Contraseña" : "Nueva contraseña (dejar vacío para no cambiar)");
        txtPassword.getStyleClass().add("empleado-dialog-input");

        ComboBox<String> cmbRol = new ComboBox<>();
        cmbRol.getItems().addAll("admin", "cajero", "supervisor");
        cmbRol.setValue(rol);
        cmbRol.getStyleClass().add("empleado-dialog-input");

        contenido.getChildren().addAll(
                new Label("Nombre:"), txtNombre,
                new Label("Usuario:"), txtUsuario,
                new Label("Contraseña:"), txtPassword,
                new Label("Rol:"), cmbRol
        );

        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        org.example.servicio.DialogService.preparar(dialog, flowTarjetas);

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

                if (id != 0 && !usuarioSeguridadService.puedeCambiarRol(id, r)) {
                    mostrarAlerta("Acción no permitida", "No puedes dejar el sistema sin un usuario administrador activo.");
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
        String sqlInsert = "INSERT INTO usuarios (nombre, usuario, contrasena, password_hash, fecha_actualizacion_password, id_rol) VALUES (?, ?, '', ?, NOW(), ?)";
        try (Connection con = ConexionDB.getConexion()) {
            if (!usuarioDisponible(con, usuario, 0)) {
                mostrarAlerta("Error", "El nombre de usuario ya existe.");
                return;
            }
            try (PreparedStatement psRol = con.prepareStatement(sqlRol)) {
                psRol.setString(1, rol);
                try (ResultSet rs = psRol.executeQuery()) {
                    if (!rs.next()) {
                        mostrarAlerta("Error", "El rol seleccionado no existe.");
                        return;
                    }
                    int idRol = rs.getInt(1);
                    try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                        ps.setString(1, nombre);
                        ps.setString(2, usuario);
                        ps.setString(3, passwordService.hash(password));
                        ps.setInt(4, idRol);
                        ps.executeUpdate();
                    }
                }
            }
            cargarEmpleados();
            mostrarAlerta("Exito", "Empleado creado correctamente.");
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
            mostrarAlerta("Error", "No se pudo crear el empleado.");
        }
    }

    private void actualizarEmpleado(int id, String nombre, String usuario, String password, String rol) {
        String sqlRol = "SELECT id_rol FROM roles WHERE nombre = ?";
        try (Connection con = ConexionDB.getConexion()) {
            if (!usuarioDisponible(con, usuario, id)) {
                mostrarAlerta("Error", "El nombre de usuario ya existe.");
                return;
            }
            try (PreparedStatement psRol = con.prepareStatement(sqlRol)) {
                psRol.setString(1, rol);
                try (ResultSet rs = psRol.executeQuery()) {
                    if (!rs.next()) {
                        mostrarAlerta("Error", "El rol seleccionado no existe.");
                        return;
                    }
                    int idRol = rs.getInt(1);
                    String sql;
                    if (password.isEmpty()) {
                        sql = "UPDATE usuarios SET nombre = ?, usuario = ?, id_rol = ? WHERE id_usuario = ?";
                    } else {
                        sql = "UPDATE usuarios SET nombre = ?, usuario = ?, contrasena = '', password_hash = ?, fecha_actualizacion_password = NOW(), id_rol = ? WHERE id_usuario = ?";
                    }
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, nombre);
                        ps.setString(2, usuario);
                        if (password.isEmpty()) {
                            ps.setInt(3, idRol);
                            ps.setInt(4, id);
                        } else {
                            ps.setString(3, passwordService.hash(password));
                            ps.setInt(4, idRol);
                            ps.setInt(5, id);
                        }
                        ps.executeUpdate();
                    }
                }
            }
            cargarEmpleados();
            mostrarAlerta("Exito", "Empleado actualizado correctamente.");
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
            mostrarAlerta("Error", "No se pudo actualizar el empleado.");
        }
    }

    private boolean usuarioDisponible(Connection con, String usuario, int idActual) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE usuario = ? AND id_usuario <> ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next();
            }
        }
    }

    private boolean requerirPermiso(String permiso, String accion) {
        return PermisoService.requerirPermisoOAutorizacionAdmin(permiso, accion);
    }

    private void eliminarEmpleado(int id, String nombre) {
        if (!requerirPermiso(PermisoService.USUARIOS_DESACTIVAR, "Eliminar empleado " + nombre)) {
            return;
        }
        if (!usuarioSeguridadService.puedeDesactivarUsuario(id)) {
            mostrarAlerta("Acción no permitida", usuarioSeguridadService.mensajeProteccionAdmin(id));
            return;
        }
        org.example.servicio.DialogService.confirmar(
                flowTarjetas,
                "Eliminar empleado",
                "Seguro que deseas eliminar a " + nombre + "?"
        ).ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                String sql = "UPDATE usuarios SET activo = 0 WHERE id_usuario = ?";
                try (Connection con = ConexionDB.getConexion();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    cargarEmpleados();
                } catch (Exception e) {
                    org.example.servicio.LogService.error("Error no controlado", e);
                }
            }
        });
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
    @FXML public void irADashboard() {
        navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_REPORTES, "/org/example/vista/MenuPrincipal.fxml");
    }
    @FXML public void irAVentas() {
        navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_VENTAS, "/org/example/vista/Ventas.fxml");
    }
    @FXML private void irAInventario() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_INVENTARIO, "/org/example/vista/Inventario.fxml"); }
    @FXML private void irAClientes() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_CLIENTES, "/org/example/vista/Clientes.fxml"); }
    @FXML private void irAReportes()   { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_REPORTES, "/org/example/vista/Reportes.fxml"); }
    @FXML private void irACorteCaja()  { navegarConPermiso(org.example.servicio.PermisoService.Accion.VER_CORTE_CAJA, "/org/example/vista/CorteCaja.fxml"); }
    @FXML private void irAAuditoria() {
        navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_AUDITORIA, "/org/example/vista/Auditoria.fxml");
    }
    @FXML private void irAConfiguracion() { navegarConPermiso(org.example.servicio.PermisoService.Accion.ACCEDER_CONFIGURACION, "/org/example/vista/Configuracion.fxml"); }

    private void navegar(String ruta) {
        org.example.servicio.NavigationService.cambiarEscena(flowTarjetas, ruta);
    }

    private void navegarConPermiso(org.example.servicio.PermisoService.Accion accion, String ruta) {
        if (!org.example.servicio.PermisoService.puede(accion)) {
            mostrarAlerta("Acceso denegado", "No tienes permiso para acceder a este módulo.");
            return;
        }
        navegar(ruta);
    }

    @FXML
    public void btnCerrar() {
        org.example.servicio.NavigationService.cambiarSesion(flowTarjetas);
    }

    @FXML
    public void salirAplicacion() {
        org.example.servicio.AppExitService.salir(flowTarjetas);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        org.example.servicio.DialogService.info(flowTarjetas, titulo, mensaje);
    }
}
