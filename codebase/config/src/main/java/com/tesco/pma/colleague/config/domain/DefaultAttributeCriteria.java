package com.tesco.pma.colleague.config.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DefaultAttributeCriteria implements DictionaryItem<Integer> {

    ALL(1, "All colleagues"),
    WL_4_OR_5(2, "Only for WL4&5 (working level)"),
    LINE_MANAGER(3, "Line managers only"),
    MYR(4, "Only for colleagues with mid-year reviews"),
    Q1(5, "Only for colleagues with Q1 reminders"),
    Q3(6, "Only for colleagues with Q3 reminders"),
    OBJECTIVE(7, "Only for colleagues with objectives");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
