// Definición del paquete donde se ubica este servlet
package Controlador;

// Importar el DAO para acceder a los datos de los usuarios
import DAO.UsuarioDAO;
// Importar la entidad Usuario para manejar los datos del usuario
import Modelo.Entidades.Usuario;
// Importar la clase BufferedReader para leer el cuerpo de las peticiones
import java.io.BufferedReader;
// Importar IOException para manejar los errores de entrada y salida
import java.io.IOException;
// Importar PrintWriter para escribir la respuesta JSON al cliente
import java.io.PrintWriter;
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
// Importar JSONObject para el manejo estructurado y seguro de datos JSON
import org.json.JSONObject;
// Importar JSONArray
import org.json.JSONArray;
// Importar List
import java.util.List;

// Definición de la ruta del servlet mediante la anotación WebServlet
@WebServlet("/UsuarioServlet")
// Clase principal del servlet de usuarios que extiende de HttpServlet
public class UsuarioServlet extends HttpServlet {

    // Instancia del DAO de usuarios para realizar las operaciones en la base de datos
    private final UsuarioDAO usuarioDao = new UsuarioDAO();

    // Sobrescribir el método doPost para atender las peticiones HTTP POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            // Declaración de las excepciones que puede arrojar este método
            throws ServletException, IOException {

        // NOTA: Las cabeceras CORS manuales han sido removidas de este servlet porque
        // CorsFilter.java ya maneja CORS de forma global para todas las rutas (/*).
        // Duplicarlas aquí causa errores de bloqueo de CORS en el navegador debido a cabeceras múltiples.

        // Establecer el tipo de contenido de la respuesta como JSON
        response.setContentType("application/json");
        // Configurar la codificación de caracteres de la respuesta a UTF-8
        response.setCharacterEncoding("UTF-8");
        // Obtener el objeto PrintWriter para escribir la respuesta JSON
        PrintWriter out = response.getWriter();

        // Obtener el parámetro 'accion' de la URL de la petición
        String accion = request.getParameter("accion");

        // Objeto para construir la respuesta JSON
        JSONObject jsonRespuesta = new JSONObject();

        // Estructura de control try-catch general para atrapar excepciones en el proceso
        try {
            // Leer el cuerpo de la petición (JSON) si es enviado
            StringBuilder sb = new StringBuilder();
            // Variable temporal para guardar cada línea del cuerpo de la petición
            String linea;
            // Usar try-with-resources para asegurar el cierre del BufferedReader del request
            try (BufferedReader reader = request.getReader()) {
                // Iterar línea por línea mientras haya contenido en el cuerpo de la petición
                while ((linea = reader.readLine()) != null) {
                    // Adjuntar la línea leída al constructor de cadenas
                    sb.append(linea);
                }
            }
            // Convertir el contenido del cuerpo a un String final
            String cuerpoPeticion = sb.toString();

            // Módulo de creación de cuenta (Registro)
            if ("create".equals(accion)) {
                // Parsear la cadena JSON entrante a un JSONObject
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                // Extraer el nombre del JSON
                String name = jsonEntrada.getString("name");
                // Extraer el correo electrónico del JSON
                String email = jsonEntrada.getString("email");
                // Extraer el teléfono del JSON
                String phone = jsonEntrada.getString("phone");
                // Extraer la contraseña del JSON
                String password = jsonEntrada.getString("password");
                // Extraer la dirección del JSON
                String direccion = jsonEntrada.optString("direccion", "");

                // Crear un nuevo objeto Usuario
                Usuario nuevoUsuario = new Usuario();
                // Asignar el nombre al usuario
                nuevoUsuario.setName(name);
                // Asignar el correo al usuario
                nuevoUsuario.setEmail(email);
                // Asignar el teléfono al usuario
                nuevoUsuario.setPhone(phone);
                // Asignar la contraseña al usuario
                nuevoUsuario.setPassword(password);
                // Asignar la dirección al usuario
                nuevoUsuario.setDireccion(direccion);
                // Asignar el ID de rol por defecto ("ROL-003" = Cliente)
                nuevoUsuario.setIdRol("ROL-003");
                // Asignar el estado por defecto ("Activo")
                nuevoUsuario.setEstado("Activo");

                // Invocar al DAO para registrar el usuario en MySQL
                boolean exito = usuarioDao.registrar(nuevoUsuario);

                // Validar si el registro en la base de datos fue exitoso
                if (exito) {
                    // Definir estado exitoso en la respuesta JSON
                    jsonRespuesta.put("status", "success");
                    // Mensaje de éxito del registro
                    jsonRespuesta.put("message", "Usuario creado exitosamente");
                } else {
                    // Definir estado de error si falla la inserción en la BD
                    jsonRespuesta.put("status", "error");
                    // Mensaje detallando que el correo podría estar duplicado
                    jsonRespuesta.put("message", "El correo ya existe o hubo un error en la BD.");
                }
            }
            // Módulo de inicio de sesión (Login)
            else if ("login".equals(accion)) {
                // Parsear el JSON del cuerpo a un JSONObject
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                // Obtener el correo ingresado en el frontend
                String email = jsonEntrada.getString("email");
                // Obtener la contraseña ingresada en el frontend
                String password = jsonEntrada.getString("password");

                // Validar credenciales y buscar usuario mediante el DAO
                Usuario usuario = usuarioDao.login(email, password);

                // Comprobar si se encontró al usuario con las credenciales indicadas
                if (usuario != null) {
                    // Definir estado exitoso en la respuesta
                    jsonRespuesta.put("status", "success");
                    // Crear un sub-objeto JSON con los datos del usuario logueado
                    JSONObject jsonUsuario = new JSONObject();
                    // Agregar el ID del usuario
                    jsonUsuario.put("idUsuario", usuario.getIdUsuario());
                    // Agregar el ID del rol asignado
                    jsonUsuario.put("idRol", usuario.getIdRol());
                    // Agregar el nombre del usuario
                    jsonUsuario.put("name", usuario.getName());
                    // Agregar el correo del usuario
                    jsonUsuario.put("email", usuario.getEmail());
                    // Agregar el teléfono del usuario
                    jsonUsuario.put("phone", usuario.getPhone());
                    // Agregar el estado de la cuenta
                    jsonUsuario.put("estado", usuario.getEstado());
                    // Agregar la dirección del usuario
                    jsonUsuario.put("direccion", usuario.getDireccion() != null ? usuario.getDireccion() : "");
                    // Anidar la información del usuario en la respuesta principal
                    jsonRespuesta.put("usuario", jsonUsuario);
                } else {
                    // Definir estado de error por credenciales incorrectas
                    jsonRespuesta.put("status", "error");
                    // Mensaje informativo del error de autenticación
                    jsonRespuesta.put("message", "Credenciales incorrectas");
                }
            }
                      else if ("update".equals(accion)) {
                // Convertir el JSON entrante a un JSONObject
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                // Obtener el ID del usuario a modificar
                String idUsuario = jsonEntrada.getString("idUsuario");
                // Obtener el nuevo nombre del perfil
                String name = jsonEntrada.getString("name");
                // Obtener el nuevo correo electrónico del perfil
                String email = jsonEntrada.getString("email");
                // Obtener el nuevo número telefónico del perfil
                String phone = jsonEntrada.getString("phone");
                // Obtener la nueva dirección del perfil
                String direccion = jsonEntrada.optString("direccion", "");
 
                // Crear objeto Usuario con los nuevos valores
                Usuario usuario = new Usuario();
                // Asignar el ID del usuario a actualizar
                usuario.setIdUsuario(idUsuario);
                // Asignar el nuevo nombre
                usuario.setName(name);
                // Asignar el nuevo email
                usuario.setEmail(email);
                // Asignar el nuevo teléfono
                usuario.setPhone(phone);
                // Asignar la nueva dirección
                usuario.setDireccion(direccion);

                // Llamar al DAO para persistir la actualización en la BD
                boolean actualizado = usuarioDao.actualizarUsuario(usuario);

                // Comprobar si se efectuó la actualización correctamente
                if (actualizado) {
                    // Definir estado de éxito en la respuesta
                    jsonRespuesta.put("status", "success");
                    // Mensaje de éxito para el perfil actualizado
                    jsonRespuesta.put("message", "Perfil actualizado");
                } else {
                    // Definir estado de error si falla la actualización
                    jsonRespuesta.put("status", "error");
                    // Mensaje informativo del fallo
                    jsonRespuesta.put("message", "No se pudo actualizar el perfil");
                }
            }
            // Módulo de cambio de contraseña (Change Password)
            else if ("changePassword".equals(accion)) {
                // Parsear la cadena JSON a un objeto JSONObject
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                // Extraer el ID del usuario
                String idUsuario = jsonEntrada.getString("idUsuario");
                // Extraer la contraseña actual
                String currentPassword = jsonEntrada.getString("currentPassword");
                // Extraer la nueva contraseña
                String newPassword = jsonEntrada.getString("newPassword");

                // Invocar al DAO para realizar el cambio en la BD tras comprobar la contraseña actual
                boolean cambiado = usuarioDao.cambiarPassword(idUsuario, currentPassword, newPassword);

                // Validar si el cambio fue exitoso
                if (cambiado) {
                    // Asignar estado exitoso a la respuesta
                    jsonRespuesta.put("status", "success");
                    // Mensaje de confirmación del cambio
                    jsonRespuesta.put("message", "Contraseña actualizada exitosamente");
                } else {
                    // Asignar estado de error
                    jsonRespuesta.put("status", "error");
                    // Mensaje indicando que la contraseña actual es incorrecta o falló la BD
                    jsonRespuesta.put("message", "La contraseña actual es incorrecta");
                }
            }
            // Módulo de lectura de usuario por ID (Read User)
            else if ("readUser".equals(accion)) {
                // Parsear la cadena JSON entrante
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                // Extraer el ID del usuario a consultar
                String idUsuario = jsonEntrada.getString("idUsuario");

                // Obtener el objeto Usuario desde la base de datos por su ID
                Usuario usuario = usuarioDao.obtenerUsuarioPorId(idUsuario);

                // Verificar si se obtuvo información del usuario
                if (usuario != null) {
                    // Definir estado de éxito
                    jsonRespuesta.put("status", "success");
                    // Agregar el ID del usuario a la respuesta
                    jsonRespuesta.put("idUsuario", usuario.getIdUsuario());
                    // Agregar el ID del rol asignado a la respuesta
                    jsonRespuesta.put("idRol", usuario.getIdRol());
                    // Agregar el nombre a la respuesta
                    jsonRespuesta.put("name", usuario.getName());
                    // Agregar el correo a la respuesta
                    jsonRespuesta.put("email", usuario.getEmail());
                    // Agregar el teléfono a la respuesta
                    jsonRespuesta.put("phone", usuario.getPhone());
                    // Agregar el estado de la cuenta a la respuesta
                    jsonRespuesta.put("estado", usuario.getEstado());
                    // Agregar la dirección a la respuesta
                    jsonRespuesta.put("direccion", usuario.getDireccion() != null ? usuario.getDireccion() : "");
                } else {
                    // Definir estado de error si no existe el usuario
                    jsonRespuesta.put("status", "error");
                    // Mensaje informativo del error
                    jsonRespuesta.put("message", "Usuario no encontrado");
                }
            }
            else if ("existsEmail".equals(accion)) {
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                String email = jsonEntrada.getString("email");
                boolean existe = usuarioDao.existsEmail(email);
                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("exists", existe);
            }
            else if ("resetPassword".equals(accion)) {
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                String email = jsonEntrada.getString("email");
                String newPassword = jsonEntrada.getString("newPassword");
                boolean exito = usuarioDao.resetPassword(email, newPassword);
                if (exito) {
                    jsonRespuesta.put("status", "success");
                    jsonRespuesta.put("message", "Contraseña restablecida correctamente");
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "No se pudo restablecer la contraseña");
                }
            }
            else if ("listEmployees".equals(accion)) {
                List<Usuario> lista = usuarioDao.listarEmpleados();
                JSONArray arrayEmpleados = new JSONArray();
                for (Usuario u : lista) {
                    JSONObject item = new JSONObject();
                    item.put("id", u.getIdUsuario());
                    item.put("name", u.getName());
                    item.put("email", u.getEmail());
                    item.put("role", u.getIdRol()); // friendly name
                    item.put("status", u.getEstado());
                    arrayEmpleados.put(item);
                }
                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("employees", arrayEmpleados);
            }
            else if ("createEmployee".equals(accion)) {
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                String name = jsonEntrada.getString("name");
                String email = jsonEntrada.getString("email");
                String role = jsonEntrada.getString("role");
                String idRol = role.toLowerCase().contains("admin") ? "ROL-001" : "ROL-002";

                Usuario u = new Usuario();
                u.setName(name);
                u.setEmail(email);
                u.setIdRol(idRol);
                u.setEstado("Activo");

                boolean exito = usuarioDao.registrarEmpleado(u);
                if (exito) {
                    jsonRespuesta.put("status", "success");
                    jsonRespuesta.put("message", "Empleado creado exitosamente");
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "No se pudo crear el empleado");
                }
            }
            else if ("updateEmployee".equals(accion)) {
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                String idUsuario = jsonEntrada.getString("id");
                String name = jsonEntrada.getString("name");
                String email = jsonEntrada.getString("email");
                String role = jsonEntrada.getString("role");
                String status = jsonEntrada.getString("status");

                boolean exito = usuarioDao.actualizarEmpleado(idUsuario, name, email, role, status);
                if (exito) {
                    jsonRespuesta.put("status", "success");
                    jsonRespuesta.put("message", "Empleado actualizado");
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "No se pudo actualizar el empleado");
                }
            }
            else if ("deleteEmployee".equals(accion)) {
                JSONObject jsonEntrada = new JSONObject(cuerpoPeticion);
                String idUsuario = jsonEntrada.getString("id");

                boolean exito = usuarioDao.eliminarEmpleado(idUsuario);
                if (exito) {
                    jsonRespuesta.put("status", "success");
                    jsonRespuesta.put("message", "Empleado eliminado");
                } else {
                    jsonRespuesta.put("status", "error");
                    jsonRespuesta.put("message", "No se pudo eliminar el empleado");
                }
            }
            // Manejar acción no reconocida
            else {
                // Definir estado de error
                jsonRespuesta.put("status", "error");
                // Mensaje indicando que la acción es inválida
                jsonRespuesta.put("message", "Acción no válida");
            }
        } catch (Exception e) {
            // Asignar estado de error general
            jsonRespuesta.put("status", "error");
            // Mensaje de error detallado con la traza de la excepción
            jsonRespuesta.put("message", "Error interno en el servidor: " + e.getMessage());
        }

        // Imprimir el objeto JSON de respuesta formateado en la salida
        out.print(jsonRespuesta.toString());
    }

    // Sobrescribir el método doOptions para manejar las peticiones Preflight de CORS
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            // Declaración de excepciones asociadas
            throws ServletException, IOException {
        // Las cabeceras CORS son inyectadas de forma global por CorsFilter.java
        // Responder con un estado HTTP 200 OK
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Sobrescribir el método doGet para rechazar peticiones GET directas
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            // Declaración de excepciones asociadas
            throws ServletException, IOException {
        // Enviar error HTTP 405 (Method Not Allowed) indicando que use POST
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Utiliza peticiones POST.");
    }
}
