package com.tesco.pma.event;

import java.io.Serializable;

public interface EventResponse extends Serializable {

    String END = "end";

    String getEventName();

    String getResponseName();

    Object getSource();
}
