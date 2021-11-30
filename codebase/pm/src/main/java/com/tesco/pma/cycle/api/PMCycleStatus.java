package com.tesco.pma.cycle.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

@Getter
public enum PMCycleStatus implements DictionaryItem<Integer> {

    ACTIVE(1, "Performance cycle is active"),

    INACTIVE(2, "Performance cycle is inactive"),

    COMPLETED(3, "Performance cycle is completed"),

    DRAFT(4, "Performance cycle in draft"),

    FAILED(5,"Performance cycle start failed");

    private final Integer id;
    private final String description;

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
