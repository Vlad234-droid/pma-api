package com.tesco.pma.feedback.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The FeedbackStatus enumeration.
 */
@Getter
@RequiredArgsConstructor
public enum FeedbackStatus implements DictionaryItem<Integer> {
    DRAFT(1, "Feedback is saved as draft"),
    SUBMITTED(2, "Feedback was submitted"),
    PENDING(3, "Feedback request is pending"),
    COMPLETED(4, "Feedback is completed");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
