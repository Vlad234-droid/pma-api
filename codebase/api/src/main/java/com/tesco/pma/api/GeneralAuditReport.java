package com.tesco.pma.api;

import com.tesco.pma.colleague.api.ColleagueSimple;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class GeneralAuditReport {
    ColleagueSimple updatedBy;
    Instant updatedTime;
}
