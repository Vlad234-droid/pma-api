package com.tesco.pma.process.rest;


import com.tesco.pma.process.service.RatingService;
import com.tesco.pma.rest.HttpStatusCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/rating")
public class RatingEndpoint {

    private final RatingService ratingService;

    @Operation(summary = "Get overall rating",
            tags = {"rating"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Rating calculated")
    @GetMapping(value = "/overall")
    public String overall(@RequestParam("what_rating") String whatRating, @RequestParam("how_rating") String howRating){
        return ratingService.getOverallRating(whatRating, howRating);
    }

}
