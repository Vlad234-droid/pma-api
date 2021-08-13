package com.tesco.pma.dao.utils;

import com.tesco.pma.api.DictionaryItem;

public class DictionaryItemTypeHandler<T extends Enum<T> & DictionaryItem<Integer>> extends AbstractIdentifiedEnumTypeHandler<T> {
    private final Class<T> enumClass;

    public DictionaryItemTypeHandler(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    protected Class<T> getEnumClass() {
        return enumClass;
    }
}
