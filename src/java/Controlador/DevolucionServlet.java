package Controlador;

import DAO.DevolucionDAO;
import Modelo.Entidades.Devolucion;
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
 * Servlet que maneja las peticiones HTTP relacionadas con el módulo de devoluciones de pedidos.
 * Proporciona endpoints para solicitar devoluciones de pedidos y consultar su estado.
 */
@WebServlet("/devolucion")
public class DevolucionServlet extends HttpServlet {

    private final DevolucionDAO dao = new DevolucionDAO();

    /**
     * Procesa solicitudes POST para registrar o listar solicitudes de devolución.
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

            if ("solicitar".equals(accion)) {
                if (body.trim().isEmpty()) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Cuerpo JSON vacío");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                JSONObject jsonEntrada = new JSONObject(body);
                String idPedido = jsonEntrada.getString("idPedido");
                String motivo = jsonEntrada.getString("motivo");

                Devolucion dev = new Devolucion();
                dev.setIdPedido(idPedido);
                dev.setMotivo(motivo);

                boolean solicitado = dao.solicitarDevolucion(dev);
                if (solicitado) {
                    jsonRespuesta.put("status", "success");
                    jsonRespuesta.put("message", "Solicitud de devolución registrada exitosamente");
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "No se pudo registrar la solicitud de devolución");
                }
            } 
            else if ("listar".equals(accion)) {
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

                List<Devolucion> lista = dao.listarPorUsuario(idUsuario);
                JSONArray jsonArray = new JSONArray();
                for (Devolucion dev : lista) {
                    JSONObject item = new JSONObject();
                    item.put("idDevolucion", dev.getIdDevolucion());
                    item.put("idPedido", dev.getIdPedido());
                    item.put("motivo", dev.getMotivo());
                    item.put("fechaSolicitud", dev.getFechaSolicitud());
                    item.put("estadoDevolucion", dev.getEstadoDevolucion());
                    jsonArray.put(item);
                }

                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("returns", jsonArray);
            } 
            else if ("listarTodas".equals(accion)) {
                // Endpoint administrativo para ver todas las devoluciones del negocio
                List<Devolucion> lista = dao.listarTodas();
                JSONArray jsonArray = new JSONArray();
                for (Devolucion dev : lista) {
                    JSONObject item = new JSONObject();
                    item.put("idDevolucion", dev.getIdDevolucion());
                    item.put("idPedido", dev.getIdPedido());
                    item.put("motivo", dev.getMotivo());
                    item.put("fechaSolicitud", dev.getFechaSolicitud());
                    item.put("estadoDevolucion", dev.getEstadoDevolucion());
                    jsonArray.put(item);
                }

                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("returns", jsonArray);
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
     * Permite soportar consultas GET en caso de integración directa.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
