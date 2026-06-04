// Declarar el paquete del DAO correspondiente a los datos de Historial
package DAO;

// Importar la clase de configuración de la conexión a la base de datos
import Modelo.Config.Conexion;
// Importar la entidad Historial para representar un pedido en Java
import Modelo.Entidades.Historial;
// Importar la clase Connection para manejar la conexión de base de datos
import java.sql.Connection;
// Importar PreparedStatement para ejecutar sentencias SQL preparadas
import java.sql.PreparedStatement;
// Importar ResultSet para recorrer el resultado de las consultas SQL
import java.sql.ResultSet;
// Importar SQLException para atrapar errores relacionales con MySQL
import java.sql.SQLException;
// Importar Statement para recuperar la llave autogenerada id_pedido
import java.sql.Statement;
// Importar ArrayList para el manejo de listas de pedidos
import java.util.ArrayList;
// Importar List para devolver listados estructurados de objetos Historial
import java.util.List;
// Importar JSONObject para manejar de forma interna la lista de productos de la orden
import org.json.JSONObject;
// Importar JSONArray para representar el listado de productos de forma estructurada
import org.json.JSONArray;

// Definición de la clase de acceso a datos para el Historial de pedidos
public class HistorialDAO {

    // Método auxiliar para asegurar que las tablas requeridas existan en MySQL
    private void crearTablasSiNoExisten(Connection con) {
        // Las tablas se gestionan externamente mediante turpial_basedatos.txt
    }

    // Método que registra un nuevo pedido con sus respectivos productos
    public boolean registrarPedido(Historial h, JSONArray productos) {
        // Query next id_pedido
        String queryMaxPedidoId = "SELECT id_pedido FROM pedido ORDER BY id_pedido DESC LIMIT 1";
        String nextPedidoId = "PED-001";
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxPedidoId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_pedido");
                if (maxId != null && maxId.startsWith("PED-")) {
                    try {
                        int num = Integer.parseInt(maxId.substring(4));
                        nextPedidoId = String.format("PED-%03d", num + 1);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing pedido ID: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting max pedido ID: " + e.getMessage());
        }

        h.setIdPedido(nextPedidoId);

        // Query max id_detallepedido
        String queryMaxDetalleId = "SELECT id_detallepedido FROM detallepedido ORDER BY id_detallepedido DESC LIMIT 1";
        int lastDetalleNum = 0;
        try (Connection con = Conexion.getConnection();
             PreparedStatement psMax = con.prepareStatement(queryMaxDetalleId);
             ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_detallepedido");
                if (maxId != null && maxId.startsWith("DET-")) {
                    try {
                        lastDetalleNum = Integer.parseInt(maxId.substring(4));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing detalle ID: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting max detalle ID: " + e.getMessage());
        }

        String sqlPedido = "INSERT INTO pedido (id_pedido, id_usuario, tipo_entrega, total, estado, fecha_pedido) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detallepedido (id_detallepedido, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlLookupProd = "SELECT id_producto FROM producto WHERE nombre = ?";

        try (Connection con = Conexion.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement psP = con.prepareStatement(sqlPedido)) {
                psP.setString(1, h.getIdPedido());
                psP.setString(2, h.getIdUsuario());
                psP.setString(3, "Para consumir aquí"); // Default enum value
                psP.setDouble(4, h.getTotal());
                psP.setString(5, "En preparación"); // Default valid enum value
                psP.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));

                int filasAfectadas = psP.executeUpdate();
                if (filasAfectadas == 0) {
                    con.rollback();
                    return false;
                }

                try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                     PreparedStatement psLookup = con.prepareStatement(sqlLookupProd)) {

                    for (int i = 0; i < productos.length(); i++) {
                        JSONObject prod = productos.getJSONObject(i);
                        String prodName = prod.getString("name");
                        double price = prod.getDouble("price");
                        int quantity = prod.getInt("quantity");

                        // Look up id_producto
                        String idProducto = "";
                        psLookup.setString(1, prodName);
                        try (ResultSet rsL = psLookup.executeQuery()) {
                            if (rsL.next()) {
                                idProducto = rsL.getString("id_producto");
                            } else {
                                idProducto = "PROD-001";
                            }
                        }

                        // Generate next id_detallepedido
                        lastDetalleNum++;
                        String nextDetalleId = String.format("DET-%03d", lastDetalleNum);

                        psD.setString(1, nextDetalleId);
                        psD.setString(2, h.getIdPedido());
                        psD.setString(3, idProducto);
                        psD.setInt(4, quantity);
                        psD.setDouble(5, price);
                        psD.setDouble(6, price * quantity); // subtotal
                        psD.addBatch();
                    }
                    psD.executeBatch();
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error en registrarPedido: " + e.getMessage());
        }
        return false;
    }

    // Método que obtiene la lista de pedidos realizados por un usuario
    public List<Historial> obtenerHistorialUsuario(String idUsuario) {
        List<Historial> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido WHERE id_usuario = ? ORDER BY fecha_pedido DESC";

        try (Connection con = Conexion.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Historial h = new Historial();
                        h.setIdPedido(rs.getString("id_pedido"));
                        h.setIdUsuario(rs.getString("id_usuario"));
                        h.setFecha(rs.getString("fecha_pedido"));
                        h.setTotal(rs.getDouble("total"));
                        h.setEstado(rs.getString("estado"));
                        lista.add(h);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR SQL HISTORIAL:");
            e.printStackTrace();
        }
        return lista;
    }
}