package com.tesco.pma.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewStatus implements DictionaryItem<Integer> {

    DRAFT(1, "Review is saved as draft"),
    WAITING_FOR_APPROVAL(2, "Review was submitted but not yet approved"),
    APPROVED(3, "Review was approved"),
    DECLINED(4, "Review was declined"),
    COMPLETED(5, "Review is completed");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
