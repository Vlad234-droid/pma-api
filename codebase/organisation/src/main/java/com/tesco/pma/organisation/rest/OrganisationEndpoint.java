package com.tesco.pma.organisation.rest;


import com.tesco.pma.organisation.api.BusinessUnit;
import com.tesco.pma.organisation.api.BusinessUnitResponse;
import com.tesco.pma.organisation.service.OrganisationService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/organisations")
public class OrganisationEndpoint {

    private final OrganisationService organisationService;


    @Operation(summary = "Get business unit structure by root identifier",
            tags = {"organisation"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the business unit structure")
    @GetMapping(value = "/business-units/{unitUuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<BusinessUnitResponse> getBusinessUnitStructure(@PathVariable UUID unitUuid) {
        return RestResponse.success(organisationService.getStructure(unitUuid));
    }

    @Operation(summary = "Get business unit structure by composite key",
            tags = {"organisation"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the business unit structure")
    @GetMapping(value = "/business-units", produces = APPLICATION_JSON_VALUE)
    public RestResponse<BusinessUnitResponse> getBusinessUnitStructureByCompositeKey(@RequestParam String compositeKey) {
        return RestResponse.success(organisationService.getPublishedChildStructureByCompositeKey(compositeKey));
    }

    @Operation(summary = "Create business unit",
            tags = {"organisation"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Created business unit")
    @PostMapping(value = "/business-units", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public RestResponse<?> create(@RequestBody BusinessUnit businessUnit) {
        organisationService.createBusinessUnit(businessUnit);
        return RestResponse.success();
    }

    @Operation(summary = "Update business unit",
            tags = {"organisation"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Updated business unit")
    @PostMapping(value = "/business-units/{unitUuid}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public RestResponse<?> update(@PathVariable UUID unitUuid, @RequestBody BusinessUnit businessUnit) {
        businessUnit.setUuid(unitUuid);
        organisationService.updateBusinessUnit(businessUnit);
        return RestResponse.success();
    }

    @Operation(summary = "Publish business unit", tags = {"organisation"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Structure has been published")
    @PostMapping(value = "/business-units/{unitUuid}/publish", produces = APPLICATION_JSON_VALUE)
    public RestResponse<?> publishBusinessUnitStructure(@PathVariable UUID unitUuid) {
        organisationService.publishUnit(unitUuid);
        return RestResponse.success();
    }

    @Operation(summary = "Unpublish business unit", tags = {"organisation"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Structure has been unpublished")
    @DeleteMapping(value = "/business-units/{unitUuid}/publish", produces = APPLICATION_JSON_VALUE)
    public RestResponse<?> unpublishBusinessUnitStructure(@PathVariable UUID unitUuid) {
        organisationService.unpublishUnit(unitUuid);
        return RestResponse.success();
    }
}