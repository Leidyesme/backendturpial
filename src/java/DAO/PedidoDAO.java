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
        // Crea el contenedor (lista) que albergará los objetos Pedido obtenidos de la base de datos.
        List<Pedido> lista = new ArrayList<>();
        
        // Define la consulta SQL. Usa un LEFT JOIN para traer los datos del pedido y, 
        // si existe, el nombre del usuario asociado (sin descartar pedidos sin usuario).
        String sql = "SELECT p.*, u.name AS user_name FROM pedido p "
                    + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario";

        // Establece la conexión y ejecuta la consulta. El try-with-resources asegura que 
        // la conexión, el statement y el resultado se cierren al terminar el bloque.
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            // Itera sobre cada registro (fila) devuelto por la base de datos.
            while (rs.next()) {
                // Instancia un nuevo objeto Pedido para mapear los datos de la fila actual.
                Pedido p = new Pedido();

                // Transfiere el valor de la columna 'id_pedido' al objeto Pedido.
                p.setIdPedido(rs.getString("id_pedido"));

                // Transfiere el 'id_usuario' al objeto (puede ser nulo en caso de ser un invitado).
                p.setIdUsuario(rs.getString("id_usuario"));

                // Lógica de respaldo: si 'customer_name' está vacío, toma el nombre de 'usuario' (u.name).
                String clientName = rs.getString("customer_name");
                if (clientName == null || clientName.trim().isEmpty()) {
                    clientName = rs.getString("user_name");
                }
                // Asigna un valor por defecto si el cliente no está registrado.
                p.setNombreClienteOpcional(clientName != null ? clientName : "Cliente Anónimo");

                // Mapea el tipo de entrega (p.ej. 'Local' o 'Domicilio').
                p.setTipoEntrega(rs.getString("tipo_entrega"));

                // Obtiene el número de mesa (tipo primitivo).
                int numeroMesa = rs.getInt("numero_mesa");

                // Verifica si el valor en la base de datos fue NULL, para no asignar 0 erróneamente.
                if (rs.wasNull()) {
                    p.setNumeroMesa(null);
                } else {
                    p.setNumeroMesa(numeroMesa);
                }

                // Asigna los campos restantes del pedido desde la consulta SQL.
                p.setDireccionEntrega(rs.getString("direccion_entrega"));
                p.setObservaciones(rs.getString("observaciones"));
                p.setTotal(rs.getDouble("total"));
                p.setEstado(rs.getString("estado"));
                p.setFechaPedido(rs.getString("fecha_pedido"));

                // Añade el objeto completamente mapeado a la lista de retorno.
                lista.add(p);
            }

        } catch (SQLException e) {
            // Captura cualquier error de conexión o consulta SQL y lo registra en la consola.
            System.err.println("ERROR SQL AL LISTAR PEDIDOS: " + e.getMessage());
            e.printStackTrace();
        }

        // Devuelve la lista completa al componente que invocó a este método (usualmente el Servlet).
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
        // SQL: busca el ID más alto, lo convierte a número después de 'PED-' para ordenarlos numéricamente.
        String queryMaxId = "SELECT id_pedido FROM pedido ORDER BY CAST(SUBSTRING(id_pedido, 5) AS UNSIGNED) DESC LIMIT 1";
        
        // Identificador por defecto para cuando no existen pedidos previos.
        String nextId = "PED-001";

        // Bloque para calcular automáticamente el siguiente número correlativo de ID.
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement psMax = con.prepareStatement(queryMaxId);
            ResultSet rsMax = psMax.executeQuery()
        ) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_pedido");
                // Valida formato y extrae el número para sumarle 1.
                if (maxId != null && maxId.startsWith("PED-")) {
                    int num = Integer.parseInt(maxId.substring(4));
                    nextId = String.format("PED-%03d", num + 1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Advertencia obteniendo ID máximo de pedido (se usará PED-001): " + e.getMessage());
        }

        // Asigna el nuevo ID generado al objeto pedido antes de insertar.
        p.setIdPedido(nextId);

        // Bloque similar para obtener el siguiente ID de la tabla 'detallepedido'.
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

        // Define las sentencias SQL de inserción para la cabecera y el detalle del pedido.
        String sql = "INSERT INTO pedido "
                     + "(id_pedido, id_usuario, customer_name, tipo_entrega, numero_mesa, direccion_entrega, observaciones, total, estado, fecha_pedido) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        String sqlDetalle = "INSERT INTO detallepedido (id_detallepedido, id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)";

        // Inicio de la transacción: crucial para asegurar que el pedido y sus detalles se guarden como una unidad.
        try (Connection con = Conexion.getConnection()) {
            con.setAutoCommit(false); // Desactiva el autoguardado (transaccionalidad manual).

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                // Mapea los valores del objeto Pedido a la consulta principal.
                ps.setString(1, p.getIdPedido());
                ps.setString(2, p.getIdUsuario());
                ps.setString(3, p.getNombreClienteOpcional());
                ps.setString(4, p.getTipoEntrega());

                // Manejo de campo opcional 'numero_mesa'.
                if (p.getNumeroMesa() != null) {
                    ps.setInt(5, p.getNumeroMesa());
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }

                ps.setString(6, p.getDireccionEntrega());
                ps.setString(7, p.getObservaciones());
                ps.setDouble(8, p.getTotal());
                ps.setString(9, p.getEstado());

                // Ejecuta la inserción de la cabecera.
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    con.rollback(); // Si falla, revierte todo.
                    return false;
                }

                // Inserción de los productos (detalles) en un lote (batch) para mayor rendimiento.
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
                            psD.addBatch(); // Acumula las sentencias.
                        }
                        psD.executeBatch(); // Ejecuta todas las inserciones a la vez.
                    }
                }

                con.commit(); // Si todo es correcto, confirma (graba) los cambios.
                return true;
            } catch (SQLException e) {
                con.rollback(); // Si ocurre un error, deshace cualquier cambio previo.
                throw e;
            } finally {
                con.setAutoCommit(true); // Restaura el modo de guardado automático.
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL REGISTRAR PEDIDO: " + e.getMessage());
            e.printStackTrace();
        }

        return false; // Error general.
    }

    /**
     * Actualiza el estado de un pedido en la base de datos MySQL.
     *
     * @param idPedido Identificador del pedido.
     * @param nuevoEstado El nuevo estado (ej: 'Listo', 'En espera', 'Entregado').
     * @return true si la actualización afectó al menos una fila, false de lo contrario.
     */
    public boolean actualizarEstado(String idPedido, String nuevoEstado) {
        // Actualiza solo la columna 'estado' donde coincida el ID.
        String sql = "UPDATE pedido SET estado = ? WHERE id_pedido = ?";
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, idPedido);
            // Si la consulta afecta más de 0 filas, fue exitosa.
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
        
        // Obtener la cabecera del pedido mediante un LEFT JOIN para obtener el nombre del usuario.
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
                    // Carga todos los campos del pedido en un objeto JSON para enviar al frontend.
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
                    return null; // El pedido no existe.
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL OBTENER CABECERA EN PedidoDAO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        // Obtener la lista de productos asociados al pedido usando un JOIN con 'producto'.
        String sqlDetalles = "SELECT dp.*, prod.nombre AS producto_nombre FROM detallepedido dp "
                           + "JOIN producto prod ON dp.id_producto = prod.id_producto "
                           + "WHERE dp.id_pedido = ?";
        
        org.json.JSONArray arrayProductos = new org.json.JSONArray();
        // Ejecuta la consulta para los detalles asociados al idPedido.
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sqlDetalles)
        ) {
            ps.setString(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Crea un objeto para cada producto y lo agrega a la lista de productos.
                    org.json.JSONObject prod = new org.json.JSONObject();
                    prod.put("name", rs.getString("producto_nombre"));
                    prod.put("quantity", rs.getInt("cantidad"));
                    prod.put("price", rs.getDouble("precio_unitario"));
                    arrayProductos.put(prod);  // Agrega cada producto al arreglo
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR SQL AL OBTENER DETALLES EN PedidoDAO: " + e.getMessage());
            e.printStackTrace();
        }
        // Inyecta el arreglo de productos dentro del objeto JSON principal y lo retorna.
        res.put("products", arrayProductos);
        return res;
    }
}