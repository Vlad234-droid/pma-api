package com.tesco.pma.organization.rest;


import com.tesco.pma.organization.api.BusinessUnit;
import com.tesco.pma.organization.api.BusinessUnitResponse;
import com.tesco.pma.organization.service.OrganizationService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/organizations")
public class OrganizationEndpoint {

    private final OrganizationService organizationService;


    @Operation(summary = "Get business unit structure by root identifier",
            tags = {"organization-api"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the business unit structure")
    @GetMapping(value = "/business-units/{unitUuid}/structure", produces = APPLICATION_JSON_VALUE)
    public RestResponse<BusinessUnitResponse> getBusinessUnitStructure(@PathVariable UUID unitUuid) {
        return RestResponse.success(organizationService.getStructure(unitUuid));
    }

    @Operation(summary = "Create business unit",
            tags = {"organization-api"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Created business unit")
    @PostMapping(value = "/business-units", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public RestResponse<?> create(@RequestBody BusinessUnit businessUnit) {
        organizationService.createBusinessUnit(businessUnit);
        return RestResponse.success();
    }

    @Operation(summary = "Publish business unit", tags = {"organization-api"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Structure has been published")
    @PostMapping(value = "/business-units/{unitUuid}/publish", produces = APPLICATION_JSON_VALUE)
    public RestResponse<?> publishBusinessUnitStructure(@PathVariable UUID unitUuid) {
        organizationService.publishUnit(unitUuid);
        return RestResponse.success();
    }

    @Operation(summary = "Unpublish business unit", tags = {"organization-api"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Structure has been unpublished")
    @DeleteMapping(value = "/business-units/{unitUuid}/publish", produces = APPLICATION_JSON_VALUE)
    public RestResponse<?> unpublishBusinessUnitStructure(@PathVariable UUID unitUuid) {
        organizationService.unpublishUnit(unitUuid);
        return RestResponse.success();
    }
}