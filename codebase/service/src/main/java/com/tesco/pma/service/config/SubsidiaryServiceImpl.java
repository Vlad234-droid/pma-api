package com.tesco.pma.service.config;


import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.dao.SubsidiaryDAO;
import com.tesco.pma.api.Subsidiary;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.exception.ErrorCodes.SUBSIDIARIES_NOT_FOUND;
import static com.tesco.pma.exception.ErrorCodes.SUBSIDIARY_NAME_ALREADY_EXISTS;
import static com.tesco.pma.exception.ErrorCodes.SUBSIDIARY_NOT_FOUND;

/**
 * Subsidiary service implementation
 */
@Service
@Validated
@RequiredArgsConstructor
public class SubsidiaryServiceImpl implements SubsidiaryService {

    private static final String SUBSIDIARY_NAME = "subsidiaryName";
    private static final String SUBSIDIARY_UUID = "subsidiaryUuid";
    private static final String REPORTING_PERIOD_SIZE = "reportingPeriodSize";

    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final SubsidiaryDAO subsidiaryDAO;

    @Override
    @Transactional
    public Subsidiary createSubsidiary(Subsidiary subsidiary) {
        try {
            subsidiary.setUuid(UUID.randomUUID());
            subsidiaryDAO.insert(subsidiary);
        } catch (DuplicateKeyException ex) {
            throw new DatabaseConstraintViolationException(SUBSIDIARY_NAME_ALREADY_EXISTS.name(),
                    messageSourceAccessor.getMessage(SUBSIDIARY_NAME_ALREADY_EXISTS,
                            Map.of(SUBSIDIARY_NAME, subsidiary.getName())), null, ex);
        }
        return subsidiary;
    }

    @Override
    @Transactional
    public Subsidiary updateSubsidiary(Subsidiary subsidiary) {
        try {
            if (1 == subsidiaryDAO.update(subsidiary)) {
                return subsidiary;
            } else {
                throw new NotFoundException(SUBSIDIARY_NOT_FOUND.name(),
                        messageSourceAccessor.getMessage(SUBSIDIARY_NOT_FOUND,
                                Collections.singletonMap(SUBSIDIARY_UUID, subsidiary.getUuid())), null);
            }
        } catch (DuplicateKeyException ex) {
            throw new DatabaseConstraintViolationException(SUBSIDIARY_NAME_ALREADY_EXISTS.name(),
                    messageSourceAccessor.getMessage(SUBSIDIARY_NAME_ALREADY_EXISTS,
                            Map.of(SUBSIDIARY_NAME, subsidiary.getName())), null, ex);
        }
    }

    @Override
    public Subsidiary getSubsidiary(UUID subsidiaryUuid) {
        return Optional.ofNullable(subsidiaryDAO.get(subsidiaryUuid)).orElseThrow(() ->
                new NotFoundException(SUBSIDIARY_NOT_FOUND.name(),
                        messageSourceAccessor.getMessage(SUBSIDIARY_NOT_FOUND,
                                Collections.singletonMap(SUBSIDIARY_UUID, subsidiaryUuid)), null));
    }

    @Override
    public List<Subsidiary> getSubsidiaries() {
        return Optional.ofNullable(subsidiaryDAO.getAll()).orElseThrow(() ->
                new NotFoundException(SUBSIDIARIES_NOT_FOUND.name(),
                        messageSourceAccessor.getMessage(SUBSIDIARIES_NOT_FOUND), null));
    }

    @Override
    @Transactional
    public void deleteSubsidiary(UUID subsidiaryUuid) {
        subsidiaryDAO.deleteSubsidiary(subsidiaryUuid);
    }
}
