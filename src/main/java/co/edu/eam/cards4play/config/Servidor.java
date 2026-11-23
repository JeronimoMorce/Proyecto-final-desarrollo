package co.edu.eam.cards4play.config;

import co.edu.eam.cards4play.controller.ControladorBase;
import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.repository.ConfiguracionGson;
import co.edu.eam.cards4play.security.FiltroAutenticacion;
import com.google.gson.Gson;
import spark.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.awaitInitialization;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.port;

/**
 * Configura y enciende el servidor web con Spark Java.
 *
 * Orden en que Spark atiende cada petición:
 *
 *   petición HTTP
 *     -> before(...)        filtros: tipo JSON + autenticación (FiltroAutenticacion)
 *     -> la ruta que coincide (get/post/put/delete de algún controlador)
 *     -> respuesta JSON
 *
 *   Si en cualquier punto se lanza una excepción, Spark salta a exception(...),
 *   que la convierte en una respuesta de error JSON con su código HTTP.
 */
public class Servidor {

    private static final String TIPO_JSON = "application/json;charset=utf-8";

    private final Gson gson = ConfiguracionGson.crearGson();

    public void iniciar(int puerto, FiltroAutenticacion filtroAutenticacion, List<ControladorBase> controladores) {
        // 1. Puerto. port() debe ir ANTES de declarar cualquier ruta.
        port(puerto);

        // 2. Filtros que corren antes de cada petición
        before((request, response) -> response.type(TIPO_JSON)); // todas las respuestas son JSON
        before(filtroAutenticacion);                              // ¿quién hace la petición?

        // 3. Rutas: cada controlador registra las suyas
        for (ControladorBase controlador : controladores) {
            controlador.registrarRutas();
        }

        // 4. Manejo de errores: siempre JSON con el mismo formato
        exception(Cards4PlayException.class, (error, request, response) ->
                responderError(response, error.getCodigoHttp(), error.getMessage(), error.getDetalles()));

        exception(Exception.class, (error, request, response) -> {
            error.printStackTrace(); // en la consola queda el detalle para el programador
            responderError(response, 500, "Error interno del servidor", null);
        });

        notFound((request, response) -> {
            response.type(TIPO_JSON);
            return errorJson(404, "No existe la ruta " + request.requestMethod() + " " + request.pathInfo()
                    + ". Consulte GET /api/ayuda", null);
        });

        internalServerError((request, response) -> {
            response.type(TIPO_JSON);
            return errorJson(500, "Error interno del servidor", null);
        });

        // 5. Esperar a que el servidor esté listo
        awaitInitialization();
    }

    private void responderError(Response response, int codigo, String mensaje, Map<String, Object> detalles) {
        response.status(codigo);
        response.type(TIPO_JSON);
        response.body(errorJson(codigo, mensaje, detalles));
    }

    /** Formato uniforme de error: { "error": "...", "codigo": 409, ...detalles } */
    private String errorJson(int codigo, String mensaje, Map<String, Object> detalles) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("error", mensaje);
        json.put("codigo", codigo);
        if (detalles != null) {
            json.putAll(detalles);
        }
        return gson.toJson(json);
    }
}
