package com.tesco.pma.cycle.api;

import com.tesco.pma.colleague.api.Profile;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PMCycleUserProfile {
    private UUID uuid;
    private Profile profile;
}
