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
public class PMReview extends PMElement {
    public static final String PM_REVIEW = "review";
    public static final String PM_REVIEW_PREFIX = PM_PREFIX + PM_REVIEW + "_";
    public static final String PM_REVIEW_TYPE = PM_REVIEW_PREFIX + "review_type";
    public static final String PM_REVIEW_MIN = PM_REVIEW_PREFIX + "review_min";
    public static final String PM_REVIEW_MAX = PM_REVIEW_PREFIX + "review_max";

    public static final String DEFAULT_PM_REVIEW_MIN = "1";
    public static final String DEFAULT_PM_REVIEW_MAX = "1";

    private DictionaryItem<Integer> reviewType;
    private PMForm form;

    public PMReview(String id, String code, String description, DictionaryItem<Integer> type) {
        super(id, code, description, type);
    }
}
