package com.tesco.pma.reports.dashboard.domain;

import com.tesco.pma.colleague.api.ColleagueSimple;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class ColleagueReportTargeting extends ColleagueSimple {
    Map<String, String> tags;
}
