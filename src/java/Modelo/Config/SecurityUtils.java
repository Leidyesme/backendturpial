package Modelo.Config; // Qué hace: Declara el paquete Modelo.Config. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Configuración o Utilidades de seguridad de la arquitectura del proyecto.

import java.security.MessageDigest; // Qué hace: Importa la clase MessageDigest de la librería java.security. | Para qué sirve / Destino: Proporciona las funciones criptográficas necesarias para calcular resúmenes de mensajes o hashes (como SHA-256).
import java.security.NoSuchAlgorithmException; // Qué hace: Importa la excepción NoSuchAlgorithmException de java.security. | Para qué sirve / Destino: Permite capturar el error en caso de que el algoritmo criptográfico solicitado no esté disponible en la máquina virtual de Java.

/**
 * Clase de utilidad encargada de proveer métodos de seguridad y criptografía.
 * Proporciona el hasheo de contraseñas mediante SHA-256 para evitar almacenamiento en texto plano.
 */
public class SecurityUtils { // Qué hace: Declara la clase pública SecurityUtils. | Para qué sirve / Destino: Actúa como un componente utilitario transversal que cifra las credenciales antes de ser enviadas desde los controladores o DAOs hacia la base de datos MySQL.

    /**
     * Hashea una contraseña usando el algoritmo SHA-256.
     * 
     * @param password Contraseña en texto plano.
     * @return Representación en formato hexadecimal del hash SHA-256 de la contraseña.
     */
    public static String hashPassword(String password) { // Qué hace: Declara el método público estático hashPassword que recibe un String y retorna un String. | Para qué sirve / Destino: Expone la lógica de cifrado para que cualquier DAO o controlador pueda transformar claves planas en hashes seguros antes de persistirlas en MySQL.
        if (password == null) { // Qué hace: Evalúa si el parámetro password recibido es nulo. | Para qué sirve / Destino: Previene excepciones por referencias nulas (NullPointerException) al intentar procesar credenciales vacías.
            return null; // Qué hace: Retorna null de inmediato si la contraseña es nula. | Para qué sirve / Destino: Evita la ejecución innecesaria del algoritmo criptográfico ante datos inválidos.
        }
        try { // Qué hace: Inicia un bloque de control de excepciones try-catch. | Para qué sirve / Destino: Captura posibles errores relacionados con la disponibilidad del algoritmo criptográfico SHA-256 en Java.
            // Obtener instancia del algoritmo SHA-256 de MessageDigest
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); // Qué hace: Solicita a la factoría de MessageDigest una instancia configurada con el algoritmo SHA-256. | Para qué sirve / Destino: Inicializa el motor criptográfico que transformará el texto plano en un resumen hash seguro.
            
            // Computar el hash en base a los bytes de la contraseña en UTF-8
            byte[] encodedHash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8)); // Qué hace: Convierte la contraseña a bytes usando codificación UTF-8 y calcula su resumen hash en un arreglo de bytes. | Para qué sirve / Destino: Genera la huella digital criptográfica única de la contraseña del usuario.
            
            // Convertir el arreglo de bytes resultante a su representación hexadecimal
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length); // Qué hace: Inicializa un StringBuilder con una capacidad calculada al doble de la longitud del arreglo de bytes. | Para qué sirve / Destino: Optimiza la construcción de la cadena de texto hexadecimal resultante sin sobrecargar la memoria.
            for (byte b : encodedHash) { // Qué hace: Itera sobre cada byte del arreglo de bytes hasheados. | Para qué sirve / Destino: Recorre la huella digital criptográfica para formatearla caracter por caracter.
                String hex = Integer.toHexString(0xff & b); // Qué hace: Convierte el byte actual a su representación hexadecimal en formato de cadena. | Para qué sirve / Destino: Traduce cada valor binario del hash a caracteres legibles hexadecimales.
                if (hex.length() == 1) { // Qué hace: Comprueba si la cadena hexadecimal resultante tiene una longitud de 1. | Para qué sirve / Destino: Detecta si falta el cero inicial para mantener el formato de doble dígito por byte.
                    hexString.append('0'); // Qué hace: Añade un '0' al StringBuilder si la longitud del hexadecimal es menor a 2. | Para qué sirve / Destino: Asegura la consistencia y el ancho fijo estándar de la representación hexadecimal del hash.
                }
                hexString.append(hex); // Qué hace: Añade la cadena hexadecimal procesada al objeto StringBuilder. | Para qué sirve / Destino: Concatena los bloques de texto para conformar el hash completo final.
            }
            return hexString.toString(); // Qué hace: Convierte el StringBuilder a un String plano y lo retorna. | Para qué sirve / Destino: Devuelve el hash SHA-256 final listo para ser comparado o guardado por los DAOs en la base de datos MySQL.
        } catch (NoSuchAlgorithmException e) { // Qué hace: Captura la excepción NoSuchAlgorithmException si el algoritmo no se encuentra. | Para qué sirve / Destino: Maneja errores fatales del entorno de ejecución de Java si faltan proveedores de seguridad.
            // Relanzar como excepción en tiempo de ejecución en caso de error interno
            throw new RuntimeException("Error fatal al inicializar el algoritmo de hasheo SHA-256", e); // Qué hace: Envuelve y relanzar la excepción como un RuntimeException. | Para qué sirve / Destino: Interrumpe de manera controlada la ejecución de la aplicación si el sistema de cifrado no está disponible.
        }
    }
}