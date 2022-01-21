package com.tesco.pma.api;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum ValueType implements DictionaryItem<Integer> {

    BLANK(1, "Value is blank or empty"),
    ERROR(2, "Formula returns error"),
    STRING(3, "String value type"),
    BOOLEAN(4, "Boolean value type"),
    NUMBER(5, "Number value type"),
    DATE(6, "Date value type"),
    INTEGER(7, "Integer value type"),
    DECIMAL(8, "Decimal value type");

    private final Integer id;
    private final String description;

    private static final Map<Integer, ValueType> BY_ID = new HashMap<>();

    static {
        for (ValueType e : ValueType.values()) {
            if (BY_ID.put(e.getId(), e) != null) {
                throw new IllegalArgumentException("duplicate id: " + e.getId());
            }
        }
    }

    ValueType(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }

    public static ValueType getById(Integer id) {
        return BY_ID.get(id);
    }
}
