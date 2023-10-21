package it.gov.pagopa.miladapter.engine.task;

import it.gov.pagopa.miladapter.model.HTTPConfiguration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.GenericRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import it.gov.pagopa.miladapter.util.EngineVariablesUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Map;

public interface RestExternalTaskHandler extends ExternalTaskHandler {
    default void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            getLogger().info("MIL-Adapter invoked on Task {}", externalTask.getId());
            Map<String, Object> variables = EngineVariablesUtils.getTaskVariablesCaseInsensitive(externalTask);
            if (getRestConfigurationProperties().isLogEngineInputVariablesEnabled()) {
                getLogger().info("Input Engine Variables: {}", variables);
            }
            HTTPConfiguration httpConfiguration = EngineVariablesToHTTPConfigurationUtils.getHttpConfiguration(variables, isMILFlow());
            VariableMap variableMap = getRestService().executeRestCall(httpConfiguration);
            externalTaskService.complete(externalTask, variableMap);
        } catch (Exception e) {
            getLogger().error("Error on MIL-Adapter execution: {}", e.getMessage(), e);
            externalTaskService.handleFailure(externalTask, e.getMessage(), e.getMessage().concat(Arrays.toString(e.getStackTrace())), 0, 0);
        }

    }

    Logger getLogger();

    RestConfigurationProperties getRestConfigurationProperties();

    GenericRestService getRestService();

    boolean isMILFlow();
}
