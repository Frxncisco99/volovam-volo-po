package org.example.servicio;

import org.example.dao.TicketDAO;
import org.example.modelo.Ticket;
import org.example.modelo.Ticket.LineaTicket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orquesta la generación e impresión del ticket.
 *
 * Flujo:
 *   1. PagoController llama a procesarTicket(idVenta, ...) justo después del commit.
 *   2. TicketService reconstruye el Ticket desde la BD (TicketDAO).
 *   3. Abre la vista previa (TicketController / Ticket.fxml).
 *   4. Desde la vista previa el cajero puede imprimir con TicketImpresora.
 */
public class TicketService {

    private final TicketDAO ticketDAO       = new TicketDAO();
    private final TicketImpresora impresora = new TicketImpresora();

    // ── Modo 1: Reconstruir desde BD (usado normalmente después del commit) ─

    /**
     * Reconstruye el ticket leyendo la BD y lo devuelve listo para mostrar/imprimir.
     *
     * @param idVenta El id generado por PagoController tras el INSERT de ventas.
     */
    public Ticket generarDesdeDB(int idVenta) throws Exception {
        return ticketDAO.obtenerTicketPorVenta(idVenta);
    }

    // ── Modo 2: Construir en memoria (por si se necesita sin ir a BD) ────────

    /**
     * Construye el ticket directamente desde los objetos en memoria.
     * Útil si queremos mostrar la vista previa ANTES de que la BD devuelva datos.
     *
     * @param idVenta        Id de la venta recién insertada.
     * @param carrito        Map<idProducto, Object[]{nombre, precio, cantidad}>
     * @param total          Total de la venta.
     * @param montoRecibido  Dinero entregado por el cliente.
     * @param cambio         Cambio devuelto.
     * @param nombreCajero   Nombre del cajero en sesión.
     * @param numeroCaja     Id de caja en sesión.
     */
    public Ticket generarDesdeMemoria(int idVenta,
                                      Map<Integer, Object[]> carrito,
                                      double total,
                                      double montoRecibido,
                                      double cambio,
                                      String nombreCajero,
                                      int numeroCaja) {
        List<LineaTicket> lineas = new ArrayList<>();
        for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
            Object[] item   = entry.getValue();
            String nombre   = (String) item[0];
            double precio   = (double) item[1];
            int    cantidad = (int)    item[2];
            lineas.add(new LineaTicket(nombre, cantidad, precio));
        }

        return new Ticket(
                idVenta,
                java.time.LocalDateTime.now(),
                nombreCajero,
                lineas,
                total,
                montoRecibido,
                cambio,
                numeroCaja
        );
    }

    // ── Impresión ────────────────────────────────────────────────────────────

    /**
     * Imprime el ticket directamente en la impresora térmica.
     * Lanza excepción si no hay impresora o falla el envío.
     */
    public void imprimir(Ticket ticket) throws Exception {
        impresora.imprimir(ticket);
    }

    /**
     * Devuelve el texto plano del ticket para mostrarlo en pantalla.
     */
    public String textoPlano(Ticket ticket) {
        return impresora.generarTextoPlano(ticket);
    }
}