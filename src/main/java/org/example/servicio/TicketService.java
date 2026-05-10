package org.example.servicio;

import org.example.dao.TicketDAO;
import org.example.modelo.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Orquesta la generación e impresión del ticket.
 *
 * CAMBIO: textoPlano() ya no delega a TicketImpresora (layout hardcodeado).
 * Ahora carga la configuración desde Preferences y usa TicketRenderer,
 * la misma fuente de verdad que ConfiguracionController y TicketPreviewController.
 *
 * Métodos intactos: generarDesdeDB(), generarDesdeMemoria(), imprimir().
 */
public class TicketService {

    private final TicketDAO       ticketDAO = new TicketDAO();
    private final TicketImpresora impresora = new TicketImpresora();

    // ── Construcción del ticket ───────────────────────────────────────────────

    public Ticket generarDesdeDB(int idVenta) throws Exception {
        return ticketDAO.obtenerTicketPorVenta(idVenta);
    }

    public Ticket generarDesdeMemoria(int idVenta,
                                      Map<Integer, Object[]> carrito,
                                      double total,
                                      double montoRecibido,
                                      double cambio,
                                      String nombreCajero,
                                      int numeroCaja) {
        List<Ticket.LineaTicket> lineas = new ArrayList<>();
        for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
            Object[] item   = entry.getValue();
            String   nombre = (String) item[0];
            double   precio = (double) item[1];
            int      cant   = (int)    item[2];
            lineas.add(new Ticket.LineaTicket(nombre, cant, precio));
        }
        return new Ticket(idVenta,
                java.time.LocalDateTime.now(),
                nombreCajero, lineas,
                total, montoRecibido, cambio, numeroCaja);
    }

    // ── Impresión ─────────────────────────────────────────────────────────────

    /**
     * Imprime usando la configuración guardada en Preferences.
     * Usa TicketRenderer internamente → mismo layout que la vista previa.
     */
    public void imprimir(Ticket ticket) throws Exception {
        Prefs p = cargarPrefs();
        impresora.imprimirConRenderer(ticket,
                p.nombre, p.giro, p.direccion, p.ciudad, p.telefono,
                p.encabezado, p.pie, p.aviso,
                p.logo, p.folio, p.desglose, p.qr,
                true, true,   // fecha y cajero siempre en impresión real
                p.ancho);
    }

    /**
     * Devuelve el texto del ticket para mostrarlo en TicketController.
     * Usa TicketRenderer con la configuración guardada → idéntico a la vista previa.
     */
    public String textoPlano(Ticket ticket) {
        Prefs p = cargarPrefs();
        return TicketRenderer.generar(ticket,
                p.nombre, p.giro, p.direccion, p.ciudad, p.telefono,
                p.encabezado, p.pie, p.aviso,
                p.logo, p.folio, p.desglose, p.qr,
                true, true,
                p.ancho);
    }

    // ── Carga de Preferences ──────────────────────────────────────────────────

    private Prefs cargarPrefs() {
        Preferences prefs = Preferences.userNodeForPackage(
                org.example.controlador.ConfiguracionController.class);
        return new Prefs(prefs);
    }

    /** DTO interno para no repetir la lógica de Preferences en cada método. */
    private static class Prefs {
        final String  nombre, giro, direccion, ciudad, telefono;
        final String  encabezado, pie, aviso;
        final boolean logo, folio, desglose, qr;
        final int     ancho;

        Prefs(Preferences p) {
            nombre    = p.get("ticket_nombre",    "");
            giro      = p.get("ticket_giro",      "");
            direccion = p.get("ticket_direccion", "");
            ciudad    = p.get("ticket_ciudad",    "");
            telefono  = p.get("ticket_telefono",  "");
            encabezado = p.get("ticket_encabezado", "");
            pie       = p.get("ticket_pie",       "");
            aviso     = p.get("ticket_aviso",
                    "Este ticket no es comprobante fiscal");
            logo      = p.getBoolean("ticket_logo",     true);
            folio     = p.getBoolean("ticket_folio",    true);
            desglose  = p.getBoolean("ticket_desglose", true);
            qr        = p.getBoolean("ticket_qr",       false);
            String anchoPapel = p.get("ticket_ancho", "58 mm");
            ancho     = "58 mm".equals(anchoPapel)
                    ? TicketRenderer.ANCHO_58MM
                    : TicketRenderer.ANCHO_80MM;
        }
    }
}