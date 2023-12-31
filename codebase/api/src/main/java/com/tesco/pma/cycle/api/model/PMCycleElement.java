package com.tesco.pma.cycle.api.model;

import com.tesco.pma.cycle.api.PMCycleType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 20.10.2021 Time: 11:44
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PMCycleElement extends PMElement {
    private static final long serialVersionUID = -8336744174325602486L;

    public static final String PM_CYCLE = "cycle";
    public static final String PM_CYCLE_PREFIX = PM_PREFIX + PM_CYCLE + "_";

    public static final String PM_CYCLE_TYPE = PM_CYCLE_PREFIX + "type";
    public static final String PM_CYCLE_START_TIME = PM_CYCLE_PREFIX + "start_time";
    public static final String PM_CYCLE_BEFORE_START = PM_CYCLE_PREFIX + "before_start";
    public static final String PM_CYCLE_END_TIME = PM_CYCLE_PREFIX + "end_time";
    public static final String PM_CYCLE_BEFORE_END = PM_CYCLE_PREFIX + "before_end";
    public static final String PM_CYCLE_MAX = PM_CYCLE_PREFIX + "max";

    private PMCycleType cycleType;
    private List<PMTimelinePointElement> timelinePoints = new ArrayList<>();

    public PMCycleElement() {
        setType(PMElementType.CYCLE);
    }

    public static List<String> getPropertyNames() {
        return getPropertyNames(PMCycleElement.class, PM_CYCLE_PREFIX + "(?!prefix)([\\w]+)$");
    }
}
