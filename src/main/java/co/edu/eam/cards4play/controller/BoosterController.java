package co.edu.eam.cards4play.controller;

import co.edu.eam.cards4play.dto.BoosterRequest;
import co.edu.eam.cards4play.mapper.MapeadorRespuesta;
import co.edu.eam.cards4play.model.Booster;
import co.edu.eam.cards4play.model.Carta;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.service.BoosterService;
import co.edu.eam.cards4play.service.ProductoService;
import co.edu.eam.cards4play.service.ServicioTasaCambio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Rutas de boosters (los "packs").
 *
 *   GET  /api/boosters             boosters a la venta, SIN mostrar sus cartas (cualquiera)
 *   POST /api/boosters             crear un booster; se sortean 10 cartas   (ADMIN)   -> 201
 *   GET  /api/mis-boosters         boosters que compró el cliente             (CLIENTE)
 *   POST /api/boosters/:id/abrir   abrir un booster propio, una sola vez      (CLIENTE) -> 200
 *
 * Para COMPRAR un booster se usa POST /api/compras, igual que cualquier producto.
 */
public class BoosterController extends ControladorBase {

    private final BoosterService servicio;
    private final ProductoService productoService;
    private final ServicioTasaCambio servicioTasa;

    public BoosterController(BoosterService servicio, ProductoService productoService,
                             ServicioTasaCambio servicioTasa) {
        this.servicio = servicio;
        this.productoService = productoService;
        this.servicioTasa = servicioTasa;
    }

    @Override
    public void registrarRutas() {

        get("/api/boosters", (request, response) -> json(MapeadorRespuesta.productos(
                productoService.listar(usuarioActual(request), "BOOSTER"), servicioTasa.obtenerTasa())));

        post("/api/boosters", (request, response) -> {
            BoosterRequest datos = leerCuerpo(request, BoosterRequest.class);
            Booster booster = servicio.crear(requerirAdmin(request), datos);
            response.status(201);
            return json(MapeadorRespuesta.producto(booster, servicioTasa.obtenerTasa()));
        });

        get("/api/mis-boosters", (request, response) -> {
            Cliente cliente = requerirCliente(request);
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Booster booster : servicio.misBoosters(cliente)) {
                lista.add(MapeadorRespuesta.boosterDelDueno(booster));
            }
            return json(lista);
        });

        post("/api/boosters/:id/abrir", (request, response) -> {
            Cliente cliente = requerirCliente(request);
            String boosterId = request.params(":id");
            List<Carta> cartas = servicio.abrir(cliente, boosterId);

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("boosterId", boosterId);
            respuesta.put("mensaje", "Booster abierto. Estas son tus " + cartas.size() + " cartas");
            respuesta.put("cartas", MapeadorRespuesta.cartas(cartas));
            return json(respuesta);
        });
    }
}
