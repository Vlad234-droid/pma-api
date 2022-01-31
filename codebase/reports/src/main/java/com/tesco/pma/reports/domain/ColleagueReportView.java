package com.tesco.pma.reports.domain;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.colleague.api.ColleagueSimple;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class ColleagueReportView extends ColleagueSimple {
    MapJson tags;
}
