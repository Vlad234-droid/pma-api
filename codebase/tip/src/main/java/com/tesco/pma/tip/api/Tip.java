package com.tesco.pma.tip.api;

import com.tesco.pma.organisation.api.ConfigEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@SuppressWarnings("PMD.ShortClassName")
public class Tip implements Serializable {

    private static final long serialVersionUID = -8380094330281630022L;

    @Schema(description = "Backend field for DB record.")
    private UUID pkUuid;

    @Schema(description = "Backend field for managing history.")
    private Integer version = 1;

    @Schema(description = "Key for managing tip with versions.")
    private UUID uuid;

    @Schema(description = "Title.", required = true,
            example = "Do you know?")
    private String title;

    @Schema(description = "Description.", required = true,
            example = "That you can submit new objectives at any time during the performance cycle?")
    private String description;

    @Schema(description = "Target organisation tree level.", required = true)
    private ConfigEntry targetOrganisation;

    @Schema(description = "Image link.", required = true,
            example = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")
    private String imageLink;

    @Schema(description = "Published checkbox.", example = "false")
    private Boolean published = Boolean.FALSE;

    @Schema(description = "All history of tip's changing.")
    private List<Tip> history;

    @Schema(defaultValue = "Now.")
    private Instant createdTime;

    @Schema(defaultValue = "Now.")
    private Instant updatedTime;
}
