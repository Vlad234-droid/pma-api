package com.tesco.pma.process.dao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Param;

import com.tesco.pma.api.StatusHistoryRecord;
import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.process.api.PMRuntimeProcess;
import com.tesco.pma.process.api.PMProcessStatus;

import static java.time.Instant.now;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 18:46
 */
public interface PMRuntimeProcessDAO {
    /**
     * Creates the process
     * @param process Creating process
     *                Note: sets last update time if it is not set
     * @return number of created instances: 0 or 1
     */
    default int create(PMRuntimeProcess process) {
        if (process.getLastUpdateTime() == null) {
            process.setLastUpdateTime(now());
        }
        if (1 == createInt(process)) {
            return createHistoryRecord(process.getId(), process.getStatus(), process.getLastUpdateTime());
        }
        return 0;
    }

    int createInt(@Param("process") PMRuntimeProcess process);

    /**
     * Updates status allowed by set of old statuses.
     * If allowed statuses are null or empty there no any restrictions.
     * @param uuid process identifier
     * @param status new status
     * @param statusFilter status filter
     * @return number of updated records or 0
     */
    default int updateStatus(UUID uuid, PMProcessStatus status, DictionaryFilter<PMProcessStatus> statusFilter) {
        var updateTime = now();
        if (1 == updateStatusInt(uuid, status, updateTime, statusFilter)) {
            return createHistoryRecord(uuid, status, updateTime);
        }
        return 0;
    }

    PMRuntimeProcess read(@Param("uuid") UUID uuid);

    List<StatusHistoryRecord<UUID, PMProcessStatus>> readHistory(@Param("uuid") UUID uuid);

    List<PMRuntimeProcess> findByColleagueAndProcessName(@Param("colleagueUuid") UUID colleagueUuid,
                                                         @Param("processName") String processName);

    //todo: find methods

    int updateStatusInt(@Param("uuid") UUID uuid, @Param("status") PMProcessStatus status, @Param("updateTime") Instant updateTime,
                        @Param("statusFilter") DictionaryFilter<PMProcessStatus> statusFilter);

    int createHistoryRecord(@Param("uuid") UUID uuid, @Param("status") PMProcessStatus status, @Param("updateTime") Instant updateTime);
}
