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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;

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
     * {@code POST  /tips} : Create a new tip.
     *
     * @param tip the tip to create.
     * @return the {@link RestResponse} with status {@code 201 (Created)} and with body the new tip,
     * or with status {@code 400 (Bad Request)} if the tip has already an key.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/tips")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new list of tip with items", tags = {"tip"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "New Tip successfully created.")
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
     * {@code GET  /tips/:key} : get the "key" tip.
     *
     * @param key the key of the tip to retrieve.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the tip, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/tips/{key}")
    @Operation(summary = "Get tip by key", tags = {"tip"})
    public RestResponse<Tip> read(@PathVariable final String key) {
        log.debug("REST request to get Tip : {}", key);
        return RestResponse.success(tipService.findOne(key));
    }

    /**
     * {@code GET  /tips/:key/history} : get the history by "key" tip.
     *
     * @param key the key of the tip to retrieve.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the list of tips.
     */
    @GetMapping("/tips/{key}/history")
    @Operation(summary = "Get tip history by key", tags = {"tip"})
    public RestResponse<List<Tip>> readHistory(@PathVariable final String key) {
        log.debug("REST request to get Tip history : {}", key);
        return RestResponse.success(tipService.findHistory(key));
    }

    /**
     * {@code PUT  /tips/:key} : Updates an existing tip.
     *
     * @param key the key of the tip to save.
     * @param tip  the tip to update.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the updated tip,
     * or with status {@code 400 (Bad Request)} if the tip is not valid.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/tips/{key}")
    @Operation(summary = "Updates an existing Tip", tags = {"tip"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Tip updated successfully.")
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid key")
    public RestResponse<Tip> update(@PathVariable final String key, @Valid @RequestBody Tip tip) throws URISyntaxException {
        log.debug("REST request to update Tip : {}, {}", key, tip);
        tip.setKey(key);
        return RestResponse.success(tipService.update(tip));
    }

    /**
     * {@code DELETE  /tips/:key} : Delete an existing tip.
     *
     * @param key the key of the tip to delete.
     * @return the {@link RestResponse} with status {@code 204 (NO_CONTENT)}.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @DeleteMapping("/tips/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an existing Tip", tags = {"tip"})
    @ApiResponse(responseCode = HttpStatusCodes.NO_CONTENT, description = "Tip successfully deleted.")
    public RestResponse<Void> delete(@PathVariable final String key) {
        log.debug("REST request to delete Tip with key: {}", key);
        tipService.delete(key);
        return RestResponse.success();
    }

    /**
     * {@code PATCH  /tips/:key/publish} : Publish tip
     *
     * @param key the key of the tip.
     * @return the {@link RestResponse} with status {@code 204 (No content)},
     * or with status {@code 404 (Not Found)} if the tip is not found.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping(value = "/tips/{key}/publish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Publish tip", tags = {"tip"})
    @ApiResponse(responseCode = HttpStatusCodes.NO_CONTENT, description = "Tip published successfully.")
    public RestResponse<Void> publish(@PathVariable final String key) throws URISyntaxException {
        log.debug("REST request to publish Tip with key : {}", key);
        tipService.publish(key);
        return RestResponse.success();
    }

}
