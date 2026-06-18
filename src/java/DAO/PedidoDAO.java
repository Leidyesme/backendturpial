package DAO;

// Importar la clase de conexión para conectarse a la base de datos MySQL
import Modelo.Config.Conexion;
// Importar la entidad Pedido para mapear registros relacionales a objetos Java
import Modelo.Entidades.Pedido;

// Importar interfaces necesarias de JDBC para interactuar con la base de datos relacional
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Importar clases de colecciones estándar de Java para almacenar las listas de registros
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Clase de Acceso a Datos (DAO) para la entidad Pedido.
 * Encargada de realizar operaciones CRUD en la tabla 'pedido' de MySQL.
 */
public class PedidoDAO {

    /**
     * Constructor por defecto.
     * Se ha removido la ejecución de sentencias DDL (ALTER TABLE) para evitar fallos de
     * seguridad y permisos denegados cuando se usan roles de base de datos restringidos.
     */
    public PedidoDAO() {
        // Inicialización básica del DAO (sin operaciones DDL dinámicas)
    }

    /**
     * Recupera y lista todos los pedidos registrados en la tabla 'pedido'.
     *
     * @return Una lista de objetos de tipo Pedido.
     */
    public List<Pedido> listar() {
        // Inicializar lista para almacenar los pedidos recuperados
        List<Pedido> lista = new ArrayList<>();
        
        // Consulta SQL para seleccionar todas las columnas de la tabla pedido
        String sql = "SELECT * FROM pedido";

        // Usar try-with-resources para asegurar el cierre automático de la conexión, sentencia y conjunto de resultados
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            // Recorrer el conjunto de resultados fila por fila
            while (rs.next()) {
                // Instanciar un nuevo objeto Pedido para mapear la fila actual
                Pedido p = new Pedido();

                // Mapear la columna id_pedido
                p.setIdPedido(rs.getString("id_pedido"));

                // Mapear la columna id_usuario (puede ser nulo si no está registrado)
                p.setIdUsuario(rs.getString("id_usuario"));

                // Mapear el nombre del cliente opcional (para pedidos sin usuario registrado)
                p.setNombreClienteOpcional(rs.getString("customer_name"));

                // Mapear el tipo de entrega (enum de base de datos)
                p.setTipoEntrega(rs.getString("tipo_entrega"));

                // Recuperar el número de mesa como entero básico
                int numeroMesa = rs.getInt("numero_mesa");

                // Validar si el valor obtenido de la base de datos es NULL en SQL
                if (rs.wasNull()) {
                    // Si era null, asignar null a la propiedad Integer del objeto
                    p.setNumeroMesa(null);
                } else {
                    // En caso contrario, asignar el valor numérico recuperado
                    p.setNumeroMesa(numeroMesa);
                }

                // Mapear la dirección de entrega
                p.setDireccionEntrega(rs.getString("direccion_entrega"));

                // Mapear las observaciones adicionales
                p.setObservaciones(rs.getString("observaciones"));

                // Mapear el monto total
                p.setTotal(rs.getDouble("total"));

                // Mapear el estado del pedido
                p.setEstado(rs.getString("estado"));

                // Mapear la fecha y hora de creación del pedido
                p.setFechaPedido(rs.getString("fecha_pedido"));

                // Agregar el objeto mapeado a la lista de retorno
                lista.add(p);
            }

        } catch (SQLException e) {
            // Manejar y reportar excepciones de base de datos en consola
            System.err.println("ERROR SQL AL LISTAR PEDIDOS: " + e.getMessage());
            e.printStackTrace();
        }

