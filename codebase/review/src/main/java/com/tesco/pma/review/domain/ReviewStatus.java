package com.tesco.pma.review.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewStatus implements DictionaryItem<Integer> {

    DRAFT(1, "Review is saved as draft"),
    WAITING_FOR_APPROVAL(2, "Review was submitted but not yet approved"),
    APPROVED(3, "Review was approved"),
    RETURNED(4, "Review was returned"),
    COMPLETED(5, "Review is completed");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
