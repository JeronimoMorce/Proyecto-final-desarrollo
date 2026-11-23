package co.edu.eam.cards4play.controller;

import co.edu.eam.cards4play.dto.Peticion;
import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Administrador;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Usuario;
import co.edu.eam.cards4play.repository.ConfiguracionGson;
import co.edu.eam.cards4play.security.FiltroAutenticacion;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import spark.Request;

/**
 * Herramientas que comparten todos los controladores.
 *
 * Un CONTROLADOR es la puerta de entrada HTTP: recibe la petición, saca los
 * datos (ruta, cuerpo, usuario), llama al servicio y devuelve la respuesta en JSON.
 * No tiene reglas de negocio: esas están en los servicios y en el modelo.
 *
 * Cada controlador hereda de esta clase y registra sus rutas en registrarRutas().
 */
public abstract class ControladorBase {

    protected final Gson gson = ConfiguracionGson.crearGson();

    /** Cada controlador declara aquí sus rutas: get(...), post(...), put(...), delete(...). */
    public abstract void registrarRutas();

    /** Convierte cualquier objeto (normalmente un Map del MapeadorRespuesta) en texto JSON. */
    protected String json(Object objeto) {
        return gson.toJson(objeto);
    }

    /**
     * Lee el cuerpo JSON de la petición y lo convierte en el DTO indicado.
     *  - Cuerpo vacío       -> se trata como "{}" (cada DTO dirá qué campos faltan).
     *  - JSON mal escrito   -> 400 con un ejemplo del formato correcto.
     *  - Tipo equivocado    -> 400 (por ejemplo "stock": "tres" en vez de 3).
     */
    protected <T extends Peticion> T leerCuerpo(Request request, Class<T> claseDto) {
        String cuerpo = request.body();
        if (cuerpo == null || cuerpo.isBlank()) {
            cuerpo = "{}";
        }
        try {
            T dto = gson.fromJson(cuerpo, claseDto);
            if (dto == null) {
                throw new JsonParseException("cuerpo vacío");
            }
            return dto;
        } catch (JsonParseException | IllegalStateException | NumberFormatException error) {
            throw new Cards4PlayException(400, "El cuerpo de la petición no es un JSON válido "
                    + "o algún campo tiene un tipo equivocado")
                    .conDetalle("ejemplo", ejemploDe(claseDto));
        }
    }

    /** Usuario que hizo la petición (lo guardó FiltroAutenticacion). */
    protected Usuario usuarioActual(Request request) {
        return request.attribute(FiltroAutenticacion.ATRIBUTO_USUARIO);
    }

    /** Devuelve el usuario si es administrador; si no, 403. */
    protected Administrador requerirAdmin(Request request) {
        Usuario usuario = usuarioActual(request);
        if (!(usuario instanceof Administrador)) {
            throw new Cards4PlayException(403, "Esta acción es solo para el administrador");
        }
        return (Administrador) usuario;
    }

    /** Devuelve el usuario si es cliente; si no, 403. */
    protected Cliente requerirCliente(Request request) {
        Usuario usuario = usuarioActual(request);
        if (!(usuario instanceof Cliente)) {
            throw new Cards4PlayException(403, "Esta acción es solo para clientes");
        }
        return (Cliente) usuario;
    }

    /** Crea un DTO vacío solo para pedirle su ejemplo. */
    private Object ejemploDe(Class<? extends Peticion> claseDto) {
        try {
            return claseDto.getDeclaredConstructor().newInstance().ejemplo();
        } catch (ReflectiveOperationException error) {
            return null;
        }
    }
}
