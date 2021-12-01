package com.tesco.pma.review.domain;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SimplifiedReview implements Serializable {
    private static final long serialVersionUID = -771526432976133963L;

    UUID uuid;
    PMReviewType type;
    PMTimelinePointStatus status;
    Integer number;
}
