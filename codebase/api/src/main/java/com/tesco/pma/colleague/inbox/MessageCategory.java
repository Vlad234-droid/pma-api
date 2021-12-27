package com.tesco.pma.colleague.inbox;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageCategory {
    OWN( 1, "own"),
    TEAM(2, "team");

    private final int categoryId;
    private final String value;

    MessageCategory(int categoryId, String value) {
        this.categoryId = categoryId;
        this.value = value;
    }

    @JsonCreator
    public static MessageCategory fromValue(String value) {
        for (MessageCategory action : MessageCategory.values()) {
            if (action.value.equals(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public Integer getCategoryId() {
        return categoryId;
    }
}
