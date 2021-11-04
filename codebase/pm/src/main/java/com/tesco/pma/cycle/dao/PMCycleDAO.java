package com.tesco.pma.cycle.dao;

import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycle;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PMCycleDAO {

    void create(@Param("cycle") PMCycle cycle);

    int updateStatus(@Param("uuid") UUID uuid,
                     @Param("status") PMCycleStatus status,
                     @Param("prevStatuses") Collection<PMCycleStatus> prevStatuses);

    List<PMCycle> getByStatus(@Param("status") PMCycleStatus status);

    PMCycle read(@Param("uuid") UUID uuid);

    int publish(@Param("uuid") PMCycle cycle);

    int update(@Param("cycle") PMCycle cycle,
               @Param("prevStatuses") Collection<PMCycleStatus> prevStatuses);

    PMCycle getCurrentByColleague(UUID colleagueUuid);

    List<PMCycle> getByColleague(UUID colleagueUuid);
}
