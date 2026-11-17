package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.model.enums.Rareza;
import co.edu.eam.cards4play.model.enums.Rol;
import co.edu.eam.cards4play.service.GeneradorBooster;

import java.time.LocalDate;
import java.util.List;

/**
 * Administrador de la tienda: gestiona clientes, inventario, boosters y torneos.
 *
 * Es el único que puede registrar clientes y crear boosters y torneos.
 * (La restricción "solo el admin puede" se validará con el rol en la entrega de la API REST.)
 *
 * Los métodos que tocan el inventario lo reciben como parámetro: así se ve en
 * el código la flecha "Administrador gestiona Inventario" del diagrama.
 *
 * Relaciones UML:
 *  - Hereda de Usuario.
 *  - Gestiona el Inventario.
 *  - Usa GeneradorBooster para crear boosters.
 *  - Crea Torneo y registra Cliente.
 */
public class Administrador extends Usuario {

    public Administrador(String username, String password) {
        super(username, password, Rol.ADMIN); // todo Administrador nace con rol ADMIN
    }

    /** Crea un cliente nuevo con el usuario y la contraseña que le asigna el administrador. */
    public Cliente registrarCliente(String username, String password) {
        return new Cliente(username, password);
    }

    /** Crea una carta y la guarda en el inventario. */
    public Carta registrarCarta(Inventario inventario, String nombre, Rareza rareza, double precioUsd, int stock) {
        Carta carta = new Carta(nombre, precioUsd, stock, rareza);
        inventario.registrar(carta);
        return carta;
    }

    /** Crea un producto sellado y lo guarda en el inventario. */
    public ProductoSellado registrarProductoSellado(Inventario inventario, String nombre, double precioUsd, int stock) {
        ProductoSellado sellado = new ProductoSellado(nombre, precioUsd, stock);
        inventario.registrar(sellado);
        return sellado;
    }

    /** Crea un accesorio y lo guarda en el inventario. */
    public Accesorio registrarAccesorio(Inventario inventario, String nombre, double precioUsd, int stock) {
        Accesorio accesorio = new Accesorio(nombre, precioUsd, stock);
        inventario.registrar(accesorio);
        return accesorio;
    }

    /**
     * Crea un booster. El administrador no elige las cartas: se lo pide al
     * GeneradorBooster, que las sortea, y luego guarda el booster en el inventario.
     */
    public Booster crearBooster(Inventario inventario, GeneradorBooster generador) {
        Booster booster = generador.generar(inventario);
        inventario.registrar(booster);
        return booster;
    }

    /** Crea un torneo. Las inscripciones cierran el día anterior a la fecha del torneo. */
    public Torneo crearTorneo(String nombre, LocalDate fecha, int maxParticipantes) {
        return new Torneo(nombre, fecha, maxParticipantes);
    }

    /** Vista del administrador: todos los productos con su stock. */
    public List<Producto> consultarStock(Inventario inventario) {
        return inventario.consultarStock();
    }
}
