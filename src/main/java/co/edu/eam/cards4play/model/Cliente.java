package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.enums.Rol;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente de la tienda: consulta productos, compra, abre sus boosters y se inscribe en torneos.
 *
 * Fíjense que el Cliente casi no tiene lógica propia: le PIDE el trabajo a las
 * otras clases (Inventario, Booster, Torneo). Eso es lo que muestran las
 * flechas del diagrama UML.
 *
 * Relaciones UML:
 *  - Hereda de Usuario.
 *  - Tiene muchas Compra (historial).
 *  - Consulta el Inventario.
 *  - Es propietario de Booster y participante de Torneo.
 */
public class Cliente extends Usuario {

    private List<Compra> historial;

    public Cliente(String username, String password) {
        super(username, password, Rol.CLIENTE); // todo Cliente nace con rol CLIENTE
        this.historial = new ArrayList<>();
    }

    /** Lo que el cliente puede ver para comprar: solo los productos disponibles. */
    public List<Producto> consultarProductos(Inventario inventario) {
        return inventario.consultarDisponibles();
    }

    /**
     * Compra un producto y deja el registro en el historial.
     *
     * @param tasa tasa de cambio vigente; se usa para guardar cuánto se pagó en pesos
     */
    public Compra comprar(Producto producto, TasaCambio tasa) {
        if (producto == null) {
            throw new Cards4PlayException(404, "El producto que se quiere comprar no existe");
        }
        if (!producto.estaDisponible()) {
            throw new Cards4PlayException(409, "El producto " + producto.getNombre() + " ya no está disponible");
        }

        if (producto instanceof Booster) {
            // Un booster además queda asignado a su comprador
            Booster booster = (Booster) producto;
            booster.vender(this); // "this" es este mismo cliente
        } else {
            producto.descontarStock();
        }

        Compra compra = new Compra(producto, tasa);
        historial.add(compra);
        return compra;
    }

    /** Abre un booster propio. Las validaciones (dueño, ya abierto) las hace el Booster. */
    public List<Carta> abrirBooster(Booster booster) {
        return booster.abrir(this);
    }

    /** Se inscribe en un torneo usando la fecha de hoy. Las validaciones las hace el Torneo. */
    public void inscribirse(Torneo torneo) {
        torneo.inscribir(this, LocalDate.now());
    }

    public List<Compra> getHistorial() {
        return new ArrayList<>(historial);
    }

    /** Recorre el historial y devuelve solo los boosters comprados (abiertos o no). */
    public List<Booster> getMisBoosters() {
        List<Booster> boosters = new ArrayList<>();
        for (Compra compra : historial) {
            if (compra.getProducto() instanceof Booster) {
                boosters.add((Booster) compra.getProducto());
            }
        }
        return boosters;
    }
}
