package com.tesco.pma.process.api;

import com.tesco.pma.api.Jsonb;
import com.tesco.pma.process.api.model.PMCycle;

import lombok.Data;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.10.2021 Time: 22:35
 */
@Data
public class PMProcessMetadata implements Jsonb {
    private PMCycle cycle;
}
