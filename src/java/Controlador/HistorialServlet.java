package Controlador; // Define el paquete al que pertenece la clase en la capa de controladores.

import DAO.HistorialDAO; // Importa la clase de acceso a datos para interactuar con el historial de pedidos en MySQL.
import DAO.UsuarioDAO; // Importa la clase de acceso a datos para consultar información y roles de usuario en MySQL.
import Modelo.Entidades.Historial; // Importa la entidad de modelo que representa un pedido en el historial.
import Modelo.Entidades.Usuario; // Importa la entidad de modelo que representa a un usuario del sistema.
import java.io.BufferedReader; // Importa la clase BufferedReader para leer el flujo de datos de texto de la petición HTTP.
import java.io.IOException; // Importa la excepción para la gestión de errores de entrada y salida (E/S).
import java.io.PrintWriter; // Importa la clase PrintWriter para escribir respuestas de texto/JSON hacia el cliente.
import java.util.List; // Importa la interfaz List para manejar colecciones de elementos de historial.
import jakarta.servlet.ServletException; // Importa la excepción para la gestión de errores específicos de servlets.
import jakarta.servlet.annotation.WebServlet; // Importa la anotación @WebServlet para mapear la ruta URL del servlet.
import jakarta.servlet.http.HttpServlet; // Importa la clase base HttpServlet para crear servlets HTTP.
import jakarta.servlet.http.HttpServletRequest; // Importa la interfaz HttpServletRequest para procesar peticiones HTTP del cliente.
import jakarta.servlet.http.HttpServletResponse; // Importar HttpServletResponse para emitir respuestas HTTP.
import org.json.JSONObject; // Importar JSONObject para manejar objetos JSON.
import org.json.JSONArray; // Importar JSONArray para manejar arreglos de objetos JSON.

/**
 * Servlet encargado de administrar el historial de pedidos de los usuarios.
 * Recibe peticiones para registrar pedidos en lote y listar pedidos históricos.
 */
@WebServlet("/HistorialServlet") // Mapea este servlet a la ruta URL "/HistorialServlet" para recibir las peticiones del frontend.
public class HistorialServlet extends HttpServlet { // Declara la clase pública HistorialServlet extendiendo de la clase base HttpServlet.

    // Instancia del DAO de historial para persistir y consultar datos
    private final HistorialDAO historialDao = new HistorialDAO(); // Instancia el DAO para comunicarse con las tablas de historial y pedidos en MySQL.

