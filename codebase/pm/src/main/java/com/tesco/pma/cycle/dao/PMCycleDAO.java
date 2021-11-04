package com.tesco.pma.cycle.dao;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleTimelinePoint;
import com.tesco.pma.cycle.api.PMCycleStatus;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

public interface PMCycleDAO {

    default void create(PMCycle cycle) {
        create(cycle, now());
    }

    void create(@Param("cycle") PMCycle cycle,
                @Param("now") Instant now);

    int updateStatus(@Param("uuid") UUID uuid,
                     @Param("status") PMCycleStatus status,
                     @Param("statusFilter") DictionaryFilter<PMCycleStatus> statusFilter);

    List<PMCycle> getByStatus(@Param("status") PMCycleStatus status);

    PMCycle read(@Param("uuid") UUID uuid);

    int publish(@Param("uuid") PMCycle cycle);

    int update(@Param("cycle") PMCycle cycle,
               @Param("statusFilter") DictionaryFilter<PMCycleStatus> statusFilter);

    PMCycle getCurrentByColleague(UUID colleagueUuid);

    List<PMCycle> getByColleague(@Param("colleagueUuid") UUID colleagueUuid,
                                 @Param("statusFilter") DictionaryFilter<PMCycleStatus> statusFilter);

    default List<PMCycle> getByColleague(UUID colleagueUuid) {
        return getByColleague(colleagueUuid, null);
    }

    List<PMCycleTimelinePoint> readTimeline(@Param("uuid") UUID uuid);

    int updateMetadata(@Param("uuid") UUID uuid, @Param("metadata") String metadata);
}
