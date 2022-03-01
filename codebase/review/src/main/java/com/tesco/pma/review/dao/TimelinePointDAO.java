package com.tesco.pma.review.dao;

import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.review.domain.TimelinePoint;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

/**
 * Interface to perform database operation on timeline points
 */
public interface TimelinePointDAO {

    /**
     * Creates a timeline point
     *
     * @param tlPoint a timeline point
     * @return number of created timeline points
     */
    default int create(TimelinePoint tlPoint) {
        tlPoint.setLastUpdatedTime(now());
        return intCreate(tlPoint);
    }

    /**
     * Creates a timeline point
     *
     * @param tlPoint a timeline point
     * @return number of created timeline points
     */
    int intCreate(@Param("tlPoint") TimelinePoint tlPoint);

    /**
     * Creates list of timeline points
     *
     * @param timelinePoints list of timeline points
     * @return number of created timeline points
     */
    default int saveAll(Collection<TimelinePoint> timelinePoints) {
        var now = now();
        timelinePoints.forEach(tp -> tp.setLastUpdatedTime(now));
        return intSaveAll(timelinePoints);
    }

    /**
     * Creates list of timeline points
     *
     * @param timelinePoints list of timeline points
     * @return number of created timeline points
     */
    int intSaveAll(@Param("tlPoints") Collection<TimelinePoint> timelinePoints);

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
     * @param tlPoint         a timeline point
     * @param allowedStatuses allowed statuses for updating timeline point
     * @return number of updated timeline points
     */
    default int update(TimelinePoint tlPoint, Collection<PMTimelinePointStatus> allowedStatuses) {
        tlPoint.setLastUpdatedTime(now());
        return intUpdate(tlPoint, allowedStatuses);
    }

    /**
     * Update a timeline point
     *
     * @param tlPoint         a timeline point
     * @param allowedStatuses allowed statuses for updating timeline point
     * @return number of updated timeline points
     */
    int intUpdate(@Param("tlPoint") TimelinePoint tlPoint,
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
     * Updates a timeline point status
     *
     * @param uuid         an identifier of timeline point
     * @param newStatus    a new timeline point status
     * @param prevStatuses previous timeline point statuses
     * @return number of updated timeline point statuses
     */
    default int updateStatus(@Param("uuid") UUID uuid,
                             @Param("newStatus") PMTimelinePointStatus newStatus,
                             @Param("prevStatuses") Collection<PMTimelinePointStatus> prevStatuses) {
        return intUpdateStatus(uuid, newStatus, prevStatuses, now());
    }

    int intUpdateStatus(@Param("uuid") UUID uuid,
                        @Param("newStatus") PMTimelinePointStatus newStatus,
                        @Param("prevStatuses") Collection<PMTimelinePointStatus> prevStatuses,
                        @Param("lastUpdatedTime") Instant lastUpdatedTime);

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

    /**
     * Returns time line by PM cycleUuid
     *
     * @param colleagueUuid an identifier of colleague
     * @param pmCycleStatus a status of colleague cycle
     * @return a list of TimelinePoint
     */
    List<TimelinePoint> getTimeline(@Param("colleagueUuid") UUID colleagueUuid,
                                    @Param("pmCycleStatus") PMCycleStatus pmCycleStatus);


    /**
     * Returns timeline by UUID
     *
     * @param id     an identifier of timeline
     * @return TimelinePoint
     */
    TimelinePoint getTimelineByUUID(@Param("timelineUUID") UUID id);

}
