// Declarar el paquete DAO al que pertenece esta clase de persistencia
package DAO;
// Qué hace: Define la pertenencia de la clase al paquete DAO.
// Para qué sirve / Destino: Organiza las clases de acceso a datos para estructurar la comunicación con MySQL.

// Importar la clase Conexion para gestionar la conectividad JDBC
import Modelo.Config.Conexion;
// Qué hace: Importa la clase utilitaria de conexión.
// Para qué sirve / Destino: Permite obtener instancias activas de `Connection` para interactuar con la base de datos MySQL.

// Importar la entidad Producto para el mapeo de objetos de dominio
import Modelo.Entidades.Producto;
// Qué hace: Importa la clase del modelo Producto.
// Para qué sirve / Destino: Facilita la transferencia de datos estructurados entre la base de datos y los controladores o vistas.

// Importar clases nativas de JDBC para manejar la comunicación relacional
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
// Qué hace: Importa las interfaces para conexiones, sentencias preparadas y conjuntos de resultados.
// Para qué sirve / Destino: Administran el canal de comunicación, la ejecución segura de consultas SQL y el recorrido de datos en MySQL.

// Importar colecciones de Java para el manejo de listas dinámicas
import java.util.ArrayList;
import java.util.List;
// Qué hace: Importa las clases ArrayList y List.
// Para qué sirve / Destino: Permiten almacenar y retornar colecciones dinámicas de objetos Producto hacia la capa de control.

/**
 * Clase ProductoDAO encargada de la persistencia y operaciones CRUD
 * para los productos de la cafetería en la base de datos MySQL.
 */
public class ProductoDAO {
// Qué hace: Declara la clase pública ProductoDAO.
// Para qué sirve / Destino: Actúa como el componente DAO central para ejecutar operaciones sobre la tabla Producto en MySQL.

    /**
     * Constructor por defecto. Asegura la modificación de la columna imagen a LONGTEXT en la BD.
     */
    public ProductoDAO() {
    // Qué hace: Define el constructor de la clase.
    // Para qué sirve / Destino: Se ejecuta al instanciar el DAO para adaptar automáticamente la estructura de la tabla en MySQL si es necesario.

        try (Connection con = Conexion.getConnection();
             java.sql.Statement stmt = con.createStatement()) {
        // Qué hace: Obtiene una conexión y crea un objeto Statement mediante recursos.
        // Para qué sirve / Destino: Establece la comunicación inicial con la base de datos MySQL para tareas DDL.

            stmt.executeUpdate("ALTER TABLE Producto MODIFY COLUMN imagen LONGTEXT");
            // Qué hace: Ejecuta una instrucción SQL para modificar el tipo de datos de la columna imagen.
            // Para qué sirve / Destino: Modifica la estructura de la tabla Producto en MySQL para admitir imágenes en Base64 de gran tamaño.

        } catch (Exception e) {
        // Qué hace: Captura cualquier excepción generada durante la ejecución del DDL.
        // Para qué sirve / Destino: Previene interrupciones en la aplicación si la columna ya se encuentra modificada.

            // Ignorar si no se requiere o ya está modificado
        }
    }

