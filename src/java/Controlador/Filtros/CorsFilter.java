package Controlador.Filtros; // Qué hace: Declara el paquete Controlador.Filtros. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Controladores y Filtros HTTP de la arquitectura del proyecto.

import jakarta.servlet.Filter; // Qué hace: Importa la interfaz Filter de Jakarta EE. | Para qué sirve / Destino: Define el contrato estándar para interceptar peticiones y respuestas HTTP antes o después de llegar a los servlets o controladores.
import jakarta.servlet.FilterChain; // Qué hace: Importa la clase FilterChain de Jakarta EE. | Para qué sirve / Destino: Permite pasar el flujo de la petición HTTP hacia el siguiente eslabón de la cadena, ya sea otro filtro o el servlet controlador final.
import jakarta.servlet.FilterConfig; // Qué hace: Importa la interfaz FilterConfig de Jakarta EE. | Para qué sirve / Destino: Proporciona los parámetros de configuración inicial del filtro durante su ciclo de vida en el contenedor web.
import jakarta.servlet.ServletException; // Qué hace: Importa la excepción ServletException. | Para qué sirve / Destino: Permite manejar y propagar errores generales ocurridos durante el procesamiento del filtro en el servidor web.
import jakarta.servlet.ServletRequest; // Qué hace: Importa la interfaz ServletRequest. | Para qué sirve / Destino: Representa la solicitud genérica recibida por el servidor antes de ser casteada a HTTP.
import jakarta.servlet.ServletResponse; // Qué hace: Importa la interfaz ServletResponse. | Para qué sirve / Destino: Representa la respuesta genérica antes de ser devuelta al cliente web.
import jakarta.servlet.annotation.WebFilter; // Qué hace: Importa la anotación @WebFilter. | Para qué sirve / Destino: Permite registrar y mapear el filtro de forma automática en el contenedor de servlets sin requerir configuración manual en web.xml.
import jakarta.servlet.http.HttpServletRequest; // Qué hace: Importa la interfaz HttpServletRequest. | Para qué sirve / Destino: Proporciona métodos específicos para interactuar con la petición HTTP entrante desde el frontend.
import jakarta.servlet.http.HttpServletResponse; // Qué hace: Importa la interfaz HttpServletResponse. | Para qué sirve / Destino: Permite configurar las cabeceras HTTP y el estado de la respuesta enviada de vuelta hacia el navegador.
import java.io.IOException; // Qué hace: Importa la excepción IOException. | Para qué sirve / Destino: Maneja errores de entrada y salida durante la transferencia de datos de la petición y respuesta HTTP.

// Este filtro interceptará absolutamente todas las peticiones (/*)
@WebFilter("/*") // Qué hace: Anota la clase como un filtro web activo para todas las rutas y endpoints de la aplicación. | Para qué sirve / Destino: Intercepta cada solicitud proveniente del frontend antes de que llegue a cualquier controlador o servlet de la aplicación.
public class CorsFilter implements Filter { // Qué hace: Declara la clase pública CorsFilter que implementa la interfaz Filter. | Para qué sirve / Destino: Actúa como un filtro de seguridad y compatibilidad CORS (Cross-Origin Resource Sharing) para permitir la comunicación entre el frontend y el backend en Java.

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { // Qué hace: Sobrescribe el método init del ciclo de vida del filtro. | Para qué sirve / Destino: Permite inicializar recursos o configuraciones opcionales al arrancar el servidor web.
        // Método de inicialización (opcional)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) // Qué hace: Sobrescribe el método principal doFilter que intercepta las peticiones y respuestas. | Para qué sirve / Destino: Ejecuta la lógica de inyección de cabeceras CORS para habilitar el intercambio de recursos entre orígenes cruzados hacia los controladores.
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request; // Qué hace: Convierte el objeto ServletRequest genérico en un HttpServletRequest específico para HTTP. | Para qué sirve / Destino: Permite consultar detalles de la petición del cliente, como el método HTTP (GET, POST, OPTIONS).
        HttpServletResponse res = (HttpServletResponse) response; // Qué hace: Convierte el objeto ServletResponse genérico en un HttpServletResponse específico para HTTP. | Para qué sirve / Destino: Habilita la manipulación de cabeceras de respuesta que se enviarán de regreso al navegador del usuario.

        // Permite el acceso desde cualquier origen (puedes cambiar "*" por http://127.0.0.1:5500 si usas Live Server)
        res.setHeader("Access-Control-Allow-Origin", "*"); // Qué hace: Configura la cabecera HTTP Access-Control-Allow-Origin con el valor "*". | Para qué sirve / Destino: Autoriza a cualquier dominio o cliente frontend externo a consumir los servicios y controladores de la aplicación Java.
        
        // Métodos HTTP permitidos para las peticiones del frontend
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Qué hace: Configura la cabecera HTTP Access-Control-Allow-Methods especificando los verbos permitidos. | Para qué sirve / Destino: Indica a los navegadores qué operaciones HTTP están autorizadas al interactuar con los controladores del backend.
        
        // Cabeceras permitidas (esencial si luego envías JSON o tokens de autenticación)
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With"); // Qué hace: Configura la cabecera HTTP Access-Control-Allow-Headers permitiendo metadatos específicos. | Para qué sirve / Destino: Permite el envío de contenido estructurado en JSON o tokens de seguridad hacia los controladores.
        
        // Tiempo en segundos que el navegador puede guardar en caché esta configuración CORS
        res.setHeader("Access-Control-Max-Age", "3600"); // Qué hace: Configura la cabecera Access-Control-Max-Age con un tiempo de caché de 3600 segundos (1 hora). | Para qué sirve / Destino: Evita que el navegador realice peticiones de verificación repetitivas (preflight), optimizando el rendimiento de la comunicación con el servidor.

        // Si es una petición de tipo OPTIONS (Preflight), respondemos OK inmediatamente sin pasar al Servlet
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) { // Qué hace: Evalúa si el método HTTP de la solicitud entrante es de tipo OPTIONS. | Para qué sirve / Destino: Identifica las peticiones de comprobación previa que envían los navegadores antes de ejecutar solicitudes complejas de la API.
            res.setStatus(HttpServletResponse.SC_OK); // Qué hace: Establece el código de estado HTTP 200 (OK) en la respuesta. | Para qué sirve / Destino: Responde de manera inmediata a la validación del navegador sin saturar los controladores de la aplicación.
        } else {
            // Si es una petición normal (GET, POST, etc.), continúa su camino hacia el Servlet correspondiente
            chain.doFilter(request, response); // Qué hace: Invoca el método doFilter del objeto FilterChain para continuar con el flujo normal de la petición. | Para qué sirve / Destino: Envía la solicitud directamente hacia el controlador o servlet encargado de gestionar la lógica de negocio y los DAOs.
        }
    }

    @Override
    public void destroy() { // Qué hace: Sobrescribe el método destroy del ciclo de vida del filtro. | Para qué sirve / Destino: Permite liberar recursos o conexiones al detener la aplicación web en el servidor.
        // Método de destrucción (opcional)
    }
}