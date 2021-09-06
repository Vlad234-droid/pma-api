package com.tesco.pma.colleague.profile.service.rest.model.colleague.workrelationships;

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
