package com.tesco.pma.event;

public interface EventListener {

    void handle(Event event) throws Exception;
}
