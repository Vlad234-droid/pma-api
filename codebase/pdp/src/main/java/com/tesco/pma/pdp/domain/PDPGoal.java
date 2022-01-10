package com.tesco.pma.pdp.domain;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.pdp.api.PDPGoalStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class PDPGoal implements Serializable {

    private static final long serialVersionUID = 6489001062784353323L;

    UUID uuid;
    UUID colleagueUuid;
    Integer number;
    MapJson properties;
    LocalDate achievementDate;
    PDPGoalStatus status;
}