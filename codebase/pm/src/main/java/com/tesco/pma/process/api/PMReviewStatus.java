package com.tesco.pma.process.api;

import com.tesco.pma.api.DictionaryItem;

import lombok.Getter;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 18:33
 */
@Getter
public enum PMReviewStatus implements DictionaryItem<Integer> {

    DRAFT(1, "Saved as draft"),
    WAITING_FOR_APPROVAL(2, "Waiting for approval"),
    APPROVED(3, "Approved"),
    RETURNED(4, "Returned back to fix"),
    TIMEOUT(5, "Timeout expired");

    private Integer id;
    private String description;

    PMReviewStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }
}
