package com.tesco.pma.objective.service.rest;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.PersonalObjective;
import com.tesco.pma.objective.service.ObjectiveService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
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
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Validated
@RequiredArgsConstructor
public class ObjectiveEndpoint {
    private final ObjectiveService objectiveService;
    private final NamedMessageSourceAccessor messages;

    /**
     * POST call to create a PersonalObjective.
     *
     * @param personalObjective a PersonalObjective
     * @return a RestResponse parameterized with PersonalObjective
     */
    @Operation(summary = "Create a personal objective", description = "PersonalObjective created", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/objectives", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Validated({ValidationGroup.WithoutId.class, Default.class})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<PersonalObjective> createPersonalObjective(@RequestBody @Valid PersonalObjective personalObjective) {
        return success(objectiveService.createPersonalObjective(personalObjective));
    }

    /**
     * Get call using a Path param and return a PersonalObjective as JSON.
     *
     * @param uuid an identifier
     * @return a RestResponse parameterized with PersonalObjective
     */
    @Operation(summary = "Get a personal objective by its uuid", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the PersonalObjective")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PersonalObjective not found", content = @Content)
    @GetMapping(path = "/objectives/{uuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<PersonalObjective> getPersonalObjective(@PathVariable("uuid") UUID uuid) {
        return success(objectiveService.getPersonalObjectiveByUuid(uuid));
    }

    /**
     * PUT call to update a PersonalObjective.
     *
     * @param uuid              an identifier
     * @param personalObjective a PersonalObjective
     * @return a RestResponse parameterized with PersonalObjective
     */
    @Operation(summary = "Update existing personal objective", description = "Update existing PersonalObjective", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "PersonalObjective updated")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PersonalObjective not found", content = @Content)
    @PutMapping(path = "/objectives/{uuid}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Validated({ValidationGroup.WithoutId.class, Default.class})
    public RestResponse<PersonalObjective> updatePersonalObjective(@PathVariable("uuid") @NotNull UUID uuid,
                                                                   @RequestBody @Valid PersonalObjective personalObjective) {
        personalObjective.setUuid(uuid);
        return success(objectiveService.updatePersonalObjective(personalObjective));
    }

    /**
     * DELETE call to delete a PersonalObjective.
     *
     * @param uuid an identifier
     * @return a RestResponse with success field of boolean value
     */
    @Operation(summary = "Delete existing personal objective", description = "Delete existing PersonalObjective", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "PersonalObjective deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "PersonalObjective not found", content = @Content)
    @DeleteMapping(path = "/objectives/{uuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Void> deletePersonalObjective(@PathVariable("uuid") @NotNull UUID uuid) {
        objectiveService.deletePersonalObjective(uuid);
        return success();
    }

    /**
     * POST call to create group's objectives.
     *
     * @param businessUnitUuid     business unit an identifier
     * @param performanceCycleUuid performance cycle an identifier
     * @param groupObjectives      group's objectives
     * @return a RestResponse parameterized with group's objectives
     */
    @Operation(summary = "Create new group's objectives", description = "Group's objectives created", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/business-units/{businessUnitUuid}/performance-cycles/{performanceCycleUuid}/objectives",
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<List<GroupObjective>> createGroupObjectives(@PathVariable("businessUnitUuid") UUID businessUnitUuid,
                                                                    @PathVariable("performanceCycleUuid") UUID performanceCycleUuid,
                                                                    @RequestBody @Valid List<GroupObjective> groupObjectives) {
        return RestResponse.success(objectiveService.createGroupObjectives(businessUnitUuid, performanceCycleUuid, groupObjectives));
    }

    /**
     * PUT call to update group's objectives.
     *
     * @param businessUnitUuid     business unit an identifier
     * @param performanceCycleUuid performance cycle an identifier
     * @param groupObjectives      group's objectives
     * @return a RestResponse parameterized with group's objectives
     */
    @Operation(summary = "Update group's objectives", description = "Group's objectives updated", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Group's objectives updated")
    @PutMapping(path = "/business-units/{businessUnitUuid}/performance-cycles/{performanceCycleUuid}/objectives",
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public RestResponse<List<GroupObjective>> updateGroupObjectives(@PathVariable("businessUnitUuid") UUID businessUnitUuid,
                                                                    @PathVariable("performanceCycleUuid") UUID performanceCycleUuid,
                                                                    @RequestBody @Valid List<GroupObjective> groupObjectives) {
        return RestResponse.success(objectiveService.updateGroupObjectives(businessUnitUuid, performanceCycleUuid, groupObjectives));
    }

    /**
     * Get call using a Path param and return a list of Group Objectives as JSON.
     *
     * @param businessUnitUuid     business unit an identifier
     * @param performanceCycleUuid performance cycle an identifier
     * @return a RestResponse parameterized with list of Group Objectives
     */
    @Operation(summary = "Get all group's objectives by business unit and performance cycle", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found Group Objectives")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Group Objectives not found", content = @Content)
    @GetMapping(path = "/business-units/{businessUnitUuid}/performance-cycles/{performanceCycleUuid}/objectives",
            produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<GroupObjective>> getGroupObjectives(@PathVariable("businessUnitUuid") UUID businessUnitUuid,
                                                                 @PathVariable("performanceCycleUuid") UUID performanceCycleUuid) {
        return success(objectiveService.getAllGroupObjectives(businessUnitUuid, performanceCycleUuid));
    }
}
