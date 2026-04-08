package org.example.dao;

import org.example.modelo.Categoria;
import org.example.dao.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> obtenerCategorias() {

        List<Categoria> lista = new ArrayList<>();

        try (Connection con = ConexionDB.getConexion()) {

            String sql = "SELECT * FROM categorias";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public void insertarCategoria(String nombre) {
        try (Connection con = ConexionDB.getConexion()) {

            String sql = "INSERT INTO categorias(nombre) VALUES(?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}