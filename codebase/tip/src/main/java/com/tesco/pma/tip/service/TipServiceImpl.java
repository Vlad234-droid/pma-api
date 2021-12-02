package com.tesco.pma.tip.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.tip.api.Tip;
import com.tesco.pma.tip.dao.TipDAO;
import com.tesco.pma.tip.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service Implementation for managing {@link Tip}.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class TipServiceImpl implements TipService {

    private static final String PARAM_NAME = "paramName";
    private static final String PARAM_VALUE = "paramValue";

    private final TipDAO tipDAO;
    private final NamedMessageSourceAccessor namedMessageSourceAccessor;

    @Override
    @Transactional
    public Tip create(Tip tip) {
        log.debug("Service create Tip : {}", tip);
        tip.setUuid(UUID.randomUUID());
        tip.setVersion(1);
        tip.setCreatedTime(Instant.now());
        tip.setUpdatedTime(Instant.now());
        try {
            tipDAO.create(tip);
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseConstraintViolationException(
                    com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION.getCode(), ex.getLocalizedMessage(), null, ex);
        }
        return tip;
    }

    @Override
    public List<Tip> findAll(RequestQuery requestQuery) {
        log.debug("Service find all tips");
        return tipDAO.findAll(requestQuery);
    }

    @Override
    public Tip findOne(String key) {
        log.debug("Service find one tip by key : {}", key);
        Tip tip = tipDAO.read(key);
        if (tip == null) {
            return throwNotFound(key);
        }
        return tip;
    }

    @Override
    public List<Tip> findHistory(String key) {
        log.debug("Service find history by key : {}", key);
        return tipDAO.findHistory(key);
    }

    @Override
    @Transactional
    public Tip update(Tip tip) {
        log.debug("Service update the tip : {}", tip);
        Tip lastVersionTip = tipDAO.read(tip.getKey());
        if (lastVersionTip == null) {
            throwNotFound(tip.getKey());
        }
        tip.setUuid(UUID.randomUUID());
        tip.setKey(lastVersionTip.getKey());
        tip.setVersion(lastVersionTip.getVersion() + 1);
        tip.setCreatedTime(lastVersionTip.getCreatedTime());
        tip.setUpdatedTime(Instant.now());
        try {
            tipDAO.create(tip);
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseConstraintViolationException(
                    com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION.getCode(), ex.getLocalizedMessage(), null, ex);
        }
        return tip;
    }

    @Override
    @Transactional
    public void delete(String key) {
        log.debug("Service delete tip by key : {}", key);
        if (1 != tipDAO.delete(key)) {
            throwNotFound(key);
        }
    }

    @Override
    @Transactional
    public void publish(String key) {
        log.debug("Service publish tip by key : {}", key);
        Tip tip = tipDAO.read(key);
        if (tip == null) {
            throwNotFound(key);
        }
        tip.setUuid(UUID.randomUUID());
        tip.setVersion(tip.getVersion() + 1);
        tip.setPublished(true);
        tip.setUpdatedTime(Instant.now());
        try {
            tipDAO.create(tip);
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseConstraintViolationException(
                    com.tesco.pma.exception.ErrorCodes.CONSTRAINT_VIOLATION.getCode(), ex.getLocalizedMessage(), null, ex);
        }
    }

    private Tip throwNotFound(String key) {
        String message = namedMessageSourceAccessor.getMessage(ErrorCodes.TIP_NOT_FOUND,
                Map.of(PARAM_NAME, "key", PARAM_VALUE, key));
        throw new NotFoundException(ErrorCodes.TIP_NOT_FOUND.getCode(), message);
    }
}
