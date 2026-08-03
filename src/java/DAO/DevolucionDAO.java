// Definición del paquete DAO donde se agrupan las clases encargadas de la persistencia y acceso a datos
package DAO;

// Importar la clase de configuración de base de datos para establecer la conexión con MySQL
import Modelo.Config.Conexion;
// Importar la entidad Devolucion para modelar y transferir los datos de las devoluciones de pedidos
import Modelo.Entidades.Devolucion;
// Importar la interfaz Connection para administrar la sesión de conexión con la base de datos
import java.sql.Connection;
// Importar PreparedStatement para compilar y ejecutar consultas SQL parametrizadas de manera segura
import java.sql.PreparedStatement;
// Importar ResultSet para almacenar y recorrer los resultados devueltos por una consulta SQL
import java.sql.ResultSet;
// Importar SQLException para manejar de forma controlada las excepciones e incidencias con la base de datos
import java.sql.SQLException;
// Importar ArrayList para la instanciación de listas dinámicas de objetos
import java.util.ArrayList;
// Importar la interfaz List para definir colecciones de datos tipadas
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad Devolucion.
 * Encargada de registrar y listar las solicitudes de devolución de pedidos en MySQL.
 */
// Declaración de la clase pública DevolucionDAO que centraliza las operaciones de base de datos para las devoluciones
public class DevolucionDAO {

    // Bloque estático de inicialización que se ejecuta una sola vez cuando la clase es cargada en memoria por la JVM
    static {
        // Migración automática: asegura que la columna 'respuesta_admin' exista en la tabla Devolucion.
        // Se ejecuta una sola vez al cargar la clase en la máquina virtual.
        // Bloque try-with-resources para obtener una conexión activa con la base de datos MySQL
        try (Connection con = Conexion.getConnection()) {
            // Validar que la conexión devuelta no sea nula antes de intentar operaciones DDL
            if (con != null) {
                // Intentar ejecutar una sentencia ALTER TABLE moderna compatible con cláusulas condicionales
                try (PreparedStatement ps = con.prepareStatement(
                        "ALTER TABLE Devolucion ADD COLUMN IF NOT EXISTS respuesta_admin VARCHAR(255) NULL")) {
                    // Ejecutar la actualización DDL en la base de datos MySQL
                    ps.executeUpdate();
                    // Imprimir mensaje informativo en consola indicando éxito en la verificación o creación de la columna
                    System.out.println("Migración Devolucion: Columna respuesta_admin verificada/creada exitosamente.");
                } catch (SQLException e) {
                    // Fallback para versiones de MySQL que no soportan IF NOT EXISTS en ALTER TABLE
                    // Intentar la ejecución de un ALTER TABLE estándar sin la cláusula condicional
                    try (PreparedStatement ps2 = con.prepareStatement(
                            "ALTER TABLE Devolucion ADD respuesta_admin VARCHAR(255) NULL")) {
                        // Ejecutar la sentencia alternativa en la base de datos
                        ps2.executeUpdate();
                        // Mostrar mensaje de éxito indicando que se aplicó el método alternativo (fallback)
                        System.out.println("Migración Devolucion: Columna respuesta_admin agregada mediante fallback.");
                    } catch (SQLException ex) {
                        // Excepción esperada si la columna ya existía en la tabla
                    }
                }
            }
        } catch (SQLException e) {
            // Capturar errores generales de conexión o SQL en la migración estática y registrarlos como advertencia
            System.err.println("Advertencia ejecutando migración estática de Devolucion: " + e.getMessage());
        }
    }

    /**
     * Constructor por defecto del DAO de Devoluciones.
     */
    // Constructor público vacío para instanciar el DAO desde los controladores o servicios del sistema
    public DevolucionDAO() {
    }

