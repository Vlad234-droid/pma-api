package com.tesco.pma.cycle.api.model;

import com.tesco.pma.api.Jsonb;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * Date: 14.10.2021 Time: 22:35
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PMCycleMetadata implements Jsonb {
    private PMCycleElement cycle;
}
