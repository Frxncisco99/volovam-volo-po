package org.example.servicio;

import org.example.dao.ConexionDB;
import org.example.modelo.SesionUsuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InventarioMovimientoService {

    private static final InventarioMovimientoService INSTANCE = new InventarioMovimientoService();
    public static InventarioMovimientoService get() { return INSTANCE; }

    public enum TipoMovimiento {
        VENTA, DEVOLUCION, AJUSTE_ENTRADA, AJUSTE_SALIDA, MERMA, CANCELACION
    }

    /**
     * Registra un movimiento de inventario.
     * Obtiene el stock actual automáticamente.
     */
    public void registrar(Connection con, int idProducto, TipoMovimiento tipo,
                          int cantidad, int referenciaId, String referenciaTipo,
                          String notas) throws Exception {

        int stockActual = obtenerStock(con, idProducto);
        int stockNuevo  = calcularStockNuevo(stockActual, tipo, cantidad);

        if (stockNuevo < 0) {
            throw new IllegalStateException(
                    "Stock insuficiente para el producto #" + idProducto +
                            ". Stock actual: " + stockActual + ", intento: -" + cantidad
            );
        }

        String sql = """
            INSERT INTO movimientos_inventario 
                (id_producto, tipo, cantidad, stock_anterior, stock_nuevo,
                 referencia_id, referencia_tipo, id_usuario, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setString(2, tipo.name());
            ps.setInt(3, cantidad);
            ps.setInt(4, stockActual);
            ps.setInt(5, stockNuevo);
            ps.setInt(6, referenciaId);
            ps.setString(7, referenciaTipo);
            ps.setInt(8, SesionUsuario.getInstancia().getIdUsuario());
            ps.setString(9, notas);
            ps.executeUpdate();
        }
    }

    private int obtenerStock(Connection con, int idProducto) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT stock FROM productos WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        throw new Exception("Producto #" + idProducto + " no encontrado.");
    }

    private int calcularStockNuevo(int stockActual, TipoMovimiento tipo, int cantidad) {
        return switch (tipo) {
            case VENTA, AJUSTE_SALIDA, MERMA -> stockActual - cantidad;
            case DEVOLUCION, AJUSTE_ENTRADA, CANCELACION -> stockActual + cantidad;
        };
    }
}