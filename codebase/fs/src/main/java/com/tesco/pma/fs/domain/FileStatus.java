package com.tesco.pma.fs.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

/**
 * File statuses
 */
@Getter
public enum FileStatus implements DictionaryItem<Integer> {

    DRAFT(1, "File is in DRAFT status"),
    ACTIVE(2, "File is in ACTIVE status"),
    INACTIVE(3, "File is in INACTIVE status");

    private final Integer id;
    private final String description;

    FileStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }
}