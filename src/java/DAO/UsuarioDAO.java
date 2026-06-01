package DAO;

import Modelo.Config.Conexion;
import Modelo.Entidades.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdRol(rs.getInt("id"));
                u.setName(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                lista.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO Listar: " + e.getMessage());
        }
        return lista;
    }

    public boolean registrar(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre, email) VALUES (?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error en DAO Registrar: " + e.getMessage());
            return false;
        }
    }
}