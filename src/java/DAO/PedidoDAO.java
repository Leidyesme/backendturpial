package DAO; // Define el paquete al que pertenece la clase (capa de Acceso a Datos).

// Importar la clase de conexión para conectarse a la base de datos MySQL
import Modelo.Config.Conexion; // Destino: Se comunica con la clase de configuración para obtener conexiones activas a MySQL.
// Importar la entidad Pedido para mapear registros relacionales a objetos Java
import Modelo.Entidades.Pedido; // Destino: Se comunica con la entidad Pedido para estructurar los datos del negocio.

// Importar interfaces necesarias de JDBC para interactuar con la base de datos relacional
import java.sql.Connection; // Destino: Interfaz de Java JDBC para gestionar conexiones físicas a MySQL.
import java.sql.PreparedStatement; // Destino: Interfaz para ejecutar consultas SQL parametrizadas de forma segura.
import java.sql.ResultSet; // Destino: Interfaz para almacenar y recorrer los resultados devueltos por MySQL.
import java.sql.SQLException; // Destino: Clase para manejar excepciones y errores provenientes de la base de datos.

// Importar clases de colecciones estándar de Java para almacenar las listas de registros
import java.util.ArrayList; // Destino: Estructura de datos utilizada para almacenar listas dinámicas de objetos Pedido.
import java.util.List; // Destino: Interfaz List que define el tipo de retorno para los métodos de listado.
import org.json.JSONArray; // Destino: Librería externa para manipular arreglos estructurados en formato JSON (hacia controladores/frontend).
import org.json.JSONObject; // Destino: Librería externa para manipular objetos estructurados en formato JSON (hacia controladores/frontend).


/**
 * Clase de Acceso a Datos (DAO) para la entidad Pedido.
 * Encargada de realizar operaciones CRUD en la tabla 'pedido' de MySQL.
 */
public class PedidoDAO { // Declara la clase pública PedidoDAO que encapsula la lógica de persistencia de los pedidos.

    /**
     * Constructor por defecto.
     * Se ha removido la ejecución de sentencias DDL (ALTER TABLE) para evitar fallos de
     * seguridad y permisos denegados cuando se usan roles de base de datos restringidos.
     */
    public PedidoDAO() { // Método constructor que se ejecuta al instanciar la clase.
        try (Connection con = Conexion.getConnection(); // Qué hace: Obtiene una conexión activa a MySQL. Destino: Capa de configuración / Base de datos.
             java.sql.Statement stmt = con.createStatement()) { // Qué hace: Crea una sentencia SQL genérica. Destino: Base de datos MySQL.
            stmt.executeUpdate("ALTER TABLE pedido ADD COLUMN estado_pago VARCHAR(20) DEFAULT 'Sin pagar'"); // Qué hace: Intenta alterar la tabla añadiendo la columna de pago. Destino: Estructura de la tabla 'pedido' en MySQL.
        } catch (SQLException e) { // Qué hace: Captura errores SQL si la columna ya existe o faltan permisos. Destino: Manejo interno de excepciones.
            // Ignorar si la columna ya existe o falla por permisos
        }
    }