    /**
     * Registra una nueva solicitud de devolución en la base de datos.
     * Genera automáticamente un identificador incremental con formato 'DEV-XXX'.
     *
     * @param dev Objeto Devolucion con el idPedido y motivo de la solicitud.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    // Método público para registrar una devolución, recibe un objeto de tipo Devolucion y retorna un booleano de éxito
    public boolean solicitarDevolucion(Devolucion dev) {
        // Consulta SQL para obtener el identificador de devolución con mayor numeración actual en la tabla
        String queryMaxId = "SELECT id_devolucion FROM Devolucion ORDER BY CAST(SUBSTRING(id_devolucion, 5) AS UNSIGNED) DESC LIMIT 1";
        // Definir un valor por defecto inicial para el primer identificador en caso de que la tabla esté vacía
        String nextId = "DEV-001";

        // Obtener el último ID registrado para calcular el consecutivo
        // Bloque try-with-resources para abrir conexión, preparar la consulta del ID máximo y ejecutarla en MySQL
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
            // Comprobar si la consulta arrojó al menos un resultado (registro previo)
            if (rsMax.next()) {
                // Extraer el valor del ID máximo encontrado en la base de datos
                String maxId = rsMax.getString("id_devolucion");
                // Validar que el ID obtenido no sea nulo y comience con el prefijo esperado 'DEV-'
                if (maxId != null && maxId.startsWith("DEV-")) {
                    try {
                        // Extraer los caracteres numéricos posteriores al prefijo y convertirlos a entero
                        int num = Integer.parseInt(maxId.substring(4));
                        // Formatear el nuevo ID consecutivo incrementado en uno con ceros a la izquierda (ej. DEV-002)
                        nextId = String.format("DEV-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        // Capturar errores de formato numérico al parsear el sub-string y reportarlo en la consola
                        System.err.println("Error parseando ID de devolución máximo: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            // Capturar errores SQL al consultar el ID máximo y registrar una advertencia en la consola
            System.err.println("Advertencia obteniendo ID máximo de devolución: " + e.getMessage());
        }

        // Asignar el identificador calculado o por defecto al atributo idDevolucion de la entidad
        dev.setIdDevolucion(nextId);
        // Establecer el estado inicial de la solicitud como 'Pendiente' en la entidad
        dev.setEstadoDevolucion("Pendiente");

        // Definir la sentencia SQL parametrizada para insertar la nueva devolución en la tabla MySQL
        String sql = "INSERT INTO Devolucion (id_devolucion, id_pedido, motivo, fecha_solicitud, estado_devolucion) VALUES (?, ?, ?, NOW(), ?)";

        // Bloque try-with-resources para conectar a MySQL y preparar la sentencia de inserción de forma segura
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Asignar el ID de devolución al primer parámetro de la consulta SQL
            ps.setString(1, dev.getIdDevolucion());
            // Asignar el ID de pedido al segundo parámetro de la consulta SQL
            ps.setString(2, dev.getIdPedido());
            // Asignar el motivo de la devolución al tercer parámetro de la consulta SQL
            ps.setString(3, dev.getMotivo());
            // Asignar el estado de devolución al cuarto parámetro de la consulta SQL
            ps.setString(4, dev.getEstadoDevolucion());

            // Ejecutar la inserción en la base de datos y retornar true si se afectó al menos una fila
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Capturar errores críticos de SQL durante la inserción y volcar la traza de la excepción
            System.err.println("ERROR SQL AL SOLICITAR DEVOLUCIÓN: " + e.getMessage());
            e.printStackTrace();
        }

        // Retornar falso si ocurre alguna excepción o falla la operación en la base de datos
        return false;
    }

    /**
     * Lista todas las solicitudes de devolución asociadas a los pedidos de un usuario específico.
     * Realiza un INNER JOIN con la tabla de pedidos para validar la pertenencia del pedido al usuario.
     *
     * @param idUsuario Identificador del usuario que solicita la consulta.
     * @return Lista de devoluciones del usuario.
     */
    // Método público para listar devoluciones filtradas por cliente, retorna una lista de objetos Devolucion
    public List<Devolucion> listarPorUsuario(String idUsuario) {
        // Inicializar una lista dinámica vacía para almacenar los registros recuperados
        List<Devolucion> lista = new ArrayList<>();
        // Definir la consulta SQL con un INNER JOIN entre devoluciones y pedidos para filtrar por el ID del usuario propietario
        String sql = "SELECT d.* FROM Devolucion d "
                   + "INNER JOIN pedido p ON d.id_pedido = p.id_pedido "
                   + "WHERE p.id_usuario = ? "
                   + "ORDER BY d.fecha_solicitud DESC";

        // Bloque try-with-resources para establecer la conexión con MySQL y preparar la sentencia SQL
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Asignar el ID de usuario como parámetro en la consulta SQL
            ps.setString(1, idUsuario);
            
            // Ejecutar la consulta y gestionar el ResultSet dentro de un bloque seguro para lectura de filas
            try (ResultSet rs = ps.executeQuery()) {
                // Iterar a través de cada registro devuelto por la base de datos
                while (rs.next()) {
                    // Instanciar un nuevo objeto de la Entidad Devolucion por cada registro encontrado
                    Devolucion dev = new Devolucion();
                    // Extraer y asignar el ID de devolución desde la base de datos hacia la entidad
                    dev.setIdDevolucion(rs.getString("id_devolucion"));
                    // Extraer y asignar el ID de pedido hacia la entidad
                    dev.setIdPedido(rs.getString("id_pedido"));
                    // Extraer y asignar el motivo hacia la entidad
                    dev.setMotivo(rs.getString("motivo"));
                    // Extraer y asignar la fecha de solicitud hacia la entidad
                    dev.setFechaSolicitud(rs.getString("fecha_solicitud"));
                    // Extraer y asignar el estado de la devolución hacia la entidad
                    dev.setEstadoDevolucion(rs.getString("estado_devolucion"));
                    // Extraer y asignar la respuesta del administrador hacia la entidad
                    dev.setRespuestaAdmin(rs.getString("respuesta_admin"));
                    // Agregar la entidad completa a la lista dinámica
                    lista.add(dev);
                }
            }
        } catch (SQLException e) {
            // Capturar errores SQL durante la consulta y volcar los detalles del error en la consola
            System.err.println("ERROR SQL AL LISTAR DEVOLUCIONES POR USUARIO: " + e.getMessage());
            e.printStackTrace();
        }

