package com.tesco.pma.reporting.dashboard.domain;

import com.tesco.pma.colleague.api.ColleagueSimple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class ColleagueReportTargeting extends ColleagueSimple {
    private Map<String, String> tags;
}
