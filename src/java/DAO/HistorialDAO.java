// Declarar el paquete del DAO correspondiente a los datos de Historial
package DAO;

// Importar la clase de configuración de la conexión a la base de datos
import Modelo.Config.Conexion;
// Qué hace: Importa la clase de conexión JDBC.
// Para qué sirve / Destino: Permite obtener instancias de conexión (`Connection`) para comunicarse con la base de datos MySQL.

// Importar la entidad Historial para representar un pedido en Java
import Modelo.Entidades.Historial;
// Qué hace: Importa la entidad de dominio Historial.
// Para qué sirve / Destino: Facilita el mapeo de los datos del pedido en objetos Java para transferirlos hacia los controladores o servlets.

// Importar la clase Connection para manejar la conexión de base de datos
import java.sql.Connection;
// Qué hace: Importa la interfaz Connection de JDBC.
// Para qué sirve / Destino: Administra el canal de comunicación con la base de datos MySQL.

// Importar PreparedStatement para ejecutar sentencias SQL preparadas
import java.sql.PreparedStatement;
// Qué hace: Importa la interfaz PreparedStatement.
// Para qué sirve / Destino: Permite ejecutar consultas SQL parametrizadas de manera segura, comunicándose directamente con MySQL.

// Importar ResultSet para recorrer el resultado de las consultas SQL
import java.sql.ResultSet;
// Qué hace: Importa la clase ResultSet.
// Para qué sirve / Destino: Almacena y recorre iterativamente los registros tabulares devueltos por las consultas SELECT en MySQL.

// Importar SQLException para atrapar errores relacionales con MySQL
import java.sql.SQLException;
// Qué hace: Importa la clase SQLException.
// Para qué sirve / Destino: Captura errores relacionados con fallos de conexión o sintaxis en las consultas hacia la base de datos MySQL.

// Importar ArrayList para el manejo de listas de pedidos
import java.util.ArrayList;
// Qué hace: Importa la implementación ArrayList de Java.
// Para qué sirve / Destino: Permite crear listas dinámicas en memoria para almacenar colecciones de objetos Historial.

// Importar List para devolver listados estructurados de objetos Historial
import java.util.List;
// Qué hace: Importa la interfaz List de Java.
// Para qué sirve / Destino: Define el tipo de dato abstracto que retornan los métodos de consulta hacia la capa de controladores.

// Importar JSONObject para manejar de forma interna la lista de productos de la orden
import org.json.JSONObject;
// Qué hace: Importa la clase JSONObject de la librería org.json.
// Para qué sirve / Destino: Permite deserializar y manipular los datos individuales de los productos recibidos desde el cliente.

// Importar JSONArray para representar el listado de productos de forma estructurada
import org.json.JSONArray;
// Qué hace: Importa la clase JSONArray de la librería org.json.
// Para qué sirve / Destino: Permite procesar el carrito de compras estructurado como una lista de objetos JSON enviado por la interfaz.

/**
 * Clase de acceso a datos para el Historial de pedidos del cliente.
 * Administra el guardado compuesto (maestro-detalle) de pedidos y consultas de historial.
 */
public class HistorialDAO {
// Qué hace: Declara la clase pública HistorialDAO.
// Para qué sirve / Destino: Actúa como el componente central de persistencia y comunicación con MySQL para los pedidos.

