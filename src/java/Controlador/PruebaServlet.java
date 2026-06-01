package Controlador;

import DAO.UsuarioDAO;
import Modelo.Entidades.Usuario;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PruebaServlet", urlPatterns = {"/PruebaServlet"})
public class PruebaServlet extends HttpServlet {

    private final UsuarioDAO usuarioDao = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Configuramos la respuesta para que el navegador sepa que le devolvemos JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Capturamos el parámetro 'accion' enviado en la URL (?accion=registrar)
        String accion = request.getParameter("accion");

        if ("registrar".equals(accion)) {
            try {
                // 1. Leer el cuerpo de la petición (el JSON que envía el frontend)
                StringBuilder sb = new StringBuilder();
                String linea;
                try (BufferedReader reader = request.getReader()) {
                    while ((linea = reader.readLine()) != null) {
                        sb.append(linea);
                    }
                }
                String jsonInput = sb.toString();

                // 2. Mapeo rápido de datos extraídos del JSON manual o usando expresiones
                // (Para evitar errores si no tienes Gson/Jackson en NetBeans, extraemos los textos básicos)
                String name = extraerPropiedadJson(jsonInput, "name");
                String email = extraerPropiedadJson(jsonInput, "email");
                String phone = extraerPropiedadJson(jsonInput, "phone");
                String password = extraerPropiedadJson(jsonInput, "password");
                
                // Construimos el objeto Usuario
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setName(name);
                nuevoUsuario.setEmail(email);
                nuevoUsuario.setPhone(phone);
                nuevoUsuario.setPassword(password);
                nuevoUsuario.setIdRol(3); // Por defecto 3 = Cliente
                nuevoUsuario.setEstado("Activo");

                // 3. Llamamos al DAO para guardar en MySQL
                boolean exito = usuarioDao.registrar(nuevoUsuario);

                // 4. Responder al Frontend en formato JSON
                if (exito) {
                    out.print("{\"status\":\"success\", \"message\":\"Usuario creado exitosamente\"}");
                } else {
                    out.print("{\"status\":\"error\", \"message\":\"El correo ya existe o hubo un error en la BD.\"}");
                }

            } catch (Exception e) {
                out.print("{\"status\":\"error\", \"message\":\"Error interno en el servidor: " + e.getMessage() + "\"}");
            }
        } else {
            out.print("{\"status\":\"error\", \"message\":\"Acción no permitida o no encontrada.\"}");
        }
        out.flush();
    }

    // Método auxiliar rápido para extraer campos de un JSON plano sin librerías externas
    private String extraerPropiedadJson(String json, String propiedad) {
        try {
            String llave = "\"" + propiedad + "\":\"";
            int inicio = json.indexOf(llave) + llave.length();
            int fin = json.indexOf("\"", inicio);
            return json.substring(inicio, fin);
        } catch (Exception e) {
            // Si es un entero como idRol, buscamos sin comillas en el valor
            try {
                String llaveNum = "\"" + propiedad + "\":";
                int inicio = json.indexOf(llaveNum) + llaveNum.length();
                int fin = json.indexOf(",", inicio);
                if (fin == -1) fin = json.indexOf("}", inicio);
                return json.substring(inicio, fin).trim();
            } catch (Exception ex) {
                return "";
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redireccionar posts o peticiones GET no autorizadas
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Utiliza peticiones POST.");
    }
}