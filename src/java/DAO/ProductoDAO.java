package DAO;

import Modelo.Config.Conexion;
import Modelo.Entidades.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listar() {

        List<Producto> lista =
                new ArrayList<>();

        String sql =
                "SELECT * FROM Producto";

        try (

            Connection con =
                    Conexion.getConnection();

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ResultSet rs =
                    ps.executeQuery()

        ) {

            while (rs.next()) {

                Producto p =
                        new Producto();

                p.setIdProducto(
                        rs.getString(
                                "id_producto"
                        )
                );

                p.setIdCategoria(
                        rs.getString(
                                "id_categoria"
                        )
                );

                p.setNombre(
                        rs.getString(
                                "nombre"
                        )
                );

                p.setDescripcion(
                        rs.getString(
                                "descripcion"
                        )
                );

                p.setPrecio(
                        rs.getDouble(
                                "precio"
                        )
                );

                p.setStock(
                        rs.getInt(
                                "stock"
                        )
                );

                p.setEstado(
                        rs.getString(
                                "estado"
                        )
                );

                p.setImagen(
                        rs.getString(
                                "imagen"
                        )
                );

                lista.add(p);

            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return lista;
    }

    public List<java.util.Map<String, String>> listarCategorias() {
        List<java.util.Map<String, String>> lista = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre FROM Categoria WHERE estado = 'Activo'";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, String> cat = new java.util.HashMap<>();
                cat.put("idCategoria", rs.getString("id_categoria"));
                cat.put("nombre", rs.getString("nombre"));
                lista.add(cat);
            }
        } catch (Exception e) {
            System.err.println("Error al listar categorías en DAO: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}