package com.tesco.pma.cycle.service;

import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PerformanceCycle;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Performance cycle service
 */
public interface PerformanceCycleService {

    PerformanceCycle create(@NotNull PerformanceCycle cycle);

    PerformanceCycle publish(@NotNull PerformanceCycle cycle);

    PerformanceCycle updateStatus(@NotNull UUID cycleUuid,
                                  @NotNull PMCycleStatus status);

    PerformanceCycle getPerformanceCycle(@NotNull UUID uuid);

    PerformanceCycle updatePerformanceCycle(@NotNull PerformanceCycle uuid,
                                            @NotNull Collection<PMCycleStatus> oldStatuses);

    List<PerformanceCycle> getAllPerformanceCyclesForStatus(@NotNull PMCycleStatus status);
}

