package Controlador;

import DAO.DevolucionDAO;
import DAO.UsuarioDAO;
import Modelo.Entidades.Devolucion;
import Modelo.Entidades.Usuario;
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
                String idUsuario = jsonEntrada.optString("idUsuario", null);

                // CONTROL DE ACCESO BASADO EN ROLES (RBAC):
                // Justificación de negocio: Únicamente los usuarios con rol de Cliente ("ROL-003") pueden
                // iniciar solicitudes de devolución. Empleados y administradores no tienen permitida esta opción.
                if (idUsuario != null && !idUsuario.isEmpty()) {
                    UsuarioDAO usuarioDao = new UsuarioDAO();
                    Usuario user = usuarioDao.obtenerUsuarioPorId(idUsuario);
                    if (user == null || !"ROL-003".equals(user.getIdRol())) {
                        jsonRespuesta.put("status", "error");
                        jsonRespuesta.put("message", "Acceso denegado: Solo los clientes tienen permitido solicitar devoluciones.");
                        out.print(jsonRespuesta.toString());
                        return;
                    }
                }

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

                // CONTROL DE ACCESO / LÓGICA DE NEGOCIO:
                // Cada usuario con rol de Cliente puede ver el estado y respuesta de las devoluciones que él mismo creó.
                // Mapeamos respuesta_admin en el JSON de salida para que sea visible en el panel del cliente.
                List<Devolucion> lista = dao.listarPorUsuario(idUsuario);
                JSONArray jsonArray = new JSONArray();
                for (Devolucion dev : lista) {
                    JSONObject item = new JSONObject();
                    item.put("idDevolucion", dev.getIdDevolucion());
                    item.put("idPedido", dev.getIdPedido());
                    item.put("motivo", dev.getMotivo());
                    item.put("fechaSolicitud", dev.getFechaSolicitud());
                    item.put("estadoDevolucion", dev.getEstadoDevolucion());
                    item.put("respuestaAdmin", dev.getRespuestaAdmin() != null ? dev.getRespuestaAdmin() : "");
                    jsonArray.put(item);
                }

                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("returns", jsonArray);
            } 
            else if ("listarTodas".equals(accion)) {
                // CONTROL DE ACCESO BASADO EN ROLES (RBAC):
                // Justificación de negocio: Únicamente los administradores ("ROL-001") tienen permiso
                // de auditar y ver el listado general de devoluciones del sistema.
                String idUsuario = null;
                if (!body.trim().isEmpty()) {
                    JSONObject jsonEntrada = new JSONObject(body);
                    idUsuario = jsonEntrada.optString("idUsuario", null);
                }
                if (idUsuario == null || idUsuario.isEmpty()) {
                    idUsuario = request.getParameter("idUsuario");
                }

                if (idUsuario != null && !idUsuario.isEmpty()) {
                    UsuarioDAO usuarioDao = new UsuarioDAO();
                    Usuario user = usuarioDao.obtenerUsuarioPorId(idUsuario);
                    if (user == null || !"ROL-001".equals(user.getIdRol())) {
                        jsonRespuesta.put("status", "error");
                        jsonRespuesta.put("message", "Acceso denegado: Rol no autorizado para ver todas las devoluciones.");
                        out.print(jsonRespuesta.toString());
                        return;
                    }
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Acceso denegado: Se requiere idUsuario para validar permisos.");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                List<Devolucion> lista = dao.listarTodas();
                JSONArray jsonArray = new JSONArray();
                for (Devolucion dev : lista) {
                    JSONObject item = new JSONObject();
                    item.put("idDevolucion", dev.getIdDevolucion());
                    item.put("idPedido", dev.getIdPedido());
                    item.put("motivo", dev.getMotivo());
                    item.put("fechaSolicitud", dev.getFechaSolicitud());
                    item.put("estadoDevolucion", dev.getEstadoDevolucion());
                    item.put("respuestaAdmin", dev.getRespuestaAdmin() != null ? dev.getRespuestaAdmin() : "");
                    jsonArray.put(item);
                }

                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("returns", jsonArray);
            } 
            else if ("responder".equals(accion)) {
                // CONTROL DE ACCESO BASADO EN ROLES (RBAC):
                // Justificación de negocio: Únicamente los administradores ("ROL-001") tienen permitido
                // resolver/responder a las devoluciones hechas por los clientes.
                if (body.trim().isEmpty()) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Cuerpo JSON vacío");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                JSONObject jsonEntrada = new JSONObject(body);
                String idUsuario = jsonEntrada.getString("idUsuario");
                String idDevolucion = jsonEntrada.getString("idDevolucion");
                String estado = jsonEntrada.getString("estado"); // 'Aprobada' o 'Rechazada'
                String respuestaAdmin = jsonEntrada.getString("respuestaAdmin");

                UsuarioDAO usuarioDao = new UsuarioDAO();
                Usuario user = usuarioDao.obtenerUsuarioPorId(idUsuario);
                if (user == null || !"ROL-001".equals(user.getIdRol())) {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "Acceso denegado: Solo administradores pueden procesar devoluciones.");
                    out.print(jsonRespuesta.toString());
                    return;
                }

                boolean procesado = dao.procesarDevolucion(idDevolucion, estado, respuestaAdmin);
                if (procesado) {
                    jsonRespuesta.put("status", "success");
                    jsonRespuesta.put("message", "Devolución procesada correctamente");
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "No se pudo actualizar el estado de la devolución");
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
     * Permite soportar consultas GET en caso de integración directa.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
