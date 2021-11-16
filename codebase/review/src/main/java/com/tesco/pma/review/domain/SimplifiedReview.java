package com.tesco.pma.review.domain;

import com.tesco.pma.cycle.api.PMReviewStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SimplifiedReview {
    UUID uuid;
    PMReviewType type;
    PMReviewStatus status;
    Integer number;
}