    /**
     * Recupera y lista todos los pedidos registrados en la tabla 'pedido'.
     *
     * @return Una lista de objetos de tipo Pedido.
     */
    public List<Pedido> listar() { // Declara el método público listar que retorna una lista de entidades Pedido hacia los controladores.
        List<Pedido> lista = new ArrayList<>(); // Qué hace: Instancia una lista vacía para almacenar los pedidos recuperados. Destino: Memoria local del método.
        
        String sql = "SELECT p.*, u.name AS user_name FROM pedido p " // Qué hace: Define la consulta SQL con un LEFT JOIN hacia la tabla usuario. Destino: Base de datos MySQL.
                    + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario"; // Qué hace: Completa la sentencia para asociar datos del usuario al pedido. Destino: Base de datos MySQL.

        try (
            Connection con = Conexion.getConnection(); // Qué hace: Obtiene la conexión a la base de datos. Destino: Clase Conexion.
            PreparedStatement ps = con.prepareStatement(sql); // Qué hace: Prepara la consulta SQL para su ejecución segura. Destino: Base de datos MySQL.
            ResultSet rs = ps.executeQuery() // Qué hace: Ejecuta el SELECT y almacena los resultados. Destino: Tabla 'pedido' y 'usuario' en MySQL.
        ) {
            while (rs.next()) { // Qué hace: Itera sobre cada registro devuelto por la base de datos. Destino: Bucle de procesamiento de filas.
                Pedido p = new Pedido(); // Qué hace: Crea una nueva instancia de la entidad Pedido por cada fila. Destino: Capa de entidades.

                p.setIdPedido(safeString(rs, "id_pedido", "PED-000")); // Qué hace: Asigna el ID del pedido mapeándolo de forma segura. Destino: Objeto Pedido.
                p.setIdUsuario(safeString(rs, "id_usuario", null)); // Qué hace: Asigna el ID del usuario asociado de forma segura. Destino: Objeto Pedido.

                String clientName = safeString(rs, "customer_name", null); // Qué hace: Obtiene el nombre del cliente opcional de la tabla pedido. Destino: Registro temporal.
                if (clientName == null) { // Qué hace: Evalúa si el nombre del cliente es nulo. Destino: Estructura de control condicional.
                    clientName = safeString(rs, "user_name", "Cliente Anónimo"); // Qué hace: Asigna el nombre del usuario o un valor por defecto. Destino: Variable local.
                }
                p.setNombreClienteOpcional(clientName); // Qué hace: Establece el nombre final del cliente en la entidad. Destino: Objeto Pedido.
                p.setTipoEntrega(safeString(rs, "tipo_entrega", "Para consumir aquí")); // Qué hace: Establece el tipo de entrega del pedido. Destino: Objeto Pedido.
                p.setNumeroMesa(safeInt(rs, "numero_mesa")); // Qué hace: Establece el número de mesa asociado de forma segura. Destino: Objeto Pedido.
                p.setDireccionEntrega(safeString(rs, "direccion_entrega", "No especificada")); // Qué hace: Establece la dirección de entrega. Destino: Objeto Pedido.
                p.setObservaciones(safeString(rs, "observaciones", "")); // Qué hace: Establece las observaciones del pedido. Destino: Objeto Pedido.
                p.setTotal(safeDouble(rs, "total")); // Qué hace: Establece el monto total del pedido. Destino: Objeto Pedido.
                p.setEstado(safeString(rs, "estado", "En preparación")); // Qué hace: Establece el estado actual del pedido. Destino: Objeto Pedido.
                p.setFechaPedido(safeString(rs, "fecha_pedido", "")); // Qué hace: Establece la fecha en que se realizó el pedido. Destino: Objeto Pedido.
                p.setEstadoPago(safeString(rs, "estado_pago", "Sin pagar")); // Qué hace: Establece el estado de pago del pedido. Destino: Objeto Pedido.

                lista.add(p); // Qué hace: Agrega el objeto Pedido completo a la lista. Destino: Variable lista.
            }

        } catch (SQLException e) { // Qué hace: Captura errores relacionados con la ejecución del SQL de listado. Destino: Bloque de excepción.
            System.err.println("ERROR SQL AL LISTAR PEDIDOS: " + e.getMessage()); // Qué hace: Imprime el mensaje de error en la consola de sistema. Destino: Consola de errores.
            e.printStackTrace(); // Qué hace: Muestra el rastro completo de la pila de errores. Destino: Consola de errores.
        }

        return lista; // Qué hace: Retorna la lista completa de pedidos procesados hacia el llamador (controlador). Destino: Capa de control / Servlets.
    }

    private String safeString(ResultSet rs, String col, String def) { // Método auxiliar privado para extraer strings de forma segura sin excepciones por nulos.
        try {
            String v = rs.getString(col); // Qué hace: Extrae el valor de la columna como String. Destino: ResultSet de JDBC.
            return (v != null && !v.trim().isEmpty() && !v.equalsIgnoreCase("null")) ? v : def; // Qué hace: Valida que el texto sea válido, sino retorna el valor por defecto. Destino: Retorno del método.
        } catch (Exception e) {
            return def; // Qué hace: Retorna el valor por defecto si ocurre cualquier error al leer la columna. Destino: Retorno del método.
        }
    }

    private Integer safeInt(ResultSet rs, String col) { // Método auxiliar privado para extraer enteros de forma segura.
        try {
            int v = rs.getInt(col); // Qué hace: Extrae el valor entero de la columna. Destino: ResultSet de JDBC.
            return rs.wasNull() ? null : v; // Qué hace: Retorna null si el valor en la base de datos era nulo, de lo contrario el entero. Destino: Retorno del método.
        } catch (Exception e) {
            return null; // Qué hace: Retorna null en caso de excepción. Destino: Retorno del método.
        }
    }

