package com.tesco.pma.colleague.api.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Department implements Serializable {

    private static final long serialVersionUID = -6989177427521322603L;

    String id;
    String name;
    String businessType;
}
