package org.example.controlador;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmpleadosController {

    @FXML private TableView<ObservableList<String>> tablaEmpleados;
    @FXML private TableColumn<ObservableList<String>, String> colId;
    @FXML private TableColumn<ObservableList<String>, String> colNombre;
    @FXML private TableColumn<ObservableList<String>, String> colUsuario;
    @FXML private TableColumn<ObservableList<String>, String> colRol;
    @FXML private TableColumn<ObservableList<String>, String> colEstado;
    @FXML private TableColumn<ObservableList<String>, String> colAcciones;
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

        configurarTabla();
        cargarEmpleados();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        colUsuario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        colRol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));

        // Columna de acciones con botones
        colAcciones.setCellFactory(col -> new TableCell<>() {
            final Button btnEditar = new Button("Editar");
            final Button btnEliminar = new Button("Eliminar");
            final HBox hbox = new HBox(6, btnEditar, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #6B4226; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px;");

                btnEditar.setOnAction(e -> {
                    ObservableList<String> fila = getTableView().getItems().get(getIndex());
                    mostrarDialogoEmpleado(
                            Integer.parseInt(fila.get(0)),
                            fila.get(1), fila.get(2), fila.get(3)
                    );
                });

                btnEliminar.setOnAction(e -> {
                    ObservableList<String> fila = getTableView().getItems().get(getIndex());
                    eliminarEmpleado(Integer.parseInt(fila.get(0)), fila.get(1));
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void cargarEmpleados() {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();
        String sql = "SELECT u.id_usuario, u.nombre, u.usuario, r.nombre, u.activo FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(String.valueOf(rs.getInt(1)));
                fila.add(rs.getString(2));
                fila.add(rs.getString(3));
                fila.add(rs.getString(4));
                fila.add(rs.getInt(5) == 1 ? "Activo" : "Inactivo");
                datos.add(fila);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tablaEmpleados.setItems(datos);
    }

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
        String sqlRol = "SELECT id_rol FROM roles WHERE nombre = ?";
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
                    ps = con.prepareStatement(sql);
                    ps.setString(1, nombre);
                    ps.setString(2, usuario);
                    ps.setInt(3, idRol);
                    ps.setInt(4, id);
                } else {
                    sql = "UPDATE usuarios SET nombre = ?, usuario = ?, contrasena = ?, id_rol = ? WHERE id_usuario = ?";
                    ps = con.prepareStatement(sql);
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

    private void navegar(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent root = loader.load();
            Stage stage = (Stage) tablaEmpleados.getScene().getWindow();
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
}