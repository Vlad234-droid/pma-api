package com.tesco.pma.dao.utils.jsonb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.api.MapProperties;

public class MapJsonbTypeHandler extends JsonbTypeHandler<MapProperties> {
    public MapJsonbTypeHandler(ObjectMapper objectMapper) {
        super(MapProperties.class, objectMapper);
    }
}
