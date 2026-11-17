package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.exception.Cards4PlayException;

import java.util.ArrayList;
import java.util.List;

/**
 * Sobre con 10 cartas aleatorias.
 *
 * Reglas del enunciado que se cumplen aquí:
 *  - Cada booster es una unidad única: su stock siempre empieza en 1.
 *  - Su contenido queda fijo desde que se crea.
 *  - Solo puede venderse una vez.
 *  - Solo su dueño puede abrirlo, y una única vez.
 *  - Mientras esté cerrado no se muestran sus cartas.
 *
 * Relaciones UML:
 *  - Hereda de Producto.
 *  - Contiene 10 Carta (agregación: las cartas siguen existiendo en el inventario).
 *  - Tiene un propietario (Cliente) cuando se vende; se guarda su id.
 *  - GeneradorBooster es quien lo crea.
 */
public class Booster extends Producto {

    private List<Carta> cartas;
    private boolean abierto;
    private String propietarioId; // null mientras nadie lo haya comprado

    public Booster(String nombre, double precioUsd, List<Carta> cartas) {
        super("BOO", nombre, precioUsd, 1); // stock 1: cada booster es único
        this.cartas = new ArrayList<>(cartas); // copia, para que nadie cambie el contenido desde afuera
        this.abierto = false;
        this.propietarioId = null;
    }

    /** Un booster está vendido cuando ya tiene dueño. */
    public boolean estaVendido() {
        return propietarioId != null;
    }

    /** Asigna el booster al cliente que lo compra. */
    public void vender(Cliente cliente) {
        if (estaVendido()) {
            throw new Cards4PlayException(409, "El booster " + id + " ya fue vendido");
        }
        this.propietarioId = cliente.getId();
        descontarStock(); // el stock pasa de 1 a 0 y deja de estar disponible
    }

    /** Abre el booster y devuelve sus cartas. Solo el dueño puede hacerlo, una vez. */
    public List<Carta> abrir(Cliente cliente) {
        if (!estaVendido() || !propietarioId.equals(cliente.getId())) {
            throw new Cards4PlayException(403, "Solo el dueño del booster puede abrirlo");
        }
        if (abierto) {
            throw new Cards4PlayException(409, "El booster " + id + " ya fue abierto");
        }
        this.abierto = true;
        return getCartasVisibles();
    }

    /**
     * Devuelve las cartas solo si el booster ya fue abierto.
     * Si está cerrado devuelve una lista vacía: así el contenido permanece oculto.
     */
    public List<Carta> getCartasVisibles() {
        if (abierto) {
            return new ArrayList<>(cartas);
        }
        return new ArrayList<>();
    }

    public boolean isAbierto() {
        return abierto;
    }

    public String getPropietarioId() {
        return propietarioId;
    }

    @Override
    public String toString() {
        String estado = abierto ? "abierto" : "cerrado";
        return super.toString() + " | " + estado;
    }
}
