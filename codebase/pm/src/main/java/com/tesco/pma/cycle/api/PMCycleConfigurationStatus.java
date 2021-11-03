package com.tesco.pma.cycle.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

@Getter
public enum PMCycleConfigurationStatus implements DictionaryItem<Integer> {

    ACTIVE(1, "Performance cycle configuration is active"),

    INACTIVE(2, "Performance cycle configuration is inactive"),

    REMOVED(3, "Performance cycle configuration is removed"),

    DRAFT(4, "Performance cycle configuration in draft");

    private Integer id;
    private String description;

    PMCycleConfigurationStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }

    public static PMCycleConfigurationStatus getByCode(String code) {
        for (PMCycleConfigurationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
