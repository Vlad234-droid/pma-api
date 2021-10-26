package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DisableAccountRequest {
    @JsonProperty("accountName")
    private String name;
    @JsonProperty("status")
    private String status;
}
