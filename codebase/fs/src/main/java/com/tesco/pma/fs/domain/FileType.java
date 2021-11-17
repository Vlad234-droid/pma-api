package com.tesco.pma.fs.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

/**
 * File types
 */
@Getter
public enum FileType implements DictionaryItem<Integer> {

    BPMN(1, "Business Process Model file"),
    FORM(2, "GUI Form file"),
    PDF(3, "Portable document format file"),
    PPT(4, "PowerPoint presentation file"),
    XLS(5, "Excel file");

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