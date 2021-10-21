package com.tesco.pma.dao.utils.jsonb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.api.MapJson;

public class MapJsonbTypeHandler extends JsonbTypeHandler<MapJson> {
    public MapJsonbTypeHandler(ObjectMapper objectMapper) {
        super(MapJson.class, objectMapper);
    }
}
