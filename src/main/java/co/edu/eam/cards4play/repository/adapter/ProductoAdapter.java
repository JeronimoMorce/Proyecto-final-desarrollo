package co.edu.eam.cards4play.repository.adapter;

import co.edu.eam.cards4play.model.Accesorio;
import co.edu.eam.cards4play.model.Booster;
import co.edu.eam.cards4play.model.Carta;
import co.edu.eam.cards4play.model.Producto;
import co.edu.eam.cards4play.model.ProductoSellado;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Le enseña a Gson a guardar y leer objetos de tipo Producto.
 *
 * Problema: Producto es abstracta. Al leer el archivo Gson no sabe si debe
 * crear una Carta, un Booster, un ProductoSellado o un Accesorio.
 *
 * Solución:
 *  - Al GUARDAR se agrega un campo extra "tipo" con el nombre de la clase real.
 *  - Al LEER se mira ese campo y se crea el objeto de la clase correcta.
 *
 * Ejemplo de lo que queda en el archivo:
 *   { "rareza": "UR", "id": "CAR-1a2b3c4d", "nombre": "Dragón", "precioUsd": 25.0, "stock": 2, "tipo": "Carta" }
 */
public class ProductoAdapter implements JsonSerializer<Producto>, JsonDeserializer<Producto> {

    /** Objeto Java -> JSON */
    @Override
    public JsonElement serialize(Producto producto, Type tipo, JsonSerializationContext contexto) {
        // Se convierte usando la clase real del objeto (Carta, Booster...) para no perder sus atributos
        JsonObject objeto = contexto.serialize(producto, producto.getClass()).getAsJsonObject();
        objeto.addProperty("tipo", producto.getClass().getSimpleName());
        return objeto;
    }

    /** JSON -> objeto Java */
    @Override
    public Producto deserialize(JsonElement json, Type tipo, JsonDeserializationContext contexto) {
        JsonObject objeto = json.getAsJsonObject();
        String nombreTipo = objeto.get("tipo").getAsString();

        switch (nombreTipo) {
            case "Carta":
                return contexto.deserialize(objeto, Carta.class);
            case "Booster":
                return contexto.deserialize(objeto, Booster.class);
            case "ProductoSellado":
                return contexto.deserialize(objeto, ProductoSellado.class);
            case "Accesorio":
                return contexto.deserialize(objeto, Accesorio.class);
            default:
                throw new JsonParseException("Tipo de producto desconocido: " + nombreTipo);
        }
    }
}
