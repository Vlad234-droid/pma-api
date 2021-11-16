package com.tesco.pma.review.domain;


import com.tesco.pma.api.ReviewStatus;
import com.tesco.pma.api.ReviewType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

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
    UUID cycleUuid;
    UUID colleagueUuid;
    ReviewType type;
    List<ReviewStatusCounter> statusStats;

    public Map<ReviewStatus, Integer> getMapStatusStats() {
        if (statusStats == null) {
            return Collections.emptyMap();
        }
        return statusStats
                .stream()
                .collect(Collectors.toMap(ReviewStatusCounter::getStatus, ReviewStatusCounter::getCount));
    }
}
