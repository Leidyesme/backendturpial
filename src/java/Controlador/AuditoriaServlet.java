package Controlador; // Define el paquete al que pertenece la clase (capa de Controladores).

import DAO.AuditoriaDAO; // Importa la clase de acceso a datos para interactuar con la base de datos de auditoría.
import Modelo.Entidades.Auditoria; // Importa la entidad de modelo que representa un registro de auditoría.
import jakarta.servlet.ServletException; // Importa la excepción para errores específicos de servlets.
import jakarta.servlet.annotation.WebServlet; // Importa la anotación para mapear la URL del servlet.
import jakarta.servlet.http.HttpServlet; // Importa la clase base para crear servlets HTTP.
import jakarta.servlet.http.HttpServletRequest; // Importa la interfaz para manejar peticiones HTTP del cliente.
import jakarta.servlet.http.HttpServletResponse; // Importa la interfaz para manejar la respuesta HTTP hacia el cliente.
import java.io.BufferedReader; // Importa la clase para leer flujos de texto de la petición.
import java.io.IOException; // Importa la excepción de entrada/salida.
import java.io.PrintWriter; // Importa la clase para escribir texto en la respuesta HTTP.
import java.util.List; // Importa la interfaz List para manejar colecciones de datos.
import org.json.JSONArray; // Importa la librería externa para manipular arreglos JSON.
import org.json.JSONObject; // Importa la librería externa para manipular objetos JSON.

/**
 * Servlet que maneja las peticiones HTTP relacionadas con el log de auditoría/actividad del usuario.
 * Proporciona endpoints para registrar y listar actividades del usuario.
 */
@WebServlet("/auditoria") // Mapea este servlet a la ruta URL "/auditoria" para recibir peticiones del cliente.
public class AuditoriaServlet extends HttpServlet { // Declara la clase pública AuditoriaServlet extendiendo de HttpServlet.

    // Instancia del DAO para acceder a los métodos de base de datos.
    private final AuditoriaDAO dao = new AuditoriaDAO(); // Instancia la clase de acceso a datos para comunicarse con MySQL a través de los métodos DAO.

