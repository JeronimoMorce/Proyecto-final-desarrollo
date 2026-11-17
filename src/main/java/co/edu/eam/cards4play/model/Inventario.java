package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.enums.Rareza;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventario de la tienda: guarda todos los productos registrados.
 *
 * Ofrece dos "vistas" de la misma lista (corrección del profesor):
 *  - consultarDisponibles(): lo que ve el CLIENTE (solo lo que se puede comprar).
 *  - consultarStock():       lo que ve el ADMINISTRADOR (todo, incluso agotados y vendidos).
 *
 * Gracias al polimorfismo la lista es de Producto, pero dentro hay cartas,
 * boosters, sellados y accesorios mezclados.
 *
 * Relaciones UML:
 *  - Inventario contiene muchos Producto (composición).
 *  - Administrador gestiona el Inventario; Cliente lo consulta.
 *  - GeneradorBooster le pide las cartas por rareza.
 */
public class Inventario {

    private final List<Producto> productos = new ArrayList<>();

    /** Agrega un producto nuevo al inventario. */
    public void registrar(Producto producto) {
        if (producto == null) {
            throw new Cards4PlayException(400, "No se puede registrar un producto vacío");
        }
        productos.add(producto);
    }

    /** Busca un producto por su id. Si no existe lanza un error 404. */
    public Producto buscarPorId(String id) {
        for (Producto producto : productos) {
            if (producto.getId().equals(id)) {
                return producto;
            }
        }
        throw new Cards4PlayException(404, "No existe un producto con id " + id);
    }

    /** Vista del cliente: productos que todavía se pueden comprar. */
    public List<Producto> consultarDisponibles() {
        List<Producto> disponibles = new ArrayList<>();
        for (Producto producto : productos) {
            if (producto.estaDisponible()) {
                disponibles.add(producto);
            }
        }
        return disponibles;
    }

    /** Vista del administrador: todos los productos con su stock. */
    public List<Producto> consultarStock() {
        return new ArrayList<>(productos); // se devuelve una copia para proteger la lista original
    }

    /** Devuelve las cartas registradas que tienen la rareza indicada. */
    public List<Carta> cartasPorRareza(Rareza rareza) {
        List<Carta> cartas = new ArrayList<>();
        for (Producto producto : productos) {
            // instanceof pregunta: ¿este producto es realmente una Carta?
            if (producto instanceof Carta) {
                Carta carta = (Carta) producto;
                if (carta.getRareza() == rareza) {
                    cartas.add(carta);
                }
            }
        }
        return cartas;
    }
}
