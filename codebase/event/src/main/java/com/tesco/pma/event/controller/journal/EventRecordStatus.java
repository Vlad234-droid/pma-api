package com.tesco.pma.event.controller.journal;

import com.tesco.pma.api.DictionaryItem;

public enum EventRecordStatus implements DictionaryItem<Integer> {

    REGISTERED(1, "Event is registered"),
    SENT(2, "Event is sent");

    private final int id;
    private final String description;

    EventRecordStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getDescription() {
        return description;
    }
}
