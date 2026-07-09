package DAO;

import Modelo.Config.Conexion;
import Modelo.Entidades.Auditoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad Auditoria.
 * Gestiona el registro y consulta de actividades críticas en la base de datos.
 */
public class AuditoriaDAO {

    public AuditoriaDAO() {
    }

    /**
     * Registra un nuevo evento de actividad en el log de auditoría.
     * Genera automáticamente un identificador correlativo con formato 'AUD-XXX'.
     */
    public boolean registrarActividad(Auditoria aud) {
        String queryMaxId = "SELECT id_historial FROM Auditoria ORDER BY CAST(SUBSTRING(id_historial, 5) AS UNSIGNED) DESC LIMIT 1";
        String nextId = "AUD-001";

        // Obtención del último ID para cálculo del consecutivo
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_historial");
                if (maxId != null && maxId.startsWith("AUD-")) {
                    try {
                        int num = Integer.parseInt(maxId.substring(4));
                        nextId = String.format("AUD-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parseando ID de auditoria máximo: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Advertencia obteniendo ID máximo de auditoría: " + e.getMessage());
        }

        aud.setIdHistorial(nextId);

        String sql = "INSERT INTO Auditoria (id_historial, id_usuario, accion, tipo_accion, fecha) VALUES (?, ?, ?, ?, NOW())";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, aud.getIdHistorial());
            ps.setString(2, aud.getIdUsuario());
            ps.setString(3, aud.getAccion());
            ps.setString(4, aud.getTipoAccion());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL REGISTRAR AUDITORÍA: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Recupera el listado de registros de actividad de un usuario específico.
     * Ordenados de más reciente a más antiguo.
     */
    public List<Auditoria> listarPorUsuario(String idUsuario) {
        List<Auditoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM Auditoria WHERE id_usuario = ? ORDER BY fecha DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auditoria aud = new Auditoria();
                    aud.setIdHistorial(rs.getString("id_historial"));
                    aud.setIdUsuario(rs.getString("id_usuario"));
                    aud.setAccion(rs.getString("accion"));
                    aud.setTipoAccion(rs.getString("tipo_accion"));
                    aud.setFecha(rs.getString("fecha"));
                    lista.add(aud);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL CONSULTAR HISTORIAL DE AUDITORÍA: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }
}