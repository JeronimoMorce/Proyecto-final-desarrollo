package co.edu.eam.cards4play.repository;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Inventario;
import co.edu.eam.cards4play.model.Producto;
import co.edu.eam.cards4play.model.Torneo;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * La "base de datos" de la aplicación: tiene en memoria todos los datos y los
 * guarda en archivos JSON cada vez que cambian.
 *
 *   carpetaDatos/
 *     productos.json   -> el Inventario (cartas, boosters, sellados, accesorios)
 *     clientes.json    -> los clientes con su historial de compras
 *     torneos.json     -> los torneos con los ids de sus participantes
 *
 * Ciclo de vida:
 *  1. Al arrancar el servidor se llama cargar(): lee los 3 archivos.
 *  2. Mientras funciona, los servicios leen y modifican los datos en memoria.
 *  3. Después de cada cambio, el servicio llama guardarProductos(),
 *     guardarClientes() o guardarTorneos().
 *
 * Los servicios usan "synchronized (almacen)" al modificar datos: si llegan dos
 * compras al mismo tiempo, una espera a que termine la otra. Así no se vende
 * dos veces el mismo booster.
 *
 * Relación: todos los servicios reciben el mismo AlmacenDatos (se crea una sola vez en Main).
 */
public class AlmacenDatos {

    private final RepositorioJson<Producto> repoProductos;
    private final RepositorioJson<Cliente> repoClientes;
    private final RepositorioJson<Torneo> repoTorneos;

    private final Inventario inventario = new Inventario();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Torneo> torneos = new ArrayList<>();

    public AlmacenDatos(String carpetaDatos) {
        this.repoProductos = new RepositorioJson<>(Paths.get(carpetaDatos, "productos.json").toString(), Producto.class);
        this.repoClientes = new RepositorioJson<>(Paths.get(carpetaDatos, "clientes.json").toString(), Cliente.class);
        this.repoTorneos = new RepositorioJson<>(Paths.get(carpetaDatos, "torneos.json").toString(), Torneo.class);
    }

    /** Lee los archivos JSON y llena las listas en memoria. Se llama una vez al iniciar. */
    public synchronized void cargar() {
        for (Producto producto : repoProductos.cargar()) {
            inventario.registrar(producto);
        }
        for (Cliente cliente : repoClientes.cargar()) {
            if (cliente.tieneDatosCompletos()) {
                clientes.add(cliente);
            } else {
                System.out.println("AVISO: se descarta un cliente con formato antiguo o incompleto");
            }
        }
        for (Torneo torneo : repoTorneos.cargar()) {
            if (torneo.tieneDatosCompletos()) {
                torneos.add(torneo);
            } else {
                System.out.println("AVISO: se descarta un torneo con formato antiguo o incompleto");
            }
        }
        System.out.println("Datos cargados: " + inventario.consultarStock().size() + " productos, "
                + clientes.size() + " clientes, " + torneos.size() + " torneos");
    }

    // ---------------- Guardar (uno por archivo) ----------------

    public synchronized void guardarProductos() {
        repoProductos.guardar(inventario.consultarStock());
    }

    public synchronized void guardarClientes() {
        repoClientes.guardar(clientes);
    }

    public synchronized void guardarTorneos() {
        repoTorneos.guardar(torneos);
    }

    // ---------------- Inventario ----------------

    public Inventario getInventario() {
        return inventario;
    }

    // ---------------- Clientes ----------------

    public synchronized List<Cliente> getClientes() {
        return new ArrayList<>(clientes);
    }

    public synchronized Cliente buscarCliente(String id) {
        for (Cliente cliente : clientes) {
            if (cliente.getId().equals(id)) {
                return cliente;
            }
        }
        throw new Cards4PlayException(404, "No existe un cliente con id " + id);
    }

    /** Devuelve el cliente con ese username, o null si no existe (lo usa el login). */
    public synchronized Cliente buscarClientePorUsername(String username) {
        for (Cliente cliente : clientes) {
            if (cliente.getUsername().equalsIgnoreCase(username)) {
                return cliente;
            }
        }
        return null;
    }

    public synchronized void agregarCliente(Cliente cliente) {
        clientes.add(cliente);
    }

    public synchronized void eliminarCliente(Cliente cliente) {
        clientes.remove(cliente);
    }

    // ---------------- Torneos ----------------

    public synchronized List<Torneo> getTorneos() {
        return new ArrayList<>(torneos);
    }

    public synchronized Torneo buscarTorneo(String id) {
        for (Torneo torneo : torneos) {
            if (torneo.getId().equals(id)) {
                return torneo;
            }
        }
        throw new Cards4PlayException(404, "No existe un torneo con id " + id);
    }

    public synchronized void agregarTorneo(Torneo torneo) {
        torneos.add(torneo);
    }

    public synchronized void eliminarTorneo(Torneo torneo) {
        torneos.remove(torneo);
    }
}
