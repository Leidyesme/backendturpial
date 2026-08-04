package Modelo.Config; // Qué hace: Declara el paquete Modelo.Config. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Configuración de la arquitectura del proyecto.

// Importar clases necesarias para establecer y controlar la conexión JDBC con la base de datos MySQL
import java.sql.Connection; // Qué hace: Importa la interfaz Connection de JDBC. | Para qué sirve / Destino: Permite manejar el canal de comunicación activo con la base de datos MySQL.
import java.sql.DriverManager; // Qué hace: Importa la clase DriverManager de JDBC. | Para qué sirve / Destino: Facilita la gestión de los drivers de base de datos y la creación de sesiones de conexión.
import java.sql.SQLException; // Qué hace: Importa la clase SQLException de JDBC. | Para qué sirve / Destino: Permite capturar y manejar los errores específicos generados durante las operaciones con la base de datos.

/**
 * Clase encargada de la conexión y desconexión con la base de datos 'turpial'.
 * Carga de manera dinámica las credenciales y parámetros de conexión de
 * variables de entorno utilizando EnvConfig, cumpliendo las reglas de portabilidad.
 */
public class Conexion { // Qué hace: Declara la clase pública Conexion. | Para qué sirve / Destino: Actúa como el componente centralizador de la persistencia para proveer canales de comunicación hacia la base de datos MySQL para todos los DAOs del proyecto.
    
    // URL de la base de datos cargada dinámicamente; por defecto apunta a localhost
    private static final String URL = EnvConfig.get("DB_URL", "jdbc:mysql://localhost:3306/turpial?serverTimezone=UTC&useSSL=false"); // Qué hace: Declara una constante privada estática que define la ruta de conexión a MySQL mediante EnvConfig o un valor por defecto. | Para qué sirve / Destino: Suministra los parámetros de red, puerto e instancia de la base de datos MySQL requeridos por los DAOs.
    
    // Nombre del usuario de la BD cargado dinámicamente; por defecto es root
    private static final String USER = EnvConfig.get("DB_USER", "root"); // Qué hace: Declara una constante privada estática que almacena el usuario de la base de datos. | Para qué sirve / Destino: Proporciona la credencial de autenticación inicial requerida para acceder al servidor MySQL.
    
    // Contraseña de la BD cargada dinámicamente; por defecto es la contraseña por defecto
    private static final String PASSWORD = EnvConfig.get("DB_PASSWORD", "root"); // Qué hace: Declara una constante privada estática que almacena la contraseña de la base de datos. | Para qué sirve / Destino: Proporciona la credencial secreta de seguridad para validar el acceso al servidor MySQL.
    
    // Nombre de la clase del Driver de MySQL para realizar el puente de conexión
    private static final String DRIVER = EnvConfig.get("DB_DRIVER", "com.mysql.cj.jdbc.Driver"); // Qué hace: Declara una constante privada estática con la ruta del Driver JDBC de MySQL. | Para qué sirve / Destino: Define el conector técnico necesario para realizar el puente de comunicación con la base de datos relacional.
    
    /**
     * Método estático encargado de crear y retornar un objeto Connection activo.
     * Realiza el registro del driver JDBC y la autenticación con la base de datos.
     * 
     * @return Connection activa o null si ocurre algún fallo.
     */
    public static Connection getConnection() { // Qué hace: Declara el método público estático getConnection que retorna un objeto Connection. | Para qué sirve / Destino: Proporciona una sesión de conexión activa lista para ser utilizada por cualquier DAO al realizar consultas o transacciones en MySQL.
        // Inicializar objeto de conexión como nulo
        Connection conexion = null; // Qué hace: Declara e inicializa una variable local de tipo Connection en null. | Para qué sirve / Destino: Prepara el contenedor del canal de comunicación antes de intentar la conexión con la base de datos.
        try { // Qué hace: Inicia un bloque de control de excepciones (try-catch). | Para qué sirve / Destino: Captura posibles fallos de clases o de SQL durante el proceso de conexión con MySQL.
            // Registrar el Driver de MySQL en el cargador de clases en tiempo de ejecución
            Class.forName(DRIVER); // Qué hace: Carga dinámicamente la clase del Driver JDBC en memoria. | Para qué sirve / Destino: Registra el conector de MySQL ante el DriverManager para habilitar la comunicación con la base de datos.
            
            // Establecer la conexión utilizando la URL, usuario y contraseña provistos por variables de entorno
            conexion = DriverManager.getConnection(URL, USER, PASSWORD); // Qué hace: Solicita al DriverManager una conexión activa usando las credenciales y URL definidas. | Para qué sirve / Destino: Establece el enlace físico de red y sesión con el servidor de bases de datos MySQL.
            
            // Registrar éxito de conexión en la consola del servidor
            System.out.println("conectando a:"+URL); // Qué hace: Imprime un mensaje informativo en la consola con la URL de conexión. | Para qué sirve / Destino: Facilita la auditoría y depuración técnica del estado de la conexión en el servidor.
        } catch (ClassNotFoundException e) { // Qué hace: Captura la excepción ClassNotFoundException si el driver no está disponible. | Para qué sirve / Destino: Maneja errores críticos cuando falta la librería del conector JDBC en el proyecto.
            // Capturar error si el driver JDBC de MySQL no está presente en el classpath
            System.err.println("Error: No se encontró el Driver de MySQL -> " + e.getMessage()); // Qué hace: Imprime el error detallado de la ausencia del driver en la salida estándar de errores. | Para qué sirve / Destino: Notifica al desarrollador sobre problemas de configuración en las dependencias del proyecto.
        } catch (SQLException e) { // Qué hace: Captura la excepción SQLException generada por errores de base de datos. | Para qué sirve / Destino: Maneja fallos de red, credenciales incorrectas o problemas del servicio de MySQL.
            // Capturar error en la autenticación o configuración de red del servidor MySQL
            System.err.println("Error de SQL al conectar a la base de datos -> " + e.getMessage()); // Qué hace: Imprime el error específico de SQL en la salida estándar de errores. | Para qué sirve / Destino: Facilita la identificación de problemas de acceso o sintaxis de conexión hacia la base de datos.
        }
        // Retornar el objeto de conexión
        return conexion; // Qué hace: Retorna el objeto Connection (activo o nulo si falló). | Para qué sirve / Destino: Devuelve el canal de comunicación hacia el DAO o servicio que invocó el método para ejecutar operaciones en MySQL.
    }
}