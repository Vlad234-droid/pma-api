package com.tesco.pma.colleague.security.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RemoveRoleRequest {
    @JsonProperty("accountName")
    private String accountName;
    @JsonProperty("role")
    private Object role;
}
