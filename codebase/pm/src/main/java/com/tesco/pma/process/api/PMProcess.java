package com.tesco.pma.process.api;

import java.time.Instant;
import java.util.UUID;

import com.tesco.pma.api.Identified;
import com.tesco.pma.api.StatusAware;

import lombok.Data;

/**
 * Reflects PMA runtime process
 *
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 19:02
 */
@Data
public class PMProcess implements Identified<UUID>, StatusAware<PMProcessStatus> {
    private UUID id;
    private UUID colleagueUuid;
    private PMProcessStatus status;
    private UUID bpmProcessId;
    private Instant lastUpdateTime;
}
