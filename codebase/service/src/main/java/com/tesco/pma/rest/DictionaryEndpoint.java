package com.tesco.pma.rest;


import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.service.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/dictionaries")
public class DictionaryEndpoint {

    private final DictionaryService service;

    @Operation(summary = "Get dictionary item", tags = {"dictionaries"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the dictionary item")
    @GetMapping("/{dictionary}/{id}")
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<GeneralDictionaryItem> read(@PathVariable String dictionary, @PathVariable Integer id) {
        return RestResponse.success(service.read(dictionary, id));
    }

    @Operation(summary = "Get dictionary items", tags = {"dictionaries"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "List of the dictionary items")
    @GetMapping("/{dictionary}")
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<List<GeneralDictionaryItem>> findAll(@PathVariable String dictionary, @RequestParam(required = false) String code) {
        List<GeneralDictionaryItem> result;
        if (code == null) {
            result = service.findAll(dictionary);
        } else {
            result = Collections.singletonList(service.findByCode(dictionary, code));
        }
        return RestResponse.success(result);
    }

}