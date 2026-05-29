package Controlador.Filtros;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Este filtro interceptará absolutamente todas las peticiones (/*)
@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Método de inicialización (opcional)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Permite el acceso desde cualquier origen (puedes cambiar "*" por http://127.0.0.1:5500 si usas Live Server)
        res.setHeader("Access-Control-Allow-Origin", "*");
        
        // Métodos HTTP permitidos para las peticiones del frontend
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        
        // Cabeceras permitidas (esencial si luego envías JSON o tokens de autenticación)
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        
        // Tiempo en segundos que el navegador puede guardar en caché esta configuración CORS
        res.setHeader("Access-Control-Max-Age", "3600");

        // Si es una petición de tipo OPTIONS (Preflight), respondemos OK inmediatamente sin pasar al Servlet
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Si es una petición normal (GET, POST, etc.), continúa su camino hacia el Servlet correspondiente
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Método de destrucción (opcional)
    }
}