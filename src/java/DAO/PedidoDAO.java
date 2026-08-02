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
        try (Connection con = Conexion.getConnection();
             java.sql.Statement stmt = con.createStatement()) {
            stmt.executeUpdate("ALTER TABLE pedido ADD COLUMN estado_pago VARCHAR(20) DEFAULT 'Sin pagar'");
        } catch (SQLException e) {
            // Ignorar si la columna ya existe o falla por permisos
        }
    }

    /**
     * Recupera y lista todos los pedidos registrados en la tabla 'pedido'.
     *
     * @return Una lista de objetos de tipo Pedido.
     */
    public List<Pedido> listar() {
        List<Pedido> lista = new ArrayList<>();
        
        String sql = "SELECT p.*, u.name AS user_name FROM pedido p "
                    + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario";

        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Pedido p = new Pedido();

                p.setIdPedido(safeString(rs, "id_pedido", "PED-000"));
                p.setIdUsuario(safeString(rs, "id_usuario", null));

                String clientName = safeString(rs, "customer_name", null);
                if (clientName == null) {
                    clientName = safeString(rs, "user_name", "Cliente Anónimo");
                }
                p.setNombreClienteOpcional(clientName);
                p.setTipoEntrega(safeString(rs, "tipo_entrega", "Para consumir aquí"));
                p.setNumeroMesa(safeInt(rs, "numero_mesa"));
                p.setDireccionEntrega(safeString(rs, "direccion_entrega", "No especificada"));
                p.setObservaciones(safeString(rs, "observaciones", ""));
                p.setTotal(safeDouble(rs, "total"));
                p.setEstado(safeString(rs, "estado", "En preparación"));
                p.setFechaPedido(safeString(rs, "fecha_pedido", ""));
                p.setEstadoPago(safeString(rs, "estado_pago", "Sin pagar"));

                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("ERROR SQL AL LISTAR PEDIDOS: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    private String safeString(ResultSet rs, String col, String def) {
        try {
            String v = rs.getString(col);
            return (v != null && !v.trim().isEmpty() && !v.equalsIgnoreCase("null")) ? v : def;
        } catch (Exception e) {
            return def;
        }
    }

    private Integer safeInt(ResultSet rs, String col) {
        try {
            int v = rs.getInt(col);
            return rs.wasNull() ? null : v;
        } catch (Exception e) {
            return null;
        }
    }

    private double safeDouble(ResultSet rs, String col) {
        try {
            return rs.getDouble(col);
        } catch (Exception e) {
            return 0.0;
        }
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
     * @param idUsuario Identificador del usuario que realiza la acción.
     * @return true si la actualización afectó al menos una fila, false de lo contrario.
     */
    public boolean actualizarEstado(String idPedido, String nuevoEstado, String idUsuario) {
        // Actualiza solo la columna 'estado' donde coincida el ID.
        String sql = "UPDATE pedido SET estado = ? WHERE id_pedido = ?";
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, idPedido);
            
            boolean ok = ps.executeUpdate() > 0;
            if (ok && "Entregado".equalsIgnoreCase(nuevoEstado)) {
                // Registrar en la tabla HistorialPedidos como 'Finalizado'
                HistorialDAO historialDao = new HistorialDAO();
                boolean histOk = historialDao.registrarMovimientoHistorial(idPedido, idUsuario, "Finalizado", "Pedido entregado correctamente");
                if (!histOk) {
                    System.err.println("[WARN - PedidoDAO] No se pudo registrar el movimiento en HistorialPedidos para " + idPedido);
                }
            }
            return ok;
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

                    String estadoPago = "Sin pagar";
                    try {
                        estadoPago = rs.getString("estado_pago");
                        if (estadoPago == null) estadoPago = "Sin pagar";
                    } catch (SQLException e) {
                        estadoPago = "Sin pagar";
                    }
                    res.put("estadoPago", estadoPago);
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

    /**
     * Actualiza únicamente el estado de pago del pedido ('Pagado' o 'Sin pagar').
     */
    public boolean actualizarEstadoPago(String idPedido, String nuevoEstadoPago) {
        String sql = "UPDATE pedido SET estado_pago = ? WHERE id_pedido = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstadoPago);
            ps.setString(2, idPedido);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando estado_pago en PedidoDAO: " + e.getMessage());
            return false;
        }
    }
}
