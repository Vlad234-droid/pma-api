package com.tesco.pma.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupObjectiveStatus implements DictionaryItem<Integer> {

    DRAFT(1, "Group objective is saved as draft"),
    PUBLISHED(2, "Group objective was published"),
    UNPUBLISHED(3, "Group objective was unpublished");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