    private double safeDouble(ResultSet rs, String col) { // Método auxiliar privado para extraer valores decimales de forma segura.
        try {
            return rs.getDouble(col); // Qué hace: Extrae el valor de tipo double de la columna. Destino: ResultSet de JDBC.
        } catch (Exception e) {
            return 0.0; // Qué hace: Retorna 0.0 por defecto si ocurre un error. Destino: Retorno del método.
        }
    }

    /**
     * Registra un nuevo pedido en la base de datos generando automáticamente
     * un identificador incremental con formato 'PED-XXX'.
     *
     * @param p Objeto Pedido con la información a insertar.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public boolean registrar(Pedido p, JSONArray productos) { // Método público para registrar un pedido y sus detalles transaccionalmente.
        // SQL: busca el ID más alto, lo convierte a número después de 'PED-' para ordenarlos numéricamente.
        String queryMaxId = "SELECT id_pedido FROM pedido ORDER BY CAST(SUBSTRING(id_pedido, 5) AS UNSIGNED) DESC LIMIT 1"; // Qué hace: Define la consulta SQL para hallar el último ID de pedido. Destino: Base de datos MySQL.
        
        // Identificador por defecto para cuando no existen pedidos previos.
        String nextId = "PED-001"; // Qué hace: Inicializa el ID por defecto. Destino: Variable local.

        // Bloque para calcular automáticamente el siguiente número correlativo de ID.
        try (
            Connection con = Conexion.getConnection(); // Qué hace: Obtiene la conexión a la base de datos. Destino: Clase Conexion.
            PreparedStatement psMax = con.prepareStatement(queryMaxId); // Qué hace: Prepara la consulta para buscar el ID máximo. Destino: Base de datos MySQL.
            ResultSet rsMax = psMax.executeQuery() // Qué hace: Ejecuta la consulta del ID máximo. Destino: Base de datos MySQL.
        ) {
            if (rsMax.next()) { // Qué hace: Verifica si existe un resultado previo. Destino: Bloque condicional.
                String maxId = rsMax.getString("id_pedido"); // Qué hace: Extrae el ID máximo actual. Destino: Variable local.
                // Valida formato y extrae el número para sumarle 1.
                if (maxId != null && maxId.startsWith("PED-")) { // Qué hace: Valida el prefijo del ID. Destino: Condicional de formato.
                    int num = Integer.parseInt(maxId.substring(4)); // Qué hace: Convierte la parte numérica del ID a entero. Destino: Variable local.
                    nextId = String.format("PED-%03d", num + 1); // Qué hace: Formatea el nuevo ID consecutivo (ej. PED-002). Destino: Variable nextId.
                }
            }
        } catch (SQLException e) { // Qué hace: Captura errores al calcular el ID máximo. Destino: Excepción SQL.
            System.err.println("Advertencia obteniendo ID máximo de pedido (se usará PED-001): " + e.getMessage()); // Qué hace: Imprime advertencia en consola. Destino: Consola de errores.
        }

        // Asigna el nuevo ID generado al objeto pedido antes de insertar.
        p.setIdPedido(nextId); // Qué hace: Inyecta el ID generado en la entidad Pedido. Destino: Objeto Pedido.

        // Bloque similar para obtener el siguiente ID de la tabla 'detallepedido'.
        String queryMaxDetalleId = "SELECT id_detallepedido FROM detallepedido ORDER BY CAST(SUBSTRING(id_detallepedido, 5) AS UNSIGNED) DESC LIMIT 1"; // Qué hace: Define la consulta SQL para el último ID de detalle. Destino: Base de datos MySQL.
        int lastDetalleNum = 0; // Qué hace: Inicializa el contador del detalle en 0. Destino: Variable local.
        try (
            Connection con = Conexion.getConnection(); // Qué hace: Abre conexión a la BD. Destino: Clase Conexion.
            PreparedStatement psMaxD = con.prepareStatement(queryMaxDetalleId); // Qué hace: Prepara la sentencia para el ID de detalle máximo. Destino: Base de datos MySQL.
            ResultSet rsMaxD = psMaxD.executeQuery() // Qué hace: Ejecuta la consulta de detalle máximo. Destino: Base de datos MySQL.
        ) {
            if (rsMaxD.next()) { // Qué hace: Evalúa si hay registros previos de detalles. Destino: Condicional.
                String maxDetId = rsMaxD.getString("id_detallepedido"); // Qué hace: Obtiene el ID del detalle máximo. Destino: Variable local.
                if (maxDetId != null && maxDetId.startsWith("DET-")) { // Qué hace: Valida el formato del prefijo DET-. Destino: Condicional.
                    lastDetalleNum = Integer.parseInt(maxDetId.substring(4)); // Qué hace: Extrae el número base del detalle. Destino: Variable lastDetalleNum.
                }
            }
        } catch (SQLException e) { // Qué hace: Captura errores al obtener el ID máximo del detalle. Destino: Excepción SQL.
            System.err.println("Advertencia obteniendo ID máximo de detalle: " + e.getMessage()); // Qué hace: Imprime advertencia en consola. Destino: Consola de errores.
        }

        // Define las sentencias SQL de inserción para la cabecera y el detalle del pedido.
        String sql = "INSERT INTO pedido " // Qué hace: Define la sentencia SQL para insertar la cabecera del pedido. Destino: Base de datos MySQL.
                   + "(id_pedido, id_usuario, customer_name, tipo_entrega, numero_mesa, direccion_entrega, observaciones, total, estado, fecha_pedido) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        String sqlDetalle = "INSERT INTO detallepedido (id_detallepedido, id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)"; // Qué hace: Define la sentencia SQL para insertar los productos del detalle. Destino: Base de datos MySQL.

        // Inicio de la transacción: crucial para asegurar que el pedido y sus detalles se guarden como una unidad.
        try (Connection con = Conexion.getConnection()) { // Qué hace: Obtiene la conexión principal para la transacción. Destino: Clase Conexion.
            con.setAutoCommit(false); // Desactiva el autoguardado (transaccionalidad manual). Qué hace: Configura JDBC para control manual de transacciones. Destino: Conexión JDBC.

            try (PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Prepara la sentencia de inserción de la cabecera. Destino: Base de datos MySQL.
                // Mapea los valores del objeto Pedido a la consulta principal.
                ps.setString(1, p.getIdPedido()); // Qué hace: Asigna el ID del pedido al parámetro 1. Destino: PreparedStatement.
                ps.setString(2, p.getIdUsuario()); // Qué hace: Asigna el ID del usuario al parámetro 2. Destino: PreparedStatement.
                ps.setString(3, p.getNombreClienteOpcional()); // Qué hace: Asigna el nombre del cliente opcional al parámetro 3. Destino: PreparedStatement.
                ps.setString(4, p.getTipoEntrega()); // Qué hace: Asigna el tipo de entrega al parámetro 4. Destino: PreparedStatement.

                // Manejo de campo opcional 'numero_mesa'.
                if (p.getNumeroMesa() != null) { // Qué hace: Comprueba si se especificó un número de mesa. Destino: Estructura condicional.
                    ps.setInt(5, p.getNumeroMesa()); // Qué hace: Asigna el número de mesa como entero. Destino: PreparedStatement.
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER); // Qué hace: Envía un valor SQL NULL si no hay mesa. Destino: PreparedStatement.
                }

                ps.setString(6, p.getDireccionEntrega()); // Qué hace: Asigna la dirección de entrega al parámetro 6. Destino: PreparedStatement.
                ps.setString(7, p.getObservaciones()); // Qué hace: Asigna las observaciones al parámetro 7. Destino: PreparedStatement.
                ps.setDouble(8, p.getTotal()); // Qué hace: Asigna el total monetario al parámetro 8. Destino: PreparedStatement.
                ps.setString(9, p.getEstado()); // Qué hace: Asigna el estado inicial del pedido al parámetro 9. Destino: PreparedStatement.

                // Ejecuta la inserción de la cabecera.
                int affectedRows = ps.executeUpdate(); // Qué hace: Ejecuta el INSERT principal y obtiene filas afectadas. Destino: Tabla 'pedido' en MySQL.
                if (affectedRows == 0) { // Qué hace: Verifica si la inserción falló (0 filas afectadas). Destino: Estructura condicional.
                    con.rollback(); // Si falla, revierte todo. Qué hace: Revierte los cambios de la transacción actual. Destino: Conexión JDBC.
                    return false; // Qué hace: Retorna falso indicando fallo. Destino: Llamador del método.
                }

                // Inserción de los productos (detalles) en un lote (batch) para mayor rendimiento.
                if (productos != null && productos.length() > 0) { // Qué hace: Verifica que existan productos en el arreglo JSON. Destino: Estructura condicional.
                    try (PreparedStatement psD = con.prepareStatement(sqlDetalle)) { // Qué hace: Prepara la sentencia SQL de inserción de detalles. Destino: Base de datos MySQL.
                        for (int i = 0; i < productos.length(); i++) { // Qué hace: Itera sobre cada producto del arreglo JSON. Destino: Bucle for.
                            JSONObject prod = productos.getJSONObject(i); // Qué hace: Extrae el objeto JSON del producto actual. Destino: Variable local.
                            lastDetalleNum++; // Qué hace: Incrementa el correlativo numérico del detalle. Destino: Variable lastDetalleNum.
                            String nextDetId = String.format("DET-%03d", lastDetalleNum); // Qué hace: Genera el ID formateado del detalle (ej. DET-001). Destino: Variable local.

                            psD.setString(1, nextDetId); // Qué hace: Asigna el ID del detalle al parámetro 1. Destino: PreparedStatement del detalle.
                            psD.setString(2, p.getIdPedido()); // Qué hace: Asigna el ID del pedido asociado al parámetro 2. Destino: PreparedStatement del detalle.
                            psD.setString(3, prod.getString("idProducto")); // Qué hace: Asigna el ID del producto al parámetro 3. Destino: PreparedStatement del detalle.
                            psD.setInt(4, prod.getInt("quantity")); // Qué hace: Asigna la cantidad comprada al parámetro 4. Destino: PreparedStatement del detalle.
                            psD.setDouble(5, prod.getDouble("price")); // Qué hace: Asigna el precio unitario al parámetro 5. Destino: PreparedStatement del detalle.
                            psD.addBatch(); // Acumula las sentencias. Qué hace: Agrega la sentencia actual al lote de ejecución masiva. Destino: PreparedStatement batch.
                        }
                        psD.executeBatch(); // Ejecuta todas las inserciones a la vez. Qué hace: Ejecuta todas las consultas acumuladas en lote de golpe. Destino: Tabla 'detallepedido' en MySQL.
                    }
                }

                con.commit(); // Si todo es correcto, confirma (graba) los cambios. Qué hace: Confirma permanentemente la transacción en la base de datos. Destino: Base de datos MySQL.
                return true; // Qué hace: Retorna true indicando que el registro completo fue exitoso. Destino: Controlador / Llamador.
            } catch (SQLException e) {
                con.rollback(); // Si ocurre un error, deshace cualquier cambio previo. Qué hace: Revierte la transacción ante cualquier fallo interno. Destino: Base de datos MySQL.
                throw e; // Qué hace: Relanza la excepción hacia el catch exterior. Destino: Bloque catch superior.
            } finally {
                con.setAutoCommit(true); // Restaura el modo de guardado automático. Qué hace: Restablece el autocommit por defecto de la conexión. Destino: Conexión JDBC.
            }
        } catch (SQLException e) { // Qué hace: Captura errores globales de SQL en el proceso de registro. Destino: Manejo de errores.
            System.err.println("ERROR SQL AL REGISTRAR PEDIDO: " + e.getMessage()); // Qué hace: Imprime el error por consola. Destino: Consola de errores.
            e.printStackTrace(); // Qué hace: Muestra el rastro de la traza del error. Destino: Consola de errores.
        }

        return false; // Error general. Qué hace: Retorna falso si ocurrió alguna excepción general. Destino: Llamador del método.
    }

    /**
     * Actualiza el estado de un pedido en la base de datos MySQL.
     *
     * @param idPedido Identificador del pedido.
     * @param nuevoEstado El nuevo estado (ej: 'Listo', 'En espera', 'Entregado').
     * @param idUsuario Identificador del usuario que realiza la acción.
     * @return true si la actualización afectó al menos una fila, false de lo contrario.
     */
    public boolean actualizarEstado(String idPedido, String nuevoEstado, String idUsuario) { // Método público para cambiar el estado de un pedido.
        // Actualiza solo la columna 'estado' donde coincida el ID.
        String sql = "UPDATE pedido SET estado = ? WHERE id_pedido = ?"; // Qué hace: Define la sentencia SQL para actualizar el estado del pedido. Destino: Base de datos MySQL.
        try (
            Connection con = Conexion.getConnection(); // Qué hace: Obtiene la conexión activa. Destino: Clase Conexion.
            PreparedStatement ps = con.prepareStatement(sql) // Qué hace: Prepara la sentencia SQL de actualización. Destino: Base de datos MySQL.
        ) {
            ps.setString(1, nuevoEstado); // Qué hace: Asigna el nuevo estado al parámetro 1. Destino: PreparedStatement.
            ps.setString(2, idPedido); // Qué hace: Asigna el ID del pedido al parámetro 2. Destino: PreparedStatement.
            
            boolean ok = ps.executeUpdate() > 0; // Qué hace: Ejecuta el UPDATE y verifica si afectó más de 0 filas. Destino: Tabla 'pedido' en MySQL.
            if (ok && "Entregado".equalsIgnoreCase(nuevoEstado)) { // Qué hace: Valida si se actualizó con éxito y el estado es "Entregado". Destino: Estructura condicional.
                // Registrar en la tabla HistorialPedidos como 'Finalizado'
                HistorialDAO historialDao = new HistorialDAO(); // Qué hace: Instancia el DAO del historial. Destino: Capa DAO (HistorialDAO).
                boolean histOk = historialDao.registrarMovimientoHistorial(idPedido, idUsuario, "Finalizado", "Pedido entregado correctamente"); // Qué hace: Registra el movimiento en el historial. Destino: Método registrarMovimientoHistorial en HistorialDAO.
                if (!histOk) { // Qué hace: Comprueba si el registro en el historial falló. Destino: Estructura condicional.
                    System.err.println("[WARN - PedidoDAO] No se pudo registrar el movimiento en HistorialPedidos para " + idPedido); // Qué hace: Imprime advertencia en consola. Destino: Consola de errores.
                }
            }
            return ok; // Qué hace: Retorna el resultado booleano de la actualización. Destino: Controlador llamador.
        } catch (SQLException e) { // Qué hace: Captura errores SQL durante la actualización del estado. Destino: Excepción SQL.
            System.err.println("ERROR SQL AL ACTUALIZAR ESTADO DE PEDIDO: " + e.getMessage()); // Qué hace: Imprime error en consola. Destino: Consola de errores.
            e.printStackTrace(); // Qué hace: Muestra la pila de errores. Destino: Consola de errores.
        }
        return false; // Qué hace: Retorna false si ocurre un error. Destino: Controlador llamador.
    }

