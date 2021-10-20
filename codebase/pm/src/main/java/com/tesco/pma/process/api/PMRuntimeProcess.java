package com.tesco.pma.process.api;

import java.time.Instant;
import java.util.UUID;

import com.tesco.pma.api.Identified;
import com.tesco.pma.api.StatusAware;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reflects PMA runtime process
 *
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 19:02
 */
@Data
@NoArgsConstructor
public class PMRuntimeProcess implements Identified<UUID>, StatusAware<PMProcessStatus> {
    private UUID id;
    private UUID colleagueUuid;
    private PMProcessStatus status;
    private UUID bpmProcessId;
    private String bpmProcessName;
    private Instant lastUpdateTime;
    private UUID metadataId;

    public PMRuntimeProcess(UUID id, UUID colleagueUuid, PMProcessStatus status, UUID bpmProcessId,
                            String bpmProcessName, Instant lastUpdateTime) {
        this.id = id;
        this.colleagueUuid = colleagueUuid;
        this.status = status;
        this.bpmProcessId = bpmProcessId;
        this.bpmProcessName = bpmProcessName;
        this.lastUpdateTime = lastUpdateTime;
    }
}
