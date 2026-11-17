package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.model.enums.FuenteTasa;
import co.edu.eam.cards4play.util.GeneradorId;

import java.time.LocalDateTime;

/**
 * Registro de una compra hecha por un cliente.
 *
 * Guarda una "foto" del precio en el momento de comprar: si mañana cambia la
 * tasa de cambio, la compra de hoy conserva lo que realmente se pagó.
 *
 * Importante: Compra NO consulta la API. El precio en pesos lo calcula el
 * Producto y aquí solo se guarda el resultado.
 *
 * Relaciones UML:
 *  - Cliente tiene muchas Compra (su historial).
 *  - Cada Compra apunta a un Producto.
 */
public class Compra {

    private String id;
    private LocalDateTime fecha;
    private Producto producto;
    private double precioUsd;
    private double precioCop;
    private FuenteTasa fuenteTasa;

    public Compra(Producto producto, TasaCambio tasa) {
        this.id = GeneradorId.generar("COM");
        this.fecha = LocalDateTime.now();
        this.producto = producto;
        this.precioUsd = producto.getPrecioUsd();
        this.precioCop = producto.calcularPrecioCop(tasa); // el cálculo lo hace Producto
        this.fuenteTasa = tasa.getFuente();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Producto getProducto() {
        return producto;
    }

    public double getPrecioUsd() {
        return precioUsd;
    }

    public double getPrecioCop() {
        return precioCop;
    }

    public FuenteTasa getFuenteTasa() {
        return fuenteTasa;
    }

    @Override
    public String toString() {
        return "Compra [" + id + "] " + producto.getNombre()
                + " | USD " + precioUsd
                + " | COP " + String.format("%,.0f", precioCop)
                + " | fuenteTasa: " + fuenteTasa;
    }
}
