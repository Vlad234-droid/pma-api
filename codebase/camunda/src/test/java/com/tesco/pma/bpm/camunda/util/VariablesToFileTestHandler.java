package com.tesco.pma.bpm.camunda.util;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.CamundaExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

        var properties = new Properties();
        ((CamundaExecutionContext) context).getDelegateExecution().getVariables().forEach((key, value) -> {
            properties.setProperty(key, value.toString());
        });
        try (var fs = Files.newBufferedWriter(Paths.get(targetPath, targetFileName),
                StandardCharsets.ISO_8859_1,
                StandardOpenOption.CREATE_NEW)) {
            properties.store(fs, targetFileName);
        }
    }
}
