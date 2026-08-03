// Definición del paquete donde se ubica esta clase dentro de la arquitectura
package DAO;

// Importar la clase de configuración para gestionar la conexión a la base de datos
import Modelo.Config.Conexion;
// Importar la entidad Auditoria para manejar los datos del log
import Modelo.Entidades.Auditoria;
// Importar la clase Connection para manejar la sesión con la base de datos
import java.sql.Connection;
// Importar PreparedStatement para ejecutar consultas SQL parametrizadas de forma segura
import java.sql.PreparedStatement;
// Importar ResultSet para almacenar y recorrer los resultados devueltos por la base de datos
import java.sql.ResultSet;
// Importar SQLException para el manejo de errores relacionados con operaciones SQL
import java.sql.SQLException;
// Importar ArrayList para la implementación de listas dinámicas
import java.util.ArrayList;
// Importar la interfaz List para manejar colecciones de elementos
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad Auditoria.
 * Gestiona el registro y consulta de actividades críticas en la base de datos.
 */
// Declaración de la clase pública AuditoriaDAO encargada de la persistencia de auditorías
public class AuditoriaDAO {

    // Constructor vacío por defecto de la clase DAO
    public AuditoriaDAO() {
    }

    /**
     * Registra un nuevo evento de actividad en el log de auditoría.
     * Genera automáticamente un identificador correlativo con formato 'AUD-XXX'.
     */
    // Método público para registrar una actividad que recibe un objeto Entidad y devuelve un booleano de éxito
    public boolean registrarActividad(Auditoria aud) {
        // Consulta SQL para obtener el ID más alto existente en la tabla de auditoría para calcular el consecutivo
        String queryMaxId = "SELECT id_historial FROM Auditoria ORDER BY CAST(SUBSTRING(id_historial, 5) AS UNSIGNED) DESC LIMIT 1";
        // Inicializar el valor por defecto del siguiente identificador en caso de que la tabla esté vacía
        String nextId = "AUD-001";

        // Obtención del último ID para cálculo del consecutivo
        // Bloque try-with-resources para abrir la conexión, preparar la sentencia y ejecutar la consulta en MySQL de forma segura
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
            // Verificar si existe un registro previo en el resultado de la consulta
            if (rsMax.next()) {
                // Extraer el valor del identificador máximo de la base de datos
                String maxId = rsMax.getString("id_historial");
                // Comprobar que el ID no sea nulo y cumpla con el prefijo esperado
                if (maxId != null && maxId.startsWith("AUD-")) {
                    try {
                        // Extraer la parte numérica posterior al prefijo y transformarla a entero
                        int num = Integer.parseInt(maxId.substring(4));
                        // Formatear el siguiente identificador incremental con ceros a la izquierda
                        nextId = String.format("AUD-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        // Capturar errores de conversión numérica y registrar la advertencia en consola
                        System.err.println("Error parseando ID de auditoria máximo: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            // Capturar errores SQL durante la consulta del ID máximo y registrar la advertencia
            System.err.println("Advertencia obteniendo ID máximo de auditoría: " + e.getMessage());
        }

        // Asignar el identificador generado o por defecto a la Entidad Auditoria
        aud.setIdHistorial(nextId);

        // Definir la sentencia SQL parametrizada para insertar un nuevo registro de auditoría en MySQL
        String sql = "INSERT INTO Auditoria (id_historial, id_usuario, accion, tipo_accion, fecha) VALUES (?, ?, ?, ?, NOW())";

        // Bloque try-with-resources para conectar con la base de datos y preparar la inserción SQL
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Asignar el ID de historial al primer parámetro de la consulta SQL
            ps.setString(1, aud.getIdHistorial());
            // Asignar el ID de usuario al segundo parámetro de la consulta SQL
            ps.setString(2, aud.getIdUsuario());
            // Asignar la descripción de la acción al tercer parámetro de la consulta SQL
            ps.setString(3, aud.getAccion());
            // Asignar el tipo de acción al cuarto parámetro de la consulta SQL
            ps.setString(4, aud.getTipoAccion());

            // Ejecutar la actualización en la base de datos MySQL y retornar true si se afectó al menos una fila
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Capturar cualquier error crítico de SQL durante la inserción y mostrar la traza en la consola
            System.err.println("ERROR SQL AL REGISTRAR AUDITORÍA: " + e.getMessage());
            e.printStackTrace();
        }

        // Retornar falso en caso de que ocurra una excepción o falle la inserción en la base de datos
        return false;
    }

    /**
     * Recupera el listado de registros de actividad de un usuario específico.
     * Ordenados de más reciente a más antiguo.
     */
    // Método público que retorna una lista de la Entidad Auditoria filtrada por el ID de un usuario
    public List<Auditoria> listarPorUsuario(String idUsuario) {
        // Inicializar una lista dinámica vacía para almacenar los registros recuperados
        List<Auditoria> lista = new ArrayList<>();
        // Definir la consulta SQL para seleccionar los historiales de un usuario ordenados de forma descendente por fecha
        String sql = "SELECT * FROM Auditoria WHERE id_usuario = ? ORDER BY fecha DESC";

        // Bloque try-with-resources para conectar a MySQL y preparar la sentencia de consulta
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Asignar el ID de usuario como parámetro en la consulta SQL
            ps.setString(1, idUsuario);
            
            // Ejecutar la consulta y almacenar el resultado en un ResultSet dentro de un bloque seguro
            try (ResultSet rs = ps.executeQuery()) {
                // Iterar a través de cada registro devuelto por la base de datos
                while (rs.next()) {
                    // Instanciar un nuevo objeto de la Entidad Auditoria por cada fila encontrada
                    Auditoria aud = new Auditoria();
                    // Extraer y asignar el ID de historial desde la base de datos hacia la entidad
                    aud.setIdHistorial(rs.getString("id_historial"));
                    // Extraer y asignar el ID de usuario hacia la entidad
                    aud.setIdUsuario(rs.getString("id_usuario"));
                    // Extraer y asignar la descripción de la acción hacia la entidad
                    aud.setAccion(rs.getString("accion"));
                    // Extraer y asignar el tipo de acción hacia la entidad
                    aud.setTipoAccion(rs.getString("tipo_accion"));
                    // Extraer y asignar la fecha del evento hacia la entidad
                    aud.setFecha(rs.getString("fecha"));
                    // Agregar la entidad poblada a la lista de resultados
                    lista.add(aud);
                }
            }
        } catch (SQLException e) {
            // Capturar errores SQL durante la consulta y mostrar la traza en la consola
            System.err.println("ERROR SQL AL CONSULTAR HISTORIAL DE AUDITORÍA: " + e.getMessage());
            e.printStackTrace();
        }

        // Retornar la lista con los objetos Auditoria obtenidos desde la base de datos hacia el controlador o capa superior
        return lista;
    }
}