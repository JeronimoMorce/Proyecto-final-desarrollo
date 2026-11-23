package co.edu.eam.cards4play.security;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Usuario;
import co.edu.eam.cards4play.service.AutenticacionService;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Filtro que se ejecuta ANTES de cada petición y revisa quién la hace.
 *
 * Usa autenticación HTTP "Basic": en cada petición el cliente envía el encabezado
 *
 *     Authorization: Basic bmljb2xlOmNsYXZlMTIz
 *
 * donde "bmljb2xlOmNsYXZlMTIz" es "nicole:clave123" codificado en Base64.
 * En Postman no hay que hacerlo a mano: pestaña Authorization > Basic Auth.
 *
 * Resultado:
 *  - Sin encabezado o con datos incorrectos -> 401 (la petición se detiene aquí).
 *  - Correcto -> guarda el Usuario en la petición con request.attribute("usuario", ...)
 *    para que el controlador sepa quién es y qué rol tiene.
 *
 * Rutas públicas (no piden usuario): /api/salud y /api/ayuda.
 *
 * OJO: Basic Auth envía la contraseña solo codificada, no cifrada. Por eso en
 * producción siempre se usa HTTPS (las plataformas PaaS lo dan por defecto).
 */
public class FiltroAutenticacion implements Filter {

    /** Nombre con el que se guarda el usuario dentro de la petición. */
    public static final String ATRIBUTO_USUARIO = "usuario";

    private static final List<String> RUTAS_PUBLICAS = Arrays.asList("/api/salud", "/api/ayuda");

    private final AutenticacionService autenticacion;

    public FiltroAutenticacion(AutenticacionService autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public void handle(Request request, Response response) {
        String ruta = request.pathInfo();
        if (!ruta.startsWith("/api/") || RUTAS_PUBLICAS.contains(ruta)) {
            return; // no se pide usuario
        }

        String encabezado = request.headers("Authorization");
        if (encabezado == null || !encabezado.startsWith("Basic ")) {
            // Este encabezado le indica al cliente (navegador, Postman) que use Basic Auth
            response.header("WWW-Authenticate", "Basic realm=\"Cards4Play\"");
            throw new Cards4PlayException(401, "Debe autenticarse con Basic Auth (usuario y contraseña)");
        }

        String[] credenciales = decodificar(encabezado.substring("Basic ".length()));
        Usuario usuario = autenticacion.autenticar(credenciales[0], credenciales[1]); // 401 si no coinciden
        request.attribute(ATRIBUTO_USUARIO, usuario);
    }

    /** "bmljb2xlOmNsYXZlMTIz" -> ["nicole", "clave123"] */
    private String[] decodificar(String base64) {
        try {
            String texto = new String(Base64.getDecoder().decode(base64.trim()), StandardCharsets.UTF_8);
            int separador = texto.indexOf(':');
            if (separador < 0) {
                throw new IllegalArgumentException("sin ':'");
            }
            return new String[]{texto.substring(0, separador), texto.substring(separador + 1)};
        } catch (IllegalArgumentException error) {
            throw new Cards4PlayException(401, "El encabezado Authorization no tiene el formato Basic correcto");
        }
    }
}
