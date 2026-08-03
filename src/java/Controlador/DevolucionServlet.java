package Controlador; // Define el paquete al que pertenece la clase (capa de Controladores).

import DAO.DevolucionDAO; // Importa la clase de acceso a datos para interactuar con la tabla de devoluciones en MySQL.
import DAO.UsuarioDAO; // Importa la clase de acceso a datos para consultar información y roles de usuario en MySQL.
import Modelo.Entidades.Devolucion; // Importa la entidad de modelo que representa un registro de devolución.
import Modelo.Entidades.Usuario; // Importa la entidad de modelo que representa un usuario del sistema.
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
 * Servlet que maneja las peticiones HTTP relacionadas con el módulo de devoluciones de pedidos.
 * Proporciona endpoints para solicitar devoluciones de pedidos y consultar su estado.
 */
@WebServlet("/devolucion") // Mapea este servlet a la ruta URL "/devolucion" para recibir peticiones del cliente.
public class DevolucionServlet extends HttpServlet { // Declara la clase pública DevolucionServlet extendiendo de HttpServlet.

    // Instancia del DAO para interactuar con la tabla de devoluciones en MySQL.
    private final DevolucionDAO dao = new DevolucionDAO(); // Instancia la clase de acceso a datos para comunicarse con la base de datos MySQL en las operaciones de devolución.

