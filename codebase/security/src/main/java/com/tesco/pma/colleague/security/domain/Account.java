package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Used to give the account to a particular resource under a particular role.
 */
@Data
public class Account {

    @JsonProperty("accountName")
    private String name;

    @JsonProperty("accountID")
    private String id;

    @JsonProperty("accountStatus")
    private String status;

    @JsonProperty("accountType")
    private String type;

    @JsonProperty("lastLogin")
    private String lastLogin;

    @JsonProperty("roleId")
    private String roleId;

    @JsonProperty("roleName")
    private String roleName;

    @JsonProperty("employeeNumber")
    private String employeeNumber;

}


