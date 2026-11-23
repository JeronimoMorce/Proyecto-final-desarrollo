package co.edu.eam.cards4play.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de PUT /api/torneos/:id (el administrador modifica un torneo).
 *
 * Todos los campos son opcionales, pero debe llegar al menos uno.
 *
 *   { "maxParticipantes": 32 }
 *   { "fecha": "2026-12-18", "fechaLimiteInscripcion": "2026-12-16" }
 */
public class ActualizarTorneoRequest extends Peticion {

    private String nombre;
    private String fecha;
    private String fechaLimiteInscripcion;
    private Integer maxParticipantes;

    @Override
    protected void revisarCampos(List<String> faltantes) {
        if (nombre == null && fecha == null && fechaLimiteInscripcion == null && maxParticipantes == null) {
            faltantes.add("al menos uno de: nombre, fecha, fechaLimiteInscripcion, maxParticipantes");
        }
    }

    @Override
    public Map<String, Object> ejemplo() {
        Map<String, Object> ejemplo = new LinkedHashMap<>();
        ejemplo.put("maxParticipantes", 32);
        return ejemplo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public String getFechaLimiteInscripcion() {
        return fechaLimiteInscripcion;
    }

    public Integer getMaxParticipantes() {
        return maxParticipantes;
    }
}
