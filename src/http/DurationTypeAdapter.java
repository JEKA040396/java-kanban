package http;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Duration;

public class DurationTypeAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

    @Override
    public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
        // Сериализуем Duration как количество минут (или секунд)
        return new JsonPrimitive(src.toMinutes());
    }

    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // Десериализуем число как минуты
        long minutes = json.getAsLong();
        return Duration.ofMinutes(minutes);
    }
}