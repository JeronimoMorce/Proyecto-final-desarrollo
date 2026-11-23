package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.util.GeneradorId;

/**
 * Clase base (abstracta) de todo lo que vende la tienda.
 *
 * Es abstracta porque no existe un "producto" a secas: siempre es una Carta,
 * un Booster, un ProductoSellado o un Accesorio. Aquí va lo que todos comparten.
 *
 * Los atributos son "protected" para que las clases hijas puedan usarlos,
 * pero desde afuera solo se accede con los métodos (encapsulamiento).
 *
 * Relaciones UML:
 *  - Carta, Booster, ProductoSellado y Accesorio heredan de Producto.
 *  - Inventario contiene muchos Producto.
 *  - Producto usa TasaCambio para calcular el precio en pesos.
 */
public abstract class Producto {

    protected String id;
    protected String nombre;
    protected double precioUsd;
    protected int stock;

    /**
     * @param prefijoId texto corto para el id, por ejemplo "CAR" para cartas
     */
    protected Producto(String prefijoId, String nombre, double precioUsd, int stock) {
        this.id = GeneradorId.generar(prefijoId);
        cambiarNombre(nombre);
        cambiarPrecioUsd(precioUsd);
        cambiarStock(stock);
    }

    /**
     * Calcula el precio en pesos colombianos con la tasa recibida.
     * La conversión se hace al CONSULTAR el producto (corrección del profesor),
     * por eso este método está en Producto y no en Compra.
     */
    public double calcularPrecioCop(TasaCambio tasa) {
        return tasa.convertir(precioUsd);
    }

    /** Un producto se puede vender si todavía hay unidades. */
    public boolean estaDisponible() {
        return stock > 0;
    }

    /** Resta una unidad del stock cuando se vende. */
    public void descontarStock() {
        if (stock <= 0) {
            throw new Cards4PlayException(409, "No hay unidades disponibles de: " + nombre);
        }
        stock--;
    }

    // ---------- Métodos para modificar (los usa PUT /api/productos/:id) ----------
    // Cada uno valida antes de cambiar, así el objeto nunca queda con datos inválidos.

    public void cambiarNombre(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new Cards4PlayException(400, "El nombre del producto es obligatorio");
        }
        this.nombre = nuevoNombre.trim();
    }

    public void cambiarPrecioUsd(double nuevoPrecio) {
        if (nuevoPrecio <= 0) {
            throw new Cards4PlayException(400, "El precio en USD debe ser mayor que cero");
        }
        this.precioUsd = nuevoPrecio;
    }

    public void cambiarStock(int nuevoStock) {
        if (nuevoStock < 0) {
            throw new Cards4PlayException(400, "El stock no puede ser negativo");
        }
        this.stock = nuevoStock;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioUsd() {
        return precioUsd;
    }

    public int getStock() {
        return stock;
    }

    /** Nombre del tipo en mayúsculas: "CARTA", "BOOSTER", "PRODUCTOSELLADO", "ACCESORIO". */
    public String getTipo() {
        return getClass().getSimpleName().toUpperCase();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + id + "] " + nombre
                + " | USD " + precioUsd + " | stock: " + stock;
    }
}
