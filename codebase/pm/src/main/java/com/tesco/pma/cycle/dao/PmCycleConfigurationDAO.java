package com.tesco.pma.cycle.dao;

import com.tesco.pma.cycle.api.PMCycleConfigurationStatus;
import com.tesco.pma.cycle.api.PMCycleConfiguration;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PmCycleConfigurationDAO {

    void createCycle(@Param("config") PMCycleConfiguration config);

    int updateCycleStatus(@Param("uuid") UUID uuid,
                          @Param("status") PMCycleConfigurationStatus status,
                          @Param("prevStatuses") Collection<PMCycleConfigurationStatus> prevStatuses);

    List<PMCycleConfiguration> getAllPmCyclesForStatus(@Param("status") PMCycleConfigurationStatus status);

    PMCycleConfiguration getPmCycle(@Param("uuid") UUID uuid);

    int publishCycle(@Param("uuid") PMCycleConfiguration config);

    int updatePmCycle(@Param("config") PMCycleConfiguration config,
                      @Param("prevStatuses") Collection<PMCycleConfigurationStatus> prevStatuses);


}
