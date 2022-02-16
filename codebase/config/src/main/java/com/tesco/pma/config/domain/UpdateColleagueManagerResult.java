package com.tesco.pma.config.domain;

import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
public class UpdateColleagueManagerResult {

    Map<UUID, UUID> managerUpdated;
    Map<UUID, UUID> managerIsNotPresent;
}
