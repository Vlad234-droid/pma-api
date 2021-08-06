package com.tesco.pma.service.cep;

import lombok.Value;

@Value
public class EventMetadata {
    String eventId;
    String feedId;
    String classifier;
}
