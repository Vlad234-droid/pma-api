package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

@Getter
public enum AccountStatus implements DictionaryItem<Integer> {

    ENABLED(1, "enabled", "Account is enabled"),
    DISABLED(2, "disabled", "Account is disabled");

    private final Integer id;
    private final String code;
    private final String description;

    AccountStatus(Integer id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
