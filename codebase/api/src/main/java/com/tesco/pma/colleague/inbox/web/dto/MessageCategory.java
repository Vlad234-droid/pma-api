package com.tesco.pma.colleague.inbox.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Colleague Inbox API
 * ${COLLEAGUE_INBOX_API_URL}/v1/messages
 * Category for a message to send to a colleague by Colleague Inbox
 */
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
