package com.tesco.pma.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@JsonDeserialize(using = EventDeserializer.class)
@JsonSerialize(using = EventSerializer.class)
public interface Event extends Serializable {

    String getEventName();

    String getEventId();

    EventPriority getEventPriority();

    Date getEventCreationDate();

    Event getCallbackEvent();

    String getCallbackServiceURL();

    Serializable getEventProperty(String name);

    Map<String, Serializable> getEventProperties();
}
