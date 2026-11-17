package co.edu.eam.cards4play.model;

/**
 * Accesorio de juego (protectores, tapetes, dados, etc.).
 *
 * Igual que ProductoSellado, solo usa los datos básicos que hereda de Producto.
 *
 * Relación UML: hereda de Producto.
 */
public class Accesorio extends Producto {

    public Accesorio(String nombre, double precioUsd, int stock) {
        super("ACC", nombre, precioUsd, stock);
    }
}
