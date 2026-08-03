// Definición del paquete para configuraciones de modelo
package Modelo.Config; // Qué hace: Declara el paquete Modelo.Config. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Configuración de la arquitectura del proyecto.

// Importar clases necesarias para lectura de archivos, mapas y flujos de entrada/salida
import java.io.BufferedReader; // Qué hace: Importa la clase BufferedReader de Java IO. | Para qué sirve / Destino: Permite leer texto de un flujo de entrada de manera eficiente mediante almacenamiento en búfer para procesar el archivo de configuración.
import java.io.File; // Qué hace: Importa la clase File de Java IO. | Para qué sirve / Destino: Facilita la representación y verificación de rutas y existencia de archivos en el sistema de archivos local.
import java.io.FileReader; // Qué hace: Importa la clase FileReader de Java IO. | Para qué sirve / Destino: Permite abrir flujos de lectura orientados a caracteres desde archivos de texto.
import java.io.IOException; // Qué hace: Importa la excepción IOException. | Para qué sirve / Destino: Captura y maneja los errores de E/S ocurridos durante la apertura o lectura de archivos.
import java.util.HashMap; // Qué hace: Importa la clase HashMap de Java Util. | Para qué sirve / Destino: Proporciona una estructura de almacenamiento basada en pares clave-valor para mantener las configuraciones en memoria.
import java.util.Map; // Qué hace: Importa la interfaz Map de Java Util. | Para qué sirve / Destino: Define el contrato abstracto para la gestión de colecciones de pares clave-valor.

/**
 * Clase utilitaria encargada de cargar y administrar variables de configuración.
 * Permite leer un archivo '.env' local en desarrollo y obtener variables de entorno
 * del sistema operativo en producción (ideal para Docker o Kubernetes).
 */
public class EnvConfig { // Qué hace: Declara la clase pública EnvConfig. | Para qué sirve / Destino: Actúa como el gestor global de propiedades de entorno, proveyendo credenciales dinámicas (como las de conexión a MySQL) para toda la aplicación.
    
    // Mapa estático en memoria para almacenar las variables leídas del archivo .env
    private static final Map<String, String> envMap = new HashMap<>(); // Qué hace: Declara e inicializa un mapa estático privado para almacenar las propiedades de configuración. | Para qué sirve / Destino: Mantiene en memoria las configuraciones cargadas para ser consultadas rápidamente por clases de infraestructura como Conexion.

