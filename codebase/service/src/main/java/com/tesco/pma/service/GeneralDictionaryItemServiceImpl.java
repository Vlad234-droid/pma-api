package com.tesco.pma.service;

import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.dao.GeneralDictionaryItemDAO;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class GeneralDictionaryItemServiceImpl implements GeneralDictionaryItemService {

    private static final String PARAM_NAME = "paramName";
    private static final String PARAM_VALUE = "paramValue";

    private final GeneralDictionaryItemDAO dao;
    private final NamedMessageSourceAccessor namedMessageSourceAccessor;

    @Override
    public GeneralDictionaryItem read(String dictionary, Integer id) {
        GeneralDictionaryItem dictionaryItem = dao.read(dictionary, id);
        if (dictionaryItem == null) {
            String message = namedMessageSourceAccessor.getMessage(ErrorCodes.DICTIONARY_ITEM_NOT_FOUND,
                    Map.of(PARAM_NAME, "id", PARAM_VALUE, id));
            throw new NotFoundException(ErrorCodes.DICTIONARY_ITEM_NOT_FOUND.getCode(), message);
        }
        return dictionaryItem;
    }

    @Override
    public GeneralDictionaryItem findByCode(String dictionary, String code) {
        GeneralDictionaryItem dictionaryItem = dao.findByCode(dictionary, code);
        if (dictionaryItem == null) {
            String message = namedMessageSourceAccessor.getMessage(ErrorCodes.DICTIONARY_ITEM_NOT_FOUND,
                    Map.of(PARAM_NAME, "code", PARAM_VALUE, code));
            throw new NotFoundException(ErrorCodes.DICTIONARY_ITEM_NOT_FOUND.getCode(), message);
        }
        return dictionaryItem;
    }

    @Override
    public List<GeneralDictionaryItem> findAll(String dictionary) {
        return dao.findAll(dictionary);
    }

}
