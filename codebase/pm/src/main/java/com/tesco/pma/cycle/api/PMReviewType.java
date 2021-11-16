package com.tesco.pma.cycle.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PMReviewType implements DictionaryItem<Integer> {

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
