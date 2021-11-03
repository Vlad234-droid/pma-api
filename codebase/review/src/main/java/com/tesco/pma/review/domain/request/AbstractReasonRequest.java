package com.tesco.pma.review.domain.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractReasonRequest {
    String reason;
}
