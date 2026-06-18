// Definición del paquete para configuraciones de modelo
package Modelo.Config;

// Importar clases necesarias para lectura de archivos, mapas y flujos de entrada/salida
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase utilitaria encargada de cargar y administrar variables de configuración.
 * Permite leer un archivo '.env' local en desarrollo y obtener variables de entorno
 * del sistema operativo en producción (ideal para Docker o Kubernetes).
 */
public class EnvConfig {
    
    // Mapa estático en memoria para almacenar las variables leídas del archivo .env
    private static final Map<String, String> envMap = new HashMap<>();

    // Bloque estático de inicialización que se ejecuta automáticamente al cargar la clase
    static {
        // Intentar localizar el archivo .env en la raíz del proyecto
        File envFile = new File(".env");
        
        // Si no se encuentra en la raíz del proyecto actual, intentar buscarlo en catalina.base (Tomcat)
        if (!envFile.exists()) {
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                // Configurar la ruta del archivo relativa a la carpeta de ejecución de Tomcat
                envFile = new File(catalinaBase, ".env");
            }
        }
        
        // Si el archivo .env existe, proceder a leer y parsear su contenido
        if (envFile.exists()) {
            // Usar try-with-resources para asegurar el cierre automático del lector de archivos
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String linea;
                // Leer el archivo línea por línea hasta el final
                while ((linea = reader.readLine()) != null) {
                    // Limpiar espacios en blanco al inicio y al final de la línea
                    linea = linea.trim();
                    
                    // Omitir líneas vacías o aquellas que comiencen con '#' (comentarios)
                    if (linea.isEmpty() || linea.startsWith("#")) {
                        continue;
                    }
                    
                    // Buscar la posición del primer caracter '=' que separa la clave del valor
                    int eqIdx = linea.indexOf('=');
                    // Validar que el signo '=' no sea el primer caracter y exista en la línea
                    if (eqIdx > 0) {
                        // Extraer la clave y remover espacios en blanco adicionales
                        String clave = linea.substring(0, eqIdx).trim();
                        // Extraer el valor y remover espacios en blanco adicionales
                        String valor = linea.substring(eqIdx + 1).trim();
                        
                        // Si el valor está rodeado por comillas dobles, removerlas
                        if (valor.startsWith("\"") && valor.endsWith("\"") && valor.length() >= 2) {
                            valor = valor.substring(1, valor.length() - 1);
                        } 
                        // Si el valor está rodeado por comillas simples, removerlas
                        else if (valor.startsWith("'") && valor.endsWith("'") && valor.length() >= 2) {
                            valor = valor.substring(1, valor.length() - 1);
                        }
                        
                        // Guardar el par clave-valor en nuestro mapa en memoria
                        envMap.put(clave, valor);
                    }
                }
                // Imprimir mensaje informativo de carga exitosa
                System.out.println("Archivo .env cargado correctamente desde: " + envFile.getAbsolutePath());
            } catch (IOException e) {
                // Registrar el error en consola si falla la lectura del archivo
                System.err.println("Advertencia: No se pudo leer el archivo .env -> " + e.getMessage());
            }
        } else {
            // Imprimir mensaje informando que no hay .env local, por lo que usará variables de entorno globales
            System.out.println("No se encontró archivo .env local. Se usarán las variables del sistema.");
        }
    }

    /**
     * Obtiene el valor de una variable de configuración por su clave.
     * 
     * @param clave Clave de la variable a consultar.
     * @param valorDefecto Valor de retorno si la clave no se encuentra.
     * @return El valor correspondiente de la variable, o el valor por defecto si no está definida.
     */
    public static String get(String clave, String valorDefecto) {
        // Buscar primero en el mapa del archivo .env local
        String valor = envMap.get(clave);
        
        // Si no se encuentra en el mapa local, intentar obtener la variable de entorno del sistema
        if (valor == null) {
            valor = System.getenv(clave);
        }
        
        // Retornar el valor encontrado o el valor por defecto en caso de nulidad
        return valor != null ? valor : valorDefecto;
    }
}
