package org.example.dao;

import org.example.modelo.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // ACTUALIZAR — agrega stock_minimo al UPDATE
    public void actualizarProducto(Producto p) {
        String sql = "UPDATE productos SET nombre=?, precio=?, costo=?, stock=?, stock_minimo=?, id_categoria=? WHERE id_producto=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setDouble(3, p.getCosto());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            ps.setInt(6, p.getIdCategoria());
            ps.setInt(7, p.getIdProducto());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ELIMINACIÓN LÓGICA — sin cambios
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

    // INSERTAR — agrega stock_minimo al INSERT
    public void insertarProducto(Producto p) {
        System.out.println("Conectando...");

        String sql = "INSERT INTO productos(nombre, precio, costo, stock, stock_minimo, activo, id_categoria) VALUES (?, ?, ?, ?, ?, 1, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setDouble(3, p.getCosto());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            ps.setInt(6, p.getIdCategoria());

            int filas = ps.executeUpdate();
            System.out.println("FILAS INSERTADAS: " + filas);

            if (filas == 0) System.out.println("NO SE INSERTÓ NADA");
            System.out.println("Insertado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // OBTENER — agrega stock_minimo al SELECT y al constructor
    public List<Producto> obtenerProductos() {
        List<Producto> lista = new ArrayList<>();

        String sql = """
            SELECT
                p.id_producto,
                p.nombre,
                p.precio,
                p.costo,
                p.stock,
                p.stock_minimo,
                p.id_categoria,
                c.nombre AS categoria,
                p.activo
            FROM productos p
            INNER JOIN categorias c ON p.id_categoria = c.id_categoria
            WHERE p.activo = 1
        """;

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
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
                p.setCategoria(rs.getString("categoria"));
                lista.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // AJUSTE RÁPIDO DE STOCK — suma o resta sin tocar otros campos
    public void ajustarStock(int idProducto, int cantidad) {
        String sql = "UPDATE productos SET stock = stock + ? WHERE id_producto = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);  // positivo = suma, negativo = resta
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}