package Modelo.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// clase encargada de la coneccion con la base de datos
public class Conexion {
    // Parámetros de conexión de la base de datos 'turpial'
    private static final String URL = "jdbc:mysql://localhost:3306/turpial?serverTimezone=UTC&useSSL=false";
    private static final String USER = "root"; // O el usuario que definiste en tu script ('admin' o 'clie')
    private static final String PASSWORD = "#Aprendiz2024"; 
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // metodo que se encarga de la crear y devolver la conexion
    public static Connection getConnection() {
        Connection conexion = null;
        try {
            // Registrar el Driver de MySQL
            Class.forName(DRIVER);
            // Obtener la conexión
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a la base de datos 'turpial'.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el Driver de MySQL -> " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de SQL al conectar a la base de datos -> " + e.getMessage());
        }
        return conexion;
    }
}