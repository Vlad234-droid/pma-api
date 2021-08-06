package com.tesco.pma.service.config;

import com.tesco.pma.api.Subsidiary;
import com.tesco.pma.validation.ValidationGroup;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.List;
import java.util.UUID;

/**
 * Subsidiary service
 * Implementation classes must be annotated with @org.springframework.validation.annotation.Validated.
 */
public interface SubsidiaryService {

    /**
     * Creates a subsidiary
     *
     * @param subsidiary a Subsidiary
     * @return a Subsidiary
     * @throws NotFoundException                    if subsidiary not found
     * @throws DatabaseConstraintViolationException if subsidiary name already exists
     */
    @Validated({ValidationGroup.OnCreate.class, Default.class})
    Subsidiary createSubsidiary(@NotNull @Valid Subsidiary subsidiary);

    /**
     * Updates an existing subsidiary
     *
     * @param subsidiary a Subsidiary
     * @return a Subsidiary
     * @throws NotFoundException                    if subsidiary not found
     * @throws DatabaseConstraintViolationException if subsidiary name already exists
     */
    @Validated({ValidationGroup.OnUpdate.class, Default.class})
    Subsidiary updateSubsidiary(@NotNull @Valid Subsidiary subsidiary);

    /**
     * Returns a subsidiary
     *
     * @param subsidiaryUuid an identifier
     * @return a Subsidiary
     * @throws NotFoundException if subsidiary not found
     */
    Subsidiary getSubsidiary(@NotNull UUID subsidiaryUuid);

    /**
     * Deletes a subsidiary
     *
     * @param subsidiaryUuid an identifier
     * @throws NotFoundException           if subsidiary not found
     * @throws SubsidiaryDeletionException if subsidiary is linked to the reporting period
     */
    void deleteSubsidiary(@NotNull UUID subsidiaryUuid);

    /**
     * Returns all subsidiaries
     *
     * @return a list of Subsidiaries
     * @throws NotFoundException if subsidiary not found
     */
    List<Subsidiary> getSubsidiaries();

}
