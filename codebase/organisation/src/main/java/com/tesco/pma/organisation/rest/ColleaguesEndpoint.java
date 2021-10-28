package com.tesco.pma.organisation.rest;


import com.tesco.pma.organisation.api.Colleague;
import com.tesco.pma.organisation.service.SearchColleaguesService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/colleagues")
public class ColleaguesEndpoint {

    private final SearchColleaguesService searchColleaguesService;

    @Operation(summary = "Autocomplete search among colleagues by full name", tags = {"colleagues"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Search among colleagues by full name")
    @GetMapping(value = "/suggestions", produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<Colleague>> getSuggestionsFullName(@RequestParam String fullName) {
        return RestResponse.success(searchColleaguesService.getAllSuggestions(fullName));
    }

    @Operation(summary = "Autocomplete search among subordinate colleagues by full name", tags = {"colleagues"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Search among colleagues by full name")
    @GetMapping(value = "/suggestions/subordinates", produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<Colleague>> getSuggestionsAmongSubordinatesFullName(@RequestParam String fullName) {
        return RestResponse.success(searchColleaguesService.getSuggestionsSubordinates(fullName));
    }

}
