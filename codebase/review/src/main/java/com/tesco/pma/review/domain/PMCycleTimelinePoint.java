package com.tesco.pma.review.domain;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.model.PMElementType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PMCycleTimelinePoint implements Serializable {
    private static final long serialVersionUID = -5674053418450820379L;

    UUID cycleUuid;
    String code;
    String description;
    PMElementType type;
    PMReviewType reviewType;
    LocalDate startDate;
    PMTimelinePointStatus status;
    Integer count;
}