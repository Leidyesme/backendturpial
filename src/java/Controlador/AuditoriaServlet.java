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

    private final AuditoriaDAO dao = new AuditoriaDAO();

    /**
     * Procesa solicitudes POST para registrar o listar registros de auditoría.
     *
     * @param request Petición del cliente conteniendo el parámetro de acción y el cuerpo JSON.
     * @param response Respuesta JSON enviada al cliente.
     * @throws ServletException si ocurre un error específico del servlet.
     * @throws IOException si ocurre un error de E/S.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String accion = request.getParameter("accion");
        JSONObject jsonRespuesta = new JSONObject();

        try {
            // Leer cuerpo del request
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();

            if ("listar".equals(accion)) {
                String idUsuario = null;
                if (!body.trim().isEmpty()) {
                    JSONObject jsonEntrada = new JSONObject(body);
                    idUsuario = jsonEntrada.optString("idUsuario", null);
                }
                if (idUsuario == null || idUsuario.isEmpty()) {
                    idUsuario = request.getParameter("idUsuario");
                }

                if (idUsuario == null || idUsuario.isEmpty()) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Se requiere el parámetro 'idUsuario'");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                List<Auditoria> lista = dao.listarPorUsuario(idUsuario);
                JSONArray jsonArray = new JSONArray();
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
            else if ("registrar".equals(accion)) {
                if (body.trim().isEmpty()) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Cuerpo JSON vacío");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                JSONObject jsonEntrada = new JSONObject(body);
                String idUsuario = jsonEntrada.getString("idUsuario");
                String descripcion = jsonEntrada.getString("accion");
                String tipoAccion = jsonEntrada.getString("tipoAccion");

                Auditoria aud = new Auditoria();
                aud.setIdUsuario(idUsuario);
                aud.setAccion(descripcion);
                aud.setTipoAccion(tipoAccion);

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
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "Acción no reconocida");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonRespuesta.put("status", "error");
            jsonRespuesta.put("message", "Error interno: " + e.getMessage());
        }

        out.print(jsonRespuesta.toString());
        out.flush();
    }

    /**
     * Responde a peticiones preflight OPTIONS para CORS.
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Permite soportar listado directo a través de GET para fines de depuración o integración simple.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
