package Controlador;

import DAO.ProductoDAO;
import Modelo.Entidades.Producto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet que maneja las peticiones HTTP para el catálogo de productos.
 * Implementa operaciones CRUD (Crear, Leer, Actualizar, Borrar) mediante verbos HTTP.
 */
@WebServlet("/producto")
public class ProductoServlet extends HttpServlet {

    private final ProductoDAO dao = new ProductoDAO();

    /**
     * Procesa peticiones GET para listar productos o categorías.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        // Listar categorías
        if ("listCategories".equals(accion)) {
            List<Map<String, String>> listaCat = dao.listarCategorias();
            JSONArray jsonArray = new JSONArray();
            for (Map<String, String> cat : listaCat) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("idCategoria", cat.get("idCategoria"));
                jsonItem.put("nombre", cat.get("nombre"));
                jsonArray.put(jsonItem);
            }
            response.getWriter().print(jsonArray.toString());
            return;
        }

        // Listar productos
        List<Producto> lista = dao.listar();
        JSONArray jsonArray = new JSONArray();
        for (Producto p : lista) {
            JSONObject item = new JSONObject();
            item.put("idProducto", p.getIdProducto());
            item.put("nombre", p.getNombre());
            item.put("descripcion", p.getDescripcion());
            item.put("precio", p.getPrecio());
            item.put("stock", p.getStock());
            item.put("imagen", p.getImagen());
            jsonArray.put(item);
        }
        response.getWriter().print(jsonArray.toString());
    }

    /**
     * Procesa peticiones POST para registrar un nuevo producto.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonRespuesta = new JSONObject();

        try {
            String body = leerCuerpo(request);
            if (body.trim().isEmpty()) {
                enviarError(response, "Cuerpo JSON vacío");
                return;
            }

            JSONObject jsonEntrada = new JSONObject(body);
            Producto p = new Producto();
            p.setNombre(jsonEntrada.getString("name"));
            p.setPrecio(jsonEntrada.getDouble("price"));
            p.setStock(jsonEntrada.getInt("stock"));
            p.setIdCategoria(jsonEntrada.getString("category"));
            p.setImagen(jsonEntrada.optString("image", ""));

            if (dao.registrar(p)) {
                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("message", "Producto registrado exitosamente");
                jsonRespuesta.put("idProducto", p.getIdProducto());
            } else {
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "No se pudo registrar el producto");
            }
        } catch (Exception e) {
            jsonRespuesta.put("status", "error");
            jsonRespuesta.put("message", "Error: " + e.getMessage());
        }
        response.getWriter().print(jsonRespuesta.toString());
    }

    /**
     * Procesa peticiones PUT para actualizar un producto existente.
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        JSONObject jsonRespuesta = new JSONObject();

        try {
            String body = leerCuerpo(request);
            JSONObject jsonEntrada = new JSONObject(body);
            
            Producto p = new Producto();
            
            String rawId = String.valueOf(jsonEntrada.get("idProducto"));
            if (rawId.matches("\\d+")) {
                rawId = "PROD-" + String.format("%03d", Integer.parseInt(rawId));
            }
            p.setIdProducto(rawId);

            p.setNombre(jsonEntrada.getString("name"));
            p.setPrecio(jsonEntrada.getDouble("price"));
            p.setStock(jsonEntrada.getInt("stock"));

            String rawCat = String.valueOf(jsonEntrada.get("category"));
            if (rawCat.matches("\\d+")) {
                rawCat = "CAT-" + String.format("%03d", Integer.parseInt(rawCat));
            }
            p.setIdCategoria(rawCat);

            p.setImagen(jsonEntrada.optString("image", ""));

            if (dao.actualizar(p)) {
                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("message", "Producto actualizado correctamente");
            } else {
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "No se pudo actualizar");
            }
        } catch (Exception e) {
            jsonRespuesta.put("status", "error");
            jsonRespuesta.put("message", "Error: " + e.getMessage());
        }
        response.getWriter().print(jsonRespuesta.toString());
    }

    /**
     * Procesa peticiones DELETE para eliminar un producto.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        String idProducto = request.getParameter("idProducto");
        JSONObject jsonRespuesta = new JSONObject();

        if (idProducto != null && dao.eliminar(idProducto)) {
            jsonRespuesta.put("status", "success");
            jsonRespuesta.put("message", "Producto eliminado");
        } else {
            jsonRespuesta.put("status", "error");
            jsonRespuesta.put("message", "No se pudo eliminar");
        }
        response.getWriter().print(jsonRespuesta.toString());
    }

    // Métodos auxiliares para limpieza de código
    private String leerCuerpo(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private void enviarError(HttpServletResponse response, String mensaje) throws IOException {
        JSONObject err = new JSONObject();
        err.put("status", "error");
        err.put("message", mensaje);
        response.getWriter().print(err.toString());
    }
}