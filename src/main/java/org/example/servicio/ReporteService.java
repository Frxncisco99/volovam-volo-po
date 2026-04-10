package org.example.servicio;

import org.example.dao.ProductoDAO;
import org.example.dao.TicketDAO;
import org.example.modelo.Producto;
import org.example.modelo.Ticket;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReporteService {

    private TicketDAO ticketDAO = new TicketDAO();
    private ProductoDAO productoDAO = new ProductoDAO();

    // 🔹 Obtener tickets por rango
    public List<Ticket> obtenerTickets(LocalDateTime inicio, LocalDateTime fin) throws Exception {
        return ticketDAO.obtenerTicketsPorFecha(inicio, fin);
    }

    // 🔹 Total vendido
    public double calcularTotal(List<Ticket> tickets) {
        return tickets.stream()
                .mapToDouble(Ticket::getTotal)
                .sum();
    }

    // 🔹 Número de tickets
    public int contarTickets(List<Ticket> tickets) {
        return tickets.size();
    }

    // 🔹 Promedio
    public double calcularPromedio(List<Ticket> tickets) {
        if (tickets.isEmpty()) return 0;
        return calcularTotal(tickets) / tickets.size();
    }

    // 🔹 Top productos
    public Map<String, Integer> topProductos(List<Ticket> tickets) {
        Map<String, Integer> mapa = new HashMap<>();

        for (Ticket t : tickets) {
            for (Ticket.LineaTicket l : t.getLineas()) {
                mapa.merge(l.getNombreProducto(), l.getCantidad(), Integer::sum);
            }
        }

        return mapa.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a,b)->a,
                        LinkedHashMap::new
                ));
    }

    // 🔹 Bajo inventario
    public List<Producto> obtenerBajoStock() {
        return productoDAO.obtenerProductos().stream()
                .filter(Producto::isBajoStock)
                .toList();
    }
}