package com.tesco.pma.cycle.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface PMCycleMappingService {

    /**
     * Get all PM Cycle keys
     * @return Set of keys
     * */
    Set<String> getPmCycleMappingKeys();

    /**
     * Get PM Cycle keys for the given list of colleagues
     * @param colleaguesUuids of colleague uuids
     * @return Map of keys per uuid
     * */
    Map<UUID, String> getPmCycleMappingKeys(List<UUID> colleaguesUuids);


}
