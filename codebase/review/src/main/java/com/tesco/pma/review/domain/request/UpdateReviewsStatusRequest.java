package com.tesco.pma.review.domain.request;

import com.tesco.pma.review.domain.Review;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
public class UpdateReviewsStatusRequest extends AbstractReasonRequest {
    List<Review> reviews;
}