    /**
     * Recupera el listado completo de productos registrados en el sistema.
     * @return Lista de objetos Producto.
     */
    public List<Producto> listar() {
    // Qué hace: Define el método público para consultar y listar todos los productos.
    // Para qué sirve / Destino: Proporciona los datos requeridos por los controladores para mostrarlos en las vistas de la cafetería.

        List<Producto> lista = new ArrayList<>();
        // Qué hace: Inicializa una lista dinámica vacía en memoria.
        // Para qué sirve / Destino: Almacenará los objetos Producto mapeados desde los registros de MySQL.

        String sql = "SELECT * FROM Producto";
        // Qué hace: Define la cadena con la consulta SQL para seleccionar todos los registros.
        // Para qué sirve / Destino: Instrucción que se enviará a la base de datos MySQL.

        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
        // Qué hace: Abre la conexión, prepara la sentencia SQL y ejecuta la consulta obteniendo un ResultSet mediante try-with-resources.
        // Para qué sirve / Destino: Ejecuta de forma segura la consulta SELECT contra la base de datos MySQL.

            while (rs.next()) {
            // Qué hace: Recorre iterativamente cada fila devuelta por el conjunto de resultados.
            // Para qué sirve / Destino: Permite procesar uno a uno los registros tabulares enviados por MySQL.

                Producto p = new Producto();
                // Qué hace: Instancia un nuevo objeto de la entidad Producto.
                // Para qué sirve / Destino: Crea la estructura en memoria para mapear los datos relacionales.

                p.setIdProducto(rs.getString("id_producto"));
                // Qué hace: Extrae el ID del producto de la columna SQL y lo asigna al objeto.
                // Para qué sirve / Destino: Actualiza el atributo idProducto en la entidad Producto.

                p.setIdCategoria(rs.getString("id_categoria"));
                // Qué hace: Extrae el ID de categoría de la columna SQL y lo asigna al objeto.
                // Para qué sirve / Destino: Actualiza el atributo idCategoria en la entidad Producto.

                p.setNombre(rs.getString("nombre"));
                // Qué hace: Extrae el nombre del producto de la columna SQL y lo asigna al objeto.
                // Para qué sirve / Destino: Actualiza el atributo nombre en la entidad Producto.

                p.setDescripcion(rs.getString("descripcion"));
                // Qué hace: Extrae la descripción de la columna SQL y la asigna al objeto.
                // Para qué sirve / Destino: Actualiza el atributo descripcion en la entidad Producto.

                p.setPrecio(rs.getDouble("precio"));
                // Qué hace: Extrae el precio como número decimal y lo asigna al objeto.
                // Para qué sirve / Destino: Actualiza el atributo precio en la entidad Producto.

                p.setStock(rs.getString("stock") != null ? rs.getInt("stock") : 0);
                // Qué hace: Extrae el stock como valor entero y lo asigna al objeto.
                // Para qué sirve / Destino: Actualiza el atributo stock en la entidad Producto.

                p.setEstado(rs.getString("estado"));
                // Qué hace: Extrae el estado del producto y lo asigna al objeto.
                // Para qué sirve / Destino: Actualiza el atributo estado en la entidad Producto.

                p.setImagen(rs.getString("imagen"));
                // Qué hace: Extrae la cadena de la imagen y la asigna al objeto.
                // Para qué sirve / Destino: Actualiza el atributo imagen en la entidad Producto.

                lista.add(p);
                // Qué hace: Añade el objeto Producto completamente poblado a la lista dinámica.
                // Para qué sirve / Destino: Acumula los registros para retornarlos hacia la capa de control.
            }
        } catch (Exception e) {
        // Qué hace: Captura cualquier excepción SQL o de conectividad ocurrida en el bloque.
        // Para qué sirve / Destino: Maneja errores durante la recuperación de datos desde MySQL.

            e.printStackTrace();
            // Qué hace: Imprime la traza completa del error en la consola estándar.
            // Para qué sirve / Destino: Facilita la depuración técnica para el desarrollador.
        }

