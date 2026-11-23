package co.edu.eam.cards4play.dto;

import co.edu.eam.cards4play.service.GeneradorBooster;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de POST /api/boosters (el administrador crea un booster).
 *
 * El administrador NO elige las cartas (las sortea GeneradorBooster), así que
 * el cuerpo puede ir vacío. Opcionalmente se envía el precio:
 *
 *   { }                    -> usa el precio por defecto (6.0 USD)
 *   { "precioUsd": 7.5 }   -> usa ese precio
 */
public class BoosterRequest extends Peticion {

    private Double precioUsd;

    @Override
    protected void revisarCampos(List<String> faltantes) {
        // No hay campos obligatorios
    }

    @Override
    public Map<String, Object> ejemplo() {
        Map<String, Object> ejemplo = new LinkedHashMap<>();
        ejemplo.put("precioUsd", GeneradorBooster.PRECIO_BOOSTER_USD);
        return ejemplo;
    }

    /** Devuelve el precio enviado o, si no llegó, el precio por defecto. */
    public double getPrecioUsdOPorDefecto() {
        return precioUsd == null ? GeneradorBooster.PRECIO_BOOSTER_USD : precioUsd;
    }
}
