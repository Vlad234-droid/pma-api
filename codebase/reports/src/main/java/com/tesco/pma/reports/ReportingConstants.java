package com.tesco.pma.reports;

import lombok.experimental.UtilityClass;

/**
 * Constants used in reporting: tags, ratings, etc
 */
@UtilityClass
@SuppressWarnings("PMD.ClassNamingConventions")
public class ReportingConstants {
    // Parameters
    public static final String QUERY_PARAMS = "queryParams";
    // Targeting Tags
    public static final String MUST_CREATE_OBJECTIVE = "must_create_objective";
    public static final String HAS_OBJECTIVE_SUBMITTED = "has_objective_submitted";
    public static final String HAS_OBJECTIVE_APPROVED = "has_objective_approved";
    public static final String MUST_CREATE_MYR = "must_create_myr";
    public static final String HAS_MYR_APPROVED = "has_myr_approved";
    public static final String HAS_MYR_SUBMITTED = "has_myr_submitted";
    public static final String MUST_CREATE_EYR = "must_create_eyr";
    public static final String HAS_EYR_APPROVED = "has_eyr_approved";
    public static final String HAS_EYR_SUBMITTED = "has_eyr_submitted";
    public static final String MYR_WHAT_RATING = "myr_what_rating";
    public static final String MYR_HOW_RATING = "myr_how_rating";
    public static final String EYR_WHAT_RATING = "eyr_what_rating";
    public static final String EYR_HOW_RATING = "eyr_how_rating";
    public static final String HAS_EYR_APPROVED_1_QUARTER = "has_eyr_approved_1_quarter";
    public static final String HAS_EYR_APPROVED_2_QUARTER = "has_eyr_approved_2_quarter";
    public static final String HAS_EYR_APPROVED_3_QUARTER = "has_eyr_approved_3_quarter";
    public static final String HAS_EYR_APPROVED_4_QUARTER = "has_eyr_approved_4_quarter";
    public static final String IS_NEW_TO_BUSINESS = "is_new_to_business";
    public static final String HAS_FEEDBACK_REQUESTED = "has_feedback_requested";
    public static final String HAS_FEEDBACK_GIVEN = "has_feedback_given";
    // Ratings
    public static final String BELOW_EXPECTED_RATING = "Below expected";
    public static final String SATISFACTORY_RATING = "Satisfactory";
    public static final String GREAT_RATING = "Great";
    public static final String OUTSTANDING_RATING = "Outstanding";
}
