package co.edu.eam.cards4play.repository;

import co.edu.eam.cards4play.model.Producto;
import co.edu.eam.cards4play.repository.adapter.FechaAdapter;
import co.edu.eam.cards4play.repository.adapter.FechaHoraAdapter;
import co.edu.eam.cards4play.repository.adapter.ProductoAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Crea el objeto Gson con la configuración que necesita el proyecto.
 *
 * Gson sabe convertir solo los tipos sencillos (String, int, double, listas...).
 * Para tres tipos hay que "enseñarle" con adaptadores:
 *  - Producto:      es abstracta; hay que decirle si cada producto es Carta, Booster, etc.
 *  - LocalDate:     fecha del torneo.
 *  - LocalDateTime: fecha y hora de la compra.
 */
public final class ConfiguracionGson {

    private ConfiguracionGson() {
    }

    public static Gson crearGson() {
        return new GsonBuilder()
                .setPrettyPrinting() // JSON con saltos de línea, fácil de leer
                .registerTypeAdapter(Producto.class, new ProductoAdapter())
                .registerTypeAdapter(LocalDate.class, new FechaAdapter())
                .registerTypeAdapter(LocalDateTime.class, new FechaHoraAdapter())
                .create();
    }
}
