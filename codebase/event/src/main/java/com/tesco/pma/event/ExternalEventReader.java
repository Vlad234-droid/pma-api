package com.tesco.pma.event;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reader that reads event from TN message format.
 *
 * <p>TN message format is JSON with two main objects: "headers" and "payload".
 *
 * <p>Event fields mapping:
 * <ul>
 *  <li>eventName - headers.operation</li>
 *  <li>eventId - headers.messageCorrelationId</li>
 *  <li>eventCreationDate - headers.createdDate.iso (see parsing dates to check supported date formats)</li>
 * </ul>
 * The only mandatory field is headers.operation.
 *
 * <p>All fields (including those used for event fields) are added to the eventProperties, property name is the path to the fields in json
 *  (separated by dots), value is the field value.
 *
 * <p>Example of property name: "payload.operationDetails.extractedFileName", value: "AAA234.txt"
 *
 * <p>Most of the fields are added to event properties as string values. Special cases are dates and collections.
 *
 *  <p>Date parsing. In case of structure like
 *  <pre>
 *  "payload" {
 *      "someDate": {
 *          "iso": "2019-05-15 12:01:59.443+0500",
 *          "unixMs": 1557921719872
 *      }
 *  }
 *  </pre>
 *  , e.g. object contains two fields, "iso and "unixMs" - reader tries to parse value of the "iso" field using format
 *  "yyyy-MM-dd[ ]['T']HH:mm:ss[.SSS][.SS][XXX][XX][X][O][OOOO][ ]". If it's parsed successfully - value with type java.util.Date will be
 *  set for parent field. So previous case value for property "payload.someDate" will be set as Date object. This doesn't change the regular
 *  behavior, so properties "payload.someDate.iso" and "payload.someDate.unixMs" will be added as usual, as text values.
 *
 * <p>Collections parsing. For collections - List object will be added as value. List will contain all values from collection, value nodes
 *  will be added as String values, objects will be added as LinkedHashMap with property names in the same notation as for all properties,
 *  but names will use object placed in collection as name root.
 *
 * <p>Examples.
 *
 * <p>1. For next JSON:
 *  <pre>
 *  "payload": {
 *      "indexes": ["file 1", "file 2"]
 *  }
 *  </pre>
 *  list property "payload.indexes" will be added. List will contain to String objects: "file 1" and "file 2"
 *
 *  <p>2. For next JSON:
 *  <pre>
 *  "payload": {
 *      "operationDetails": {
 *          "rejectionReasons": [
 *              {
 *                  "code": "FILE_NAME_VALIDATION_FAILED",
 *                  "description" {
 *                      "en": "File name don't match to configured pattern",
 *                      "sp": "El nombre del archivo no coincide con el patrón configurado"
 *                  }
 *              }
 *          ]
 *      }
 *  }
 *  </pre>
 *  list property "payload.operationDetails.rejectionReasons" will be added. List will contain one object as value: LinkedHashMap with
 *  three entries:
 *  <ul>
 *  <li>"code": "FILE_NAME_VALIDATION_FAILED"</li>
 *  <li>"description.en": "File name don't match to configured pattern"</li>
 *  <li>"description.sp": "El nombre del archivo no coincide con el patrón configurado"</li>
 *  </ul>
 */
public class ExternalEventReader implements EventDeserializer.ValueReader {

    @Override
    public Event read(JsonNode node) throws IOException {
        JsonNode headers = node.path("headers");
        JsonNode operation = headers.path("operation");
        if (operation.isMissingNode()) {
            return null;
        }

        JsonNode messageCorrelationId = headers.path("messageCorrelationId");
        JsonNode createdDate = headers.path("createdDate").path("iso");
        Date createdDateParsed = parseDate(createdDate);

        EventSupport event = new EventSupport(operation.asText(), messageCorrelationId.asText(), createdDateParsed);

        Map<String, Serializable> props = new LinkedHashMap<>();
        jsonToProperties(new ArrayList<>(), node, props);
        event.setEventProperties(props);
        return event;
    }

    private void jsonToProperties(List<String> path, JsonNode node, Map<String, Serializable> properties) {
        if (node.isValueNode()) {
            processValueNode(path, node, properties);
        } else if (node.isArray()) {
            processArrayNode(path, node, properties);
        } else if (node.isObject()) {
            processObjectNode(path, node, properties);
        }
    }

    private void processValueNode(List<String> path, JsonNode node, Map<String, Serializable> properties) {
        properties.put(pathToString(path), node.asText());
    }

    private void processArrayNode(List<String> path, JsonNode node, Map<String, Serializable> properties) {
        ArrayList<Serializable> values = new ArrayList<>();
        node.forEach(child -> {
            if (child.isValueNode()) {
                values.add(child.asText());
            } else {
                LinkedHashMap<String, Serializable> childProperties = new LinkedHashMap<>();
                jsonToProperties(new ArrayList<>(), child, childProperties);
                values.add(childProperties);
            }
        });
        properties.put(pathToString(path), values);
    }

    private void processObjectNode(List<String> path, JsonNode node, Map<String, Serializable> properties) {
        processIfDate(path, node, properties);
        path.add(null);
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = node.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = fieldsIterator.next();
            path.set(path.size() - 1, entry.getKey());
            jsonToProperties(path, entry.getValue(), properties);
        }
        path.remove(path.size() - 1);
    }

    private void processIfDate(List<String> path, JsonNode node, Map<String, Serializable> properties) {
        JsonNode iso = node.path("iso");
        if (iso.isValueNode()) { // assume it's a date structure like headers.createdDate.iso
            Date date = parseDate(iso);
            if (date != null) {
                properties.put(pathToString(path), date);
            }
        }
    }

    private Date parseDate(JsonNode node) {
        Date value = null;
        try {
            String textPatched = node.asText() + " "; // see comments on SerdeUtils.looseDateTimeFormatter
            ZonedDateTime zdt = ZonedDateTime.parse(textPatched, SerdeUtils.LOOSE_DATE_TIME_FORMATTER);
            value = Date.from(zdt.toInstant());
        } catch (Exception e) {
            // Ignore
        }
        return value;
    }

    private String pathToString(List<String> path) {
        return path.stream().map(chunk -> chunk.contains(".") ? "[" + chunk + "]" : chunk)
                   .collect(Collectors.joining("."));
    }
}