        // Retornar la lista poblada de objetos Devolucion hacia la capa controladora que solicitó la información
        return lista;
    }

    /**
     * Lista todas las solicitudes de devolución registradas en la base de datos.
     * Método útil para el panel de administración.
    *
     * @return Lista de todas las devoluciones.
     */
    // Método público para listar todas las devoluciones del sistema de forma global, orientado al panel de administración
    public List<Devolucion> listarTodas() {
        // Inicializar una lista dinámica vacía para almacenar los registros de devoluciones
        List<Devolucion> lista = new ArrayList<>();
        // Definir la consulta SQL para obtener todas las devoluciones ordenadas por fecha de solicitud de forma descendente
        String sql = "SELECT * FROM Devolucion ORDER BY fecha_solicitud DESC";

        // Bloque try-with-resources para conectar a MySQL, preparar la sentencia y ejecutar la consulta en un solo paso
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Iterar sobre cada fila devuelta por la base de datos
            while (rs.next()) {
                // Instanciar un objeto Devolucion por cada fila obtenida
                Devolucion dev = new Devolucion();
                // Asignar el ID de devolución obtenido del ResultSet a la entidad
                dev.setIdDevolucion(rs.getString("id_devolucion"));
                // Asignar el ID de pedido a la entidad
                dev.setIdPedido(rs.getString("id_pedido"));
                // Asignar el motivo de la devolución a la entidad
                dev.setMotivo(rs.getString("motivo"));
                // Asignar la fecha de solicitud a la entidad
                dev.setFechaSolicitud(rs.getString("fecha_solicitud"));
                // Asignar el estado de la devolución a la entidad
                dev.setEstadoDevolucion(rs.getString("estado_devolucion"));
                // Asignar la respuesta del administrador a la entidad
                dev.setRespuestaAdmin(rs.getString("respuesta_admin"));
                // Añadir la entidad mapeada a la lista de resultados
                lista.add(dev);
            }
        } catch (SQLException e) {
            // Capturar cualquier excepción SQL y registrar el error en la consola
            System.err.println("ERROR SQL AL LISTAR TODAS LAS DEVOLUCIONES: " + e.getMessage());
            e.printStackTrace();
        }

        // Retornar la lista completa de devoluciones hacia el controlador administrador
        return lista;
    }
    
    // Método para que el administrador actualice el estado y la respuesta
    // Método público transaccional para procesar una solicitud de devolución (aprobar/rechazar) por parte de un administrador
    public boolean procesarDevolucion(String idDevolucion, String estado, String respuesta, String idUsuarioAdmin) {
        // Definir la sentencia SQL para actualizar el estado y la respuesta del administrador en la tabla Devolucion
        String sqlUpdate = "UPDATE Devolucion SET estado_devolucion = ?, respuesta_admin = ? WHERE id_devolucion = ?";
        // Definir la sentencia SQL para consultar el ID de pedido vinculado a la devolución
        String sqlGetPedido = "SELECT id_pedido FROM Devolucion WHERE id_devolucion = ?";
        
        // Bloque try-with-resources para abrir la conexión a la base de datos MySQL
        try (Connection con = Conexion.getConnection()) {
            // Desactivar el modo de confirmación automática (autocommit) para habilitar el control manual de transacciones
            con.setAutoCommit(false);
            
            // Bloque try-with-resources para instanciar los PreparedStatements de actualización y consulta dentro de la transacción
            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                 PreparedStatement psGet = con.prepareStatement(sqlGetPedido)) {
                
                // 1. Obtener id_pedido
                // Declarar variable local para almacenar el ID del pedido asociado
                String idPedido = null;
                // Configurar el parámetro de la devolución en la sentencia de consulta del pedido
                psGet.setString(1, idDevolucion);
                // Ejecutar la consulta para obtener el pedido vinculado y procesar su resultado de forma segura
                try (ResultSet rs = psGet.executeQuery()) {
                    // Verificar si se encontró el registro de la devolución
                    if (rs.next()) {
                        // Extraer el identificador del pedido desde la base de datos
                        idPedido = rs.getString("id_pedido");
                    }
                }
                
                // Validar si no se encontró el pedido asociado a la devolución
                if (idPedido == null) {
                    // Revertir los cambios realizados en la transacción (rollback) por inconsistencia de datos
                    con.rollback();
                    // Retornar falso indicando fallo en el procesamiento
                    return false;
                }
                
                // 2. Actualizar estado de devolución
                // Asignar el nuevo estado de la devolución al primer parámetro de la sentencia UPDATE
                psUpdate.setString(1, estado);
                // Asignar la respuesta redactada por el administrador al segundo parámetro del UPDATE
                psUpdate.setString(2, respuesta);
                // Asignar el ID de la devolución al tercer parámetro del UPDATE como condición WHERE
                psUpdate.setString(3, idDevolucion);
                
                // Ejecutar la actualización en la base de datos y verificar si se afectó al menos una fila
                boolean ok = psUpdate.executeUpdate() > 0;
                // Comprobar si la actualización en la base de datos falló
                if (!ok) {
                    // Revertir la transacción actual (rollback) si el UPDATE no tuvo efecto
                    con.rollback();
                    // Retornar falso indicando el fallo de la operación
                    return false;
                }
                
                // 3. Si se aprueba la devolución, registrar en HistorialPedidos como 'Cancelado'
                // Comprobar mediante case-insensitive si el estado asignado corresponde a 'Aprobada'
                if ("Aprobada".equalsIgnoreCase(estado)) {
                    // Instanciar el DAO de HistorialPedidos para registrar el movimiento logístico asociado
                    HistorialDAO historialDao = new HistorialDAO();
                    // Invocar el método del HistorialDAO para registrar el cambio de estado del pedido a 'Cancelado' debido a la devolución aprobada
                    boolean histRegistrado = historialDao.registrarMovimientoHistorial(idPedido, idUsuarioAdmin, "Cancelado", "Devolución aprobada: " + respuesta);
                    // Comprobar si el registro en el historial de pedidos falló
                    if (!histRegistrado) {
                        // Registrar una advertencia en la consola indicando que no se pudo insertar el movimiento histórico secundario
                        System.err.println("Advertencia: No se pudo insertar en HistorialPedidos durante la aprobación de la devolución.");
                    }
                }
                
                // Confirmar de forma exitosa todas las operaciones ejecutadas dentro de la transacción actual (commit) hacia MySQL
                con.commit();
                // Retornar true indicando que el procesamiento de la devolución se completó con éxito
                return true;
            } catch (SQLException e) {
                // Revertir todos los cambios de la transacción (rollback) si ocurre una excepción SQL interna
                con.rollback();
                // Propagar la excepción SQL hacia el nivel superior del bloque try
                throw e;
            } finally {
                // Restaurar el modo de confirmación automática original (autocommit a true) de la conexión a la base de datos
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Imprimir la traza detallada del error SQL general en la consola
            e.printStackTrace();
            // Retornar falso al controlador debido al fallo de la transacción
            return false;
        }
    }
}