package co.edu.eam.cards4play.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para no guardar las contraseñas en texto plano.
 *
 * Se aplica SHA-256: una función que convierte "clave1" en un texto largo como
 * "2a97516c354b68848cdbd8f54a226a0a55b21ed138e207ad6c5cbb9c00aa5aea".
 * No se puede devolver ese texto a la contraseña original.
 *
 * Para revisar un login se cifra lo que escribió el usuario y se compara con lo guardado.
 * Así, si alguien abre clientes.json, no ve las contraseñas reales.
 */
public final class Seguridad {

    private Seguridad() {
    }

    public static String cifrar(String texto) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha.digest(texto.getBytes(StandardCharsets.UTF_8));
            // Cada byte se escribe como dos caracteres hexadecimales (0-9, a-f)
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException error) {
            // Todas las instalaciones de Java traen SHA-256; esto no debería pasar
            throw new IllegalStateException("SHA-256 no disponible", error);
        }
    }
}
