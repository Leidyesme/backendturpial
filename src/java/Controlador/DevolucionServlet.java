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

    // Instancia del DAO para interactuar con la tabla de devoluciones en MySQL.
    private final DevolucionDAO dao = new DevolucionDAO();

    /**
     * Procesa solicitudes POST para registrar o listar solicitudes de devolución.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Establece el formato de respuesta como JSON para el frontend.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Identifica la acción solicitada por el usuario (solicitar, listar, listarTodas, responder).
        String accion = request.getParameter("accion");
        JSONObject jsonRespuesta = new JSONObject();

        try {
            // Lectura del cuerpo de la petición (JSON) enviado por el cliente.
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();

            // CASO: SOLICITAR DEVOLUCIÓN
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

                // CONTROL DE ACCESO (RBAC): Valida que solo clientes (ROL-003) puedan solicitar devoluciones.
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

                // Crea la entidad y llama al DAO para registrarla.
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
            // CASO: LISTAR DEVOLUCIONES DE UN CLIENTE
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

                // Llama al DAO para obtener las devoluciones específicas del usuario.
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
            // CASO: LISTAR TODAS LAS DEVOLUCIONES (SOLO ADMINS)
            else if ("listarTodas".equals(accion)) {
                String idUsuario = null;
                if (!body.trim().isEmpty()) {
                    JSONObject jsonEntrada = new JSONObject(body);
                    idUsuario = jsonEntrada.optString("idUsuario", null);
                }
                if (idUsuario == null || idUsuario.isEmpty()) {
                    idUsuario = request.getParameter("idUsuario");
                }

                // CONTROL DE ACCESO (RBAC): Valida rol de administrador (ROL-001).
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
            // CASO: RESPONDER / PROCESAR DEVOLUCIÓN (ADMINS)
            else if ("responder".equals(accion)) {
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

                // CONTROL DE ACCESO (RBAC): Valida rol de administrador.
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

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}