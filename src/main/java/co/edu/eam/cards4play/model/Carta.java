package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.enums.Rareza;

/**
 * Carta individual ("single"): la unidad básica del inventario.
 *
 * Relaciones UML:
 *  - Hereda de Producto (id, nombre, precioUsd, stock).
 *  - Tiene una Rareza.
 *  - Un Booster contiene 10 cartas.
 */
public class Carta extends Producto {

    private Rareza rareza;

    public Carta(String nombre, double precioUsd, int stock, Rareza rareza) {
        super("CAR", nombre, precioUsd, stock); // llama al constructor de Producto
        if (rareza == null) {
            throw new Cards4PlayException(400, "La rareza de la carta es obligatoria");
        }
        this.rareza = rareza;
    }

    public Rareza getRareza() {
        return rareza;
    }

    @Override
    public String toString() {
        return super.toString() + " | rareza: " + rareza;
    }
}
