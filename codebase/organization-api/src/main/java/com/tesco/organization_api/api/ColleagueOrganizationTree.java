package com.tesco.organization_api.api;

import lombok.Data;

import java.util.Set;

@Data
public class ColleagueOrganizationTree {
    private PmaColleague colleague;
    private PmaColleague manager;
    private Set<PmaColleague> subordinates;
}
