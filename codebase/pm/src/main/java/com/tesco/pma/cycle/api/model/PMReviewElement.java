package com.tesco.pma.cycle.api.model;

import com.tesco.pma.api.ReviewType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 16.10.2021 Time: 20:43
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PMReviewElement extends PMTimelinePointElement {
    public static final String PM_REVIEW = "review";
    public static final String PM_REVIEW_PREFIX = PM_PREFIX + PM_REVIEW + "_";
    public static final String PM_REVIEW_TYPE = PM_REVIEW_PREFIX + "type";
    public static final String PM_REVIEW_MIN = PM_REVIEW_PREFIX + "min";
    public static final String PM_REVIEW_MAX = PM_REVIEW_PREFIX + "max";
    public static final String PM_REVIEW_START = PM_REVIEW_PREFIX + "start_time";
    public static final String PM_REVIEW_START_DELAY = PM_REVIEW_PREFIX + "start_delay";
    public static final String PM_REVIEW_DURATION = PM_REVIEW_PREFIX + "duration";
    public static final String PM_REVIEW_NOTIFY_DELAY = PM_REVIEW_PREFIX + "notify_delay";
    public static final String PM_REVIEW_PRE_NOTIFY_BEFORE = PM_REVIEW_PREFIX + "pre_notify_before";
    public static final String PM_REVIEW_PRE_NOTIFY_START_TIME = PM_REVIEW_PREFIX + "pre_notify_start_time";

    public static final String DEFAULT_PM_REVIEW_MIN = "1";
    public static final String DEFAULT_PM_REVIEW_MAX = "1";

    private ReviewType reviewType;
    private PMFormElement form;

    public PMReviewElement(String id, String code, String description) {
        super(id, code, description, PMElementType.REVIEW);
    }

    public static List<String> getPropertyNames() {
        return getPropertyNames(PMReviewElement.class, PM_REVIEW_PREFIX + "(?!prefix)([\\w]+)$");
    }
}
