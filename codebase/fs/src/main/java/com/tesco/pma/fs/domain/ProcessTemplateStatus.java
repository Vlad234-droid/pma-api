package com.tesco.pma.fs.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

/**
 * Process Template statuses
 */
@Getter
public enum ProcessTemplateStatus implements DictionaryItem<Integer> {

    DRAFT(1, "Process template is in DRAFT status"),
    ACTIVE(2, "Process template is in ACTIVE status"),
    INACTIVE(3, "Process template is in INACTIVE status");

    private final Integer id;
    private final String description;

    ProcessTemplateStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }
}