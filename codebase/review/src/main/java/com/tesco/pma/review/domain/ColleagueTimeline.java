package com.tesco.pma.review.domain;

import com.tesco.pma.colleague.api.ColleagueSimple;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class ColleagueTimeline extends ColleagueSimple {

    List<SimplifiedReview> reviews;
    List<PMCycleTimelinePoint> timeline;
}
