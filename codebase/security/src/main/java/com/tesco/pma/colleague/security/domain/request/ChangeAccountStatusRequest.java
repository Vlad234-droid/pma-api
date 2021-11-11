package com.tesco.pma.colleague.security.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeAccountStatusRequest {
    @JsonProperty("accountName")
    private String name;
    @JsonProperty("status")
    private AccountStatus status;
}
