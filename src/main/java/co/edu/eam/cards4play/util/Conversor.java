package co.edu.eam.cards4play.util;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.enums.Rareza;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/**
 * Convierte textos que llegan en las peticiones a los tipos de Java,
 * con un mensaje claro (400) cuando el texto no sirve.
 */
public final class Conversor {

    private Conversor() {
    }

    /** "2026-12-11" -> LocalDate. Responde 400 si el formato no es AAAA-MM-DD. */
    public static LocalDate fecha(String texto, String nombreCampo) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException error) {
            throw new Cards4PlayException(400, "El campo " + nombreCampo
                    + " debe tener el formato AAAA-MM-DD, por ejemplo 2026-12-11");
        }
    }

    /** Igual que fecha(), pero si el texto es null devuelve null (para campos opcionales). */
    public static LocalDate fechaOpcional(String texto, String nombreCampo) {
        return (texto == null || texto.isBlank()) ? null : fecha(texto, nombreCampo);
    }

    /** "ur" o "UR" -> Rareza.UR. Responde 400 con las opciones válidas si no existe. */
    public static Rareza rareza(String texto) {
        try {
            return Rareza.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException error) {
            throw new Cards4PlayException(400, "Rareza inválida: " + texto)
                    .conDetalle("valoresPermitidos", Arrays.asList(Rareza.values()));
        }
    }
}
