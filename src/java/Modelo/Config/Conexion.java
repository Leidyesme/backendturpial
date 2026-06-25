package Modelo.Config;

// Importar clases necesarias para establecer y controlar la conexión JDBC con la base de datos MySQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase encargada de la conexión y desconexión con la base de datos 'turpial'.
 * Carga de manera dinámica las credenciales y parámetros de conexión de
 * variables de entorno utilizando EnvConfig, cumpliendo las reglas de portabilidad.
 */
public class Conexion {
    
    // URL de la base de datos cargada dinámicamente; por defecto apunta a localhost
    private static final String URL = EnvConfig.get("DB_URL", "jdbc:mysql://localhost:3306/turpial?serverTimezone=UTC&useSSL=false");
    
    // Nombre del usuario de la BD cargado dinámicamente; por defecto es root
    private static final String USER = EnvConfig.get("DB_USER", "root");
    
    // Contraseña de la BD cargada dinámicamente; por defecto es la contraseña por defecto
    private static final String PASSWORD = EnvConfig.get("DB_PASSWORD", "#Aprendiz2024");
    
    // Nombre de la clase del Driver de MySQL para realizar el puente de conexión
    private static final String DRIVER = EnvConfig.get("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
    
    /**
     * Método estático encargado de crear y retornar un objeto Connection activo.
     * Realiza el registro del driver JDBC y la autenticación con la base de datos.
     * 
     * @return Connection activa o null si ocurre algún fallo.
     */
    public static Connection getConnection() {
        // Inicializar objeto de conexión como nulo
        Connection conexion = null;
        try {
            // Registrar el Driver de MySQL en el cargador de clases en tiempo de ejecución
            Class.forName(DRIVER);
            
            // Establecer la conexión utilizando la URL, usuario y contraseña provistos por variables de entorno
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            
            // Registrar éxito de conexión en la consola del servidor
            System.out.println("conectando a:"+URL);
        } catch (ClassNotFoundException e) {
            // Capturar error si el driver JDBC de MySQL no está presente en el classpath
            System.err.println("Error: No se encontró el Driver de MySQL -> " + e.getMessage());
        } catch (SQLException e) {
            // Capturar error en la autenticación o configuración de red del servidor MySQL
            System.err.println("Error de SQL al conectar a la base de datos -> " + e.getMessage());
        }
        // Retornar el objeto de conexión
        return conexion;
    }
}