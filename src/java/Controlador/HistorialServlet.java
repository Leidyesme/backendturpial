// Definir el paquete al que pertenece este servlet de control
package Controlador;

// Importar el DAO para interactuar con la base de datos de historial
import DAO.HistorialDAO;
// Importar la entidad Historial para representar los datos de un pedido
import Modelo.Entidades.Historial;
// Importar BufferedReader para leer el flujo de datos del request
import java.io.BufferedReader;
// Importar IOException para la gestión de errores de E/S
import java.io.IOException;
// Importar PrintWriter para escribir respuestas al cliente
import java.io.PrintWriter;
// Importar List para almacenar colecciones de elementos de historial
import java.util.List;
// Importar ServletException para la gestión de excepciones de servlet
import jakarta.servlet.ServletException;
// Importar la anotación WebServlet para mapear la ruta del servlet
import jakarta.servlet.annotation.WebServlet;
// Importar HttpServlet como clase base del servlet
import jakarta.servlet.http.HttpServlet;
// Importar HttpServletRequest para recibir peticiones HTTP
import jakarta.servlet.http.HttpServletRequest;
// Importar HttpServletResponse para emitir respuestas HTTP
import jakarta.servlet.http.HttpServletResponse;
// Importar JSONObject para manejar objetos JSON
import org.json.JSONObject;
// Importar JSONArray para manejar arreglos de objetos JSON
import org.json.JSONArray;

// Mapear la ruta URL del servlet como /HistorialServlet
@WebServlet("/HistorialServlet")
// Clase principal del servlet de historial de pedidos
public class HistorialServlet extends HttpServlet {

    // Instancia del DAO de historial para persistir y consultar datos
    private final HistorialDAO historialDao = new HistorialDAO();

    // Sobrescribir el método doPost para procesar peticiones HTTP POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            // Declaración de excepciones asociadas
            throws ServletException, IOException {

        // Configurar cabeceras CORS para permitir peticiones desde cualquier origen
        response.setHeader("Access-Control-Allow-Origin", "*");
        // Permitir métodos específicos en llamadas cruzadas
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        // Habilitar cabeceras para la comunicación de JSON
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        // Declarar el tipo de respuesta del servlet como JSON
        response.setContentType("application/json");
        // Especificar la codificación de la salida a UTF-8
        response.setCharacterEncoding("UTF-8");
        // Obtener el manejador de salida para escribir la respuesta
        PrintWriter out = response.getWriter();

        // Obtener el parámetro 'accion' desde la URL
        String accion = request.getParameter("accion");

        // Instanciar un objeto de respuesta JSON
        JSONObject jsonRespuesta = new JSONObject();

        // Bloque try-catch para capturar errores de ejecución
        try {
            // Leer el contenido JSON del cuerpo de la petición
            StringBuilder sb = new StringBuilder();
            // Variable temporal para guardar las líneas leídas
            String linea;
            // Usar try-with-resources para asegurar el cierre de Reader
            try (BufferedReader reader = request.getReader()) {
                // Iterar hasta leer todo el contenido del cuerpo
                while ((linea = reader.readLine()) != null) {
                    // Adjuntar línea al buffer
                    sb.append(linea);
                }
            }
            // Asignar el contenido a un String final
            String cuerpoPeticion = sb.toString();

            // Evaluar si la acción es registrar un nuevo pedido
            if ("registrar".equals(accion)) {
                // Convertir el cuerpo leído a un JSONObject
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                // Obtener el ID del usuario que realiza la compra
                String idUsuario = jsonEntrada.getString("idUsuario");
                // Obtener el monto total del pedido
                double total = jsonEntrada.getDouble("total");
                // Obtener el listado de productos del carrito como JSONArray
                JSONArray productos = jsonEntrada.getJSONArray("products");

                // Instanciar entidad Historial
                Historial pedido = new Historial();
                // Asignar el ID de usuario a la orden
                pedido.setIdUsuario(idUsuario);
                // Asignar el total monetario a la orden
                pedido.setTotal(total);
                // Asignar el estado inicial "En proceso" a la orden
                pedido.setEstado("En proceso");

                // Llamar al DAO para registrar el pedido y sus detalles en MySQL
                boolean registrado = historialDao.registrarPedido(pedido, productos);

                // Comprobar si se completó el registro en base de datos
                if (registrado) {
                    // Marcar estado exitoso
                    jsonRespuesta.put("status", "success");
                    // Mensaje de éxito del pedido
                    jsonRespuesta.put("message", "Pedido registrado exitosamente en la base de datos.");
                } else {
                    // Marcar estado de error
                    jsonRespuesta.put("status", "error");
                    // Mensaje informativo del error al guardar en la BD
                    jsonRespuesta.put("message", "No se pudo registrar el pedido en la base de datos.");
                }
            }
            // Evaluar si la acción es listar los pedidos del usuario
            else if ("listar".equals(accion)) {
                // Parsear la cadena JSON entrante
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                // Obtener el ID del usuario del JSON
                String idUsuario = jsonEntrada.getString("idUsuario");

                // Obtener la lista de pedidos del usuario usando el DAO
                List<Historial> lista = historialDao.obtenerHistorialUsuario(idUsuario);

                // Instanciar un JSONArray para almacenar el listado en formato JSON
                JSONArray arrayPedidos = new JSONArray();
                // Recorrer cada pedido en la lista obtenida
                for (Historial h : lista) {
                    // Crear un objeto JSON por cada pedido individual
                    JSONObject item = new JSONObject();
                    // Agregar el ID del pedido al objeto
                    item.put("idPedido", h.getIdPedido());
                    // Agregar el ID del usuario al objeto
                    item.put("idUsuario", h.getIdUsuario());
                    // Agregar la fecha del pedido al objeto
                    item.put("date", h.getFecha());
                    // Agregar el total monetario al objeto
                    item.put("total", h.getTotal());
                    // Agregar el estado de entrega al objeto
                    item.put("status", h.getEstado());
                    // Insertar el objeto de pedido en el JSONArray
                    arrayPedidos.put(item);
                }

                // Asignar estado exitoso a la respuesta principal
                jsonRespuesta.put("status", "success");
                // Anidar el listado de pedidos en la respuesta
                jsonRespuesta.put("orders", arrayPedidos);
            }
            // Manejar acción no válida
            else {
                // Marcar estado de error
                jsonRespuesta.put("status", "error");
                // Mensaje indicando acción inválida
                jsonRespuesta.put("message", "Acción no reconocida.");
            }
        } catch (Exception e) {
            // Asignar estado de error general
            jsonRespuesta.put("status", "error");
            // Mensaje informativo del error del sistema
            jsonRespuesta.put("message", "Error interno en el servlet: " + e.getMessage());
        }

        // Retornar la respuesta JSON construida al cliente
        out.print(jsonRespuesta.toString());
    }

    // Sobrescribir el método doOptions para el preflight de CORS
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            // Declaración de excepciones asociadas
            throws ServletException, IOException {
        // Habilitar acceso de origen cruzado
        response.setHeader("Access-Control-Allow-Origin", "*");
        // Habilitar métodos HTTP específicos
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        // Habilitar cabeceras requeridas para comunicación JSON
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        // Asignar estado 200 OK
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Sobrescribir el método doGet para reenviar la petición
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            // Declaración de excepciones asociadas
            throws ServletException, IOException {
        // Delegar peticiones GET a doPost (opcional en historial por si se requiere listar mediante parámetros URL)
        doPost(request, response);
    }
}
