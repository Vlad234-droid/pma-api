package com.tesco.pma.organisation.rest;


import com.tesco.pma.organisation.api.OrganisationDictionary;
import com.tesco.pma.organisation.service.OrganisationDictionaryService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/organisations")
public class OrganisationDictionaryEndpoint {

    private final OrganisationDictionaryService service;


    @Operation(summary = "Get organisation dictionary by code",
            tags = {"organisation-dictionary"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the organisation dictionary")
    @GetMapping(value = "{code}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<OrganisationDictionary> findOrganisationDictionary(@PathVariable String code) {
        return RestResponse.success(service.findOrganisationDictionary(code));
    }

    @Operation(summary = "Get all organisation dictionaries", tags = {"organisation-dictionary"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "List of organisation dictionaries")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<OrganisationDictionary>> findAllOrganisationDictionaries() {
        return RestResponse.success(service.findAllOrganisationDictionaries());
    }


    @Operation(summary = "Create organisation dictionary", tags = {"organisation-dictionary"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Created organisation dictionary")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<OrganisationDictionary> create(@RequestBody OrganisationDictionary organisationDictionary) {
        return RestResponse.success(service.create(organisationDictionary));
    }

    @Operation(summary = "Update organisation dictionary", tags = {"organisation-dictionary"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Updated organisation dictionary")
    @PutMapping(value = "/{code}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public RestResponse<?> update(@PathVariable String code, @RequestBody OrganisationDictionary organisationDictionary) {
        organisationDictionary.setCode(code);
        return RestResponse.success(service.update(organisationDictionary));
    }

    @Operation(summary = "Delete organisation dictionary", tags = {"organisation-dictionary"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Deleted organisation dictionary")
    @DeleteMapping(value = "/{code}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<?> delete(@PathVariable String code) {
        service.delete(code);
        return RestResponse.success();
    }
}