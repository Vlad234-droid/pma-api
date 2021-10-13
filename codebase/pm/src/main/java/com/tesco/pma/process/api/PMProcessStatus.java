package com.tesco.pma.process.api;

import com.tesco.pma.api.DictionaryItem;

import lombok.Getter;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 18:04
 */
@Getter
public enum PMProcessStatus implements DictionaryItem<Integer> {
    /**
     * PM process is registered for the colleague
     */
    REGISTERED(1, "PM process is registered for the colleague"),
    /**
     * PM process is started for the colleague
     */
    STARTED(2, "PM process is started for the colleague"),
    /**
     * The review is started
     */
    IN_REVIEW(3, "The review is started"),
    /**
     * Process is waiting for the next review
     */
    WAITING_NEXT_REVIEW(4, "Process is waiting for the next review"),
    /**
     * PM process completed
     */
    COMPLETED(5, "PM process completed");

    private Integer id;
    private String description;

    PMProcessStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }
}
