package com.tesco.pma.flow.configuration;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tesco.pma.service.deployment.ProcessMetadataService;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 07.10.2021 Time: 21:44
 */
@Component
public class PMAPluginsRegisterer extends AbstractProcessEnginePlugin {
    @Autowired
    ProcessMetadataService processMetadataService;

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

        // get all existing preParseListeners
        List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();

        if (preParseListeners == null) {

            // if no preParseListener exists, create new list
            preParseListeners = new ArrayList<>();
            processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
        }

        // add new BPMN Parse Listener
        preParseListeners.add(new ReviewUserTasksParseListener(processMetadataService));
    }
}
