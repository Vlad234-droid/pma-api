package com.tesco.pma.cycle.dao;

import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PerformanceCycle;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PmCycleDAO {

    int createCycle(@Param("cycle") PerformanceCycle cycle);

    int updateCycleStatus(@Param("uuid") UUID uuid,
                          @Param("status") PMCycleStatus status,
                          @Param("prevStatuses") Collection<PMCycleStatus> prevStatuses);

    List<PerformanceCycle> getAllPmCyclesForStatus(@Param("status") PMCycleStatus status);

    PerformanceCycle getPmCycle(@Param("uuid") UUID uuid);

    int publishCycle(@Param("uuid") PerformanceCycle cycle);

    int updatePmCycle(@Param("cycle") PerformanceCycle cycle,
                      @Param("prevStatuses") Collection<PMCycleStatus> prevStatuses);


}
