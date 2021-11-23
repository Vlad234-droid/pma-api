package com.tesco.pma.colleague.config.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DefaultAttributeCriteria implements DictionaryItem<Integer> {

    ALL_COLLEAGUES(1, "All colleagues"),
    WK_4_5_ONLY(2, "Only for WK4&5 (working level)"),
    LINE_MANAGER_ONLY(3, "Line managers only"),
    COLLEAGUES_WITH_MID_YEAR_REVIEW_ONLY(4, "Only for colleagues with mid-year reviews"),
    COLLEAGUES_WITH_Q1_REMINDERS_ONLY(5, "Only for colleagues with Q1 reminders"),
    COLLEAGUES_WITH_Q3_REMINDERS_ONLY(6, "Only for colleagues with Q3 reminders"),
    COLLEAGUES_WITH_OBJECTIVES_ONLY(7, "Only for colleagues with objectives");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
