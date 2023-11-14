package it.gov.pagopa.miladapter.engine.task;

import java.util.Arrays;
import java.util.Map;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;

import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.GenericRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesUtils;

public interface RestExternalTaskHandler extends ExternalTaskHandler {
    default void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            getLogger().info("MIL-Adapter invoked on Task {}", externalTask.getId());
            Map<String, Object> variables = EngineVariablesUtils.getTaskVariablesCaseInsensitive(externalTask);
            if (getRestConfigurationProperties().isLogEngineInputVariablesEnabled()) {
                getLogger().info("Input Engine Variables: {}", variables);
            }
            Configuration configuration = getHttpConfiguration(variables);
            VariableMap variableMap = getRestService().executeRestCall(configuration);
            externalTaskService.complete(externalTask, variableMap);
        } catch (Exception e) {
            getLogger().error("Error on MIL-Adapter execution: {}", e.getMessage(), e);
            externalTaskService.handleFailure(externalTask, e.getMessage(),
                    e.getMessage().concat(Arrays.toString(e.getStackTrace())), 0, 0);
        }

    }

    Logger getLogger();

    RestConfigurationProperties getRestConfigurationProperties();

    GenericRestService getRestService();

    boolean isMILFlow();

    Configuration getHttpConfiguration(Map<String, Object> variables);

}
