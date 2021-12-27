package com.tesco.pma.colleague.inbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateMessageRequestDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("sentAt")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime sentAt;

    @JsonProperty("senderName")
    private String senderName;

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    @JsonProperty("link")
    private String link;

    @JsonProperty("category")
    private MessageCategory category;

    @JsonProperty("recipients")
    private List<RecipientDto> recipients = new ArrayList<>();

    @JsonProperty("parameters")
    private List<ParameterDto> parameters = new ArrayList<>();
}
