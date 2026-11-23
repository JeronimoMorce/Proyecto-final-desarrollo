package co.edu.eam.cards4play.dto;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de POST /api/torneos (el administrador crea un torneo).
 *
 *   { "nombre": "Copa Cards4Play", "fecha": "2026-12-11", "maxParticipantes": 16,
 *     "fechaLimiteInscripcion": "2026-12-09" }
 *
 * Las fechas llegan como texto con formato AAAA-MM-DD.
 * fechaLimiteInscripcion es opcional: si no llega, se cierra el día anterior al torneo.
 */
public class TorneoRequest extends Peticion {

    private String nombre;
    private String fecha;
    private Integer maxParticipantes;
    private String fechaLimiteInscripcion;

    @Override
    protected void revisarCampos(List<String> faltantes) {
        if (vacio(nombre)) {
            faltantes.add("nombre");
        }
        if (vacio(fecha)) {
            faltantes.add("fecha");
        }
        if (maxParticipantes == null) {
            faltantes.add("maxParticipantes");
        }
    }

    @Override
    public Map<String, Object> ejemplo() {
        LocalDate enDosSemanas = LocalDate.now().plusWeeks(2);
        Map<String, Object> ejemplo = new LinkedHashMap<>();
        ejemplo.put("nombre", "Copa Cards4Play");
        ejemplo.put("fecha", enDosSemanas.toString());
        ejemplo.put("maxParticipantes", 16);
        ejemplo.put("fechaLimiteInscripcion", enDosSemanas.minusDays(2).toString());
        return ejemplo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public Integer getMaxParticipantes() {
        return maxParticipantes;
    }

    public String getFechaLimiteInscripcion() {
        return fechaLimiteInscripcion;
    }
}
