package it.gov.pagopa.miladapter.engine.task;

import it.gov.pagopa.miladapter.model.HTTPConfiguration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.MILRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import it.gov.pagopa.miladapter.util.EngineVariablesUtils;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;


@Component
@Slf4j
@ExternalTaskSubscription(
        includeExtensionProperties = true,
        topicName = "mil-adapter")
public class RestTaskHandler implements ExternalTaskHandler {

    @Autowired
    MILRestService milRestService;

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        try {
            log.info("MIL-Adapter invoked on Task {}", externalTask.getId());
            Map<String, Object> variables = EngineVariablesUtils.getTaskVariablesCaseInsensitive(externalTask);
            if (restConfigurationProperties.isLogEngineInputVariablesEnabled()) {
                log.info("Input Engine Variables: {}", variables);
            }
            HTTPConfiguration httpConfiguration = EngineVariablesToHTTPConfigurationUtils.getHttpConfiguration(variables);
            VariableMap variableMap = milRestService.executeMILRestCall(httpConfiguration);
            externalTaskService.complete(externalTask, variableMap);
        } catch (Exception e) {
            log.error("Error on MIL-Adapter execution: {}", e.getMessage(), e);
            externalTaskService.handleFailure(externalTask, e.getMessage(), e.getMessage().concat(Arrays.toString(e.getStackTrace())), 0, 0);
        }


    }
}
