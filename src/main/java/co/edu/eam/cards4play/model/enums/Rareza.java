package co.edu.eam.cards4play.model.enums;

import java.util.Random;

/**
 * Rarezas de las cartas con su probabilidad de aparición en un booster.
 *
 * El enunciado pide que las probabilidades estén en UN solo lugar del código
 * y que sumen 100 %. Ese lugar es este enum: 50 + 25 + 15 + 7 + 3 = 100.
 *
 * El orden en que están escritas importa: va de la menos rara (N) a la más
 * rara (UR). GeneradorBooster usa ese orden para la regla de respaldo.
 *
 * Relación UML: Carta tiene una Rareza; GeneradorBooster sortea una Rareza.
 */
public enum Rareza {
    N("Normal", 50),
    UC("Poco común", 25),
    R("Raro", 15),
    SR("Súper raro", 7),
    UR("Ultra raro", 3);

    /** Generador de números aleatorios compartido por todos los sorteos. */
    private static final Random RANDOM = new Random();

    private final String nombre;
    private final int probabilidad;

    Rareza(String nombre, int probabilidad) {
        this.nombre = nombre;
        this.probabilidad = probabilidad;
    }

    /**
     * Sortea una rareza respetando la tabla de probabilidades.
     *
     * Idea: se saca un número del 1 al 100 y se van sumando las probabilidades.
     *   1 a 50   -> N
     *   51 a 75  -> UC
     *   76 a 90  -> R
     *   91 a 97  -> SR
     *   98 a 100 -> UR
     */
    public static Rareza sortear() {
        int numero = RANDOM.nextInt(100) + 1; // número entre 1 y 100
        int acumulado = 0;
        for (Rareza rareza : values()) {
            acumulado += rareza.probabilidad;
            if (numero <= acumulado) {
                return rareza;
            }
        }
        return N; // nunca debería llegar aquí porque las probabilidades suman 100
    }

    /** Devuelve true si la rareza es R, SR o UR (se usa para la bonificación). */
    public boolean esRaraOSuperior() {
        // ordinal() es la posición en el enum: N=0, UC=1, R=2, SR=3, UR=4
        return this.ordinal() >= R.ordinal();
    }

    public String getNombre() {
        return nombre;
    }

    public int getProbabilidad() {
        return probabilidad;
    }
}
