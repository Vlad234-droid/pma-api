package com.tesco.pma.fs.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.domain.ProcessTemplate;
import com.tesco.pma.fs.domain.UploadMetadata;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Template service
 * Implementation classes must be annotated with @org.springframework.validation.annotation.Validated.
 */
public interface TemplateService {

    /**
     * Upload template file and save it to database
     *
     * @param inputStream data of template file
     * @param uploadMetadata represents the parts of template metadata
     * @param file a template file
     * @param creatorId represents the creator identifier
     * @return uploaded template
     * @throws RegistrationException if failed to save template file to database
     */
    ProcessTemplate uploadTemplate(@NotNull InputStream inputStream, @NotNull UploadMetadata uploadMetadata,
                                   @NotNull MultipartFile file, @NotNull String creatorId) throws IOException;

    /**
     * Read all information about template by its identifier
     *
     * @param templateUuid template identifier
     * @param includeFileContent identifies if include file content
     * @return Process Template data
     * @throws NotFoundException if template by uuid is not found
     */
    ProcessTemplate readTemplateByUuid(@NotNull UUID templateUuid, boolean includeFileContent);
}
