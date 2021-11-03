package com.tesco.pma.cycle.dao;

import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycle;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PmCycleDAO {

    int createCycle(@Param("cycle") PMCycle cycle);

    int updateCycleStatus(@Param("uuid") UUID uuid,
                          @Param("status") PMCycleStatus status,
                          @Param("prevStatuses") Collection<PMCycleStatus> prevStatuses);

    List<PMCycle> getAllPmCyclesForStatus(@Param("status") PMCycleStatus status);

    PMCycle getPmCycle(@Param("uuid") UUID uuid);

    int publishCycle(@Param("uuid") PMCycle cycle);

    int updatePmCycle(@Param("cycle") PMCycle cycle,
                      @Param("prevStatuses") Collection<PMCycleStatus> prevStatuses);


}
