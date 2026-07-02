package Controlador;

import DAO.ProductoDAO;
import Modelo.Entidades.Producto;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/producto")
public class ProductoServlet extends HttpServlet {

    ProductoDAO dao =
            new ProductoDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        String accion = request.getParameter("accion");
        if ("listCategories".equals(accion)) {
            List<java.util.Map<String, String>> listaCat = dao.listarCategorias();
            JSONArray jsonArray = new JSONArray();
            for (java.util.Map<String, String> cat : listaCat) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("idCategoria", cat.get("idCategoria"));
                jsonItem.put("nombre", cat.get("nombre"));
                jsonArray.put(jsonItem);
            }
            response.getWriter().print(jsonArray.toString());
            return;
        }

        List<Producto> lista =
                dao.listar();

        StringBuilder json =
                new StringBuilder();

        json.append("[");

        for (
                int i = 0;
                i < lista.size();
                i++
        ) {

            Producto p =
                    lista.get(i);

            json.append("{");

            json.append(
                    "\"idProducto\":\""
                    + p.getIdProducto()
                    + "\","
            );

            json.append(
                    "\"nombre\":\""
                    + p.getNombre()
                    + "\","
            );

            json.append(
                    "\"descripcion\":\""
                    + p.getDescripcion()
                    + "\","
            );

            json.append(
                    "\"precio\":"
                    + p.getPrecio()
                    + ","
            );

            json.append(
                    "\"stock\":"
                    + p.getStock()
                    + ","
            );

            json.append(
                    "\"imagen\":\""
                    + p.getImagen()
                    + "\""
            );

            json.append("}");

            if (
                    i < lista.size() - 1
            ) {
                json.append(",");
            }
        }

        json.append("]");

        response
                .getWriter()
                .print(json.toString());
    }

    /**
     * Procesa peticiones HTTP POST para registrar un nuevo producto en la base de datos.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Log para rastrear la entrada al método doPost en la consola de NetBeans
        System.out.println("[INFO - ProductoServlet] doPost iniciado - Procesando registro de producto.");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonRespuesta = new JSONObject();

        try {
            // Leer el cuerpo de la petición que viaja en formato JSON
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();

            // Imprimir el cuerpo JSON recibido para depuración
            System.out.println("[DEBUG - ProductoServlet] Cuerpo JSON recibido en doPost: " + body);

            if (body.trim().isEmpty()) {
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "El cuerpo de la petición JSON está vacío");
                response.getWriter().print(jsonRespuesta.toString());
                return;
            }

            // Parsear datos de entrada
            JSONObject jsonEntrada = new JSONObject(body);
            String nombre = jsonEntrada.getString("name");
            double precio = jsonEntrada.getDouble("price");
            int stock = jsonEntrada.getInt("stock");
            String idCategoria = jsonEntrada.getString("category"); // ID de la categoría (ej: CAT-001)
            String imagen = jsonEntrada.optString("image", "");

            // Crear instancia de la entidad Producto
            Producto p = new Producto();
            p.setNombre(nombre);
            p.setPrecio(precio);
            p.setStock(stock);
            p.setIdCategoria(idCategoria);
            p.setImagen(imagen);

            // Guardar a través del DAO
            boolean registrado = dao.registrar(p);
            if (registrado) {
                System.out.println("[INFO - ProductoServlet] Producto registrado exitosamente en BD con ID: " + p.getIdProducto());
                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("message", "Producto registrado exitosamente en la base de datos");
                jsonRespuesta.put("idProducto", p.getIdProducto());
            } else {
                System.out.println("[ERROR - ProductoServlet] No se pudo registrar el producto mediante el DAO.");
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "No se pudo registrar el producto en la base de datos");
            }
        } catch (Exception e) {
            System.err.println("[EXCEPCIÓN - ProductoServlet] Error en doPost: " + e.getMessage());
            e.printStackTrace();
            jsonRespuesta.put("status", "error");
            jsonRespuesta.put("message", "Error interno en el servidor: " + e.getMessage());
        }

        response.getWriter().print(jsonRespuesta.toString());
    }

    /**
     * Procesa peticiones HTTP PUT para actualizar un producto existente.
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Log para rastrear la entrada al método doPut en la consola de NetBeans
        System.out.println("[INFO - ProductoServlet] doPut iniciado - Procesando actualización de producto.");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonRespuesta = new JSONObject();

        try {
            // Leer cuerpo de la petición
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();

            // Imprimir el cuerpo JSON recibido para depuración
            System.out.println("[DEBUG - ProductoServlet] Cuerpo JSON recibido en doPut: " + body);

            if (body.trim().isEmpty()) {
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "El cuerpo de la petición JSON está vacío");
                response.getWriter().print(jsonRespuesta.toString());
                return;
            }

            // Parsear datos actualizados
            JSONObject jsonEntrada = new JSONObject(body);
            String idProducto = jsonEntrada.getString("idProducto");
            String nombre = jsonEntrada.getString("name");
            double precio = jsonEntrada.getDouble("price");
            int stock = jsonEntrada.getInt("stock");
            String idCategoria = jsonEntrada.getString("category"); // ID de la categoría (ej: CAT-001)
            String imagen = jsonEntrada.optString("image", "");

            // Crear instancia de la entidad Producto a actualizar
            Producto p = new Producto();
            p.setIdProducto(idProducto);
            p.setNombre(nombre);
            p.setPrecio(precio);
            p.setStock(stock);
            p.setIdCategoria(idCategoria);
            p.setImagen(imagen);

            // Modificar a través del DAO
            boolean actualizado = dao.actualizar(p);
            if (actualizado) {
                System.out.println("[INFO - ProductoServlet] Producto ID " + idProducto + " actualizado exitosamente en BD.");
                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("message", "Producto actualizado exitosamente en la base de datos");
            } else {
                System.out.println("[ERROR - ProductoServlet] No se pudo actualizar el producto ID " + idProducto + " mediante el DAO.");
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "No se pudo actualizar el producto en la base de datos");
            }
        } catch (Exception e) {
            System.err.println("[EXCEPCIÓN - ProductoServlet] Error en doPut: " + e.getMessage());
            e.printStackTrace();
            jsonRespuesta.put("status", "error");
            jsonRespuesta.put("message", "Error interno en el servidor: " + e.getMessage());
        }

        response.getWriter().print(jsonRespuesta.toString());
    }

    /**
     * Procesa peticiones HTTP DELETE para eliminar un producto de la base de datos.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Log para rastrear la entrada al método doDelete en la consola de NetBeans
        System.out.println("[INFO - ProductoServlet] doDelete iniciado - Procesando eliminación de producto.");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonRespuesta = new JSONObject();

        try {
            // Leer el parámetro idProducto de la query string
            String idProducto = request.getParameter("idProducto");
            System.out.println("[DEBUG - ProductoServlet] Parámetro idProducto recibido: " + idProducto);

            if (idProducto == null || idProducto.trim().isEmpty()) {
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "Se requiere el parámetro 'idProducto' en la URL");
                response.getWriter().print(jsonRespuesta.toString());
                return;
            }

            // Eliminar a través del DAO
            boolean eliminado = dao.eliminar(idProducto);
            if (eliminado) {
                System.out.println("[INFO - ProductoServlet] Producto ID " + idProducto + " eliminado exitosamente de la BD.");
                jsonRespuesta.put("status", "success");
                jsonRespuesta.put("message", "Producto eliminado exitosamente de la base de datos");
            } else {
                System.out.println("[ERROR - ProductoServlet] No se pudo eliminar el producto ID " + idProducto + " mediante el DAO.");
                jsonRespuesta.put("status", "error");
                jsonRespuesta.put("message", "No se pudo eliminar el producto de la base de datos");
            }
        } catch (Exception e) {
            System.err.println("[EXCEPCIÓN - ProductoServlet] Error en doDelete: " + e.getMessage());
            e.printStackTrace();
            jsonRespuesta.put("status", "error");
            jsonRespuesta.put("message", "Error interno en el servidor: " + e.getMessage());
        }

        response.getWriter().print(jsonRespuesta.toString());
    }
}