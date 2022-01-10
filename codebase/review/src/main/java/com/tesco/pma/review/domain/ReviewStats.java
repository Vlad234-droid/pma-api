package com.tesco.pma.review.domain;


import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ReviewStats {
    UUID tlPointUuid;
    PMReviewType type;
    List<ReviewStatusCounter> statusStats;

    public Map<PMTimelinePointStatus, Integer> getMapStatusStats() {
        if (statusStats == null) {
            return Collections.emptyMap();
        }
        return statusStats
                .stream()
                .collect(Collectors.toMap(ReviewStatusCounter::getStatus, ReviewStatusCounter::getCount));
    }

    public Integer getCountAll() {
        if (statusStats == null) {
            return 0;
        }
        return statusStats
                .stream()
                .mapToInt(ReviewStatusCounter::getCount)
                .sum();
    }

    public Integer getCount(List<PMTimelinePointStatus> statuses) {
        if (statusStats == null) {
            return 0;
        }
        return statusStats
                .stream()
                .filter(ss -> statuses.contains(ss.getStatus()))
                .mapToInt(ReviewStatusCounter::getCount)
                .sum();
    }

    public PMTimelinePointStatus getLastUpdatedStatus(PMTimelinePointStatus skipStatus) {
        PMTimelinePointStatus lastUpdatedStatus = null;
        Instant lastUpdatedTime = null;
        for (ReviewStatusCounter reviewStatusCounter : statusStats) {
            if (reviewStatusCounter.getStatus().equals(skipStatus)) {
                continue;
            }
            if (lastUpdatedTime == null
                    || reviewStatusCounter.getLastUpdatedTime().compareTo(lastUpdatedTime) > 0) {
                lastUpdatedTime = reviewStatusCounter.getLastUpdatedTime();
                lastUpdatedStatus = reviewStatusCounter.getStatus();
            }
        }

        return lastUpdatedStatus;
    }
}
