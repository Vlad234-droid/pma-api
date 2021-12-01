package com.tesco.pma.review.dao;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.review.domain.TimelinePoint;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on timeline points
 */
public interface TimelinePointDAO {

    /**
     * Creates a timeline point
     *
     * @param tLPoint a timeline point
     * @return number of created timeline points
     */
    int create(@Param("tLPoint") TimelinePoint tLPoint);

    /**
     * Returns a timeline point by an identifier of timeline point
     *
     * @param uuid an identifier of timeline point
     * @return a timeline point
     */
    TimelinePoint read(@Param("uuid") UUID uuid);

    /**
     * Update a timeline point
     *
     * @param tLPoint         a timeline point
     * @param allowedStatuses allowed statuses for updating timeline point
     * @return number of updated timeline points
     */
    int update(@Param("tLPoint") TimelinePoint tLPoint,
               @Param("allowedStatuses") Collection<PMTimelinePointStatus> allowedStatuses);

    /**
     * Delete a timeline point
     *
     * @param uuid            an identifier of timeline point
     * @param allowedStatuses allowed statuses for deleting timeline point
     * @return number of timeline points
     */
    int delete(@Param("uuid") UUID uuid,
               @Param("allowedStatuses") Collection<PMTimelinePointStatus> allowedStatuses);

    /**
     * Returns timeline points by parameters.
     * if some parameter is null it will be ignored
     *
     * @param colleagueCycleUuid an identifier of colleague cycle
     * @param code               a code of timeline point
     * @param status             a timeline point status
     * @return a list of timeline points
     */
    List<TimelinePoint> getByParams(@Param("colleagueCycleUuid") UUID colleagueCycleUuid,
                                    @Param("code") String code,
                                    @Param("status") PMTimelinePointStatus status);

    /**
     * Updates a timeline point status
     * if some parameter is null it will be ignored
     *
     * @param colleagueCycleUuid an identifier of colleague cycle
     * @param code               a code of timeline point
     * @param newStatus          a new timeline point status
     * @param prevStatuses       previous timeline point statuses
     * @return number of updated timeline point statuses
     */
    int updateStatusByParams(@Param("colleagueCycleUuid") UUID colleagueCycleUuid,
                             @Param("code") String code,
                             @Param("newStatus") PMTimelinePointStatus newStatus,
                             @Param("prevStatuses") Collection<PMTimelinePointStatus> prevStatuses);

    /**
     * Delete timeline points by parameters
     * if some parameter is null it will be ignored
     *
     * @param colleagueCycleUuid an identifier of colleague cycle
     * @param code               a code of timeline point
     * @param status             a timeline point status
     * @param allowedStatuses    allowed statuses for deleting timeline point
     * @return number of deleted timeline points
     */
    int deleteByParams(@Param("colleagueCycleUuid") UUID colleagueCycleUuid,
                       @Param("code") String code,
                       @Param("status") PMTimelinePointStatus status,
                       @Param("allowedStatuses") Collection<PMTimelinePointStatus> allowedStatuses);

}
