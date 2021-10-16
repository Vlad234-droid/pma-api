package com.tesco.pma.objective.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ObjectiveStatus implements DictionaryItem<Integer> {

    DRAFT(1, "An objective is saved as draft"),
    WAITING_FOR_APPROVAL(2, "An objective objective was submitted to LM approval but not yet approved"),
    APPROVED(3, "An objective was approved by LM"),
    RETURNED(4, "An objective was returned by LM"),
    COMPLETED(5, "An objective is completed");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
