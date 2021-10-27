package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;

@Data
@NoArgsConstructor
public class RemoveRoleRequest {
    @JsonProperty("accountName")
    private String accountName;
    @JsonProperty("role")
    private Collection<String> roles = new HashSet<>();
}
