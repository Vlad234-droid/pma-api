package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

@Getter
public enum AccountType implements DictionaryItem<Integer> {

    USER(1, "user", "User account type");

    private final Integer id;
    private final String code;
    private final String description;

    AccountType(Integer id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

}
