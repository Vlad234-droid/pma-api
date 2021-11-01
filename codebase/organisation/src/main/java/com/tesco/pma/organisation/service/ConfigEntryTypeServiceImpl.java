package com.tesco.pma.organisation.service;

import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.organisation.api.ConfigEntryErrorCodes;
import com.tesco.pma.organisation.dao.ConfigEntryTypeDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigEntryTypeServiceImpl implements ConfigEntryTypeService {

    private static final String ID = "id";

    private final ConfigEntryTypeDAO dao;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public GeneralDictionaryItem findConfigEntryType(int id) {
        return Optional.ofNullable(dao.findConfigEntryType(id))
                .orElseThrow(() -> new NotFoundException(ConfigEntryErrorCodes.CONFIG_ENTRY_TYPE_NOT_FOUND.getCode(),
                        messageSourceAccessor.getMessage(ConfigEntryErrorCodes.CONFIG_ENTRY_TYPE_NOT_FOUND,
                                Map.of(ID, id))));
    }

    @Override
    public List<GeneralDictionaryItem> findAllConfigEntryTypes() {
        return dao.findAllConfigEntryTypes();
    }

    @Override
    public GeneralDictionaryItem create(GeneralDictionaryItem item) {
        try {
            dao.create(item);
            return item;
        } catch (DuplicateKeyException ex) {
            throw new DatabaseConstraintViolationException(ConfigEntryErrorCodes.CONFIG_ENTRY_TYPE_ALREADY_EXISTS.name(),
                    messageSourceAccessor.getMessage(ConfigEntryErrorCodes.CONFIG_ENTRY_TYPE_ALREADY_EXISTS,
                            Map.of(ID, item.getId())), null, ex);
        }
    }

    @Override
    public GeneralDictionaryItem update(GeneralDictionaryItem item) {
        var update = dao.update(item);
        if (1 == update) {
            return item;
        } else {
            throw new NotFoundException(ConfigEntryErrorCodes.CONFIG_ENTRY_TYPE_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(ConfigEntryErrorCodes.CONFIG_ENTRY_TYPE_NOT_FOUND,
                            Map.of(ID, item.getId())));
        }
    }

    @Override
    public void delete(int id) {
        var delete = dao.delete(id);
        if (1 != delete) {
            throw new NotFoundException(ConfigEntryErrorCodes.CONFIG_ENTRY_TYPE_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(ConfigEntryErrorCodes.CONFIG_ENTRY_TYPE_NOT_FOUND,
                            Map.of(ID, id)));
        }
    }
}
