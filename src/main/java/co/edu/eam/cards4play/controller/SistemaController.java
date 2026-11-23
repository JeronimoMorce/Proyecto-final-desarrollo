package co.edu.eam.cards4play.controller;

import co.edu.eam.cards4play.dto.ActualizarProductoRequest;
import co.edu.eam.cards4play.dto.ActualizarTorneoRequest;
import co.edu.eam.cards4play.dto.BoosterRequest;
import co.edu.eam.cards4play.dto.CartaRequest;
import co.edu.eam.cards4play.dto.ClienteRequest;
import co.edu.eam.cards4play.dto.CompraRequest;
import co.edu.eam.cards4play.dto.ProductoRequest;
import co.edu.eam.cards4play.dto.TorneoRequest;
import co.edu.eam.cards4play.mapper.MapeadorRespuesta;
import co.edu.eam.cards4play.model.TasaCambio;
import co.edu.eam.cards4play.service.ServicioTasaCambio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;

/**
 * Rutas generales del sistema.
 *
 *   GET /api/salud   ¿el servidor está vivo?            (pública)
 *   GET /api/ayuda   lista de rutas con ejemplos de cuerpo (pública)
 *   GET /api/yo      ¿con qué usuario estoy autenticado?
 *   GET /api/tasa    tasa USD -> COP actual y su fuente
 */
public class SistemaController extends ControladorBase {

    private final ServicioTasaCambio servicioTasa;

    public SistemaController(ServicioTasaCambio servicioTasa) {
        this.servicioTasa = servicioTasa;
    }

    @Override
    public void registrarRutas() {

        get("/api/salud", (request, response) -> {
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("estado", "ok");
            respuesta.put("aplicacion", "Cards4Play");
            return json(respuesta);
        });

        // Ruta de salud que pide la guía de despliegue. No empieza por /api/,
        // así que FiltroAutenticacion la deja pasar sin usuario ni contraseña.
        get("/health", (request, response) -> {
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("estado", "API funcionando");
            return json(respuesta);
        });

        get("/api/ayuda", (request, response) -> json(construirAyuda()));

        get("/api/yo", (request, response) -> json(MapeadorRespuesta.usuario(usuarioActual(request))));

        get("/api/tasa", (request, response) -> {
            TasaCambio tasa = servicioTasa.obtenerTasa();
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("usdACop", tasa.getValor());
            respuesta.put("fuenteTasa", tasa.getFuente());
            return json(respuesta);
        });
    }

    /** Arma el "manual" de la API. Los ejemplos salen de los mismos DTO que validan las peticiones. */
    private Map<String, Object> construirAyuda() {
        List<Map<String, Object>> rutas = new ArrayList<>();
        rutas.add(ruta("GET", "/api/salud", "PÚBLICA", "Comprueba que el servidor responde", null));
        rutas.add(ruta("GET", "/api/ayuda", "PÚBLICA", "Esta lista", null));
        rutas.add(ruta("GET", "/api/yo", "CUALQUIERA", "Usuario autenticado", null));
        rutas.add(ruta("GET", "/api/tasa", "CUALQUIERA", "Tasa USD a COP y su fuente", null));

        rutas.add(ruta("POST", "/api/clientes", "ADMIN", "Registrar cliente", new ClienteRequest().ejemplo()));
        rutas.add(ruta("GET", "/api/clientes", "ADMIN", "Listar clientes", null));
        rutas.add(ruta("GET", "/api/clientes/:id", "ADMIN", "Ver un cliente", null));
        rutas.add(ruta("GET", "/api/clientes/:id/compras", "ADMIN", "Historial de un cliente", null));
        rutas.add(ruta("DELETE", "/api/clientes/:id", "ADMIN", "Eliminar cliente sin compras ni torneos", null));

        rutas.add(ruta("GET", "/api/productos?tipo=CARTA|BOOSTER|SELLADO|ACCESORIO", "CUALQUIERA",
                "Inventario (cliente: solo disponibles; admin: todo el stock)", null));
        rutas.add(ruta("GET", "/api/productos/:id", "CUALQUIERA", "Ver un producto", null));
        rutas.add(ruta("GET", "/api/cartas", "CUALQUIERA", "Listar cartas", null));
        rutas.add(ruta("GET", "/api/sellados", "CUALQUIERA", "Listar productos sellados", null));
        rutas.add(ruta("GET", "/api/accesorios", "CUALQUIERA", "Listar accesorios", null));
        rutas.add(ruta("POST", "/api/cartas", "ADMIN", "Registrar carta (rareza: N, UC, R, SR, UR)",
                new CartaRequest().ejemplo()));
        rutas.add(ruta("POST", "/api/sellados", "ADMIN", "Registrar producto sellado", new ProductoRequest().ejemplo()));
        rutas.add(ruta("POST", "/api/accesorios", "ADMIN", "Registrar accesorio", new ProductoRequest().ejemplo()));
        rutas.add(ruta("PUT", "/api/productos/:id", "ADMIN", "Modificar producto (campos opcionales)",
                new ActualizarProductoRequest().ejemplo()));
        rutas.add(ruta("DELETE", "/api/productos/:id", "ADMIN", "Eliminar producto (no boosters vendidos)", null));

        rutas.add(ruta("GET", "/api/boosters", "CUALQUIERA", "Boosters a la venta, sin revelar cartas", null));
        rutas.add(ruta("POST", "/api/boosters", "ADMIN", "Crear booster (precioUsd opcional)", new BoosterRequest().ejemplo()));
        rutas.add(ruta("GET", "/api/mis-boosters", "CLIENTE", "Mis boosters, abiertos o no", null));
        rutas.add(ruta("POST", "/api/boosters/:id/abrir", "CLIENTE", "Abrir un booster propio (una vez)", null));

        rutas.add(ruta("POST", "/api/compras", "CLIENTE", "Comprar un producto", new CompraRequest().ejemplo()));
        rutas.add(ruta("GET", "/api/compras", "CLIENTE", "Mi historial de compras", null));

        rutas.add(ruta("GET", "/api/torneos", "CUALQUIERA", "Listar torneos", null));
        rutas.add(ruta("GET", "/api/torneos/:id", "CUALQUIERA", "Ver un torneo", null));
        rutas.add(ruta("POST", "/api/torneos", "ADMIN", "Crear torneo (fechaLimiteInscripcion opcional)",
                new TorneoRequest().ejemplo()));
        rutas.add(ruta("PUT", "/api/torneos/:id", "ADMIN", "Modificar torneo (campos opcionales)",
                new ActualizarTorneoRequest().ejemplo()));
        rutas.add(ruta("DELETE", "/api/torneos/:id", "ADMIN", "Eliminar torneo sin inscritos", null));
        rutas.add(ruta("POST", "/api/torneos/:id/inscripciones", "CLIENTE", "Inscribirse en un torneo", null));

        Map<String, Object> ayuda = new LinkedHashMap<>();
        ayuda.put("autenticacion", "Basic Auth: en Postman, pestaña Authorization > Basic Auth con usuario y contraseña");
        ayuda.put("formatoErrores", "{ \"error\": \"descripción\", \"codigo\": 400, ... }");
        ayuda.put("rutas", rutas);
        return ayuda;
    }

    private Map<String, Object> ruta(String metodo, String ruta, String rol, String descripcion,
                                     Map<String, Object> cuerpoEjemplo) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("metodo", metodo);
        json.put("ruta", ruta);
        json.put("rol", rol);
        json.put("descripcion", descripcion);
        if (cuerpoEjemplo != null) {
            json.put("cuerpoEjemplo", cuerpoEjemplo);
        }
        return json;
    }
}
