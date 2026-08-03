package Controlador; // Define el paquete al que pertenece esta clase dentro de la capa de controladores.

// Importar la clase DAO para acceder a los datos de los pedidos
import DAO.PedidoDAO; // Importa la clase de acceso a datos PedidoDAO para interactuar con las tablas de pedidos en MySQL.
// Importar la entidad Pedido para manejar los datos del pedido
import Modelo.Entidades.Pedido; // Importa la entidad de modelo Pedido para mapear la información de los registros.
// Importar ServletException para la gestión de excepciones de servlets
import jakarta.servlet.ServletException; // Importa la excepción para el manejo de errores propios de servlets en el contenedor web.
// Importar la anotación WebServlet para mapear el servlet a una URL
import jakarta.servlet.annotation.WebServlet; // Importa la anotación @WebServlet para definir la ruta URL de acceso al servlet.
// Importar HttpServlet para extender la clase base del servlet
import jakarta.servlet.http.HttpServlet; // Importa la clase base HttpServlet para proveer el ciclo de vida del servlet HTTP.
// Importar HttpServletRequest para recibir la petición del cliente
import jakarta.servlet.http.HttpServletRequest; // Importa la interfaz HttpServletRequest para procesar los datos de la solicitud HTTP del cliente.
// Importar HttpServletResponse para enviar la respuesta al cliente
import jakarta.servlet.http.HttpServletResponse; // Importa la interfaz HttpServletResponse para configurar y enviar la respuesta HTTP.
// Importar IOException para manejar los errores de entrada y salida
import java.io.IOException; // Importa la clase IOException para el manejo de excepciones de operaciones de E/S.
// Importar PrintWriter para escribir la respuesta al cliente
import java.io.PrintWriter; // Importa la clase PrintWriter para emitir texto o respuestas estructuradas hacia el cliente.
// Importar BufferedReader para leer el cuerpo de las peticiones
import java.io.BufferedReader; // Importa la clase BufferedReader para leer flujos de texto del cuerpo de peticiones HTTP.
// Importar List para almacenar colecciones de pedidos
import java.util.List; // Importa la interfaz List para manejar colecciones genéricas de objetos Pedido.
// Importar JSONObject para el manejo estructurado y seguro de datos JSON
import org.json.JSONObject; // Importa la clase JSONObject para la manipulación de objetos en formato JSON.
// Importar JSONArray para representar colecciones estructuradas en formato JSON
import org.json.JSONArray; // Importa la clase JSONArray para manejar colecciones de objetos JSON.

/**
 * Servlet encargado de manejar las peticiones relacionadas con los pedidos.
 * Procesa listados mediante solicitudes GET y creaciones de pedidos mediante POST.
 */
@WebServlet("/pedido") // Mapea el servlet a la ruta URL "/pedido" para recibir las peticiones del frontend.
public class PedidoServlet extends HttpServlet { // Declara la clase pública PedidoServlet extendiendo de HttpServlet.

    // Instanciar el DAO de pedidos para interactuar con la base de datos
    private final PedidoDAO dao = new PedidoDAO(); // Instancia el objeto PedidoDAO para realizar consultas y modificaciones en la base de datos MySQL.

