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

@WebServlet("/PruebaServlet")
public class PruebaServlet extends HttpServlet {

    private final UsuarioDAO usuarioDao = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        
        response.setHeader( "Access-Control-Allow-Origin", "*");

        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");

        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        System.out.println("Entró al servlet");
        
        // Configuramos la respuesta para que el navegador sepa que le devolvemos JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Capturamos el parámetro 'accion' enviado en la URL (?accion=registrar)
        String accion = request.getParameter("accion");

        if ("create".equals(accion)) {
            try {
                // Leer el cuerpo de la petición (el JSON que envía el frontend)
                StringBuilder sb = new StringBuilder();
                String linea;
                try (BufferedReader reader = request.getReader()) {
                    while ((linea = reader.readLine()) != null) {
                        sb.append(linea);
                    }
                }
                String jsonInput = sb.toString();

                // Mapeo de datos extraídos del JSON manual o usando expresiones
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
        }else if ("login".equals(accion)) {

            try {

                StringBuilder sb = new StringBuilder();
                String linea;

                try (BufferedReader reader = request.getReader()) {

                    while ((linea = reader.readLine())!= null) {
                        sb.append(linea);
                    }
                }

                String jsonInput = sb.toString();
                String email = extraerPropiedadJson(jsonInput, "email");
                String password = extraerPropiedadJson( jsonInput, "password");
                Usuario usuario = usuarioDao.login( email, password);

                if (usuario != null) {
                    out.print("{" + "\"status\":\"success\"," + "\"name\":\"" + usuario.getName()
                        + "\"," + "\"email\":\""  + usuario.getEmail() + "\"" + "}");
                }
                else {
                    out.print("{" + "\"status\":\"error\"," + "\"message\":\"Credenciales incorrectas\"" + "}");
                }
            }
            catch (Exception e) {
                out.print ( "{" + "\"status\":\"error\"," + "\"message\":\"" + e.getMessage() + "\"" + "}");
            }
        }
    }
    // manejamos las peticiones options necesarias para cors
    @Override
    protected void doOptions(
        HttpServletRequest request,
        HttpServletResponse response
    )
    throws ServletException, IOException {

        response.setHeader(
            "Access-Control-Allow-Origin",
            "*"
        );

        response.setHeader(
            "Access-Control-Allow-Methods",
            "POST, GET, OPTIONS"
        );

        response.setHeader(
            "Access-Control-Allow-Headers",
            "Content-Type"
        );

        response.setStatus(
            HttpServletResponse.SC_OK
        );
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
    @Override
    protected void service(
        HttpServletRequest request,
        HttpServletResponse response
    )
    throws ServletException, IOException {

        response.setHeader(
            "Access-Control-Allow-Origin",
            "*"
        );

        response.setHeader(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
        );

        response.setHeader(
            "Access-Control-Allow-Headers",
            "*"
        );

        response.setHeader(
            "Access-Control-Allow-Credentials",
            "true"
        );

        // Responder inmediatamente OPTIONS
        if (
            request.getMethod().equalsIgnoreCase("OPTIONS")
        ) {

            response.setStatus(
                HttpServletResponse.SC_OK
            );

            return;
        }

        super.service(request, response);
    }
}