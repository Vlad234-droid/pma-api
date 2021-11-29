package com.tesco.pma.review.service;

import com.tesco.pma.review.domain.Review;

import java.util.List;
import java.util.UUID;

public interface ObjectiveSharingService {

    /**
     * Enable sharing colleague's objectives
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     */
    void shareObjectives(UUID colleagueUuid, UUID cycleUuid);

    /**
     * Disable sharing colleague's objectives
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     */
    void stopSharingObjectives(UUID colleagueUuid, UUID cycleUuid);

    /**
     * Check if colleague enable sharing objectives
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     * @return true/false
     */
    boolean isColleagueShareObjectives(UUID colleagueUuid, UUID cycleUuid);

    /**
     * Get list of colleague's shared approved objectives
     *
     * @param colleagueUuid - colleague identifier
     * @return list of shared approved objectives
     */
    List<Review> getSharedObjectivesForColleague(UUID colleagueUuid);
}