    // Bloque estático de inicialización que se ejecuta automáticamente al cargar la clase
    static { // Qué hace: Define un bloque de inicialización estático que se ejecuta una sola vez al cargar la clase en memoria. | Para qué sirve / Destino: Automatiza la lectura del archivo de entorno al iniciar la aplicación antes de que cualquier componente o DAO intente acceder a la base de datos.
        // Intentar localizar el archivo .env en la raíz del proyecto
        File envFile = new File(".env"); // Qué hace: Crea una instancia de File apuntando al archivo ".env" en el directorio raíz actual. | Para qué sirve / Destino: Localiza el archivo de configuración local para entornos de desarrollo.
        
        // Si no se encuentra en la raíz del proyecto actual, intentar buscarlo en catalina.base (Tomcat)
        if (!envFile.exists()) { // Qué hace: Evalúa si el archivo .env no existe en la ruta raíz del proyecto. | Para qué sirve / Destino: Permite activar un mecanismo alternativo de búsqueda para servidores de aplicaciones.
            String catalinaBase = System.getProperty("catalina.base"); // Qué hace: Obtiene la propiedad del sistema que indica el directorio base del servidor Tomcat. | Para qué sirve / Destino: Identifica la ruta de ejecución en servidores web desplegados.
            if (catalinaBase != null) { // Qué hace: Verifica si la variable del servidor Tomcat no es nula. | Para qué sirve / Destino: Valida que la aplicación se esté ejecutando bajo un entorno de servidor de aplicaciones compatible.
                // Configurar la ruta del archivo relativa a la carpeta de ejecución de Tomcat
                envFile = new File(catalinaBase, ".env"); // Qué hace: Reasigna la referencia del archivo .env al directorio base de Tomcat. | Para qué sirve / Destino: Permite que el sistema lea las configuraciones de producción ubicadas en el servidor web.
            }
        }
        
        // Si el archivo .env existe, proceder a leer y parsear su contenido
        if (envFile.exists()) { // Qué hace: Comprueba si el archivo .env fue encontrado en alguna de las rutas evaluadas. | Para qué sirve / Destino: Asegura la existencia del archivo antes de intentar abrir flujos de lectura de datos.
            // Usar try-with-resources para asegurar el cierre automático del lector de archivos
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) { // Qué hace: Abre un bloque try-with-resources instanciando un BufferedReader para leer el archivo. | Para qué sirve / Destino: Garantiza la liberación de recursos del sistema al terminar la lectura del archivo de entorno.
                String linea; // Qué hace: Declara una variable auxiliar para almacenar cada línea leída. | Para qué sirve / Destino: Almacena temporalmente la cadena de texto de la línea analizada en el bucle.
                // Leer el archivo línea por línea hasta el final
                while ((linea = reader.readLine()) != null) { // Qué hace: Itera el archivo línea por línea hasta encontrar el fin del documento. | Para qué sirve / Destino: Recorre todo el contenido de configuración definido en el archivo .env.
                    // Limpiar espacios en blanco al inicio y al final de la línea
                    linea = linea.trim(); // Qué hace: Remueve los espacios en blanco sobrantes al principio y al final de la línea. | Para qué sirve / Destino: Normaliza el formato del texto leído para evitar errores de sintaxis en las claves o valores.
                    
                    // Omitir líneas vacías o aquellas que comiencen com '#' (comentarios)
                    if (linea.isEmpty() || linea.startsWith("#")) { // Qué hace: Evalúa si la línea está vacía o es un comentario iniciado con '#'. | Para qué sirve / Destino: Filtra las líneas que no contienen datos de configuración válidos.
                        continue; // Qué hace: Salta a la siguiente iteración del ciclo. | Para qué sirve / Destino: Ignora los comentarios y líneas en blanco del archivo .env.
                    }
                    
                    // Buscar la posición del primer caracter '=' que separa la clave del valor
                    int eqIdx = linea.indexOf('='); // Qué hace: Encuentra el índice del carácter separador '=' en la línea. | Para qué sirve / Destino: Localiza el punto de división entre la propiedad (clave) y su respectiva configuración (valor).
                    // Validar que el signo '=' no sea el primer caracter y exista en la línea
                    if (eqIdx > 0) { // Qué hace: Comprueba que el signo '=' exista y no esté en la primera posición. | Para qué sirve / Destino: Valida que la estructura de la línea cumpla con el formato clave=valor.
                        // Extraer la clave y remover espacios en blanco adicionales
                        String clave = linea.substring(0, eqIdx).trim(); // Qué hace: Extrae la subcadena correspondiente a la clave y elimina espacios. | Para qué sirve / Destino: Obtiene el identificador de la variable de configuración.
                        // Extraer el valor y remover espacios en blanco adicionales
                        String valor = linea.substring(eqIdx + 1).trim(); // Qué hace: Extrae la subcadena correspondiente al valor y elimina espacios. | Para qué sirve / Destino: Obtiene el contenido de la variable de configuración.
                        
                        // Si el valor está rodeado por comillas dobles, removerlas
                        if (valor.startsWith("\"") && valor.endsWith("\"") && valor.length() >= 2) { // Qué hace: Comprueba si el valor está delimitado por comillas dobles. | Para qué sirve / Destino: Detecta valores de texto encapsulados en comillas dobles.
                            valor = valor.substring(1, valor.length() - 1); // Qué hace: Extrae el texto interno removiendo las comillas dobles inicial y final. | Para qué sirve / Destino: Limpia el valor final de la variable eliminando marcas de formato de texto.
                        } 
                        // Si el valor está rodeado por comillas simples, removerlas
                        else if (valor.startsWith("'") && valor.endsWith("'") && valor.length() >= 2) { // Qué hace: Comprueba si el valor está delimitado por comillas simples. | Para qué sirve / Destino: Detecta valores de texto encapsulados en comillas simples.
                            valor = valor.substring(1, valor.length() - 1); // Qué hace: Extrae el texto interno removiendo las comillas simples inicial y final. | Para qué sirve / Destino: Limpia el valor final de la variable eliminando marcas de formato de texto.
                        }
                        
                        // Guardar el par clave-valor en nuestro mapa en memoria
                        envMap.put(clave, valor); // Qué hace: Inserta la clave y el valor procesados en el mapa estático `envMap`. | Para qué sirve / Destino: Registra la configuración en memoria para que pueda ser consultada por los componentes de conexión a MySQL.
                    }
                }
                // Imprimir mensaje informativo de carga exitosa
                System.out.println("Archivo .env cargado correctamente desde: " + envFile.getAbsolutePath()); // Qué hace: Imprime en consola un mensaje indicando el éxito y la ruta de carga del archivo .env. | Para qué sirve / Destino: Facilita la trazabilidad y depuración del sistema en la consola del servidor.
            } catch (IOException e) { // Qué hace: Captura la excepción IOException en caso de fallos de lectura. | Para qué sirve / Destino: Maneja errores imprevistos al intentar abrir o leer el archivo de entorno.
                // Registrar el error en consola si falla la lectura del archivo
                System.err.println("Advertencia: No se pudo leer el archivo .env -> " + e.getMessage()); // Qué hace: Imprime un mensaje de advertencia detallado en la salida estándar de errores. | Para qué sirve / Destino: Notifica problemas de acceso al archivo de configuración sin interrumpir necesariamente la ejecución general si existen respaldos.
            }
        } else { // Qué hace: Se ejecuta si el archivo .env no existe en ninguna ruta evaluada. | Para qué sirve / Destino: Maneja entornos de producción basados puramente en variables de entorno del sistema operativo.
            // Imprimir mensaje informando que no hay .env local, por lo que usará variables de entorno globales
            System.out.println("No se encontró archivo .env local. Se usarán las variables del sistema."); // Qué hace: Imprime un aviso informando que se recurrirá a las variables globales del sistema operativo. | Para qué sirve / Destino: Informa al administrador sobre el modo de configuración activo en el entorno de despliegue.
        }
    }

    /**
     * Obtiene el valor de una variable de configuración por su clave.
     * 
     * @param clave Clave de la variable a consultar.
     * @param valorDefecto Valor de retorno si la clave no se encuentra.
     * @return El valor correspondiente de la variable, o el valor por defecto si no está definida.
     */
    public static String get(String clave, String valorDefecto) { // Qué hace: Declara el método público estático get que recibe una clave y un valor por defecto, retornando un String. | Para qué sirve / Destino: Proporciona la interfaz estandarizada para que clases de configuración como Conexion obtengan los parámetros necesarios para acceder a MySQL.
        // Buscar primero en el mapa del archivo .env local
        String valor = envMap.get(clave); // Qué hace: Consulta el mapa en memoria utilizando la clave proporcionada. | Para qué sirve / Destino: Busca el valor de configuración cargado desde el archivo local .env.
        
        // Si no se encuentra en el mapa local, intentar obtener la variable de entorno del sistema
        if (valor == null) { // Qué hace: Evalúa si el valor obtenido es nulo (no existe en el archivo local). | Para qué sirve / Destino: Activa el mecanismo de respaldo consultando las variables del sistema operativo.
            valor = System.getenv(clave); // Qué hace: Solicita al entorno del sistema operativo el valor asociado a la clave. | Para qué sirve / Destino: Permite obtener configuraciones inyectadas por contenedores (como Docker o Kubernetes) en producción.
        }
        
        // Retornar el valor encontrado o el valor por defecto en caso de nulidad
        return valor != null ? valor : valorDefecto; // Qué hace: Retorna el valor encontrado o el valor por defecto si ambos métodos resultan en nulo mediante un operador ternario. | Para qué sirve / Destino: Asegura que el sistema siempre reciba un parámetro válido, garantizando la estabilidad al configurar la conexión a la base de datos u otros servicios.
    }
}