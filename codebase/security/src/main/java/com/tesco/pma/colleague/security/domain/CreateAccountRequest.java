package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;

@Data
@NoArgsConstructor
public class CreateAccountRequest {

    @JsonProperty("accountName")
    private String name;

    @JsonProperty("accountType")
    private AccountType type;

    @JsonProperty("iamId")
    private String iamId;

    @JsonProperty("status")
    private AccountStatus status;

    @JsonProperty("role")
    private Collection<String> roles = new HashSet<>();

}
