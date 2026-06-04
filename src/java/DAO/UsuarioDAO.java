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

    public UsuarioDAO() {
        try (Connection con = Conexion.getConnection()) {
            if (con != null) {
                try {
                    con.createStatement().executeUpdate("ALTER TABLE usuario ADD COLUMN direccion VARCHAR(255) NULL");
                    System.out.println("Columna 'direccion' agregada exitosamente a la tabla 'usuario'.");
                } catch (SQLException e) {
                    // Ignorar error si la columna ya existe
                    if (e.getErrorCode() != 1060 && !e.getMessage().toLowerCase().contains("duplicate column")) {
                        System.err.println("Error al intentar agregar la columna 'direccion': " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar en constructor UsuarioDAO: " + e.getMessage());
        }
    }

    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getString("id_usuario"));
                u.setIdRol(rs.getString("id_rol"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setPassword(rs.getString("password"));
                u.setEstado(rs.getString("status"));
                u.setDireccion(rs.getString("direccion"));
                lista.add(u);
            }
        } catch (SQLException e) {
            System.out.println("ERROR SQL EN LISTAR:");
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Metodo que registra un nuevo usuario.
     */
    public boolean registrar(Usuario u) {
        String queryMaxId = "SELECT id_usuario FROM usuario ORDER BY id_usuario DESC LIMIT 1";
        String nextId = "USR-001";
        
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_usuario");
                if (maxId != null && maxId.startsWith("USR-")) {
                    try {
                        int num = Integer.parseInt(maxId.substring(4));
                        nextId = String.format("USR-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing user ID: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting max user ID: " + e.getMessage());
        }

        u.setIdUsuario(nextId);

        String sql = "INSERT INTO usuario (id_usuario, id_rol, name, email, phone, password, status, direccion, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getIdUsuario());
            ps.setString(2, u.getIdRol());
            ps.setString(3, u.getName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getPassword());
            ps.setString(7, u.getEstado());
            ps.setString(8, u.getDireccion());
            ps.setDate(9, new java.sql.Date(System.currentTimeMillis()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ERROR SQL REGISTRO:");
            e.printStackTrace();
        }

        return false;
    }

    // Metodo que valida el inicio de sesión
    public Usuario login(String email, String password) {
        String sql = "SELECT * FROM usuario WHERE email = ? AND password = ?";

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getString("id_usuario"));
                usuario.setIdRol(rs.getString("id_rol"));
                usuario.setName(rs.getString("name"));
                usuario.setEmail(rs.getString("email"));
                usuario.setPhone(rs.getString("phone"));
                usuario.setEstado(rs.getString("status"));
                usuario.setDireccion(rs.getString("direccion"));
                return usuario;
            }

        } catch (SQLException e) {
            System.err.println("Error login DAO: " + e.getMessage());
        }
        return null;
    }

    //Metodo que actualiza la información del usuario
    public boolean actualizarUsuario(Usuario u) {
        String sql = "UPDATE usuario SET name = ?, email = ?, phone = ?, direccion = ? WHERE id_usuario = ?";

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getDireccion());
            ps.setString(5, u.getIdUsuario());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ERROR SQL ACTUALIZAR:");
            e.printStackTrace();
        }
        return false;
    }

    //Metodo que actualiza la contraseña del usuario
    public boolean cambiarPassword(String idUsuario, String currentPassword, String newPassword) {
        String verificarSql = "SELECT * FROM usuario WHERE id_usuario = ? AND password = ?";
        String updateSql = "UPDATE usuario SET password = ? WHERE id_usuario = ?";

        try (Connection con = Conexion.getConnection()) {

            PreparedStatement verificarPs = con.prepareStatement(verificarSql);
            verificarPs.setString(1, idUsuario);
            verificarPs.setString(2, currentPassword);
            ResultSet rs = verificarPs.executeQuery();

            if (!rs.next()) {
                return false;
            }

            PreparedStatement updatePs = con.prepareStatement(updateSql);
            updatePs.setString(1, newPassword);
            updatePs.setString(2, idUsuario);

            return updatePs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ERROR SQL PASSWORD:");
            e.printStackTrace();
        }
        return false;
    }

    // Metodo que obtiene un usuario por ID
    public Usuario obtenerUsuarioPorId(String idUsuario) {
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getString("id_usuario"));
                usuario.setIdRol(rs.getString("id_rol"));
                usuario.setName(rs.getString("name"));
                usuario.setEmail(rs.getString("email"));
                usuario.setPhone(rs.getString("phone"));
                usuario.setEstado(rs.getString("status"));
                usuario.setDireccion(rs.getString("direccion"));
                return usuario;
            }
        } catch (SQLException e) {
            System.out.println("ERROR SQL READ USER:");
            e.printStackTrace();
        }

        return null;
    }

    // Metodo que verifica si un correo ya existe en la base de datos
    public boolean existsEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
        }
        return false;
    }

    // Metodo que restablece la contraseña de un usuario mediante su correo
    public boolean resetPassword(String email, String newPassword) {
        String sql = "UPDATE usuario SET password = ? WHERE email = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
        }
        return false;
    }

    // Metodo que lista todos los empleados (roles Administrador y Empleado)
    public List<Usuario> listarEmpleados() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.*, r.nombre AS rol_nombre FROM usuario u JOIN roles r ON u.id_rol = r.id_rol WHERE u.id_rol IN ('ROL-001', 'ROL-002')";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getString("id_usuario"));
                u.setIdRol(rs.getString("rol_nombre"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setPassword(rs.getString("password"));
                u.setEstado(rs.getString("status"));
                lista.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error listing employees: " + e.getMessage());
        }
        return lista;
    }

    // Metodo que registra un nuevo empleado
    public boolean registrarEmpleado(Usuario u) {
        if (u.getPassword() == null || u.getPassword().isEmpty()) {
            u.setPassword("empleado123");
        }
        if (u.getPhone() == null || u.getPhone().isEmpty()) {
            u.setPhone("0000000000");
        }
        return registrar(u);
    }

    // Metodo que elimina un empleado
    public boolean eliminarEmpleado(String idUsuario) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
        }
        return false;
    }

    // Metodo que actualiza un empleado
    public boolean actualizarEmpleado(String idUsuario, String name, String email, String role, String status) {
        String idRol = role.toLowerCase().contains("admin") ? "ROL-001" : "ROL-002";
        String sql = "UPDATE usuario SET name = ?, email = ?, id_rol = ?, status = ? WHERE id_usuario = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, idRol);
            ps.setString(4, status);
            ps.setString(5, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
        }
        return false;
    }
}
