package com.tesco.pma.process.api.model;

import com.tesco.pma.api.DictionaryItem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 16.10.2021 Time: 20:43
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PMReviewElement extends PMElement {
    public static final String PM_REVIEW = "review";
    public static final String PM_REVIEW_PREFIX = PM_PREFIX + PM_REVIEW + "_";
    public static final String PM_REVIEW_TYPE = PM_REVIEW_PREFIX + "type";
    public static final String PM_REVIEW_MIN = PM_REVIEW_PREFIX + "min";
    public static final String PM_REVIEW_MAX = PM_REVIEW_PREFIX + "max";
    public static final String PM_REVIEW_START = PM_REVIEW_PREFIX + "start_time";
    public static final String PM_REVIEW_END = PM_REVIEW_PREFIX + "end_time";
    public static final String PM_REVIEW_DURATION = PM_REVIEW_PREFIX + "duration";
    public static final String PM_REVIEW_NOTIFY_DELAY = PM_REVIEW_PREFIX + "notify_delay";
    public static final String PM_REVIEW_PRE_NOTIFY_BEFORE = PM_REVIEW_PREFIX + "pre_notify_before";
    public static final String PM_REVIEW_PRE_NOTIFY_START_TIME = PM_REVIEW_PREFIX + "pre_notify_start_time";

    public static final String DEFAULT_PM_REVIEW_MIN = "1";
    public static final String DEFAULT_PM_REVIEW_MAX = "1";

    private DictionaryItem<Integer> reviewType;
    private PMFormElement form;

    public PMReviewElement(String id, String code, String description, DictionaryItem<Integer> type) {
        super(id, code, description, type);
    }
}
