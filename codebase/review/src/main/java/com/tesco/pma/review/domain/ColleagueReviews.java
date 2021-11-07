package com.tesco.pma.review.domain;

import com.tesco.pma.cycle.api.PMCycleTimelinePoint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ColleagueReviews {
    UUID uuid;
    String firstName;
    String lastName;
    String jobName;
    String businessType;
    List<PMCycleTimelinePoint> timeline;
}
