package org.example.dao;

import org.example.modelo.Producto;
import org.example.servicio.FiscalSchemaService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public void actualizarProducto(Producto p) {
        String sql = "UPDATE productos SET nombre=?, precio=?, costo=?, stock=?, " +
                "stock_minimo=?, id_categoria=?, codigo_barras=? WHERE id_producto=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            FiscalSchemaService.asegurarEstructura(conn);

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setDouble(3, p.getCosto());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            ps.setInt(6, p.getIdCategoria());
            // null si no lleva código — MySQL lo guarda como NULL
            if (p.getCodigoBarras() != null && !p.getCodigoBarras().isBlank()) {
                ps.setString(7, p.getCodigoBarras());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            ps.setInt(8, p.getIdProducto());

            ps.executeUpdate();
            if (p.getIdImpuesto() > 0) {
                new ImpuestoDAO().asignarAProducto(conn, p.getIdProducto(), p.getIdImpuesto());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void eliminarLogico(int id) {
        String sql = "UPDATE productos SET activo = 0 WHERE id_producto=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertarProducto(Producto p) {
        String sql = "INSERT INTO productos(nombre, codigo_barras, precio, costo, " +
                "stock, stock_minimo, activo, id_categoria) VALUES (?, ?, ?, ?, ?, ?, 1, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            FiscalSchemaService.asegurarEstructura(conn);

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
                if (rs.next()) {
                    p.setIdProducto(rs.getInt(1));
                }
            }
            if (p.getIdProducto() > 0 && p.getIdImpuesto() > 0) {
                new ImpuestoDAO().asignarAProducto(conn, p.getIdProducto(), p.getIdImpuesto());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Producto> obtenerProductos() {
        List<Producto> lista = new ArrayList<>();

        String sql = """
            SELECT p.id_producto, p.nombre, p.codigo_barras, p.precio, p.costo,
                   p.stock, p.stock_minimo, p.id_categoria,
                   c.nombre AS categoria, p.activo,
                   i.id_impuesto, i.clave AS impuesto_clave, i.nombre AS impuesto_nombre
            FROM productos p
            INNER JOIN categorias c ON p.id_categoria = c.id_categoria
            LEFT JOIN producto_impuesto pi ON pi.id_producto = p.id_producto
            LEFT JOIN impuestos i ON i.id_impuesto = pi.id_impuesto
            WHERE p.activo = 1
        """;

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            FiscalSchemaService.asegurarEstructura(conn);
            try (ResultSet rs = ps.executeQuery()) {
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
                    p.setIdImpuesto(rs.getInt("id_impuesto"));
                    p.setImpuestoClave(rs.getString("impuesto_clave"));
                    p.setImpuestoNombre(rs.getString("impuesto_nombre"));
                    lista.add(p);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public void ajustarStock(int idProducto, int cantidad) {
        String sql = "UPDATE productos SET stock = stock + ? WHERE id_producto = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
