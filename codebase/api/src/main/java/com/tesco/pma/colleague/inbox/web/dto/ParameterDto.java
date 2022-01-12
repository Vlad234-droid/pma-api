package com.tesco.pma.colleague.inbox.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Colleague Inbox API
 * ${COLLEAGUE_INBOX_API_URL}/v1/messages
 * Parameters for a message to send to a colleague by Colleague Inbox
 */
@Getter
@Setter
public class ParameterDto implements Serializable {

    private static final long serialVersionUID = 4590301136843472185L;

    @JsonProperty("name")
    private String name;

    @JsonProperty("colleagueId")
    private UUID colleagueId;
}
