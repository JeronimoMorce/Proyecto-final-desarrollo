package co.edu.eam.cards4play.controller;

import co.edu.eam.cards4play.dto.ActualizarProductoRequest;
import co.edu.eam.cards4play.dto.CartaRequest;
import co.edu.eam.cards4play.dto.ProductoRequest;
import co.edu.eam.cards4play.mapper.MapeadorRespuesta;
import co.edu.eam.cards4play.model.Producto;
import co.edu.eam.cards4play.model.TasaCambio;
import co.edu.eam.cards4play.service.ProductoService;
import co.edu.eam.cards4play.service.ServicioTasaCambio;
import spark.Request;
import spark.Route;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.patch;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 * Rutas del inventario.
 *
 * Consultar (cualquier usuario autenticado; el cliente ve solo lo disponible):
 *   GET /api/productos           todos (filtro opcional ?tipo=CARTA|BOOSTER|SELLADO|ACCESORIO)
 *   GET /api/productos/:id       uno
 *   GET /api/cartas              atajos por tipo
 *   GET /api/sellados
 *   GET /api/accesorios
 *   (GET /api/boosters está en BoosterController)
 *
 * Registrar y modificar (solo ADMIN):
 *   POST   /api/cartas           -> 201
 *   POST   /api/sellados         -> 201
 *   POST   /api/accesorios       -> 201
 *   PUT    /api/productos/:id    -> 200
 *   DELETE /api/productos/:id    -> 204
 *
 * Toda respuesta con productos trae precioCop y fuenteTasa (API o DEFECTO).
 * La tasa se pide aquí, al CONSULTAR el producto (corrección del profesor).
 */
public class ProductoController extends ControladorBase {

    private final ProductoService servicio;
    private final ServicioTasaCambio servicioTasa;

    public ProductoController(ProductoService servicio, ServicioTasaCambio servicioTasa) {
        this.servicio = servicio;
        this.servicioTasa = servicioTasa;
    }

    @Override
    public void registrarRutas() {

        // ---------- Consultas ----------
        get("/api/productos", (request, response) ->
                listar(request, request.queryParams("tipo"))); // ?tipo=CARTA -> "CARTA"
        get("/api/cartas", (request, response) -> listar(request, "CARTA"));
        get("/api/sellados", (request, response) -> listar(request, "PRODUCTOSELLADO"));
        get("/api/accesorios", (request, response) -> listar(request, "ACCESORIO"));

        get("/api/productos/:id", (request, response) -> {
            Producto producto = servicio.buscar(request.params(":id"));
            return json(MapeadorRespuesta.producto(producto, servicioTasa.obtenerTasa()));
        });

        // ---------- Registro (admin) ----------
        post("/api/cartas", (request, response) -> {
            CartaRequest datos = leerCuerpo(request, CartaRequest.class);
            Producto carta = servicio.registrarCarta(requerirAdmin(request), datos);
            response.status(201);
            return json(MapeadorRespuesta.producto(carta, servicioTasa.obtenerTasa()));
        });

        post("/api/sellados", (request, response) -> {
            ProductoRequest datos = leerCuerpo(request, ProductoRequest.class);
            Producto sellado = servicio.registrarSellado(requerirAdmin(request), datos);
            response.status(201);
            return json(MapeadorRespuesta.producto(sellado, servicioTasa.obtenerTasa()));
        });

        post("/api/accesorios", (request, response) -> {
            ProductoRequest datos = leerCuerpo(request, ProductoRequest.class);
            Producto accesorio = servicio.registrarAccesorio(requerirAdmin(request), datos);
            response.status(201);
            return json(MapeadorRespuesta.producto(accesorio, servicioTasa.obtenerTasa()));
        });

        // ---------- Modificar y eliminar (admin) ----------
        // PATCH = modificar solo algunos campos. Se guarda el manejador en una variable
        // (Route) para registrarlo en PATCH y en PUT sin repetir código.
        Route actualizar = (request, response) -> {
            requerirAdmin(request);
            ActualizarProductoRequest datos = leerCuerpo(request, ActualizarProductoRequest.class);
            Producto producto = servicio.actualizar(request.params(":id"), datos);
            return json(MapeadorRespuesta.producto(producto, servicioTasa.obtenerTasa()));
        };
        patch("/api/productos/:id", actualizar);
        put("/api/productos/:id", actualizar);

        delete("/api/productos/:id", (request, response) -> {
            requerirAdmin(request);
            servicio.eliminar(request.params(":id"));
            response.status(204);
            return "";
        });
    }

    /** Lista según el rol del usuario y el tipo pedido, con precios en COP. */
    private String listar(Request request, String tipo) {
        TasaCambio tasa = servicioTasa.obtenerTasa(); // una sola consulta para toda la lista
        return json(MapeadorRespuesta.productos(servicio.listar(usuarioActual(request), tipo), tasa));
    }
}
