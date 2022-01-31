package com.tesco.pma.tip.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.organisation.service.ConfigEntryService;
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

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
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
    private static final String NF_PUBLISHED_EVENT_NAME = "NF_TIPS_RECEIVED";
    private static final String TIP_UUID_PARAM_NAME = "TIP_UUID";

    private final TipDAO tipDAO;
    private final ConfigEntryService configEntryService;
    private final NamedMessageSourceAccessor namedMessageSourceAccessor;

    @Override
    @Transactional
    public Tip create(Tip tip) {
        log.debug("Service create Tip : {}", tip);
        Tip lastVersionTip = tipDAO.read(tip.getUuid());
        if (lastVersionTip == null) {
            tip.setVersion(1);
            tip.setCreatedTime(Instant.now());
        } else {
            tip.setKey(lastVersionTip.getKey());
            tip.setVersion(lastVersionTip.getVersion() + 1);
            tip.setCreatedTime(lastVersionTip.getCreatedTime());
        }
        tip.setUuid(UUID.randomUUID());
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
    public Tip findOne(UUID uuid) {
        log.debug("Service find one tip : {}", uuid);
        Tip tip = tipDAO.read(uuid);
        if (tip == null) {
            throwNotFound(uuid);
        }
        return tip;
    }

    @Override
    public List<Tip> findHistory(UUID uuid) {
        log.debug("Service find history : {}", uuid);
        return tipDAO.findHistory(uuid);
    }

    @Override
    @Transactional
    public void delete(UUID uuid, boolean withHistory) {
        log.debug("Service delete tip : {}", uuid);
        if (withHistory) {
            Tip tip = tipDAO.read(uuid);
            if (tip == null) {
                throwNotFound(uuid);
            }
            tipDAO.deleteByKey(tip.getKey());
        } else {
            if (1 != tipDAO.delete(uuid)) {
                throwNotFound(uuid);
            }
        }
    }

    @Override
    @Transactional
    public Tip publish(UUID uuid) {
        log.debug("Service publish Tip : {}", uuid);
        Tip tip = tipDAO.read(uuid);
        if (tip == null) {
            throwNotFound(uuid);
        }
        tip.setUpdatedTime(Instant.now());
        tip.setPublished(true);
        tipDAO.publish(tip);

        var params = new HashMap<String, Serializable>();
        params.put(TIP_UUID_PARAM_NAME, tip.getUuid());

        configEntryService.propagateEventsByCompositeKey(tip.getTargetOrganisation().getCompositeKey(),
                NF_PUBLISHED_EVENT_NAME, params);

        return tip;
    }

    private void throwNotFound(UUID uuid) {
        String message = namedMessageSourceAccessor.getMessage(ErrorCodes.TIP_NOT_FOUND,
                Map.of(PARAM_NAME, "uuid", PARAM_VALUE, uuid));
        throw new NotFoundException(ErrorCodes.TIP_NOT_FOUND.getCode(), message);
    }
}
