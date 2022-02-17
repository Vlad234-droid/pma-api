package com.tesco.pma.cms.service;

import com.tesco.pma.colleague.api.Colleague;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
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

    private final ProcessEngine processEngine;

    enum Params {
        KEY,
        COLLEAGUE
    }
    
    @Override
    public Map<String, String> getHelpFaqUrls(Colleague colleague, Collection<String> keys) {
        var decisionService = processEngine.getDecisionService();
        var allKeys = ObjectUtils.isEmpty(keys) ? getAllKeys(processEngine.getRepositoryService()) : keys;

        return allKeys.stream().collect(Collectors.toMap(key -> key, key -> getUrl(decisionService, key, colleague)));
    }

    private Collection<String> getAllKeys(RepositoryService repositoryService) {
        var ddId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(HELP_FAQ_URLS).singleResult().getId();
        DmnModelInstance dmnModelInstance = repositoryService.getDmnModelInstance(ddId);

        var modelType = dmnModelInstance.getModel().getType(DecisionTable.class);
        var tables = dmnModelInstance.getModelElementsByType(modelType);
        if (!tables.isEmpty()) {
            var table = (DecisionTable) tables.iterator().next();
            return table.getRules().stream().map(this::getKeyText).collect(Collectors.toSet());
        }
        return Collections.emptyList();
    }

    private String getKeyText(Rule rule) {
        var entry = rule.getInputEntries().iterator().next();
        return StringUtils.remove(entry.getTextContent(), '"');
    }

    private String getUrl(DecisionService decisionService, String key, Colleague colleague) {
        var variables = Variables.createVariables()
                .putValue(Params.KEY.name(), key)
                .putValue(Params.COLLEAGUE.name(), colleague);

        var result = decisionService.evaluateDecisionTableByKey(HELP_FAQ_URLS, variables);
        return result.getSingleEntry();
    }
}
