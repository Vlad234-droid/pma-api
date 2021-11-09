package com.tesco.pma.fs.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

/**
 * File types
 */
@Getter
public enum FileType implements DictionaryItem<Integer> {

    BPMN(1, "File type is BPMN"),
    FORM(2, "File type is FORM"),
    PDF(3, "File type is PDF"),
    PPT(4, "File type is PPT"),
    XLS(5, "File type is XLS");

    private final Integer id;
    private final String description;

    FileType(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }
}