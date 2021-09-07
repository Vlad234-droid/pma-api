package com.tesco.organization_api.rest;


import com.tesco.organization_api.api.ColleagueOrganizationTree;
import com.tesco.organization_api.service.OrganizationService;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.service.cep.EventRequest;
import com.tesco.pma.service.colleague.client.model.Colleague;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;
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
    @GetMapping(value = "/departments/{departmentId}/colleagues/{colleagueUuid}/tree", produces = APPLICATION_JSON_VALUE)
    public RestResponse<ColleagueOrganizationTree> getColleagueOrganizationTree(@PathVariable String departmentId,
                                                                                @PathVariable UUID colleagueUuid) {
        return success(organizationService.getColleagueTree(colleagueUuid, departmentId));
    }

    @Operation(summary = "Update colleague organization after CEP event", tags = {"organization-api"})
    @ApiResponse(responseCode = HttpStatusCodes.TOO_MANY_REQUESTS, description = "Too Many Requests", content = @Content)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/events")
    public void startProcess(@RequestBody EventRequest<Colleague> eventRequest) {
        if (eventRequest.getPayload() == null) {
            log.warn(LogFormatter.formatMessage(ErrorCodes.EVENT_PAYLOAD_ERROR, "Invalid payload was received from CEP"));
            return;
        }
        organizationService.processCepEvent(eventRequest);
    }
}