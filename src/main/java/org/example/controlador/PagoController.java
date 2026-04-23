package org.example.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class PagoController {

    @FXML private Label lblTotalPagar;
    @FXML private Label lblClientePago;
    @FXML private Label lblCambio;
    @FXML private Label lblCambioMixto;
    @FXML private Label lblInfoFiado;
    @FXML private TextField txtDineroRecibido;
    @FXML private TextField txtReferencia;
    @FXML private TextField txtReferenciaTransferencia;
    @FXML private TextField txtEfectivoMixto;
    @FXML private TextField txtTarjetaMixto;
    @FXML private Button btnConfirmar;
    @FXML private Button btnEfectivo;
    @FXML private Button btnTarjeta;
    @FXML private Button btnTransferencia;
    @FXML private Button btnMixto;
    @FXML private Button btnFiado;
    @FXML private VBox panelEfectivo;
    @FXML private VBox panelTarjeta;
    @FXML private VBox panelTransferencia;
    @FXML private VBox panelMixto;
    @FXML private VBox panelFiado;

    // Dólares
    @FXML private Button btnDolares;
    @FXML private VBox panelDolares;
    @FXML private TextField txtDolaresRecibidos;
    @FXML private Label lblCambioDolares;
    @FXML private Label lblEquivalentePesos;
    @FXML private Label lblTipoCambio;

    // Mixto pesos y dólares
    @FXML private Button btnMixtoUSD;
    @FXML private VBox panelMixtoUSD;
    @FXML private TextField txtPesosMixtoUSD;
    @FXML private TextField txtDolaresMixtoUSD;
    @FXML private Label lblEquivalenteMixtoUSD;
    @FXML private Label lblCambioMixtoUSD;

    private double total;
    private double tipoCambioDolar = SesionUsuario.getInstancia().getTipoCambioDolar();;
    private Map<Integer, Object[]> carrito;
    private VentasController ventasController;
    private int idCliente = 1;
    private String nombreCliente = "Publico General";
    private double limiteCredito = 0;
    private double saldoCliente = 0;
    private String metodoPago = "EFECTIVO";



    // Datos
    public void setDatos(double total, Map<Integer, Object[]> carrito,
                         VentasController ventasController,
                         int idCliente, String nombreCliente,
                         double limiteCredito, double saldoCliente) {
        this.total            = total;
        this.carrito          = carrito;
        this.ventasController = ventasController;
        this.idCliente        = idCliente;
        this.nombreCliente    = nombreCliente;
        this.limiteCredito    = limiteCredito;
        this.saldoCliente     = saldoCliente;

        lblTotalPagar.setText("$" + String.format("%.2f", total));
        lblClientePago.setText(nombreCliente);

        // Fiado
        if (limiteCredito > 0) {
            double disponible = limiteCredito - saldoCliente;
            lblInfoFiado.setText("Credito disponible: $" + String.format("%.2f", disponible));
            btnFiado.setDisable(disponible < total);
        } else {
            btnFiado.setDisable(true);
            btnFiado.setStyle(btnFiado.getStyle() + "; -fx-opacity: 0.4;");
        }

        // Listener efectivo
        txtDineroRecibido.textProperty().addListener((obs, old, nuevo) -> {
            try {
                double recibido = Double.parseDouble(nuevo);
                double cambio = recibido - total;
                lblCambio.setText("$" + String.format("%.2f", Math.max(cambio, 0)));
                lblCambio.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " +
                        (cambio >= 0 ? "#3B6D11" : "#C0392B") + ";");
            } catch (NumberFormatException e) {
                lblCambio.setText("$0.00");
            }
        });

        // Listener mixto pesos y tarjeta
        Runnable calcularMixto = () -> {
            try {
                double ef  = Double.parseDouble(txtEfectivoMixto.getText().isEmpty() ? "0" : txtEfectivoMixto.getText());
                double tar = Double.parseDouble(txtTarjetaMixto.getText().isEmpty()  ? "0" : txtTarjetaMixto.getText());
                double cambio = (ef + tar) - total;
                lblCambioMixto.setText("$" + String.format("%.2f", Math.max(cambio, 0)));
                lblCambioMixto.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " +
                        (cambio >= 0 ? "#3B6D11" : "#C0392B") + ";");
            } catch (NumberFormatException e) {
                lblCambioMixto.setText("$0.00");
            }
        };
        txtEfectivoMixto.textProperty().addListener((obs, old, nuevo) -> calcularMixto.run());
        txtTarjetaMixto.textProperty().addListener((obs, old, nuevo)  -> calcularMixto.run());

        // Listener dólares solos
        txtDolaresRecibidos.textProperty().addListener((obs, old, nuevo) -> {
            try {
                double dolares = Double.parseDouble(nuevo);
                double enPesos = dolares * tipoCambioDolar;
                double cambio  = enPesos - total;
                lblCambioDolares.setText("$" + String.format("%.2f", Math.max(cambio, 0)) +
                        " MXN  (" + String.format("%.2f", Math.max(cambio / tipoCambioDolar, 0)) + " USD)");
                lblCambioDolares.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " +
                        (cambio >= 0 ? "#3B6D11" : "#C0392B") + ";");
                lblEquivalentePesos.setText("≈ $" + String.format("%.2f", enPesos) + " MXN");
            } catch (NumberFormatException e) {
                lblCambioDolares.setText("$0.00");
                lblEquivalentePesos.setText("");
            }
        });

        // Listener mixto pesos y dólares
        Runnable calcularMixtoUSD = () -> {
            try {
                double pesos   = Double.parseDouble(txtPesosMixtoUSD.getText().isEmpty()   ? "0" : txtPesosMixtoUSD.getText());
                double dolares = Double.parseDouble(txtDolaresMixtoUSD.getText().isEmpty() ? "0" : txtDolaresMixtoUSD.getText());
                double enPesos = dolares * tipoCambioDolar;
                double suma    = pesos + enPesos;
                double cambio  = suma - total;
                lblEquivalenteMixtoUSD.setText("Dólares equivalen a: $" + String.format("%.2f", enPesos) + " MXN");
                lblCambioMixtoUSD.setText("$" + String.format("%.2f", Math.max(cambio, 0)) + " MXN");
                lblCambioMixtoUSD.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " +
                        (cambio >= 0 ? "#3B6D11" : "#C0392B") + ";");
            } catch (NumberFormatException e) {
                lblEquivalenteMixtoUSD.setText("");
                lblCambioMixtoUSD.setText("$0.00");
            }
        };
        txtPesosMixtoUSD.textProperty().addListener((obs, old, nuevo)   -> calcularMixtoUSD.run());
        txtDolaresMixtoUSD.textProperty().addListener((obs, old, nuevo) -> calcularMixtoUSD.run());

        // Enter confirma en efectivo y dólares
        txtDineroRecibido.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) handleConfirmar();
        });
        txtDolaresRecibidos.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) handleConfirmar();
        });



        lblTipoCambio.setText("$" + String.format("%.2f", tipoCambioDolar) + " MXN/USD");

        // Efectivo por defecto
        seleccionarEfectivo();
        javafx.application.Platform.runLater(() -> txtDineroRecibido.requestFocus());
    }

    // Mostrar solo el panel indicado
    private void mostrarPanel(VBox panel) {
        panelEfectivo.setVisible(false);      panelEfectivo.setManaged(false);
        panelTarjeta.setVisible(false);       panelTarjeta.setManaged(false);
        panelTransferencia.setVisible(false); panelTransferencia.setManaged(false);
        panelMixto.setVisible(false);         panelMixto.setManaged(false);
        panelFiado.setVisible(false);         panelFiado.setManaged(false);
        panelDolares.setVisible(false);       panelDolares.setManaged(false);
        panelMixtoUSD.setVisible(false);      panelMixtoUSD.setManaged(false);
        panel.setVisible(true);
        panel.setManaged(true);
    }

    // Resalta botón activo
    private void resaltarBoton(Button activo) {
        String estiloActivo   = "-fx-background-color: #6B4226; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold;";
        String estiloInactivo = "-fx-background-color: white; -fx-text-fill: #6B4226; -fx-background-radius: 8; -fx-border-color: #6B4226; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand; -fx-font-size: 12px;";
        btnEfectivo.setStyle(estiloInactivo);
        btnTarjeta.setStyle(estiloInactivo);
        btnTransferencia.setStyle(estiloInactivo);
        btnMixto.setStyle(estiloInactivo);
        btnFiado.setStyle(estiloInactivo);
        btnDolares.setStyle(estiloInactivo);
        btnMixtoUSD.setStyle(estiloInactivo);
        activo.setStyle(estiloActivo);
    }

    // Selectores de metodo

    @FXML public void seleccionarEfectivo() {
        metodoPago = "EFECTIVO";
        mostrarPanel(panelEfectivo);
        resaltarBoton(btnEfectivo);
        txtDineroRecibido.requestFocus();
    }

    @FXML public void seleccionarTarjeta() {
        metodoPago = "TARJETA";
        mostrarPanel(panelTarjeta);
        resaltarBoton(btnTarjeta);
    }

    @FXML public void seleccionarTransferencia() {
        metodoPago = "TRANSFERENCIA";
        mostrarPanel(panelTransferencia);
        resaltarBoton(btnTransferencia);
    }

    @FXML public void seleccionarMixto() {
        metodoPago = "MIXTO";
        mostrarPanel(panelMixto);
        resaltarBoton(btnMixto);
        txtEfectivoMixto.requestFocus();
    }

    @FXML public void seleccionarFiado() {
        metodoPago = "FIADO";
        mostrarPanel(panelFiado);
        resaltarBoton(btnFiado);
    }

    @FXML public void seleccionarDolares() {
        metodoPago = "DOLARES";
        mostrarPanel(panelDolares);
        resaltarBoton(btnDolares);
        txtDolaresRecibidos.requestFocus();
    }

    @FXML public void seleccionarMixtoUSD() {
        metodoPago = "MIXTO_USD";
        mostrarPanel(panelMixtoUSD);
        resaltarBoton(btnMixtoUSD);
        txtPesosMixtoUSD.requestFocus();
    }

    // Confirmar cobro
    @FXML
    public void handleConfirmar() {
        double montoEfectivo = 0;
        double montoTarjeta  = 0;
        double cambio        = 0;

        switch (metodoPago) {
            case "EFECTIVO":
                if (txtDineroRecibido.getText().trim().isEmpty()) {
                    mostrarAlerta("Campo vacío", "Ingresa el dinero recibido.");
                    return;
                }
                try {
                    montoEfectivo = Double.parseDouble(txtDineroRecibido.getText().trim());
                    cambio = montoEfectivo - total;
                    if (montoEfectivo < total) {
                        mostrarAlerta("Pago insuficiente", "El dinero recibido es menor al total.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Ingresa un numero valido.");
                    return;
                }
                break;

            case "TARJETA":
            case "TRANSFERENCIA":
                montoTarjeta = total;
                break;

            case "MIXTO":
                try {
                    montoEfectivo = Double.parseDouble(txtEfectivoMixto.getText().isEmpty() ? "0" : txtEfectivoMixto.getText());
                    montoTarjeta  = Double.parseDouble(txtTarjetaMixto.getText().isEmpty()  ? "0" : txtTarjetaMixto.getText());
                    double suma = montoEfectivo + montoTarjeta;
                    cambio = suma - total;
                    if (suma < total) {
                        mostrarAlerta("Pago insuficiente", "La suma de los pagos es menor al total.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Ingresa montos validos.");
                    return;
                }
                break;

            case "FIADO":
                double disponible = limiteCredito - saldoCliente;
                if (total > disponible) {
                    mostrarAlerta("Credito insuficiente", "El cliente no tiene suficiente credito.");
                    return;
                }
                break;

            case "DOLARES":
                if (txtDolaresRecibidos.getText().trim().isEmpty()) {
                    mostrarAlerta("Campo vacío", "Ingresa los dólares recibidos.");
                    return;
                }
                try {
                    double dolares = Double.parseDouble(txtDolaresRecibidos.getText().trim());
                    montoEfectivo  = dolares * tipoCambioDolar;
                    cambio         = montoEfectivo - total;
                    if (montoEfectivo < total) {
                        mostrarAlerta("Pago insuficiente",
                                String.format("Con $%.2f USD (≈ $%.2f MXN) no alcanza para cubrir $%.2f MXN.",
                                        dolares, montoEfectivo, total));
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Ingresa un número válido.");
                    return;
                }
                break;

            case "MIXTO_USD":
                try {
                    double pesos   = Double.parseDouble(txtPesosMixtoUSD.getText().isEmpty()   ? "0" : txtPesosMixtoUSD.getText());
                    double dolares = Double.parseDouble(txtDolaresMixtoUSD.getText().isEmpty() ? "0" : txtDolaresMixtoUSD.getText());
                    double enPesos = dolares * tipoCambioDolar;
                    double suma    = pesos + enPesos;
                    cambio         = suma - total;
                    if (suma < total) {
                        mostrarAlerta("Pago insuficiente",
                                String.format("$%.2f MXN + $%.2f USD (≈ $%.2f MXN) = $%.2f MXN. Faltan $%.2f MXN.",
                                        pesos, dolares, enPesos, suma, total - suma));
                        return;
                    }
                    montoEfectivo = suma;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Ingresa montos válidos.");
                    return;
                }
                break;
        }

        guardarVenta(montoEfectivo, montoTarjeta, cambio);
    }

    // Guardar venta
    private void guardarVenta(double montoEfectivo, double montoTarjeta, double cambio) {
        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);

            // Venta
            PreparedStatement psVenta = con.prepareStatement(
                    "INSERT INTO ventas (total, id_usuario, id_caja, id_cliente) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            psVenta.setDouble(1, total);
            psVenta.setInt(2, SesionUsuario.getInstancia().getIdUsuario());
            psVenta.setInt(3, SesionUsuario.getInstancia().getIdCaja());
            psVenta.setInt(4, idCliente);
            psVenta.executeUpdate();

            ResultSet rs = psVenta.getGeneratedKeys();
            rs.next();
            int idVenta = rs.getInt(1);

            // Detalle y stock
            for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
                int idProducto  = entry.getKey();
                double precio   = (double) entry.getValue()[1];
                int cantidad    = (int)    entry.getValue()[2];
                double subtotal = precio * cantidad;

                PreparedStatement psDetalle = con.prepareStatement(
                        "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)");
                psDetalle.setInt(1, idVenta);
                psDetalle.setInt(2, idProducto);
                psDetalle.setInt(3, cantidad);
                psDetalle.setDouble(4, precio);
                psDetalle.setDouble(5, subtotal);
                psDetalle.executeUpdate();

                PreparedStatement psStock = con.prepareStatement(
                        "UPDATE productos SET stock = stock - ? WHERE id_producto = ?");
                psStock.setInt(1, cantidad);
                psStock.setInt(2, idProducto);
                psStock.executeUpdate();
            }

            // Pago
            if (metodoPago.equals("FIADO")) {
                PreparedStatement psSaldo = con.prepareStatement(
                        "UPDATE clientes SET saldo_actual = saldo_actual + ? WHERE id_cliente = ?");
                psSaldo.setDouble(1, total);
                psSaldo.setInt(2, idCliente);
                psSaldo.executeUpdate();

                PreparedStatement psCargo = con.prepareStatement(
                        "INSERT INTO pagos_cliente (id_cliente, monto, tipo, id_venta, notas) VALUES (?, ?, 'CARGO', ?, 'Venta a credito')");
                psCargo.setInt(1, idCliente);
                psCargo.setDouble(2, total);
                psCargo.setInt(3, idVenta);
                psCargo.executeUpdate();
            } else {
                String ref = switch (metodoPago) {
                    case "TARJETA"       -> txtReferencia.getText().trim();
                    case "TRANSFERENCIA" -> txtReferenciaTransferencia.getText().trim();
                    case "DOLARES"       -> txtDolaresRecibidos.getText().trim()
                            + " USD (x$" + String.format("%.2f", tipoCambioDolar) + ")";
                    case "MIXTO_USD"     -> txtPesosMixtoUSD.getText().trim() + " MXN + "
                            + txtDolaresMixtoUSD.getText().trim()
                            + " USD (x$" + String.format("%.2f", tipoCambioDolar) + ")";
                    default -> "";
                };

                PreparedStatement psPago = con.prepareStatement(
                        "INSERT INTO pagos (id_venta, tipo_pago, monto_recibido, cambio) VALUES (?, ?, ?, ?)");
                psPago.setInt(1, idVenta);
                psPago.setString(2, metodoPago);
                psPago.setDouble(3, montoEfectivo + montoTarjeta);
                psPago.setDouble(4, cambio);
                psPago.executeUpdate();
            }

            con.commit();

            Stage stage = (Stage) btnConfirmar.getScene().getWindow();
            stage.close();
            ventasController.ventaCompletada();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar la venta.");
        }
    }

    @FXML
    public void handleCancelar() {
        Stage stage = (Stage) btnConfirmar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}