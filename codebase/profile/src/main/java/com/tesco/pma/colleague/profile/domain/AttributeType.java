package com.tesco.pma.colleague.profile.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

@Getter
public enum AttributeType implements DictionaryItem<Integer> {

    STRING(1, "String type of attribute"),
    DATE(2, "Date type of attribute"),
    NUMBER(3, "Number type of attribute"),
    BOOLEAN(4, "Boolean type of attribute");

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
