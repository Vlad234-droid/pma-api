package com.tesco.pma.config.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class ImportError {
    private UUID requestUuid;
    private UUID colleagueUuid;
    private String code;
    private String message;
}
