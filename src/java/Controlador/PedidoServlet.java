package Controlador;

import DAO.PedidoDAO;
import Modelo.Entidades.Pedido;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;


/**
 * Servlet encargado
 * de manejar pedidos.
 */
@WebServlet("/pedido")
public class PedidoServlet extends HttpServlet {

    /**
     * DAO de pedidos.
     */
    PedidoDAO dao =
            new PedidoDAO();

    /**
     * METODO GET
     *
     * Lista pedidos.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException,
            IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        /**
         * Obtener lista
         * de pedidos.
         */
        List<Pedido> lista =
                dao.listar();

        /**
         * Construir JSON manualmente.
         */
        StringBuilder json =
                new StringBuilder();

        json.append("[");

        for (
            int i = 0;
            i < lista.size();
            i++
        ) {

            Pedido p =
                    lista.get(i);

            json.append("{");

            json.append(
                "\"idPedido\":\""
                + p.getIdPedido()
                + "\","
            );

            json.append(
                "\"idUsuario\":\""
                + p.getIdUsuario()
                + "\","
            );

            json.append(
                "\"nombreClienteOpcional\":\""
                + p.getNombreClienteOpcional()
                + "\","
            );

            json.append(
                "\"tipoEntrega\":\""
                + p.getTipoEntrega()
                + "\","
            );

            json.append(
                "\"numeroMesa\":\""
                + p.getNumeroMesa()
                + "\","
            );

            json.append(
                "\"direccionEntrega\":\""
                + p.getDireccionEntrega()
                + "\","
            );

            json.append(
                "\"observaciones\":\""
                + p.getObservaciones()
                + "\","
            );

            json.append(
                "\"total\":\""
                + p.getTotal()
                + "\","
            );

            json.append(
                "\"estado\":\""
                + p.getEstado()
                + "\","
            );

            json.append(
                "\"fechaPedido\":\""
                + p.getFechaPedido()
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

        /**
         * Enviar respuesta.
         */
        PrintWriter out =
                response.getWriter();

        out.print(json.toString());

        out.flush();
    }

    /**
     * METODO POST
     *
     * Registrar pedido.
     */
    @Override

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    )

    throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            // LEER JSON ENVIADO
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            BufferedReader reader =request.getReader();

            while ((line = reader.readLine())!= null) {
                jsonBuilder.append(line);
            }

            // CONVERTIR A JSON
            JSONObject json =new JSONObject(jsonBuilder.toString());

            // CREAR OBJETO PEDIDO
            Pedido p =new Pedido();

            // OBTENER DATOS

            p.setIdUsuario(json.optString("idUsuario",null));
            p.setNombreClienteOpcional(json.getString("nombreClienteOpcional"));
            p.setTipoEntrega(json.getString("tipoEntrega"));

            // NUMERO DE MESA
            if (!json.isNull("numeroMesa")) {

                p.setNumeroMesa(json.getInt("numeroMesa"));

            } else {
                p.setNumeroMesa(null);
            }

            // DIRECCION
            p.setDireccionEntrega(json.optString("direccionEntrega",null));

            // OBSERVACIONES
            p.setObservaciones(json.optString("observaciones",""));

            // TOTAL
            p.setTotal(json.getDouble("total"));

            // ESTADO
            p.setEstado("En preparación");

            // VALIDAR MESA
            if ("Para consumir aquí".equalsIgnoreCase(p.getTipoEntrega())&&p.getNumeroMesa()== null) {

                response.getWriter().write("{"+ "\"status\":\"error\","+ "\"message\":\"Ingrese número de mesa\""+ "}");
                return;
            }

            // MOSTRAR EN CONSOLA
            System.out.println("Cliente: "+ p.getNombreClienteOpcional());

            System.out.println("Total: "+ p.getTotal());

            // REGISTRAR
            boolean registrado =dao.registrar(p);

            // RESPUESTA
            if (registrado) {

                response.getWriter().write("{"+ "\"status\":\"success\","+ "\"message\":\"Pedido registrado correctamente\""+ "}");

            } else {

                response.getWriter().write("{"+ "\"status\":\"error\","+ "\"message\":\"Error registrando pedido\""+ "}");
            }

        } catch (Exception e) {

            e.printStackTrace();
            response.getWriter().write("{"+ "\"status\":\"error\","+ "\"message\":\""+ e.getMessage()+ "\""+ "}");
        }
    }
}