// Definir el paquete al que pertenece este servlet de control
package Controlador;

// Importar el DAO para interactuar con la base de datos de historial
import DAO.HistorialDAO;
import DAO.UsuarioDAO;
import Modelo.Entidades.Historial;
import Modelo.Entidades.Usuario;
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

/**
 * Servlet encargado de administrar el historial de pedidos de los usuarios.
 * Recibe peticiones para registrar pedidos en lote y listar pedidos históricos.
 */
@WebServlet("/HistorialServlet")
public class HistorialServlet extends HttpServlet {

    // Instancia del DAO de historial para persistir y consultar datos
    private final HistorialDAO historialDao = new HistorialDAO();

    /**
     * Procesa peticiones HTTP POST y GET delegadas.
     * Soporta lectura de parámetros desde el cuerpo JSON o mediante parámetros de URL (fallback).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // NOTA DE PORTABILIDAD Y SEGURIDAD: Las cabeceras CORS manuales (Access-Control-Allow-Origin, etc.)
        // han sido removidas de este servlet, ya que son inyectadas de forma global por 'CorsFilter.java'.
        // Mantenerlas aquí causaba cabeceras duplicadas ("*, *"), provocando que los navegadores modernos bloquearan las peticiones.

        // Declarar el tipo de respuesta del servlet como JSON y codificación UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Obtener el PrintWriter para emitir la respuesta JSON al cliente
        PrintWriter out = response.getWriter();

        // Obtener el parámetro 'accion' desde la URL
        String accion = request.getParameter("accion");

        // Instanciar el objeto JSON que contendrá la respuesta final
        JSONObject jsonRespuesta = new JSONObject();

        try {
            // Leer el contenido del cuerpo de la petición (JSON)
            StringBuilder sb = new StringBuilder();
            String linea;
            try (BufferedReader reader = request.getReader()) {
                while ((linea = reader.readLine()) != null) {
                    sb.append(linea);
                }
            }
            
            // Asignar el cuerpo de la petición a un String
            String cuerpoPeticion = sb.toString();

            // MÓDULO 1: REGISTRAR UN NUEVO PEDIDO
            if ("registrar".equals(accion)) {
                // Validar que el cuerpo no esté vacío para el registro
                if (cuerpoPeticion.trim().isEmpty()) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "El cuerpo de la petición está vacío. Se requiere un objeto JSON.");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                // Convertir el cuerpo leído a un JSONObject de entrada
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                
                // Extraer atributos obligatorios para el registro
                String idUsuario = jsonEntrada.getString("idUsuario");
                double total = jsonEntrada.getDouble("total");
                // Obtener el listado de productos del carrito
                JSONArray productos = jsonEntrada.getJSONArray("products");

                // Instanciar entidad Historial
                Historial pedido = new Historial();
                pedido.setIdUsuario(idUsuario);
                pedido.setTotal(total);
                pedido.setEstado("En preparación"); // Definir estado inicial estándar

                // Guardar la información compuesta en base de datos mediante el DAO
                boolean registrado = historialDao.registrarPedido(pedido, productos);

                // Responder según el resultado de la transacción
                if (registrado) {
                    jsonRespuesta.put("status", "success");
                    jsonRespuesta.put("message", "Pedido registrado exitosamente en la base de datos.");
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "No se pudo registrar el pedido en la base de datos.");
                }
            }
            // MÓDULO 2: LISTAR EL HISTORIAL DE PEDIDOS
            else if ("listar".equals(accion)) {
                String idUsuario = null;

                // Validar si el cuerpo de la petición contiene información
                if (!cuerpoPeticion.trim().isEmpty()) {
                    // Parsear el JSON para extraer el idUsuario
                    JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                    idUsuario = jsonEntrada.optString("idUsuario", null);
                }
                
                // FALLBACK: Si no vino en el cuerpo JSON, intentar leer el idUsuario desde la URL (soporte para GET)
                if (idUsuario == null || idUsuario.isEmpty()) {
                    idUsuario = request.getParameter("idUsuario");
                }

                // Si no se proporcionó idUsuario en ninguna de las vías, responder con error
                if (idUsuario == null || idUsuario.isEmpty()) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Se requiere el parámetro 'idUsuario'.");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                // CONTROL DE ACCESO BASADO EN ROLES (RBAC):
                // Justificación de negocio:
                // - Administrador ("ROL-001"): Tiene permiso para auditar/ver todos los pedidos realizados en el sistema.
                // - Cliente ("ROL-003"): Solo puede visualizar sus propios pedidos.
                // - Empleado ("ROL-002"): No tiene acceso al módulo de historial de pedidos.
                UsuarioDAO usuarioDao = new UsuarioDAO();
                Usuario user = usuarioDao.obtenerUsuarioPorId(idUsuario);

                List<Historial> lista;
                if (user == null) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Usuario no encontrado.");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                if ("ROL-001".equals(user.getIdRol())) {
                    lista = historialDao.obtenerTodosLosPedidos();
                } else if ("ROL-003".equals(user.getIdRol())) {
                    lista = historialDao.obtenerHistorialUsuario(idUsuario);
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Acceso denegado: El rol asignado no cuenta con permisos para ver historiales de pedidos.");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                // Instanciar un JSONArray para almacenar la lista en formato JSON
                JSONArray arrayPedidos = new JSONArray();
                for (Historial h : lista) {
                    JSONObject item = new JSONObject();
                    item.put("idPedido", h.getIdPedido());
                    item.put("idUsuario", h.getIdUsuario());
                    item.put("date", h.getFecha());
                    item.put("total", h.getTotal());
                    item.put("status", h.getEstado());
                    item.put("tipoEntrega", h.getTipoEntrega() != null ? h.getTipoEntrega() : "");
                    item.put("customerName", h.getCustomerName() != null ? h.getCustomerName() : "Cliente Anónimo");
                    arrayPedidos.put(item);
                }

                // Construir respuesta exitosa con la colección integrada
                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("orders", arrayPedidos);
            }
            // ACCIÓN NO CONTROLADA
            else {
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "Acción no reconocida.");
            }
        } catch (Exception e) {
            // Registrar excepción en la consola del servidor
            e.printStackTrace();
            
            // Construir respuesta de error interna
            jsonRespuesta.put("status", "error");
            jsonRespuesta.put("message", "Error interno en el servlet: " + e.getMessage());
        }

        // Retornar la respuesta JSON construida al cliente
        out.print(jsonRespuesta.toString());
        out.flush();
    }

    /**
     * Maneja las peticiones Preflight de CORS delegando al filtro o respondiendo OK.
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Responder con un estado HTTP 200 OK, delegando las cabeceras al filtro global
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Procesa peticiones HTTP GET delegándolas al método doPost para compatibilidad
     * con la recuperación de historial directa desde parámetros en la URL.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirigir el procesamiento al método doPost de forma segura
        doPost(request, response);
    }
}
