package co.edu.eam.cards4play.mapper;

import co.edu.eam.cards4play.model.Booster;
import co.edu.eam.cards4play.model.Carta;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Compra;
import co.edu.eam.cards4play.model.Inventario;
import co.edu.eam.cards4play.model.Producto;
import co.edu.eam.cards4play.model.TasaCambio;
import co.edu.eam.cards4play.model.Torneo;
import co.edu.eam.cards4play.model.Usuario;
import co.edu.eam.cards4play.repository.AlmacenDatos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte los objetos del modelo en el JSON que se le responde al usuario de la API.
 *
 * ¿Por qué no responder el objeto tal cual? Porque hay datos que NO deben salir:
 *  - la contraseña cifrada de los usuarios,
 *  - las cartas de un booster que todavía no se ha abierto.
 * Y hay datos que el objeto no tiene guardados pero sí se deben mostrar:
 *  - el precio en COP y la fuente de la tasa (se calculan con la tasa del momento).
 *
 * Cada método arma un Map (clave -> valor). Gson lo convierte en JSON:
 *   Map {"id"="CAR-1", "nombre"="Dragón"}   ->   { "id": "CAR-1", "nombre": "Dragón" }
 *
 * Se usa LinkedHashMap para que las claves salgan en el orden en que se agregan.
 */
public final class MapeadorRespuesta {

    private MapeadorRespuesta() {
    }

    // ---------------- Productos ----------------

    /** Un producto con su precio en COP. Las cartas de un booster solo salen si ya fue abierto. */
    public static Map<String, Object> producto(Producto producto, TasaCambio tasa) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", producto.getId());
        json.put("tipo", producto.getTipo());
        json.put("nombre", producto.getNombre());
        json.put("precioUsd", producto.getPrecioUsd());
        json.put("precioCop", redondear(producto.calcularPrecioCop(tasa)));
        json.put("fuenteTasa", tasa.getFuente());
        json.put("stock", producto.getStock());
        json.put("disponible", producto.estaDisponible());

        if (producto instanceof Carta) {
            json.put("rareza", ((Carta) producto).getRareza());
        }
        if (producto instanceof Booster) {
            Booster booster = (Booster) producto;
            json.put("vendido", booster.estaVendido());
            json.put("abierto", booster.isAbierto());
            agregarContenidoBooster(json, booster);
        }
        return json;
    }

    public static List<Map<String, Object>> productos(List<Producto> productos, TasaCambio tasa) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Producto producto : productos) {
            lista.add(producto(producto, tasa));
        }
        return lista;
    }

    /** Un booster visto por su dueño (en "mis boosters"). */
    public static Map<String, Object> boosterDelDueno(Booster booster) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", booster.getId());
        json.put("nombre", booster.getNombre());
        json.put("abierto", booster.isAbierto());
        agregarContenidoBooster(json, booster);
        return json;
    }

    public static List<Map<String, Object>> cartas(List<Carta> cartas) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Carta carta : cartas) {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", carta.getId());
            json.put("nombre", carta.getNombre());
            json.put("rareza", carta.getRareza());
            lista.add(json);
        }
        return lista;
    }

    /** Abierto: muestra las cartas. Cerrado: un aviso, nunca las cartas. */
    private static void agregarContenidoBooster(Map<String, Object> json, Booster booster) {
        if (booster.isAbierto()) {
            json.put("cartas", cartas(booster.getCartasVisibles()));
        } else {
            json.put("contenido", "oculto hasta que el dueño lo abra");
        }
    }

    // ---------------- Usuarios y clientes ----------------

    /** Datos públicos de un usuario: nunca la contraseña. */
    public static Map<String, Object> usuario(Usuario usuario) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", usuario.getId());
        json.put("username", usuario.getUsername());
        json.put("rol", usuario.getRol());
        return json;
    }

    public static Map<String, Object> cliente(Cliente cliente) {
        Map<String, Object> json = usuario(cliente);
        json.put("totalCompras", cliente.getHistorial().size());
        return json;
    }

    public static List<Map<String, Object>> clientes(List<Cliente> clientes) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Cliente cliente : clientes) {
            lista.add(cliente(cliente));
        }
        return lista;
    }

    // ---------------- Compras ----------------

    /**
     * Una compra del historial. Si fue un booster se busca en el inventario
     * para mostrar su estado actual: abierto con sus cartas, o cerrado sin detalle.
     */
    public static Map<String, Object> compra(Compra compra, Inventario inventario) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", compra.getId());
        json.put("fecha", compra.getFecha().toString());
        json.put("productoId", compra.getProductoId());
        json.put("nombreProducto", compra.getNombreProducto());
        json.put("tipoProducto", compra.getTipoProducto());
        json.put("precioUsd", compra.getPrecioUsd());
        json.put("precioCop", redondear(compra.getPrecioCop()));
        json.put("fuenteTasa", compra.getFuenteTasa());

        if (compra.esDeBooster()) {
            Booster booster = (Booster) inventario.buscarPorId(compra.getProductoId());
            json.put("boosterAbierto", booster.isAbierto());
            agregarContenidoBooster(json, booster);
        }
        return json;
    }

    public static List<Map<String, Object>> compras(List<Compra> compras, Inventario inventario) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Compra compra : compras) {
            lista.add(compra(compra, inventario));
        }
        return lista;
    }

    // ---------------- Torneos ----------------

    /** Un torneo con sus cupos y la lista de participantes (id y username). */
    public static Map<String, Object> torneo(Torneo torneo, AlmacenDatos almacen) {
        int inscritos = torneo.getParticipantesIds().size();

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", torneo.getId());
        json.put("nombre", torneo.getNombre());
        json.put("fecha", torneo.getFecha().toString());
        json.put("fechaLimiteInscripcion", torneo.getFechaLimiteInscripcion().toString());
        json.put("inscripcionAbierta", torneo.inscripcionAbierta(LocalDate.now()));
        json.put("maxParticipantes", torneo.getMaxParticipantes());
        json.put("inscritos", inscritos);
        json.put("cuposDisponibles", torneo.getMaxParticipantes() - inscritos);

        List<Map<String, Object>> participantes = new ArrayList<>();
        for (String clienteId : torneo.getParticipantesIds()) {
            participantes.add(usuario(almacen.buscarCliente(clienteId)));
        }
        json.put("participantes", participantes);
        return json;
    }

    public static List<Map<String, Object>> torneos(List<Torneo> torneos, AlmacenDatos almacen) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Torneo torneo : torneos) {
            lista.add(torneo(torneo, almacen));
        }
        return lista;
    }

    // ---------------- Utilidades ----------------

    /** Deja un número con 2 decimales: 15651.0864 -> 15651.09 */
    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
