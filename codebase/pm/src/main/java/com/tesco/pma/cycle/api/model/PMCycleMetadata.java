package com.tesco.pma.cycle.api.model;

import com.tesco.pma.api.Jsonb;
import lombok.Data;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.10.2021 Time: 22:35
 */
@Data
public class PMCycleMetadata implements Jsonb {
    public enum PMElementType {
        ELEMENT,
        TIMELINE,
        REVIEW,
        CYCLE
    }

    private PMCycleElement cycle;
}
