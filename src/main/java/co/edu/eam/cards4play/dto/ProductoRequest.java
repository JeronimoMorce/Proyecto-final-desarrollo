package co.edu.eam.cards4play.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de POST /api/sellados y POST /api/accesorios.
 * Los dos tipos piden lo mismo, por eso comparten este DTO.
 *
 *   { "nombre": "Caja de inicio", "precioUsd": 30.0, "stock": 3 }
 */
public class ProductoRequest extends Peticion {

    private String nombre;
    private Double precioUsd;
    private Integer stock;

    @Override
    protected void revisarCampos(List<String> faltantes) {
        if (vacio(nombre)) {
            faltantes.add("nombre");
        }
        if (precioUsd == null) {
            faltantes.add("precioUsd");
        }
        if (stock == null) {
            faltantes.add("stock");
        }
    }

    @Override
    public Map<String, Object> ejemplo() {
        Map<String, Object> ejemplo = new LinkedHashMap<>();
        ejemplo.put("nombre", "Caja de inicio");
        ejemplo.put("precioUsd", 30.0);
        ejemplo.put("stock", 3);
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
