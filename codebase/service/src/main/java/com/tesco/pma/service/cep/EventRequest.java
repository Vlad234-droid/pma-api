package com.tesco.pma.service.cep;

import com.tesco.pma.api.Identified;
import lombok.Value;

@Value
public class EventRequest<T extends Identified<?>> {
    EventMetadata metadata;
    T payload;
}
