package com.tesco.pma.colleague.profile.rest.model.colleague.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobResponse {
    String id;
    String code;
    String name;
    String costCategory;
}
