package com.tesco.pma.cycle.api.model;

import com.tesco.pma.api.DictionaryItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 11.11.2021 Time: 13:11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PMTimelinePointElement extends PMElement {
    public static final String PM_TIMELINE_POINT = "timeline_point";
    public static final String PM_TIMELINE_POINT_PREFIX = PM_PREFIX + PM_TIMELINE_POINT + "_";
    public static final String PM_TIMELINE_POINT_START_TIME = PM_TIMELINE_POINT_PREFIX + "start_time";

    public PMTimelinePointElement(String id, String code, String description, DictionaryItem<Integer> type) {
        super(id, code, description, type);
    }

    public static List<String> getPropertyNames() {
        return getPropertyNames(PMTimelinePointElement.class, PM_TIMELINE_POINT_PREFIX + "(?!prefix)([\\w]+)$");
    }
}