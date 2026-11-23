package co.edu.eam.cards4play.repository;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y lee listas de objetos en un archivo de texto con formato JSON.
 *
 * Es una clase GENÉRICA: la letra T representa "cualquier tipo". Una sola
 * clase sirve para productos, clientes y torneos sin repetir código:
 *
 *   new RepositorioJson<>("data/productos.json", Producto.class);
 *   new RepositorioJson<>("data/clientes.json", Cliente.class);
 *
 * En Docker la carpeta de datos es un VOLUMEN: los archivos sobreviven aunque
 * el contenedor se borre.
 */
public class RepositorioJson<T> {

    private final Path ruta;
    private final Type tipoLista; // le dice a Gson qué clase de objetos hay dentro de la lista
    private final Gson gson;

    public RepositorioJson(String rutaArchivo, Class<T> clase) {
        this.ruta = Paths.get(rutaArchivo);
        this.tipoLista = TypeToken.getParameterized(List.class, clase).getType(); // "List<clase>"
        this.gson = ConfiguracionGson.crearGson();
    }

    /**
     * Lee el archivo y devuelve la lista.
     *  - Si el archivo todavía no existe (primera ejecución) devuelve una lista vacía.
     *  - Si el archivo está dañado o tiene un formato viejo, lo renombra a ".bak"
     *    (para no perderlo) y empieza con una lista vacía.
     */
    public List<T> cargar() {
        if (!Files.exists(ruta)) {
            return new ArrayList<>();
        }
        try {
            String json = Files.readString(ruta, StandardCharsets.UTF_8);
            List<T> datos = gson.fromJson(json, tipoLista);
            return datos == null ? new ArrayList<>() : datos;
        } catch (JsonParseException | IllegalStateException | NullPointerException error) {
            respaldarArchivoDanado(error);
            return new ArrayList<>();
        } catch (IOException error) {
            throw new Cards4PlayException(500, "No se pudo leer el archivo " + ruta);
        }
    }

    /**
     * Escribe la lista completa en el archivo (reemplaza lo que había).
     *
     * Escritura segura: primero se escribe en un archivo temporal y después se
     * reemplaza el original. Si la app se apaga a la mitad, el archivo original no queda dañado.
     */
    public void guardar(List<T> datos) {
        try {
            if (ruta.getParent() != null) {
                Files.createDirectories(ruta.getParent()); // crea la carpeta de datos si no existe
            }
            Path temporal = Paths.get(ruta + ".tmp");
            Files.writeString(temporal, gson.toJson(datos, tipoLista), StandardCharsets.UTF_8);
            Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            throw new Cards4PlayException(500, "No se pudo guardar el archivo " + ruta);
        }
    }

    private void respaldarArchivoDanado(Exception error) {
        Path respaldo = Paths.get(ruta + ".bak");
        try {
            Files.move(ruta, respaldo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignorada) {
            // si ni siquiera se puede mover, se deja como está
        }
        System.out.println("AVISO: " + ruta + " no se pudo leer (" + error.getMessage()
                + "). Se guardó una copia en " + respaldo + " y se empieza sin esos datos.");
    }
}
