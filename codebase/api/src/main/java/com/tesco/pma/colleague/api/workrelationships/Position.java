package com.tesco.pma.colleague.api.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Position implements Serializable {

    private static final long serialVersionUID = 1L;

    String id;
    String code;
    String name;
    String teamName;
}
