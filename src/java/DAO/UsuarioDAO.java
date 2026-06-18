package DAO;

// Importar la clase de conexión para conectarse a la base de datos MySQL
import Modelo.Config.Conexion;
// Importar la entidad Usuario para mapear registros relacionales a objetos Java
import Modelo.Entidades.Usuario;

// Importar interfaces necesarias de JDBC para interactuar con la base de datos relacional
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Importar clases de colecciones estándar de Java para almacenar las listas de registros
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad Usuario.
 * Encargada de realizar operaciones CRUD y autenticación en la tabla 'usuario' de MySQL.
 */
public class UsuarioDAO {

    /**
     * Constructor por defecto.
     * Se ha removido la ejecución de sentencias DDL (ALTER TABLE) para evitar fallos de
     * seguridad y permisos denegados cuando se usan roles de base de datos restringidos.
     */
    public UsuarioDAO() {
        // Inicialización básica del DAO (sin operaciones DDL dinámicas)
    }

    /**
     * Recupera y lista todos los usuarios registrados en la tabla 'usuario'.
     *
     * @return Una lista de objetos de tipo Usuario.
     */
    public List<Usuario> listar() {
        // Inicializar la lista de usuarios
        List<Usuario> lista = new ArrayList<>();
        // Consulta SQL parametrizada para seleccionar todas las columnas de usuario
        String sql = "SELECT * FROM usuario";

        // Cargar conexión, preparar sentencia y ejecutar la consulta
        try (Connection con = Conexion.getConnection(); 
             PreparedStatement ps = con.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {

            // Iterar por cada una de las filas retornadas
            while (rs.next()) {
                // Instanciar un nuevo objeto Usuario para mapear la fila
                Usuario u = new Usuario();
                // Asignar identificador del usuario
                u.setIdUsuario(rs.getString("id_usuario"));
                // Asignar el ID de rol asignado
                u.setIdRol(rs.getString("id_rol"));
                // Asignar el nombre del usuario
                u.setName(rs.getString("name"));
                // Asignar el correo electrónico
                u.setEmail(rs.getString("email"));
                // Asignar el número de teléfono
                u.setPhone(rs.getString("phone"));
                // Asignar la contraseña (guardada en texto plano actualmente)
                u.setPassword(rs.getString("password"));
                // Asignar el estado de la cuenta (status)
                u.setEstado(rs.getString("status"));
                // Asignar la dirección física
                u.setDireccion(rs.getString("direccion"));
                // Agregar el usuario a la lista de resultados
                lista.add(u);
            }
        } catch (SQLException e) {
            // Registrar error de consulta SQL en consola
            System.err.println("ERROR SQL EN LISTAR USUARIOS: " + e.getMessage());
            e.printStackTrace();
        }
        // Retornar la lista obtenida
        return lista;
    }

    /**
     * Registra un nuevo usuario en la base de datos generando automáticamente
     * un identificador incremental con formato 'USR-XXX'.
     *
     * @param u Objeto Usuario con la información a insertar.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public boolean registrar(Usuario u) {
        // Consulta SQL para obtener el último ID de usuario registrado
        String queryMaxId = "SELECT id_usuario FROM usuario ORDER BY id_usuario DESC LIMIT 1";
        // Identificador por defecto si la tabla no contiene registros
        String nextId = "USR-001";
        
        // Ejecutar consulta para obtener el último ID
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_usuario");
                // Verificar que cumpla con el prefijo esperado
                if (maxId != null && maxId.startsWith("USR-")) {
                    try {
                        // Extraer la porción numérica del ID, incrementarla en 1
                        int num = Integer.parseInt(maxId.substring(4));
                        // Formatear el nuevo ID de usuario
                        nextId = String.format("USR-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parseando el id de usuario máximo: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo el ID máximo de usuario: " + e.getMessage());
        }

        // Asignar el nuevo ID generado al objeto de usuario
        u.setIdUsuario(nextId);

        // Autogenerar teléfono único si viene vacío o por defecto para evitar error de duplicados
        if (u.getPhone() == null || u.getPhone().trim().isEmpty() || "0000000000".equals(u.getPhone())) {
            String numericPart = nextId.replaceAll("[^0-9]", "");
            u.setPhone("3000000" + numericPart);
        }

        // Autogenerar dirección si viene vacía para cumplir restricciones de base de datos
        if (u.getDireccion() == null || u.getDireccion().trim().isEmpty()) {
            u.setDireccion("Calle Empleado " + nextId);
        }

        // Consulta SQL parametrizada para realizar la inserción
        String sql = "INSERT INTO usuario (id_usuario, id_rol, name, email, phone, password, status, direccion, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Ejecutar inserción en la base de datos
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Configurar los parámetros de la consulta INSERT
            ps.setString(1, u.getIdUsuario());
            ps.setString(2, u.getIdRol());
            ps.setString(3, u.getName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getPassword());
            ps.setString(7, u.getEstado());
            ps.setString(8, u.getDireccion());
            // Asignar la fecha actual del sistema
            ps.setDate(9, new java.sql.Date(System.currentTimeMillis()));

            // Ejecutar consulta y retornar true si se insertó el registro
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ERROR SQL EN REGISTRO DE USUARIO: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Valida las credenciales de inicio de sesión de un usuario.
     *
     * @param email Correo electrónico ingresado.
     * @param password Contraseña ingresada.
     * @return El objeto Usuario correspondiente si las credenciales son válidas, null de lo contrario.
     */
    public Usuario login(String email, String password) {
        // Consulta SQL para buscar coincidencia de correo y contraseña
        String sql = "SELECT * FROM usuario WHERE email = ? AND password = ?";

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignar los valores a los parámetros del query
            ps.setString(1, email);
            ps.setString(2, password);

            // Ejecutar consulta de selección
            try (ResultSet rs = ps.executeQuery()) {
                // Si existe coincidencia
                if (rs.next()) {
                    // Instanciar y rellenar los datos del usuario autenticado
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
            }

        } catch (SQLException e) {
            System.err.println("Error login en UsuarioDAO: " + e.getMessage());
        }
        return null;
    }

    /**
     * Actualiza la información básica del perfil de un usuario.
     *
     * @param u Objeto Usuario con la nueva información.
     * @return true si la actualización fue exitosa, false de lo contrario.
     */
    public boolean actualizarUsuario(Usuario u) {
        // Sentencia SQL parametrizada de actualización de perfil
        String sql = "UPDATE usuario SET name = ?, email = ?, phone = ?, direccion = ? WHERE id_usuario = ?";

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignar parámetros a la sentencia UPDATE
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getDireccion());
            ps.setString(5, u.getIdUsuario());

            // Retornar true si afectó al menos una fila
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL ACTUALIZAR USUARIO: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Realiza el cambio de contraseña de un usuario validando primero su contraseña actual.
     *
     * @param idUsuario Identificador del usuario.
     * @param currentPassword Contraseña actual.
     * @param newPassword Nueva contraseña a establecer.
     * @return true si la contraseña se actualizó correctamente, false si la contraseña actual es incorrecta o falló.
     */
    public boolean cambiarPassword(String idUsuario, String currentPassword, String newPassword) {
        // Consulta para verificar la contraseña actual
        String verificarSql = "SELECT * FROM usuario WHERE id_usuario = ? AND password = ?";
        // Sentencia para actualizar a la nueva contraseña
        String updateSql = "UPDATE usuario SET password = ? WHERE id_usuario = ?";

        try (Connection con = Conexion.getConnection()) {

            // Ejecutar la verificación inicial
            try (PreparedStatement verificarPs = con.prepareStatement(verificarSql)) {
                verificarPs.setString(1, idUsuario);
                verificarPs.setString(2, currentPassword);
                try (ResultSet rs = verificarPs.executeQuery()) {
                    // Si no coincide la contraseña actual, abortar el proceso retornando falso
                    if (!rs.next()) {
                        return false;
                    }
                }
            }

            // Ejecutar la actualización de la contraseña
            try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                updatePs.setString(1, newPassword);
                updatePs.setString(2, idUsuario);
                return updatePs.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL CAMBIAR PASSWORD: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene los datos de un usuario por su identificador único.
     *
     * @param idUsuario Identificador del usuario.
     * @return Objeto Usuario mapeado, o null si no se encuentra.
     */
    public Usuario obtenerUsuarioPorId(String idUsuario) {
        // Consulta SQL para buscar usuario por clave primaria
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL OBTENER USUARIO POR ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Verifica si un correo electrónico ya está registrado en la base de datos.
     *
     * @param email Correo electrónico a comprobar.
     * @return true si el correo existe, false de lo contrario.
     */
    public boolean existsEmail(String email) {
        // Consulta agregada para contar registros con el correo
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error comprobando existencia del correo: " + e.getMessage());
        }
        return false;
    }

    /**
     * Restablece la contraseña de un usuario a partir de su correo electrónico.
     *
     * @param email Correo electrónico del usuario.
     * @param newPassword Nueva contraseña.
     * @return true si se restableció exitosamente, false de lo contrario.
     */
    public boolean resetPassword(String email, String newPassword) {
        // Sentencia SQL parametrizada de actualización de contraseña por correo
        String sql = "UPDATE usuario SET password = ? WHERE email = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error restableciendo password por correo: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lista todos los usuarios con roles de Empleado (ROL-002) o Administrador (ROL-001).
     * Nota: En este método el campo 'idRol' del objeto Usuario es asignado intencionalmente
     * con el *nombre* amigable del rol (ej: 'Administrador' o 'Empleado') para facilitar
     * la serialización directa y presentación en el frontend.
     *
     * @return Una lista de usuarios administrativos u operativos.
     */
    public List<Usuario> listarEmpleados() {
        List<Usuario> lista = new ArrayList<>();
        // Consulta SQL con INNER JOIN para recuperar el nombre descriptivo del rol
        String sql = "SELECT u.*, r.nombre AS rol_nombre FROM usuario u JOIN roles r ON u.id_rol = r.id_rol WHERE u.id_rol IN ('ROL-001', 'ROL-002')";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getString("id_usuario"));
                
                // IMPORTANTE: Se mapea a idRol el nombre amigable obtenido del JOIN (rol_nombre) para presentación
                u.setIdRol(rs.getString("rol_nombre"));
                
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setPassword(rs.getString("password"));
                u.setEstado(rs.getString("status"));
                lista.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar empleados en DAO: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registra un nuevo empleado asignando valores por defecto si no se ingresaron.
     *
     * @param u Objeto Usuario con la información del empleado.
     * @return true si el registro fue exitoso, false de lo contrario.
     */
    public boolean registrarEmpleado(Usuario u) {
        // Si no se asignó contraseña, proveer una por defecto
        if (u.getPassword() == null || u.getPassword().isEmpty()) {
            u.setPassword("empleado123");
        }
        // Si no se asignó teléfono, proveer una cadena de ceros por defecto para cumplir la restricción
        if (u.getPhone() == null || u.getPhone().isEmpty()) {
            u.setPhone("0000000000");
        }
        // Invocar el método de registro estándar
        return registrar(u);
    }

    /**
     * Elimina físicamente un usuario de la base de datos por su identificador.
     *
     * @param idUsuario Identificador del usuario.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     */
    public boolean eliminarEmpleado(String idUsuario) {
        // Consulta SQL DELETE parametrizada
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error eliminando empleado en el DAO: " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza la información administrativa de un empleado.
     *
     * @param idUsuario Identificador del usuario a modificar.
     * @param name Nuevo nombre.
     * @param email Nuevo correo.
     * @param role Rol descriptivo del empleado (se traduce internamente a ROL-001 o ROL-002).
     * @param status Nuevo estado de la cuenta (Activo/Inactivo).
     * @return true si la actualización fue exitosa, false de lo contrario.
     */
    public boolean actualizarEmpleado(String idUsuario, String name, String email, String role, String status) {
        // Traducir el nombre amigable de rol a su correspondiente ID de base de datos
        String idRol = role.toLowerCase().contains("admin") ? "ROL-001" : "ROL-002";
        // Sentencia SQL de actualización parametrizada
        String sql = "UPDATE usuario SET name = ?, email = ?, id_rol = ?, status = ? WHERE id_usuario = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, idRol);
            ps.setString(4, status);
            ps.setString(5, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando empleado en el DAO: " + e.getMessage());
        }
        return false;
    }
}
