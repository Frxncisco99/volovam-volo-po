package org.example.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import org.example.modelo.Ticket;
import org.example.servicio.AuditoriaService;
import org.example.servicio.FolioService;
import org.example.servicio.InventarioMovimientoService;
import org.example.servicio.TicketService;

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
    @FXML private TextField txtOtrosDescripcion;
    @FXML private Button btnConfirmar;
    @FXML private Button btnEfectivo;
    @FXML private Button btnTarjeta;
    @FXML private Button btnTransferencia;
    @FXML private Button btnMixto;
    @FXML private Button btnFiado;
    @FXML private Button btnOtros;
    @FXML private VBox panelEfectivo;
    @FXML private VBox panelTarjeta;
    @FXML private VBox panelTransferencia;
    @FXML private VBox panelMixto;
    @FXML private VBox panelFiado;
    @FXML private VBox panelOtros;
    @FXML private VBox cajasCambio;   // el VBox verde del cambio

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

    // Checkbox imprimir ticket
    @FXML private CheckBox chkImprimirTicket;

    private double total;
    private double tipoCambioDolar;
    private Map<Integer, Object[]> carrito;
    private VentasController ventasController;
    private int idCliente = 1;
    private String nombreCliente = "Publico General";
    private double limiteCredito = 0;
    private double saldoCliente = 0;
    private String metodoPago = "EFECTIVO";

    // Estilos de botones
    private static final String ESTILO_ACTIVO =
            "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 12px;" +
                    "-fx-font-weight: bold; -fx-background-radius: 10;" +
                    "-fx-border-color: #2563eb; -fx-border-width: 2; -fx-border-radius: 10; -fx-cursor: hand;";
    private static final String ESTILO_INACTIVO =
            "-fx-background-color: white; -fx-text-fill: #1a2e4a; -fx-font-size: 12px;" +
                    "-fx-background-radius: 10; -fx-border-color: #c8d8e8;" +
                    "-fx-border-width: 1.5; -fx-border-radius: 10; -fx-cursor: hand;";

    // ── Recibe datos desde VentasController ─────────────────────────────────
    public void setDatos(double total, Map<Integer, Object[]> carrito,
                         VentasController ventasController,
                         int idCliente, String nombreCliente,
                         double limiteCredito, double saldoCliente) {

        this.tipoCambioDolar = SesionUsuario.getInstancia().getTipoCambioDolar();
        this.total            = total;
        this.carrito          = carrito;
        this.ventasController = ventasController;
        this.idCliente        = idCliente;
        this.nombreCliente    = nombreCliente;
        this.limiteCredito    = limiteCredito;
        this.saldoCliente     = saldoCliente;

        lblTotalPagar.setText("$" + String.format("%.2f", total));
        lblClientePago.setText(nombreCliente);
        lblTipoCambio.setText("$" + String.format("%.2f", tipoCambioDolar) + " MXN/USD");
        txtDineroRecibido.setText(String.format("%.2f", total));

        // Deshabilitar Fiado si no hay crédito suficiente
        if (limiteCredito > 0) {
            double disponible = limiteCredito - saldoCliente;
            lblInfoFiado.setText("Credito disponible: $" + String.format("%.2f", disponible));
            btnFiado.setDisable(disponible < total);
        } else {
            btnFiado.setDisable(true);
            btnFiado.setStyle(ESTILO_INACTIVO + "; -fx-opacity: 0.45;");
        }

        // ── Listener efectivo ──────────────────────────────────────────────
        txtDineroRecibido.textProperty().addListener((obs, old, nuevo) -> {
            try {
                double recibido = Double.parseDouble(nuevo);
                double cambio   = recibido - total;
                lblCambio.setText("$" + String.format("%.2f", Math.max(cambio, 0)));
                // Color fondo verde si alcanza, rojo si no
                cajasCambio.setStyle(cajasCambio.getStyle()
                        .replace("-fx-background-color: #22c55e;", "")
                        .replace("-fx-background-color: #ef4444;", "")
                );
                if (cambio >= 0) {
                    cajasCambio.setStyle("-fx-background-color: #22c55e; -fx-background-radius: 10; -fx-padding: 14 16; -fx-min-width: 160; -fx-min-height: 72;");
                } else {
                    cajasCambio.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 10; -fx-padding: 14 16; -fx-min-width: 160; -fx-min-height: 72;");
                }
            } catch (NumberFormatException e) {
                lblCambio.setText("$0.00");
                cajasCambio.setStyle("-fx-background-color: #22c55e; -fx-background-radius: 10; -fx-padding: 14 16; -fx-min-width: 160; -fx-min-height: 72;");
            }
        });

        // ── Listener mixto MXN + tarjeta ──────────────────────────────────
        Runnable calcularMixto = () -> {
            try {
                double ef  = parse(txtEfectivoMixto.getText());
                double tar = parse(txtTarjetaMixto.getText());
                double cambio = (ef + tar) - total;
                lblCambioMixto.setText("$" + String.format("%.2f", Math.max(cambio, 0)));
                lblCambioMixto.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " +
                        (cambio >= 0 ? "#22c55e" : "#ef4444") + ";");
            } catch (NumberFormatException e) {
                lblCambioMixto.setText("$0.00");
            }
        };
        txtEfectivoMixto.textProperty().addListener((obs, o, n) -> calcularMixto.run());
        txtTarjetaMixto.textProperty().addListener((obs, o, n)  -> calcularMixto.run());

        // ── Listener dólares solos ─────────────────────────────────────────
        txtDolaresRecibidos.textProperty().addListener((obs, old, nuevo) -> {
            try {
                double dolares = Double.parseDouble(nuevo);
                double enPesos = dolares * tipoCambioDolar;
                double cambio  = enPesos - total;
                lblCambioDolares.setText("$" + String.format("%.2f", Math.max(cambio, 0)) +
                        " MXN  (" + String.format("%.2f", Math.max(cambio / tipoCambioDolar, 0)) + " USD)");
                lblCambioDolares.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " +
                        (cambio >= 0 ? "#22c55e" : "#ef4444") + ";");
                lblEquivalentePesos.setText("≈ $" + String.format("%.2f", enPesos) + " MXN");
            } catch (NumberFormatException e) {
                lblCambioDolares.setText("$0.00");
                lblEquivalentePesos.setText("");
            }
        });

        // ── Listener mixto MXN + USD ──────────────────────────────────────
        Runnable calcularMixtoUSD = () -> {
            try {
                double pesos   = parse(txtPesosMixtoUSD.getText());
                double dolares = parse(txtDolaresMixtoUSD.getText());
                double enPesos = dolares * tipoCambioDolar;
                double suma    = pesos + enPesos;
                double cambio  = suma - total;
                lblEquivalenteMixtoUSD.setText("Dólares equivalen a: $" + String.format("%.2f", enPesos) + " MXN");
                lblCambioMixtoUSD.setText("$" + String.format("%.2f", Math.max(cambio, 0)) + " MXN");
                lblCambioMixtoUSD.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " +
                        (cambio >= 0 ? "#22c55e" : "#ef4444") + ";");
            } catch (NumberFormatException e) {
                lblEquivalenteMixtoUSD.setText("");
                lblCambioMixtoUSD.setText("$0.00");
            }
        };
        txtPesosMixtoUSD.textProperty().addListener((obs, o, n)   -> calcularMixtoUSD.run());
        txtDolaresMixtoUSD.textProperty().addListener((obs, o, n) -> calcularMixtoUSD.run());

        // Enter confirma
        txtDineroRecibido.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) handleConfirmar();
        });
        txtDolaresRecibidos.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) handleConfirmar();
        });

        // Efectivo por defecto
        seleccionarEfectivo();
        javafx.application.Platform.runLater(() -> txtDineroRecibido.requestFocus());
    }

    // ── Denominaciones rápidas ────────────────────────────────────────────
    @FXML private void denominacion50()  { txtDineroRecibido.setText("50.00"); }
    @FXML private void denominacion100() { txtDineroRecibido.setText("100.00"); }
    @FXML private void denominacion200() { txtDineroRecibido.setText("200.00"); }
    @FXML private void denominacion500() { txtDineroRecibido.setText("500.00"); }

    // ── Mostrar solo el panel indicado ────────────────────────────────────
    private void mostrarPanel(VBox panel) {
        panelEfectivo.setVisible(false);      panelEfectivo.setManaged(false);
        panelTarjeta.setVisible(false);       panelTarjeta.setManaged(false);
        panelTransferencia.setVisible(false); panelTransferencia.setManaged(false);
        panelMixto.setVisible(false);         panelMixto.setManaged(false);
        panelFiado.setVisible(false);         panelFiado.setManaged(false);
        panelDolares.setVisible(false);       panelDolares.setManaged(false);
        panelMixtoUSD.setVisible(false);      panelMixtoUSD.setManaged(false);
        panelOtros.setVisible(false);         panelOtros.setManaged(false);
        panel.setVisible(true);
        panel.setManaged(true);
    }

    private void resaltarBoton(Button activo) {
        for (Button b : new Button[]{btnEfectivo, btnTarjeta, btnTransferencia,
                btnDolares, btnMixtoUSD, btnMixto, btnFiado, btnOtros}) {
            b.setStyle(ESTILO_INACTIVO);
        }
        activo.setStyle(ESTILO_ACTIVO);
    }

    // ── Selectores de método ──────────────────────────────────────────────
    @FXML public void seleccionarEfectivo() {
        metodoPago = "EFECTIVO";
        mostrarPanel(panelEfectivo);
        resaltarBoton(btnEfectivo);
        if (txtDineroRecibido.getText().trim().isEmpty()) {
            txtDineroRecibido.setText(String.format("%.2f", total));
        }
        javafx.application.Platform.runLater(() -> {
            txtDineroRecibido.requestFocus();
            txtDineroRecibido.selectAll();
        });
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
        javafx.application.Platform.runLater(() -> txtEfectivoMixto.requestFocus());
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
        javafx.application.Platform.runLater(() -> txtDolaresRecibidos.requestFocus());
    }
    @FXML public void seleccionarMixtoUSD() {
        metodoPago = "MIXTO_USD";
        mostrarPanel(panelMixtoUSD);
        resaltarBoton(btnMixtoUSD);
        javafx.application.Platform.runLater(() -> txtPesosMixtoUSD.requestFocus());
    }
    @FXML public void seleccionarOtros() {
        metodoPago = "OTROS";
        mostrarPanel(panelOtros);
        resaltarBoton(btnOtros);
        javafx.application.Platform.runLater(() -> txtOtrosDescripcion.requestFocus());
    }

    // ── Confirmar cobro ───────────────────────────────────────────────────
    @FXML
    public void handleConfirmar() {
        double montoEfectivo = 0;
        double montoTarjeta  = 0;
        double cambio        = 0;

        switch (metodoPago) {
            case "EFECTIVO":
                if (txtDineroRecibido.getText().trim().isEmpty()) {
                    mostrarAlerta("Campo vacío", "Ingresa el dinero recibido."); return;
                }
                try {
                    montoEfectivo = Double.parseDouble(txtDineroRecibido.getText().trim());
                    cambio = montoEfectivo - total;
                    if (montoEfectivo < total) {
                        mostrarAlerta("Pago insuficiente", "El dinero recibido es menor al total."); return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Ingresa un numero valido."); return;
                }
                break;

            case "TARJETA":
            case "TRANSFERENCIA":
            case "OTROS":
                montoTarjeta = total;
                break;

            case "MIXTO":
                try {
                    montoEfectivo = parse(txtEfectivoMixto.getText());
                    montoTarjeta  = parse(txtTarjetaMixto.getText());
                    double suma = montoEfectivo + montoTarjeta;
                    cambio = suma - total;
                    if (suma < total) {
                        mostrarAlerta("Pago insuficiente", "La suma de los pagos es menor al total."); return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Ingresa montos validos."); return;
                }
                break;

            case "FIADO":
                double disponible = limiteCredito - saldoCliente;
                if (total > disponible) {
                    mostrarAlerta("Credito insuficiente", "El cliente no tiene suficiente credito."); return;
                }
                break;

            case "DOLARES":
                if (txtDolaresRecibidos.getText().trim().isEmpty()) {
                    mostrarAlerta("Campo vacío", "Ingresa los dólares recibidos."); return;
                }
                try {
                    double dolares = Double.parseDouble(txtDolaresRecibidos.getText().trim());
                    montoEfectivo  = dolares * tipoCambioDolar;
                    cambio         = montoEfectivo - total;
                    if (montoEfectivo < total) {
                        mostrarAlerta("Pago insuficiente",
                                String.format("Con $%.2f USD no alcanza para cubrir $%.2f MXN.", dolares, total));
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Ingresa un número válido."); return;
                }
                break;

            case "MIXTO_USD":
                try {
                    double pesos   = parse(txtPesosMixtoUSD.getText());
                    double dolares = parse(txtDolaresMixtoUSD.getText());
                    double enPesos = dolares * tipoCambioDolar;
                    double suma    = pesos + enPesos;
                    cambio         = suma - total;
                    if (suma < total) {
                        mostrarAlerta("Pago insuficiente",
                                String.format("$%.2f MXN + $%.2f USD = $%.2f MXN. Faltan $%.2f MXN.",
                                        pesos, dolares, suma, total - suma));
                        return;
                    }
                    montoEfectivo = suma;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Ingresa montos válidos."); return;
                }
                break;
        }

        guardarVenta(montoEfectivo, montoTarjeta, cambio);
    }

    // ── Guardar venta en BD ───────────────────────────────────────────────
    private void guardarVenta(double montoEfectivo, double montoTarjeta, double cambio) {
        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);

            // 1. Insertar venta
            PreparedStatement psVenta = con.prepareStatement(
                    "INSERT INTO ventas (total, id_usuario, id_caja, id_cliente, metodo_pago, estado) " +
                            "VALUES (?, ?, ?, ?, ?, 'COMPLETADA')",
                    Statement.RETURN_GENERATED_KEYS);
            psVenta.setDouble(1, total);
            psVenta.setInt(2, SesionUsuario.getInstancia().getIdUsuario());
            psVenta.setInt(3, SesionUsuario.getInstancia().getIdCaja());
            psVenta.setInt(4, idCliente);
            psVenta.setString(5, metodoPago);
            psVenta.executeUpdate();

            ResultSet rs = psVenta.getGeneratedKeys();
            rs.next();
            int idVenta = rs.getInt(1);

            // 2. Detalle + movimientos + stock (en ese orden)
            InventarioMovimientoService invService = InventarioMovimientoService.get();

            for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
                int    idProducto = entry.getKey();
                double precio     = (double) entry.getValue()[1];
                int    cantidad   = (int)    entry.getValue()[2];
                double subtotal   = precio * cantidad;

                // Detalle de venta
                PreparedStatement psDetalle = con.prepareStatement(
                        "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)");
                psDetalle.setInt(1, idVenta);
                psDetalle.setInt(2, idProducto);
                psDetalle.setInt(3, cantidad);
                psDetalle.setDouble(4, precio);
                psDetalle.setDouble(5, subtotal);
                psDetalle.executeUpdate();

                // Registrar movimiento ANTES de descontar stock
                // (así obtenerStock() lee el stock correcto todavía)
                try {
                    invService.registrar(con, idProducto,
                            InventarioMovimientoService.TipoMovimiento.VENTA,
                            cantidad, idVenta, "VENTA",
                            "Venta " + FolioService.venta(idVenta));
                } catch (Exception ex) {
                    ex.printStackTrace(); // no rompe la venta si falla el log
                }

                // Descontar stock
                PreparedStatement psStock = con.prepareStatement(
                        "UPDATE productos SET stock = stock - ? WHERE id_producto = ?");
                psStock.setInt(1, cantidad);
                psStock.setInt(2, idProducto);
                psStock.executeUpdate();
            }

            // 3. Fiado o pago normal
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
                PreparedStatement psPago = con.prepareStatement(
                        "INSERT INTO pagos (id_venta, tipo_pago, monto_recibido, cambio) VALUES (?, ?, ?, ?)");
                psPago.setInt(1, idVenta);
                psPago.setString(2, metodoPago);
                psPago.setDouble(3, montoRegistradoEnPago(montoEfectivo, montoTarjeta));
                psPago.setDouble(4, cambio);
                psPago.executeUpdate();
            }

            con.commit();

            // Auditoría (fuera de transacción)
            AuditoriaService.get().registrar(
                    "VENTA", "ventas", idVenta,
                    String.format("Venta %s — Total: $%.2f — Método: %s — Cliente ID: %d",
                            FolioService.venta(idVenta), total, metodoPago, idCliente)
            );

            int idVentaFinal = idVenta;
            Stage stagePago = (Stage) btnConfirmar.getScene().getWindow();
            stagePago.close();
            ventasController.ventaCompletada();

            if (chkImprimirTicket != null && chkImprimirTicket.isSelected()) {
                try {
                    TicketService ticketService = new TicketService();
                    Ticket ticket = ticketService.generarDesdeDB(idVentaFinal);
                    ticketService.imprimir(ticket);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar la venta.");
        }
    }
    @FXML
    public void handleCancelar() {
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }

    private double parse(String s) {
        return (s == null || s.isEmpty()) ? 0.0 : Double.parseDouble(s);
    }

    private double montoRegistradoEnPago(double montoEfectivo, double montoTarjeta) {
        if ("MIXTO".equals(metodoPago) || "MIXTO_USD".equals(metodoPago)) {
            return montoEfectivo;
        }
        return montoEfectivo + montoTarjeta;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
