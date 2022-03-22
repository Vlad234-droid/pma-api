package com.tesco.pma.bpm.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Rule;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class DmnTablesUtils {

    public Set<String> getAllKeys(RepositoryService repositoryService, String tableName) {
        return getAll(repositoryService, tableName, DmnTablesUtils::getKeyText);
    }

    public Set<String> getAllResults(RepositoryService repositoryService, String tableName) {
        return getAll(repositoryService, tableName, DmnTablesUtils::getResultText);
    }

    private Set<String> getAll(RepositoryService repositoryService, String tableName, Function<Rule, String> func) {
        var table = getTable(repositoryService, tableName);

        if (table == null) {
            return Collections.emptySet();
        }

        return table.getRules().stream()
                .map(func)
                .collect(Collectors.toSet());
    }

    private DecisionTable getTable(RepositoryService repositoryService, String tableName) {
        var ddId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(tableName)
                .list().stream()
                .reduce((f,s) -> s.getVersion() > f.getVersion() ? s : f)
                .orElseThrow(() -> new DmnDecisionResultException("No result")).getId();

        var dmnModelInstance = repositoryService.getDmnModelInstance(ddId);
        var modelType = dmnModelInstance.getModel().getType(DecisionTable.class);
        var tables = dmnModelInstance.getModelElementsByType(modelType);

        if (tables.isEmpty()) {
            return null;
        }

        return (DecisionTable) tables.iterator().next();
    }

    private String getKeyText(Rule rule) {
        var entry = rule.getInputEntries().iterator().next();
        return StringUtils.remove(entry.getTextContent(), '"');
    }

    private String getResultText(Rule rule) {
        var entry = rule.getOutputEntries().iterator().next();
        return StringUtils.remove(entry.getTextContent(), '"');
    }

}
