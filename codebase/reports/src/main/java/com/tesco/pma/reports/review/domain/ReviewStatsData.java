package com.tesco.pma.reports.review.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Review report statistics data
 */
@EqualsAndHashCode
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewStatsData {

    // TODO: make it (all percentages) decimal instead of integer; add all review percentages here
    Integer objectivesSubmittedPercentage;
    Integer objectivesApprovedPercentage;

    public List<Object> toList() {
        var statistics = new ArrayList<>();

        statistics.add(objectivesSubmittedPercentage);
        statistics.add(objectivesApprovedPercentage);

        return statistics;
    }
}

    /*
    <resultMap id="colleagueReportTagsResultMap" type="map">
        <result column="must_create_objective" property="must_create_objective"/>
        <result column="has_objective_submitted" property="has_objective_submitted"/>
        <result column="has_objective_approved" property="has_objective_approved"/>
        <result column="must_create_myr" property="must_create_myr"/>
        <result column="has_myr_submitted" property="has_myr_submitted"/>
        <result column="has_myr_approved" property="has_myr_approved"/>
        <result column="myr_what_rating" property="myr_what_rating"/>
        <result column="myr_how_rating" property="myr_how_rating"/>
        <result column="must_create_eyr" property="must_create_eyr"/>
        <result column="has_eyr_submitted" property="has_eyr_submitted"/>
        <result column="has_eyr_approved" property="has_eyr_approved"/>
        <result column="eyr_what_rating" property="eyr_what_rating"/>
        <result column="eyr_how_rating" property="eyr_how_rating"/>
    </resultMap> */