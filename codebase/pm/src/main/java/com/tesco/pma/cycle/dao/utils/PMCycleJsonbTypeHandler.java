package com.tesco.pma.cycle.dao.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.dao.utils.jsonb.JsonbTypeHandler;

public class PMCycleJsonbTypeHandler extends JsonbTypeHandler<PMCycleMetadata> {

    public PMCycleJsonbTypeHandler(ObjectMapper objectMapper) {
        super(PMCycleMetadata.class, objectMapper);
    }
}