    /**
     * Procesa solicitudes POST para registrar o listar registros de auditoría.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doPost para manejar peticiones HTTP POST recibidas desde el frontend.
        // Configura la respuesta como JSON para que el frontend pueda procesarla fácilmente.
        response.setContentType("application/json"); // Establece el tipo de contenido MIME de la respuesta HTTP como JSON.
        response.setCharacterEncoding("UTF-8"); // Configura la codificación de caracteres a UTF-8 para la respuesta.
        PrintWriter out = response.getWriter(); // Obtiene el objeto PrintWriter para enviar datos de texto/JSON de vuelta al cliente.

        // Obtiene el parámetro de acción (ej: 'listar' o 'registrar') desde la URL.
        String accion = request.getParameter("accion"); // Extrae el parámetro "accion" enviado por la URL o query string.
        JSONObject jsonRespuesta = new JSONObject(); // Instancia un objeto JSON vacío para estructurar la respuesta hacia el frontend.

        try {
            // Lectura del cuerpo de la petición (JSON crudo) enviado desde el frontend.
            StringBuilder sb = new StringBuilder(); // Inicializa un StringBuilder para concatenar las líneas del cuerpo de la petición.
            String line; // Variable temporal para almacenar cada línea leída del flujo.
            try (BufferedReader reader = request.getReader()) { // Obtiene el lector del flujo de entrada de la solicitud HTTP.
                while ((line = reader.readLine()) != null) { // Lee línea por línea el contenido JSON enviado en el cuerpo de la petición.
                    sb.append(line); // Concatena cada línea en el StringBuilder.
                }
            }
            String body = sb.toString(); // Convierte el contenido acumulado del cuerpo a una cadena de texto (String).

            // LÓGICA DE LISTADO: Recupera el historial de actividades de un usuario.
            if ("listar".equals(accion)) { // Comprueba si la acción solicitada es "listar".
                String idUsuario = null; // Inicializa la variable para almacenar el identificador del usuario.
                // Intenta obtener idUsuario desde el JSON del cuerpo o desde un parámetro URL.
                if (!body.trim().isEmpty()) { // Verifica si el cuerpo de la petición no está vacío.
                    JSONObject jsonEntrada = new JSONObject(body); // Parsea el cuerpo de la petición a un objeto JSON.
                    idUsuario = jsonEntrada.optString("idUsuario", null); // Extrae de manera segura la propiedad "idUsuario" del JSON.
                }
                if (idUsuario == null || idUsuario.isEmpty()) { // Comprueba si el ID de usuario sigue sin encontrarse.
                    idUsuario = request.getParameter("idUsuario"); // Intenta obtener el ID de usuario directamente desde los parámetros HTTP de la URL.
                }

                // Validación: si no hay ID, no podemos listar nada.
                if (idUsuario == null || idUsuario.isEmpty()) { // Valida si el ID de usuario es nulo o vacío tras ambos intentos.
                    jsonRespuesta.put("status", "error"); // Asigna el estado de error en el objeto JSON de respuesta.
                    jsonRespuesta.put("message", "Se requiere el parámetro 'idUsuario'"); // Asigna el mensaje descriptivo del error de validación.
                    out.print(jsonRespuesta.toString()); // Escribe el JSON de respuesta hacia el cliente HTTP.
                    return; // Interrumpe la ejecución del método.
                }

                // Llama al DAO para obtener la lista de la base de datos.
                List<Auditoria> lista = dao.listarPorUsuario(idUsuario); // Invoca al método del DAO para consultar registros en MySQL filtrados por usuario.
                JSONArray jsonArray = new JSONArray(); // Crea un arreglo JSON para almacenar la lista de objetos convertidos.
                // Convierte la lista de objetos Java a un JSONArray para el frontend.
                for (Auditoria aud : lista) { // Itera sobre cada entidad Auditoria obtenida de la base de datos.
                    JSONObject item = new JSONObject(); // Crea un objeto JSON por cada iteración para representar un registro individual.
                    item.put("idHistorial", aud.getIdHistorial()); // Agrega el identificador del historial al objeto JSON.
                    item.put("idUsuario", aud.getIdUsuario()); // Agrega el identificador del usuario al objeto JSON.
                    item.put("accion", aud.getAccion()); // Agrega la descripción de la acción al objeto JSON.
                    item.put("tipoAccion", aud.getTipoAccion()); // Agrega el tipo de acción al objeto JSON.
                    item.put("fecha", aud.getFecha()); // Agrega la fecha de la actividad al objeto JSON.
                    jsonArray.put(item); // Inserta el objeto JSON del registro dentro del arreglo JSON principal.
                }

                jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta general.
                jsonRespuesta.put("activities", jsonArray); // Inserta el arreglo JSON con las actividades dentro de la respuesta hacia el frontend.
            } 
            // LÓGICA DE REGISTRO: Guarda una nueva actividad en el historial.
            else if ("registrar".equals(accion)) { // Comprueba si la acción solicitada es "registrar".
                if (body.trim().isEmpty()) { // Valida si el cuerpo JSON enviado está vacío.
                    jsonRespuesta.put("status", "error"); // Asigna el estado de error en la respuesta.
                    jsonRespuesta.put("message", "Cuerpo JSON vacío"); // Especifica el mensaje indicando que falta el cuerpo de la petición.
                    out.print(jsonRespuesta.toString()); // Envía el JSON de respuesta al cliente.
                    return; // Termina la ejecución del método.
                }

              // Extrae los datos del JSON recibido.
                JSONObject jsonEntrada = new JSONObject(body); // Parsea el cuerpo de la petición a un objeto JSON.
                String idUsuario = jsonEntrada.getString("idUsuario"); // Extrae obligatoriamente el ID del usuario desde el JSON.
                String descripcion = jsonEntrada.getString("accion"); // Extrae la descripción de la acción desde el JSON.
                String tipoAccion = jsonEntrada.getString("tipoAccion"); // Extrae el tipo de acción desde el JSON.

              // Crea la entidad intermedia para enviarla al DAO.
                Auditoria aud = new Auditoria(); // Instancia un nuevo objeto de la entidad Auditoria.
                aud.setIdUsuario(idUsuario); // Asigna el ID de usuario a la entidad de modelo.
                aud.setAccion(descripcion); // Asigna la descripción de la acción a la entidad de modelo.
                aud.setTipoAccion(tipoAccion); // Asigna el tipo de acción a la entidad de modelo.

              // Ejecuta la inserción en base de datos.
                boolean registrado = dao.registrarActividad(aud); // Invoca al DAO para insertar la entidad de auditoría en la base de datos MySQL.
                if (registrado) { // Valida si el método del DAO retornó true (inserción exitosa).
                    jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta.
                    jsonRespuesta.put("message", "Actividad registrada exitosamente"); // Configura el mensaje de éxito para el cliente.
                } else {
                    jsonRespuesta.put("status", "error"); // Configura el estado de error si el registro falló en la capa de datos.
                    jsonRespuesta.put("message", "No se pudo registrar la actividad"); // Configura el mensaje descriptivo del fallo.
                }
            } 
            else {
              // Manejo de acciones no definidas.
                jsonRespuesta.put("status", "error"); // Configura el estado de error por acción desconocida.
                jsonRespuesta.put("message", "Acción no reconocida"); // Establece el mensaje de advertencia para acciones no válidas.
            }
        } catch (Exception e) {
          // Manejo de errores internos (ej: JSON mal formado).
            e.printStackTrace(); // Imprime la traza completa de la excepción en la consola de errores del servidor.
            jsonRespuesta.put("status", "error"); // Configura el estado como error en el JSON de respuesta global.
            jsonRespuesta.put("message", "Error interno: " + e.getMessage()); // Incluye el mensaje de la excepción capturada hacia el cliente.
        }

        // Envía el JSON final de respuesta al cliente.
        out.print(jsonRespuesta.toString()); // Escribe el objeto JSON completo convertido a texto en el flujo de salida HTTP.
        out.flush(); // Asegura que todos los datos pendientes del búfer se envíen inmediatamente al cliente.
    }

    /**
     * Responde a peticiones preflight OPTIONS (necesario para CORS).
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doOptions para atender peticiones de verificación previa (CORS preflight).
        response.setStatus(HttpServletResponse.SC_OK); // Establece el estado HTTP 200 OK como respuesta exitosa a la petición preliminar de opciones.
    }

    /**
     * Soporta GET para simplificar pruebas de integración o depuración.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doGet para redirigir peticiones GET hacia el método doPost.
        doPost(request, response); // Reenvía los parámetros de la solicitud GET para que sean procesados por la misma lógica central en doPost.
    }
}