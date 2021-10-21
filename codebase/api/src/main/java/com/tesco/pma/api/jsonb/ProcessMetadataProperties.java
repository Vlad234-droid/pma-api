package com.tesco.pma.api.jsonb;

import com.tesco.pma.api.Jsonb;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class ProcessMetadataProperties implements Jsonb {

    public static final String PROCESS_ELEMENTS = "process_elements";

    private final List<MetadataElement> processMetadataElements;

    @Data
    @AllArgsConstructor
    public static class MetadataElement {
        private String id;
        private String code;
        private String description;
        private String type;
        private String properties;
    }
}
