package com.tesco.pma.review.domain;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.model.PMElementType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TimelinePoint implements Serializable {
    private static final long serialVersionUID = 1819211912152945571L;

    UUID uuid;
    UUID colleagueCycleUuid;
    String code;
    String description;
    PMElementType type;
    Instant startTime;
    Instant endTime;
    MapJson properties;
    PMTimelinePointStatus status;
    PMReviewType reviewType;
    Integer count;
    Instant lastUpdatedTime;
}
