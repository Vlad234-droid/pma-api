package com.tesco.pma.pdp.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

/**
 * PDP Goal statuses
 */
@Getter
public enum PDPGoalStatus implements DictionaryItem<Integer> {

    DRAFT(1, "PDP goal is saved as draft"),
    PUBLISHED(2, "PDP goal was published"),
    UNPUBLISHED(3, "PDP goal was unpublished");

    private final Integer id;
    private final String description;

    PDPGoalStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }
}
