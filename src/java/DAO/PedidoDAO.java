package DAO;

import Modelo.Config.Conexion;
import Modelo.Entidades.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de
 * todas las operaciones SQL
 * relacionadas con pedidos.
 */
public class PedidoDAO {

    /**
     * CONSTRUCTOR
     *
     * Verifica si existe la columna
     * numero_mesa.
     */
    public PedidoDAO() {

        try (
            Connection con =
                Conexion.getConnection()
        ) {

            if (con != null) {

                try {

                    con.createStatement()
                            .executeUpdate(
                        "ALTER TABLE pedido "
                      + "ADD COLUMN numero_mesa INT NULL"
                    );

                    System.out.println(
                        "Columna numero_mesa agregada."
                    );

                } catch (SQLException e) {

                    /**
                     * Ignora el error
                     * si la columna ya existe.
                     */
                    if (
                        e.getErrorCode() != 1060
                    ) {

                        System.err.println(
                            "Error agregando columna: "
                            + e.getMessage()
                        );
                    }
                }
            }

        } catch (SQLException e) {

            System.err.println(
                "Error constructor PedidoDAO: "
                + e.getMessage()
            );
        }
    }

    /**
     * METODO PARA LISTAR PEDIDOS
     */
    public List<Pedido> listar() {

        List<Pedido> lista =
                new ArrayList<>();

        String sql =
            "SELECT * FROM pedido";

        try (

            Connection con =
                Conexion.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql);

            ResultSet rs =
                ps.executeQuery()

        ) {

            while (rs.next()) {

                Pedido p =
                    new Pedido();

                p.setIdPedido(
                    rs.getString("id_pedido")
                );

                p.setIdUsuario(
                    rs.getString("id_usuario")
                );

                p.setNombreClienteOpcional(
                    rs.getString(
                        "nombre_cliente_opcional"
                    )
                );

                p.setTipoEntrega(
                    rs.getString(
                        "tipo_entrega"
                    )
                );

                int numeroMesa =
                    rs.getInt("numero_mesa");

                /**
                 * VALIDAR NULL.
                 */
                if (rs.wasNull()) {

                    p.setNumeroMesa(null);

                } else {

                    p.setNumeroMesa(numeroMesa);
                }

                p.setDireccionEntrega(
                    rs.getString(
                        "direccion_entrega"
                    )
                );

                p.setObservaciones(
                    rs.getString(
                        "observaciones"
                    )
                );

                p.setTotal(
                    rs.getDouble("total")
                );

                p.setEstado(
                    rs.getString("estado")
                );

                p.setFechaPedido(
                    rs.getString(
                        "fecha_pedido"
                    )
                );

                lista.add(p);
            }

        } catch (SQLException e) {

            System.out.println(
                "ERROR SQL LISTAR PEDIDOS:"
            );

            e.printStackTrace();
        }

        return lista;
    }

    /**
     * METODO PARA REGISTRAR PEDIDOS
     */
    public boolean registrar(
            Pedido p
    ) {

        /**
         * Consulta para obtener
         * el último ID registrado.
         */
        String queryMaxId =
            "SELECT id_pedido "
          + "FROM pedido "
          + "ORDER BY id_pedido DESC "
          + "LIMIT 1";

        /**
         * ID inicial.
         */
        String nextId =
            "PED-001";

        /**
         * Generar siguiente ID.
         */
        try (

            Connection con =
                Conexion.getConnection();

            PreparedStatement psMax =
                con.prepareStatement(
                    queryMaxId
                );

            ResultSet rsMax =
                psMax.executeQuery()

        ) {

            if (rsMax.next()) {

                String maxId =
                    rsMax.getString(
                        "id_pedido"
                    );

                if (
                    maxId != null
                    &&
                    maxId.startsWith("PED-")
                ) {

                    int num =
                        Integer.parseInt(
                            maxId.substring(4)
                        );

                    nextId =
                        String.format(
                            "PED-%03d",
                            num + 1
                        );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                "Error obteniendo ID pedido: "
                + e.getMessage()
            );
        }

        /**
         * Asignar ID generado.
         */
        p.setIdPedido(nextId);

        /**
         * Consulta INSERT.
         */
        String sql =
            "INSERT INTO pedido "
          + "("
          + "id_pedido, "
          + "id_usuario, "
          + "nombre_cliente_opcional, "
          + "tipo_entrega, "
          + "numero_mesa, "
          + "direccion_entrega, "
          + "observaciones, "
          + "total, "
          + "estado, "
          + "fecha_pedido"
          + ") "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (

            Connection con =
                Conexion.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql)

        ) {

            ps.setString(
                1,
                p.getIdPedido()
            );

            ps.setString(
                2,
                p.getIdUsuario()
            );

            ps.setString(
                3,
                p.getNombreClienteOpcional()
            );

            ps.setString(
                4,
                p.getTipoEntrega()
            );

            /**
             * Validar si numeroMesa
             * es null.
             */
            if (
                p.getNumeroMesa()
                != null
            ) {

                ps.setInt(
                    5,
                    p.getNumeroMesa()
                );

            } else {

                ps.setNull(
                    5,
                    java.sql.Types.INTEGER
                );
            }

            ps.setString(
                6,
                p.getDireccionEntrega()
            );

            ps.setString(
                7,
                p.getObservaciones()
            );

            ps.setDouble(
                8,
                p.getTotal()
            );

            ps.setString(
                9,
                p.getEstado()
            );

            /**
             * Ejecutar INSERT.
             */
            return
                ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                "ERROR SQL REGISTRAR PEDIDO:"
            );

            e.printStackTrace();
        }

        return false;
    }
}