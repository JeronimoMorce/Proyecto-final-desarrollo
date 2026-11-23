package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.enums.Rol;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente de la tienda: consulta productos, compra, abre sus boosters y se inscribe en torneos.
 *
 * El Cliente casi no tiene lógica propia: le PIDE el trabajo a las otras
 * clases (Inventario, Booster, Torneo). Eso es lo que muestran las flechas del UML.
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
        if (producto instanceof Booster && ((Booster) producto).estaVendido()) {
            throw new Cards4PlayException(409, "El booster " + producto.getId() + " ya fue vendido");
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

    /** Además de lo de Usuario, el historial debe existir y cada compra debe apuntar a un producto. */
    @Override
    public boolean tieneDatosCompletos() {
        if (!super.tieneDatosCompletos() || historial == null) {
            return false;
        }
        for (Compra compra : historial) {
            if (compra.getProductoId() == null) {
                return false;
            }
        }
        return true;
    }

    public List<Compra> getHistorial() {
        return new ArrayList<>(historial);
    }

    /** Busca en el inventario los boosters cuyo dueño es este cliente (abiertos o no). */
    public List<Booster> getMisBoosters(Inventario inventario) {
        List<Booster> boosters = new ArrayList<>();
        for (Producto producto : inventario.consultarStock()) {
            if (producto instanceof Booster && ((Booster) producto).esDe(this)) {
                boosters.add((Booster) producto);
            }
        }
        return boosters;
    }
}
