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

import java.util.List;

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
            throws ServletException,
            IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        /**
         * Crear objeto pedido.
         */
        Pedido p =
                new Pedido();

        /**
         * Obtener datos
         * enviados desde frontend.
         */
        p.setIdUsuario(
            request.getParameter(
                "idUsuario"
            )
        );

        p.setNombreClienteOpcional(
            request.getParameter(
                "nombreClienteOpcional"
            )
        );

        p.setTipoEntrega(
            request.getParameter(
                "tipoEntrega"
            )
        );

        /**
         * Validar número mesa.
         */
        String mesa =
            request.getParameter(
                "numeroMesa"
            );

        if (
            mesa != null
            &&
            !mesa.isEmpty()
        ) {

            p.setNumeroMesa(
                Integer.parseInt(mesa)
            );
        }

        p.setDireccionEntrega(
            request.getParameter(
                "direccionEntrega"
            )
        );

        p.setObservaciones(
            request.getParameter(
                "observaciones"
            )
        );

        p.setTotal(
            Double.parseDouble(
                request.getParameter(
                    "total"
                )
            )
        );

        /**
         * Estado inicial.
         */
        p.setEstado(
            "En preparación"
        );

        /**
         * Validación:
         * consumo local requiere mesa.
         */
        if (
            p.getTipoEntrega()
                .equalsIgnoreCase(
                    "Para consumir aquí"
                )
            &&
            p.getNumeroMesa()
                == null
        ) {

            response.getWriter().write(
                "{"
                + "\"status\":\"error\","
                + "\"message\":\"Debe ingresar número de mesa.\""
                + "}"
            );

            return;
        }

        /**
         * Registrar pedido.
         */
        boolean registrado =
                dao.registrar(p);

        /**
         * Respuesta final.
         */
        if (registrado) {

            response.getWriter().write(
                "{"
                + "\"status\":\"success\","
                + "\"message\":\"Pedido registrado correctamente.\""
                + "}"
            );

        } else {

            response.getWriter().write(
                "{"
                + "\"status\":\"error\","
                + "\"message\":\"No se pudo registrar el pedido.\""
                + "}"
            );
        }
    }
}