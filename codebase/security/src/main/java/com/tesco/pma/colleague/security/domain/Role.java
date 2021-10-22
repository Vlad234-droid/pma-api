package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Used to give the user access to a particular resource under a particular role.
 */
@Data
public class Role {

    @JsonProperty("roleName")
    private String name;

    @JsonProperty("roleID")
    private String id;

    @JsonProperty("roleDescription")
    private String description;

    @JsonProperty("roleOwner")
    private String owner;

    @JsonProperty("parentRoleId")
    private String parentRoleId;

}


