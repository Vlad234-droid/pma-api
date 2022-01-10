package com.tesco.pma.bpm.camunda.util;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * Maps key-value extensions to input variables from direct and parent activities.
 * If the parent activity has the same variable name it will be ignored.
 *
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-01-10 20:25
 */
public class ExtensionsDelegateVariableMapping implements DelegateVariableMapping {
    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
        mapInputVariables(superExecution, subVariables, superExecution.getBpmnModelElementInstance());
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
        // does not implement
    }

    private void mapInputVariables(final DelegateExecution superExecution, final VariableMap subVariables,
                                   final ModelElementInstance child) {
        if (child instanceof BaseElement) {
            var extensions = ExtensionsUtil.getExtensionsProperties((BaseElement) child);
            extensions.forEach(subVariables::putIfAbsent);
        }
        var parent = child.getParentElement();
        if (parent != null) {
            mapInputVariables(superExecution, subVariables, parent);
        }
    }
}
