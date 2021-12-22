package com.tesco.pma.cycle.dao;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PMColleagueCycleDAO {

    /**
     * Gets pm colleague cycle by its uuid
     *
     * @param uuid - identifier
     * @return PM colleague cycle or null if nothing found
     */
    PMColleagueCycle read(@Param("uuid") UUID uuid);

    /**
     * Gets list of pm colleague cycles
     *
     * @param cycleUuid     - PM cycle identifier
     * @param colleagueUuid - colleague identifier
     * @param status        - PM colleague cycle status
     * @return - collection of PM colleague cycles
     */
    List<PMColleagueCycle> getByParams(@Param("cycleUuid") UUID cycleUuid,
                                       @Param("colleagueUuid") UUID colleagueUuid,
                                       @Param("status") PMCycleStatus status);

    /**
     * Stores collection of PM colleague cycles
     *
     * @param pmColleagueCycles - objects to be stored
     * @return number of stored objects
     */
    int saveAll(@Param("colleagueCycles") Collection<PMColleagueCycle> pmColleagueCycles);

    /**
     * Stores PM colleague cycle
     *
     * @param pmColleagueCycle - object to be stored
     * @return number of stored objects
     */
    int create(@Param("cc") PMColleagueCycle pmColleagueCycle);

    /**
     * Updates PM colleague cycle
     *
     * @param pmColleagueCycle - object to be updated
     * @return number of updated objects
     */
    int update(@Param("cc") PMColleagueCycle pmColleagueCycle);

    /**
     * Deletes PM colleague cycle by its uuid
     *
     * @param uuid - identifier
     * @return number of deleted objects
     */
    int delete(@Param("uuid") UUID uuid);

    /**
     * Gets list of pm colleague cycles without timeline points
     *
     * @param cycleUuid - PM cycle identifier
     * @param startTime - start time filter
     * @return - collection of PM colleague cycles
     */
    List<PMColleagueCycle> getByCycleUuidWithoutTimelinePoint(UUID cycleUuid, Instant startTime);

    /**
     * Gets list of colleagues by key
     *
     * @param key                   - key
     * @param hireDate              - colleague hire date
     * @param withoutColleagueCycle - find without created colleague cycle
     * @return list of colleagues
     */
    List<ColleagueEntity> findColleagues(@Param("key") String key, @Param("hireDate") LocalDate hireDate,
                                         @Param("withoutColleagueCycle") boolean withoutColleagueCycle);
}
