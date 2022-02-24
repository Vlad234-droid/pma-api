package com.tesco.pma.cms.service;

import com.tesco.pma.cms.api.ContentEntry;
import com.tesco.pma.colleague.api.Colleague;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
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
        var allKeys = ObjectUtils.isEmpty(keys) ? getAllKeys(processEngine.getRepositoryService(), HELP_FAQ_URLS) : keys;
        return allKeys.stream().collect(Collectors.toMap(key -> key, key -> getUrl(key, colleague)));
    }

    @Override
    public Map<String, List<ContentEntry>> getHelpFaqContentEntries(Colleague colleague, Collection<String> keys) {
        var allKeys = ObjectUtils.isEmpty(keys) ? getAllKeys(processEngine.getRepositoryService(), HELP_FAQ_KEYS) : keys;
        return allKeys.stream().collect(Collectors.toMap(key -> key, key -> getContentEntries(key, colleague)));
    }


    private Collection<String> getAllKeys(RepositoryService repositoryService, String tableName) {

        var ddId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(tableName).singleResult().getId();
        var dmnModelInstance = repositoryService.getDmnModelInstance(ddId);
        var modelType = dmnModelInstance.getModel().getType(DecisionTable.class);
        var tables = dmnModelInstance.getModelElementsByType(modelType);

        if (tables.isEmpty()) {
            return Collections.emptyList();
        }

        var table = (DecisionTable) tables.iterator().next();

        return table.getRules().stream()
                .map(this::getKeyText)
                .collect(Collectors.toSet());
    }

    private String getKeyText(Rule rule) {
        var entry = rule.getInputEntries().iterator().next();
        return StringUtils.remove(entry.getTextContent(), '"');
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
        return contentEntryService.findByKey(compoundKey);
    }
}
