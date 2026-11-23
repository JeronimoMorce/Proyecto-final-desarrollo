package co.edu.eam.cards4play.service;

import co.edu.eam.cards4play.dto.BoosterRequest;
import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Administrador;
import co.edu.eam.cards4play.model.Booster;
import co.edu.eam.cards4play.model.Carta;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Producto;
import co.edu.eam.cards4play.repository.AlmacenDatos;

import java.util.List;

/**
 * Lógica de los boosters: crearlos (admin), ver los propios y abrirlos (cliente).
 * La compra de un booster la hace CompraService, igual que cualquier producto.
 */
public class BoosterService {

    private final AlmacenDatos almacen;
    private final GeneradorBooster generador;

    public BoosterService(AlmacenDatos almacen, GeneradorBooster generador) {
        this.almacen = almacen;
        this.generador = generador;
    }

    /** El administrador crea un booster: GeneradorBooster sortea sus 10 cartas. */
    public Booster crear(Administrador admin, BoosterRequest datos) {
        synchronized (almacen) {
            Booster booster = admin.crearBooster(almacen.getInventario(), generador,
                    datos.getPrecioUsdOPorDefecto());
            almacen.guardarProductos();
            return booster;
        }
    }

    /** Boosters comprados por el cliente, abiertos o no. */
    public List<Booster> misBoosters(Cliente cliente) {
        return cliente.getMisBoosters(almacen.getInventario());
    }

    /**
     * El cliente abre un booster suyo.
     * Errores: 404 si no existe, 403 si es de otro, 409 si ya se abrió (los dos últimos los lanza Booster).
     */
    public List<Carta> abrir(Cliente cliente, String boosterId) {
        synchronized (almacen) {
            Booster booster = buscarBooster(boosterId);
            List<Carta> cartas = cliente.abrirBooster(booster);
            almacen.guardarProductos(); // el booster quedó con abierto = true
            return cartas;
        }
    }

    private Booster buscarBooster(String id) {
        Producto producto = almacen.getInventario().buscarPorId(id);
        if (!(producto instanceof Booster)) {
            throw new Cards4PlayException(404, "No existe un booster con id " + id);
        }
        return (Booster) producto;
    }
}
