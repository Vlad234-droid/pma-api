package com.tesco.pma.review.domain;

import com.tesco.pma.api.MapJson;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class Review extends SimplifiedReview implements Serializable {
    private static final long serialVersionUID = 310609427305520535L;

    UUID tlPointUuid;
    MapJson properties;
    Instant lastUpdatedTime;
    String changeStatusReason;
}
