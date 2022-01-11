package com.tesco.pma.colleague.api.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.ShortClassName")
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    String id;
    String code;
    String name;
    String costCategory;
}
