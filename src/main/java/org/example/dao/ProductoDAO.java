package org.example.dao;

import org.example.modelo.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // ACTUALIZAR
    public void actualizarProducto(Producto p) {
        String sql = "UPDATE productos SET nombre=?, precio=?, stock=? WHERE id_producto=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getStock());
            ps.setInt(4, p.getIdProducto());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ELIMINACIÓN LÓGICA
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
        System.out.println("Conectando...");

        String sql = "INSERT INTO productos(nombre, precio, costo, stock, activo, id_categoria) VALUES (?, ?, ?, ?, 1, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setDouble(3, p.getCosto());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getIdCategoria());

            // 🔥 AQUÍ VA EL DEBUG IMPORTANTE
            int filas = ps.executeUpdate();
            System.out.println("FILAS INSERTADAS: " + filas);

            if (filas == 0) {
                System.out.println("NO SE INSERTÓ NADA");
            }

            System.out.println("Insertado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<Producto> obtenerProductos() {
        List<Producto> lista = new ArrayList<>();

        String sql = """
        SELECT 
            p.id_producto,
            p.nombre,
            p.precio,
            p.costo,
            p.stock,
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
                        rs.getInt("id_categoria"),
                        rs.getBoolean("activo")
                );

                // 🔥 AQUÍ ESTÁ LO IMPORTANTE
                p.setCategoria(rs.getString("categoria"));

                lista.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}