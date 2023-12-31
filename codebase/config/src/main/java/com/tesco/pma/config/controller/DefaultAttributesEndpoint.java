package com.tesco.pma.config.controller;

import com.tesco.pma.config.service.DefaultAttributesService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/config")
public class DefaultAttributesEndpoint {

    private final DefaultAttributesService defaultAttributesService;

    @Operation(summary = "Update default attributes for a colleague", tags = {"Config"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Update default attributes for a colleague")
    @PutMapping(path = "/default/attributes/colleagues/{colleagueUuid}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAdmin() or isTalentAdmin()")
    public RestResponse<Void> updateDefaultAttributes(@PathVariable("colleagueUuid") UUID colleagueUuid) {
        defaultAttributesService.updateDefaultAttributes(colleagueUuid);
        return RestResponse.success();
    }


}
