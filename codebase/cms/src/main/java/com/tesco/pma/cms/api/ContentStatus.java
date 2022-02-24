package com.tesco.pma.cms.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentStatus implements DictionaryItem<Integer> {

    DRAFT(1, "Draft"),
    PUBLISHED(2, "Published"),
    UNPUBLISHED(3, "Unpublished");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
