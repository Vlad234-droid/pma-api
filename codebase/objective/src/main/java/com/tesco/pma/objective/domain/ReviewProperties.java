package com.tesco.pma.objective.domain;

import com.tesco.pma.api.Jsonb;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ReviewProperties implements Jsonb {

    private final Map<String, String> reviewProperties;
}
