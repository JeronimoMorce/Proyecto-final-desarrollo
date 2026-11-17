package co.edu.eam.cards4play.util;

import java.util.UUID;

/**
 * Utilidad para crear identificadores únicos.
 *
 * Ejemplo de resultado: "CAR-3f9a1c2b" (prefijo + 8 caracteres aleatorios).
 * El prefijo ayuda a reconocer a simple vista de qué tipo de objeto es el id.
 */
public final class GeneradorId {

    /** Constructor privado: esta clase solo tiene métodos estáticos, no se crean objetos de ella. */
    private GeneradorId() {
    }

    public static String generar(String prefijo) {
        String aleatorio = UUID.randomUUID().toString().substring(0, 8);
        return prefijo + "-" + aleatorio;
    }
}
