package com.tesco.pma.service.colleague.client.model.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Position {
    String id;
    String code;
    String name;
    String teamName;
}
