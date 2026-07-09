// Declarar el paquete del DAO correspondiente a los datos de Historial
package DAO;

// Importar la clase de configuración de la conexión a la base de datos
import Modelo.Config.Conexion;
// Importar la entidad Historial para representar un pedido en Java
import Modelo.Entidades.Historial;
// Importar la clase Connection para manejar la conexión de base de datos
import java.sql.Connection;
// Importar PreparedStatement para ejecutar sentencias SQL preparadas
import java.sql.PreparedStatement;
// Importar ResultSet para recorrer el resultado de las consultas SQL
import java.sql.ResultSet;
// Importar SQLException para atrapar errores relacionales con MySQL
import java.sql.SQLException;
// Importar ArrayList para el manejo de listas de pedidos
import java.util.ArrayList;
// Importar List para devolver listados estructurados de objetos Historial
import java.util.List;
// Importar JSONObject para manejar de forma interna la lista de productos de la orden
import org.json.JSONObject;
// Importar JSONArray para representar el listado de productos de forma estructurada
import org.json.JSONArray;

/**
 * Clase de acceso a datos para el Historial de pedidos del cliente.
 * Administra el guardado compuesto (maestro-detalle) de pedidos y consultas de historial.
 */
public class HistorialDAO {

    /**
     * Constructor por defecto del DAO.
     */
    public HistorialDAO() {
        // Constructor sin inicializaciones DDL dinámicas
    }

