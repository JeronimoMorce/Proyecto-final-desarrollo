package co.edu.eam.cards4play.dto;

import co.edu.eam.cards4play.exception.Cards4PlayException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Clase base de todos los DTO de petición.
 *
 * DTO = "Data Transfer Object": una clase sencilla que solo tiene los campos
 * que llegan en el JSON del cuerpo de la petición. Gson convierte el JSON en
 * un objeto de esta clase. Ejemplo para crear una carta:
 *
 *   { "nombre": "Dragón", "rareza": "UR", "precioUsd": 25, "stock": 2 }   ->   CartaRequest
 *
 * ¿Por qué no convertir el JSON directo en una Carta? Porque el usuario NO debe
 * poder enviar el id, el propietario de un booster ni otros datos internos.
 *
 * Esta clase es la que "pide los datos": si falta alguno, validar() responde
 * 400 con la lista de campos que faltan y un ejemplo del JSON correcto.
 *
 * Los números se declaran como Double / Integer (con mayúscula) y no como
 * double / int: así, si el campo no llega, queda en null y sabemos que falta.
 */
public abstract class Peticion {

    /** Cada DTO agrega a la lista los nombres de los campos obligatorios que no llegaron. */
    protected abstract void revisarCampos(List<String> faltantes);

    /** Cada DTO devuelve un ejemplo del JSON que espera. Se muestra en los errores y en /api/ayuda. */
    public abstract Map<String, Object> ejemplo();

    /** Lanza 400 si falta algún campo obligatorio. */
    public void validar() {
        List<String> faltantes = new ArrayList<>();
        revisarCampos(faltantes);
        if (!faltantes.isEmpty()) {
            throw new Cards4PlayException(400, "Faltan datos obligatorios: " + String.join(", ", faltantes))
                    .conDetalle("camposFaltantes", faltantes)
                    .conDetalle("ejemplo", ejemplo());
        }
    }

    /** Ayuda para los DTO: un texto falta si es null o está vacío. */
    protected static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
