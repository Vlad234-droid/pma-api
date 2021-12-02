package com.tesco.pma.process.api;

import java.time.Instant;
import java.util.UUID;

import com.tesco.pma.api.Identified;
import com.tesco.pma.api.StatusAware;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reflects PMA runtime process
 *
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 19:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PMRuntimeProcess implements Identified<UUID>, StatusAware<PMProcessStatus> {
    private UUID id;
    private PMProcessStatus status;
    private UUID cycleUuid;
    private String bpmProcessId;
    private String businessKey;
    private Instant lastUpdateTime;
}
