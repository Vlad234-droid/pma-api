package com.tesco.pma.bpm.camunda.util;

import lombok.experimental.UtilityClass;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-01-10 21:13
 */
@UtilityClass
public class ExtensionsUtil {
    /**
     * Utility method to get extensions properties
     *
     * @param element source element
     * @return collection of Camunda properties
     */
    public static Collection<CamundaProperty> getCamundaProperties(BaseElement element) {
        var extensionElements = element.getExtensionElements();
        if (extensionElements != null) {
            var propsQuery = extensionElements.getElementsQuery().filterByType(CamundaProperties.class);
            if (propsQuery.count() > 0) {
                return propsQuery.singleResult().getCamundaProperties();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Utility method to get extensions properties
     * Note: all keys are converted to lower case
     *
     * @param element source element
     * @return map of properties
     */
    public static Map<String, String> getExtensionsProperties(BaseElement element) {
        return getExtensionsProperties(getCamundaProperties(element));
    }

    /**
     * Utility method to get extensions properties
     * Note: all keys are converted to lower case
     *
     * @param properties collection of Camunda properties
     * @return map of properties
     */
    public static Map<String, String> getExtensionsProperties(Collection<CamundaProperty> properties) {
        return properties.stream()
                .collect(Collectors.toMap(camundaProperty -> camundaProperty.getCamundaName().toLowerCase(),
                        CamundaProperty::getCamundaValue));
    }
}
