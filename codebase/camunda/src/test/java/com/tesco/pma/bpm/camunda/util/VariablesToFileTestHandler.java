package com.tesco.pma.bpm.camunda.util;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.CamundaExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-01-10 21:52
 */
public class VariablesToFileTestHandler extends CamundaAbstractFlowHandler {
    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var targetPath = context.getVariable(ExtensionsDelegateVariableMappingTest.FlowParams.TARGET_PATH, String.class);
        var targetFileName = context.getVariable(ExtensionsDelegateVariableMappingTest.FlowParams.TARGET_FILE_NAME, String.class);
        var targetFile = new File(targetPath, targetFileName);

        var properties = new Properties();
        ((CamundaExecutionContext) context).getDelegateExecution().getVariables().forEach((key, value) -> {
            properties.setProperty(key, value.toString());
        });
        try (var fs = new FileOutputStream(targetFile)) {
            properties.store(fs, targetFileName);
        }
    }
}
