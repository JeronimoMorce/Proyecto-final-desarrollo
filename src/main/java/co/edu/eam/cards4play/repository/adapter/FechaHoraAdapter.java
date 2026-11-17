package co.edu.eam.cards4play.repository.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * Igual que FechaAdapter, pero para fecha con hora (LocalDateTime).
 * Ejemplo de texto guardado: "2026-11-13T09:51:30".
 */
public class FechaHoraAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    @Override
    public JsonElement serialize(LocalDateTime fechaHora, Type tipo, JsonSerializationContext contexto) {
        return new JsonPrimitive(fechaHora.toString());
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type tipo, JsonDeserializationContext contexto) {
        return LocalDateTime.parse(json.getAsString());
    }
}
