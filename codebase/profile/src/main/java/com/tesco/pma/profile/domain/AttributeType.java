package com.tesco.pma.profile.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

@Getter
public enum AttributeType implements DictionaryItem {

    STRING(1, "String type of attribute"),
    DATE(2, "Date type of attribute");

    private final Integer id;
    private final String description;

    AttributeType(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }

}
