package com.tesco.pma.cycle.dao.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.cycle.api.model.PMTimelinePointElement;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PMCycleJsonbCombinedSerializer {
    private static final String ID = "id";
    private static final String CODE = "code";
    private static final String DESCRIPTION = "description";
    private static final String TYPE = "type";
    private static final String CYCLE_TYPE = "cycleType";
    private static final String REVIEW_TYPE = "reviewType";
    private static final String CYCLE = "cycle";
    private static final String PROPERTIES = "properties";
    private static final String TIMELINEPOINTS = "timelinePoints";

    public static class PMCycleJsonbSerializer extends JsonSerializer<PMCycleMetadata> {

        @Override
        public void serialize(PMCycleMetadata value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeFieldName(CYCLE);
            gen.writeStartObject();
            PMCycleElement cycle = value.getCycle();
            gen.writeStringField(ID, cycle.getId());
            gen.writeStringField(CODE, cycle.getCode());
            gen.writeStringField(DESCRIPTION, cycle.getDescription());
            gen.writeStringField(TYPE, cycle.getType().name());
            gen.writeStringField(CYCLE_TYPE, cycle.getCycleType().name());

            gen.writeFieldName(PROPERTIES);
            gen.writeStartObject();
            var cyclePropertyValues = cycle.getProperties();
            for (var kv : cyclePropertyValues.entrySet()) {
                gen.writeObjectField(kv.getKey(), kv.getValue());
            }
            gen.writeEndObject();

            gen.writeFieldName(TIMELINEPOINTS);
            gen.writeStartArray();

            var timelinePoints = cycle.getTimelinePoints();
            for (PMTimelinePointElement tlp : timelinePoints) {
                gen.writeObject(tlp);
            }
            gen.writeEndArray();

            gen.writeEndObject();
            gen.writeEndObject();
        }

        @Override
        public Class<PMCycleMetadata> handledType() {
            return PMCycleMetadata.class;
        }
    }

    public static class PMCycleJsonbDeserializer extends JsonDeserializer<PMCycleMetadata> {

        @Override
        public PMCycleMetadata deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = jp.getCodec();
            JsonNode node = codec.readTree(jp);
            JsonNode cycle = node.get(CYCLE);

            final var result = new PMCycleMetadata();
            var element = new PMCycleElement();

            element.setId(cycle.get(ID).asText());
            element.setCode(cycle.get(CODE).asText());
            element.setDescription(cycle.get(DESCRIPTION).asText());
            element.setCycleType(PMCycleType.valueOf(cycle.get(CYCLE_TYPE).asText()));
            element.setType(PMElementType.valueOf(cycle.get(TYPE).asText()));

            var listOrObjectNodeProps = cycle.get(PROPERTIES);
            final Map<String, String> props = new HashMap<>();
            var fieldNames = listOrObjectNodeProps.fieldNames();
            while (fieldNames.hasNext()) {
                String name = fieldNames.next();
                JsonNode value = listOrObjectNodeProps.get(name);
                props.put(name, value.asText());
            }

            element.setProperties(props);

            var listOrObjectNodeTLP = cycle.get(TIMELINEPOINTS);
            final List<PMTimelinePointElement> pmReviewElements = new ArrayList<>();
            if (listOrObjectNodeTLP.isArray()) {
                for (JsonNode cls : listOrObjectNodeTLP) {
                    pmReviewElements.add(codec.treeToValue(cls, PMTimelinePointElement.class));
                }
            } else {
                pmReviewElements.add(codec.treeToValue(listOrObjectNodeTLP, PMTimelinePointElement.class));
            }

            element.setTimelinePoints(pmReviewElements);
            result.setCycle(element);

            return result;
        }

        @Override
        public Class<PMCycleMetadata> handledType() {
            return PMCycleMetadata.class;
        }
    }

    public SimpleModule getSimpleModule() {
        var module = new SimpleModule(PMCycleJsonbSerializer.class.getSimpleName());
        module.addSerializer(new PMCycleJsonbSerializer());
        module.addDeserializer(PMCycleMetadata.class, new PMCycleJsonbDeserializer());
        return module;
    }

}
