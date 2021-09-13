package com.tesco.pma.objective.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ObjectiveStatus implements DictionaryItem<Integer> {

    DRAFT(1, "A draft of objective"),
    SUBMITTED(2, "An objective is submitted"),
    APPROVED(3, "An objective is approved"),
    REJECTED(4, "An objective is rejected"),
    COMPLETED(5, "An objective is completed");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
