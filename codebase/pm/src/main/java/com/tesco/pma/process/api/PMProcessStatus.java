package com.tesco.pma.process.api;

import com.tesco.pma.api.DictionaryItem;

import lombok.Getter;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 18:04
 */
@Getter
public enum PMProcessStatus implements DictionaryItem<Integer> {
    /**
     * PM process is registered for the business key
     */
    REGISTERED(1, "PM process is registered for the business key"),
    /**
     * PM process is started for the business key
     */
    STARTED(2, "PM process is started for the business key"),
    /**
     * PM process is suspended
     */
    SUSPENDED(3, "PM process is suspended"),
    /**
     * PM process is terminated
     */
    TERMINATED(4, "PM process is terminated"),
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
