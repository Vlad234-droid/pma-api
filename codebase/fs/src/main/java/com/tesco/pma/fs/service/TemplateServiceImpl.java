package com.tesco.pma.fs.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.dao.TemplateDAO;
import com.tesco.pma.fs.domain.ProcessTemplate;
import com.tesco.pma.fs.domain.UploadMetadata;
import com.tesco.pma.logging.TraceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.ERROR_FILE_NOT_FOUND;
import static com.tesco.pma.fs.domain.ProcessTemplateStatus.DRAFT;
import static com.tesco.pma.fs.exception.ErrorCodes.ERROR_FILE_REGISTRATION_FAILED;

/**
 * Template service implementation
 */
@Service
@Validated
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateDAO templateDao;

    @Override
    @Transactional
    public ProcessTemplate uploadTemplate(InputStream inputStream, UploadMetadata uploadMetadata,
                                          MultipartFile file, String creatorId) throws IOException {
        var template = new ProcessTemplate();
        template.setUuid(UUID.fromString(TraceUtils.getTraceId().getValue()));
        var fileName = file.getOriginalFilename();
        template.setFileName(fileName);
        template.setFileLength((int) file.getSize());
        template.setFileContent(file.getBytes());

        var currMomentInUTC = Instant.now();
        template.setCreatedTime(currMomentInUTC);
        template.setCreatedBy(creatorId);

        template.setPath(uploadMetadata.getPath());
        template.setStatus(uploadMetadata.getStatus() == null ? DRAFT : uploadMetadata.getStatus());
        template.setType(uploadMetadata.getType());
        template.setDescription(uploadMetadata.getDescription());
        template.setFileDate(uploadMetadata.getFileDate() == null ? currMomentInUTC : uploadMetadata.getFileDate());

        var version = templateDao.getMaxVersionForTemplate(template.getPath(), template.getFileName()) + 1;
        template.setVersion(version);

        var insertedRows = templateDao.saveTemplate(template);
        if (insertedRows != 1) {
            throw new RegistrationException(ERROR_FILE_REGISTRATION_FAILED.name(),
                    "Failed to save Template to database", fileName);
        }

        return template;
    }

    @Override
    public ProcessTemplate readTemplateByUuid(UUID templateUuid, boolean includeFileContent) {
        return Optional.ofNullable(templateDao.readTemplateByUuid(templateUuid, includeFileContent))
                .orElseThrow(() -> new NotFoundException(ERROR_FILE_NOT_FOUND.name(),
                        "Template file was not found", templateUuid.toString()));

    }
}
