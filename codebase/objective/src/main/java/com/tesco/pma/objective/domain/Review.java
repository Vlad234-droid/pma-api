package com.tesco.pma.objective.domain;

import com.tesco.pma.api.MapProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Review {
    UUID uuid;
    UUID performanceCycleUuid;
    UUID colleagueUuid;
    ReviewType type;
    Integer number;
    MapProperties properties;
    ReviewStatus status;
}
