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
 *  3. La tasa por defecto se lee de la variable de entorno TASA_DEFECTO.
 *  4. El resultado dice de dónde salió la tasa (API o DEFECTO).
 *
 * API usada: ExchangeRate-API v6 (con llave).
 *   URL:  https://v6.exchangerate-api.com/v6/{API_KEY}/latest/USD
 *   Éxito: { "result": "success", "base_code": "USD", "conversion_rates": { "COP": 3912.77, ... } }
 *   Error: { "result": "error", "error-type": "invalid-key" }
 *
 * OJO: esta API usa el campo "conversion_rates", NO "rates" (ese era el de open.er-api.com).
 *
 * La llave NO se escribe en el código (el repositorio es público). Se lee de la
 * variable de entorno EXCHANGE_API_KEY.
 *
 * Relación UML: ServicioTasaCambio crea objetos TasaCambio.
 */
public class ServicioTasaCambio {

    /** Plantilla de la URL; %s se reemplaza por la llave. */
    private static final String URL_API = "https://v6.exchangerate-api.com/v6/08b7de16761b0dab67d9ae37/latest/USD";
    private static final int TIMEOUT_MS = 3000;

    /** Valor que se usa solo si la variable de entorno TASA_DEFECTO no existe. */
    private static final double TASA_SIN_VARIABLE = 4000.0;

    private final String urlApi;
    private final int timeoutMs;
    private final double tasaDefecto;

    /** Constructor normal: arma la URL con la llave de EXCHANGE_API_KEY y lee TASA_DEFECTO. */
    public ServicioTasaCambio() {
        this(String.format(URL_API, leerVariable("EXCHANGE_API_KEY")), TIMEOUT_MS, leerTasaDefecto());
    }

    /** Constructor con todos los datos; sirve para hacer pruebas (por ejemplo, con una URL que falle). */
    public ServicioTasaCambio(String urlApi, int timeoutMs, double tasaDefecto) {
        this.urlApi = urlApi;
        this.timeoutMs = timeoutMs;
        this.tasaDefecto = tasaDefecto;
    }

    /**
     * Devuelve la tasa de cambio actual.
     * Primero intenta con la API; si algo sale mal, usa la tasa por defecto.
     */
    public TasaCambio obtenerTasa() {
        try {
            double valor = consultarApi();
            return new TasaCambio(valor, FuenteTasa.API);
        } catch (Exception error) {
            // Se muestra la causa real: sin esta línea el error queda escondido
            // y solo se ve el mensaje de la tasa por defecto.
            System.out.println("Error al consultar la API de tasa de cambio: "
                    + error.getClass().getSimpleName() + " - " + error.getMessage());
            return usarTasaDefecto();
        }
    }

    /** Hace la petición HTTP a la API y saca el valor del peso colombiano de la respuesta. */
    private double consultarApi() throws IOException, InterruptedException {
        // Cliente HTTP con tiempo máximo para conectarse
        HttpClient cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();

        // Petición GET con tiempo máximo para recibir la respuesta
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(urlApi))
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();

        HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new IOException("la API respondió con código HTTP " + respuesta.statusCode());
        }

        JsonObject json = JsonParser.parseString(respuesta.body()).getAsJsonObject();

        // 1. La API dice en "result" si todo salió bien ("success") o mal ("error")
        String resultado = json.get("result").getAsString();
        if (!"success".equals(resultado)) {
            // Ejemplos de error-type: invalid-key, inactive-account, quota-reached
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

    /**
     * Lee la variable de entorno TASA_DEFECTO.
     * En Docker se define con ENV en el Dockerfile y se puede cambiar con: docker run -e TASA_DEFECTO=4200
     */
    private static double leerTasaDefecto() {
        String texto = System.getenv("TASA_DEFECTO");
        if (texto == null || texto.isBlank()) {
            return TASA_SIN_VARIABLE;
        }
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException error) {
            return TASA_SIN_VARIABLE; // la variable existe pero no es un número
        }
    }

    /** Lee una variable de entorno; si no existe devuelve texto vacío (la API responderá error). */
    private static String leerVariable(String nombre) {
        String valor = System.getenv(nombre);
        return valor == null ? "" : valor.trim();
    }
}