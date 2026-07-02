package DAO;

// Importar la clase de conexión para conectarse a la base de datos MySQL
import Modelo.Config.Conexion;
// Importar la entidad Pedido para mapear registros relacionales a objetos Java
import Modelo.Entidades.Pedido;

// Importar interfaces necesarias de JDBC para interactuar con la base de datos relacional
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Importar clases de colecciones estándar de Java para almacenar las listas de registros
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Clase de Acceso a Datos (DAO) para la entidad Pedido.
 * Encargada de realizar operaciones CRUD en la tabla 'pedido' de MySQL.
 */
public class PedidoDAO {

    /**
     * Constructor por defecto.
     * Se ha removido la ejecución de sentencias DDL (ALTER TABLE) para evitar fallos de
     * seguridad y permisos denegados cuando se usan roles de base de datos restringidos.
     */
    public PedidoDAO() {
        // Inicialización básica del DAO (sin operaciones DDL dinámicas)
    }

    /**
     * Recupera y lista todos los pedidos registrados en la tabla 'pedido'.
     *
     * @return Una lista de objetos de tipo Pedido.
     */
    public List<Pedido> listar() {
        // Crea el contenedor vacío para los resultados.
        List<Pedido> lista = new ArrayList<>();
        
        // Consulta SQL con LEFT JOIN para obtener el nombre real del usuario registrado
        String sql = "SELECT p.*, u.name AS user_name FROM pedido p "
                   + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario";

        // Intenta conectar y ejecutar la consulta.
        // Usar try-with-resources para asegurar el cierre automático de la conexión, sentencia y conjunto de resultados
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            // Recorrer el conjunto de resultados fila por fila
            // Mientras haya filas en el resultado...
            while (rs.next()) {
                // Instanciar un nuevo objeto Pedido para mapear la fila actual
                Pedido p = new Pedido();

                // Mapeo de columnas: Transfiere el valor de cada celda al atributo del objeto.
                p.setIdPedido(rs.getString("id_pedido"));

                // Mapear la columna id_usuario (puede ser nulo si no está registrado)
                p.setIdUsuario(rs.getString("id_usuario"));

                // Mapear el nombre del cliente obteniendo el nombre real de usuario como fallback
                // Si el nombre del cliente está vacío, usa el nombre que viene de la tabla usuario
                String clientName = rs.getString("customer_name");
                if (clientName == null || clientName.trim().isEmpty()) {
                    clientName = rs.getString("user_name");
                }
                p.setNombreClienteOpcional(clientName != null ? clientName : "Cliente Anónimo");

                // Mapear el tipo de entrega (enum de base de datos)
                p.setTipoEntrega(rs.getString("tipo_entrega"));

                // Recuperar el número de mesa como entero básico
                int numeroMesa = rs.getInt("numero_mesa");

                // Validar si el valor obtenido de la base de datos es NULL en SQL
                if (rs.wasNull()) {
                    // Si era null, asignar null a la propiedad Integer del objeto
                    p.setNumeroMesa(null);
                } else {
                    // En caso contrario, asignar el valor numérico recuperado
                    p.setNumeroMesa(numeroMesa);
                }

                // Mapear la dirección de entrega
                p.setDireccionEntrega(rs.getString("direccion_entrega"));

                // Mapear las observaciones adicionales
                p.setObservaciones(rs.getString("observaciones"));

                // Mapear el monto total
                p.setTotal(rs.getDouble("total"));

                // Mapear el estado del pedido
                p.setEstado(rs.getString("estado"));

                // Mapear la fecha y hora de creación del pedido
                p.setFechaPedido(rs.getString("fecha_pedido"));

                // Agregar el objeto mapeado a la lista de retorno
                lista.add(p);
            }

        } catch (SQLException e) {
            // Manejar y reportar excepciones de base de datos en consola
            System.err.println("ERROR SQL AL LISTAR PEDIDOS: " + e.getMessage());
            e.printStackTrace();
        }

