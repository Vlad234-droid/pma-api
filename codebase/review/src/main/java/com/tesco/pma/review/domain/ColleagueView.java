package com.tesco.pma.review.domain;

import com.tesco.pma.colleague.api.ColleagueSimple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class ColleagueView extends ColleagueSimple {
    private List<SimplifiedReview> reviews;
    private List<TimelinePoint> timeline;
}
