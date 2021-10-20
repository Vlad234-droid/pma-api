package com.tesco.pma.objective.domain.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewBodyRequest {
    Map<String, String> reviewProperties;
    UUID groupObjectiveUuid;
}