    /**
     * Procesa solicitudes POST para registrar o listar solicitudes de devolución.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doPost para procesar peticiones HTTP POST enviadas por el frontend.
        // Establece el formato de respuesta como JSON para el frontend.
        response.setContentType("application/json"); // Configura el tipo de contenido MIME de la respuesta HTTP como formato JSON.
        response.setCharacterEncoding("UTF-8"); // Establece la codificación de caracteres en UTF-8 para la respuesta.
        PrintWriter out = response.getWriter(); // Obtiene el objeto PrintWriter para enviar la respuesta estructurada en texto hacia el cliente.

        // Identifica la acción solicitada por el usuario (solicitar, listar, listarTodas, responder).
        String accion = request.getParameter("accion"); // Extrae el parámetro "accion" enviado en la URL o consulta HTTP.
        JSONObject jsonRespuesta = new JSONObject(); // Instancia un objeto JSON vacío para estructurar la respuesta hacia el cliente.

        try {
            // Lectura del cuerpo de la petición (JSON) enviado por el cliente.
            StringBuilder sb = new StringBuilder(); // Inicializa un StringBuilder para acumular las líneas del cuerpo de la petición.
            String line; // Variable temporal para almacenar cada línea leída del flujo.
            try (BufferedReader reader = request.getReader()) { // Obtiene el lector de flujo de entrada de la solicitud HTTP.
                while ((line = reader.readLine()) != null) { // Lee de forma iterativa cada línea del cuerpo de la solicitud.
                    sb.append(line); // Concatena la línea leída en el StringBuilder.
                }
            }
            String body = sb.toString(); // Convierte el contenido acumulado del cuerpo a una cadena de texto (String).

            // CASO: SOLICITAR DEVOLUCIÓN
            if ("solicitar".equals(accion)) { // Evalúa si la acción solicitada corresponde a "solicitar".
                if (body.trim().isEmpty()) { // Valida si el cuerpo de la petición JSON está vacío.
                    jsonRespuesta.put("status", "error"); // Asigna el estado de error en el JSON de respuesta.
                    jsonRespuesta.put("message", "Cuerpo JSON vacío"); // Agrega un mensaje descriptivo del error al JSON.
                    out.print(jsonRespuesta.toString()); // Escribe el JSON de respuesta hacia el cliente.
                    return; // Interrumpe la ejecución del método.
                }

                JSONObject jsonEntrada = new JSONObject(body); // Parsea la cadena del cuerpo a un objeto JSON de entrada.
                String idPedido = jsonEntrada.getString("idPedido"); // Extrae el identificador del pedido del JSON recibido.
                String motivo = jsonEntrada.getString("motivo"); // Extrae el motivo de la devolución del JSON recibido.
                String idUsuario = jsonEntrada.optString("idUsuario", null); // Extrae de manera opcional el ID del usuario del JSON.

                // CONTROL DE ACCESO (RBAC): Valida que solo clientes (ROL-003) puedan solicitar devoluciones.
                if (idUsuario != null && !idUsuario.isEmpty()) { // Verifica si se proporcionó un ID de usuario para validar permisos.
                    UsuarioDAO usuarioDao = new UsuarioDAO(); // Instancia el DAO de usuarios para consultar los datos del usuario en la base de datos MySQL.
                    Usuario user = usuarioDao.obtenerUsuarioPorId(idUsuario); // Consulta el usuario por su ID mediante el DAO.
                    if (user == null || !"ROL-003".equals(user.getIdRol())) { // Valida si el usuario no existe o no tiene asignado el rol de cliente (ROL-003).
                        jsonRespuesta.put("status", "error"); // Configura el estado de error por falta de permisos.
                        jsonRespuesta.put("message", "Acceso denegado: Solo los clientes tienen permitido solicitar devoluciones."); // Agrega mensaje explicativo de control de acceso.
                        out.print(jsonRespuesta.toString()); // Escribe la respuesta JSON de error hacia el cliente.
                        return; // Interrumpe la ejecución del método.
                  }
                }

                // Crea la entidad y llama al DAO para registrarla.
                Devolucion dev = new Devolucion(); // Instancia un nuevo objeto de la entidad Devolucion.
                dev.setIdPedido(idPedido); // Asigna el ID del pedido a la entidad de modelo.
                dev.setMotivo(motivo); // Asigna el motivo de devolución a la entidad de modelo.

                boolean solicitado = dao.solicitarDevolucion(dev); // Invoca al DAO para insertar la solicitud de devolución en la base de datos MySQL.
                if (solicitado) { // Comprueba si el registro en la base de datos fue exitoso.
                    jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta.
                    jsonRespuesta.put("message", "Solicitud de devolución registrada exitosamente"); // Configura el mensaje de éxito para el cliente.
                } else {
                    jsonRespuesta.put("status", "error"); // Configura el estado de error si el DAO falla en la inserción.
                    jsonRespuesta.put("message", "No se pudo registrar la solicitud de devolución"); // Configura el mensaje de error para el cliente.
                }
            } 
            // CASO: LISTAR DEVOLUCIONES DE UN CLIENTE
            else if ("listar".equals(accion)) { // Evalúa si la acción solicitada corresponde a listar devoluciones de un usuario específico.
                String idUsuario = null; // Inicializa la variable para almacenar el ID de usuario.
                if (!body.trim().isEmpty()) { // Verifica si el cuerpo JSON no está vacío.
                    JSONObject jsonEntrada = new JSONObject(body); // Parsea el cuerpo de la petición a un objeto JSON.
                    idUsuario = jsonEntrada.optString("idUsuario", null); // Extrae de manera segura la propiedad idUsuario del JSON.
                }
                if (idUsuario == null || idUsuario.isEmpty()) { // Comprueba si el ID de usuario no fue encontrado en el cuerpo JSON.
                    idUsuario = request.getParameter("idUsuario"); // Intenta obtener el ID de usuario directamente desde los parámetros URL.
                }

                if (idUsuario == null || idUsuario.isEmpty()) { // Valida si el ID de usuario sigue ausente después de ambos intentos.
                    jsonRespuesta.put("status", "error"); // Configura el estado de error por falta de parámetros obligatorios.
                    jsonRespuesta.put("message", "Se requiere el parámetro 'idUsuario'"); // Agrega el mensaje descriptivo del error.
                    out.print(jsonRespuesta.toString()); // Envía el JSON de respuesta de error al cliente.
                    return; // Interrumpe la ejecución del método.
                }

                // Llama al DAO para obtener las devoluciones específicas del usuario.
                List<Devolucion> lista = dao.listarPorUsuario(idUsuario); // Invoca al DAO para consultar registros de devolución filtrados por usuario en MySQL.
                JSONArray jsonArray = new JSONArray(); // Crea un arreglo JSON para almacenar los registros transformados.
                for (Devolucion dev : lista) { // Itera sobre cada entidad de devolución obtenida de la base de datos.
                    JSONObject item = new JSONObject(); // Instancia un objeto JSON para representar cada devolución individual.
                    item.put("idDevolucion", dev.getIdDevolucion()); // Agrega el ID de devolución al objeto JSON.
                    item.put("idPedido", dev.getIdPedido()); // Agrega el ID de pedido al objeto JSON.
                    item.put("motivo", dev.getMotivo()); // Agrega el motivo al objeto JSON.
                    item.put("fechaSolicitud", dev.getFechaSolicitud()); // Agrega la fecha de solicitud al objeto JSON.
                    item.put("estadoDevolucion", dev.getEstadoDevolucion()); // Agrega el estado de devolución al objeto JSON.
                    item.put("respuestaAdmin", dev.getRespuestaAdmin() != null ? dev.getRespuestaAdmin() : ""); // Agrega la respuesta del administrador asegurando que no sea nula.
                    jsonArray.put(item); // Inserta el objeto JSON individual dentro del arreglo JSON principal.
                }

                jsonRespuesta.put("status", "success"); // Configura el estado de éxito en la respuesta general.
                jsonRespuesta.put("returns", jsonArray); // Inserta el arreglo JSON con las devoluciones en la respuesta hacia el cliente.
          } 
          // CASO: LISTAR TODAS LAS DEVOLUCIONES (SOLO ADMINS)
          else if ("listarTodas".equals(accion)) { // Evalúa si la acción solicitada corresponde a listar todas las devoluciones.
                String idUsuario = null; // Inicializa la variable para el ID de usuario.
                if (!body.trim().isEmpty()) { // Verifica si el cuerpo JSON no está vacío.
                    JSONObject jsonEntrada = new JSONObject(body); // Parsea el cuerpo de la petición a un objeto JSON.
                    idUsuario = jsonEntrada.optString("idUsuario", null); // Extrae de manera segura el ID del usuario del JSON.
                }
                if (idUsuario == null || idUsuario.isEmpty()) { // Comprueba si el ID de usuario no se obtuvo del cuerpo.
                    idUsuario = request.getParameter("idUsuario"); // Intenta obtener el ID de usuario desde los parámetros URL.
                }

                // CONTROL DE ACCESO (RBAC): Valida rol de administrador (ROL-001).
                if (idUsuario != null && !idUsuario.isEmpty()) { // Verifica si se proporcionó un ID de usuario para autenticar permisos.
                    UsuarioDAO usuarioDao = new UsuarioDAO(); // Instancia el DAO de usuarios para consultar información en MySQL.
                    Usuario user = usuarioDao.obtenerUsuarioPorId(idUsuario); // Consulta los datos del usuario utilizando el DAO.
                    if (user == null || !"ROL-001".equals(user.getIdRol())) { // Valida si el usuario no existe o carece del rol de administrador (ROL-001).
                        jsonRespuesta.put("status", "error"); // Configura el estado de error por falta de privilegios administrativos.
                        jsonRespuesta.put("message", "Acceso denegado: Rol no autorizado para ver todas las devoluciones."); // Agrega mensaje explicativo del bloqueo de seguridad.
                        out.print(jsonRespuesta.toString()); // Escribe la respuesta JSON de error hacia el cliente.
                        return; // Interrumpe la ejecución del método.
                  }
                } else {
                    jsonRespuesta.put("status", "error"); // Configura el estado de error si no se envía identificación para validación.
                    jsonRespuesta.put("message", "Acceso denegado: Se requiere idUsuario para validar permisos."); // Especifica el mensaje de requerimiento de identificación.
                    out.print(jsonRespuesta.toString()); // Escribe el JSON de respuesta de error hacia el cliente.
                    return; // Interrumpe la ejecución del método.
                }

                List<Devolucion> lista = dao.listarTodas(); // Invoca al DAO para obtener la lista completa de devoluciones desde la base de datos MySQL.
                JSONArray jsonArray = new JSONArray(); // Crea un arreglo JSON para almacenar las devoluciones mapeadas.
                for (Devolucion dev : lista) { // Itera sobre cada entidad de devolución obtenida.
                    JSONObject item = new JSONObject(); // Instancia un objeto JSON para representar cada registro individual.
                    item.put("idDevolucion", dev.getIdDevolucion()); // Inserta el ID de devolución en el objeto JSON.
                    item.put("idPedido", dev.getIdPedido()); // Inserta el ID de pedido en el objeto JSON.
                    item.put("motivo", dev.getMotivo()); // Inserta el motivo en el objeto JSON.
                    item.put("fechaSolicitud", dev.getFechaSolicitud()); // Inserta la fecha de solicitud en el objeto JSON.
                    item.put("estadoDevolucion", dev.getEstadoDevolucion()); // Inserta el estado de devolución en el objeto JSON.
                    item.put("respuestaAdmin", dev.getRespuestaAdmin() != null ? dev.getRespuestaAdmin() : ""); // Inserta la respuesta del administrador con validación de nulidad.
                    jsonArray.put(item); // Añade el objeto JSON individual al arreglo general.
                }

                jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta general.
                jsonRespuesta.put("returns", jsonArray); // Inserta el arreglo JSON con todas las devoluciones hacia el cliente.
      } 
          // CASO: RESPONDER / PROCESAR DEVOLUCIÓN (ADMINS)
          else if ("responder".equals(accion)) { // Evalúa si la acción solicitada corresponde a procesar/responder una devolución.
                if (body.trim().isEmpty()) { // Valida si el cuerpo de la petición JSON está vacío.
                    jsonRespuesta.put("status", "error"); // Configura el estado de error por cuerpo vacío.
                    jsonRespuesta.put("message", "Cuerpo JSON vacío"); // Agrega el mensaje descriptivo del error.
                    out.print(jsonRespuesta.toString()); // Envía el JSON de respuesta al cliente.
                    return; // Interrumpe la ejecución del método.
                }

                JSONObject jsonEntrada = new JSONObject(body); // Parsea el cuerpo JSON recibido a un objeto de entrada.
                String idUsuario = jsonEntrada.getString("idUsuario"); // Extrae el ID del usuario administrador desde el JSON.
                String idDevolucion = jsonEntrada.getString("idDevolucion"); // Extrae el ID de la devolución a procesar desde el JSON.
                String estado = jsonEntrada.getString("estado"); // Extrae el nuevo estado ('Aprobada' o 'Rechazada') desde el JSON.
                String respuestaAdmin = jsonEntrada.getString("respuestaAdmin"); // Extrae el texto de respuesta del administrador desde el JSON.

                // CONTROL DE ACCESO (RBAC): Valida rol de administrador.
                UsuarioDAO usuarioDao = new UsuarioDAO(); // Instancia el DAO de usuarios para verificar credenciales en MySQL.
                Usuario user = usuarioDao.obtenerUsuarioPorId(idUsuario); // Consulta los datos del usuario mediante el DAO.
                if (user == null || !"ROL-001".equals(user.getIdRol())) { // Valida si el usuario no existe o no cuenta con permisos de administrador (ROL-001).
                    jsonRespuesta.put("status", "error"); // Configura el estado de error por falta de privilegios.
                    jsonRespuesta.put("message", "Acceso denegado: Solo administradores pueden procesar devoluciones."); // Agrega mensaje explicativo del bloqueo de seguridad.
                    out.print(jsonRespuesta.toString()); // Escribe el JSON de respuesta de error hacia el cliente.
                    return; // Interrumpe la ejecución del método.
                }

                boolean procesado = dao.procesarDevolucion(idDevolucion, estado, respuestaAdmin, idUsuario); // Invoca al DAO para actualizar la devolución y persistir el cambio en la base de datos MySQL.
                if (procesado) { // Comprueba si la actualización en la base de datos fue exitosa.
                    jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta.
                    jsonRespuesta.put("message", "Devolución procesada correctamente"); // Configura el mensaje de éxito para el cliente.
                } else {
                    jsonRespuesta.put("status", "error"); // Configura el estado de error si el DAO falla al actualizar el registro.
                    jsonRespuesta.put("message", "No se pudo actualizar el estado de la devolución"); // Configura el mensaje de fallo para el cliente.
                }
          }
          else {
                jsonRespuesta.put("status", "error"); // Configura el estado de error para acciones no reconocidas o inválidas.
                jsonRespuesta.put("message", "Acción no reconocida"); // Establece el mensaje de advertencia correspondiente.
          }
      } catch (Exception e) {
            e.printStackTrace(); // Imprime la traza completa del error en la consola del servidor para propósitos de depuración.
            jsonRespuesta.put("status", "error"); // Configura el estado general de la respuesta como error.
            jsonRespuesta.put("message", "Error interno: " + e.getMessage()); // Incluye el detalle de la excepción capturada dentro del JSON hacia el cliente.
      }

        out.print(jsonRespuesta.toString()); // Envía la representación de texto del objeto JSON de respuesta final al cliente.
        out.flush(); // Fuerza el vaciado inmediato del búfer de salida HTTP.
      }

      @Override
      protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doOptions para atender peticiones de verificación previa CORS (preflight).
        response.setStatus(HttpServletResponse.SC_OK); // Establece el código de estado HTTP 200 OK para aceptar peticiones preliminares del navegador.
      }

      @Override
      protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doGet para reenviar solicitudes HTTP GET hacia la lógica central en doPost.
        doPost(request, response); // Redirige los parámetros y el contexto de la petición GET para ser gestionados por doPost.
      }
}