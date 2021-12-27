package com.tesco.pma.colleague.inbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class RecipientDto {

    @JsonProperty("recipientUuid")
    private UUID recipientUuid;
}