    /**
     * Obtiene un pedido completo de la base de datos (cabecera y lista de productos de detalle).
     *
     * @param idPedido Identificador del pedido.
     * @return JSONObject conteniendo los datos consolidados del pedido.
     */
    public org.json.JSONObject obtenerPedidoConProductos(String idPedido) { // Método público para consolidar cabecera y detalles en un JSON para el frontend.
        org.json.JSONObject res = new org.json.JSONObject(); // Qué hace: Crea el objeto JSON principal de respuesta. Destino: Objeto JSON en memoria.
        
        // Obtener la cabecera del pedido mediante un LEFT JOIN para obtener el nombre del usuario.
        String sqlPedido = "SELECT p.*, u.name AS user_name FROM pedido p " // Qué hace: Define la consulta SQL para obtener la cabecera y el nombre de usuario asociado. Destino: Base de datos MySQL.
                          + "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario "
                          + "WHERE p.id_pedido = ?";
        
        try (
            Connection con = Conexion.getConnection(); // Qué hace: Obtiene la conexión a la base de datos. Destino: Clase Conexion.
            PreparedStatement ps = con.prepareStatement(sqlPedido) // Qué hace: Prepara la consulta SQL para la cabecera. Destino: Base de datos MySQL.
        ) {
            ps.setString(1, idPedido); // Qué hace: Asigna el ID del pedido al parámetro de búsqueda. Destino: PreparedStatement.
            try (ResultSet rs = ps.executeQuery()) { // Qué hace: Ejecuta la consulta de la cabecera. Destino: Base de datos MySQL.
                if (rs.next()) { // Qué hace: Verifica si se encontró el pedido. Destino: Estructura condicional.
                    // Carga todos los campos del pedido en un objeto JSON para enviar al frontend.
                    res.put("idPedido", rs.getString("id_pedido")); // Qué hace: Agrega el ID del pedido al JSON. Destino: Objeto JSON 'res'.
                    res.put("idUsuario", rs.getString("id_usuario") != null ? rs.getString("id_usuario") : ""); // Qué hace: Agrega el ID del usuario o cadena vacía. Destino: Objeto JSON 'res'.
                    
                    String clientName = rs.getString("customer_name"); // Qué hace: Extrae el nombre del cliente opcional. Destino: Variable local.
                    if (clientName == null || clientName.trim().isEmpty()) { // Qué hace: Valida si está vacío o nulo. Destino: Estructura condicional.
                        clientName = rs.getString("user_name"); // Qué hace: Toma el nombre del usuario registrado como alternativa. Destino: Variable local.
                    }
                    res.put("nombreClienteOpcional", clientName != null ? clientName : "Cliente Anónimo"); // Qué hace: Asigna el nombre final del cliente al JSON. Destino: Objeto JSON 'res'.
                    res.put("tipoEntrega", rs.getString("tipo_entrega")); // Qué hace: Agrega el tipo de entrega al JSON. Destino: Objeto JSON 'res'.
                    
                    int numeroMesa = rs.getInt("numero_mesa"); // Qué hace: Extrae el número de mesa. Destino: Variable local.
                    if (rs.wasNull()) { // Qué hace: Valida si el número de mesa era nulo en la BD. Destino: Estructura condicional.
                        res.put("numeroMesa", org.json.JSONObject.NULL); // Qué hace: Inserta un JSON nulo si no hay mesa. Destino: Objeto JSON 'res'.
                    } else {
                        res.put("numeroMesa", numeroMesa); // Qué hace: Inserta el número de mesa real. Destino: Objeto JSON 'res'.
                    }
                    
                    res.put("direccionEntrega", rs.getString("direccion_entrega") != null ? rs.getString("direccion_entrega") : ""); // Qué hace: Agrega la dirección de entrega al JSON. Destino: Objeto JSON 'res'.
                    res.put("observaciones", rs.getString("observaciones") != null ? rs.getString("observaciones") : ""); // Qué hace: Agrega las observaciones al JSON. Destino: Objeto JSON 'res'.
                    res.put("total", rs.getDouble("total")); // Qué hace: Agrega el total al JSON. Destino: Objeto JSON 'res'.
                    res.put("estado", rs.getString("estado")); // Qué hace: Agrega el estado del pedido al JSON. Destino: Objeto JSON 'res'.
                    res.put("fechaPedido", rs.getString("fecha_pedido")); // Qué hace: Agrega la fecha del pedido al JSON. Destino: Objeto JSON 'res'.

                    String estadoPago = "Sin pagar"; // Qué hace: Define un valor por defecto para el estado de pago. Destino: Variable local.
                    try {
                        estadoPago = rs.getString("estado_pago"); // Qué hace: Intenta extraer el estado de pago de la BD. Destino: ResultSet.
                        if (estadoPago == null) estadoPago = "Sin pagar"; // Qué hace: Evalúa si es nulo para asignar el valor por defecto. Destino: Variable local.
                    } catch (SQLException e) {
                        estadoPago = "Sin pagar"; // Qué hace: Asigna el valor por defecto si ocurre una excepción de columna. Destino: Variable local.
                    }
                    res.put("estadoPago", estadoPago); // Qué hace: Agrega el estado de pago al objeto JSON de respuesta. Destino: Objeto JSON 'res'.
                } else {
                    return null; // El pedido no existe. Qué hace: Retorna null si no se encontró el pedido en la BD. Destino: Controlador llamador.
                }
            }
        } catch (SQLException e) { // Qué hace: Captura errores SQL al buscar la cabecera. Destino: Excepción SQL.
            System.err.println("ERROR SQL AL OBTENER CABECERA EN PedidoDAO: " + e.getMessage()); // Qué hace: Imprime el error en consola. Destino: Consola de errores.
            e.printStackTrace(); // Qué hace: Muestra la traza del error. Destino: Consola de errores.
            return null; // Qué hace: Retorna null ante fallos. Destino: Controlador llamador.
        }

        // Obtener la lista de productos asociados al pedido usando un JOIN con 'producto'.
        String sqlDetalles = "SELECT dp.*, prod.nombre AS producto_nombre FROM detallepedido dp " // Qué hace: Define la consulta SQL para obtener los detalles junto al nombre del producto. Destino: Base de datos MySQL.
                           + "JOIN producto prod ON dp.id_producto = prod.id_producto "
                           + "WHERE dp.id_pedido = ?";
        
        org.json.JSONArray arrayProductos = new org.json.JSONArray(); // Qué hace: Crea un arreglo JSON para almacenar la lista de productos del detalle. Destino: Objeto JSONArray en memoria.
        // Ejecuta la consulta para los detalles asociados al idPedido.
        try (
            Connection con = Conexion.getConnection(); // Qué hace: Obtiene la conexión a la base de datos. Destino: Clase Conexion.
            PreparedStatement ps = con.prepareStatement(sqlDetalles) // Qué hace: Prepara la consulta para los detalles. Destino: Base de datos MySQL.
        ) {
            ps.setString(1, idPedido); // Qué hace: Asigna el ID del pedido al parámetro de la consulta de detalles. Destino: PreparedStatement.
            try (ResultSet rs = ps.executeQuery()) { // Qué hace: Ejecuta la consulta de detalles. Destino: Base de datos MySQL.
                while (rs.next()) { // Qué hace: Recorre cada fila de productos asociados al pedido. Destino: Bucle while.
                    // Crea un objeto para cada producto y lo agrega a la lista de productos.
                    org.json.JSONObject prod = new org.json.JSONObject(); // Qué hace: Instancia un objeto JSON por cada producto del detalle. Destino: Objeto JSON individual.
                    prod.put("name", rs.getString("producto_nombre")); // Qué hace: Agrega el nombre del producto al JSON. Destino: Objeto JSON del producto.
                    prod.put("quantity", rs.getInt("cantidad")); // Qué hace: Agrega la cantidad comprada al JSON. Destino: Objeto JSON del producto.
                    prod.put("price", rs.getDouble("precio_unitario")); // Qué hace: Agrega el precio unitario al JSON. Destino: Objeto JSON del producto.
                    arrayProductos.put(prod);  // Agrega cada producto al arreglo. Qué hace: Inserta el producto en el JSONArray. Destino: Variable arrayProductos.
                }
            }
        } catch (SQLException e) { // Qué hace: Captura errores SQL al consultar los detalles del pedido. Destino: Excepción SQL.
            System.err.println("ERROR SQL AL OBTENER DETALLES EN PedidoDAO: " + e.getMessage()); // Qué hace: Imprime el error en consola. Destino: Consola de errores.
            e.printStackTrace(); // Qué hace: Muestra la traza del error. Destino: Consola de errores.
        }
        // Inyecta el arreglo de productos dentro del objeto JSON principal y lo retorna.
        res.put("products", arrayProductos); // Qué hace: Inserta la lista de productos dentro de la respuesta global JSON. Destino: Objeto JSON 'res'.
        return res; // Qué hace: Retorna el objeto JSON consolidado con toda la información al controlador. Destino: Capa de control / Servlets.
    }

