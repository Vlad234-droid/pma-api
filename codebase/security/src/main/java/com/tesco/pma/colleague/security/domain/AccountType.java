package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

@Getter
public enum AccountType implements DictionaryItem<Integer> {

    USER(1, "User");

    private final Integer id;
    private final String description;

    AccountType(Integer id, String description) {
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
