package com.tesco.pma.process.api.model;

import com.tesco.pma.api.DictionaryItem;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 20.10.2021 Time: 10:54
 */
@Getter
@AllArgsConstructor
public enum PMCycleType implements DictionaryItem<Integer> {
    FISCAL(1, "Fiscal year start"),
    HIRING_DATE(2, "Hiring date");

    private Integer id;
    private String description;

    @Override
    public String getCode() {
        return name();
    }
}
