package com.tesco.pma.cycle.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PMTimelinePointStatus implements DictionaryItem<Integer> {

    DRAFT(1, "Saved as draft"),
    WAITING_FOR_APPROVAL(2, "Submitted but not yet approved"),
    APPROVED(3, "Approved"),
    DECLINED(4, "Declined"),
    COMPLETED(5, "Completed"),
    OVERDUE(6, "Overdue"),
    STARTED(7, "Started"),
    NOT_STARTED(8, "Not started"),
    NOT_CREATED(9, "Not created");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
