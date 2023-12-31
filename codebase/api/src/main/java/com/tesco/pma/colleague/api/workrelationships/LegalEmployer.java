package com.tesco.pma.colleague.api.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LegalEmployer implements Serializable {

    private static final long serialVersionUID = 8726736011020554869L;

    Long id;
    String name;
}
