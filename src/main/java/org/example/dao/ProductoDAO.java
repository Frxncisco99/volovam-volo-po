package org.example.dao;

import org.example.modelo.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public void actualizarProducto(Producto p) {
        String sql = "UPDATE productos SET nombre=?, precio=?, costo=?, stock=?, " +
                "stock_minimo=?, id_categoria=?, codigo_barras=? WHERE id_producto=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setDouble(3, p.getCosto());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            ps.setInt(6, p.getIdCategoria());
            // null si no lleva código - MySQL lo guarda como NULL
            if (p.getCodigoBarras() != null && !p.getCodigoBarras().isBlank()) {
                ps.setString(7, p.getCodigoBarras());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            ps.setInt(8, p.getIdProducto());

            ps.executeUpdate();

        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    public void eliminarLogico(int id) {
        String sql = "UPDATE productos SET activo = 0 WHERE id_producto=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    public int insertarProducto(Producto p) {
        String sql = "INSERT INTO productos(nombre, codigo_barras, precio, costo, " +
                "stock, stock_minimo, activo, id_categoria) VALUES (?, ?, ?, ?, ?, ?, 1, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            // null si no lleva código
            if (p.getCodigoBarras() != null && !p.getCodigoBarras().isBlank()) {
                ps.setString(2, p.getCodigoBarras());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setDouble(3, p.getPrecio());
            ps.setDouble(4, p.getCosto());
            ps.setInt(5, p.getStock());
            ps.setInt(6, p.getStockMinimo());
            ps.setInt(7, p.getIdCategoria());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
        return 0;
    }

    public List<Producto> obtenerProductos() {
        List<Producto> lista = new ArrayList<>();

        try (Connection conn = ConexionDB.getConexion()) {
            boolean incluyeProveedor = tablaExiste(conn, "producto_proveedor");
            String sql = incluyeProveedor ? """
                SELECT p.id_producto, p.nombre, p.codigo_barras, p.precio, p.costo,
                       p.stock, p.stock_minimo, p.id_categoria,
                       c.nombre AS categoria, p.activo,
                       COALESCE(pr.nombre, '') AS proveedor_principal
                FROM productos p
                INNER JOIN categorias c ON p.id_categoria = c.id_categoria
                LEFT JOIN (
                    SELECT id_producto, MIN(id_proveedor) AS id_proveedor
                    FROM producto_proveedor
                    WHERE proveedor_principal = 1 AND activo = 1
                    GROUP BY id_producto
                ) pp ON pp.id_producto = p.id_producto
                LEFT JOIN proveedores pr ON pr.id_proveedor = pp.id_proveedor
                WHERE p.activo = 1
            """ : """
                SELECT p.id_producto, p.nombre, p.codigo_barras, p.precio, p.costo,
                       p.stock, p.stock_minimo, p.id_categoria,
                       c.nombre AS categoria, p.activo
                FROM productos p
                INNER JOIN categorias c ON p.id_categoria = c.id_categoria
                WHERE p.activo = 1
            """;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Producto p = new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getDouble("costo"),
                            rs.getInt("stock"),
                            rs.getInt("stock_minimo"),
                            rs.getInt("id_categoria"),
                            rs.getBoolean("activo")
                    );
                    p.setCodigoBarras(rs.getString("codigo_barras")); // puede ser null
                    p.setCategoria(rs.getString("categoria"));
                    p.setProveedorPrincipal(incluyeProveedor ? rs.getString("proveedor_principal") : "");
                    lista.add(p);
                }
            }

        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }

        return lista;
    }

    private boolean tablaExiste(Connection conn, String tabla) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), null, tabla, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    public boolean ajustarStock(Connection conn, int idProducto, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock = stock + ? WHERE id_producto = ? AND stock + ? >= 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() == 1;
        }
    }

    public void ajustarStock(int idProducto, int cantidad) {
        try (Connection conn = ConexionDB.getConexion()) {
            if (!ajustarStock(conn, idProducto, cantidad)) {
                throw new IllegalStateException("Stock insuficiente o producto no encontrado.");
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }
}
