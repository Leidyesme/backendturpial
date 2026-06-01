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
        String sql = "SELECT * FROM Usuario";
        
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdRol(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                lista.add(u);
            }
        } catch (SQLException e) {

            System.out.println("ERROR SQL EN REGISTRO:");
            e.printStackTrace();
        }
        return lista;
    }

    public boolean registrar(Usuario u) {
        String sql = "INSERT INTO Usuario (name, email, phone, password, estado) VALUES (?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getPassword());
            ps.setString(6, u.getEstado());
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
//            System.err.println("Error en DAO Registrar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Metodo que valida el inicio de sesión
    public Usuario login(String email,String password) {

    String sql = "SELECT * FROM usuario " + "WHERE email = ? " + "AND password = ?";

    try (Connection con = Conexion.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        //Reemplazamos los parametros
        ps.setString(1, email);
        ps.setString(2, password);
        
        //Se ejecuta la consukta
        ResultSet rs = ps.executeQuery();

        // Verificamos si se encontro el usuario
        if (rs.next()) {

            Usuario usuario = new Usuario();

            usuario.setIdUsuario(rs.getInt("id_usuario"));

            usuario.setIdRol(rs.getInt("id_rol"));

            usuario.setName(rs.getString("name"));

            usuario.setEmail(rs.getString("email"));

            usuario.setPhone(rs.getString("phone"));

            usuario.setEstado(rs.getString("estado"));

            return usuario;
        }

    }

    catch (SQLException e) {

        System.err.println("Error login DAO: " + e.getMessage());
    }
    return null;
    }
}