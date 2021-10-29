package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

@Getter
public enum AccountStatus implements DictionaryItem<Integer> {

    ENABLED(1, "enabled"),
    DISABLED(2, "disabled");

    private final Integer id;
    private final String description;

    AccountStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }

    @JsonValue
    @Override
    public String getDescription() {
        return description;
    }

}
