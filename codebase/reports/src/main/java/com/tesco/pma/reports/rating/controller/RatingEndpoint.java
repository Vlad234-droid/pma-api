package com.tesco.pma.reports.rating.controller;


import com.tesco.pma.reports.rating.service.RatingService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RatingEndpoint {

    private final RatingService ratingService;

    @Operation(summary = "Get overall rating", tags = {"rating"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Rating calculated")
    @GetMapping(value = "/reports/overall-rating")
    public RestResponse<Map<String, String>> overall(@RequestParam("what_rating") String whatRating,
                                                     @RequestParam("how_rating") String howRating) {
        return RestResponse.success(Map.of("overall_rating", ratingService.getOverallRating(whatRating, howRating)));
    }

}
