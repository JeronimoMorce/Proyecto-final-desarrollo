package co.edu.eam.cards4play.controller;

import co.edu.eam.cards4play.dto.ActualizarTorneoRequest;
import co.edu.eam.cards4play.dto.TorneoRequest;
import co.edu.eam.cards4play.mapper.MapeadorRespuesta;
import co.edu.eam.cards4play.model.Torneo;
import co.edu.eam.cards4play.repository.AlmacenDatos;
import co.edu.eam.cards4play.service.TorneoService;
import spark.Route;

import java.util.LinkedHashMap;
import java.util.Map;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.patch;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 * Rutas de torneos.
 *
 *   GET    /api/torneos                    listar (cualquiera)
 *   GET    /api/torneos/:id                ver uno (cualquiera)
 *   POST   /api/torneos                    crear (ADMIN)                 -> 201
 *   PUT    /api/torneos/:id                modificar (ADMIN)             -> 200
 *   DELETE /api/torneos/:id                eliminar sin inscritos (ADMIN) -> 204
 *   POST   /api/torneos/:id/inscripciones  inscribirse (CLIENTE)         -> 201
 */
public class TorneoController extends ControladorBase {

    private final TorneoService servicio;
    private final AlmacenDatos almacen;

    public TorneoController(TorneoService servicio, AlmacenDatos almacen) {
        this.servicio = servicio;
        this.almacen = almacen;
    }

    @Override
    public void registrarRutas() {

        get("/api/torneos", (request, response) ->
                json(MapeadorRespuesta.torneos(servicio.listar(), almacen)));

        get("/api/torneos/:id", (request, response) ->
                json(MapeadorRespuesta.torneo(servicio.buscar(request.params(":id")), almacen)));

        post("/api/torneos", (request, response) -> {
            TorneoRequest datos = leerCuerpo(request, TorneoRequest.class);
            Torneo torneo = servicio.crear(requerirAdmin(request), datos);
            response.status(201);
            return json(MapeadorRespuesta.torneo(torneo, almacen));
        });

        // PATCH y PUT usan el mismo manejador (ver ProductoController)
        Route actualizar = (request, response) -> {
            requerirAdmin(request);
            ActualizarTorneoRequest datos = leerCuerpo(request, ActualizarTorneoRequest.class);
            Torneo torneo = servicio.actualizar(request.params(":id"), datos);
            return json(MapeadorRespuesta.torneo(torneo, almacen));
        };
        patch("/api/torneos/:id", actualizar);
        put("/api/torneos/:id", actualizar);

        delete("/api/torneos/:id", (request, response) -> {
            requerirAdmin(request);
            servicio.eliminar(request.params(":id"));
            response.status(204);
            return "";
        });

        post("/api/torneos/:id/inscripciones", (request, response) -> {
            Torneo torneo = servicio.inscribir(requerirCliente(request), request.params(":id"));
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("mensaje", "Inscripción registrada en " + torneo.getNombre());
            respuesta.put("torneo", MapeadorRespuesta.torneo(torneo, almacen));
            response.status(201);
            return json(respuesta);
        });
    }
}
