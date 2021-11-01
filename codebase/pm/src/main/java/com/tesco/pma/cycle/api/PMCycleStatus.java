package com.tesco.pma.cycle.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

@Getter
public enum PMCycleStatus implements DictionaryItem<Integer> {

    ACTIVE(1, "Performance cycle is active"),

    INACTIVE(2, "Performance cycle is inactive"),

    REMOVED(3, "Performance cycle is removed"),

    DRAFT(4, "Performance cycle in draft");

    private Integer id;
    private String description;

    PMCycleStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }

    public static PMCycleStatus getByCode(String code) {
        for (PMCycleStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
