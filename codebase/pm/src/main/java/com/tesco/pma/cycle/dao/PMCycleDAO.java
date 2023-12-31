package com.tesco.pma.cycle.dao;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

public interface PMCycleDAO {

    /**
     * Updates status for existing performance cycle
     *
     * @param uuid         performance cycle UUID
     * @param status       new status
     * @param statusFilter previous status filter
     * @return number of updated entities
     */
    int updateStatus(@Param("uuid") UUID uuid,
                     @Param("status") PMCycleStatus status,
                     @Param("statusFilter") DictionaryFilter<PMCycleStatus> statusFilter);

    /**
     * Getting performance cycle by UUID
     *
     * @param uuid performance cycle UUID
     * @return performance cycle
     */
    PMCycle read(@Param("uuid") UUID uuid,
                 @Param("statusFilter") DictionaryFilter<PMCycleStatus> statusFilter);

    /**
     * Getting performance cycle for existing colleague
     *
     * @param colleagueUuid colleague UUID
     * @return list of performance cycle
     */
    List<PMCycle> getByColleague(@Param("colleagueUuid") UUID colleagueUuid,
                                 @Param("statusFilter") DictionaryFilter<PMCycleStatus> statusFilter);

    default List<PMCycle> getByColleague(UUID colleagueUuid) {
        return getByColleague(colleagueUuid, null);
    }

    /**
     * Updates metadata for existing performance cycle
     *
     * @param uuid     performance cycle UUID
     * @param metadata performance cycle metadata
     * @return number of updated entities
     */
    int updateMetadata(@Param("uuid") UUID uuid, @Param("metadata") String metadata);

    /**
     * Returns list of performance cycles
     *
     * @param includeMetadata include metadata in response or not
     * @return list of number of performance cycles
     */
    List<PMCycle> findAll(@Param("requestQuery") RequestQuery requestQuery,
                          @Param("includeMetadata") boolean includeMetadata);

    /**
     * Updates existing performance cycle
     *
     * @param cycle        performance cycle
     * @param statusFilter previous status filter
     * @return number of updated entities
     */
    int update(@Param("cycle") PMCycle cycle,
               @Param("statusFilter") DictionaryFilter<PMCycleStatus> statusFilter);


    /**
     * Creates performance cycle
     *
     * @param cycle performance cycle
     * @return number of updated entities
     */
    default int create(PMCycle cycle) {
        return intCreate(cycle, now());
    }

    int intCreate(@Param("cycle") PMCycle cycle,
                  @Param("now") Instant now);
}
