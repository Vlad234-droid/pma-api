package com.tesco.pma.service.config.rest;


import com.tesco.pma.api.Subsidiary;
import com.tesco.pma.service.config.SubsidiaryService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Subsidiary service endpoint.
 */
@RestController
@RequestMapping(path = "/subsidiaries")
@Validated
public class SubsidiaryEndpoint {

    @Autowired
    SubsidiaryService subsidiaryService;

    /**
     * POST call to create a Subsidiary.
     *
     * @param subsidiary a Subsidiary
     * @return a RestResponse parameterized with Subsidiary
     */
    @Operation(summary = "Create a Subsidiary", description = "Subsidiary created", tags = {"subsidiary"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Validated({ValidationGroup.WithoutId.class, Default.class})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<Subsidiary> createSubsidiary(@RequestBody @Valid Subsidiary subsidiary) {
        return success(subsidiaryService.createSubsidiary(subsidiary));
    }

    /**
     * GET to return list of Subsidiaries as JSON.
     *
     * @return a RestResponse parameterized with list of Subsidiary
     */
    @Operation(summary = "Get all Subsidiaries", description = "Get all Subsidiaries", tags = {"subsidiary"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Successful operation")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<Subsidiary>> getSubsidiaries() {
        return success(subsidiaryService.getSubsidiaries());
    }

    /**
     * Get call using a Path param and return a Subsidiary as JSON.
     *
     * @param subsidiaryUuid an identifier
     * @return a RestResponse parameterized with Subsidiary
     */
    @Operation(summary = "Get a Subsidiary by its uuid", tags = {"subsidiary"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Subsidiary")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Subsidiary not found", content = @Content)
    @GetMapping(path = "/{subsidiaryUuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Subsidiary> getSubsidiary(@PathVariable("subsidiaryUuid") @NotNull UUID subsidiaryUuid) {
        return success(subsidiaryService.getSubsidiary(subsidiaryUuid));
    }


    /**
     * PUT call to update a Subsidiary.
     *
     * @param subsidiaryUuid an identifier
     * @param subsidiary     a Subsidiary
     * @return a RestResponse parameterized with Subsidiary
     */
    @Operation(summary = "Update existing Subsidiary", description = "Update existing Subsidiary", tags = {"subsidiary"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Subsidiary updated")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Subsidiary not found", content = @Content)
    @PutMapping(path = "/{subsidiaryUuid}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Validated({ValidationGroup.WithoutId.class, Default.class})
    public RestResponse<Subsidiary> updateSubsidiary(@PathVariable("subsidiaryUuid") @NotNull UUID subsidiaryUuid,
                                                     @RequestBody @Valid Subsidiary subsidiary) {
        subsidiary.setUuid(subsidiaryUuid);
        return success(subsidiaryService.updateSubsidiary(subsidiary));
    }

    /**
     * DELETE call to delete a Subsidiary.
     *
     * @param subsidiaryUuid an identifier
     * @return a RestResponse with success field of boolean value
     */
    @Operation(summary = "Delete existing Subsidiary", description = "Delete existing Subsidiary", tags = {"subsidiary"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Subsidiary deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Subsidiary not found", content = @Content)
    @DeleteMapping(path = "/{subsidiaryUuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Void> deleteSubsidiary(@PathVariable("subsidiaryUuid") @NotNull UUID subsidiaryUuid) {
        subsidiaryService.deleteSubsidiary(subsidiaryUuid);
        return success();
    }

}
