package com.tesco.pma.dao.utils.jsonb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.api.jsonb.ProcessMetadataProperties;

public class ProcessMetadataJsonbTypeHandler extends JsonbTypeHandler<ProcessMetadataProperties> {

    public ProcessMetadataJsonbTypeHandler(ObjectMapper objectMapper) {
        super(ProcessMetadataProperties.class, objectMapper);
    }
}
