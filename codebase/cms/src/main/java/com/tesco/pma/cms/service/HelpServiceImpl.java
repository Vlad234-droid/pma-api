package com.tesco.pma.cms.service;

import com.tesco.pma.bpm.util.DmnTablesUtils;
import com.tesco.pma.cms.api.ContentEntry;
import com.tesco.pma.colleague.api.Colleague;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-15 23:20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HelpServiceImpl implements HelpService {
    private static final String HELP_FAQ_URLS = "help_faq_urls";
    private static final String HELP_FAQ_KEYS = "help_faq_keys";

    private final ProcessEngine processEngine;
    private final ContentEntryService contentEntryService;

    enum Params {
        KEY,
        COLLEAGUE
    }
    
    @Override
    public Map<String, String> getHelpFaqUrls(Colleague colleague, Collection<String> keys) {
        var allKeys = ObjectUtils.isEmpty(keys) ? DmnTablesUtils.getAllKeys(processEngine.getRepositoryService(), HELP_FAQ_URLS) : keys;
        return allKeys.stream().collect(Collectors.toMap(key -> key, key -> getUrl(key, colleague)));
    }

    @Override
    public Map<String, List<ContentEntry>> getHelpFaqs(Colleague colleague, Collection<String> keys) {
        var allKeys = ObjectUtils.isEmpty(keys) ? DmnTablesUtils.getAllKeys(processEngine.getRepositoryService(), HELP_FAQ_KEYS) : keys;
        return allKeys.stream().collect(Collectors.toMap(key -> key, key -> getContentEntries(key, colleague)));
    }

    private String getUrl(String key, Colleague colleague) {

        var variables = Variables.createVariables()
                .putValue(Params.KEY.name(), key)
                .putValue(Params.COLLEAGUE.name(), colleague);

        var decisionService = processEngine.getDecisionService();
        var result = decisionService.evaluateDecisionTableByKey(HELP_FAQ_URLS, variables);
        return result.getSingleEntry();
    }

    private List<ContentEntry> getContentEntries(String key, Colleague colleague) {

        var variables = Variables.createVariables()
                .putValue(Params.KEY.name(), key)
                .putValue(Params.COLLEAGUE.name(), colleague);

        var decisionService = processEngine.getDecisionService();
        var compoundKey = (String) decisionService.evaluateDecisionTableByKey(HELP_FAQ_KEYS, variables).getSingleEntry();
        return contentEntryService.find(compoundKey);
    }
}
