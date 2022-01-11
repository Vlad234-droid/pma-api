package com.tesco.colleague.inbox.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Colleague Inbox API
 * ${COLLEAGUE_INBOX_API_URL}/v1/messages
 * Information about recipient of a message to send to a colleague by Colleague Inbox
 */
@Getter
@Setter
public class RecipientDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("recipientUuid")
    private UUID recipientUuid;
}

