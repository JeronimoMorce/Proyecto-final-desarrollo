package co.edu.eam.cards4play.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de POST /api/cartas (el administrador registra una carta).
 *
 *   { "nombre": "Dragón Ancestral", "rareza": "UR", "precioUsd": 25.0, "stock": 2 }
 *
 * rareza llega como texto ("N", "UC", "R", "SR", "UR"); el servicio la convierte al enum
 * y responde 400 si no es una de esas cinco.
 */
public class CartaRequest extends Peticion {

    private String nombre;
    private String rareza;
    private Double precioUsd;
    private Integer stock;

    @Override
    protected void revisarCampos(List<String> faltantes) {
        if (vacio(nombre)) {
            faltantes.add("nombre");
        }
        if (vacio(rareza)) {
            faltantes.add("rareza");
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
        ejemplo.put("nombre", "Dragón Ancestral");
        ejemplo.put("rareza", "UR");
        ejemplo.put("precioUsd", 25.0);
        ejemplo.put("stock", 2);
        return ejemplo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRareza() {
        return rareza;
    }

    public Double getPrecioUsd() {
        return precioUsd;
    }

    public Integer getStock() {
        return stock;
    }
}
