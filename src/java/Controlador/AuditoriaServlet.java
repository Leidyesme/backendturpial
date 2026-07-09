package Controlador;

import DAO.AuditoriaDAO;
import Modelo.Entidades.Auditoria;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet que maneja las peticiones HTTP relacionadas con el log de auditoría/actividad del usuario.
 * Proporciona endpoints para registrar y listar actividades del usuario.
 */
@WebServlet("/auditoria")
public class AuditoriaServlet extends HttpServlet {

    // Instancia del DAO para acceder a los métodos de base de datos.
    private final AuditoriaDAO dao = new AuditoriaDAO();

    /**
     * Procesa solicitudes POST para registrar o listar registros de auditoría.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Configura la respuesta como JSON para que el frontend pueda procesarla fácilmente.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Obtiene el parámetro de acción (ej: 'listar' o 'registrar') desde la URL.
        String accion = request.getParameter("accion");
        JSONObject jsonRespuesta = new JSONObject();

        try {
            // Lectura del cuerpo de la petición (JSON crudo) enviado desde el frontend.
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();

            // LÓGICA DE LISTADO: Recupera el historial de actividades de un usuario.
            if ("listar".equals(accion)) {
                String idUsuario = null;
                // Intenta obtener idUsuario desde el JSON del cuerpo o desde un parámetro URL.
                if (!body.trim().isEmpty()) {
                    JSONObject jsonEntrada = new JSONObject(body);
                    idUsuario = jsonEntrada.optString("idUsuario", null);
                }
                if (idUsuario == null || idUsuario.isEmpty()) {
                    idUsuario = request.getParameter("idUsuario");
                }

                // Validación: si no hay ID, no podemos listar nada.
                if (idUsuario == null || idUsuario.isEmpty()) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Se requiere el parámetro 'idUsuario'");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                // Llama al DAO para obtener la lista de la base de datos.
                List<Auditoria> lista = dao.listarPorUsuario(idUsuario);
                JSONArray jsonArray = new JSONArray();
                // Convierte la lista de objetos Java a un JSONArray para el frontend.
                for (Auditoria aud : lista) {
                    JSONObject item = new JSONObject();
                    item.put("idHistorial", aud.getIdHistorial());
                    item.put("idUsuario", aud.getIdUsuario());
                    item.put("accion", aud.getAccion());
                    item.put("tipoAccion", aud.getTipoAccion());
                    item.put("fecha", aud.getFecha());
                    jsonArray.put(item);
                }

                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("activities", jsonArray);
            } 
            // LÓGICA DE REGISTRO: Guarda una nueva actividad en el historial.
            else if ("registrar".equals(accion)) {
                if (body.trim().isEmpty()) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Cuerpo JSON vacío");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                // Extrae los datos del JSON recibido.
                JSONObject jsonEntrada = new JSONObject(body);
                String idUsuario = jsonEntrada.getString("idUsuario");
                String descripcion = jsonEntrada.getString("accion");
                String tipoAccion = jsonEntrada.getString("tipoAccion");

                // Crea la entidad intermedia para enviarla al DAO.
                Auditoria aud = new Auditoria();
                aud.setIdUsuario(idUsuario);
                aud.setAccion(descripcion);
                aud.setTipoAccion(tipoAccion);

                // Ejecuta la inserción en base de datos.
                boolean registrado = dao.registrarActividad(aud);
                if (registrado) {
                    jsonRespuesta.put("status", "success");
                    jsonRespuesta.put("message", "Actividad registrada exitosamente");
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "No se pudo registrar la actividad");
                }
            } 
            else {
                // Manejo de acciones no definidas.
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "Acción no reconocida");
            }
        } catch (Exception e) {
            // Manejo de errores internos (ej: JSON mal formado).
            e.printStackTrace();
            jsonRespuesta.put("status", "error");
            jsonRespuesta.put("message", "Error interno: " + e.getMessage());
        }

        // Envía el JSON final de respuesta al cliente.
        out.print(jsonRespuesta.toString());
        out.flush();
    }

    /**
     * Responde a peticiones preflight OPTIONS (necesario para CORS).
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Soporta GET para simplificar pruebas de integración o depuración.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}