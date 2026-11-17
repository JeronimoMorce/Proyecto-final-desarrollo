package co.edu.eam.cards4play.model;

/**
 * Producto sellado (cajas, mazos armados, etc.).
 *
 * Según el enunciado solo necesita nombre y precio, y eso ya lo tiene Producto.
 * Por eso la clase no agrega atributos: existe para distinguir el TIPO de producto.
 *
 * Relación UML: hereda de Producto.
 */
public class ProductoSellado extends Producto {

    public ProductoSellado(String nombre, double precioUsd, int stock) {
        super("SEL", nombre, precioUsd, stock);
    }
}
