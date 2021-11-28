package com.tesco.pma.colleague.security.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.AccountType;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Object roleId;
}
