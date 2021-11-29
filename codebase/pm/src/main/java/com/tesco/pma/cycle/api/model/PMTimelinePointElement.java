package com.tesco.pma.cycle.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 11.11.2021 Time: 13:11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PMTimelinePointElement extends PMElement {
    private static final long serialVersionUID = 8220411457724646393L;

    public static final String PM_TIMELINE_POINT = "timeline_point";
    public static final String PM_TIMELINE_POINT_PREFIX = PM_PREFIX + PM_TIMELINE_POINT + "_";
    public static final String PM_TIMELINE_POINT_START_TIME = PM_TIMELINE_POINT_PREFIX + "start_time";
    public static final String PM_TIMELINE_POINT_START_DELAY = PM_TIMELINE_POINT_PREFIX + "start_delay";

    public PMTimelinePointElement() {
        setType(PMElementType.TIMELINE_POINT);
    }

    public PMTimelinePointElement(String id, String code, String description) {
        super(id, code, description, PMElementType.TIMELINE_POINT);
    }

    protected PMTimelinePointElement(String id, String code, String description, PMElementType type) {
        super(id, code, description, type);
    }

    public static List<String> getPropertyNames() {
        return getPropertyNames(PMTimelinePointElement.class, PM_TIMELINE_POINT_PREFIX + "(?!prefix)([\\w]+)$");
    }
}