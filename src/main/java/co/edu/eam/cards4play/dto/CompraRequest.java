package co.edu.eam.cards4play.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de POST /api/compras (un cliente compra un producto).
 *
 *   { "productoId": "BOO-1a2b3c4d" }
 *
 * El id se copia de la respuesta de GET /api/productos.
 */
public class CompraRequest extends Peticion {

    private String productoId;

    @Override
    protected void revisarCampos(List<String> faltantes) {
        if (vacio(productoId)) {
            faltantes.add("productoId");
        }
    }

    @Override
    public Map<String, Object> ejemplo() {
        Map<String, Object> ejemplo = new LinkedHashMap<>();
        ejemplo.put("productoId", "BOO-1a2b3c4d");
        return ejemplo;
    }

    public String getProductoId() {
        return productoId;
    }
}
