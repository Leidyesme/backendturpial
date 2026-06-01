package controlador;

import DAO.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Modelo.Entidades.Usuario;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


// Se encarga de las peticiones del registro y login de los usuarios
@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet {

     // DAO de usuario
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // metodo post el cual recibe peticiones del frontend
    @Override
    protected void doPost(

        HttpServletRequest request,

        HttpServletResponse response

    )

    throws ServletException, IOException {

        // Configuración CORS
        response.setHeader("Access-Control-Allow-Origin", "*");

        response.setHeader("Access-Control-Allow-Methods", "POST");

        response.setHeader( "Access-Control-Allow-Headers", "Content-Type");

        //tipo de respuesta
        response.setContentType("application/json");

        //obtiene la accion
        String accion = request.getParameter("accion");

        // Se lee el json que fue enviado por el frontend
        BufferedReader reader = request.getReader();

        StringBuilder jsonBuilder = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        // el json es convertido en string
        String json = jsonBuilder.toString();

        // extraemos los datos del json
        String name = extraerPropiedadJson(json, "name");
        String email = extraerPropiedadJson(json, "email");
        String phone = extraerPropiedadJson(json, "phone");
        String password = extraerPropiedadJson(json, "password");

        // Crea objeto Usuario
        Usuario usuario = new Usuario();

        usuario.setName(name);
        usuario.setEmail(email);
        usuario.setPhone(phone);
        usuario.setPassword(password);


        // utilizamos un writer para responder
         
        PrintWriter out =
            response.getWriter();


        //Acción registrar
        if ("create".equals(accion)) {

            boolean registrado = usuarioDAO.registrar(usuario);

            //Respuesta JSON
            out.print( "{ \"success\": " + registrado + " }" );
        }


        //Acción login
        else if ("login".equals(accion)) {

            Usuario usuarioEncontrado = usuarioDAO.login(email,password);


            //Verifica si existe
            if (usuarioEncontrado != null) {
                out.print("{ \"success\": true }");
            }

            else {
                out.print( "{ \"success\": false }");
            }
        }
    }

    // Metodo que se encarga de extraer manualmente las propiedades del json
    private String extraerPropiedadJson(String json,String propiedad) {

        String buscar = "\"" + propiedad + "\":\"";

        int inicio = json.indexOf(buscar) + buscar.length();

        int fin =json.indexOf( "\"", inicio);

        return json.substring( inicio, fin);

    }

}