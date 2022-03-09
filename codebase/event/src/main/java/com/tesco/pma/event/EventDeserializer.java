package com.tesco.pma.event;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Supports events deserializing
 */
public class EventDeserializer extends StdDeserializer<Event> {

    private static final long serialVersionUID = 7047795002792356540L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    interface ValueReader {

        Serializable read(JsonNode node) throws IOException;
    }

    public EventDeserializer() {
        super(Event.class);
    }

    @Override
    public Event deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        return readEvent(node);
    }

    private Event readEvent(JsonNode node) throws IOException {
        if (node == null) {
            return null;
        }
        JsonNode classNode = node.get(SerdeUtils.OBJECT_CLASS_FIELD);
        if (classNode == null) {
            return new ExternalEventReader().read(node);
        }
        return (Event) getValueReader(SerdeUtils.SupportedTypes.EVENT).read(node);
    }

    private Map<String, Serializable> readProperties(JsonNode node) throws IOException {
        if (node == null) {
            return Collections.emptyMap();
        }

        Map<String, Serializable> result = new HashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> i = node.fields(); i.hasNext();) { //NOPMD
            Map.Entry<String, JsonNode> entry = i.next();
            if (!EventSupport.isDefaultProperty(entry.getKey())) {
                JsonNode value = entry.getValue();
                if (value.isArray()) {
                    readCollection(result, entry.getKey(), value);
                } else {
                    Serializable readValue = readValue(value);
                    if (readValue != null) {
                        result.put(entry.getKey(), readValue);
                    }
                }
            }
        }
        return result;
    }

    private void readCollection(Map<String, Serializable> result, String key, JsonNode value) throws IOException {
        ArrayList<Serializable> list = new ArrayList<>();
        for (JsonNode childNode : value) {
            Serializable readValue = readValue(childNode);
            if (readValue != null) {
                list.add(readValue);
            }
        }
        if (!list.isEmpty()) {
            result.put(key, list);
        }
    }

    private Serializable readValue(JsonNode node) throws IOException {
        JsonNode classNode = node.get(SerdeUtils.OBJECT_CLASS_FIELD);
        if (classNode == null) {
            return null;
        }
        Optional<SerdeUtils.SupportedTypes> supportedType = SerdeUtils.SupportedTypes.getSupportedType(classNode.textValue());
        if (!supportedType.isPresent()) {
            throw new IOException(String.format("Class %s is unsupported therefore value could not be deserialized",
                                                classNode.textValue()));
        }
        ValueReader reader = getValueReader(supportedType.get());
        return reader.read(node);
    }

    private ValueReader getValueReader(SerdeUtils.SupportedTypes supportedType) {
        switch (supportedType) {
            case STRING:
                return node -> node.get(SerdeUtils.OBJECT_VALUE_FIELD).textValue();
            case INT:
                return node -> node.get(SerdeUtils.OBJECT_VALUE_FIELD).intValue();
            case LONG:
                return node -> node.get(SerdeUtils.OBJECT_VALUE_FIELD).longValue();
            case FLOAT:
                return node -> node.get(SerdeUtils.OBJECT_VALUE_FIELD).floatValue();
            case DOUBLE:
                return node -> node.get(SerdeUtils.OBJECT_VALUE_FIELD).doubleValue();
            case BIGINT:
                return node -> node.get(SerdeUtils.OBJECT_VALUE_FIELD).bigIntegerValue();
            case BIGDECIMAL:
                return node -> node.get(SerdeUtils.OBJECT_VALUE_FIELD).decimalValue();
            case DATE:
                return node -> {
                    try {
                        return SerdeUtils.getDateFormatter().parse(node.get(SerdeUtils.OBJECT_VALUE_FIELD).textValue());
                    } catch (Exception e) {
                        throw new IOException(e);
                    }
                };
            case EVENT:
                return node -> {
                    String eventName = node.get(SerdeUtils.EventProperties.EVENT_NAME.name()).asText();
                    try {
                        EventSupport event = new EventSupport(eventName, node.get(SerdeUtils.EventProperties.EVENT_ID.name()).asText());
                        event.setEventPriority(
                                EventPriority.getByName(node.get(SerdeUtils.EventProperties.EVENT_PRIORITY.name()).asText()));
                        JsonNode callbackServiceURLNode = node.get(SerdeUtils.EventProperties.CALLBACK_SERVICE_URL.name());
                        if (callbackServiceURLNode != null) {
                            event.setCallbackServiceURL(callbackServiceURLNode.asText());
                        }
                        event.setCallbackEvent(readEvent(node.get(SerdeUtils.EventProperties.CALLBACK_EVENT.name())));
                        event.setEventProperties(readProperties(node.get(SerdeUtils.EventProperties.PROPERTIES.name())));
                        return event;
                    } catch (Exception e) {
                        throw new IOException("Event cannot be instantiated", e);
                    }
                };
            default:
                return node -> {
                    String className = node.get(SerdeUtils.OBJECT_CLASS_FIELD).asText();
                    try {
                        return Serializable.class
                                .cast(OBJECT_MAPPER.treeToValue(node.get(SerdeUtils.OBJECT_VALUE_FIELD), Class.forName(className)));
                    } catch (Exception e) {
                        throw new IOException("Event cannot be instantiated", e);
                    }
                };
        }
    }
}
