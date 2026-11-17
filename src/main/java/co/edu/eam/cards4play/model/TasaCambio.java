package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.model.enums.FuenteTasa;

/**
 * Representa una tasa de cambio USD -> COP ya consultada.
 *
 * Guarda dos datos: cuánto vale un dólar en pesos y de dónde salió ese valor
 * (API o DEFECTO). Así cualquier precio en COP puede decir su fuente.
 *
 * Relaciones UML:
 *  - ServicioTasaCambio crea objetos TasaCambio.
 *  - Producto usa una TasaCambio para calcular su precio en COP.
 */
public class TasaCambio {

    private final double valor;
    private final FuenteTasa fuente;

    public TasaCambio(double valor, FuenteTasa fuente) {
        this.valor = valor;
        this.fuente = fuente;
    }

    /** Convierte una cantidad en dólares a pesos colombianos. */
    public double convertir(double usd) {
        return usd * valor;
    }

    public double getValor() {
        return valor;
    }

    public FuenteTasa getFuente() {
        return fuente;
    }
}
