package com.tesco.pma.service.colleague.client.model.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Department {
    String id;
    String name;
    String businessType;
}
