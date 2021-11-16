package com.tesco.pma.review.domain;

import com.tesco.pma.api.PMElementType;
import com.tesco.pma.api.ReviewStatus;
import com.tesco.pma.api.ReviewType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PMCycleTimelinePoint {
    UUID cycleUuid;
    String code;
    String description;
    PMElementType type;
    ReviewType reviewType;
    LocalDate startDate;
    ReviewStatus status;
    Integer count;
}