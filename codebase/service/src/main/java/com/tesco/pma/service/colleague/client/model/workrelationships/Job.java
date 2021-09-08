package com.tesco.pma.service.colleague.client.model.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.ShortClassName")
public class Job {
    String id;
    String code;
    String name;
    String costCategory;
}
