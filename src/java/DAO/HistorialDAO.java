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
    public boolean registrarPedido(Historial h, JSONArray productos) {
        // 1. OBTENER EL ID SIGUIENTE PARA EL PEDIDO
        String queryNextPedidoId = "SELECT id_pedido FROM pedido ORDER BY id_pedido DESC LIMIT 1";
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

        // 2. OBTENER EL ID SIGUIENTE PARA EL DETALLE DEL PEDIDO
        String queryNextDetalleId = "SELECT id_detallepedido FROM detallepedido ORDER BY id_detallepedido DESC LIMIT 1";
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
        // Sentencia para insertar en la cabecera (pedido)
        String sqlPedido = "INSERT INTO pedido (id_pedido, id_usuario, tipo_entrega, total, estado, fecha_pedido) VALUES (?, ?, ?, ?, ?, ?)";
        
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
                psP.setString(3, "Para consumir aquí"); // Tipo de entrega por defecto en este canal
                psP.setDouble(4, h.getTotal());
                psP.setString(5, "En preparación"); // Estado inicial válido
                // Establecer fecha actual
                psP.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));

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
        List<Historial> lista = new ArrayList<>();
        // Consulta SQL con LEFT JOIN para recuperar nombre de cliente e información de entrega
        String sql = "SELECT p.*, u.name AS user_name FROM pedido p "
                   + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario "
                   + "WHERE p.id_usuario = ? ORDER BY p.fecha_pedido DESC";

        try (Connection con = Conexion.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                // Enlazar el parámetro idUsuario
                ps.setString(1, idUsuario);
                
                try (ResultSet rs = ps.executeQuery()) {
                    // Mapear cada registro del historial encontrado
                    while (rs.next()) {
                        Historial h = new Historial();
                        h.setIdPedido(rs.getString("id_pedido"));
                        h.setIdUsuario(rs.getString("id_usuario"));
                        h.setFecha(rs.getString("fecha_pedido"));
                        h.setTotal(rs.getDouble("total"));
                        h.setEstado(rs.getString("estado"));
                        h.setTipoEntrega(rs.getString("tipo_entrega"));

                        String clientName = rs.getString("customer_name");
                        if (clientName == null || clientName.trim().isEmpty()) {
                            clientName = rs.getString("user_name");
                        }
                        h.setCustomerName(clientName != null ? clientName : "Cliente Anónimo");

                        // Agregar el registro mapeado a la colección
                        lista.add(h);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL OBTENER HISTORIAL DE USUARIO: " + e.getMessage());
            e.printStackTrace();
        }
        // Retornar lista
        return lista;
    }
}