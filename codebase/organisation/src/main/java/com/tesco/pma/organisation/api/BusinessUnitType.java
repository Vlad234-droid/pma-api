package com.tesco.pma.organisation.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TODO Replace by universal dictionary item
@RequiredArgsConstructor
@Getter
public enum BusinessUnitType implements DictionaryItem<Integer> {
    HEAD_OFFICE(1, "Head office business unit type"),
    SUBSIDIARY(2, "Subsidiary business unit type"),
    DEPARTMENT(3, "Department business unit type");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