    /**
     * Procesa peticiones HTTP POST y GET delegadas.
     * Soporta lectura de parámetros desde el cuerpo JSON o mediante parámetros de URL (fallback).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doPost para gestionar las solicitudes HTTP POST entrantes.

        // NOTA DE PORTABILIDAD Y SEGURIDAD: Las cabeceras CORS manuales (Access-Control-Allow-Origin, etc.)
        // han sido removidas de este servlet, ya que son inyectadas de forma global por 'CorsFilter.java'.
        // Mantenerlas aquí causaba cabeceras duplicadas ("*, *"), provocando que los navegadores modernos bloquearan las peticiones.

        // Declarar el tipo de respuesta del servlet como JSON y codificación UTF-8
        response.setContentType("application/json"); // Establece el tipo de contenido MIME de la respuesta HTTP como formato JSON.
        response.setCharacterEncoding("UTF-8"); // Configura la codificación de caracteres en UTF-8 para la respuesta HTTP.
        
        // Obtener el PrintWriter para emitir la respuesta JSON al cliente
        PrintWriter out = response.getWriter(); // Obtiene el objeto PrintWriter para enviar datos estructurados de vuelta al cliente.

        // Obtener el parámetro 'accion' desde la URL
        String accion = request.getParameter("accion"); // Extrae el parámetro "accion" enviado por la URL o query string para determinar la ruta lógica.

        // Instanciar el objeto JSON que contendrá la respuesta final
        JSONObject jsonRespuesta = new JSONObject(); // Crea un objeto JSON vacío para estructurar la respuesta hacia el frontend.

        try {
            // Leer el contenido del cuerpo de la petición (JSON)
            StringBuilder sb = new StringBuilder(); // Inicializa un StringBuilder para acumular las líneas del cuerpo de la petición.
            String linea; // Declara una variable temporal para almacenar cada línea leída del flujo.
            try (BufferedReader reader = request.getReader()) { // Obtiene el lector del flujo de entrada de la solicitud HTTP.
                while ((linea = reader.readLine()) != null) { // Lee de forma iterativa cada línea del cuerpo de la petición.
                    sb.append(linea); // Concatena cada línea leída en el StringBuilder.
                }
            }
            
            // Asignar el cuerpo de la petición a un String
            String cuerpoPeticion = sb.toString(); // Convierte el contenido acumulado del cuerpo de la petición a una cadena de texto.

            // MÓDULO 1: REGISTRAR UN NUEVO PEDIDO
            if ("registrar".equals(accion)) { // Evalúa si la acción solicitada corresponde a "registrar".
                // Validar que el cuerpo no esté vacío para el registro
                if (cuerpoPeticion.trim().isEmpty()) { // Comprueba si el cuerpo de la petición JSON está vacío.
                    jsonRespuesta.put("status", "error"); // Asigna el estado de error en el objeto JSON de respuesta.
                    jsonRespuesta.put("message", "El cuerpo de la petición está vacío. Se requiere un objeto JSON."); // Asigna un mensaje descriptivo del error al JSON.
                    out.print(jsonRespuesta.toString()); // Envía el JSON de respuesta de error hacia el cliente.
                    return; // Interrumpe la ejecución del método.
                }

                // Convertir el cuerpo leído a un JSONObject de entrada
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion); // Parsea la cadena del cuerpo a un objeto JSON para facilitar la extracción de datos.
                
                // Extraer atributos obligatorios para el registro
                String idUsuario = jsonEntrada.optString("idUsuario", null); // Extrae de manera segura la propiedad idUsuario del JSON.
                double total = jsonEntrada.optDouble("total", 0.0); // Extrae el costo total del pedido del JSON.
                // Obtener el listado de productos del carrito (soporta 'products' y 'productos')
                JSONArray productos = jsonEntrada.optJSONArray("products"); // Intenta extraer el arreglo de productos bajo la clave "products".
                if (productos == null || productos.isEmpty()) { // Comprueba si no se encontraron productos con la clave anterior.
                    productos = jsonEntrada.optJSONArray("productos"); // Intenta extraer el arreglo alternativo bajo la clave "productos".
                }

                // Extraer información de entrega adicional enviada por el cliente
                String tipoEntrega = jsonEntrada.optString("tipoEntrega", "Para consumir aquí"); // Extrae el tipo de entrega con un valor predeterminado.
                String direccion = jsonEntrada.optString("direccion", null); // Extrae la dirección de entrega del JSON.
                Integer numeroMesa = null; // Declara la variable para almacenar el número de mesa.
                if (!jsonEntrada.isNull("numeroMesa")) { // Verifica que el campo numeroMesa no sea nulo en el JSON.
                    String mesaStr = String.valueOf(jsonEntrada.get("numeroMesa")).trim(); // Convierte el valor de la mesa a una cadena de texto recortada.
                    if (!mesaStr.isEmpty() && !mesaStr.equalsIgnoreCase("null")) { // Valida que la cadena no esté vacía ni sea literalmente "null".
                        try {
                            numeroMesa = Integer.parseInt(mesaStr); // Intenta convertir el texto de la mesa a un valor entero.
                        } catch (NumberFormatException e) {
                            System.err.println("Error parseando número de mesa en HistorialServlet: " + e.getMessage()); // Registra el error de conversión numérica en la consola estándar de errores.
                        }
                    }
                }

                // Instanciar entidad Historial
                Historial pedido = new Historial(); // Instancia un nuevo objeto de la entidad de modelo Historial.
                pedido.setIdUsuario(idUsuario); // Asigna el ID de usuario a la entidad de modelo.
                pedido.setTotal(total); // Asigna el valor total del pedido a la entidad de modelo.
                pedido.setEstado("En preparación"); // Define el estado inicial estándar del pedido en la entidad.

                // Guardar la información compuesta en base de datos mediante el DAO (obteniendo diagnóstico detallado)
                String errorRegistro = historialDao.registrarPedidoDetallado(pedido, productos, tipoEntrega, numeroMesa, direccion); // Invoca al DAO para persistir el pedido completo y sus detalles transaccionalmente en MySQL.

                // Responder según el resultado de la transacción
                if (errorRegistro == null) { // Comprueba si el DAO retornó un error nulo, indicando éxito en la persistencia.
                    jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta.
                    jsonRespuesta.put("message", "Pedido registrado exitosamente en la base de datos."); // Configura el mensaje de éxito para el cliente.
                } else {
                    jsonRespuesta.put("status", "error"); // Configura el estado de error si el DAO devolvió un mensaje de fallo.
                    jsonRespuesta.put("message", errorRegistro); // Inserta el mensaje de error detallado devuelto por la capa de datos.
                }
            }
            // MÓDULO 2: LISTAR EL HISTORIAL DE PEDIDOS
            else if ("listar".equals(accion)) { // Evalúa si la acción solicitada corresponde a listar el historial de pedidos.
                String idUsuario = null; // Inicializa la variable para almacenar el ID de usuario.

                // Validar si el cuerpo de la petición contiene información
                if (!cuerpoPeticion.trim().isEmpty()) { // Verifica si el cuerpo JSON no está vacío.
                    // Parsear el JSON para extraer el idUsuario
                    JSONObject jsonEntrada = new JSONObject(cuerpoPeticion); // Parsea el cuerpo de la petición a un objeto JSON.
                    idUsuario = jsonEntrada.optString("idUsuario", null); // Extrae de manera segura la propiedad idUsuario del JSON.
                }
                
                // FALLBACK: Si no vino en el cuerpo JSON, intentar leer el idUsuario desde la URL (soporte para GET)
                if (idUsuario == null || idUsuario.isEmpty()) { // Comprueba si el ID de usuario sigue ausente tras revisar el cuerpo.
                    idUsuario = request.getParameter("idUsuario"); // Intenta obtener el ID de usuario directamente desde los parámetros de la URL.
                }

                // Si no se proporcionó idUsuario en ninguna de las vías, responder con error
                if (idUsuario == null || idUsuario.isEmpty()) { // Valida si el ID de usuario sigue sin encontrarse en ninguna fuente.
                    jsonRespuesta.put("status", "error"); // Configura el estado de error por falta de parámetros obligatorios.
                    jsonRespuesta.put("message", "Se requiere el parámetro 'idUsuario'."); // Agrega el mensaje descriptivo del error al JSON.
                    out.print(jsonRespuesta.toString()); // Envía el JSON de respuesta de error hacia el cliente.
                    return; // Interrumpe la ejecución del método.
                }

                // CONTROL DE ACCESO BASADO EN ROLES (RBAC):
                // Justificación de negocio:
                // - Administrador ("ROL-001"): Tiene permiso para auditar/ver todos los pedidos realizados en el sistema.
                // - Cliente ("ROL-003"): Solo puede visualizar sus propios pedidos.
                // - Empleado ("ROL-002"): No tiene acceso al módulo de historial de pedidos.
                UsuarioDAO usuarioDao = new UsuarioDAO(); // Instancia el DAO de usuarios para consultar roles en la base de datos MySQL.
                Usuario user = usuarioDao.obtenerUsuarioPorId(idUsuario); // Consulta los datos del usuario mediante el DAO.

                List<Historial> lista; // Declara una lista para almacenar los registros de historial recuperados.
              	if (user == null) { // Valida si el usuario consultado no existe en la base de datos.
                  	jsonRespuesta.put("status", "error"); // Configura el estado de error en la respuesta.
                  	jsonRespuesta.put("message", "Usuario no encontrado."); // Especifica el mensaje indicando que el usuario no existe.
                  	out.print(jsonRespuesta.toString()); // Envía el JSON de respuesta al cliente.
                  	return; // Interrumpe la ejecución del método.
              	}

              	if ("ROL-001".equals(user.getIdRol())) { // Comprueba si el usuario tiene rol de administrador (ROL-001).
                  	lista = historialDao.obtenerTodosLosPedidos(); // Invoca al DAO para obtener la lista de todos los pedidos registrados en MySQL.
              	} else if ("ROL-003".equals(user.getIdRol())) { // Comprueba si el usuario tiene rol de cliente (ROL-003).
                  	lista = historialDao.obtenerHistorialUsuario(idUsuario); // Invoca al DAO para obtener únicamente los pedidos del usuario específico desde MySQL.
              	} else {
                  	jsonRespuesta.put("status", "error"); // Configura el estado de error por falta de permisos del rol.
                  	jsonRespuesta.put("message", "Acceso denegado: El rol asignado no cuenta con permisos para ver historiales de pedidos."); // Agrega mensaje explicativo del bloqueo de seguridad.
                  	out.print(jsonRespuesta.toString()); // Escribe el JSON de respuesta de error hacia el cliente.
                  	return; // Interrumpe la ejecución del método.
            	}

                // Instanciar un JSONArray para almacenar la lista en formato JSON
              	JSONArray arrayPedidos = new JSONArray(); // Crea un arreglo JSON para almacenar los objetos de pedido transformados.
              	for (Historial h : lista) { // Itera sobre cada entidad Historial obtenida de la base de datos.
                  	JSONObject item = new JSONObject(); // Instancia un objeto JSON para representar cada pedido individual.
                  	item.put("idPedido", h.getIdPedido()); // Inserta el ID de pedido en el objeto JSON.
                  	item.put("idUsuario", h.getIdUsuario()); // Inserta el ID de usuario en el objeto JSON.
                  	item.put("date", h.getFecha()); // Inserta la fecha del pedido en el objeto JSON.
                  	item.put("total", h.getTotal()); // Inserta el total del pedido en el objeto JSON.
                  	item.put("status", h.getEstado()); // Inserta el estado del pedido en el objeto JSON.
                  	item.put("tipoEntrega", h.getTipoEntrega() != null ? h.getTipoEntrega() : ""); // Inserta el tipo de entrega validando que no sea nulo.
                  	item.put("estadoPago", h.getEstadoPago() != null ? h.getEstadoPago() : "Sin pagar"); // Inserta el estado de pago asignando un valor por defecto si es nulo.
                  	item.put("customerName", h.getCustomerName() != null ? h.getCustomerName() : "Cliente Anónimo"); // Inserta el nombre del cliente con un valor predeterminado si es nulo.
                  	arrayPedidos.put(item); // Añade el objeto JSON individual al arreglo general de pedidos.
            	}

                // Construir respuesta exitosa con la colección integrada
              	jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta general.
              	jsonRespuesta.put("orders", arrayPedidos); // Inserta el arreglo JSON con los pedidos obtenidos hacia el cliente.
            }
            // ACCIÓN NO CONTROLADA
          	else {
              	jsonRespuesta.put("status", "error"); // Configura el estado de error para acciones no reconocidas.
              	jsonRespuesta.put("message", "Acción no reconocida."); // Establece el mensaje de advertencia correspondiente.
          	}
        } catch (Exception e) {
          	// Registrar excepción en la consola del servidor
          	e.printStackTrace(); // Imprime la traza completa de la excepción en la consola de errores del servidor para propósitos de depuración.
            
          	// Construir respuesta de error interna
          	jsonRespuesta.put("status", "error"); // Configura el estado global de la respuesta como error.
          	jsonRespuesta.put("message", "Error interno en el servlet: " + e.getMessage()); // Incluye el detalle de la excepción capturada dentro del JSON hacia el cliente.
      	}

      	// Retornar la respuesta JSON construida al cliente
    	out.print(jsonRespuesta.toString()); // Escribe la representación de texto del objeto JSON de respuesta final en el flujo de salida HTTP.
    	out.flush(); // Fuerza el vaciado inmediato del búfer de salida HTTP hacia el cliente.
    }

    /**
     * Maneja las peticiones Preflight de CORS delegando al filtro o respondiendo OK.
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doOptions para atender peticiones de verificación previa CORS (preflight).
      	// Responder con un estado HTTP 200 OK, delegando las cabeceras al filtro global
      	response.setStatus(HttpServletResponse.SC_OK); // Establece el código de estado HTTP 200 OK para aceptar peticiones preliminares del navegador.
    }

    /**
     * Procesa peticiones HTTP GET delegándolas al método doPost para compatibilidad
     * con la recuperación de historial directa desde parámetros en la URL.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doGet para reenviar solicitudes HTTP GET hacia la lógica central en doPost.
      	// Redirigir el procesamiento al método doPost de forma segura
      	doPost(request, response); // Redirige los parámetros y el contexto de la petición GET para ser gestionados por doPost.
    }
}