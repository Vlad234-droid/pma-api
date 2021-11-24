package com.tesco.pma.dao.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tesco.pma.api.MapJson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapJsonbCombinedSerializer {

    public static class MapJsonbDeserializer extends JsonDeserializer<MapJson> {

        @Override
        public MapJson deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            var objectCodec = jsonParser.getCodec();
            final JsonNode listOrObjectNode = objectCodec.readTree(jsonParser);
            final Map<String, String> result = new HashMap<>();

            Iterator<String> fieldNames = listOrObjectNode.fieldNames();

            while (fieldNames.hasNext()) {
                String name = fieldNames.next();
                JsonNode value = listOrObjectNode.get(name);
                result.put(name, value.asText());
            }
            return new MapJson(result);
        }

        @Override
        public Class<MapJson> handledType() {
            return MapJson.class;
        }
    }

    public static class MapJsonbSerializer extends JsonSerializer<MapJson> {

        @Override
        public void serialize(MapJson value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            var reviewPropertyValues = value.getMapJson();
            for (var ci : reviewPropertyValues.keySet()) {
                gen.writeObjectField(ci, value.getMapJson().get(ci));
            }
            gen.writeEndObject();
        }

        @Override
        public Class<MapJson> handledType() {
            return MapJson.class;
        }
    }

    public SimpleModule getSimpleModule() {
        var module = new SimpleModule(MapJsonbSerializer.class.getSimpleName());
        module.addSerializer(new MapJsonbSerializer());
        module.addDeserializer(MapJson.class, new MapJsonbDeserializer());
        return module;
    }
}
