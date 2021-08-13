package com.tesco.pma.service.colleague.client.model.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Grade {
    String id;
    String code;
}
