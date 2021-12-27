package com.tesco.pma.review.domain;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ReviewStatusCounter {
    PMTimelinePointStatus status;
    Integer count;
    Instant lastUpdatedTime;

    public ReviewStatusCounter(PMTimelinePointStatus status, Integer count) {
        this.status = status;
        this.count = count;
    }
}
