package co.edu.eam.cards4play.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Excepción propia del sistema para las reglas de negocio que no se cumplen.
 *
 * Guarda el código HTTP que se le responde al cliente de la API:
 *
 *   400 -> datos inválidos o incompletos
 *   401 -> no se envió usuario y contraseña, o son incorrectos
 *   403 -> el usuario no tiene permiso (por ejemplo, un cliente intentando una acción de admin)
 *   404 -> algo que no existe
 *   409 -> conflicto (sin cupo, booster ya vendido, ya abierto, etc.)
 *
 * Además puede llevar "detalles": información extra que se agrega a la
 * respuesta JSON, por ejemplo la lista de campos que faltan y un ejemplo.
 *
 * ¿Quién la atrapa? El manejador de errores de config/Servidor.java la convierte en:
 *   { "error": "mensaje", "codigo": 409, ...detalles }
 *
 * Extiende RuntimeException para no obligar a escribir "throws" en cada método.
 */
public class Cards4PlayException extends RuntimeException {

    private final int codigoHttp;
    private final Map<String, Object> detalles = new LinkedHashMap<>();

    public Cards4PlayException(int codigoHttp, String mensaje) {
        super(mensaje);
        this.codigoHttp = codigoHttp;
    }

    /** Agrega un dato extra a la respuesta de error. Devuelve la misma excepción para encadenar. */
    public Cards4PlayException conDetalle(String clave, Object valor) {
        detalles.put(clave, valor);
        return this;
    }

    public int getCodigoHttp() {
        return codigoHttp;
    }

    public Map<String, Object> getDetalles() {
        return detalles;
    }
}
