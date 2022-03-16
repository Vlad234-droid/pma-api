package com.tesco.pma.cep.cfapi.v2.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Subcategory {

    @JsonProperty("newJoiner")
    NEW_JOINER("newJoiner"),

    @JsonProperty("importedInCF")
    IMPORTED_IN_CF("importedInCF"),

    @JsonProperty("reinstatement")
    REINSTATEMENT("reinstatement"),

    @JsonProperty("departmentUpdate")
    DEPARTMENT_UPDATE("departmentUpdate"),

    @JsonProperty("jobUpdate")
    JOB_UPDATE("jobUpdate"),

    @JsonProperty("positionUpdate")
    POSITION_UPDATE("positionUpdate"),

    @JsonProperty("profileUpdate")
    PROFILE_UPDATE("profileUpdate"),

    @JsonProperty("contactUpdate")
    CONTACT_UPDATE("contactUpdate"),

    @JsonProperty("skillsUpdate")
    SKILLS_UPDATE("skillsUpdate"),

    @JsonProperty("externalSystemsUpdate")
    EXTERNAL_SYSTEM_UPDATE("externalSystemsUpdate"),

    @JsonProperty("contractUpdate")
    CONTRACT_UPDATE("contractUpdate"),

    @JsonProperty("workRelationshipUpdate")
    WORK_RELATIONSHIP_UPDATE("workRelationshipUpdate"),

    @JsonProperty("other")
    OTHER_UPDATE("other"),

    @JsonProperty("sourceSystemUpdate")
    SOURCE_SYSTEM_UPDATE("sourceSystemUpdate");

    private final String subcategoryName;

    Subcategory(String subcategoryName) {
        this.subcategoryName = subcategoryName;
    }

    public String subcategoryName() {
        return subcategoryName;
    }

}
