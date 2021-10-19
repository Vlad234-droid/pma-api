package com.tesco.pma.fs.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

/**
 * Process Template types
 */
@Getter
public enum ProcessTemplateType implements DictionaryItem<Integer> {

    BPMN(1, "Process template type is BPMN"),
    FORM(2, "Process template type is FORM");

    private final Integer id;
    private final String description;

    ProcessTemplateType(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }
}