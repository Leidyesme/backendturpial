package Controlador; // Define el paquete al que pertenece esta clase dentro de la arquitectura de controladores.

import DAO.ProductoDAO; // Importa la clase ProductoDAO para gestionar la persistencia y operaciones con la base de datos MySQL.
import Modelo.Entidades.Producto; // Importa la entidad Producto para estructurar los datos del modelo de negocio.
import jakarta.servlet.annotation.WebServlet; // Importa la anotación @WebServlet para definir la ruta URL del servlet.
import jakarta.servlet.http.HttpServlet; // Importa la clase base HttpServlet para manejar el ciclo de vida de peticiones web.
import jakarta.servlet.http.HttpServletRequest; // Importa la interfaz HttpServletRequest para procesar datos de la solicitud HTTP.
import jakarta.servlet.http.HttpServletResponse; // Importa la interfaz HttpServletResponse para configurar la respuesta HTTP.
import java.io.BufferedReader; // Importa BufferedReader para leer datos de texto desde el flujo de entrada.
import java.io.IOException; // Importa IOException para manejar errores de operaciones de entrada y salida.
import java.util.List; // Importa List para manejar colecciones genéricas.
import java.util.Map; // Importa Map para estructurar datos en pares clave-valor.
import org.json.JSONArray; // Importa JSONArray para manipular colecciones de objetos en formato JSON.
import org.json.JSONObject; // Importa JSONObject para crear y manipular objetos en formato JSON.

/**
 * Servlet que maneja las peticiones HTTP para el catálogo de productos.
 * Implementa operaciones CRUD (Crear, Leer, Actualizar, Borrar) mediante verbos HTTP.
 */
@WebServlet("/producto") // Mapea el servlet a la ruta URL "/producto" para recibir peticiones del cliente o frontend.
public class ProductoServlet extends HttpServlet { // Declara la clase pública ProductoServlet heredando de HttpServlet.

    // Instancia el DAO de productos para la comunicación directa con la base de datos MySQL.
    private final ProductoDAO dao = new ProductoDAO(); // Crea una instancia fija de ProductoDAO para ejecutar consultas y operaciones SQL.