        // Retornar la lista resultante
        return lista;
    }

    /**
     * Registra un nuevo pedido en la base de datos generando automáticamente
     * un identificador incremental con formato 'PED-XXX'.
     *
     * @param p Objeto Pedido con la información a insertar.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public boolean registrar(Pedido p, JSONArray productos) {
        // Consulta SQL para buscar el último ID de pedido registrado ordenado de forma descendente
        String queryMaxId = "SELECT id_pedido FROM pedido ORDER BY id_pedido DESC LIMIT 1";
        
        // Identificador por defecto si la tabla está completamente vacía
        String nextId = "PED-001";

        // Obtener la conexión y buscar el último ID registrado para calcular el siguiente
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement psMax = con.prepareStatement(queryMaxId);
            ResultSet rsMax = psMax.executeQuery()
        ) {
            // Si existe al menos un registro en la tabla
            if (rsMax.next()) {
                // Recuperar el identificador del pedido
                String maxId = rsMax.getString("id_pedido");

                // Validar que el ID no sea nulo y cumpla el formato esperado 'PED-'
                if (maxId != null && maxId.startsWith("PED-")) {
                    // Extraer la parte numérica del ID, parsearla e incrementarla en 1
                    int num = Integer.parseInt(maxId.substring(4));
                    // Formatear el nuevo ID completando con ceros a la izquierda
                    nextId = String.format("PED-%03d", num + 1);
                }
            }
        } catch (SQLException e) {
            // Registrar advertencia si falla el cálculo del ID, pero continuar usando el ID por defecto
            System.err.println("Advertencia obteniendo ID máximo de pedido (se usará PED-001): " + e.getMessage());
        }

        // Asignar el identificador incremental al objeto pedido
        p.setIdPedido(nextId);

        // Obtener el consecutivo del detalle de pedido
        String queryMaxDetalleId = "SELECT id_detallepedido FROM detallepedido ORDER BY id_detallepedido DESC LIMIT 1";
        int lastDetalleNum = 0;
        try (
            Connection con = Conexion.getConnection();
            PreparedStatement psMaxD = con.prepareStatement(queryMaxDetalleId);
            ResultSet rsMaxD = psMaxD.executeQuery()
        ) {
            if (rsMaxD.next()) {
                String maxDetId = rsMaxD.getString("id_detallepedido");
                if (maxDetId != null && maxDetId.startsWith("DET-")) {
                    lastDetalleNum = Integer.parseInt(maxDetId.substring(4));
                }
            }
        } catch (SQLException e) {
            System.err.println("Advertencia obteniendo ID máximo de detalle: " + e.getMessage());
        }

        // Sentencia SQL parametrizada para insertar la información del pedido
        String sql = "INSERT INTO pedido "
                  + "(id_pedido, id_usuario, customer_name, tipo_entrega, numero_mesa, direccion_entrega, observaciones, total, estado, fecha_pedido) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        String sqlDetalle = "INSERT INTO detallepedido (id_detallepedido, id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)";

        // Usar try-with-resources para ejecutar de forma segura la inserción en la BD
        try (Connection con = Conexion.getConnection()) {
            con.setAutoCommit(false); // Transaccionalidad

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                // Asignar el ID de pedido al parámetro 1
                ps.setString(1, p.getIdPedido());

                // Asignar el ID de usuario (puede ser null para clientes no registrados)
                ps.setString(2, p.getIdUsuario());

                // Asignar el nombre del cliente
                ps.setString(3, p.getNombreClienteOpcional());

                // Asignar el tipo de entrega
                ps.setString(4, p.getTipoEntrega());

                // Validar si el número de mesa es null
                if (p.getNumeroMesa() != null) {
                    // Asignar el número de mesa al parámetro 5 si está definido
                    ps.setInt(5, p.getNumeroMesa());
                } else {
                    // Asignar tipo NULL de base de datos al parámetro 5 si es nulo
                    ps.setNull(5, java.sql.Types.INTEGER);
                }

                // Asignar la dirección de entrega
                ps.setString(6, p.getDireccionEntrega());

                // Asignar observaciones adicionales
                ps.setString(7, p.getObservaciones());

                // Asignar el monto total
                ps.setDouble(8, p.getTotal());

                // Asignar el estado del pedido
                ps.setString(9, p.getEstado());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    con.rollback();
                    return false;
                }

                // Insertar detalles del pedido
                if (productos != null && productos.length() > 0) {
                    try (PreparedStatement psD = con.prepareStatement(sqlDetalle)) {
                        for (int i = 0; i < productos.length(); i++) {
                            JSONObject prod = productos.getJSONObject(i);
                            lastDetalleNum++;
                            String nextDetId = String.format("DET-%03d", lastDetalleNum);

                            psD.setString(1, nextDetId);
                            psD.setString(2, p.getIdPedido());
                            psD.setString(3, prod.getString("idProducto"));
                            psD.setInt(4, prod.getInt("quantity"));
                            psD.setDouble(5, prod.getDouble("price"));
                            psD.addBatch();
                        }
                        psD.executeBatch();
                    }
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Registrar error en caso de fallo durante la inserción
            System.err.println("ERROR SQL AL REGISTRAR PEDIDO: " + e.getMessage());
            e.printStackTrace();
        }

        // Retornar falso si ocurrió algún error y la inserción falló
        return false;
    }
}