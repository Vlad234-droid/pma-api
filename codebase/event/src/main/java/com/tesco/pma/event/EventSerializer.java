package com.tesco.pma.event;

import static com.tesco.pma.event.SerdeUtils.EventProperties.CALLBACK_EVENT;
import static com.tesco.pma.event.SerdeUtils.EventProperties.CALLBACK_SERVICE_URL;
import static com.tesco.pma.event.SerdeUtils.EventProperties.EVENT_ID;
import static com.tesco.pma.event.SerdeUtils.EventProperties.EVENT_NAME;
import static com.tesco.pma.event.SerdeUtils.EventProperties.EVENT_PRIORITY;
import static com.tesco.pma.event.SerdeUtils.EventProperties.PROPERTIES;
import static com.tesco.pma.event.SerdeUtils.OBJECT_CLASS_FIELD;
import static com.tesco.pma.event.SerdeUtils.OBJECT_VALUE_FIELD;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import com.tesco.pma.event.SerdeUtils.EventProperties;
import com.tesco.pma.event.SerdeUtils.SupportedTypes;

public class EventSerializer extends StdSerializer<Event> {

    private static final long serialVersionUID = 6851588408504162084L;

    private interface ValueWriter {

        void write(JsonGenerator gen, Serializable value) throws IOException;
    }

    public EventSerializer() {
        super(Event.class);
    }

    @Override
    public void serialize(Event event, JsonGenerator gen, SerializerProvider provider) throws IOException {
        ValueWriter valueWriter = getValueWriter(SupportedTypes.EVENT);
        writeObject(valueWriter, event, gen);
    }

    private void writeProperties(String fieldName, Map<String, Serializable> properties, JsonGenerator gen) throws IOException {
        Set<String> keysToSerialize =
                properties.keySet().stream().filter(key -> EventProperties.find(key) == null).collect(Collectors.toSet());
        if (keysToSerialize.isEmpty()) {
            return;
        }
        gen.writeObjectFieldStart(fieldName);
        for (String key : keysToSerialize) {
            Serializable value = properties.get(key);
            if (value instanceof Collection) {
                writeCollection(key, (Collection<?>) value, gen);
            } else {
                Optional<SupportedTypes> supportedType = SupportedTypes.getSupportedType(value);
                if (supportedType.isPresent()) {
                    writeObject(getValueWriter(supportedType.get()), value, key, gen);
                }
            }
        }
        gen.writeEndObject();
    }

    private void writeCollection(String fieldName, Collection<?> values, JsonGenerator gen) throws IOException {
        if (values.isEmpty()) {
            return;
        }
        gen.writeArrayFieldStart(fieldName);
        for (Object value : values) {
            Serializable serializableValue = (Serializable) value;
            Optional<SupportedTypes> supportedType = SupportedTypes.getSupportedType(serializableValue);
            if (supportedType.isPresent()) {
                writeObject(getValueWriter(supportedType.get()), serializableValue, gen);
            }
        }
        gen.writeEndArray();
    }

    private ValueWriter getValueWriter(SupportedTypes supportedType) {
        switch (supportedType) {
            case STRING:
                return (gen, value) -> gen.writeStringField(OBJECT_VALUE_FIELD, (String) value);
            case INT:
                return (gen, value) -> gen.writeNumberField(OBJECT_VALUE_FIELD, (Integer) value);
            case LONG:
                return (gen, value) -> gen.writeNumberField(OBJECT_VALUE_FIELD, (Long) value);
            case FLOAT:
                return (gen, value) -> gen.writeNumberField(OBJECT_VALUE_FIELD, (Float) value);
            case DOUBLE:
                return (gen, value) -> gen.writeNumberField(OBJECT_VALUE_FIELD, (Double) value);
            case BIGINT:
                return (gen, value) -> {
                    gen.writeFieldName(OBJECT_VALUE_FIELD);
                    gen.writeNumber((BigInteger) value);
                };
            case BIGDECIMAL:
                return (gen, value) -> gen.writeNumberField(OBJECT_VALUE_FIELD, (BigDecimal) value);
            case DATE:
                return (gen, value) -> gen.writeStringField(OBJECT_VALUE_FIELD, SerdeUtils.getDateFormatter().format((Date) value));
            case EVENT:
                return (gen, value) -> {
                    Event event = (Event) value;
                    if (event == null) {
                        return;
                    }
                    gen.writeStringField(EVENT_NAME.name(), event.getEventName());
                    gen.writeStringField(EVENT_ID.name(), event.getEventId());
                    gen.writeStringField(EVENT_PRIORITY.name(), event.getEventPriority().name());
                    if (event.getCallbackEvent() != null) {
                        gen.writeObjectField(CALLBACK_EVENT.name(), event.getCallbackEvent());
                    }
                    if (event.getCallbackServiceURL() != null) {
                        gen.writeStringField(CALLBACK_SERVICE_URL.name(), event.getCallbackServiceURL());
                    }
                    writeProperties(PROPERTIES.name(), event.getEventProperties(), gen);
                };
            default:
                return (gen, value) -> gen.writeObjectField(OBJECT_VALUE_FIELD, value);
        }
    }

    private void writeObject(ValueWriter valueWriter, Serializable value, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeStringField(OBJECT_CLASS_FIELD, value.getClass().getName());
        valueWriter.write(gen, value);
        gen.writeEndObject();
    }
    
    private void writeObject(ValueWriter valueWriter, Serializable value, String objectField, JsonGenerator gen) throws IOException {
        gen.writeObjectFieldStart(objectField);
        gen.writeStringField(OBJECT_CLASS_FIELD, value.getClass().getName());
        valueWriter.write(gen, value);
        gen.writeEndObject();
    }
}
