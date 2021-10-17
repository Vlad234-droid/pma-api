package com.tesco.pma.process.api.model;

import com.tesco.pma.api.DictionaryItem;

import lombok.Getter;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 16.10.2021 Time: 20:49
 */
@Getter
public enum PMReviewType implements DictionaryItem<Integer> {
    /**
     * Objective review
     */
    OBJECTIVE(1, "Objective review"),
    /**
     * Objective approval
     */
    OBJECTIVE_APPROVAL(2, "Objective approval"),
    /**
     * Quarter review
     */
    QUARTER(3, "Quarter review"),
    /**
     * Quarter approval
     */
    QUARTER_APPROVAL(4, "Quarter approval"),
    /**
     * Mid year review
     */
    MYR(5, "Mid year review"),
    /**
     * Mid year review approval
     */
    MYR_APPROVAL(6, "Mid year review approval"),
    /**
     * End of year review
     */
    EYR(7, "End of year review"),
    /**
     * End of year review approval
     */
    EYR_APPROVAL(8, "End of year review approval");

    private Integer id;
    private String description;

    PMReviewType(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }
}
