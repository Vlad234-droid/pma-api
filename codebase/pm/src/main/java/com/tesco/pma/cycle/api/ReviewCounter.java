package com.tesco.pma.cycle.api;

import com.tesco.pma.api.ReviewStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCounter {
    Long count;
    ReviewStatus status;
}