    /**
     * Registra un nuevo pedido junto a todos sus detalles (productos comprados)
     * utilizando una transacción atómica (commit/rollback) y ejecución en lote (batch).
     *
     * @param h Entidad Historial representando el maestro del pedido.
     * @param productos JSONArray con el detalle de productos enviados desde el cliente.
     * @return true si la transacción se completó correctamente, false de lo contrario.
     */
    public boolean registrarPedido(Historial h, JSONArray productos, String tipoEntrega, Integer numeroMesa, String direccionEntrega) {
        // 1. OBTENER EL ID SIGUIENTE PARA EL PEDIDO ORDENADO NUMÉRICAMENTE
        String queryNextPedidoId = "SELECT id_pedido FROM pedido ORDER BY CAST(SUBSTRING(id_pedido, 5) AS UNSIGNED) DESC LIMIT 1";
        String nextPedidoId = "PED-001";
        
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryNextPedidoId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_pedido");
                if (maxId != null && maxId.startsWith("PED-")) {
                    try {
                        int num = Integer.parseInt(maxId.substring(4));
                        nextPedidoId = String.format("PED-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parseando ID de pedido máximo: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo el ID máximo de pedido en HistorialDAO: " + e.getMessage());
        }

        // Asignar el ID calculado a la cabecera del pedido
        h.setIdPedido(nextPedidoId);

        // 2. OBTENER EL ID SIGUIENTE PARA EL DETALLE DEL PEDIDO ORDENADO NUMÉRICAMENTE
        String queryNextDetalleId = "SELECT id_detallepedido FROM detallepedido ORDER BY CAST(SUBSTRING(id_detallepedido, 5) AS UNSIGNED) DESC LIMIT 1";
        int lastDetalleNum = 0;
        
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryNextDetalleId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_detallepedido");
                if (maxId != null && maxId.startsWith("DET-")) {
                    try {
                        lastDetalleNum = Integer.parseInt(maxId.substring(4));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parseando ID de detalle máximo: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo el ID máximo de detalle en HistorialDAO: " + e.getMessage());
        }

        // 3. DECLARAR CONSULTAS SQL
        // Sentencia para insertar en la cabecera (pedido) incluyendo las columnas adicionales
        String sqlPedido = "INSERT INTO pedido (id_pedido, id_usuario, tipo_entrega, total, estado, fecha_pedido, numero_mesa, direccion_entrega) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        // Sentencia para insertar en la tabla de detalles (detallepedido).
        // CORRECCIÓN DE BUG: Se ha removido la columna 'subtotal' de la consulta INSERT, 
        // ya que no existe en el esquema físico de la tabla y causaba fallos críticos SQL.
        String sqlDetalle = "INSERT INTO detallepedido (id_detallepedido, id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)";
        
        // Consulta para traducir el nombre amigable del producto en su ID formal ('PROD-XXX')
        String sqlLookupProd = "SELECT id_producto FROM producto WHERE nombre = ?";

        // 4. EJECUTAR TRANSACCIÓN ATÓMICA
        try (Connection con = Conexion.getConnection()) {
            // Desactivar confirmación automática para controlar la transacción manualmente
            con.setAutoCommit(false);

            try (PreparedStatement psP = con.prepareStatement(sqlPedido)) {
                // Configurar los parámetros de la cabecera del pedido
                psP.setString(1, h.getIdPedido());
                psP.setString(2, h.getIdUsuario());
                psP.setString(3, tipoEntrega != null ? tipoEntrega : "Para consumir aquí");
                psP.setDouble(4, h.getTotal());
                psP.setString(5, "En preparación"); // Estado inicial válido
                // Establecer fecha actual
                psP.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
                
                // Manejar número de mesa opcional
                if (numeroMesa != null && numeroMesa > 0) {
                    psP.setInt(7, numeroMesa);
                } else {
                    psP.setNull(7, java.sql.Types.INTEGER);
                }

                // Manejar dirección opcional
                psP.setString(8, direccionEntrega);

                // Insertar cabecera del pedido
                int filasAfectadas = psP.executeUpdate();
                if (filasAfectadas == 0) {
                    // Si falla la inserción, deshacer cambios y retornar falso
                    con.rollback();
                    return false;
                }

                // Preparar inserciones en lote para el detalle del pedido
                try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                     PreparedStatement psLookup = con.prepareStatement(sqlLookupProd)) {

                    // Iterar sobre el listado de productos recibidos en el JSON
                    for (int i = 0; i < productos.length(); i++) {
                        JSONObject prod = productos.getJSONObject(i);
                        String prodName = prod.getString("name");
                        double price = prod.getDouble("price");
                        int quantity = prod.getInt("quantity");

                        // Traducir nombre del producto a su ID
                        String idProducto = "";
                        psLookup.setString(1, prodName);
                        try (ResultSet rsL = psLookup.executeQuery()) {
                            if (rsL.next()) {
                                idProducto = rsL.getString("id_producto");
                            } else {
                                // ID por defecto en caso de no encontrarse coincidencia
                                idProducto = "PROD-001";
                            }
                        }

                        // Calcular e incrementar el ID consecutivo para el detallepedido (formato 'DET-XXX')
                        lastDetalleNum++;
                        String nextDetalleId = String.format("DET-%03d", lastDetalleNum);

                        // Configurar parámetros del detalle correspondiente
                        psD.setString(1, nextDetalleId);
                        psD.setString(2, h.getIdPedido());
                        psD.setString(3, idProducto);
                        psD.setInt(4, quantity);
                        psD.setDouble(5, price);
                        
                        // Añadir la operación al lote de ejecución (batch)
                        psD.addBatch();
                    }
                    // Ejecutar todos los inserts del lote de manera conjunta en la base de datos
                    psD.executeBatch();
                }

                // Si todo fue exitoso, confirmar la transacción escribiendo permanentemente los cambios
                con.commit();
                return true;
            } catch (SQLException e) {
                // Ante cualquier error, deshacer todos los cambios realizados en esta transacción
                con.rollback();
                System.err.println("SQL Exception ocurrida en la transacción (Rollback ejecutado): " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                // Restaurar el comportamiento de confirmación automática por defecto
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error general de conexión en registrarPedido: " + e.getMessage());
        }
        return false;
    }

    /**
     * Recupera el listado de pedidos completados o en proceso realizados por un usuario específico.
     *
     * @param idUsuario Identificador único del usuario a consultar.
     * @return Lista de objetos Historial correspondientes.
     */
    public List<Historial> obtenerHistorialUsuario(String idUsuario) {
        // Inicializar la lista dinámica ArrayList que almacenará los registros mapeados del historial de pedidos del usuario.
        List<Historial> lista = new ArrayList<>();
        
        // Consulta SQL parametrizada con LEFT JOIN para combinar datos del pedido con el nombre de usuario de la cabecera.
        String sql = "SELECT p.*, u.name AS user_name FROM pedido p "
                   + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario "
                   + "WHERE p.id_usuario = ? ORDER BY p.fecha_pedido DESC";
        // Abrir la conexión y preparar la ejecución de la consulta. El uso de try-with-resources asegura el cierre de los streams JDBC.
        try (Connection con = Conexion.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                // Vincular el idUsuario recibido por parámetros al primer marcador de posición (?) en la sentencia preparada.
                ps.setString(1, idUsuario);
                
                // Ejecutar la consulta en base de datos y obtener el ResultSet con los registros devueltos.
                try (ResultSet rs = ps.executeQuery()) {
                    // Recorrer iterativamente cada registro devuelto por la base de datos.
                    while (rs.next()) {
                        // Instanciar un objeto Historial por cada fila de la consulta.
                        Historial h = new Historial();
                        // Mapear el ID único del pedido.
                        h.setIdPedido(rs.getString("id_pedido"));
                        // Mapear el ID del usuario propietario del pedido.
                        h.setIdUsuario(rs.getString("id_usuario"));
                        // Mapear la fecha del registro del pedido.
                        h.setFecha(rs.getString("fecha_pedido"));
                        // Mapear el total financiero del pedido.
                        h.setTotal(rs.getDouble("total"));
                        // Mapear el estado del pedido ('En preparación', 'Listo', 'En espera', 'Entregado').
                        h.setEstado(rs.getString("estado"));
                        // Mapear el tipo de entrega seleccionado por el cliente.
                        h.setTipoEntrega(rs.getString("tipo_entrega"));
                        // Recuperar el valor de la columna 'customer_name' (utilizado para pedidos de clientes no registrados).
                        String clientName = rs.getString("customer_name");
                        // Lógica de respaldo: si está vacío o nulo, usar el nombre del usuario registrado obtenido del JOIN.
                        if (clientName == null || clientName.trim().isEmpty()) {
                            clientName = rs.getString("user_name");
                        }
                        // Asignar el nombre del cliente final. Si ambos resultan nulos, usar un valor por defecto no destructivo.
                        h.setCustomerName(clientName != null ? clientName : "Cliente Anónimo");
                        // Agregar el objeto de dominio completamente cargado al listado general de retorno.
                        lista.add(h);
                    }
                }
            }
        } catch (SQLException e) {
            // Escribir el log del error de SQL en consola para el diagnóstico y depuración de fallos de red o base de datos.
            System.err.println("ERROR SQL AL OBTENER HISTORIAL DE USUARIO: " + e.getMessage());
            e.printStackTrace();
        }
        // Retornar el listado estructurado de pedidos al Servlet.
        return lista;
    }
    /**
     * Recupera el listado completo de todos los pedidos realizados en el sistema.
     * Método reservado para visualización administrativa (Administrador).
     *
     * @return Lista de objetos Historial correspondientes a todos los pedidos.
     */
    public List<Historial> obtenerTodosLosPedidos() {
        // Inicializar el ArrayList que contendrá el historial global de pedidos del negocio.
        List<Historial> lista = new ArrayList<>();
        
        // Consulta SQL estructurada para obtener todos los registros de la tabla pedidos y cruzarlos con el nombre del usuario asignado.
        String sql = "SELECT p.*, u.name AS user_name FROM pedido p "
                   + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario "
                   + "ORDER BY p.fecha_pedido DESC";
        // Abrir conexión JDBC de forma atómica y preparar el PreparedStatement.
        try (Connection con = Conexion.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                // Ejecutar la consulta SELECT global sin parámetros de filtro restrictivo.
                try (ResultSet rs = ps.executeQuery()) {
                    // Recorrer el ResultSet de filas devueltas por MySQL.
                    while (rs.next()) {
                        // Crear instancia para el mapeo de propiedades del pedido.
                        Historial h = new Historial();
                        h.setIdPedido(rs.getString("id_pedido"));
                        h.setIdUsuario(rs.getString("id_usuario"));
                        h.setFecha(rs.getString("fecha_pedido"));
                        h.setTotal(rs.getDouble("total"));
                        h.setEstado(rs.getString("estado"));
                        h.setTipoEntrega(rs.getString("tipo_entrega"));
                        // Recuperar el valor de la columna 'customer_name' (nombre opcional del cliente).
                        String clientName = rs.getString("customer_name");
                        // Validar si el nombre opcional es nulo para recurrir al nombre del usuario en el JOIN.
                        if (clientName == null || clientName.trim().isEmpty()) {
                            clientName = rs.getString("user_name");
                        }
                        // Asignar el nombre amigable resultante o en su defecto "Cliente Anónimo".
                        h.setCustomerName(clientName != null ? clientName : "Cliente Anónimo");
                        // Añadir a la lista de pedidos generales.
                        lista.add(h);
                    }
                }
            }
        } catch (SQLException e) {
            // Capturar y registrar la excepción SQL.
            System.err.println("ERROR SQL AL OBTENER HISTORIAL GENERAL: " + e.getMessage());
            e.printStackTrace();
        }
        // Retornar la lista al componente del Servlet controlador.
        return lista;
    }

    /**
     * Registra un evento en la tabla HistorialPedidos.
     *
     * @param idPedido Identificador del pedido.
     * @param idUsuario Identificador del usuario que realiza la acción.
     * @param estado Estado del movimiento ('Finalizado' o 'Cancelado').
     * @param descripcion Descripción del evento.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public boolean registrarMovimientoHistorial(String idPedido, String idUsuario, String estado, String descripcion) {
        String queryMaxId = "SELECT id_historialpedido FROM HistorialPedidos ORDER BY CAST(SUBSTRING(id_historialpedido, 5) AS UNSIGNED) DESC LIMIT 1";
        String nextId = "HIS-001";

        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_historialpedido");
                if (maxId != null && maxId.startsWith("HIS-")) {
                    try {
                        int num = Integer.parseInt(maxId.substring(4));
                        nextId = String.format("HIS-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parseando ID de historial máximo: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo el ID máximo de historial en HistorialDAO: " + e.getMessage());
        }

        String sql = "INSERT INTO HistorialPedidos (id_historialpedido, id_pedido, id_usuario, fecha_movimiento, estado, descripcion) VALUES (?, ?, ?, NOW(), ?, ?)";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nextId);
            ps.setString(2, idPedido);
            ps.setString(3, idUsuario);
            ps.setString(4, estado);
            ps.setString(5, descripcion);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL REGISTRAR EN HISTORIALPEDIDOS: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}