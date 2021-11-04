package com.tesco.pma.feedback.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Object type that is feedback given on/to or requested about/from.
 */
@Getter
@RequiredArgsConstructor
public enum FeedbackTargetType  implements DictionaryItem<Integer> {
    GOAL(1, "Feedback is given on or requested about goal"),
    OBJECTIVE(2, "Feedback is given on or requested about objective"),
    VALUE_BEHAVIOR(3, "Feedback is given on or requested about value and behavior"),
    OTHER(4, "Feedback is given on or requested about anything else");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
