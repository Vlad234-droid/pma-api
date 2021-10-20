package com.tesco.pma.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class MapProperties implements Jsonb {

    private final Map<String, String> mapProperties;
}
