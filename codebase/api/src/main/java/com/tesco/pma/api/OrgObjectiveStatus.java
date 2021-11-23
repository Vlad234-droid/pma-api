package com.tesco.pma.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OrgObjectiveStatus implements DictionaryItem<Integer> {

    DRAFT(1, "Organisation objective is saved as draft"),
    PUBLISHED(2, "Organisation objective was published"),
    UNPUBLISHED(3, "Organisation objective was unpublished");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
