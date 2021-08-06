package com.tesco.pma.configuration.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tesco.pma.api.User;
import com.tesco.pma.api.security.SubsidiaryPermission;

import java.util.Collection;

@SuppressWarnings("PMD.ClassNamingConventions")
public abstract class UserMixIn extends User {
    @Override
    @JsonIgnoreProperties("colleagueUuid")
    public abstract Collection<SubsidiaryPermission> getSubsidiaryPermissions();

}
