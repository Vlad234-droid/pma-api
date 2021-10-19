package com.tesco.pma.process.service;

import java.util.UUID;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.process.api.PMRuntimeProcess;
import com.tesco.pma.process.api.PMProcessStatus;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.10.2021 Time: 18:47
 */
public interface PMProcessService {
    /**
     * Creates a new instance of the process
     * @param process creating process.
     *                Notes:
     *                1. UUID identifier is generating automatically
     *                2. REGISTERED status is applying
     *                3.
     */
    void register(PMRuntimeProcess process);

    /**
     * Returns process by uuid
     *
     * @param uuid process identifier
     * @return the instance of the process otherwise
     * @throws com.tesco.pma.exception.NotFoundException if the was not found
     */
    PMRuntimeProcess getProcess(UUID uuid);

    /**
     * Updates the status of the process by uuid with the filter
     * @param uuid process identifier
     * @param status required status
     * @param statusFilter filter of statuses
     */
    void updateStatus(UUID uuid, PMProcessStatus status, DictionaryFilter<PMProcessStatus> statusFilter);
}
