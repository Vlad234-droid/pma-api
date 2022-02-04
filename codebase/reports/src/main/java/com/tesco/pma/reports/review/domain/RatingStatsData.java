package com.tesco.pma.reports.review.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Rating statistics data
 */
@EqualsAndHashCode
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingStatsData {
    Integer ratingPercentage;
    Long ratingCount;

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum OverallRating implements DictionaryItem<Integer> {
        BELOW_EXPECTED(1, "Below expected"),
        SATISFACTORY(2, "Satisfactory"),
        GREAT(3, "Great"),
        OUTSTANDING(4, "Outstanding");

        private Integer id;
        private String description;

        @Override
        public String getCode() {
            return name();
        }
    }
}