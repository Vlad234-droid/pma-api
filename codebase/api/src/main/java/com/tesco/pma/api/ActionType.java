package com.tesco.pma.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ActionType implements DictionaryItem<Integer> {

    SAVE(1, "Save"),
    PUBLISH(2, "Publish"),
    UNPUBLISH(3, "Un-publish");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
