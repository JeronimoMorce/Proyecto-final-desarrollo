package co.edu.eam.cards4play.exception;

/**
 * Excepción propia del sistema para las reglas de negocio que no se cumplen.
 *
 * Además del mensaje guarda el código HTTP que le corresponde según la tabla
 * del enunciado. En esta entrega solo lo mostramos por consola; en la entrega
 * de la API REST ese código será el que se responda al cliente.
 *
 *   400 -> datos inválidos o incompletos
 *   403 -> acción no permitida (por ejemplo, abrir un booster ajeno)
 *   404 -> algo que no existe
 *   409 -> conflicto (sin cupo, booster ya vendido, ya abierto, etc.)
 *
 * Extiende RuntimeException para no obligar a escribir "throws" en cada método.
 */
public class Cards4PlayException extends RuntimeException {

    private final int codigoHttp;

    public Cards4PlayException(int codigoHttp, String mensaje) {
        super(mensaje);
        this.codigoHttp = codigoHttp;
    }

    public int getCodigoHttp() {
        return codigoHttp;
    }
}
