package co.edu.eam.cards4play.service;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Booster;
import co.edu.eam.cards4play.model.Carta;
import co.edu.eam.cards4play.model.Inventario;
import co.edu.eam.cards4play.model.enums.Rareza;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Servicio que arma los boosters: elige las 10 cartas según las probabilidades de rareza.
 *
 * Pasos para cada carta (los pide el enunciado):
 *  1. Se sortea una rareza con la tabla de probabilidades (Rareza.sortear()).
 *  2. Regla de respaldo: si no hay cartas de esa rareza, se usa la rareza inferior
 *     más cercana que sí tenga cartas; si no hay ninguna inferior, la superior más cercana.
 *  3. Se elige al azar una carta de la rareza resultante (se permiten repetidas).
 *
 * Bonificación: se garantiza al menos una carta R o superior en cada booster.
 *
 * Relaciones UML:
 *  - GeneradorBooster crea Booster.
 *  - Lee las cartas del Inventario y sortea con Rareza.
 *  - Administrador lo usa en crearBooster().
 */
public class GeneradorBooster {

    private static final int CARTAS_POR_BOOSTER = 10;

    /** El enunciado no define el precio del booster; se deja en un solo lugar para cambiarlo fácil. */
    private static final double PRECIO_BOOSTER_USD = 6.0;

    private final Random random = new Random();

    /** Crea un booster nuevo con 10 cartas tomadas del inventario. */
    public Booster generar(Inventario inventario) {
        if (!hayCartasRegistradas(inventario)) {
            throw new Cards4PlayException(409, "No se puede crear el booster: no hay cartas registradas");
        }

        List<Carta> cartas = new ArrayList<>();
        for (int i = 0; i < CARTAS_POR_BOOSTER; i++) {
            cartas.add(elegirCarta(inventario));
        }

        garantizarRaraOSuperior(cartas, inventario);

        return new Booster("Booster Cards4Play", PRECIO_BOOSTER_USD, cartas);
    }

    /** Elige UNA carta: sortea rareza, aplica respaldo y toma una carta al azar de esa rareza. */
    private Carta elegirCarta(Inventario inventario) {
        Rareza sorteada = Rareza.sortear();
        Rareza disponible = aplicarRespaldo(sorteada, inventario);
        List<Carta> candidatas = inventario.cartasPorRareza(disponible);
        int posicion = random.nextInt(candidatas.size()); // posición al azar dentro de la lista
        return candidatas.get(posicion);
    }

    /**
     * Regla de respaldo. Las rarezas están ordenadas así: N, UC, R, SR, UR.
     * Ejemplo: sale SR pero no hay cartas SR -> se prueba R, luego UC, luego N;
     * si tampoco hay, se prueba hacia arriba: UR.
     */
    private Rareza aplicarRespaldo(Rareza rareza, Inventario inventario) {
        if (!inventario.cartasPorRareza(rareza).isEmpty()) {
            return rareza; // sí hay cartas de la rareza sorteada: no se cambia nada
        }

        Rareza[] todas = Rareza.values();

        // 1. Buscar hacia abajo (rarezas inferiores), de la más cercana a la más lejana
        for (int i = rareza.ordinal() - 1; i >= 0; i--) {
            if (!inventario.cartasPorRareza(todas[i]).isEmpty()) {
                return todas[i];
            }
        }

        // 2. Buscar hacia arriba (rarezas superiores), de la más cercana a la más lejana
        for (int i = rareza.ordinal() + 1; i < todas.length; i++) {
            if (!inventario.cartasPorRareza(todas[i]).isEmpty()) {
                return todas[i];
            }
        }

        // No se llega aquí porque generar() ya revisó que exista al menos una carta
        throw new Cards4PlayException(409, "No hay cartas registradas");
    }

    /**
     * Bonificación (+0.2): si ninguna de las 10 cartas es R o superior, se cambia la
     * última por una carta R, SR o UR. Las otras 9 quedan como salieron en el sorteo.
     * Si la tienda no tiene ninguna carta R o superior, no se puede garantizar y no se hace nada.
     */
    private void garantizarRaraOSuperior(List<Carta> cartas, Inventario inventario) {
        for (Carta carta : cartas) {
            if (carta.getRareza().esRaraOSuperior()) {
                return; // ya hay una: no hay que hacer nada
            }
        }

        // Reunir todas las cartas R, SR y UR que existan en el inventario
        List<Carta> rarasDisponibles = new ArrayList<>();
        for (Rareza rareza : Rareza.values()) {
            if (rareza.esRaraOSuperior()) {
                rarasDisponibles.addAll(inventario.cartasPorRareza(rareza));
            }
        }

        if (rarasDisponibles.isEmpty()) {
            return;
        }

        Carta cartaRara = rarasDisponibles.get(random.nextInt(rarasDisponibles.size()));
        cartas.set(cartas.size() - 1, cartaRara); // reemplaza la última carta
    }

    /** Revisa si existe al menos una carta de cualquier rareza. */
    private boolean hayCartasRegistradas(Inventario inventario) {
        for (Rareza rareza : Rareza.values()) {
            if (!inventario.cartasPorRareza(rareza).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
