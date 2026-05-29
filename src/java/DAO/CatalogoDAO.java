package DAO;

import Modelo.Config.Conexion;
import Modelo.Entidades.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UsuarioDAO {

    // Método para registrar un usuario en la tabla 'Usuario'
    public boolean registrar(Usuario usuario) {
        // Tu script de base de datos define las columnas: id_rol, name, correo, telefono, contrasena, estado
        String sql = "INSERT INTO Usuario (id_rol, name, correo, telefono, contrasena, estado) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            // Pasamos los parámetros de la entidad al Query SQL
            ps.setInt(1, usuario.getIdRol());
            ps.setString(2, usuario.getName());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getTelefono());
            ps.setString(5, usuario.getContrasena()); // Nota: Idealmente aquí aplicarías un Hash MD5/BCrypt en el futuro
            ps.setString(6, usuario.getEstado());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0; // Retorna verdadero si se insertó con éxito
            
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario en UsuarioDAO: " + e.getMessage());
            return false;
        }
    }
}