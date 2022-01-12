package com.tesco.pma.fs.api;

import com.tesco.pma.api.DictionaryItem;
import com.tesco.pma.api.GeneralDictionaryItem;
import lombok.Getter;

/**
 * File types
 */
public class FileType extends GeneralDictionaryItem {

    private static final long serialVersionUID = 7180587031625827206L;

    @Getter
    public enum FileTypeEnum implements DictionaryItem<Integer> {

        BPMN(1, "Business Process Model file"),
        FORM(2, "GUI Form file"),
        PDF(3, "Portable document format file"),
        PPT(4, "PowerPoint presentation file"),
        XLS(5, "Excel file"),
        DMN(6, "Decision Matrix file"),
        DOC(7, "Word document");

        private final Integer id;
        private final String description;

        FileTypeEnum(Integer id, String description) {
            this.id = id;
            this.description = description;
        }

        @Override
        public String getCode() {
            return name();
        }
    }
}