        // Retornar la lista resultante
        return lista;
    }

    /**
     * Registra un nuevo pedido en la base de datos generando automáticamente
     * un identificador incremental con formato 'PED-XXX'.
     *
     * @param p Objeto Pedido con la información a insertar.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public boolean registrar(Pedido p, JSONArray productos) {
        // Consulta SQL para buscar el último ID de pedido registrado ordenado numéricamente
        String queryMaxId = "SELECT id_pedido FROM pedido ORDER BY CAST(SUBSTRING(id_pedido, 5) AS UNSIGNED) DESC LIMIT 1";
        
        // Identificador por defecto si la tabla está completamente vacía
        String nextId = "PED-001";

        // Obtener la conexión y buscar el último ID registrado para calcular el siguiente
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement psMax = con.prepareStatement(queryMaxId);
            ResultSet rsMax = psMax.executeQuery()
        ) {
            // Si existe al menos un registro en la tabla
            if (rsMax.next()) {
                // Recuperar el identificador del pedido
                String maxId = rsMax.getString("id_pedido");

                // Validar que el ID no sea nulo y cumpla el formato esperado 'PED-'
                if (maxId != null && maxId.startsWith("PED-")) {
                    // Extraer la parte numérica del ID, parsearla e incrementarla en 1
                    int num = Integer.parseInt(maxId.substring(4));
                    // Formatear el nuevo ID completando con ceros a la izquierda
                    nextId = String.format("PED-%03d", num + 1);
                }
            }
        } catch (SQLException e) {
            // Registrar advertencia si falla el cálculo del ID, pero continuar usando el ID por defecto
            System.err.println("Advertencia obteniendo ID máximo de pedido (se usará PED-001): " + e.getMessage());
        }

        // Asignar el identificador incremental al objeto pedido
        p.setIdPedido(nextId);

        // Obtener el consecutivo del detalle de pedido ordenado numéricamente
        String queryMaxDetalleId = "SELECT id_detallepedido FROM detallepedido ORDER BY CAST(SUBSTRING(id_detallepedido, 5) AS UNSIGNED) DESC LIMIT 1";
        int lastDetalleNum = 0;
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement psMaxD = con.prepareStatement(queryMaxDetalleId);
            ResultSet rsMaxD = psMaxD.executeQuery()
        ) {
            if (rsMaxD.next()) {
                String maxDetId = rsMaxD.getString("id_detallepedido");
                if (maxDetId != null && maxDetId.startsWith("DET-")) {
                    lastDetalleNum = Integer.parseInt(maxDetId.substring(4));
                }
            }
        } catch (SQLException e) {
            System.err.println("Advertencia obteniendo ID máximo de detalle: " + e.getMessage());
        }

        // Sentencia SQL parametrizada para insertar la información del pedido
        String sql = "INSERT INTO pedido "
                  + "(id_pedido, id_usuario, customer_name, tipo_entrega, numero_mesa, direccion_entrega, observaciones, total, estado, fecha_pedido) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        String sqlDetalle = "INSERT INTO detallepedido (id_detallepedido, id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)";

        // Usar try-with-resources para ejecutar de forma segura la inserción en la BD
        try (Connection con = Conexion.getConnection()) {
            con.setAutoCommit(false); // Transaccionalidad   MYSQL no aguarda nada permanentet hasta que se le diga commit

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                // Asignar el ID de pedido al parámetro 1
                ps.setString(1, p.getIdPedido());

                // Asignar el ID de usuario (puede ser null para clientes no registrados)
                ps.setString(2, p.getIdUsuario());

                // Asignar el nombre del cliente
                ps.setString(3, p.getNombreClienteOpcional());

                // Asignar el tipo de entrega
                ps.setString(4, p.getTipoEntrega());

                // Validar si el número de mesa es null
                if (p.getNumeroMesa() != null) {
                    // Asignar el número de mesa al parámetro 5 si está definido
                    ps.setInt(5, p.getNumeroMesa());
                } else {
                    // Asignar tipo NULL de base de datos al parámetro 5 si es nulo
                    ps.setNull(5, java.sql.Types.INTEGER);
                }

                // Asignar la dirección de entrega
                ps.setString(6, p.getDireccionEntrega());

                // Asignar observaciones adicionales
                ps.setString(7, p.getObservaciones());

                // Asignar el monto total
                ps.setDouble(8, p.getTotal());

                // Asignar el estado del pedido
                ps.setString(9, p.getEstado());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    con.rollback();
                    return false;
                }

                // Insertar detalles del pedido
                if (productos != null && productos.length() > 0) {
                    try (PreparedStatement psD = con.prepareStatement(sqlDetalle)) {
                        for (int i = 0; i < productos.length(); i++) {
                            JSONObject prod = productos.getJSONObject(i);
                            lastDetalleNum++;
                            String nextDetId = String.format("DET-%03d", lastDetalleNum);

                            psD.setString(1, nextDetId);
                            psD.setString(2, p.getIdPedido());
                            psD.setString(3, prod.getString("idProducto"));
                            psD.setInt(4, prod.getInt("quantity"));
                            psD.setDouble(5, prod.getDouble("price"));
                            psD.addBatch();
                        }
                        psD.executeBatch();
                    }
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Registrar error en caso de fallo durante la inserción
            System.err.println("ERROR SQL AL REGISTRAR PEDIDO: " + e.getMessage());
            e.printStackTrace();
        }

        // Retornar falso si ocurrió algún error y la inserción falló
        return false;
    }

    /**
     * Actualiza el estado de un pedido en la base de datos MySQL.
     *
     * @param idPedido Identificador del pedido.
     * @param nuevoEstado El nuevo estado (ej: 'Listo', 'En espera', 'Entregado').
     * @return true si la actualización afectó al menos una fila, false de lo contrario.
     */
    public boolean actualizarEstado(String idPedido, String nuevoEstado) {
        String sql = "UPDATE pedido SET estado = ? WHERE id_pedido = ?";
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, idPedido);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL ACTUALIZAR ESTADO DE PEDIDO: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Obtiene un pedido completo de la base de datos (cabecera y lista de productos de detalle).
     *
     * @param idPedido Identificador del pedido.
     * @return JSONObject conteniendo los datos consolidados del pedido.
     */
    public org.json.JSONObject obtenerPedidoConProductos(String idPedido) {
        org.json.JSONObject res = new org.json.JSONObject();
        
        // 1. Obtener la cabecera del pedido
        String sqlPedido = "SELECT p.*, u.name AS user_name FROM pedido p "
                         + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario "
                         + "WHERE p.id_pedido = ?";
        
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sqlPedido)
        ) {
            ps.setString(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    res.put("idPedido", rs.getString("id_pedido"));
                    res.put("idUsuario", rs.getString("id_usuario") != null ? rs.getString("id_usuario") : "");
                    
                    String clientName = rs.getString("customer_name");
                    if (clientName == null || clientName.trim().isEmpty()) {
                        clientName = rs.getString("user_name");
                    }
                    res.put("nombreClienteOpcional", clientName != null ? clientName : "Cliente Anónimo");
                    res.put("tipoEntrega", rs.getString("tipo_entrega"));
                    
                    int numeroMesa = rs.getInt("numero_mesa");
                    if (rs.wasNull()) {
                        res.put("numeroMesa", org.json.JSONObject.NULL);
                    } else {
                        res.put("numeroMesa", numeroMesa);
                    }
                    
                    res.put("direccionEntrega", rs.getString("direccion_entrega") != null ? rs.getString("direccion_entrega") : "");
                    res.put("observaciones", rs.getString("observaciones") != null ? rs.getString("observaciones") : "");
                    res.put("total", rs.getDouble("total"));
                    res.put("estado", rs.getString("estado"));
                    res.put("fechaPedido", rs.getString("fecha_pedido"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL OBTENER CABECERA EN PedidoDAO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        // 2. Obtener la lista de productos asociados
        String sqlDetalles = "SELECT dp.*, prod.nombre AS producto_nombre FROM detallepedido dp "
                           + "JOIN producto prod ON dp.id_producto = prod.id_producto "
                           + "WHERE dp.id_pedido = ?";
        
        org.json.JSONArray arrayProductos = new org.json.JSONArray();
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sqlDetalles)
        ) {
            ps.setString(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    org.json.JSONObject prod = new org.json.JSONObject();
                    prod.put("name", rs.getString("producto_nombre"));
                    prod.put("quantity", rs.getInt("cantidad"));
                    prod.put("price", rs.getDouble("precio_unitario"));
                    arrayProductos.put(prod);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL OBTENER DETALLES EN PedidoDAO: " + e.getMessage());
            e.printStackTrace();
        }
        res.put("products", arrayProductos);
        return res;
    }
}