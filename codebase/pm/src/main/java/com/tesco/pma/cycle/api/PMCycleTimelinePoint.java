package com.tesco.pma.cycle.api;

import com.tesco.pma.api.ReviewStatus;
import com.tesco.pma.api.ReviewType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PMCycleTimelinePoint {
    String cycleUuid;
    String code;
    String description;
    ReviewType type;
    ReviewStatus status;
    Date startDate;
}