    /**
     * Constructor por defecto del DAO.
     */
    public HistorialDAO() {
    // Qué hace: Declara el constructor de la clase HistorialDAO.
    // Para qué sirve / Destino: Se ejecuta al instanciar el DAO para asegurar la actualización estructural de la base de datos.

        try (Connection con = Conexion.getConnection();
             java.sql.Statement stmt = con.createStatement()) {
        // Qué hace: Abre una conexión y un objeto Statement usando try-with-resources.
        // Para qué sirve / Destino: Establece comunicación inicial con la base de datos MySQL para realizar modificaciones de esquema.

            stmt.executeUpdate("ALTER TABLE pedido ADD COLUMN estado_pago VARCHAR(20) DEFAULT 'Sin pagar'");
            // Qué hace: Ejecuta una instrucción DDL para agregar la columna estado_pago a la tabla pedido si no existe.
            // Para qué sirve / Destino: Interactúa con la base de datos MySQL para prevenir errores de columnas faltantes.

        } catch (SQLException e) {
        // Qué hace: Captura cualquier excepción de tipo SQL generada en el bloque anterior.
        // Para qué sirve / Destino: Maneja errores relacionales a nivel de base de datos.

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
    // Qué hace: Define un método público sobrecargado que retorna un booleano.
    // Para qué sirve / Destino: Recibe los datos desde los controladores y delega la ejecución transaccional principal.

        return registrarPedidoDetallado(h, productos, tipoEntrega, numeroMesa, direccionEntrega) == null;
        // Qué hace: Invoca al método detallado y evalúa si el resultado es nulo.
        // Para qué sirve / Destino: Retorna true al controlador si el registro fue exitoso o false en caso de error.
    }

    /**
     * Registra un nuevo pedido junto a sus detalles emitiendo una respuesta estructurada.
     * Retorna null si fue exitoso, o una cadena explicativa con la causa exacta del error.
     */
    public String registrarPedidoDetallado(Historial h, JSONArray productos, String tipoEntrega, Integer numeroMesa, String direccionEntrega) {
    // Qué hace: Define el método principal encargado de procesar la transacción completa del pedido.
    // Para qué sirve / Destino: Comunica los controladores web con la lógica transaccional compleja en MySQL.

        if (productos == null || productos.isEmpty()) {
        // Qué hace: Valida si el arreglo de productos es nulo o está vacío.
        // Para qué sirve / Destino: Filtra carritos inválidos antes de abrir conexiones o interactuar con la base de datos.

            return "El carrito de compras está vacío o no contiene productos válidos.";
            // Qué hace: Retorna un mensaje de error descriptivo.
            // Para qué sirve / Destino: Envía la respuesta de fallo directamente hacia el controlador.
        }

        try (Connection con = Conexion.getConnection()) {
        // Qué hace: Obtiene una conexión JDBC mediante try-with-resources.
        // Para qué sirve / Destino: Establece la comunicación activa con la base de datos MySQL.

            if (con == null) {
            // Qué hace: Verifica si la conexión devuelta es nula.
            // Para qué sirve / Destino: Controla fallos de disponibilidad del servidor de base de datos.

                return "No se pudo conectar con la base de datos de El Turpial.";
                // Qué hace: Retorna un mensaje de error de conectividad.
                // Para qué sirve / Destino: Informa al controlador sobre la imposibilidad de comunicarse con MySQL.
            }
            con.setAutoCommit(false);
            // Qué hace: Desactiva el modo de confirmación automática de la conexión.
            // Para qué sirve / Destino: Inicia una transacción controlada manualmente para aplicar commit o rollback en MySQL.

            // 1. OBTENER EL ID SIGUIENTE PARA EL PEDIDO DENTRO DE LA MISMA TRANSACCIÓN
            String queryNextPedidoId = "SELECT id_pedido FROM pedido ORDER BY CAST(SUBSTRING(id_pedido, 5) AS UNSIGNED) DESC LIMIT 1";
            // Qué hace: Define la consulta SQL para obtener el ID de pedido más alto actual.
            // Para qué sirve / Destino: Prepara la instrucción para consultar la tabla pedido en MySQL.

            String nextPedidoId = "PED-001";
            // Qué hace: Inicializa una cadena con el ID por defecto para el primer pedido.
            // Para qué sirve / Destino: Establece un valor base en la lógica de negocio de Java.

            try (PreparedStatement psMax = con.prepareStatement(queryNextPedidoId);
                 ResultSet rsMax = psMax.executeQuery()) {
            // Qué hace: Prepara y ejecuta la consulta para hallar el identificador máximo.
            // Para qué sirve / Destino: Interactúa con la base de datos MySQL para evitar duplicidad de claves primarias.

                if (rsMax.next()) {
                // Qué hace: Posiciona el cursor en el registro devuelto por la consulta.
                // Para qué sirve / Destino: Valida si ya existen pedidos registrados previamente en MySQL.

                    String maxId = rsMax.getString("id_pedido");
                    // Qué hace: Extrae el valor del ID máximo de la columna obtenida.
                    // Para qué sirve / Destino: Obtiene el texto identificador desde el resultado de la base de datos.

                    if (maxId != null && maxId.startsWith("PED-")) {
                    // Qué hace: Valida que el ID no sea nulo y cumpla con el formato esperado.
                    // Para qué sirve / Destino: Asegura la integridad del formato de las claves primarias.

                        try {
                        // Qué hace: Inicia un bloque protegido para conversión numérica.
                        // Para qué sirve / Destino: Controla posibles errores de formato al parsear cadenas.

                            int num = Integer.parseInt(maxId.substring(4));
                            // Qué hace: Extrae la porción numérica del ID de pedido.
                            // Para qué sirve / Destino: Convierte la parte numérica a entero para realizar operaciones matemáticas.

                            nextPedidoId = String.format("PED-%03d", num + 1);
                            // Qué hace: Formatea el siguiente ID correlativo sumando uno.
                            // Para qué sirve / Destino: Genera el nuevo identificador secuencial para el pedido.

                        } catch (NumberFormatException e) {
                        // Qué hace: Captura errores de conversión numérica.
                        // Para qué sirve / Destino: Registra incidencias en consola sin interrumpir la ejecución del sistema.

                            System.err.println("Error parseando ID de pedido máximo: " + e.getMessage());
                        }
                    }
                }
            }
            h.setIdPedido(nextPedidoId);
            // Qué hace: Asigna el nuevo ID generado al objeto de la entidad Historial.
            // Para qué sirve / Destino: Actualiza el modelo de datos interno en Java antes de la persistencia.

            // 2. OBTENER EL ID SIGUIENTE PARA EL DETALLE DENTRO DE LA MISMA TRANSACCIÓN
            String queryNextDetalleId = "SELECT id_detallepedido FROM detallepedido ORDER BY CAST(SUBSTRING(id_detallepedido, 5) AS UNSIGNED) DESC LIMIT 1";
            // Qué hace: Define la consulta SQL para obtener el ID de detalle de pedido más alto.
            // Para qué sirve / Destino: Prepara la instrucción para consultar la tabla detallepedido en MySQL.

            int lastDetalleNum = 0;
            // Qué hace: Inicializa en cero la variable numérica del último detalle.
            // Para qué sirve / Destino: Establece un valor base para el cálculo de identificadores de detalle.

            try (PreparedStatement psMaxD = con.prepareStatement(queryNextDetalleId);
                 ResultSet rsMaxD = psMaxD.executeQuery()) {
            // Qué hace: Prepara y ejecuta la consulta del identificador máximo de detalle.
            // Para qué sirve / Destino: Consulta la tabla detallepedido en MySQL para mantener la secuencia.

                if (rsMaxD.next()) {
                // Qué hace: Verifica si hay registros previos en el resultado.
                // Para qué sirve / Destino: Evalúa la existencia de datos en la base de datos.

                    String maxId = rsMaxD.getString("id_detallepedido");
                    // Qué hace: Recupera el string del ID máximo de detalle.
                    // Para qué sirve / Destino: Obtiene la clave primaria actual desde MySQL.

                    if (maxId != null && maxId.startsWith("DET-")) {
                    // Qué hace: Comprueba que el formato del ID comience con el prefijo correcto.
                    // Para qué sirve / Destino: Valida la consistencia de los datos almacenados.

                        try {
                        // Qué hace: Protege la conversión numérica del identificador.
                        // Para qué sirve / Destino: Previene excepciones por formato de texto inválido.

                            lastDetalleNum = Integer.parseInt(maxId.substring(4));
                            // Qué hace: Extrae y convierte la parte numérica del ID de detalle.
                            // Para qué sirve / Destino: Almacena el número entero actual para el cálculo correlativo.

                        } catch (NumberFormatException e) {
                        // Qué hace: Captura errores de parseo numérico.
                        // Para qué sirve / Destino: Muestra mensajes de depuración en la salida de errores estándar.

                            System.err.println("Error parseando ID de detalle máximo: " + e.getMessage());
                        }
                    }
                }
            }

            // 3. DECLARAR CONSULTAS SQL
            String sqlPedido = "INSERT INTO pedido (id_pedido, id_usuario, customer_name, tipo_entrega, total, estado, fecha_pedido, numero_mesa, direccion_entrega, estado_pago) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // Qué hace: Define la sentencia SQL parametrizada para insertar la cabecera del pedido.
            // Para qué sirve / Destino: Modifica la tabla pedido en la base de datos MySQL.

            String sqlDetalle = "INSERT INTO detallepedido (id_detallepedido, id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)";
            // Qué hace: Define la sentencia SQL parametrizada para insertar los detalles del pedido.
            // Para qué sirve / Destino: Modifica la tabla detallepedido en la base de datos MySQL.

            String sqlLookupProdById = "SELECT id_producto FROM producto WHERE id_producto = ?";
            // Qué hace: Define la consulta SQL para buscar productos por su ID.
            // Para qué sirve / Destino: Consulta la tabla producto en MySQL.

            String sqlLookupProdByName = "SELECT id_producto FROM producto WHERE nombre = ?";
            // Qué hace: Define la consulta SQL para buscar productos por su nombre exacto.
            // Para qué sirve / Destino: Consulta la tabla producto en MySQL.

            String sqlUpdateStock = "UPDATE Producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
            // Qué hace: Define la sentencia SQL para descontar inventario validando stock suficiente.
            // Para qué sirve / Destino: Actualiza la tabla producto en MySQL controlando existencias.

            // 4. VALIDAR USUARIO Y NOMBRE DE CLIENTE
            String finalIdUsuario = h.getIdUsuario();
            // Qué hace: Obtiene el ID del usuario desde la entidad Historial.
            // Para qué sirve / Destino: Almacena temporalmente el identificador del cliente en Java.

            String customerName = "Cliente";
            // Qué hace: Inicializa una cadena por defecto para el nombre del cliente.
            // Para qué sirve / Destino: Proporciona un valor seguro si no se especifica un usuario registrado.

            if (finalIdUsuario != null && !finalIdUsuario.trim().isEmpty() && !finalIdUsuario.equalsIgnoreCase("null")) {
            // Qué hace: Valida que el ID de usuario contenga un valor válido y no sea nulo.
            // Para qué sirve / Destino: Controla el flujo lógico para buscar usuarios registrados en la base de datos.

                try (PreparedStatement psUser = con.prepareStatement("SELECT name FROM usuario WHERE id_usuario = ?")) {
                // Qué hace: Prepara una sentencia para consultar el nombre del usuario en la tabla correspondiente.
                // Para qué sirve / Destino: Se comunica con la tabla usuario en MySQL.

                    psUser.setString(1, finalIdUsuario);
                    // Qué hace: Asigna el parámetro del ID de usuario al marcador de la consulta.
                    // Para qué sirve / Destino: Previene inyección SQL en la consulta hacia MySQL.

                    try (ResultSet rsU = psUser.executeQuery()) {
                    // Qué hace: Ejecuta la consulta del usuario y obtiene el resultado.
                    // Para qué sirve / Destino: Recupera la información desde la base de datos.

                        if (rsU.next()) {
                        // Qué hace: Comprueba si se encontró el registro del usuario.
                        // Para qué sirve / Destino: Valida la existencia del usuario en MySQL.

                            customerName = rsU.getString("name");
                            // Qué hace: Asigna el nombre real del usuario obtenido de la base de datos.
                            // Para qué sirve / Destino: Actualiza la variable customerName para la entidad.

                        } else {
                            finalIdUsuario = null;
                            // Qué hace: Restablece el ID de usuario a nulo si no se encuentra en la base de datos.
                            // Para qué sirve / Destino: Maneja la inconsistencia convirtiendo el pedido en anónimo o de invitado.
                        }
                    }
                }
            } else {
                finalIdUsuario = null;
                // Qué hace: Asigna nulo al ID de usuario si la validación inicial falla.
                // Para qué sirve / Destino: Prepara el valor nulo requerido por la base de datos para invitados.
            }

            // 5. INSERTAR CABECERA DE PEDIDO
            try (PreparedStatement psP = con.prepareStatement(sqlPedido)) {
            // Qué hace: Prepara la sentencia SQL de inserción para la cabecera del pedido.
            // Para qué sirve / Destino: Se comunica con la tabla pedido en MySQL.

                psP.setString(1, h.getIdPedido());
                // Qué hace: Asigna el ID del pedido al primer parámetro de la sentencia.
                // Para qué sirve / Destino: Define la clave primaria del registro en la tabla pedido de MySQL.

                if (finalIdUsuario != null) {
                    psP.setString(2, finalIdUsuario);
                    // Qué hace: Asigna el ID del usuario al segundo parámetro si existe.
                    // Para qué sirve / Destino: Relaciona el pedido con el usuario registrado en MySQL.
                } else {
                    psP.setNull(2, java.sql.Types.VARCHAR);
                    // Qué hace: Inserta un valor nulo en el campo de usuario si no hay sesión registrada.
                    // Para qué sirve / Destino: Permite registrar pedidos de clientes anónimos en MySQL.
                }
                psP.setString(3, customerName);
                // Qué hace: Asigna el nombre del cliente al tercer parámetro.
                // Para qué sirve / Destino: Guarda el nombre amigable del cliente en la tabla pedido de MySQL.

                psP.setString(4, sanitizarTipoEntrega(tipoEntrega));
                // Qué hace: Sanitiza el tipo de entrega y lo asigna al cuarto parámetro.
                // Para qué sirve / Destino: Asegura la compatibilidad con el ENUM de la tabla pedido en MySQL.

                psP.setDouble(5, h.getTotal());
                // Qué hace: Asigna el monto total del pedido al quinto parámetro.
                // Para qué sirve / Destino: Almacena el valor financiero de la orden en MySQL.

                psP.setString(6, sanitizarEstadoPedido(h.getEstado()));
                // Qué hace: Sanitiza el estado del pedido y lo asigna al sexto parámetro.
                // Para qué sirve / Destino: Define el estado inicial compatible con el ENUM de MySQL.

                psP.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis()));
                // Qué hace: Obtiene la marca de tiempo actual del sistema y la asigna al séptimo parámetro.
                // Para qué sirve / Destino: Registra la fecha y hora exacta del pedido en MySQL.

                if (numeroMesa != null && numeroMesa > 0) {
                    psP.setInt(8, numeroMesa);
                    // Qué hace: Asigna el número de mesa si es un valor válido.
                    // Para qué sirve / Destino: Almacena la ubicación física en el local dentro de la tabla pedido en MySQL.
                } else {
                    psP.setNull(8, java.sql.Types.INTEGER);
                    // Qué hace: Asigna un valor nulo al número de mesa si no aplica.
                    // Para qué sirve / Destino: Deja el campo vacío en MySQL para pedidos que no son en mesa.
                }

                psP.setString(9, direccionEntrega);
                // Qué hace: Asigna la dirección de entrega al noveno parámetro.
                // Para qué sirve / Destino: Almacena los datos de envío a domicilio en la tabla pedido de MySQL.

                psP.setString(10, sanitizarEstadoPago(h.getEstadoPago(), tipoEntrega));
                // Qué hace: Sanitiza el estado de pago y lo asigna al décimo parámetro.
                // Para qué sirve / Destino: Registra si la orden se encuentra pagada o pendiente en la base de datos MySQL.

                int filasAfectadas = psP.executeUpdate();
                // Qué hace: Ejecuta la inserción de la cabecera y captura el número de filas afectadas.
                // Para qué sirve / Destino: Confirma la escritura del registro maestro en la base de datos MySQL.

                if (filasAfectadas == 0) {
                // Qué hace: Evalúa si ninguna fila fue afectada por la inserción.
                // Para qué sirve / Destino: Detecta fallos imprevistos en la persistencia del pedido.

                    con.rollback();
                    // Qué hace: Revierte los cambios aplicados en la transacción de la base de datos.
                    // Para qué sirve / Destino: Garantiza la consistencia revirtiendo operaciones previas en MySQL.

                    return "No se pudo insertar la orden en la base de datos.";
                    // Qué hace: Retorna un mensaje de error explicativo.
                    // Para qué sirve / Destino: Comunica la falla de inserción hacia los controladores web.
                }

                // 6. PROCESAR CADA PRODUCTO Y DESCONTAR INVENTARIO
                try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                     PreparedStatement psLookupId = con.prepareStatement(sqlLookupProdById);
                     PreparedStatement psLookupName = con.prepareStatement(sqlLookupProdByName);
                     PreparedStatement psStock = con.prepareStatement(sqlUpdateStock)) {
                // Qué hace: Prepara de forma agrupada todas las sentencias JDBC para procesar los detalles, búsquedas y stock.
                // Para qué sirve / Destino: Optimiza la comunicación con la base de datos MySQL durante la iteración del carrito.

                    for (int i = 0; i < productos.length(); i++) {
                    // Qué hace: Inicia un ciclo iterativo para recorrer cada elemento del arreglo JSON de productos.
                    // Para qué sirve / Destino: Procesa uno a uno los artículos que el cliente añadió a su compra.

                        JSONObject prod = productos.getJSONObject(i);
                        // Qué hace: Obtiene el objeto JSON correspondiente al producto actual en la iteración.
                        // Para qué sirve / Destino: Extrae los datos individuales enviados desde la capa de vista o controlador.

                        String prodIdReq = prod.optString("idProducto", prod.optString("id", ""));
                        // Qué hace: Extrae el ID requerido del producto probando múltiples nombres de clave.
                        // Para qué sirve / Destino: Obtiene el identificador enviado por el cliente en formato JSON.

                        String prodName = prod.optString("name", prod.optString("nombre", ""));
                        // Qué hace: Extrae el nombre del producto probando opciones alternativas de claves JSON.
                        // Para qué sirve / Destino: Obtiene la descripción textual del artículo seleccionado.

                        double price = prod.optDouble("price", prod.optDouble("precio", 0.0));
                        // Qué hace: Extrae el precio unitario del producto desde el objeto JSON.
                        // Para qué sirve / Destino: Obtiene el valor financiero del artículo para el detalle.

                        int quantity = prod.optInt("quantity", prod.optInt("cantidad", 1));
                        // Qué hace: Extrae la cantidad solicitada del producto, usando 1 por defecto.
                        // Para qué sirve / Destino: Define las unidades a comprar y descontar del inventario.

                        String idProducto = null;
                        // Qué hace: Inicializa en nulo la variable local para el ID de producto resuelto.
                        // Para qué sirve / Destino: Prepara el contenedor para almacenar la clave primaria válida del catálogo.

                        if (!prodIdReq.trim().isEmpty() && !prodIdReq.equalsIgnoreCase("null")) {
                        // Qué hace: Verifica si se proporcionó un ID válido de producto.
                        // Para qué sirve / Destino: Controla el flujo para buscar el producto por su código exacto en MySQL.

                            psLookupId.setString(1, prodIdReq);
                            // Qué hace: Asigna el ID del producto al parámetro de la consulta de búsqueda.
                            // Para qué sirve / Destino: Configura la sentencia para buscar en la tabla producto de MySQL.

                            try (ResultSet rsId = psLookupId.executeQuery()) {
                            // Qué hace: Ejecuta la consulta de búsqueda por ID y obtiene el resultado.
                            // Para qué sirve / Destino: Consulta la base de datos para verificar existencias.

                                if (rsId.next()) {
                                    idProducto = rsId.getString("id_producto");
                                    // Qué hace: Asigna el ID encontrado en la base de datos a la variable local.
                                    // Para qué sirve / Destino: Confirma la existencia del producto en el catálogo de MySQL.
                                }
                            }
                        }

                        if (idProducto == null && !prodName.trim().isEmpty()) {
                        // Qué hace: Si no se halló por ID, evalúa buscar por el nombre exacto del producto.
                        // Para qué sirve / Destino: Ejecuta una estrategia de respaldo consultando la tabla producto en MySQL.

                            psLookupName.setString(1, prodName);
                            // Qué hace: Asigna el nombre del producto al parámetro de la consulta.
                            // Para qué sirve / Destino: Configura la sentencia SQL para buscar por nombre en MySQL.

                            try (ResultSet rsL = psLookupName.executeQuery()) {
                            // Qué hace: Ejecuta la consulta por nombre y obtiene el resultado.
                            // Para qué sirve / Destino: Verifica el catálogo en la base de datos.

                                if (rsL.next()) {
                                    idProducto = rsL.getString("id_producto");
                                    // Qué hace: Asigna el ID recuperado a la variable local.
                                    // Para qué sirve / Destino: Relaciona el nombre del artículo con su clave primaria en MySQL.
                                }
                            }
                        }

                        if (idProducto == null && !prodName.trim().isEmpty()) {
                        // Qué hace: Si aún no se encuentra, ejecuta una búsqueda difusa (fuzzy search) basada en coincidencias parciales.
                        // Para qué sirve / Destino: Consulta la tabla producto en MySQL usando operadores LIKE para tolerancia a variaciones de texto.

                            String cleanName = prodName.trim();
                            // Qué hace: Limpia los espacios en blanco del nombre del producto.
                            // Para qué sirve / Destino: Prepara la cadena para la consulta de coincidencia difusa.

                            try (PreparedStatement psFuzzy = con.prepareStatement(
                                    "SELECT id_producto FROM producto WHERE LOWER(nombre) LIKE LOWER(?) OR LOWER(?) LIKE CONCAT('%', LOWER(nombre), '%') LIMIT 1")) {
                            // Qué hace: Prepara una sentencia SQL con patrones de coincidencia flexible y funciones de cadena de MySQL.
                            // Para qué sirve / Destino: Se comunica con la tabla producto de MySQL para encontrar similitudes en el texto.

                                psFuzzy.setString(1, "%" + cleanName + "%");
                                psFuzzy.setString(2, cleanName);
                                // Qué hace: Asigna los parámetros de búsqueda difusa con comodines.
                                // Para qué sirve / Destino: Configura los filtros de texto para la consulta en MySQL.

                                try (ResultSet rsF = psFuzzy.executeQuery()) {
                                    if (rsF.next()) {
                                        idProducto = rsF.getString("id_producto");
                                        // Qué hace: Asigna el ID encontrado mediante la búsqueda difusa.
                                        // Para qué sirve / Destino: Recupera la clave primaria desde el catálogo de MySQL.
                                    }
                                }
                            }
                        }

                        if (idProducto == null && !prodName.trim().isEmpty()) {
                        // Qué hace: Si las búsquedas anteriores fallan, intenta buscar por la primera palabra clave principal del producto.
                        // Para qué sirve / Destino: Aplica un filtro de búsqueda por subcadenas largas en la base de datos MySQL.

                            String[] words = prodName.trim().split("\\s+");
                            // Qué hace: Divide el nombre del producto en un arreglo de palabras separadas por espacios.
                            // Para qué sirve / Destino: Facilita el análisis léxico del texto en Java.

                            if (words.length > 0 && words[0].length() >= 4) {
                            // Qué hace: Valida que exista al menos una palabra con una longitud de 4 o más caracteres.
                            // Para qué sirve / Destino: Evita consultas ambiguas con palabras demasiado cortas hacia MySQL.

                                try (PreparedStatement psWord = con.prepareStatement("SELECT id_producto FROM producto WHERE LOWER(nombre) LIKE LOWER(?) LIMIT 1")) {
                                // Qué hace: Prepara una consulta SQL filtrando por la primera palabra clave.
                                // Para qué sirve / Destino: Se comunica con la tabla producto en MySQL.

                                    psWord.setString(1, "%" + words[0] + "%");
                                    // Qué hace: Asigna el patrón de búsqueda con comodines para la palabra clave.
                                    // Para qué sirve / Destino: Configura la sentencia SQL para la base de datos.

                                    try (ResultSet rsW = psWord.executeQuery()) {
                                        if (rsW.next()) {
                                            idProducto = rsW.getString("id_producto");
                                            // Qué hace: Asigna el ID recuperado mediante la búsqueda por palabra clave.
                                            // Para qué sirve / Destino: Obtiene la coincidencia desde el catálogo de MySQL.
                                        }
                                    }
                                }
                            }
                        }

                        if (idProducto == null && !prodName.trim().isEmpty()) {
                        // Qué hace: Si el producto de ninguna manera existe en el catálogo, aplica una estrategia de auto-registro.
                        // Para qué sirve / Destino: Inserta automáticamente el producto desconocido en la base de datos para evitar fallos de venta.

                            // Auto-registrar el producto en el catálogo de MySQL para evitar fallos de venta
                            String newId = "PROD-" + String.format("%03d", (int)(System.currentTimeMillis() % 800 + 100));
                            // Qué hace: Genera un ID temporal único basado en la hora actual del sistema.
                            // Para qué sirve / Destino: Crea una clave primaria sintética para el nuevo producto en MySQL.

                            try (PreparedStatement psAuto = con.prepareStatement(
                                    "INSERT INTO producto (id_producto, id_categoria, nombre, descripcion, precio, stock, unidades_medida) VALUES (?, 'CAT-004', ?, ?, ?, 50, 'Porción')")) {
                            // Qué hace: Prepara la sentencia SQL de inserción para registrar el nuevo producto de menú.
                            // Para qué sirve / Destino: Modifica la tabla producto en la base de datos MySQL.

                                psAuto.setString(1, newId);
                                psAuto.setString(2, prodName.trim());
                                psAuto.setString(3, "Producto de menú registrado automáticamente");
                                psAuto.setDouble(4, price > 0 ? price : 5000.0);
                                // Qué hace: Asigna los valores de ID, nombre, descripción y precio a la inserción automática.
                                // Para qué sirve / Destino: Configura los datos predeterminados del producto en la tabla producto de MySQL.

                                if (psAuto.executeUpdate() > 0) {
                                    idProducto = newId;
                                    // Qué hace: Si la inserción es exitosa, asigna el nuevo ID a la variable local.
                                    // Para qué sirve / Destino: Permite continuar con el proceso de venta usando el producto recién creado en MySQL.
                                }
                            } catch (SQLException exAuto) {
                            // Qué hace: Captura errores ocurridos durante el auto-registro en la base de datos.
                            // Para qué sirve / Destino: Registra la incidencia de persistencia en la consola de errores estándar.

                                System.err.println("No se pudo auto-registrar el producto en MySQL: " + exAuto.getMessage());
                            }
                        }

                        if (idProducto == null) {
                        // Qué hace: Si tras todos los intentos el ID sigue siendo nulo, aborta el proceso.
                        // Para qué sirve / Destino: Controla errores críticos de integridad del catálogo de productos.

                            con.rollback();
                            // Qué hace: Revierte todos los cambios de la transacción actual en la base de datos.
                            // Para qué sirve / Destino: Deshace cualquier inserción previa en MySQL para mantener la consistencia.

                            return "El producto '" + (prodName.isEmpty() ? prodIdReq : prodName) + "' no existe en el catálogo de la base de datos.";
                            // Qué hace: Retorna un mensaje explicativo detallando el producto faltante.
                            // Para qué sirve / Destino: Envía la respuesta de error directamente hacia el controlador web.
                        }

                        // Actualizar inventario y verificar stock suficiente
                        psStock.setInt(1, quantity);
                        psStock.setString(2, idProducto);
                        psStock.setInt(3, quantity);
                        // Qué hace: Configura los parámetros de la sentencia de actualización de inventario (cantidad a restar, ID del producto y validación de stock mínimo).
                        // Para qué sirve / Destino: Prepara la instrucción UPDATE para controlar el stock en la tabla producto de MySQL.

                        int stockFilas = psStock.executeUpdate();
                        // Qué hace: Ejecuta la actualización de stock y almacena el número de filas afectadas.
                        // Para qué sirve / Destino: Interactúa con la tabla producto en MySQL para descontar las unidades vendidas.

                        if (stockFilas == 0) {
                        // Qué hace: Evalúa si la actualización afectó cero filas (lo que indica stock insuficiente).
                        // Para qué sirve / Destino: Valida las reglas de negocio de inventario contra la base de datos MySQL.

                            int stockDisponible = 0;
                            String nombreReal = prodName;
                            // Qué hace: Inicializa variables locales para almacenar el stock actual y el nombre real del producto.
                            // Para qué sirve / Destino: Prepara contenedores de datos para el mensaje de error detallado.

                            try (PreparedStatement psCheck = con.prepareStatement("SELECT stock, nombre FROM producto WHERE id_producto = ?")) {
                            // Qué hace: Prepara una consulta para verificar el stock actual y el nombre registrado en la base de datos.
                            // Para qué sirve / Destino: Se comunica con la tabla producto en MySQL para auditoría de stock.

                                psCheck.setString(1, idProducto);
                                // Qué hace: Asigna el ID del producto al parámetro de la consulta de verificación.
                                // Para qué sirve / Destino: Configura el filtro para buscar en la base de datos.

                                try (ResultSet rsCheck = psCheck.executeQuery()) {
                                    if (rsCheck.next()) {
                                        stockDisponible = rsCheck.getInt("stock");
                                        nombreReal = rsCheck.getString("nombre");
                                        // Qué hace: Recupera el stock real disponible y el nombre oficial desde el resultado de MySQL.
                                        // Para qué sirve / Destino: Actualiza las variables locales con información precisa de la base de datos.
                                    }
                                }
                            }
                            con.rollback();
                            // Qué hace: Revierte la transacción completa en la base de datos.
                            // Para qué sirve / Destino: Cancela el registro del pedido en MySQL debido a la falta de stock.

                            return "El producto '" + nombreReal + "' no tiene suficiente stock disponible en inventario (Stock actual: " + stockDisponible + ", solicitado: " + quantity + ").";
                            // Qué hace: Retorna un mensaje detallado sobre el déficit de inventario.
                            // Para qué sirve / Destino: Envía la explicación exacta del fallo hacia el controlador web.
                        }

                        lastDetalleNum++;
                        String nextDetalleId = String.format("DET-%03d", lastDetalleNum);
                        // Qué hace: Incrementa el número secuencial de detalle y formatea el nuevo ID único (ej. DET-001).
                        // Para qué sirve / Destino: Genera la clave primaria estructurada para la tabla detallepedido.

                        psD.setString(1, nextDetalleId);
                        psD.setString(2, h.getIdPedido());
                        psD.setString(3, idProducto);
                        psD.setInt(4, quantity);
                        psD.setDouble(5, price);
                        // Qué hace: Asigna los valores de ID de detalle, ID de pedido, ID de producto, cantidad y precio unitario a la sentencia preparada.
                        // Para qué sirve / Destino: Configura los parámetros para insertar los registros en la tabla detallepedido de MySQL.

                        psD.addBatch();
                        // Qué hace: Añade la sentencia actual a un lote de ejecución por lotes (batch).
                        // Para qué sirve / Destino: Agrupa las operaciones de detalle para optimizar su envío masivo hacia MySQL.
                    }

                    psD.executeBatch();
                    // Qué hace: Ejecuta de forma masiva todas las inserciones acumuladas en el lote.
                    // Para qué sirve / Destino: Se comunica con MySQL para procesar todos los detalles del pedido en una sola operación eficiente.
                }

                con.commit();
                // Qué hace: Confirma de manera definitiva la transacción completa en la base de datos.
                // Para qué sirve / Destino: Guarda permanentemente el pedido y sus detalles en las tablas de MySQL mediante commit.

                return null; // ÉXITO
                // Qué hace: Retorna un valor nulo para indicar que el proceso finalizó correctamente.
                // Para qué sirve / Destino: Informa al controlador que la operación se completó sin errores.

            } catch (SQLException e) {
            // Qué hace: Captura cualquier excepción de tipo SQL generada dentro de la transacción.
            // Para qué sirve / Destino: Maneja errores relacionales ocurridos durante la inserción en MySQL.

                con.rollback();
                // Qué hace: Revierte todos los cambios de la transacción ante cualquier fallo imprevisto.
                // Para qué sirve / Destino: Asegura la integridad atómica de los datos en la base de datos MySQL.

                System.err.println("Error SQL en la transacción: " + e.getMessage());
                e.printStackTrace();
                // Qué hace: Registra el mensaje de error y la traza de la excepción en la consola estándar de errores.
                // Para qué sirve / Destino: Facilita la depuración técnica para el desarrollador en Java.

                return "Error de base de datos MySQL: " + e.getMessage();
                // Qué hace: Retorna una cadena explicativa con el detalle del error SQL.
                // Para qué sirve / Destino: Envía la traza del fallo hacia los controladores web.

            } finally {
                con.setAutoCommit(true);
                // Qué hace: Restaura el modo de confirmación automática (auto-commit) original de la conexión en el bloque finally.
                // Para qué sirve / Destino: Limpia el estado de la conexión JDBC para que pueda ser reutilizada de forma segura en el pool.
            }
        } catch (SQLException e) {
        // Qué hace: Captura excepciones SQL ocurridas al momento de establecer o cerrar la conexión general.
        // Para qué sirve / Destino: Maneja fallos globales de conectividad con la base de datos.

            System.err.println("Error general de conexión JDBC: " + e.getMessage());
            // Qué hace: Imprime el error general de conexión en la consola de depuración.
            // Para qué sirve / Destino: Registra incidencias de red o de servidor de base de datos en Java.

            return "No se pudo establecer conexión con la base de datos: " + e.getMessage();
            // Qué hace: Retorna un mensaje descriptivo del fallo de conexión.
            // Para qué sirve / Destino: Comunica al controlador que la base de datos no se encuentra accesible.
        }
    }

    /**
     * Recupera el listado de pedidos completados o en proceso realizados por un usuario específico.
     *
     * @param idUsuario Identificador único del usuario a consultar.
     * @return Lista de objetos Historial correspondientes.
     */
    public List<Historial> obtenerHistorialUsuario(String idUsuario) {
    // Qué hace: Define el método público que retorna una lista de objetos Historial filtrados por usuario.
    // Para qué sirve / Destino: Proporciona datos estructurados para las vistas de historial de un cliente específico en los controladores.

        // Inicializar la lista dinámica ArrayList que almacenará los registros mapeados del historial de pedidos del usuario.
        List<Historial> lista = new ArrayList<>();
        // Qué hace: Crea una instancia de ArrayList vacía en Java.
        // Para qué sirve / Destino: Almacenará los objetos de dominio Historial mapeados desde la base de datos.
        
        // Consulta SQL parametrizada con LEFT JOIN para combinar datos del pedido con el nombre de usuario de la cabecera.
        String sql = "SELECT p.*, u.name AS user_name FROM pedido p "
                   + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario "
                   + "WHERE p.id_usuario = ? ORDER BY p.fecha_pedido DESC";
        // Qué hace: Define la sentencia SQL para consultar pedidos filtrados por ID de usuario con un cruce de tablas.
        // Para qué sirve / Destino: Consulta de manera consolidada las tablas pedido y usuario en MySQL.

        // Abrir la conexión y preparar la ejecución de la consulta. El uso de try-with-resources asegura el cierre de los streams JDBC.
        try (Connection con = Conexion.getConnection()) {
        // Qué hace: Obtiene una conexión JDBC utilizando try-with-resources.
        // Para qué sirve / Destino: Establece comunicación con la base de datos MySQL.

            try (PreparedStatement ps = con.prepareStatement(sql)) {
            // Qué hace: Prepara la consulta SQL para su ejecución segura.
            // Para qué sirve / Destino: Configura la sentencia contra la base de datos MySQL.

                // Vincular el idUsuario recibido por parámetros al primer marcador de posición (?) en la sentencia preparada.
                ps.setString(1, idUsuario);
                // Qué hace: Asigna el parámetro del ID de usuario al marcador de posición de la consulta.
                // Para qué sirve / Destino: Filtra los resultados en MySQL de forma segura previniendo inyección SQL.
                
                // Ejecutar la consulta en base de datos y obtener el ResultSet con los registros devueltos.
                try (ResultSet rs = ps.executeQuery()) {
                // Qué hace: Ejecuta la consulta SELECT y obtiene el conjunto de resultados.
                // Para qué sirve / Destino: Recibe la respuesta tabular enviada por la base de datos MySQL.

                    // Recorrer iterativamente cada registro devuelto por la base de datos.
                    while (rs.next()) {
                    // Qué hace: Mueve el cursor a la siguiente fila del conjunto de resultados.
                    // Para qué sirve / Destino: Permite procesar iterativamente cada registro devuelto por MySQL.

                        // Instanciar un objeto Historial por cada fila de la consulta.
                        Historial h = new Historial();
                        // Qué hace: Crea un nuevo objeto de la entidad Historial en memoria.
                        // Para qué sirve / Destino: Permite estructurar los datos relacionales en objetos orientados a objetos en Java.

                        // Mapear el ID único del pedido.
                        h.setIdPedido(rs.getString("id_pedido"));
                        // Qué hace: Extrae y asigna el ID del pedido desde la columna de la base de datos.
                        // Para qué sirve / Destino: Llena los atributos del modelo Historial.

                        // Mapear el ID del usuario propietario del pedido.
                        h.setIdUsuario(rs.getString("id_usuario"));
                        // Qué hace: Extrae y asigna el ID del usuario desde el resultado SQL.
                        // Para qué sirve / Destino: Actualiza el atributo de usuario en la entidad Historial.

                        // Mapear la fecha del registro del pedido.
                        h.setFecha(rs.getString("fecha_pedido"));
                        // Qué hace: Extrae y asigna la fecha del pedido obtenida de MySQL.
                        // Para qué sirve / Destino: Define la marca temporal dentro del objeto Historial.

                        // Mapear el total financiero del pedido.
                        h.setTotal(rs.getDouble("total"));
                        // Qué hace: Extrae y asigna el monto total numérico del pedido.
                        // Para qué sirve / Destino: Almacena el valor monetario en la entidad de dominio.

                        // Mapear el estado del pedido ('En preparación', 'Listo', 'En espera', 'Entregado').
                        h.setEstado(rs.getString("estado"));
                        // Qué hace: Extrae y asigna el estado actual del pedido desde la base de datos.
                        // Para qué sirve / Destino: Actualiza el estado lógico de la orden en el objeto Java.

                        // Mapear el tipo de entrega seleccionado por el cliente.
                        h.setTipoEntrega(rs.getString("tipo_entrega"));
                        // Qué hace: Extrae y asigna el tipo de entrega obtenido de MySQL.
                        // Para qué sirve / Destino: Define la modalidad de entrega en la entidad Historial.

                        h.setEstadoPago(rs.getString("estado_pago") != null ? rs.getString("estado_pago") : "Sin pagar");
                        // Qué hace: Extrae el estado de pago asignando un valor por defecto si es nulo.
                        // Para qué sirve / Destino: Actualiza la información financiera del pedido en el modelo Java.

                        // Recuperar el valor de la columna 'customer_name' (utilizado para pedidos de clientes no registrados).
                        String clientName = rs.getString("customer_name");
                        // Qué hace: Obtiene el nombre opcional del cliente guardado en la cabecera.
                        // Para qué sirve / Destino: Almacena temporalmente el nombre descriptivo del cliente.

                        // Lógica de respaldo: si está vacío o nulo, usar el nombre del usuario registrado obtenido del JOIN.
                        if (clientName == null || clientName.trim().isEmpty()) {
                            clientName = rs.getString("user_name");
                            // Qué hace: Si el nombre opcional está vacío, toma el nombre proveniente de la tabla usuario mediante el JOIN.
                            // Para qué sirve / Destino: Respalda la obtención del nombre del cliente desde la relación de tablas en MySQL.
                        }

                        // Asignar el nombre del cliente final. Si ambos resultan nulos, usar un valor por defecto no destructivo.
                        h.setCustomerName(clientName != null ? clientName : "Cliente Anónimo");
                        // Qué hace: Asigna el nombre final del cliente a la entidad, usando "Cliente Anónimo" como alternativa segura.
                        // Para qué sirve / Destino: Garantiza que siempre exista una etiqueta de nombre legible para la interfaz.

                        // Agregar el objeto de dominio completamente cargado al listado general de retorno.
                        lista.add(h);
                        // Qué hace: Añade el objeto Historial configurado a la lista dinámica.
                        // Para qué sirve / Destino: Acumula los registros para ser devueltos hacia el controlador.
                    }
                }
            }
        } catch (SQLException e) {
        // Qué hace: Captura excepciones SQL ocurridas durante la consulta del historial.
        // Para qué sirve / Destino: Maneja errores de base de datos al recuperar registros.

            // Escribir el log del error de SQL en consola para el diagnóstico y depuración de fallos de red o base de datos.
            System.err.println("ERROR SQL AL OBTENER HISTORIAL DE USUARIO: " + e.getMessage());
            e.printStackTrace();
            // Qué hace: Registra el mensaje y la traza del error en la consola de depuración estándar.
            // Para qué sirve / Destino: Facilita el análisis de incidencias técnicas en Java.
        }

        // Retornar el listado estructurado de pedidos al Servlet.
        return lista;
        // Qué hace: Retorna la lista acumulada de objetos Historial hacia el componente controlador.
        // Para qué sirve / Destino: Entrega los datos procesados para que sean mostrados en las vistas web.
    }

    /**
     * Recupera el listado completo de todos los pedidos realizados en el sistema.
     * Método reservado para visualización administrativa (Administrador).
     *
     * @return Lista de objetos Historial correspondientes a todos los pedidos.
     */
    public List<Historial> obtenerTodosLosPedidos() {
    // Qué hace: Define el método público que retorna el historial global de todos los pedidos del negocio.
    // Para qué sirve / Destino: Proporciona la información completa para los paneles de control de administración en los controladores.

        // Inicializar el ArrayList que contendrá el historial global de pedidos del negocio.
        List<Historial> lista = new ArrayList<>();
        // Qué hace: Crea una lista dinámica vacía en Java.
        // Para qué sirve / Destino: Almacenará todos los registros globales mapeados desde la base de datos.
        
        // Consulta SQL estructurada para obtener todos los registros de la tabla pedidos y cruzarlos con el nombre del usuario asignado.
        String sql = "SELECT p.*, u.name AS user_name FROM pedido p "
                   + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario "
                   + "ORDER BY p.fecha_pedido DESC";
        // Qué hace: Define la sentencia SQL para consultar todos los pedidos sin filtros restrictivos de usuario.
        // Para qué sirve / Destino: Se comunica con las tablas pedido y usuario en MySQL para obtener datos globales.

        // Abrir conexión JDBC de forma atómica y preparar el PreparedStatement.
        try (Connection con = Conexion.getConnection()) {
        // Qué hace: Abre una conexión JDBC utilizando try-with-resources.
        // Para qué sirve / Destino: Establece comunicación con la base de datos MySQL.

            try (PreparedStatement ps = con.prepareStatement(sql)) {
            // Qué hace: Prepara la sentencia SQL global.
            // Para qué sirve / Destino: Configura la consulta contra la base de datos MySQL.

                // Ejecutar la consulta SELECT global sin parámetros de filtro restrictivo.
                try (ResultSet rs = ps.executeQuery()) {
                // Qué hace: Ejecuta la consulta y obtiene el conjunto de resultados global.
                // Para qué sirve / Destino: Recibe la lista tabular completa enviada por la base de datos.

                    // Recorrer el ResultSet de filas devueltas por MySQL.
                    while (rs.next()) {
                    // Qué hace: Avanza iterativamente por cada fila del resultado.
                    // Para qué sirve / Destino: Permite procesar cada registro global devuelto por MySQL.

                        // Crear instancia para el mapeo de propiedades del pedido.
                        Historial h = new Historial();
                        // Qué hace: Crea un nuevo objeto de la entidad Historial en memoria.
                        // Para qué sirve / Destino: Permite transformar los registros planos en objetos orientados a objetos.

                        h.setIdPedido(rs.getString("id_pedido"));
                        h.setIdUsuario(rs.getString("id_usuario"));
                        h.setFecha(rs.getString("fecha_pedido"));
                        h.setTotal(rs.getDouble("total"));
                        h.setEstado(rs.getString("estado"));
                        h.setTipoEntrega(rs.getString("tipo_entrega"));
                        h.setEstadoPago(rs.getString("estado_pago") != null ? rs.getString("estado_pago") : "Sin pagar");
                        // Qué hace: Extrae y asigna las propiedades básicas del pedido desde el resultado actual de MySQL hacia el objeto Historial.
                        // Para qué sirve / Destino: Llena los atributos del modelo de datos en Java.

                        // Recuperar el valor de la columna 'customer_name' (nombre opcional del cliente).
                        String clientName = rs.getString("customer_name");
                        // Qué hace: Obtiene el nombre opcional de la cabecera del pedido.
                        // Para qué sirve / Destino: Almacena temporalmente el nombre descriptivo del cliente.

                        // Validar si el nombre opcional es nulo para recurrir al nombre del usuario en el JOIN.
                        if (clientName == null || clientName.trim().isEmpty()) {
                            clientName = rs.getString("user_name");
                            // Qué hace: Si el nombre opcional está vacío, toma el nombre obtenido mediante el JOIN con la tabla usuario.
                            // Para qué sirve / Destino: Respalda la obtención del nombre del cliente desde la relación de tablas en MySQL.
                        }

                        // Asignar el nombre amigable resultante o en su defecto "Cliente Anónimo".
                        h.setCustomerName(clientName != null ? clientName : "Cliente Anónimo");
                        // Qué hace: Asigna el nombre final del cliente a la entidad, usando una alternativa por defecto si es necesario.
                        // Para qué sirve / Destino: Garantiza la visualización de un nombre válido en la interfaz administrativa.

                        // Añadir a la lista de pedidos generales.
                        lista.add(h);
                        // Qué hace: Agrega el objeto Historial completo a la lista global.
                        // Para qué sirve / Destino: Acumula los registros para retornarlos al controlador.
                    }
                }
            }
        } catch (SQLException e) {
        // Qué hace: Captura excepciones SQL ocurridas durante la consulta global.
        // Para qué sirve / Destino: Maneja errores relacionales al recuperar todo el historial.

            // Capturar y registrar la excepción SQL.
            System.err.println("ERROR SQL AL OBTENER HISTORIAL GENERAL: " + e.getMessage());
            e.printStackTrace();
            // Qué hace: Escribe el error y la traza en la consola de depuración estándar.
            // Para qué sirve / Destino: Facilita el diagnóstico técnico de fallos en Java.
        }

        // Retornar la lista al componente del Servlet controlador.
        return lista;
        // Qué hace: Retorna la lista global de pedidos hacia el controlador o servlet administrativo.
        // Para qué sirve / Destino: Entrega los datos consolidados para su renderizado en las vistas del sistema.
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
    // Qué hace: Define el método público para registrar eventos de auditoría en la bitácora de pedidos.
    // Para qué sirve / Destino: Permite llevar un control de movimientos (como cancelaciones o finalizaciones) comunicándose con MySQL.

        String queryMaxId = "SELECT id_historialpedido FROM HistorialPedidos ORDER BY CAST(SUBSTRING(id_historialpedido, 5) AS UNSIGNED) DESC LIMIT 1";
        // Qué hace: Define la consulta SQL para obtener el ID de auditoría más alto actual.
        // Para qué sirve / Destino: Prepara la instrucción para consultar la tabla HistorialPedidos en MySQL.

        String nextId = "HIS-001";
        // Qué hace: Inicializa una cadena con el ID por defecto para el primer registro de historial.
        // Para qué sirve / Destino: Establece un valor base en la lógica de Java.

        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
        // Qué hace: Abre la conexión y ejecuta la consulta para obtener el ID máximo de historial.
        // Para qué sirve / Destino: Se comunica con la base de datos MySQL para mantener la secuencia de claves primarias.

            if (rsMax.next()) {
            // Qué hace: Posiciona el cursor en el resultado obtenido.
            // Para qué sirve / Destino: Valida si existen registros previos en la bitácora.

                String maxId = rsMax.getString("id_historialpedido");
                // Qué hace: Extrae el texto del ID máximo de historial.
                // Para qué sirve / Destino: Obtiene la clave primaria actual desde la base de datos.

                if (maxId != null && maxId.startsWith("HIS-")) {
                // Qué hace: Comprueba que el formato del ID comience con el prefijo correcto.
                // Para qué sirve / Destino: Asegura la integridad de los datos almacenados.

                    try {
                    // Qué hace: Inicia un bloque protegido para conversión numérica.
                    // Para qué sirve / Destino: Previene errores de parseo de texto.

                        int num = Integer.parseInt(maxId.substring(4));
                        // Qué hace: Extrae y convierte la parte numérica del ID de historial.
                        // Para qué sirve / Destino: Obtiene el valor entero para el cálculo correlativo.

                        nextId = String.format("HIS-%03d", num + 1);
                        // Qué hace: Formatea el siguiente ID secuencial de historial sumando uno.
                        // Para qué sirve / Destino: Genera el nuevo identificador único para la auditoría.

                    } catch (NumberFormatException e) {
                    // Qué hace: Captura errores de conversión numérica.
                    // Para qué sirve / Destino: Muestra advertencias en la consola de errores estándar.

                        System.err.println("Error parseando ID de historial máximo: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
        // Qué hace: Captura excepciones SQL durante la obtención del ID máximo de historial.
        // Para qué sirve / Destino: Maneja errores de base de datos al consultar la secuencia.

            System.err.println("Error obteniendo el ID máximo de historial en HistorialDAO: " + e.getMessage());
        }

        String sql = "INSERT INTO HistorialPedidos (id_historialpedido, id_pedido, id_usuario, fecha_movimiento, estado, descripcion) VALUES (?, ?, ?, NOW(), ?, ?)";
        // Qué hace: Define la sentencia SQL parametrizada para insertar un nuevo evento en la tabla de auditoría.
        // Para qué sirve / Destino: Prepara la operación de escritura en la tabla HistorialPedidos de MySQL.

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
        // Qué hace: Obtiene una conexión JDBC y prepara la sentencia SQL de inserción de forma segura.
        // Para qué sirve / Destino: Establece comunicación con la base de datos MySQL.

            ps.setString(1, nextId);
            ps.setString(2, idPedido);
            ps.setString(3, idUsuario);
            ps.setString(4, estado);
            ps.setString(5, descripcion);
            // Qué hace: Asigna los parámetros correspondientes (ID de historial, ID de pedido, ID de usuario, estado y descripción) a la sentencia preparada.
            // Para qué sirve / Destino: Configura los valores exactos para guardarlos en la tabla HistorialPedidos de MySQL.

            return ps.executeUpdate() > 0;
            // Qué hace: Ejecuta la inserción y retorna true si se vio afectada al menos una fila.
            // Para qué sirve / Destino: Confirma el éxito de la operación de auditoría hacia los controladores.

        } catch (SQLException e) {
        // Qué hace: Captura excepciones SQL ocurridas durante el registro del movimiento.
        // Para qué sirve / Destino: Maneja errores relacionales al escribir en la bitácora.

            System.err.println("ERROR SQL AL REGISTRAR EN HISTORIALPEDIDOS: " + e.getMessage());
            e.printStackTrace();
            // Qué hace: Registra el error y la traza en la consola estándar de errores.
            // Para qué sirve / Destino: Facilita la depuración de fallos de auditoría en Java.
        }

        return false;
        // Qué hace: Retorna false si ocurrió algún error en el proceso.
        // Para qué sirve / Destino: Informa al controlador que el registro del movimiento de historial falló.
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
    // Qué hace: Define un método privado para normalizar texto de tipos de entrega.
    // Para qué sirve / Destino: Protege la integridad de los datos garantizando compatibilidad con el ENUM de MySQL.

        if (rawTipo == null || rawTipo.trim().isEmpty()) {
            return "Para consumir aquí";
            // Qué hace: Retorna un valor por defecto si el parámetro es nulo o vacío.
            // Para qué sirve / Destino: Asegura que siempre se asigne una opción válida para la base de datos.
        }

        String normalized = rawTipo.trim().toLowerCase();
        // Qué hace: Limpia los espacios y convierte la cadena a minúsculas.
        // Para qué sirve / Destino: Facilita la evaluación de texto sin sensibilidad a mayúsculas.

        if (normalized.contains("domicilio")) {
            return "A domicilio";
            // Qué hace: Retorna el valor exacto para entregas a domicilio.
            // Para qué sirve / Destino: Homologa el texto con el ENUM definido en MySQL.
        } else if (normalized.contains("recoger") || normalized.contains("llevar")) {
            return "Para recoger";
            // Qué hace: Retorna el valor exacto para pedidos de recogida.
            // Para qué sirve / Destino: Homologa el texto con el ENUM definido en MySQL.
        } else if (normalized.contains("aqui") || normalized.contains("aquí") || normalized.contains("consumir") || normalized.contains("local") || normalized.contains("mesa")) {
            return "Para consumir aquí";
            // Qué hace: Retorna el valor exacto para consumo en el local.
            // Para qué sirve / Destino: Homologa el texto con el ENUM definido en MySQL.
        }

        return "Para consumir aquí";
        // Qué hace: Retorna un valor por defecto si no se encuentra ninguna coincidencia clara.
        // Para qué sirve / Destino: Evita excepciones o inserciones inválidas en la base de datos MySQL.
    }

    /**
     * Sanitiza y valida el estado del pedido para garantizar compatibilidad con ENUM de MySQL:
     * ('En preparación', 'Listo', 'En espera', 'Entregado').
     *
     * @param rawEstado Cadena enviada desde el sistema.
     * @return Valor sanitizado compatible con ENUM de MySQL.
     */
    private String sanitizarEstadoPedido(String rawEstado) {
    // Qué hace: Define un método privado para normalizar el estado del pedido.
    // Para qué sirve / Destino: Asegura que los estados lógicos cumplan con las restricciones del ENUM en MySQL.

        if (rawEstado == null || rawEstado.trim().isEmpty()) {
            return "En preparación";
            // Qué hace: Retorna un estado por defecto si el parámetro recibido es nulo o vacío.
            // Para qué sirve / Destino: Garantiza la consistencia inicial del estado de la orden en la base de datos.
        }

        String norm = rawEstado.trim().toLowerCase();
        // Qué hace: Limpia los espacios y pasa el texto a minúsculas.
        // Para qué sirve / Destino: Estandariza la cadena para su correcta evaluación lógica.

        if (norm.contains("preparac") || norm.contains("proceso")) {
            return "En preparación";
            // Qué hace: Retorna el estado exacto de preparación.
            // Para qué sirve / Destino: Homologa el texto con el ENUM de la tabla pedido en MySQL.
        } else if (norm.contains("listo")) {
            return "Listo";
            // Qué hace: Retorna el estado exacto de listo.
            // Para qué sirve / Destino: Homologa el texto con el ENUM de la tabla pedido en MySQL.
        } else if (norm.contains("espera") || norm.contains("pendiente")) {
            return "En espera";
            // Qué hace: Retorna el estado exacto de en espera.
            // Para qué sirve / Destino: Homologa el texto con el ENUM de la tabla pedido en MySQL.
        } else if (norm.contains("entregado") || norm.contains("completado")) {
            return "Entregado";
            // Qué hace: Retorna el estado exacto de entregado.
            // Para qué sirve / Destino: Homologa el texto con el ENUM de la tabla pedido en MySQL.
        }

        return "En preparación";
        // Qué hace: Retorna un estado por defecto si ninguna condición previa coincide.
        // Para qué sirve / Destino: Previene inserciones con valores no permitidos en la base de datos MySQL.
    }

    /**
     * Sanitiza el estado de pago del pedido ('Pagado' o 'Sin pagar').
     * Si no se especifica, 'Para consumir aquí' es por defecto 'Sin pagar',
     * mientras que 'Para recoger' o 'A domicilio' es por defecto 'Pagado'.
     */
    public String sanitizarEstadoPago(String rawEstadoPago, String tipoEntrega) {
    // Qué hace: Define un método público para normalizar y determinar el estado financiero de pago.
    // Para qué sirve / Destino: Aplica reglas de negocio sobre el pago antes de persistir la información en MySQL.

        if (rawEstadoPago != null && !rawEstadoPago.trim().isEmpty()) {
        // Qué hace: Verifica si se especificó explícitamente un estado de pago.
        // Para qué sirve / Destino: Controla el flujo para evaluar la cadena proporcionada por el sistema.

            String norm = rawEstadoPago.trim().toLowerCase();
            // Qué hace: Estandariza el texto convirtiéndolo a minúsculas sin espacios extra.
            // Para qué sirve / Destino: Facilita la evaluación lógica del estado financiero.

            if (norm.contains("pagado") || norm.contains("pago") || norm.contains("si") || norm.contains("sí")) {
                return "Pagado";
                // Qué hace: Retorna el valor exacto de pagado.
                // Para qué sirve / Destino: Homologa el texto con la columna estado_pago en MySQL.
            }
            if (norm.contains("sin") || norm.contains("no") || norm.contains("pend") || norm.contains("debe")) {
                return "Sin pagar";
                // Qué hace: Retorna el valor exacto de sin pagar.
                // Para qué sirve / Destino: Homologa el texto con la columna estado_pago en MySQL.
            }
        }

        String sanitizedTipo = sanitizarTipoEntrega(tipoEntrega);
        // Qué hace: Obtiene el tipo de entrega ya sanitizado.
        // Para qué sirve / Destino: Utiliza el contexto de entrega para aplicar reglas de pago por defecto.

        if ("Para consumir aquí".equalsIgnoreCase(sanitizedTipo)) {
            return "Sin pagar";
            // Qué hace: Define por defecto que los pedidos para consumir en local se pagan al finalizar.
            // Para qué sirve / Destino: Aplica la regla de negocio comercial predeterminada para el DAO.
        }

        return "Pagado";
        // Qué hace: Define por defecto que los pedidos a domicilio o para recoger ya vienen pagados.
        // Para qué sirve / Destino: Aplica la regla comercial predeterminada para entregas externas.
    }
}