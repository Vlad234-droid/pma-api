package com.tesco.pma.review.domain;

import com.tesco.pma.api.MapJson;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Review implements Serializable {
    private static final long serialVersionUID = 310609427305520535L;

    UUID performanceCycleUuid;
    UUID colleagueUuid;
    MapJson properties;
    String changeStatusReason;
}
