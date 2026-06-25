package DAO;

import Modelo.Config.Conexion;
import Modelo.Entidades.Devolucion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad Devolucion.
 * Encargada de registrar y listar las solicitudes de devolución de pedidos en MySQL.
 */
public class DevolucionDAO {

    static {
        // Migración automática: asegura que la columna 'respuesta_admin' exista en la tabla Devolucion.
        // Se ejecuta una sola vez al cargar la clase en la máquina virtual.
        try (Connection con = Conexion.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "ALTER TABLE Devolucion ADD COLUMN IF NOT EXISTS respuesta_admin VARCHAR(255) NULL")) {
                    ps.executeUpdate();
                    System.out.println("Migración Devolucion: Columna respuesta_admin verificada/creada exitosamente.");
                } catch (SQLException e) {
                    // Fallback para versiones de MySQL que no soportan IF NOT EXISTS en ALTER TABLE
                    try (PreparedStatement ps2 = con.prepareStatement(
                            "ALTER TABLE Devolucion ADD respuesta_admin VARCHAR(255) NULL")) {
                        ps2.executeUpdate();
                        System.out.println("Migración Devolucion: Columna respuesta_admin agregada mediante fallback.");
                    } catch (SQLException ex) {
                        // Excepción esperada si la columna ya existía en la tabla
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Advertencia ejecutando migración estática de Devolucion: " + e.getMessage());
        }
    }

    /**
     * Constructor por defecto del DAO de Devoluciones.
     */
    public DevolucionDAO() {
    }

    /**
     * Registra una nueva solicitud de devolución en la base de datos.
     * Genera automáticamente un identificador incremental con formato 'DEV-XXX'.
     *
     * @param dev Objeto Devolucion con el idPedido y motivo de la solicitud.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public boolean solicitarDevolucion(Devolucion dev) {
        String queryMaxId = "SELECT id_devolucion FROM Devolucion ORDER BY id_devolucion DESC LIMIT 1";
        String nextId = "DEV-001";

        // Obtener el último ID registrado para calcular el consecutivo
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_devolucion");
                if (maxId != null && maxId.startsWith("DEV-")) {
                    try {
                        int num = Integer.parseInt(maxId.substring(4));
                        nextId = String.format("DEV-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parseando ID de devolución máximo: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Advertencia obteniendo ID máximo de devolución: " + e.getMessage());
        }

        dev.setIdDevolucion(nextId);
        dev.setEstadoDevolucion("Pendiente");

        String sql = "INSERT INTO Devolucion (id_devolucion, id_pedido, motivo, fecha_solicitud, estado_devolucion) VALUES (?, ?, ?, NOW(), ?)";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dev.getIdDevolucion());
            ps.setString(2, dev.getIdPedido());
            ps.setString(3, dev.getMotivo());
            ps.setString(4, dev.getEstadoDevolucion());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL SOLICITAR DEVOLUCIÓN: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Lista todas las solicitudes de devolución asociadas a los pedidos de un usuario específico.
     * Realiza un INNER JOIN con la tabla de pedidos para validar la pertenencia del pedido al usuario.
     *
     * @param idUsuario Identificador del usuario que solicita la consulta.
     * @return Lista de devoluciones del usuario.
     */
    public List<Devolucion> listarPorUsuario(String idUsuario) {
        List<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT d.* FROM Devolucion d "
                   + "INNER JOIN pedido p ON d.id_pedido = p.id_pedido "
                   + "WHERE p.id_usuario = ? "
                   + "ORDER BY d.fecha_solicitud DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Devolucion dev = new Devolucion();
                    dev.setIdDevolucion(rs.getString("id_devolucion"));
                    dev.setIdPedido(rs.getString("id_pedido"));
                    dev.setMotivo(rs.getString("motivo"));
                    dev.setFechaSolicitud(rs.getString("fecha_solicitud"));
                    dev.setEstadoDevolucion(rs.getString("estado_devolucion"));
                    dev.setRespuestaAdmin(rs.getString("respuesta_admin"));
                    lista.add(dev);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL LISTAR DEVOLUCIONES POR USUARIO: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Lista todas las solicitudes de devolución registradas en la base de datos.
     * Método útil para el panel de administración.
     *
     * @return Lista de todas las devoluciones.
     */
    public List<Devolucion> listarTodas() {
        List<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT * FROM Devolucion ORDER BY fecha_solicitud DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Devolucion dev = new Devolucion();
                dev.setIdDevolucion(rs.getString("id_devolucion"));
                dev.setIdPedido(rs.getString("id_pedido"));
                dev.setMotivo(rs.getString("motivo"));
                dev.setFechaSolicitud(rs.getString("fecha_solicitud"));
                dev.setEstadoDevolucion(rs.getString("estado_devolucion"));
                dev.setRespuestaAdmin(rs.getString("respuesta_admin"));
                lista.add(dev);
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL LISTAR TODAS LAS DEVOLUCIONES: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }
    
    // MEtodo para que el administrador actualice el estado y la respuesta
    public boolean procesarDevolucion(String idDevolucion, String estado, String respuesta) {
    String sql = "UPDATE Devolucion SET estado_devolucion = ?, respuesta_admin = ? WHERE id_devolucion = ?";
    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, estado);
        ps.setString(2, respuesta);
        ps.setString(3, idDevolucion);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
}
