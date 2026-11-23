package co.edu.eam.cards4play.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de PUT /api/productos/:id (el administrador modifica un producto).
 *
 * Todos los campos son opcionales: se cambia solo lo que se envíe.
 * Pero debe llegar al menos uno.
 *
 *   { "precioUsd": 27.5 }                       -> cambia solo el precio
 *   { "nombre": "Dragón Dorado", "stock": 4 }   -> cambia nombre y stock
 */
public class ActualizarProductoRequest extends Peticion {

    private String nombre;
    private Double precioUsd;
    private Integer stock;

    @Override
    protected void revisarCampos(List<String> faltantes) {
        if (nombre == null && precioUsd == null && stock == null) {
            faltantes.add("al menos uno de: nombre, precioUsd, stock");
        }
    }

    @Override
    public Map<String, Object> ejemplo() {
        Map<String, Object> ejemplo = new LinkedHashMap<>();
        ejemplo.put("precioUsd", 27.5);
        ejemplo.put("stock", 4);
        return ejemplo;
    }

    public String getNombre() {
        return nombre;
    }

    public Double getPrecioUsd() {
        return precioUsd;
    }

    public Integer getStock() {
        return stock;
    }
}
