package com.tesco.pma.review.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewType implements DictionaryItem<Integer> {

    OBJECTIVE(1, "Objective review"),
    QUARTER(2, "Quarter review"),
    MYR(3, "Mid year review"),
    EYR(4, "End of year review");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
