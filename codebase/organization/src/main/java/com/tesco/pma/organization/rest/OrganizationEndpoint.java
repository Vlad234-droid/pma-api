package com.tesco.pma.organization.rest;


import com.tesco.pma.organization.service.OrganizationService;
import com.tesco.pma.rest.HttpStatusCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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


    @Operation(summary = "Get a colleague organization tree by department and colleague identifier",
            tags = {"organization-api"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the colleague organization tree")
    @GetMapping(value = "/business-units/{unitUuid}/tree", produces = APPLICATION_JSON_VALUE)
    public void getColleagueOrganizationTree(@PathVariable UUID unitUuid) {
    }

}