    /**
     * Actualiza únicamente el estado de pago del pedido ('Pagado' o 'Sin pagar').
     */
    public boolean actualizarEstadoPago(String idPedido, String nuevoEstadoPago) { // Método público para actualizar de forma exclusiva el campo de pago.
        String sql = "UPDATE pedido SET estado_pago = ? WHERE id_pedido = ?"; // Qué hace: Define la sentencia SQL para modificar 'estado_pago'. Destino: Base de datos MySQL.
        try (Connection con = Conexion.getConnection(); // Qué hace: Obtiene la conexión activa a la base de datos. Destino: Clase Conexion.
             PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Prepara la sentencia SQL de actualización de pago. Destino: Base de datos MySQL.
            ps.setString(1, nuevoEstadoPago); // Qué hace: Asigna el nuevo estado de pago al parámetro 1. Destino: PreparedStatement.
            ps.setString(2, idPedido); // Qué hace: Asigna el ID del pedido al parámetro 2. Destino: PreparedStatement.
            return ps.executeUpdate() > 0; // Qué hace: Ejecuta el UPDATE y retorna true si se actualizó al menos una fila. Destino: Base de datos MySQL / Llamador.
        } catch (SQLException e) { // Qué hace: Captura errores SQL específicos al modificar el pago. Destino: Excepción SQL.
            System.err.println("Error actualizando estado_pago en PedidoDAO: " + e.getMessage()); // Qué hace: Imprime el mensaje de error por consola. Destino: Consola de errores.
            return false; // Qué hace: Retorna false indicando que la operación falló. Destino: Llamador del método.
        }
    }
}