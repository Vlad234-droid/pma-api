package com.tesco.pma.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ReviewType implements DictionaryItem<Integer> {

    OBJECTIVE(1, "Objective review"),
    MYR(2, "Mid year review"),
    EYR(3, "End of year review");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
