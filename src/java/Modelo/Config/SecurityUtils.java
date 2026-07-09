package Modelo.Config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase de utilidad encargada de proveer métodos de seguridad y criptografía.
 * Proporciona el hasheo de contraseñas mediante SHA-256 para evitar almacenamiento en texto plano.
 */
public class SecurityUtils {

    /**
     * Hashea una contraseña usando el algoritmo SHA-256.
     * 
     * @param password Contraseña en texto plano.
     * @return Representación en formato hexadecimal del hash SHA-256 de la contraseña.
     */
    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            // Obtener instancia del algoritmo SHA-256 de MessageDigest
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Computar el hash en base a los bytes de la contraseña en UTF-8
            byte[] encodedHash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            // Convertir el arreglo de bytes resultante a su representación hexadecimal
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Relanzar como excepción en tiempo de ejecución en caso de error interno
            throw new RuntimeException("Error fatal al inicializar el algoritmo de hasheo SHA-256", e);
        }
    }
}