        return lista;
        // Qué hace: Retorna la lista con todos los productos mapeados.
        // Para qué sirve / Destino: Entrega la colección de datos final hacia los controladores o servlets web.
    }

    /**
     * Registra un nuevo producto en la base de datos.
     * @param p Objeto Producto con los datos recibidos del formulario.
     * @return true si la inserción fue exitosa.
     */
    public boolean registrar(Producto p) {
    // Qué hace: Define el método público para insertar un nuevo producto en el sistema.
    // Para qué sirve / Destino: Recibe los datos desde los controladores y ejecuta la persistencia en MySQL.

        System.out.println("[INFO - ProductoDAO] Iniciando registrar() para el producto: " + p.getNombre());
        // Qué hace: Imprime un mensaje informativo en la consola del servidor.
        // Para qué sirve / Destino: Permite realizar un seguimiento de la ejecución en la capa DAO.

        // Obtener el mayor ID actual con ordenamiento numérico seguro para evitar colisiones
        String queryMaxId = "SELECT id_producto FROM Producto ORDER BY CAST(SUBSTRING(id_producto, 6) AS UNSIGNED) DESC LIMIT 1";
        // Qué hace: Define la consulta SQL para obtener el ID con mayor numeración actual en la tabla.
        // Para qué sirve / Destino: Se comunica con MySQL para calcular la siguiente clave primaria secuencial.

        String nextId = "PROD-001";
        // Qué hace: Inicializa una cadena con un ID por defecto para el primer registro.
        // Para qué sirve / Destino: Establece un valor base en la lógica de negocio de Java.

        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxId);
             ResultSet rsMax = psMax.executeQuery()) {
        // Qué hace: Abre la conexión y ejecuta la consulta del ID máximo usando try-with-resources.
        // Para qué sirve / Destino: Interactúa de forma segura con la base de datos MySQL.

            if (rsMax.next()) {
            // Qué hace: Verifica si se encontró un registro previo en el resultado.
            // Para qué sirve / Destino: Evalúa si ya existen productos registrados en la base de datos.

                String maxId = rsMax.getString("id_producto");
                // Qué hace: Extrae el texto del ID máximo obtenido.
                // Para qué sirve / Destino: Recupera la clave primaria actual desde MySQL.

                if (maxId != null && maxId.startsWith("PROD-")) {
                // Qué hace: Valida que el ID comience con el prefijo esperado.
                // Para qué sirve / Destino: Asegura la integridad del formato de las claves.

                    try {
                    // Qué hace: Inicia un bloque protegido para conversión numérica.
                    // Para qué sirve / Destino: Previene errores de parseo de texto.

                        int num = Integer.parseInt(maxId.substring(5));
                        // Qué hace: Extrae la parte numérica del ID eliminando el prefijo.
                        // Para qué sirve / Destino: Convierte el texto a entero para realizar operaciones matemáticas.

                        nextId = String.format("PROD-%03d", num + 1);
                        // Qué hace: Formatea el siguiente ID secuencial sumando uno (ej. PROD-002).
                        // Para qué sirve / Destino: Genera el nuevo identificador único para el producto.

                    } catch (NumberFormatException e) {
                    // Qué hace: Captura errores de conversión numérica.
                    // Para qué sirve / Destino: Muestra una advertencia en la salida de errores estándar.

                        System.err.println("[WARN - ProductoDAO] Error al parsear el número de ID máximo: " + e.getMessage());
                    }
                }
            }
        } catch (java.sql.SQLException e) {
        // Qué hace: Captura excepciones SQL ocurridas al buscar el ID máximo.
        // Para qué sirve / Destino: Maneja errores de base de datos durante el cálculo de la clave primaria.

            System.err.println("[WARN - ProductoDAO] Error al obtener el ID máximo: " + e.getMessage());
        }

        p.setIdProducto(nextId);
        // Qué hace: Asigna el nuevo ID generado al objeto Producto.
        // Para qué sirve / Destino: Actualiza la entidad en Java con su clave primaria definitiva antes de insertar.

        if (p.getDescripcion() == null || p.getDescripcion().trim().isEmpty()) {
        // Qué hace: Valida si la descripción del producto está vacía o es nula.
        // Para qué sirve / Destino: Aplica una regla lógica de negocio para asegurar contenido en la descripción.

            p.setDescripcion(p.getNombre());
            // Qué hace: Asigna el nombre del producto como descripción por defecto.
            // Para qué sirve / Destino: Actualiza el atributo descripcion en el objeto Producto.
        }

        String unidadesMedida = "Unidad";
        // Qué hace: Define una constante local para la unidad de medida predeterminada.
        // Para qué sirve / Destino: Establece un valor por defecto requerido por la tabla en MySQL.

        String sql = "INSERT INTO Producto (id_producto, id_categoria, nombre, descripcion, precio, stock, fecha_vencimiento, unidades_medida, imagen) VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?)";
        // Qué hace: Define la sentencia SQL parametrizada para insertar un nuevo producto.
        // Para qué sirve / Destino: Prepara la operación de escritura en la tabla Producto de MySQL.

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
        // Qué hace: Obtiene una conexión JDBC y prepara la sentencia SQL de inserción de forma segura.
        // Para qué sirve / Destino: Establece comunicación activa con la base de datos MySQL.

            ps.setString(1, p.getIdProducto());
            ps.setString(2, p.getIdCategoria());
            ps.setString(3, p.getNombre());
            ps.setString(4, p.getDescripcion());
            ps.setDouble(5, p.getPrecio());
            ps.setInt(6, p.getStock());
            ps.setString(7, unidadesMedida);
            ps.setString(8, p.getImagen() != null ? p.getImagen() : "");
            // Qué hace: Asigna los valores correspondientes del objeto Producto a cada parámetro de la sentencia SQL.
            // Para qué sirve / Destino: Configura los datos seguros que se guardarán en la base de datos MySQL previniendo inyecciones SQL.

            int filas = ps.executeUpdate();
            // Qué hace: Ejecuta la inserción en la base de datos y almacena el número de filas afectadas.
            // Para qué sirve / Destino: Confirma la escritura del registro en la tabla Producto de MySQL.

            System.out.println("[INFO - ProductoDAO] registrar() - Filas insertadas: " + filas);
            // Qué hace: Imprime un mensaje informativo con el resultado de la inserción.
            // Para qué sirve / Destino: Registra la actividad de persistencia en la consola del servidor.

            return filas > 0;
            // Qué hace: Retorna true si se afectó al menos una fila en la base de datos.
            // Para qué sirve / Destino: Comunica el éxito de la operación de registro hacia el controlador.

        } catch (java.sql.SQLException e) {
        // Qué hace: Captura cualquier excepción SQL generada durante la inserción.
        // Para qué sirve / Destino: Maneja errores relacionales al escribir en la base de datos.

            System.err.println("[ERROR - ProductoDAO] registrar() falló: " + e.getMessage());
            e.printStackTrace();
            // Qué hace: Registra el mensaje de error y la traza en la consola estándar de errores.
            // Para qué sirve / Destino: Facilita la depuración de fallos de persistencia en Java.

            return false;
            // Qué hace: Retorna false indicando que el registro falló.
            // Para qué sirve / Destino: Informa al controlador que la operación no pudo completarse.
        }
    }

    /**
     * Actualiza los datos de un producto existente.
     * @param p Objeto Producto con los datos a modificar.
     * @return true si la actualización fue exitosa.
     */
    public boolean actualizar(Producto p) {
    // Qué hace: Define el método público para modificar un producto existente en la base de datos.
    // Para qué sirve / Destino: Recibe los datos actualizados desde el controlador y los persiste en MySQL.

        System.out.println("[INFO - ProductoDAO] Iniciando actualizar() para el producto ID: " + p.getIdProducto());
        // Qué hace: Imprime un mensaje informativo en la consola.
        // Para qué sirve / Destino: Realiza un seguimiento de la ejecución del método de actualización.

        if (p.getDescripcion() == null || p.getDescripcion().trim().isEmpty()) {
        // Qué hace: Valida si la descripción está vacía o es nula.
        // Para qué sirve / Destino: Aplica la regla de negocio para respaldar descripciones faltantes.

            p.setDescripcion(p.getNombre());
            // Qué hace: Asigna el nombre como descripción.
            // Para qué sirve / Destino: Actualiza el atributo en la entidad Producto.
        }

        String sql = "UPDATE Producto SET id_categoria = ?, nombre = ?, descripcion = ?, precio = ?, stock = ?, imagen = ? WHERE id_producto = ?";
        // Qué hace: Define la sentencia SQL parametrizada para actualizar los campos del producto filtrando por su ID.
        // Para qué sirve / Destino: Prepara la operación de modificación en la tabla Producto de MySQL.

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
        // Qué hace: Obtiene una conexión JDBC y prepara la sentencia de actualización de forma segura.
        // Para qué sirve / Destino: Establece comunicación activa con la base de datos MySQL.

            ps.setString(1, p.getIdCategoria());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getImagen() != null ? p.getImagen() : "");
            ps.setString(7, p.getIdProducto());
            // Qué hace: Asigna los atributos actualizados del producto a los parámetros de la sentencia SQL.
            // Para qué sirve / Destino: Configura de forma segura los nuevos valores que se actualizarán en MySQL.

            int filas = ps.executeUpdate();
            // Qué hace: Ejecuta la actualización y almacena el número de filas afectadas.
            // Para qué sirve / Destino: Modifica el registro correspondiente en la tabla Producto de MySQL.

            System.out.println("[INFO - ProductoDAO] actualizar() - Filas modificadas: " + filas);
            // Qué hace: Imprime un mensaje informativo con el resultado de la actualización.
            // Para qué sirve / Destino: Registra la actividad de modificación en la consola del servidor.

            return filas > 0;
            // Qué hace: Retorna true si se afectó al menos una fila.
            // Para qué sirve / Destino: Informa al controlador que la actualización se realizó con éxito.

        } catch (java.sql.SQLException e) {
        // Qué hace: Captura excepciones SQL ocurridas durante la actualización.
        // Para qué sirve / Destino: Maneja errores relacionales al modificar registros en MySQL.

            System.err.println("[ERROR - ProductoDAO] actualizar() falló: " + e.getMessage());
            e.printStackTrace();
            // Qué hace: Imprime el error y la traza en la consola de errores estándar.
            // Para qué sirve / Destino: Facilita el diagnóstico técnico de fallos en Java.

            return false;
            // Qué hace: Retorna false si el proceso falló.
            // Para qué sirve / Destino: Comunica al controlador que la actualización no pudo completarse.
        }
    }

    /**
     * Elimina un producto de la base de datos según su ID.
     * @param id ID del producto a eliminar.
     * @return true si la operación fue exitosa.
     */
    public boolean eliminar(String id) {
    // Qué hace: Define el método público para eliminar un producto de la base de datos.
    // Para qué sirve / Destino: Recibe el identificador desde el controlador y ejecuta el borrado en MySQL.

        System.out.println("[INFO - ProductoDAO] Iniciando eliminar() para el producto ID: " + id);
        // Qué hace: Imprime un mensaje informativo en la consola.
        // Para qué sirve / Destino: Realiza un seguimiento de la ejecución del método de eliminación.

        String sql = "DELETE FROM Producto WHERE id_producto = ?";
        // Qué hace: Define la sentencia SQL parametrizada para eliminar un registro filtrando por su ID.
        // Para qué sirve / Destino: Prepara la operación de borrado en la tabla Producto de MySQL.

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
        // Qué hace: Obtiene una conexión JDBC y prepara la sentencia de borrado de forma segura.
        // Para qué sirve / Destino: Establece comunicación con la base de datos MySQL.

            ps.setString(1, id);
            // Qué hace: Asigna el ID del producto al parámetro de la sentencia SQL.
            // Para qué sirve / Destino: Configura el filtro para eliminar el registro exacto en MySQL.

            int filas = ps.executeUpdate();
            // Qué hace: Ejecuta la eliminación y almacena el número de filas afectadas.
            // Para qué sirve / Destino: Borra el registro de la tabla Producto en la base de datos MySQL.

            System.out.println("[INFO - ProductoDAO] eliminar() - Filas borradas: " + filas);
            // Qué hace: Imprime un mensaje informativo con el resultado del borrado.
            // Para qué sirve / Destino: Registra la actividad de eliminación en la consola del servidor.

            return filas > 0;
            // Qué hace: Retorna true si se afectó al menos una fila.
            // Para qué sirve / Destino: Informa al controlador que el producto fue eliminado exitosamente.

        } catch (java.sql.SQLException e) {
        // Qué hace: Captura excepciones SQL ocurridas durante la eliminación.
        // Para qué sirve / Destino: Maneja errores relacionales al borrar registros en MySQL.

            System.err.println("[ERROR - ProductoDAO] eliminar() falló: " + e.getMessage());
            e.printStackTrace();
            // Qué hace: Imprime el error y la traza en la consola de errores estándar.
            // Para qué sirve / Destino: Facilita el diagnóstico técnico de fallos en Java.

            return false;
            // Qué hace: Retorna false si la operación falló.
            // Para qué sirve / Destino: Comunica al controlador que el producto no pudo ser eliminado.
        }
    }

    /**
     * Recupera un listado con las categorías activas del sistema.
     * @return Lista de mapas con las propiedades idCategoria y nombre.
     */
    public List<java.util.Map<String, String>> listarCategorias() {
    // Qué hace: Define el método público para consultar las categorías activas.
    // Para qué sirve / Destino: Proporciona los datos de categorías requeridos por los controladores para los formularios de productos.

        List<java.util.Map<String, String>> lista = new ArrayList<>();
        // Qué hace: Inicializa una lista dinámica de mapas clave-valor en memoria.
        // Para qué sirve / Destino: Almacenará los datos estructurados de las categorías recuperadas desde MySQL.

        String sql = "SELECT id_categoria, nombre FROM Categoria WHERE estado = 'Activo'";
        // Qué hace: Define la sentencia SQL para consultar categorías cuyo estado sea activo.
        // Para qué sirve / Destino: Consulta la tabla Categoria en la base de datos MySQL.

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
        // Qué hace: Abre la conexión, prepara la sentencia y ejecuta la consulta obteniendo un ResultSet.
        // Para qué sirve / Destino: Establece comunicación segura con la base de datos MySQL para consultar categorías.

            while (rs.next()) {
            // Qué hace: Recorre iterativamente cada fila del resultado obtenido.
            // Para qué sirve / Destino: Procesa cada categoría activa devuelta por MySQL.

                java.util.Map<String, String> cat = new java.util.HashMap<>();
                // Qué hace: Crea una nueva instancia de HashMap en memoria.
                // Para qué sirve / Destino: Permite almacenar de forma estructurada los datos de una categoría.

                cat.put("idCategoria", rs.getString("id_categoria"));
                // Qué hace: Almacena el ID de la categoría en el mapa.
                // Para qué sirve / Destino: Guarda la clave primaria de la categoría.

                cat.put("nombre", rs.getString("nombre"));
                // Qué hace: Almacena el nombre de la categoría en el mapa.
                // Para qué sirve / Destino: Guarda la descripción legible de la categoría.

                lista.add(cat);
                // Qué hace: Añade el mapa de categoría a la lista dinámica.
                // Para qué sirve / Destino: Acumula los registros para retornarlos hacia el controlador.
            }
        } catch (Exception e) {
        // Qué hace: Captura cualquier excepción ocurrida durante la consulta.
        // Para qué sirve / Destino: Maneja errores al recuperar las categorías desde la base de datos.

            System.err.println("Error al listar categorías en DAO: " + e.getMessage());
            e.printStackTrace();
            // Qué hace: Registra el error y su traza en la consola de depuración estándar.
            // Para qué sirve / Destino: Facilita la identificación de problemas técnicos en Java.
        }

        return lista;
        // Qué hace: Retorna la lista con los mapas de categorías procesados.
        // Para qué sirve / Destino: Entrega los datos estructurados hacia los controladores o servlets web.
    }
}