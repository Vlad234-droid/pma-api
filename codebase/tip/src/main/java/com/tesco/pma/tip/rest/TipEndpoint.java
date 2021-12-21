package com.tesco.pma.tip.rest;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.tip.api.Tip;
import com.tesco.pma.tip.service.TipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing {@link Tip}.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class TipEndpoint {

    private final TipService tipService;

    /**
     * {@code POST  /tips} : Create or update a tip.
     *
     * @param tip the tip to create.
     * @return the {@link RestResponse} with status {@code 201 (Created)} and with body the new tip,
     * or with status {@code 400 (Bad Request)} if the tip has already an key.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/tips")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new tip or create a new version of an existing tip", tags = {"tip"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "New Tip successfully created.")
    @PreAuthorize("isColleague()")
    public RestResponse<Tip> create(@Valid @RequestBody Tip tip) throws URISyntaxException {
        log.debug("REST request to save Tip : {}", tip);
        return RestResponse.success(tipService.create(tip));
    }

    /**
     * {@code GET  /tips} : get all the tips.
     *
     * @param requestQuery filter, sort, offset
     * @return the {@link RestResponse} with status {@code 200 (OK)} and the list of tips in body.
     */
    @GetMapping("/tips")
    @Operation(summary = "Get tips", tags = {"tip"})
    @PreAuthorize("isColleague()")
    public RestResponse<List<Tip>> read(@Parameter(example = "{\n"
            + "    \"_sort\": \"title:DESC,updated-time:ASC\",\n"
            + "    \"published\": \"true\",\n"
            + "    \"organization-key_ne\": \"l1/group/l2/ho_c/l3/salaried/l4/wl5/#v1\",\n"
            + "    \"title_contains\": \"A\",\n"
            + "    \"description_ncontains\": \"B\",\n"
            + "    \"key_in\": [\"com.tesco.pma.tip\",\"com.tesco.pma.review\"],\n"
            + "    \"organization-name_nin\": [\"WL1\",\"WL2\"],\n"
            + "    \"created-time_lt\": \"2021-11-26T14:18:42.615Z\",\n"
            + "    \"created-time_lte\": \"2021-11-26T14:18:42.615Z\",\n"
            + "    \"updated-time_gt\": \"2021-11-25T14:36:33.587Z\",\n"
            + "    \"updated-time_gte\": \"2021-11-25T14:36:33.587Z\",\n"
            + "    \"organization-name_null\": \"true\",\n"
            + "    \"_start\": \"1\",\n"
            + "    \"_limit\": \"7\",\n"
            + "    \"_search\": \"A\"\n"
            + "  }") RequestQuery requestQuery) {
        log.debug("REST request to get a Tips");
        return RestResponse.success(tipService.findAll(requestQuery));
    }

    /**
     * {@code GET  /tips/:uuid} : get the "uuid" tip.
     *
     * @param uuid the uuid of the tip to retrieve.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the tip, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/tips/{uuid}")
    @Operation(summary = "Get tip by uuid", tags = {"tip"})
    @PreAuthorize("isColleague()")
    public RestResponse<Tip> read(@PathVariable final UUID uuid) {
        log.debug("REST request to get Tip : {}", uuid);
        return RestResponse.success(tipService.findOne(uuid));
    }

    /**
     * {@code GET  /tips/:uuid/history} : get the history of tip.
     *
     * @param uuid of the tip to retrieve.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the list of tips.
     */
    @GetMapping("/tips/{uuid}/history")
    @Operation(summary = "Get tip history", tags = {"tip"})
    @PreAuthorize("isColleague()")
    public RestResponse<List<Tip>> readHistory(@PathVariable final UUID uuid) {
        log.debug("REST request to get Tip history : {}", uuid);
        return RestResponse.success(tipService.findHistory(uuid));
    }

    /**
     * {@code DELETE  /tips/:uuid} : Delete an existing tip.
     *
     * @param uuid the uuid of the tip to delete.
     * @return the {@link RestResponse} with status {@code 204 (NO_CONTENT)}.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @DeleteMapping("/tips/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an existing Tip", tags = {"tip"})
    @ApiResponse(responseCode = HttpStatusCodes.NO_CONTENT, description = "Tip successfully deleted.")
    @PreAuthorize("isColleague()")
    public RestResponse<Void> delete(@PathVariable final UUID uuid, @RequestParam(required = false) boolean withHistory)
            throws URISyntaxException {
        log.debug("REST request to delete Tip: {}", uuid);
        tipService.delete(uuid, withHistory);
        return RestResponse.success();
    }

    /**
     * {@code PATCH  /tips/:uuid/publish} : Publish tip
     *
     * @param uuid the uuid of the tip.
     * @return the {@link RestResponse} with status {@code 204 (No content)},
     * or with status {@code 404 (Not Found)} if the tip is not found.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/tips/{uuid}/publish")
    @Operation(summary = "Publish tip", tags = {"tip"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Tip published successfully.")
    @PreAuthorize("isColleague()")
    public RestResponse<Tip> publish(@PathVariable final UUID uuid) throws URISyntaxException {
        log.debug("REST request to publish Tip : {}", uuid);
        return RestResponse.success(tipService.publish(uuid));
    }

}
