package com.tesco.pma.event;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Supports events deserializing
 */
public class EventDeserializer extends StdDeserializer<Event> {

    private static final long serialVersionUID = 7047795002792356540L;

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
        for (Iterator<Map.Entry<String, JsonNode>> i = node.fields(); i.hasNext(); ) { //NOPMD
            Map.Entry<String, JsonNode> entry = i.next();
            if (!EventSupport.isDefaultProperty(entry.getKey())) {
                JsonNode value = entry.getValue();
                if (value.isArray()) {
                    result.put(entry.getKey(), readCollection(value));
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

    private ArrayList<Serializable> readCollection(JsonNode value) throws IOException { //NOPMD
        var list = new ArrayList<Serializable>();
        for (JsonNode childNode : value) {
            var readValue = readValue(childNode);
            if (readValue != null) {
                list.add(readValue);
            }
        }
        return list;
    }

    private Serializable readValue(JsonNode node) throws IOException {
        JsonNode classNode = node.get(SerdeUtils.OBJECT_CLASS_FIELD);
        if (classNode == null) {
            return null;
        }
        var supportedType = SerdeUtils.SupportedTypes.getSupportedType(classNode.textValue());
        if (supportedType.isEmpty()) {
            throw new IOException(String.format("Class %s is unsupported therefore value could not be deserialized",
                    classNode.textValue()));
        }
        var reader = getValueReader(supportedType.get());
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
            case ARRAY:
                return node -> readCollection(node.get(SerdeUtils.OBJECT_VALUE_FIELD));
            case DATE:
                return node -> {
                    try {
                        return SerdeUtils.getDateFormatter().parse(node.get(SerdeUtils.OBJECT_VALUE_FIELD).textValue());
                    } catch (Exception e) {
                        throw new IOException(e);
                    }
                };
            case UUID:
                return node -> UUID.fromString(node.get(SerdeUtils.OBJECT_VALUE_FIELD).textValue());
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
                    try {
                        return node.get(SerdeUtils.OBJECT_VALUE_FIELD).toString();
                    } catch (Exception e) {
                        throw new IOException("Event cannot be instantiated", e);
                    }
                };
        }
    }
}
