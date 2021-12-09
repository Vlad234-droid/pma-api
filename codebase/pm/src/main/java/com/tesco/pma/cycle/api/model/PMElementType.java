package com.tesco.pma.cycle.api.model;

import com.tesco.pma.api.DictionaryItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-12 16:36
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PMElementType implements DictionaryItem<Integer> {
    ELEMENT(1, "Element"),
    TIMELINE_POINT(2, "Timeline point"),
    REVIEW(3, "Review"),
    FORM(4, "Form"),
    CYCLE(5, "Cycle");

    private Integer id;
    private String description;

    @Override
    public String getCode() {
        return name();
    }

    public static PMElementType getByCode(String code) {
        for (PMElementType type : values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
