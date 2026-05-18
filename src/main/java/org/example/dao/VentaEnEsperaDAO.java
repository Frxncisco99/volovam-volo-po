package org.example.dao;

import org.example.modelo.SesionUsuario;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VentaEnEsperaDAO {

    public int guardar(Map<Integer, Object[]> carrito,
                       int idCliente,
                       String nombreCliente,
                       double limiteCredito,
                       double saldoCliente,
                       String etiqueta,
                       double total) throws Exception {
        String sql = """
                INSERT INTO ventas_en_espera
                (id_usuario, id_caja, id_cliente, nombre_cliente, limite_credito, saldo_cliente, etiqueta, carrito_data, total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        SesionUsuario sesion = SesionUsuario.getInstancia();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sesion.getIdUsuario());
            ps.setInt(2, sesion.getIdCaja());
            ps.setInt(3, idCliente);
            ps.setString(4, nombreCliente);
            ps.setDouble(5, limiteCredito);
            ps.setDouble(6, saldoCliente);
            ps.setString(7, etiqueta);
            ps.setString(8, serializarCarrito(carrito));
            ps.setDouble(9, total);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<VentaEnEspera> listarSesionActual() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        String sql = """
                SELECT id_espera, id_cliente, nombre_cliente, limite_credito, saldo_cliente,
                       etiqueta, carrito_data, total, fecha_hora
                FROM ventas_en_espera
                WHERE id_usuario = ? AND id_caja = ?
                ORDER BY fecha_hora ASC, id_espera ASC
                """;
        List<VentaEnEspera> ventas = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sesion.getIdUsuario());
            ps.setInt(2, sesion.getIdCaja());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.add(new VentaEnEspera(
                            rs.getInt("id_espera"),
                            deserializarCarrito(rs.getString("carrito_data")),
                            rs.getInt("id_cliente"),
                            rs.getString("nombre_cliente"),
                            rs.getDouble("limite_credito"),
                            rs.getDouble("saldo_cliente"),
                            rs.getString("etiqueta"),
                            rs.getDouble("total"),
                            rs.getString("fecha_hora")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ventas;
    }

    public void eliminar(int idEspera) throws Exception {
        String sql = "DELETE FROM ventas_en_espera WHERE id_espera = ? AND id_usuario = ? AND id_caja = ?";
        SesionUsuario sesion = SesionUsuario.getInstancia();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEspera);
            ps.setInt(2, sesion.getIdUsuario());
            ps.setInt(3, sesion.getIdCaja());
            ps.executeUpdate();
        }
    }

    private String serializarCarrito(Map<Integer, Object[]> carrito) {
        StringBuilder sb = new StringBuilder();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        for (Map.Entry<Integer, Object[]> entry : carrito.entrySet()) {
            Object[] item = entry.getValue();
            String nombre = String.valueOf(item[0]);
            double precio = ((Number) item[1]).doubleValue();
            int cantidad = ((Number) item[2]).intValue();
            sb.append(entry.getKey()).append('\t')
                    .append(encoder.encodeToString(nombre.getBytes(StandardCharsets.UTF_8))).append('\t')
                    .append(precio).append('\t')
                    .append(cantidad).append('\n');
        }
        return sb.toString();
    }

    private Map<Integer, Object[]> deserializarCarrito(String data) {
        Map<Integer, Object[]> carrito = new HashMap<>();
        if (data == null || data.isBlank()) {
            return carrito;
        }
        Base64.Decoder decoder = Base64.getUrlDecoder();
        for (String line : data.split("\\R")) {
            if (line.isBlank()) continue;
            String[] parts = line.split("\\t");
            if (parts.length != 4) continue;
            try {
                int idProducto = Integer.parseInt(parts[0]);
                String nombre = new String(decoder.decode(parts[1]), StandardCharsets.UTF_8);
                double precio = Double.parseDouble(parts[2]);
                int cantidad = Integer.parseInt(parts[3]);
                carrito.put(idProducto, new Object[]{nombre, precio, cantidad});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return carrito;
    }

    public static class VentaEnEspera {
        private final int idEspera;
        private final Map<Integer, Object[]> carrito;
        private final int idCliente;
        private final String nombreCliente;
        private final double limiteCredito;
        private final double saldoCliente;
        private final String etiqueta;
        private final double total;
        private final String fechaHora;

        public VentaEnEspera(int idEspera, Map<Integer, Object[]> carrito, int idCliente,
                             String nombreCliente, double limiteCredito, double saldoCliente,
                             String etiqueta, double total, String fechaHora) {
            this.idEspera = idEspera;
            this.carrito = carrito;
            this.idCliente = idCliente;
            this.nombreCliente = nombreCliente;
            this.limiteCredito = limiteCredito;
            this.saldoCliente = saldoCliente;
            this.etiqueta = etiqueta;
            this.total = total;
            this.fechaHora = fechaHora;
        }

        public int getIdEspera() { return idEspera; }
        public Map<Integer, Object[]> getCarrito() { return carrito; }
        public int getIdCliente() { return idCliente; }
        public String getNombreCliente() { return nombreCliente; }
        public double getLimiteCredito() { return limiteCredito; }
        public double getSaldoCliente() { return saldoCliente; }
        public String getEtiqueta() { return etiqueta; }
        public double getTotal() { return total; }
        public String getFechaHora() { return fechaHora; }
    }
}
