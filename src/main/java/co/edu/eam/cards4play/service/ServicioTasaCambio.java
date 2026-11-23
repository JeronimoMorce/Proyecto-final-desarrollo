package co.edu.eam.cards4play.service;

import co.edu.eam.cards4play.model.TasaCambio;
import co.edu.eam.cards4play.model.enums.FuenteTasa;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Servicio que consulta la tasa de cambio USD -> COP en una API externa.
 *
 * Reglas del enunciado que se cumplen aquí:
 *  1. La consulta tiene un tiempo máximo de espera (timeout).
 *  2. Si la API falla o no responde, se usa una tasa por defecto y se avisa por consola.
 *  3. La tasa por defecto viene de la variable de entorno TASA_DEFECTO.
 *  4. El resultado dice de dónde salió la tasa (API o DEFECTO).
 *
 * API usada: ExchangeRate-API v6 (con llave).
 *   URL:   https://v6.exchangerate-api.com/v6/{LLAVE}/latest/USD
 *   Éxito: { "result": "success", "conversion_rates": { "COP": 3912.77, ... } }
 *   Error: { "result": "error", "error-type": "invalid-key" }
 *
 * La llave NO va escrita en el código: llega por la variable de entorno
 * EXCHANGE_API_KEY (ver config/Configuracion.java). El repositorio es público.
 *
 * CACHÉ: cada vez que alguien lista productos se necesita la tasa. Para no gastar
 * la cuota gratuita de la API en cada petición, una tasa obtenida de la API se
 * reutiliza durante 10 minutos. La tasa por defecto NO se guarda en caché: en la
 * siguiente petición se vuelve a intentar con la API.
 *
 * Relación UML: ServicioTasaCambio crea objetos TasaCambio.
 */
public class ServicioTasaCambio {

    private static final String URL_API = "https://v6.exchangerate-api.com/v6/%s/latest/USD";
    private static final int TIMEOUT_MS = 3000;
    private static final long DURACION_CACHE_MS = 10 * 60 * 1000; // 10 minutos

    private final String urlApi;
    private final int timeoutMs;
    private final double tasaDefecto;

    private TasaCambio ultimaTasaApi;      // última tasa que respondió la API
    private long momentoUltimaConsulta;    // cuándo se obtuvo (milisegundos)

    /** Constructor normal: arma la URL real con la llave recibida (si no hay llave, la URL queda en null). */
    public ServicioTasaCambio(String llaveApi, double tasaDefecto) {
        this(llaveApi == null || llaveApi.isBlank() ? null : String.format(URL_API, llaveApi.trim()),
                TIMEOUT_MS, tasaDefecto);
    }

    /** Constructor con todos los datos; sirve para pruebas (por ejemplo, con una URL que falle). */
    public ServicioTasaCambio(String urlApi, int timeoutMs, double tasaDefecto) {
        this.urlApi = urlApi;
        this.timeoutMs = timeoutMs;
        this.tasaDefecto = tasaDefecto;
    }

    /**
     * Devuelve la tasa de cambio actual.
     * "synchronized": si llegan dos peticiones al mismo tiempo, la segunda espera
     * y reutiliza la caché en vez de llamar dos veces a la API.
     */
    public synchronized TasaCambio obtenerTasa() {
        boolean cacheVigente = ultimaTasaApi != null
                && System.currentTimeMillis() - momentoUltimaConsulta < DURACION_CACHE_MS;
        if (cacheVigente) {
            return ultimaTasaApi;
        }
        try {
            double valor = consultarApi();
            ultimaTasaApi = new TasaCambio(valor, FuenteTasa.API);
            momentoUltimaConsulta = System.currentTimeMillis();
            return ultimaTasaApi;
        } catch (Exception error) {
            // Se muestra la causa real para poder diagnosticar el problema
            System.out.println("Error al consultar la API de tasa de cambio: "
                    + error.getClass().getSimpleName() + " - " + error.getMessage());
            return usarTasaDefecto();
        }
    }

    /** Hace la petición HTTP a la API y saca el valor del peso colombiano de la respuesta. */
    private double consultarApi() throws IOException, InterruptedException {
        if (urlApi == null) {
            throw new IOException("no hay llave configurada (variable de entorno EXCHANGE_API_KEY)");
        }
        HttpClient cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs)) // tiempo máximo para conectarse
                .build();

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(urlApi))
                .timeout(Duration.ofMillis(timeoutMs))         // tiempo máximo para recibir la respuesta
                .GET()
                .build();

        HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new IOException("la API respondió con código HTTP " + respuesta.statusCode());
        }

        JsonObject json = JsonParser.parseString(respuesta.body()).getAsJsonObject();

        // 1. La API dice en "result" si todo salió bien ("success") o mal ("error")
        if (!"success".equals(json.get("result").getAsString())) {
            throw new IOException("la API respondió con error: " + json.get("error-type").getAsString());
        }

        // 2. Las tasas vienen dentro de "conversion_rates"
        JsonObject tasas = json.getAsJsonObject("conversion_rates");
        if (tasas == null || !tasas.has("COP")) {
            throw new IOException("la respuesta no trae la tasa COP");
        }

        double valor = tasas.get("COP").getAsDouble();
        System.out.println("Tasa obtenida de la API: 1 USD = " + valor + " COP");
        return valor;
    }

    /** Devuelve la tasa por defecto y muestra en consola el mensaje que pide el enunciado. */
    private TasaCambio usarTasaDefecto() {
        System.out.println("La API no respondió, por lo que se toma un valor por defecto de " + tasaDefecto);
        return new TasaCambio(tasaDefecto, FuenteTasa.DEFECTO);
    }
}
