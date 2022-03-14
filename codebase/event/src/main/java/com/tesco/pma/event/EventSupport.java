package com.tesco.pma.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.event.SerdeUtils.EventProperties.CALLBACK_EVENT;
import static com.tesco.pma.event.SerdeUtils.EventProperties.CALLBACK_SERVICE_URL;
import static com.tesco.pma.event.SerdeUtils.EventProperties.CREATION_DATE;
import static com.tesco.pma.event.SerdeUtils.EventProperties.EVENT_ID;
import static com.tesco.pma.event.SerdeUtils.EventProperties.EVENT_NAME;
import static com.tesco.pma.event.SerdeUtils.EventProperties.EVENT_PRIORITY;

/**
 * Note: Each event is unique independently of business content and hashCode and equals MUST be default to provide object uniqueness.
 * Each child event MUST store new properties into the properties
 */
@JsonDeserialize(using = EventDeserializer.class)
@JsonSerialize(using = EventSerializer.class)
public class EventSupport implements Event {

    private static final long serialVersionUID = 3665859671824003271L;

    protected Map<String, Serializable> properties = new LinkedHashMap<>();

    public <E extends Enum<E>> EventSupport(E eventName) {
        this(eventName == null ? null : eventName.name());
    }

    public EventSupport(String eventName) {
        this(eventName, UUID.randomUUID().toString().replace("-", ""));
    }

    EventSupport(String eventName, String eventId) {
        this(eventName, eventId, new Date());
    }

    EventSupport(String eventName, String eventId, Date creationDate) {
        if (!isValidName(eventName)) { //NOPMD
            throw new IllegalArgumentException("Event name cannot be null or empty");
        }
        properties.put(EVENT_NAME.name(), eventName);
        properties.put(EVENT_ID.name(), eventId);
        properties.put(EVENT_PRIORITY.name(), EventPriority.getDefaultPriority());
        properties.put(CREATION_DATE.name(), creationDate);
    }

    public static EventSupport create(String eventName, Map<String, Serializable> eventParams) {
        var event = new EventSupport(eventName);
        eventParams.forEach(event::putProperty);
        return event;
    }

    /**
     * Stores a property if it is not null
     * 
     * @param name property name
     * @param value property's value
     * @return event
     * @throws IllegalArgumentException
     *             1. when you try to add a one from default properties: EVENT_NAME, EVENT_ID, EVENT_PRIORITY, CALLBACK_EVENT,
     *                  CALLBACK_SERVICE_URL. You need to use an appropriate set method
     *             2. the name is null or empty
     *             3. the value is null
     */
    public final EventSupport putProperty(String name, Serializable value) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Property name cannot be null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Property value cannot be null");
        }
        if (isDefaultProperty(name)) {
            throw new IllegalArgumentException("Please use an appropriate set method instead");
        }
        if (value instanceof Collection) {
            var collectionValue = (Collection<?>) value;
            if (collectionValue.stream()
                    .anyMatch(candidate -> SerdeUtils.SupportedTypes.getSupportedType(candidate).isEmpty())) {
                throw new IllegalArgumentException("Collection property contains non supported element types");
            }
        } else {
            if (SerdeUtils.SupportedTypes.getSupportedType(value).isEmpty()) {
                throw new IllegalArgumentException(String.format("Provided value type %s is not supported", value.getClass().getName()));
            }
        }
        properties.put(name, value);
        return this;
    }

    protected boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty(); //NOPMD
    }

    public static boolean isDefaultProperty(String name) {
        return name != null && SerdeUtils.EventProperties.find(name) != null;
    }

    @Override
    public Serializable getEventProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Map<String, Serializable> getEventProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Sets properties except default properties
     * @param properties event properties
     * @return event
     */
    public EventSupport setEventProperties(Map<String, Serializable> properties) {
        if (properties != null) {
            properties.entrySet().stream().filter(entry -> !isDefaultProperty(entry.getKey()))
                    .forEach(entry -> putProperty(entry.getKey(), entry.getValue()));
        }
        return this;
    }

    @Override
    public String getEventName() {
        return (String) getEventProperty(EVENT_NAME.name());
    }

    @Override
    public String getEventId() {
        return (String) getEventProperty(EVENT_ID.name());
    }

    @Override
    public Date getEventCreationDate() {
        return (Date) getEventProperty(CREATION_DATE.name());
    }

    @Override
    public Event getCallbackEvent() {
        return (Event) getEventProperty(CALLBACK_EVENT.name());
    }

    public EventSupport setCallbackEvent(Event callbackEvent) {
        if (callbackEvent != null) {
            properties.put(CALLBACK_EVENT.name(), callbackEvent);
        }
        return this;
    }

    @Override
    public String getCallbackServiceURL() {
        return (String) getEventProperty(CALLBACK_SERVICE_URL.name());
    }

    public EventSupport setCallbackServiceURL(String callbackServiceURL) {
        if (callbackServiceURL != null) {
            properties.put(CALLBACK_SERVICE_URL.name(), callbackServiceURL);
        }
        return this;
    }

    @Override
    public EventPriority getEventPriority() {
        return (EventPriority) getEventProperty(EVENT_PRIORITY.name());
    }

    /**
     * Sets an event priority or default NORMAL if a new priority is null
     * 
     * @param eventPriority a new event priority
     * @return event
     */
    public EventSupport setEventPriority(EventPriority eventPriority) {
        properties.put(EVENT_PRIORITY.name(), eventPriority == null ? EventPriority.getDefaultPriority() : eventPriority);
        return this;
    }

    @Override
    public String toString() {
        return "EventSupport" + properties;
    }
}
