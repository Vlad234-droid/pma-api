package com.tesco.pma.cycle.api;

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
    String type;
    String status;
    Date startDate;
}
