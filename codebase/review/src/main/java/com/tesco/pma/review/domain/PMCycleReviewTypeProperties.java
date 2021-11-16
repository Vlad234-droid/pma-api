package com.tesco.pma.review.domain;

import com.tesco.pma.api.ReviewType;
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
public class PMCycleReviewTypeProperties {
    UUID cycleUuid;
    ReviewType type;
    Integer min;
    Integer max;
}
