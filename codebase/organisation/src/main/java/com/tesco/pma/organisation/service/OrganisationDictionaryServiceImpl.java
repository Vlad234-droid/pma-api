package com.tesco.pma.organisation.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.organisation.api.ConfigEntryErrorCodes;
import com.tesco.pma.organisation.api.OrganisationDictionary;
import com.tesco.pma.organisation.dao.OrganisationDictionaryDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganisationDictionaryServiceImpl implements OrganisationDictionaryService {

    private static final String CODE = "code";
    private final OrganisationDictionaryDAO dao;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public OrganisationDictionary findOrganisationDictionary(String code) {
        return Optional.ofNullable(dao.findOrganisationDictionary(code))
                .orElseThrow(() -> new NotFoundException(ConfigEntryErrorCodes.ORGANISATION_DICTIONARY_NOT_FOUND.getCode(),
                        messageSourceAccessor.getMessage(ConfigEntryErrorCodes.ORGANISATION_DICTIONARY_NOT_FOUND,
                                Map.of(CODE, code))));
    }

    @Override
    public List<OrganisationDictionary> findAllOrganisationDictionaries() {
        return dao.findAllOrganisationDictionaries();
    }

    @Override
    public OrganisationDictionary create(OrganisationDictionary item) {
        try {
            dao.create(item);
            return item;
        } catch (DuplicateKeyException ex) {
            throw new DatabaseConstraintViolationException(ConfigEntryErrorCodes.ORGANISATION_DICTIONARY_ALREADY_EXISTS.name(),
                    messageSourceAccessor.getMessage(ConfigEntryErrorCodes.ORGANISATION_DICTIONARY_ALREADY_EXISTS,
                            Map.of(CODE, item.getCode())), null, ex);
        }
    }

    @Override
    public OrganisationDictionary update(OrganisationDictionary item) {
        var update = dao.update(item);
        if (1 == update) {
            return item;
        } else {
            throw new NotFoundException(ConfigEntryErrorCodes.ORGANISATION_DICTIONARY_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(ConfigEntryErrorCodes.ORGANISATION_DICTIONARY_NOT_FOUND,
                            Map.of(CODE, item.getCode())));
        }
    }

    @Override
    public void delete(String code) {
        var delete = dao.delete(code);
        if (1 != delete) {
            throw new NotFoundException(ConfigEntryErrorCodes.ORGANISATION_DICTIONARY_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(ConfigEntryErrorCodes.ORGANISATION_DICTIONARY_NOT_FOUND,
                            Map.of(CODE, code)));
        }
    }
}
