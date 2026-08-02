package DAO;

import Modelo.Config.Conexion;
import Modelo.Entidades.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public ProductoDAO() {
        try (Connection con = Conexion.getConnection();
             java.sql.Statement stmt = con.createStatement()) {
            stmt.executeUpdate("ALTER TABLE Producto MODIFY COLUMN imagen LONGTEXT");
        } catch (Exception e) {
            // Ignorar si no se requiere o ya está modificado
        }
    }

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

    /**
     * Registra un nuevo producto en la base de datos.
     * @param p Objeto Producto con los datos recibidos del formulario.
     * @return true si la inserción fue exitosa.
     */
    public boolean registrar(Producto p) {
        System.out.println("[INFO - ProductoDAO] Iniciando registrar() para el producto: " + p.getNombre());
        
        // Obtener el mayor ID actual con ordenamiento numérico seguro para evitar colisiones
        String queryMaxId = "SELECT id_producto FROM Producto ORDER BY CAST(SUBSTRING(id_producto, 6) AS UNSIGNED) DESC LIMIT 1";
        String nextId = "PROD-001";

        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_producto");
                if (maxId != null && maxId.startsWith("PROD-")) {
                    try {
                        int num = Integer.parseInt(maxId.substring(5));
                        nextId = String.format("PROD-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        System.err.println("[WARN - ProductoDAO] Error al parsear el número de ID máximo: " + e.getMessage());
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[WARN - ProductoDAO] Error al obtener el ID máximo: " + e.getMessage());
        }

        p.setIdProducto(nextId);

        if (p.getDescripcion() == null || p.getDescripcion().trim().isEmpty()) {
            p.setDescripcion(p.getNombre());
        }

        String unidadesMedida = "Unidad";
        String sql = "INSERT INTO Producto (id_producto, id_categoria, nombre, descripcion, precio, stock, fecha_vencimiento, unidades_medida, imagen) VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?)";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, p.getIdProducto());
            ps.setString(2, p.getIdCategoria());
            ps.setString(3, p.getNombre());
            ps.setString(4, p.getDescripcion());
            ps.setDouble(5, p.getPrecio());
            ps.setInt(6, p.getStock());
            ps.setString(7, unidadesMedida);
            ps.setString(8, p.getImagen() != null ? p.getImagen() : "");
            
            int filas = ps.executeUpdate();
            System.out.println("[INFO - ProductoDAO] registrar() - Filas insertadas: " + filas);
            return filas > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("[ERROR - ProductoDAO] registrar() falló: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza los datos de un producto existente.
     * @param p Objeto Producto con los datos a modificar.
     * @return true si la actualización fue exitosa.
     */
    public boolean actualizar(Producto p) {
        System.out.println("[INFO - ProductoDAO] Iniciando actualizar() para el producto ID: " + p.getIdProducto());
        
        if (p.getDescripcion() == null || p.getDescripcion().trim().isEmpty()) {
            p.setDescripcion(p.getNombre());
        }

        String sql = "UPDATE Producto SET id_categoria = ?, nombre = ?, descripcion = ?, precio = ?, stock = ?, imagen = ? WHERE id_producto = ?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, p.getIdCategoria());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getImagen() != null ? p.getImagen() : "");
            ps.setString(7, p.getIdProducto());
            
            int filas = ps.executeUpdate();
            System.out.println("[INFO - ProductoDAO] actualizar() - Filas modificadas: " + filas);
            return filas > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("[ERROR - ProductoDAO] actualizar() falló: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina (o desactiva) un producto de la base de datos.
     * @param id ID del producto a eliminar.
     * @return true si la operación fue exitosa.
     */
    public boolean eliminar(String id) {
        System.out.println("[INFO - ProductoDAO] Iniciando eliminar() para el producto ID: " + id);
        
        String sql = "DELETE FROM Producto WHERE id_producto = ?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, id);
            
            int filas = ps.executeUpdate();
            System.out.println("[INFO - ProductoDAO] eliminar() - Filas borradas: " + filas);
            return filas > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("[ERROR - ProductoDAO] eliminar() falló: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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