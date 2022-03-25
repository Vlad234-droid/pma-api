package com.tesco.pma.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapJson implements Jsonb, Serializable {
    private static final long serialVersionUID = -4692785779291237336L;

    private Map<String, String> mapJson;
}
