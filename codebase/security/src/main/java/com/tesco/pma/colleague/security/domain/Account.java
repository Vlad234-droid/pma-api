package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tesco.pma.api.Identified;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * Used to give the account to a particular resource under a particular role.
 *
 * <p>For more information:
 *  @see <a href="https://github.dev.global.tesco.org/97-TeamTools/Colleague-Authentication-and-Access/wiki/Data-Collections">here</a>
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Account implements Identified<Long> {

    private Long id;

    @JsonProperty("accountName")
    private String name;

    @JsonProperty("accountType")
    private AccountType type;

    @JsonProperty("iamId")
    private String iamId;

    @JsonProperty("status")
    private AccountStatus status;

    @JsonProperty("lastLogin")
    private Instant lastLogin;

    @JsonProperty("employeeNumber")
    private String employeeNumber;

    @JsonProperty(value = "role")
    private String role;

    @JsonProperty(value = "roles")
    private Collection<Role> roles = new HashSet<>();

}


