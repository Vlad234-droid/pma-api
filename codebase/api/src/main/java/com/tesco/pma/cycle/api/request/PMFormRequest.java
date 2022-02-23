package com.tesco.pma.cycle.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PMFormRequest {
    String id;
    String code;
    String key;
}
