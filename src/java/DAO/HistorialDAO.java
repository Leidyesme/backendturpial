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
        try (Connection con = Conexion.getConnection();
             java.sql.Statement stmt = con.createStatement()) {
            stmt.executeUpdate("ALTER TABLE pedido ADD COLUMN estado_pago VARCHAR(20) DEFAULT 'Sin pagar'");
        } catch (SQLException e) {
            // Ignorar si la columna ya existe
        }
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
        return registrarPedidoDetallado(h, productos, tipoEntrega, numeroMesa, direccionEntrega) == null;
    }

    /**
     * Registra un nuevo pedido junto a sus detalles emitiendo una respuesta estructurada.
     * Retorna null si fue exitoso, o una cadena explicativa con la causa exacta del error.
     */
    public String registrarPedidoDetallado(Historial h, JSONArray productos, String tipoEntrega, Integer numeroMesa, String direccionEntrega) {
        if (productos == null || productos.isEmpty()) {
            return "El carrito de compras está vacío o no contiene productos válidos.";
        }

        try (Connection con = Conexion.getConnection()) {
            if (con == null) {
                return "No se pudo conectar con la base de datos de El Turpial.";
            }
            con.setAutoCommit(false);

            // 1. OBTENER EL ID SIGUIENTE PARA EL PEDIDO DENTRO DE LA MISMA TRANSACCIÓN
            String queryNextPedidoId = "SELECT id_pedido FROM pedido ORDER BY CAST(SUBSTRING(id_pedido, 5) AS UNSIGNED) DESC LIMIT 1";
            String nextPedidoId = "PED-001";
            try (PreparedStatement psMax = con.prepareStatement(queryNextPedidoId);
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
            }
            h.setIdPedido(nextPedidoId);

            // 2. OBTENER EL ID SIGUIENTE PARA EL DETALLE DENTRO DE LA MISMA TRANSACCIÓN
            String queryNextDetalleId = "SELECT id_detallepedido FROM detallepedido ORDER BY CAST(SUBSTRING(id_detallepedido, 5) AS UNSIGNED) DESC LIMIT 1";
            int lastDetalleNum = 0;
            try (PreparedStatement psMaxD = con.prepareStatement(queryNextDetalleId);
                 ResultSet rsMaxD = psMaxD.executeQuery()) {
                if (rsMaxD.next()) {
                    String maxId = rsMaxD.getString("id_detallepedido");
                    if (maxId != null && maxId.startsWith("DET-")) {
                        try {
                            lastDetalleNum = Integer.parseInt(maxId.substring(4));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parseando ID de detalle máximo: " + e.getMessage());
                        }
                    }
                }
            }

            // 3. DECLARAR CONSULTAS SQL
            String sqlPedido = "INSERT INTO pedido (id_pedido, id_usuario, customer_name, tipo_entrega, total, estado, fecha_pedido, numero_mesa, direccion_entrega, estado_pago) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlDetalle = "INSERT INTO detallepedido (id_detallepedido, id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)";
            String sqlLookupProdById = "SELECT id_producto FROM producto WHERE id_producto = ?";
            String sqlLookupProdByName = "SELECT id_producto FROM producto WHERE nombre = ?";
            String sqlUpdateStock = "UPDATE Producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";

            // 4. VALIDAR USUARIO Y NOMBRE DE CLIENTE
            String finalIdUsuario = h.getIdUsuario();
            String customerName = "Cliente";

            if (finalIdUsuario != null && !finalIdUsuario.trim().isEmpty() && !finalIdUsuario.equalsIgnoreCase("null")) {
                try (PreparedStatement psUser = con.prepareStatement("SELECT name FROM usuario WHERE id_usuario = ?")) {
                    psUser.setString(1, finalIdUsuario);
                    try (ResultSet rsU = psUser.executeQuery()) {
                        if (rsU.next()) {
                            customerName = rsU.getString("name");
                        } else {
                            finalIdUsuario = null;
                        }
                    }
                }
            } else {
                finalIdUsuario = null;
            }

            // 5. INSERTAR CABECERA DE PEDIDO
            try (PreparedStatement psP = con.prepareStatement(sqlPedido)) {
                psP.setString(1, h.getIdPedido());
                if (finalIdUsuario != null) {
                    psP.setString(2, finalIdUsuario);
                } else {
                    psP.setNull(2, java.sql.Types.VARCHAR);
                }
                psP.setString(3, customerName);
                psP.setString(4, sanitizarTipoEntrega(tipoEntrega));
                psP.setDouble(5, h.getTotal());
                psP.setString(6, sanitizarEstadoPedido(h.getEstado()));
                psP.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis()));

                if (numeroMesa != null && numeroMesa > 0) {
                    psP.setInt(8, numeroMesa);
                } else {
                    psP.setNull(8, java.sql.Types.INTEGER);
                }

                psP.setString(9, direccionEntrega);
                psP.setString(10, sanitizarEstadoPago(h.getEstadoPago(), tipoEntrega));

                int filasAfectadas = psP.executeUpdate();
                if (filasAfectadas == 0) {
                    con.rollback();
                    return "No se pudo insertar la orden en la base de datos.";
                }

                // 6. PROCESAR CADA PRODUCTO Y DESCONTAR INVENTARIO
                try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                     PreparedStatement psLookupId = con.prepareStatement(sqlLookupProdById);
                     PreparedStatement psLookupName = con.prepareStatement(sqlLookupProdByName);
                     PreparedStatement psStock = con.prepareStatement(sqlUpdateStock)) {

                    for (int i = 0; i < productos.length(); i++) {
                        JSONObject prod = productos.getJSONObject(i);
                        String prodIdReq = prod.optString("idProducto", prod.optString("id", ""));
                        String prodName = prod.optString("name", prod.optString("nombre", ""));
                        double price = prod.optDouble("price", prod.optDouble("precio", 0.0));
                        int quantity = prod.optInt("quantity", prod.optInt("cantidad", 1));

                        String idProducto = null;
                        if (!prodIdReq.trim().isEmpty() && !prodIdReq.equalsIgnoreCase("null")) {
                            psLookupId.setString(1, prodIdReq);
                            try (ResultSet rsId = psLookupId.executeQuery()) {
                                if (rsId.next()) {
                                    idProducto = rsId.getString("id_producto");
                                }
                            }
                        }

                        if (idProducto == null && !prodName.trim().isEmpty()) {
                            psLookupName.setString(1, prodName);
                            try (ResultSet rsL = psLookupName.executeQuery()) {
                                if (rsL.next()) {
                                    idProducto = rsL.getString("id_producto");
                                }
                            }
                        }

                        if (idProducto == null && !prodName.trim().isEmpty()) {
                            String cleanName = prodName.trim();
                            try (PreparedStatement psFuzzy = con.prepareStatement(
                                    "SELECT id_producto FROM producto WHERE LOWER(nombre) LIKE LOWER(?) OR LOWER(?) LIKE CONCAT('%', LOWER(nombre), '%') LIMIT 1")) {
                                psFuzzy.setString(1, "%" + cleanName + "%");
                                psFuzzy.setString(2, cleanName);
                                try (ResultSet rsF = psFuzzy.executeQuery()) {
                                    if (rsF.next()) {
                                        idProducto = rsF.getString("id_producto");
                                    }
                                }
                            }
                        }

                        if (idProducto == null && !prodName.trim().isEmpty()) {
                            String[] words = prodName.trim().split("\\s+");
                            if (words.length > 0 && words[0].length() >= 4) {
                                try (PreparedStatement psWord = con.prepareStatement("SELECT id_producto FROM producto WHERE LOWER(nombre) LIKE LOWER(?) LIMIT 1")) {
                                    psWord.setString(1, "%" + words[0] + "%");
                                    try (ResultSet rsW = psWord.executeQuery()) {
                                        if (rsW.next()) {
                                            idProducto = rsW.getString("id_producto");
                                        }
                                    }
                                }
                            }
                        }

                        if (idProducto == null && !prodName.trim().isEmpty()) {
                            // Auto-registrar el producto en el catálogo de MySQL para evitar fallos de venta
                            String newId = "PROD-" + String.format("%03d", (int)(System.currentTimeMillis() % 800 + 100));
                            try (PreparedStatement psAuto = con.prepareStatement(
                                    "INSERT INTO producto (id_producto, id_categoria, nombre, descripcion, precio, stock, unidades_medida) VALUES (?, 'CAT-004', ?, ?, ?, 50, 'Porción')")) {
                                psAuto.setString(1, newId);
                                psAuto.setString(2, prodName.trim());
                                psAuto.setString(3, "Producto de menú registrado automáticamente");
                                psAuto.setDouble(4, price > 0 ? price : 5000.0);
                                if (psAuto.executeUpdate() > 0) {
                                    idProducto = newId;
                                }
                            } catch (SQLException exAuto) {
                                System.err.println("No se pudo auto-registrar el producto en MySQL: " + exAuto.getMessage());
                            }
                        }

                        if (idProducto == null) {
                            con.rollback();
                            return "El producto '" + (prodName.isEmpty() ? prodIdReq : prodName) + "' no existe en el catálogo de la base de datos.";
                        }

                        // Actualizar inventario y verificar stock suficiente
                        psStock.setInt(1, quantity);
                        psStock.setString(2, idProducto);
                        psStock.setInt(3, quantity);
                        int stockFilas = psStock.executeUpdate();
                        if (stockFilas == 0) {
                            int stockDisponible = 0;
                            String nombreReal = prodName;
                            try (PreparedStatement psCheck = con.prepareStatement("SELECT stock, nombre FROM producto WHERE id_producto = ?")) {
                                psCheck.setString(1, idProducto);
                                try (ResultSet rsCheck = psCheck.executeQuery()) {
                                    if (rsCheck.next()) {
                                        stockDisponible = rsCheck.getInt("stock");
                                        nombreReal = rsCheck.getString("nombre");
                                    }
                                }
                            }
                            con.rollback();
                            return "El producto '" + nombreReal + "' no tiene suficiente stock disponible en inventario (Stock actual: " + stockDisponible + ", solicitado: " + quantity + ").";
                        }

                        lastDetalleNum++;
                        String nextDetalleId = String.format("DET-%03d", lastDetalleNum);

                        psD.setString(1, nextDetalleId);
                        psD.setString(2, h.getIdPedido());
                        psD.setString(3, idProducto);
                        psD.setInt(4, quantity);
                        psD.setDouble(5, price);
                        psD.addBatch();
                    }

                    psD.executeBatch();
                }

                con.commit();
                return null; // ÉXITO
            } catch (SQLException e) {
                con.rollback();
                System.err.println("Error SQL en la transacción: " + e.getMessage());
                e.printStackTrace();
                return "Error de base de datos MySQL: " + e.getMessage();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error general de conexión JDBC: " + e.getMessage());
            return "No se pudo establecer conexión con la base de datos: " + e.getMessage();
        }
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
                        h.setEstadoPago(rs.getString("estado_pago") != null ? rs.getString("estado_pago") : "Sin pagar");
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
                        h.setEstadoPago(rs.getString("estado_pago") != null ? rs.getString("estado_pago") : "Sin pagar");
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

    /**
     * Sanitiza y valida el tipo de entrega para garantizar que coincida exactamente
     * con los valores definidos en el ENUM de la tabla Pedido de MySQL:
     * ('A domicilio', 'Para recoger', 'Para consumir aquí').
     *
     * @param rawTipo Cadena enviada por el cliente.
     * @return Valor sanitizado compatible con ENUM de MySQL.
     */
    private String sanitizarTipoEntrega(String rawTipo) {
        if (rawTipo == null || rawTipo.trim().isEmpty()) {
            return "Para consumir aquí";
        }
        String normalized = rawTipo.trim().toLowerCase();
        if (normalized.contains("domicilio")) {
            return "A domicilio";
        } else if (normalized.contains("recoger") || normalized.contains("llevar")) {
            return "Para recoger";
        } else if (normalized.contains("aqui") || normalized.contains("aquí") || normalized.contains("consumir") || normalized.contains("local") || normalized.contains("mesa")) {
            return "Para consumir aquí";
        }
        return "Para consumir aquí";
    }

    /**
     * Sanitiza y valida el estado del pedido para garantizar compatibilidad con ENUM de MySQL:
     * ('En preparación', 'Listo', 'En espera', 'Entregado').
     *
     * @param rawEstado Cadena enviada desde el sistema.
     * @return Valor sanitizado compatible con ENUM de MySQL.
     */
    private String sanitizarEstadoPedido(String rawEstado) {
        if (rawEstado == null || rawEstado.trim().isEmpty()) {
            return "En preparación";
        }
        String norm = rawEstado.trim().toLowerCase();
        if (norm.contains("preparac") || norm.contains("proceso")) {
            return "En preparación";
        } else if (norm.contains("listo")) {
            return "Listo";
        } else if (norm.contains("espera") || norm.contains("pendiente")) {
            return "En espera";
        } else if (norm.contains("entregado") || norm.contains("completado")) {
            return "Entregado";
        }
        return "En preparación";
    }

    /**
     * Sanitiza el estado de pago del pedido ('Pagado' o 'Sin pagar').
     * Si no se especifica, 'Para consumir aquí' es por defecto 'Sin pagar',
     * mientras que 'Para recoger' o 'A domicilio' es por defecto 'Pagado'.
     */
    public String sanitizarEstadoPago(String rawEstadoPago, String tipoEntrega) {
        if (rawEstadoPago != null && !rawEstadoPago.trim().isEmpty()) {
            String norm = rawEstadoPago.trim().toLowerCase();
            if (norm.contains("pagado") || norm.contains("pago") || norm.contains("si") || norm.contains("sí")) {
                return "Pagado";
            }
            if (norm.contains("sin") || norm.contains("no") || norm.contains("pend") || norm.contains("debe")) {
                return "Sin pagar";
            }
        }
        String sanitizedTipo = sanitizarTipoEntrega(tipoEntrega);
        if ("Para consumir aquí".equalsIgnoreCase(sanitizedTipo)) {
            return "Sin pagar";
        }
        return "Pagado";
    }
}