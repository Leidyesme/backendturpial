package Controlador;

// Importar la clase DAO para acceder a los datos de los pedidos
import DAO.PedidoDAO;
// Importar la entidad Pedido para manejar los datos del pedido
import Modelo.Entidades.Pedido;
// Importar ServletException para la gestión de excepciones de servlets
import jakarta.servlet.ServletException;
// Importar la anotación WebServlet para mapear el servlet a una URL
import jakarta.servlet.annotation.WebServlet;
// Importar HttpServlet para extender la clase base del servlet
import jakarta.servlet.http.HttpServlet;
// Importar HttpServletRequest para recibir la petición del cliente
import jakarta.servlet.http.HttpServletRequest;
// Importar HttpServletResponse para enviar la respuesta al cliente
import jakarta.servlet.http.HttpServletResponse;
// Importar IOException para manejar los errores de entrada y salida
import java.io.IOException;
// Importar PrintWriter para escribir la respuesta al cliente
import java.io.PrintWriter;
// Importar BufferedReader para leer el cuerpo de las peticiones
import java.io.BufferedReader;
// Importar List para almacenar colecciones de pedidos
import java.util.List;
// Importar JSONObject para el manejo estructurado y seguro de datos JSON
import org.json.JSONObject;
// Importar JSONArray para representar colecciones estructuradas en formato JSON
import org.json.JSONArray;

/**
 * Servlet encargado de manejar las peticiones relacionadas con los pedidos.
 * Procesa listados mediante solicitudes GET y creaciones de pedidos mediante POST.
 */
@WebServlet("/pedido")
public class PedidoServlet extends HttpServlet {

    // Instanciar el DAO de pedidos para interactuar con la base de datos
    private final PedidoDAO dao = new PedidoDAO();

    /**
     * Procesa peticiones HTTP GET para retornar el listado completo de pedidos.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar el tipo de respuesta a JSON y codificación UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Obtener la lista de pedidos desde la base de datos a través del DAO
        List<Pedido> lista = dao.listar();

        // Instanciar un JSONArray para construir la respuesta estructurada de forma segura
        JSONArray jsonArray = new JSONArray();

        // Iterar sobre cada pedido recuperado y convertirlo a un JSONObject
        for (Pedido p : lista) {
            JSONObject jsonItem = new JSONObject();
            // Asignar identificador del pedido
            jsonItem.put("idPedido", p.getIdPedido());
            // Asignar ID de usuario (manejar opcionalidad con cadena vacía si es nulo)
            jsonItem.put("idUsuario", p.getIdUsuario() != null ? p.getIdUsuario() : "");
            // Asignar nombre del cliente no registrado
            jsonItem.put("nombreClienteOpcional", p.getNombreClienteOpcional() != null ? p.getNombreClienteOpcional() : "");
            // Asignar el tipo de entrega
            jsonItem.put("tipoEntrega", p.getTipoEntrega());
            // Asignar el número de mesa (si es nulo, registrar NULL formal en JSON)
            jsonItem.put("numeroMesa", p.getNumeroMesa() != null ? p.getNumeroMesa() : JSONObject.NULL);
            // Asignar la dirección de entrega
            jsonItem.put("direccionEntrega", p.getDireccionEntrega() != null ? p.getDireccionEntrega() : "");
            // Asignar observaciones del pedido
            jsonItem.put("observaciones", p.getObservaciones() != null ? p.getObservaciones() : "");
            // Asignar el total monetario
            jsonItem.put("total", p.getTotal());
            // Asignar el estado del pedido
            jsonItem.put("estado", p.getEstado());
            // Asignar la fecha del registro del pedido
            jsonItem.put("fechaPedido", p.getFechaPedido() != null ? p.getFechaPedido() : "");

            // Agregar el objeto de pedido al arreglo
            jsonArray.put(jsonItem);
        }

        // Obtener the PrintWriter del HttpServletResponse para enviar la respuesta
        PrintWriter out = response.getWriter();
        // Serializar el arreglo JSON a cadena y escribirlo
        out.print(jsonArray.toString());
        // Forzar el envío de los datos en el buffer de salida
        out.flush();
    }

    /**
     * Procesa peticiones HTTP POST para registrar un nuevo pedido.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar el tipo de respuesta a JSON y codificación UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Leer el cuerpo de la petición HTTP (flujo de entrada JSON)
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }

            // Convertir la cadena acumulada a un objeto JSONObject
            JSONObject json = new JSONObject(jsonBuilder.toString());

            // Instanciar un nuevo objeto Pedido
            Pedido p = new Pedido();

            // Extraer y configurar propiedades del pedido desde el objeto JSON mapeado
            p.setIdUsuario(json.optString("idUsuario", null));
            p.setNombreClienteOpcional(json.getString("nombreClienteOpcional"));
            p.setTipoEntrega(json.getString("tipoEntrega"));

            // Configurar número de mesa manejando nulidades en el JSON
            if (!json.isNull("numeroMesa")) {
                p.setNumeroMesa(json.getInt("numeroMesa"));
            } else {
                p.setNumeroMesa(null);
            }

            // Configurar dirección y observaciones con valores por defecto en caso de nulidad
            p.setDireccionEntrega(json.optString("direccionEntrega", null));
            p.setObservaciones(json.optString("observaciones", ""));
            p.setTotal(json.getDouble("total"));
            
            // Definir estado inicial del pedido
            p.setEstado("En preparación");

            // VALIDACIÓN DE NEGOCIO: Si es consumo local, requiere obligatoriamente el número de mesa
            if ("Para consumir aquí".equalsIgnoreCase(p.getTipoEntrega()) && p.getNumeroMesa() == null) {
                JSONObject errorRes = new JSONObject();
                errorRes.put("status", "error");
                errorRes.put("message", "Ingrese número de mesa");
                response.getWriter().write(errorRes.toString());
                return;
            }

            // Imprimir logs informativos en la consola del servidor
            System.out.println("Procesando pedido para: " + p.getNombreClienteOpcional());
            System.out.println("Total del pedido: " + p.getTotal());

            JSONArray productos = json.optJSONArray("products");

            // Invocar el DAO para registrar el pedido
            boolean registrado = dao.registrar(p, productos);

            // Preparar respuesta JSON estructurada según el resultado
            JSONObject resultRes = new JSONObject();
            if (registrado) {
                resultRes.put("status", "success");
                resultRes.put("message", "Pedido registrado correctamente");
            } else {
                resultRes.put("status", "error");
                resultRes.put("message", "Error registrando pedido");
            }
            
            // Retornar la respuesta al cliente
            response.getWriter().write(resultRes.toString());

        } catch (Exception e) {
            // Registrar la traza del error en los logs del servidor
            e.printStackTrace();
            
            // Construir respuesta de error segura en formato JSON
            JSONObject errorRes = new JSONObject();
            errorRes.put("status", "error");
            errorRes.put("message", "Error procesando el pedido: " + e.getMessage());
            response.getWriter().write(errorRes.toString());
        }
    }
}