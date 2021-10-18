package com.tesco.pma.objective.dao.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tesco.pma.objective.domain.ReviewProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReviewPropertiesJsonbCombinedSerializer {

    public static class ReviewPropertiesJsonbDeserializer extends JsonDeserializer<ReviewProperties> {

        @Override
        public ReviewProperties deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            var objectCodec = jsonParser.getCodec();
            final JsonNode listOrObjectNode = objectCodec.readTree(jsonParser);
            final Map<String, String> result = new HashMap<>();

            Iterator<String> fieldNames = listOrObjectNode.fieldNames();

            while (fieldNames.hasNext()) {
                String name = fieldNames.next();
                JsonNode value = listOrObjectNode.get(name);
                result.put(name, value.asText());
            }
            return new ReviewProperties(result);
        }

        @Override
        public Class<ReviewProperties> handledType() {
            return ReviewProperties.class;
        }
    }

    public static class ReviewPropertiesJsonbSerializer extends JsonSerializer<ReviewProperties> {

        @Override
        public void serialize(ReviewProperties value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            var reviewPropertyValues = value.getReviewProperties();
            for (var ci : reviewPropertyValues.keySet()) {
                gen.writeObjectField(ci, value.getReviewProperties().get(ci));
            }
            gen.writeEndObject();
        }

        @Override
        public Class<ReviewProperties> handledType() {
            return ReviewProperties.class;
        }
    }

    public SimpleModule getSimpleModule() {
        var module = new SimpleModule(ReviewPropertiesJsonbSerializer.class.getSimpleName());
        module.addSerializer(new ReviewPropertiesJsonbCombinedSerializer.ReviewPropertiesJsonbSerializer());
        module.addDeserializer(ReviewProperties.class, new ReviewPropertiesJsonbCombinedSerializer.ReviewPropertiesJsonbDeserializer());
        return module;
    }
}
