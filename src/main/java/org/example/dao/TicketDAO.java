package org.example.dao;

import org.example.modelo.Ticket;
import org.example.modelo.Ticket.LineaTicket;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de tickets.
 * SOLO LEE datos que PagoController ya guardó; no inserta ventas ni detalles.
 * Su única responsabilidad es reconstruir el Ticket completo desde la BD
 * para poder imprimirlo o mostrarlo.
 */
public class TicketDAO {

    /**
     * Reconstruye un Ticket completo a partir del id_venta ya existente.
     * Usa las tablas: ventas, detalle_venta, productos, pagos, usuarios.
     */
    public Ticket obtenerTicketPorVenta(int idVenta) throws Exception {
        String sqlVenta =
                "SELECT v.id_venta, v.fecha, v.total, v.id_caja, " +
                        "       u.nombre AS cajero " +
                        "FROM ventas v " +
                        "JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                        "WHERE v.id_venta = ?";

        String sqlDetalle =
                "SELECT p.nombre, dv.cantidad, dv.precio_unitario, dv.subtotal, p.costo " +
                        "FROM detalle_venta dv " +
                        "JOIN productos p ON dv.id_producto = p.id_producto " +
                        "WHERE dv.id_venta = ?";

        String sqlPago =
                "SELECT monto_recibido, cambio " +
                        "FROM pagos " +
                        "WHERE id_venta = ?";

        try (Connection con = ConexionDB.getConexion()) {

            // ── 1. Datos de la venta ──────────────────────────────────────
            Ticket ticket = new Ticket();
            try (PreparedStatement ps = con.prepareStatement(sqlVenta)) {
                ps.setInt(1, idVenta);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new Exception("No se encontró la venta con id: " + idVenta);
                }
                ticket.setIdVenta(rs.getInt("id_venta"));
                ticket.setFechaHora(rs.getTimestamp("fecha").toLocalDateTime());
                ticket.setTotal(rs.getDouble("total"));
                ticket.setNumeroCaja(rs.getInt("id_caja"));
                ticket.setNombreCajero(rs.getString("cajero"));
            }

            // ── 2. Líneas del detalle ────────────────────────────────────
            List<LineaTicket> lineas = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idVenta);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    LineaTicket linea = new LineaTicket(
                            rs.getString("nombre"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"),
                            rs.getDouble("costo")   // ← esto era lo que faltaba
                    );
                    linea.setSubtotal(rs.getDouble("subtotal"));
                    lineas.add(linea);
                }
            }
            ticket.setLineas(lineas);

            // Subtotal calculado desde líneas
            double subtotal = lineas.stream()
                    .mapToDouble(LineaTicket::getSubtotal).sum();
            ticket.setSubtotal(subtotal);

            // ── 3. Datos del pago ────────────────────────────────────────
            try (PreparedStatement ps = con.prepareStatement(sqlPago)) {
                ps.setInt(1, idVenta);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ticket.setMontoRecibido(rs.getDouble("monto_recibido"));
                    ticket.setCambio(rs.getDouble("cambio"));
                }
            }

            return ticket;
        }
    }

    /**
     * Devuelve los últimos N tickets registrados (para historial).
     */
    public List<Ticket> obtenerUltimosTickets(int limite) throws Exception {
        String sql =
                "SELECT v.id_venta, v.fecha, v.total, v.id_caja, u.nombre AS cajero " +
                        "FROM ventas v " +
                        "JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                        "ORDER BY v.fecha DESC LIMIT ?";

        List<Ticket> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ticket t = new Ticket();
                t.setIdVenta(rs.getInt("id_venta"));
                t.setFechaHora(rs.getTimestamp("fecha").toLocalDateTime());
                t.setTotal(rs.getDouble("total"));
                t.setNumeroCaja(rs.getInt("id_caja"));
                t.setNombreCajero(rs.getString("cajero"));
                lista.add(t);
            }
        }
        return lista;
    }

    public List<Ticket> obtenerTicketsPorFecha(LocalDateTime inicio, LocalDateTime fin) throws Exception {
        String sql = """
        SELECT v.id_venta
        FROM ventas v
        WHERE v.fecha BETWEEN ? AND ?
        ORDER BY v.fecha DESC
    """;

        // Paso 1 — recolectar todos los IDs primero (cierra el ResultSet antes de continuar)
        List<Integer> ids = new ArrayList<>();

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {


            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(fin));

            // TEMPORAL - borrar después
            System.out.println("▶ Query BETWEEN " + Timestamp.valueOf(inicio) + " AND " + Timestamp.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_venta"));
                    System.out.println("  → ID encontrado: " + rs.getInt("id_venta"));  // ← ya cerrado, usar ids
                }
            }
            System.out.println("▶ Total IDs: " + ids.size());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_venta"));
                }
            } // ← el ResultSet se cierra aquí antes de hacer más consultas
        }

        // Paso 2 — ahora sí obtener cada ticket con su propia conexión
        List<Ticket> lista = new ArrayList<>();
        for (int idVenta : ids) {
            lista.add(obtenerTicketPorVenta(idVenta));
        }


        return lista;
    }
}
