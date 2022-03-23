package com.tesco.pma.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapJson implements Jsonb, Serializable {
    private static final long serialVersionUID = -4692785779291237336L;

    private Map<String, String> mapJson;
}
