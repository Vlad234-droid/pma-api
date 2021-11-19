package com.tesco.pma.cycle.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PMReviewStatus implements DictionaryItem<Integer> {

    DRAFT(1, "Review is saved as draft"),
    WAITING_FOR_APPROVAL(2, "Review was submitted but not yet approved"),
    APPROVED(3, "Review was approved"),
    DECLINED(4, "Review was declined"),
    COMPLETED(5, "Review is completed"),
    OVERDUE(6, "Review was overdue");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
