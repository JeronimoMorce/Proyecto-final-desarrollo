package co.edu.eam.cards4play.config;

/**
 * Lee la configuración de la aplicación desde VARIABLES DE ENTORNO.
 *
 * ¿Por qué variables de entorno y no valores escritos en el código?
 *  - Cambian según dónde corre la app (tu PC, Docker, la nube) sin recompilar.
 *  - Los secretos (llave de la API, contraseña del admin) no quedan en GitHub.
 *
 * | Variable          | Para qué sirve                              | Valor si no existe |
 * |-------------------|---------------------------------------------|--------------------|
 * | PORT              | Puerto donde escucha el servidor            | 4567               |
 * | DATA_DIR          | Carpeta de los archivos JSON (el volumen)   | data               |
 * | TASA_DEFECTO      | Tasa USD->COP si la API falla               | 4000               |
 * | EXCHANGE_API_KEY  | Llave de ExchangeRate-API                   | (vacía)            |
 * | ADMIN_USERNAME    | Usuario del administrador por defecto       | admin              |
 * | ADMIN_PASSWORD    | Contraseña del administrador por defecto    | admin123           |
 *
 * En IntelliJ se configuran en: Run > Edit Configurations > Environment variables.
 * En Docker: con ENV en el Dockerfile o con -e al ejecutar "docker run".
 */
public class Configuracion {

    private final int puerto;
    private final String carpetaDatos;
    private final double tasaDefecto;
    private final String llaveApiTasa;
    private final String adminUsername;
    private final String adminPassword;

    /** Constructor privado: la configuración se crea con desdeEntorno(). */
    private Configuracion(int puerto, String carpetaDatos, double tasaDefecto,
                          String llaveApiTasa, String adminUsername, String adminPassword) {
        this.puerto = puerto;
        this.carpetaDatos = carpetaDatos;
        this.tasaDefecto = tasaDefecto;
        this.llaveApiTasa = llaveApiTasa;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    /** Arma la configuración leyendo cada variable de entorno (o su valor por defecto). */
    public static Configuracion desdeEntorno() {
        return new Configuracion(
                leerEntero("PORT", 4567),
                leerTexto("DATA_DIR", "data"),
                leerDecimal("TASA_DEFECTO", 4000.0),
                leerTexto("EXCHANGE_API_KEY", ""),
                leerTexto("ADMIN_USERNAME", "admin"),
                leerTexto("ADMIN_PASSWORD", "admin123"));
    }

    private static String leerTexto(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        return valor.trim();
    }

    private static int leerEntero(String nombre, int porDefecto) {
        try {
            return Integer.parseInt(leerTexto(nombre, String.valueOf(porDefecto)));
        } catch (NumberFormatException error) {
            System.out.println("La variable " + nombre + " no es un número entero; se usa " + porDefecto);
            return porDefecto;
        }
    }

    private static double leerDecimal(String nombre, double porDefecto) {
        try {
            return Double.parseDouble(leerTexto(nombre, String.valueOf(porDefecto)));
        } catch (NumberFormatException error) {
            System.out.println("La variable " + nombre + " no es un número; se usa " + porDefecto);
            return porDefecto;
        }
    }

    public int getPuerto() {
        return puerto;
    }

    public String getCarpetaDatos() {
        return carpetaDatos;
    }

    public double getTasaDefecto() {
        return tasaDefecto;
    }

    public String getLlaveApiTasa() {
        return llaveApiTasa;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
}
