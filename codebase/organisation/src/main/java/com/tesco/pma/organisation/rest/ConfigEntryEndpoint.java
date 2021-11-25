package com.tesco.pma.organisation.rest;


import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.ConfigEntryResponse;
import com.tesco.pma.organisation.service.ConfigEntryService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/config-entries")
public class ConfigEntryEndpoint {

    private final ConfigEntryService configEntryService;


    @Operation(summary = "Get unpublished config entry structure by root identifier",
            tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the config entry structure")
    @GetMapping(value = "{entryUuid}/unpublished", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole() and !isColleague()")
    public RestResponse<ConfigEntryResponse> getUnpublishedEntryConfigStructure(@PathVariable UUID entryUuid) {
        return RestResponse.success(configEntryService.getUnpublishedStructure(entryUuid));
    }

    @Operation(summary = "Get published config entry structure by root identifier",
            tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the config entry structure")
    @GetMapping(value = "{entryUuid}", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole() and !isColleague()")
    public RestResponse<ConfigEntryResponse> getPublishedEntryConfigStructure(@PathVariable UUID entryUuid) {
        return RestResponse.success(configEntryService.getPublishedStructure(entryUuid));
    }

    @Operation(summary = "Get published config entry structure by composite key", tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the config entry structure")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole() and !isColleague()")
    public RestResponse<List<ConfigEntryResponse>> getPublishedEntryConfigStructureByCompositeKey(@RequestParam String compositeKey) {
        return RestResponse.success(configEntryService.getPublishedChildStructureByCompositeKey(compositeKey));
    }

    @Operation(summary = "Get all unpublished root config entries ", tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the config entry structure")
    @GetMapping(value = "roots/unpublished", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole() and !isColleague()")
    public RestResponse<List<ConfigEntryResponse>> getUnpublishedRoots() {
        return RestResponse.success(configEntryService.getUnpublishedRoots());
    }

    @Operation(summary = "Get all published root config entries ", tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the config entry structure")
    @GetMapping(value = "roots", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole() and !isColleague()")
    public RestResponse<List<ConfigEntryResponse>> getPublishedRoots() {
        return RestResponse.success(configEntryService.getPublishedRoots());
    }

    @Operation(summary = "Get unpublished structure by composite key", tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the config entry structure")
    @GetMapping(value = "unpublished", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole() and !isColleague()")
    public RestResponse<List<ConfigEntryResponse>> getUnpublished(@RequestParam String compositeKey) {
        return RestResponse.success(configEntryService.getUnpublishedChildStructureByCompositeKey(compositeKey));
    }


    @Operation(summary = "Create config entry",
            tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Created config entry")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAdmin()")
    public RestResponse<?> create(@RequestBody ConfigEntry configEntry) {
        configEntryService.createConfigEntry(configEntry);
        return RestResponse.success();
    }

    @Operation(summary = "Update config entry",
            tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Updated config entry")
    @PutMapping(value = "/{entryUuid}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAdmin()")
    public RestResponse<?> update(@PathVariable UUID entryUuid, @RequestBody ConfigEntry configEntry) {
        configEntry.setUuid(entryUuid);
        configEntryService.updateConfigEntry(configEntry);
        return RestResponse.success();
    }

    @Operation(summary = "Delete config entry",
            tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Delete config entry")
    @DeleteMapping(value = "/{entryUuid}", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAdmin()")
    public RestResponse<?> delete(@PathVariable UUID entryUuid) {
        configEntryService.deleteConfigEntry(entryUuid);
        return RestResponse.success();
    }

    @Operation(summary = "Publish config entry", tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Structure has been published")
    @PostMapping(value = "/{entryUuid}/publish", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAdmin()")
    public RestResponse<?> publishEntryConfigStructure(@PathVariable UUID entryUuid) {
        configEntryService.publishConfigEntry(entryUuid);
        return RestResponse.success();
    }

    @Operation(summary = "Unpublish config entry", tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Structure has been unpublished")
    @DeleteMapping(value = "/{entryUuid}/publish", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAdmin()")
    public RestResponse<?> unpublishEntryConfigStructure(@PathVariable UUID entryUuid) {
        configEntryService.unpublishConfigEntry(entryUuid);
        return RestResponse.success();
    }

    @Operation(summary = "Get colleagues by composite key", tags = {"config-entry"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Colleagues list")
    @GetMapping(value = "/colleagues", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAdmin()")
    public RestResponse<List<ColleagueEntity>> findColleaguesByCompositeKey(@RequestParam String compositeKey) {
        return RestResponse.success(configEntryService.findColleaguesByCompositeKey(compositeKey));
    }
}