package co.edu.eam.cards4play;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Administrador;
import co.edu.eam.cards4play.model.Booster;
import co.edu.eam.cards4play.model.Carta;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Compra;
import co.edu.eam.cards4play.model.Inventario;
import co.edu.eam.cards4play.model.Producto;
import co.edu.eam.cards4play.model.TasaCambio;
import co.edu.eam.cards4play.model.Torneo;
import co.edu.eam.cards4play.model.enums.Rareza;
import co.edu.eam.cards4play.repository.RepositorioJson;
import co.edu.eam.cards4play.service.GeneradorBooster;
import co.edu.eam.cards4play.service.ServicioTasaCambio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Punto de entrada de la aplicación (Entrega 2: esqueleto).
 *
 * Todavía no hay API REST. Este main es una DEMOSTRACIÓN por consola que
 * recorre el sistema de principio a fin para evidenciar que las clases están
 * conectadas como en el diagrama UML.
 *
 * En la siguiente entrega este main arrancará el servidor (Spark Java) y cada
 * paso de la demostración se convertirá en un endpoint.
 */
public class Main {

    public static void main(String[] args) {

        // ------------------------------------------------------------------
        titulo("1. Objetos principales del sistema");
        Inventario inventario = new Inventario();
        GeneradorBooster generador = new GeneradorBooster();
        ServicioTasaCambio servicioTasa = new ServicioTasaCambio();

        // El sistema siempre arranca con un administrador por defecto
        Administrador admin = new Administrador("admin", "admin123");
        System.out.println("Administrador por defecto: " + admin);
        System.out.println("¿Contraseña correcta?   " + admin.autenticar("admin123"));
        System.out.println("¿Contraseña incorrecta? " + admin.autenticar("otra"));

        // ------------------------------------------------------------------
        titulo("2. El administrador registra clientes");
        Cliente nicole = admin.registrarCliente("nicole", "clave1");
        Cliente jeronimo = admin.registrarCliente("jeronimo", "clave2");
        System.out.println(nicole);
        System.out.println(jeronimo);

        // ------------------------------------------------------------------
        titulo("3. El administrador registra productos en el inventario");
        admin.registrarCarta(inventario, "Guerrero Novato", Rareza.N, 0.5, 20);
        admin.registrarCarta(inventario, "Arquera del Bosque", Rareza.N, 0.6, 20);
        admin.registrarCarta(inventario, "Mago Aprendiz", Rareza.UC, 1.2, 10);
        admin.registrarCarta(inventario, "Caballero de Plata", Rareza.R, 3.5, 5);
        admin.registrarCarta(inventario, "Dragón Ancestral", Rareza.UR, 25.0, 1);
        admin.registrarProductoSellado(inventario, "Caja de inicio", 30.0, 3);
        Producto protectores = admin.registrarAccesorio(inventario, "Protectores x100", 8.0, 2);
        for (Producto producto : admin.consultarStock(inventario)) {
            System.out.println(producto);
        }

        // ------------------------------------------------------------------
        titulo("4. Tasa de cambio: el precio en COP se calcula al consultar el producto");
        TasaCambio tasa = servicioTasa.obtenerTasa();
        System.out.println("1 USD = " + tasa.getValor() + " COP (fuenteTasa: " + tasa.getFuente() + ")");
        for (Producto producto : nicole.consultarProductos(inventario)) {
            double precioCop = producto.calcularPrecioCop(tasa);
            System.out.println(producto.getNombre() + " -> COP " + String.format("%,.0f", precioCop));
        }

        // ------------------------------------------------------------------
        titulo("5. Booster: crear, comprar y abrir");
        Booster booster = admin.crearBooster(inventario, generador);
        System.out.println("Creado:   " + booster);
        System.out.println("Cartas visibles antes de abrir: " + booster.getCartasVisibles().size());

        Compra compraBooster = nicole.comprar(booster, tasa);
        System.out.println("Comprado: " + compraBooster);

        List<Carta> cartasObtenidas = nicole.abrirBooster(booster);
        System.out.println("Nicole abre su booster y obtiene " + cartasObtenidas.size() + " cartas:");
        for (Carta carta : cartasObtenidas) {
            System.out.println("   - " + carta.getNombre() + " (" + carta.getRareza() + ")");
        }

        // ------------------------------------------------------------------
        titulo("6. Situaciones que el sistema debe impedir (boosters y stock)");
        try {
            jeronimo.comprar(booster, tasa); // el booster ya es de Nicole
        } catch (Cards4PlayException error) {
            mostrarError(error);
        }
        try {
            nicole.abrirBooster(booster); // ya lo abrió una vez
        } catch (Cards4PlayException error) {
            mostrarError(error);
        }
        try {
            Booster otroBooster = admin.crearBooster(inventario, generador);
            nicole.comprar(otroBooster, tasa);
            jeronimo.abrirBooster(otroBooster); // no es el dueño
        } catch (Cards4PlayException error) {
            mostrarError(error);
        }
        try {
            jeronimo.comprar(protectores, tasa); // quedan 2
            jeronimo.comprar(protectores, tasa); // queda 1
            jeronimo.comprar(protectores, tasa); // ya no hay
        } catch (Cards4PlayException error) {
            mostrarError(error);
        }

        // ------------------------------------------------------------------
        titulo("7. Torneos: cupo, inscripción duplicada y cierre por fecha");
        Torneo torneo = admin.crearTorneo("Copa Cards4Play", LocalDate.now().plusDays(7), 1);
        nicole.inscribirse(torneo);
        System.out.println(torneo);
        try {
            nicole.inscribirse(torneo); // ya está inscrita
        } catch (Cards4PlayException error) {
            mostrarError(error);
        }
        try {
            jeronimo.inscribirse(torneo); // el cupo era de 1
        } catch (Cards4PlayException error) {
            mostrarError(error);
        }
        try {
            // Torneo con cupos libres, pero se simula que "hoy" es el día del torneo
            Torneo relampago = admin.crearTorneo("Torneo Relámpago", LocalDate.now().plusDays(3), 8);
            relampago.inscribir(jeronimo, relampago.getFecha());
        } catch (Cards4PlayException error) {
            mostrarError(error);
        }

        // ------------------------------------------------------------------
        titulo("8. Historial de compras de Nicole");
        for (Compra compra : nicole.getHistorial()) {
            System.out.println(compra);
        }
        for (Booster miBooster : nicole.getMisBoosters()) {
            // Abierto: muestra sus cartas. Cerrado: 0 cartas visibles (contenido oculto).
            System.out.println(miBooster + " | cartas visibles: " + miBooster.getCartasVisibles().size());
        }

        // ------------------------------------------------------------------
        titulo("9. Persistencia en archivos JSON (carpeta data/)");
        RepositorioJson<Producto> repoProductos = new RepositorioJson<>("data/productos.json", Producto.class);
        RepositorioJson<Cliente> repoClientes = new RepositorioJson<>("data/clientes.json", Cliente.class);
        RepositorioJson<Torneo> repoTorneos = new RepositorioJson<>("data/torneos.json", Torneo.class);

        List<Cliente> clientes = new ArrayList<>();
        clientes.add(nicole);
        clientes.add(jeronimo);
        List<Torneo> torneos = new ArrayList<>();
        torneos.add(torneo);

        repoProductos.guardar(inventario.consultarStock());
        repoClientes.guardar(clientes);
        repoTorneos.guardar(torneos);
        System.out.println("Datos guardados. Ahora se leen de nuevo desde los archivos:");

        System.out.println("Productos leídos: " + repoProductos.cargar().size());
        System.out.println("Clientes leídos:  " + repoClientes.cargar().size());
        System.out.println("Torneos leídos:   " + repoTorneos.cargar().size());
    }

    /** Imprime un título para separar cada parte de la demostración. */
    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    /** Muestra el error con el código HTTP que tendrá cuando exista la API REST. */
    private static void mostrarError(Cards4PlayException error) {
        System.out.println("Rechazado (" + error.getCodigoHttp() + "): " + error.getMessage());
    }
}
