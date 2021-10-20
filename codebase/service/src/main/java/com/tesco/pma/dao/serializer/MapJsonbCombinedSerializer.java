package com.tesco.pma.dao.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tesco.pma.api.MapProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapJsonbCombinedSerializer {

    public static class ReviewPropertiesJsonbDeserializer extends JsonDeserializer<MapProperties> {

        @Override
        public MapProperties deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            var objectCodec = jsonParser.getCodec();
            final JsonNode listOrObjectNode = objectCodec.readTree(jsonParser);
            final Map<String, String> result = new HashMap<>();

            Iterator<String> fieldNames = listOrObjectNode.fieldNames();

            while (fieldNames.hasNext()) {
                String name = fieldNames.next();
                JsonNode value = listOrObjectNode.get(name);
                result.put(name, value.asText());
            }
            return new MapProperties(result);
        }

        @Override
        public Class<MapProperties> handledType() {
            return MapProperties.class;
        }
    }

    public static class ReviewPropertiesJsonbSerializer extends JsonSerializer<MapProperties> {

        @Override
        public void serialize(MapProperties value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            var reviewPropertyValues = value.getMapProperties();
            for (var ci : reviewPropertyValues.keySet()) {
                gen.writeObjectField(ci, value.getMapProperties().get(ci));
            }
            gen.writeEndObject();
        }

        @Override
        public Class<MapProperties> handledType() {
            return MapProperties.class;
        }
    }

    public SimpleModule getSimpleModule() {
        var module = new SimpleModule(ReviewPropertiesJsonbSerializer.class.getSimpleName());
        module.addSerializer(new MapJsonbCombinedSerializer.ReviewPropertiesJsonbSerializer());
        module.addDeserializer(MapProperties.class, new MapJsonbCombinedSerializer.ReviewPropertiesJsonbDeserializer());
        return module;
    }
}
