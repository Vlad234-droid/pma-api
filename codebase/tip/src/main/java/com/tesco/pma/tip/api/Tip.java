package com.tesco.pma.tip.api;

import com.tesco.pma.fs.domain.File;
import com.tesco.pma.organisation.api.ConfigEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class Tip {

    private UUID uuid;

    @Schema(description = "Free text field title.", required = true,
            example = "Do you know?")
    private String title;

    @Schema(description = "Free text field description.", required = true,
            example = "That you can submit new objectives at any time during the performance cycle?")
    private String description;

    @Schema(description = "Target organisation tree level.", required = true)
    private ConfigEntry targetOrganisation;

    @Schema(description = "Image.", required = true)
    private File image;

    @Schema(description = "Published checkbox.")
    private Boolean published = Boolean.FALSE;

    @Schema(description = "Last 5 records of date and hour when the Tip was published.")
    private Set<Instant> history = new HashSet<>();

    @Schema(defaultValue = "Now.")
    private Instant createdTime = Instant.now();

    @Schema(defaultValue = "Now.")
    private Instant updatedTime = Instant.now();
}
