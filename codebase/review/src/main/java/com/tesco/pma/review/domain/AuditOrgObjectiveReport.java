package com.tesco.pma.review.domain;

import com.tesco.pma.api.ActionType;
import com.tesco.pma.api.GeneralAuditReport;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class AuditOrgObjectiveReport extends GeneralAuditReport {
    ActionType action;
}