    /**
     * Procesa peticiones HTTP GET para retornar el listado completo de pedidos.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Sobrescribe el método doGet para manejar solicitudes HTTP GET entrantes.

        // Configurar el tipo de respuesta a JSON y codificación UTF-8
        response.setContentType("application/json"); // Establece el tipo MIME de la respuesta HTTP como formato JSON.
        response.setCharacterEncoding("UTF-8"); // Configura la codificación de caracteres en UTF-8 para la respuesta.
        PrintWriter out = response.getWriter(); // Obtiene el objeto PrintWriter para enviar la respuesta estructurada al cliente.

        // Leer parámetro idPedido de la URL
        String idPedido = request.getParameter("idPedido"); // Extrae el parámetro "idPedido" enviado mediante la cadena de consulta (query string) de la URL.

        if (idPedido != null && !idPedido.trim().isEmpty()) { // Evalúa si se proporcionó un ID de pedido válido para consulta individual.
            System.out.println("[INFO - PedidoServlet] doGet recibido con idPedido=" + idPedido); // Registra en la consola del servidor un mensaje informativo sobre la consulta del pedido.
            // Obtener el pedido completo con sus productos desde la base de datos
            JSONObject pedidoJson = dao.obtenerPedidoConProductos(idPedido); // Consulta al DAO de pedidos para obtener el detalle completo y sus productos asociados desde MySQL.
          	if (pedidoJson != null) { // Verifica si el objeto JSON del pedido fue encontrado exitosamente.
            	    out.print(pedidoJson.toString()); // Envía el JSON con la información detallada del pedido de vuelta al cliente.
          	} else {
          	    JSONObject errorJson = new JSONObject(); // Instancia un objeto JSON para estructurar la respuesta de error.
          	    errorJson.put("status", "error"); // Asigna el valor "error" al atributo status del JSON.
          	    errorJson.put("message", "Pedido no encontrado"); // Asigna el mensaje descriptivo de error al JSON.
          	    out.print(errorJson.toString()); // Envía la respuesta de error en formato JSON al cliente.
        	    }
    	} else {
    	    System.out.println("[INFO - PedidoServlet] doGet listando todos los pedidos"); // Registra en consola el inicio del proceso de listado general de pedidos.
    	    // Obtener la lista de pedidos desde la base de datos a través del DAO
    	    List<Pedido> lista = dao.listar(); // Invoca al DAO para recuperar la colección completa de entidades Pedido desde MySQL.

    	    // Instanciar un JSONArray para construir la respuesta estructurada de forma segura
    	    JSONArray jsonArray = new JSONArray(); // Crea un arreglo JSON vacío para almacenar los pedidos transformados.

    	    // Iterar sobre cada pedido recuperado y convertirlo a un JSONObject
    	    for (Pedido p : lista) { // Itera sobre cada elemento de la lista de pedidos obtenidos de la base de datos.
    		    JSONObject jsonItem = new JSONObject(); // Crea un objeto JSON para representar los atributos de cada pedido individual.
    		    // Asignar identificador del pedido
    		    jsonItem.put("idPedido", p.getIdPedido()); // Asigna el ID del pedido al objeto JSON.
    		    // Asignar ID de usuario (manejar opcionalidad con cadena vacía si es nulo)
    		    jsonItem.put("idUsuario", p.getIdUsuario() != null ? p.getIdUsuario() : ""); // Asigna el ID de usuario validando nulos hacia el JSON.
    		    // Asignar nombre del cliente no registrado o nombre de usuario
    		    jsonItem.put("nombreClienteOpcional", p.getNombreClienteOpcional() != null ? p.getNombreClienteOpcional() : ""); // Asigna el nombre opcional del cliente validando nulos.
    		    // Asignar el tipo de entrega
    		    jsonItem.put("tipoEntrega", p.getTipoEntrega()); // Asigna el tipo de entrega al objeto JSON.
    		    // Asignar el número de mesa (si es nulo, registrar NULL formal en JSON)
    		    jsonItem.put("numeroMesa", p.getNumeroMesa() != null ? p.getNumeroMesa() : JSONObject.NULL); // Asigna el número de mesa asegurando el tipo nulo en JSON si no existe.
    		    // Asignar la dirección de entrega
    		    jsonItem.put("direccionEntrega", p.getDireccionEntrega() != null ? p.getDireccionEntrega() : ""); // Asigna la dirección de entrega validando valores nulos.
    		    // Asignar observaciones del pedido
    		    jsonItem.put("observaciones", p.getObservaciones() != null ? p.getObservaciones() : ""); // Asigna las observaciones del pedido validando nulos.
    		    // Asignar el total monetario
    		    jsonItem.put("total", p.getTotal()); // Asigna el costo total del pedido al objeto JSON.
    		    // Asignar el estado del pedido
    		    jsonItem.put("estado", p.getEstado()); // Asigna el estado actual del pedido al objeto JSON.
    		    // Asignar la fecha del registro del pedido
    		    jsonItem.put("fechaPedido", p.getFechaPedido() != null ? p.getFechaPedido() : ""); // Asigna la fecha de registro validando nulos.
    		    // Asignar el estado de pago del pedido ('Pagado' o 'Sin pagar')
    		    jsonItem.put("estadoPago", p.getEstadoPago() != null ? p.getEstadoPago() : "Sin pagar"); // Asigna el estado de pago aplicando un valor predeterminado si es nulo.

    		    // Agregar el objeto de pedido al arreglo
    		    jsonArray.put(jsonItem); // Inserta el objeto JSON individual dentro del arreglo general de pedidos.
  	    }
  	    out.print(jsonArray.toString()); // Envía la cadena que representa el arreglo JSON con todos los pedidos hacia el cliente.
  	}
  	out.flush(); // Fuerza el vaciado inmediato del búfer de salida HTTP.
     }

     /**
      * Procesa peticiones HTTP POST para registrar un nuevo pedido.
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
  		    throws ServletException, IOException { // Sobrescribe el método doPost para procesar solicitudes HTTP POST de creación de pedidos.

  	// Configurar el tipo de respuesta a JSON y codificación UTF-8
  	response.setContentType("application/json"); // Define el tipo de contenido MIME como JSON para la respuesta POST.
  	response.setCharacterEncoding("UTF-8"); // Establece la codificación de caracteres UTF-8 en la respuesta HTTP.

  	try {
  	    // Leer el cuerpo de la petición HTTP (flujo de entrada JSON)
  	    StringBuilder jsonBuilder = new StringBuilder(); // Instancia un StringBuilder para concatenar el cuerpo de la solicitud entrante.
  	    String line; // Declara una variable para capturar cada línea leída del flujo.
  	    try (BufferedReader reader = request.getReader()) { // Obtiene el lector del flujo de datos de la petición HTTP del cliente.
  		    while ((line = reader.readLine()) != null) { // Lee de forma iterativa cada línea del cuerpo de la solicitud.
  			    jsonBuilder.append(line); // Acumula las líneas de texto leídas en el StringBuilder.
  		    }
  	    }

  	    // Convertir la cadena acumulada a un objeto JSONObject
  	    JSONObject json = new JSONObject(jsonBuilder.toString()); // Parsea la cadena acumulada a un objeto JSONObject para facilitar la lectura de parámetros.

  	    // Instanciar un nuevo objeto Pedido
  	    Pedido p = new Pedido(); // Crea una nueva instancia de la entidad de modelo Pedido.

  	    // Extraer y configurar propiedades del pedido desde el objeto JSON mapeado
  	    p.setIdUsuario(json.optString("idUsuario", null)); // Extrae y asigna el ID de usuario desde el JSON hacia la entidad.
  	    p.setNombreClienteOpcional(json.getString("nombreClienteOpcional")); // Extrae y asigna el nombre opcional del cliente en la entidad.
  	    p.setTipoEntrega(json.getString("tipoEntrega")); // Extrae y asigna el tipo de entrega en la entidad.

  	    // Configurar número de mesa manejando nulidades en el JSON
  	    if (!json.isNull("numeroMesa")) { // Verifica si el atributo numeroMesa no es nulo dentro del JSON.
  		    p.setNumeroMesa(json.getInt("numeroMesa")); // Asigna el valor entero de la mesa a la entidad de modelo.
  	    } else {
  		    p.setNumeroMesa(null); // Asigna un valor nulo a la mesa en la entidad si el JSON carece de este dato.
  	    }

  	    // Configurar dirección y observaciones con valores por defecto en caso de nulidad
  	    p.setDireccionEntrega(json.optString("direccionEntrega", null)); // Extrae y asigna la dirección de entrega de forma segura a la entidad.
  	    p.setObservaciones(json.optString("observaciones", "")); // Extrae y asigna las observaciones del pedido aplicando un valor por defecto.
  	    p.setTotal(json.getDouble("total")); // Extrae y asigna el valor monetario total del pedido a la entidad.
  		    
  	    // Definir estado inicial del pedido
  	    p.setEstado("En preparación"); // Establece por defecto el estado inicial del pedido en la entidad.

  	    // VALIDACIÓN DE NEGOCIO: Si es consumo local, requiere obligatoriamente el número de mesa
  	    if ("Para consumir aquí".equalsIgnoreCase(p.getTipoEntrega()) && p.getNumeroMesa() == null) { // Valida la regla de negocio para pedidos de consumo local sin número de mesa.
  		    JSONObject errorRes = new JSONObject(); // Crea un objeto JSON para retornar la respuesta de error de validación.
  		    errorRes.put("status", "error"); // Asigna el estado de error al JSON.
  		    errorRes.put("message", "Ingrese número de mesa"); // Asigna el mensaje de error correspondiente al JSON.
  		    response.getWriter().write(errorRes.toString()); // Escribe el JSON de error directamente en la respuesta HTTP hacia el cliente.
  		    return; // Interrumpe la ejecución del método.
  	    }

  	    // Imprimir logs informativos en la consola del servidor
  	    System.out.println("Procesando pedido para: " + p.getNombreClienteOpcional()); // Registra en la consola del servidor el nombre del cliente del pedido en curso.
  	    System.out.println("Total del pedido: " + p.getTotal()); // Registra en la consola del servidor el total monetario del pedido.

  	    JSONArray productos = json.optJSONArray("products"); // Extrae el arreglo JSON que contiene los productos asociados al pedido.

  	    // Invocar el DAO para registrar el pedido
  	    boolean registrado = dao.registrar(p, productos); // Invoca al DAO para realizar la inserción transaccional del pedido y sus productos asociados en MySQL.

  	    // Preparar respuesta JSON estructurada según el resultado
  	    JSONObject resultRes = new JSONObject(); // Crea un objeto JSON para estructurar el resultado de la operación.
  	    if (registrado) { // Comprueba si el registro en la base de datos fue exitoso.
  		    resultRes.put("status", "success"); // Configura el estado de éxito en el objeto JSON de respuesta.
  		    resultRes.put("message", "Pedido registrado correctamente"); // Asigna el mensaje de éxito correspondiente.
  	    } else {
  		    resultRes.put("status", "error"); // Configura el estado de error si el método del DAO falló.
  		    resultRes.put("message", "Error registrando pedido"); // Asigna el mensaje de error de registro en el JSON.
  	    }
  		    
  	    // Retornar la respuesta al cliente
  	    response.getWriter().write(resultRes.toString()); // Envía el JSON con el resultado de la operación hacia el cliente.

     } catch (Exception e) {
  	    // Registrar la traza del error en los logs del servidor
  	    e.printStackTrace(); // Imprime la traza completa de la excepción en la consola de errores del servidor.
  		    
  	    // Construir respuesta de error segura en formato JSON
  	    JSONObject errorRes = new JSONObject(); // Crea un objeto JSON para informar sobre la excepción capturada.
  	    errorRes.put("status", "error"); // Asigna el estado de error en el JSON.
  	    errorRes.put("message", "Error procesando el pedido: " + e.getMessage()); // Incluye el detalle técnico del error dentro del JSON de respuesta.
  	    response.getWriter().write(errorRes.toString()); // Envía el JSON de error generado hacia el cliente.
     }
    }

    /**
     * Procesa peticiones HTTP PUT para actualizar el estado de un pedido existente.
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
 		    throws ServletException, IOException { // Sobrescribe el método doPut para gestionar solicitudes HTTP PUT de actualización de pedidos.
 	response.setContentType("application/json"); // Establece el tipo MIME de respuesta como JSON.
 	response.setCharacterEncoding("UTF-8"); // Configura la codificación de caracteres en UTF-8 para la respuesta.
 	JSONObject jsonRespuesta = new JSONObject(); // Crea un objeto JSON vacío para armar la respuesta del método PUT.

 	try {
 	    StringBuilder sb = new StringBuilder(); // Inicializa un StringBuilder para acumular el cuerpo de la petición PUT.
 	    String line; // Declara la variable para capturar líneas del flujo de entrada.
 	    try (BufferedReader reader = request.getReader()) { // Obtiene el lector del flujo de datos de la solicitud PUT.
 		    while ((line = reader.readLine()) != null) { // Lee de forma iterativa cada línea del cuerpo de la petición.
 			    sb.append(line); // Concatena el contenido leído en el StringBuilder.
 		    }
 	    }
 	    String body = sb.toString(); // Convierte el contenido acumulado a una cadena de texto.

 	    if (body.trim().isEmpty()) { // Comprueba si el cuerpo de la petición PUT está vacío.
 		    jsonRespuesta.put("status", "error"); // Configura el estado de error en el objeto JSON.
 		    jsonRespuesta.put("message", "Cuerpo JSON vacío"); // Asigna el mensaje explicativo sobre el cuerpo vacío.
 		    response.getWriter().print(jsonRespuesta.toString()); // Envía la respuesta de error en formato JSON al cliente.
 		    return; // Interrumpe la ejecución del método.
 	    }

 	    JSONObject json = new JSONObject(body); // Parsea la cadena del cuerpo de la petición a un objeto JSON.
 	    String idPedido = json.getString("idPedido"); // Extrae el ID del pedido a actualizar desde el JSON.
 	    String nuevoEstado = json.optString("status", null); // Extrae opcionalmente el nuevo estado del pedido.
 	    String nuevoEstadoPago = json.optString("estadoPago", json.optString("paymentStatus", null)); // Extrae de manera flexible el nuevo estado de pago desde el JSON.
 	    String idUsuario = json.optString("idUsuario", null); // Extrae opcionalmente el ID de usuario que ejecuta la acción.

 	    System.out.println("[INFO - PedidoServlet] doPut recibido: idPedido=" + idPedido + ", estado=" + nuevoEstado + ", estadoPago=" + nuevoEstadoPago); // Registra en la consola los datos recibidos para la actualización.

 	    JSONObject pedidoExistente = dao.obtenerPedidoConProductos(idPedido); // Consulta al DAO si el pedido especificado existe en la base de datos MySQL.
 	    if (pedidoExistente == null) { // Valida si el pedido no fue encontrado en la base de datos.
 		    jsonRespuesta.put("status", "error"); // Configura el estado de error en el JSON.
 		    jsonRespuesta.put("message", "El pedido no fue encontrado."); // Asigna el mensaje de error de búsqueda en el JSON.
 		    response.getWriter().print(jsonRespuesta.toString()); // Envía la respuesta de error al cliente.
 		    return; // Interrumpe la ejecución del método.
 	    }

 	    if (idUsuario == null || idUsuario.trim().isEmpty() || idUsuario.equalsIgnoreCase("null")) { // Comprueba si el ID de usuario no fue proporcionado o es inválido.
 		    idUsuario = pedidoExistente.optString("idUsuario", "USR-001"); // Asigna un usuario por defecto utilizando los datos existentes o una clave estándar.
 	    }

 	    boolean cambioRealizado = false; // Declara un indicador booleano para constatar si se aplicó alguna modificación.

 	    if (nuevoEstadoPago != null && !nuevoEstadoPago.trim().isEmpty()) { // Evalúa si se envió un nuevo estado de pago válido para actualizar.
 		    String estadoPagoExistente = pedidoExistente.optString("estadoPago", "Sin pagar"); // Obtiene el estado de pago actual registrado del pedido.
 		    if ("Pagado".equalsIgnoreCase(estadoPagoExistente) && "Sin pagar".equalsIgnoreCase(nuevoEstadoPago)) { // Valida la regla de negocio que prohíbe revertir un pago confirmado.
 			    jsonRespuesta.put("status", "error"); // Configura el estado de error por violación de regla de negocio.
 			    jsonRespuesta.put("message", "No se puede cambiar a 'Sin pagar' un pedido que ya ha sido pagado."); // Asigna el mensaje explicativo correspondiente.
 			    response.getWriter().print(jsonRespuesta.toString()); // Envía el JSON de error al cliente.
 			    return; // Interrumpe la ejecución del método.
 		    }
 		    dao.actualizarEstadoPago(idPedido, nuevoEstadoPago); // Invoca al DAO para actualizar únicamente el estado de pago del pedido en MySQL.
 		    cambioRealizado = true; // Marca el indicador de cambios como verdadero.
 	    }

 	    if (nuevoEstado != null && !nuevoEstado.trim().isEmpty()) { // Evalúa si se proporcionó un nuevo estado de pedido para modificar.
 		    String estadoActual = pedidoExistente.getString("estado"); // Obtiene el estado operativo actual del pedido.
 		    if ("Entregado".equalsIgnoreCase(estadoActual)) { // Valida la regla de negocio que impide alterar pedidos ya entregados.
 			    jsonRespuesta.put("status", "error"); // Configura el estado de error en el JSON.
 			    jsonRespuesta.put("message", "No se puede cambiar el estado de un pedido que ya ha sido entregado."); // Asigna el mensaje explicativo correspondiente.
 			    response.getWriter().print(jsonRespuesta.toString()); // Envía la respuesta de error al cliente.
 			    return; // Interrumpe la ejecución del método.
 		    }
 		    dao.actualizarEstado(idPedido, nuevoEstado, idUsuario); // Invoca al DAO para actualizar el estado del pedido y registrar la auditoría en MySQL.
 		    cambioRealizado = true; // Marca el indicador de cambios como verdadero.
 	    }

 	    if (cambioRealizado) { // Comprueba si se ejecutó con éxito alguna modificación sobre el pedido.
 		    jsonRespuesta.put("status", "success"); // Configura el estado de éxito en el JSON de respuesta.
 		    jsonRespuesta.put("message", "Pedido actualizado exitosamente en MySQL"); // Asigna el mensaje de confirmación de actualización.
 	    } else {
 		    jsonRespuesta.put("status", "error"); // Configura el estado de error si no se enviaron campos válidos.
 		    jsonRespuesta.put("message", "No se enviaron campos válidos para actualizar"); // Asigna el mensaje descriptivo al JSON.
 	    }
    } catch (Exception e) {
 	    e.printStackTrace(); // Imprime la traza de la excepción en la consola del servidor.
 	    jsonRespuesta.put("status", "error"); // Configura el estado global de error en la respuesta JSON.
 	    jsonRespuesta.put("message", "Error interno en el servidor: " + e.getMessage()); // Incluye el detalle de la excepción capturada dentro del JSON.
    }
    response.getWriter().print(jsonRespuesta.toString()); // Envía la respuesta JSON final estructurada al cliente mediante el flujo de salida.
    }
}