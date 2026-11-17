package co.edu.eam.cards4play.repository.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;

/**
 * Le enseña a Gson a guardar una fecha (LocalDate) como texto, por ejemplo "2026-11-20",
 * y a convertir ese texto de nuevo en fecha al leer.
 */
public class FechaAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    @Override
    public JsonElement serialize(LocalDate fecha, Type tipo, JsonSerializationContext contexto) {
        return new JsonPrimitive(fecha.toString());
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type tipo, JsonDeserializationContext contexto) {
        return LocalDate.parse(json.getAsString());
    }
}