    /**
     * Procesa peticiones GET para listar productos o categorías.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException { // Sobrescribe el método doGet para atender solicitudes HTTP GET.
        response.setContentType("application/json"); // Configura la cabecera de la respuesta HTTP indicando formato JSON.
        response.setCharacterEncoding("UTF-8"); // Establece la codificación de caracteres UTF-8 para la respuesta.

        String accion = request.getParameter("accion"); // Captura el parámetro "accion" enviado en la URL para determinar la tarea a ejecutar.

        // Listar categorías
        if ("listCategories".equals(accion)) { // Evalúa si la acción solicitada corresponde a listar categorías.
            List<Map<String, String>> listaCat = dao.listarCategorias(); // Solicita al DAO la lista de categorías desde la base de datos MySQL.
            JSONArray jsonArray = new JSONArray(); // Instancia un contenedor JSON de tipo arreglo para las categorías.
            for (Map<String, String> cat : listaCat) { // Itera sobre cada registro de categoría obtenido.
                JSONObject jsonItem = new JSONObject(); // Crea un objeto JSON por cada categoría.
                jsonItem.put("idCategoria", cat.get("idCategoria")); // Agrega el ID de la categoría al JSON.
                jsonItem.put("nombre", cat.get("nombre")); // Agrega el nombre de la categoría al JSON.
                jsonArray.put(jsonItem); // Inserta el objeto en el arreglo JSON general.
            }
            response.getWriter().print(jsonArray.toString()); // Escribe el JSONArray serializado en la respuesta HTTP hacia el cliente.
            return; // Finaliza la ejecución del método para la rama de categorías.
        }

        // Listar productos
        List<Producto> lista = dao.listar(); // Invoca al DAO para recuperar todos los registros de productos desde MySQL.
        JSONArray jsonArray = new JSONArray(); // Crea un JSONArray para almacenar los productos transformados.
        for (Producto p : lista) { // Itera a través de cada entidad Producto devuelta por la base de datos.
            JSONObject item = new JSONObject(); // Instancia un objeto JSON para representar cada producto de forma individual.
            item.put("idProducto", p.getIdProducto()); // Asigna el identificador del producto al JSON.
            item.put("nombre", p.getNombre()); // Asigna el nombre del producto al JSON.
            item.put("descripcion", p.getDescripcion()); // Asigna la descripción del producto al JSON.
            item.put("precio", p.getPrecio()); // Asigna el precio del producto al JSON.
            item.put("stock", p.getStock()); // Asigna el stock disponible al JSON.
            item.put("imagen", p.getImagen()); // Asigna la ruta o URL de la imagen del producto al JSON.
            jsonArray.put(item); // Añade el objeto JSON del producto al arreglo principal.
        }
        response.getWriter().print(jsonArray.toString()); // Envía la respuesta HTTP con el arreglo completo de productos en formato JSON.
    }

    /**
     * Procesa peticiones POST para registrar un nuevo producto.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException { // Sobrescribe el método doPost para manejar solicitudes HTTP POST de creación.
        response.setContentType("application/json"); // Configura el tipo de contenido de la respuesta HTTP como JSON.
        response.setCharacterEncoding("UTF-8"); // Define la codificación de caracteres en UTF-8.
        JSONObject jsonRespuesta = new JSONObject(); // Inicializa un objeto JSON para estructurar la respuesta al cliente.

        try {
            String body = leerCuerpo(request); // Invoca el método auxiliar para extraer el contenido en texto del cuerpo de la petición.
            if (body.trim().isEmpty()) { // Comprueba si el cuerpo de la petición llegó vacío.
                enviarError(response, "Cuerpo JSON vacío"); // Llama al método auxiliar para reportar el error de contenido vacío.
                return; // Interrumpe la ejecución del método POST.
            }

            JSONObject jsonEntrada = new JSONObject(body); // Convierte la cadena JSON recibida en un objeto manipulable.
            Producto p = new Producto(); // Crea una nueva instancia de la entidad Producto.
            p.setNombre(jsonEntrada.getString("name")); // Asigna el nombre obtenido del JSON a la entidad.
            p.setPrecio(jsonEntrada.getDouble("price")); // Asigna el precio obtenido del JSON a la entidad.
            p.setStock(jsonEntrada.getInt("stock")); // Asigna el stock obtenido del JSON a la entidad.
            p.setIdCategoria(jsonEntrada.getString("category")); // Asigna la categoría obtenida del JSON a la entidad.
            p.setImagen(jsonEntrada.optString("image", "")); // Asigna la imagen opcional obtenida del JSON a la entidad.

            if (dao.registrar(p)) { // Invoca al DAO para realizar la inserción del producto en la base de datos MySQL.
                jsonRespuesta.put("status", "success"); // Configura el estado de éxito en la respuesta JSON.
                jsonRespuesta.put("message", "Producto registrado exitosamente"); // Configura el mensaje descriptivo de éxito.
                jsonRespuesta.put("idProducto", p.getIdProducto()); // Añade el ID generado del producto a la respuesta.
            } else {
                jsonRespuesta.put("status", "error"); // Configura el estado de error si el DAO falla en la inserción.
                jsonRespuesta.put("message", "No se pudo registrar el producto"); // Configura el mensaje descriptivo de error.
            }
        } catch (Exception e) {
            jsonRespuesta.put("status", "error"); // Configura el estado de error ante cualquier excepción imprevista.
            jsonRespuesta.put("message", "Error: " + e.getMessage()); // Agrega el detalle del mensaje de la excepción al JSON.
        }
        response.getWriter().print(jsonRespuesta.toString()); // Envía el JSON de respuesta final de la operación POST al cliente.
    }

    /**
     * Procesa peticiones PUT para actualizar un producto existente.
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException { // Sobrescribe el método doPut para atender solicitudes HTTP PUT de actualización.
        response.setContentType("application/json"); // Configura el tipo de respuesta HTTP como formato JSON.
        JSONObject jsonRespuesta = new JSONObject(); // Crea un objeto JSON para armar la respuesta de la operación.

        try {
            String body = leerCuerpo(request); // Extrae el cuerpo de la solicitud HTTP mediante el método auxiliar.
            JSONObject jsonEntrada = new JSONObject(body); // Parsea el texto del cuerpo en un objeto JSONObject.
            
            Producto p = new Producto(); // Instancia la entidad Producto para recibir los datos actualizados.
            
            String rawId = String.valueOf(jsonEntrada.get("idProducto")); // Extrae el identificador bruto del producto desde el JSON.
            if (rawId.matches("\\d+")) { // Valida si el ID consta puramente de dígitos numéricos.
                rawId = "PROD-" + String.format("%03d", Integer.parseInt(rawId)); // Formatea el ID numérico al estándar con prefijo en la aplicación.
            }
            p.setIdProducto(rawId); // Asigna el identificador formateado a la entidad Producto.

            p.setNombre(jsonEntrada.getString("name")); // Asigna el nombre actualizado desde el JSON a la entidad.
            p.setPrecio(jsonEntrada.getDouble("price")); // Asigna el precio actualizado desde el JSON a la entidad.
            p.setStock(jsonEntrada.getInt("stock")); // Asigna el stock actualizado desde el JSON a la entidad.

            String rawCat = String.valueOf(jsonEntrada.get("category")); // Extrae la categoría en formato bruto desde el JSON.
            if (rawCat.matches("\\d+")) { // Comprueba si la categoría es numérica.
                rawCat = "CAT-" + String.format("%03d", Integer.parseInt(rawCat)); // Aplica el formato estándar con prefijo para la categoría.
            }
            p.setIdCategoria(rawCat); // Asigna la categoría formateada a la entidad Producto.

            p.setImagen(jsonEntrada.optString("image", "")); // Asigna la imagen opcional de actualización a la entidad.

            if (dao.actualizar(p)) { // Invoca al DAO para ejecutar la actualización del registro en MySQL.
                jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta.
                jsonRespuesta.put("message", "Producto actualizado correctamente"); // Añade un mensaje de éxito al JSON.
            } else {
                jsonRespuesta.put("status", "error"); // Configura el estado de error si el DAO no logra actualizar.
                jsonRespuesta.put("message", "No se pudo actualizar"); // Añade un mensaje de error al JSON.
            }
        } catch (Exception e) {
            jsonRespuesta.put("status", "error"); // Configura el estado de error si ocurre una excepción durante el proceso.
            jsonRespuesta.put("message", "Error: " + e.getMessage()); // Agrega el mensaje de la excepción capturada al JSON.
        }
        response.getWriter().print(jsonRespuesta.toString()); // Envía la respuesta JSON resultante de la petición PUT al cliente.
    }

    /**
     * Procesa peticiones DELETE para eliminar un producto.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException { // Sobrescribe el método doDelete para procesar solicitudes HTTP DELETE.
        response.setContentType("application/json"); // Define la respuesta HTTP como tipo JSON.
        String idProducto = request.getParameter("idProducto"); // Extrae el parámetro "idProducto" de la cadena de consulta de la URL.
        JSONObject jsonRespuesta = new JSONObject(); // Crea un objeto JSON para estructurar la respuesta de la eliminación.

        if (idProducto != null && dao.eliminar(idProducto)) { // Verifica que el ID exista e invoca al DAO para borrar el registro en MySQL.
            jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta.
            jsonRespuesta.put("message", "Producto eliminado"); // Asigna un mensaje indicando que el producto fue eliminado.
        } else {
            jsonRespuesta.put("status", "error"); // Configura el estado de error si la validación o el borrado fallan.
            jsonRespuesta.put("message", "No se pudo eliminar"); // Asigna un mensaje de error al JSON.
        }
        response.getWriter().print(jsonRespuesta.toString()); // Escribe y envía la respuesta JSON de la eliminación hacia el cliente.
    }

    // Métodos auxiliares para limpieza de código
    private String leerCuerpo(HttpServletRequest request) throws IOException { // Declara un método auxiliar privado para leer el cuerpo HTTP.
        StringBuilder sb = new StringBuilder(); // Instancia un StringBuilder para acumular los fragmentos de texto leídos.
        try (BufferedReader reader = request.getReader()) { // Obtiene el lector de flujo de la petición HTTP de manera segura.
            String line; // Declara una variable para almacenar cada línea leída.
            while ((line = reader.readLine()) != null) sb.append(line); // Lee iterativamente cada línea del cuerpo y la concatena.
        }
        return sb.toString(); // Retorna la cadena completa extraída del cuerpo de la petición.
    }

    private void enviarError(HttpServletResponse response, String mensaje) throws IOException { // Declara un método auxiliar para centralizar respuestas de error.
        JSONObject err = new JSONObject(); // Crea un objeto JSON para estructurar los datos del error.
        err.put("status", "error"); // Asigna el estado de error en el JSON.
        err.put("message", mensaje); // Asigna el mensaje descriptivo recibido como parámetro al JSON.
        response.getWriter().print(err.toString()); // Escribe y envía el JSON de error directamente a la respuesta HTTP del cliente.
    }
}