package com.tesco.pma.cycle.api;

import com.tesco.pma.api.DictionaryItem;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 20.10.2021 Time: 10:54
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PMCycleType implements DictionaryItem<Integer> {
    FISCAL(1, "Fiscal year start"),
    HIRING(2, "Hiring date start");

    private Integer id;
    private String description;

    @Override
    public String getCode() {
        return name();
    }
}
