package com.tesco.pma.organization.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BusinessUnitType implements DictionaryItem<Integer> {
    DEPARTMENT(1, "Department business type");

    private final Integer id;
    private final String description;


    @Override
    public String getCode() {
        return name();
    }
}
