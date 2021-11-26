package com.tesco.pma.tip.rest;

import com.tesco.pma.exception.InvalidParameterException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.tip.api.Tip;
import com.tesco.pma.tip.service.TipService;
import com.tesco.pma.validation.ValidationGroup;
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
import javax.validation.groups.Default;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
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
     * {@code POST  /tips} : Create a new tip.
     *
     * @param tip the tip to create.
     * @return the {@link RestResponse} with status {@code 201 (Created)} and with body the new tip,
     * or with status {@code 400 (Bad Request)} if the tip has already an UUID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/tips")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated({ValidationGroup.OnCreate.class, Default.class})
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
            + "    \"uuid_in\": [\"982b2124-b712-429f-aad1-656cbcadc1b5\",\"80741a5e-c41c-47a0-9181-d202e9d077b7\"],\n"
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
    @Operation(summary = "Get tip by UUID", tags = {"tip"})
    public RestResponse<Tip> read(@PathVariable final UUID uuid) {
        log.debug("REST request to get Tip : {}", uuid);
        return RestResponse.success(tipService.findOne(uuid));
    }

    /**
     * {@code PUT  /tips/:uuid} : Updates an existing tip.
     *
     * @param uuid the uuid of the tip to save.
     * @param tip  the tip to update.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the updated tip,
     * or with status {@code 400 (Bad Request)} if the tip is not valid.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/tips/{uuid}")
    @Validated({ValidationGroup.OnUpdate.class, Default.class})
    @Operation(summary = "Updates an existing Tip", tags = {"tip"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Tip updated successfully.")
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid UUID")
    public RestResponse<Tip> update(@PathVariable final UUID uuid, @Valid @RequestBody Tip tip) throws URISyntaxException {
        log.debug("REST request to update Tip : {}, {}", uuid, tip);
        if (tip.getUuid() == null) {
            tip.setUuid(uuid);
        }
        if (!Objects.equals(uuid, tip.getUuid())) {
            throw new InvalidParameterException(HttpStatusCodes.BAD_REQUEST, "Path uuid does not match body uuid", "tip.uuid");
        }
        return RestResponse.success(tipService.update(tip));
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
    public RestResponse<Void> delete(@PathVariable final UUID uuid) {
        log.debug("REST request to delete Tip with uuid: {}", uuid);
        tipService.delete(uuid);
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
    @PutMapping(value = "/tips/{uuid}/publish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Publish tip", tags = {"tip"})
    @ApiResponse(responseCode = HttpStatusCodes.NO_CONTENT, description = "Tip published successfully.")
    public RestResponse<Void> publish(@PathVariable final UUID uuid) throws URISyntaxException {
        log.debug("REST request to publish Tip with uuid : {}", uuid);
        tipService.publish(uuid);
        return RestResponse.success();
    }

}
