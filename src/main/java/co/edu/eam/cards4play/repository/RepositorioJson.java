package co.edu.eam.cards4play.repository;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y lee listas de objetos en un archivo de texto con formato JSON.
 *
 * Es una clase GENÉRICA: la letra T representa "cualquier tipo". Gracias a eso
 * una sola clase sirve para todo, sin repetir código:
 *
 *   RepositorioJson<Producto> productos = new RepositorioJson<>("data/productos.json", Producto.class);
 *   RepositorioJson<Cliente>  clientes  = new RepositorioJson<>("data/clientes.json", Cliente.class);
 *
 * En Docker la carpeta "data" será un volumen, para que los archivos no se
 * pierdan al borrar el contenedor.
 */
public class RepositorioJson<T> {

    private final String rutaArchivo;
    private final Type tipoLista; // le dice a Gson qué clase de objetos hay dentro de la lista
    private final Gson gson;

    public RepositorioJson(String rutaArchivo, Class<T> clase) {
        this.rutaArchivo = rutaArchivo;
        this.tipoLista = TypeToken.getParameterized(List.class, clase).getType(); // "List<clase>"
        this.gson = ConfiguracionGson.crearGson();
    }

    /** Lee el archivo y devuelve la lista. Si el archivo todavía no existe devuelve una lista vacía. */
    public List<T> cargar() {
        Path ruta = Paths.get(rutaArchivo);
        if (!Files.exists(ruta)) {
            return new ArrayList<>();
        }
        try {
            String json = Files.readString(ruta);
            List<T> datos = gson.fromJson(json, tipoLista);
            if (datos == null) { // archivo vacío
                return new ArrayList<>();
            }
            return datos;
        } catch (IOException error) {
            throw new Cards4PlayException(500, "No se pudo leer el archivo " + rutaArchivo);
        }
    }

    /** Escribe la lista completa en el archivo (reemplaza lo que había). */
    public void guardar(List<T> datos) {
        Path ruta = Paths.get(rutaArchivo);
        try {
            if (ruta.getParent() != null) {
                Files.createDirectories(ruta.getParent()); // crea la carpeta "data" si no existe
            }
            String json = gson.toJson(datos, tipoLista);
            Files.writeString(ruta, json);
        } catch (IOException error) {
            throw new Cards4PlayException(500, "No se pudo guardar el archivo " + rutaArchivo);
        }
    }
}
