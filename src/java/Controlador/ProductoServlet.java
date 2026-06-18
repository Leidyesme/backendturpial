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
}