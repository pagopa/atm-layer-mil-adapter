package it.gov.pagopa.miladapter.engine.task;

import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.GenericRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;
import org.springframework.core.task.TaskExecutor;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface RestExternalTaskHandler extends ExternalTaskHandler {

    TaskExecutor getTaskRestExecutor();

    TaskExecutor getTaskComplExecutor();

    int getMaxTasks();

    default void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            getLogger().info("MIL-Adapter invoked on Task {}", externalTask.getId());
            Map<String, Object> variables = EngineVariablesUtils.getTaskVariablesCaseInsensitive(externalTask);
            if (getRestConfigurationProperties().isLogEngineInputVariablesEnabled()) {
                getLogger().info("Input Engine Variables: {}", variables);
            }

            Executor restPoolExecutor = this.getTaskRestExecutor();
            Configuration configuration = getHttpConfiguration(variables);
            //async mode
            CompletableFuture<VariableMap> resultAsync = CompletableFuture.supplyAsync(
                    () -> getRestService().executeRestCall(configuration), restPoolExecutor);
            Executor complPoolExecutor = this.getTaskComplExecutor();
            resultAsync
                    .thenAcceptAsync(variableMap -> {
                        try {
                        getLogger().info("Start Complete task wit transactionId: {}",configuration.getHeaders()!=null?configuration.getHeaders().get(RequiredProcessVariables.TRANSACTION_ID.getEngineValue()):"");
                        getLogger().info("Completing task {} for process instance {}",externalTask.getId(),externalTask.getProcessInstanceId());
                        getLogger().info("Variables: {}",variableMap);
                        externalTaskService.complete(externalTask, null, variableMap);
                        getLogger().info("End Complete task wit transactionId: {}",configuration.getHeaders()!=null?configuration.getHeaders().get(RequiredProcessVariables.TRANSACTION_ID.getEngineValue()):"");
                        }
                        catch (Exception e) {
                            getLogger().error("Error on MIL-Adapter execution: {}", e.getMessage(), e);
                            externalTaskService.handleFailure(externalTask, e.getMessage(),
                                    e.getMessage().concat(Arrays.toString(e.getStackTrace())), 0, 0);
                        }}, complPoolExecutor);

        } catch (Exception e) {
            getLogger().error("Error on MIL-Adapter execution: {}", e.getMessage(), e);
            externalTaskService.handleFailure(externalTask, e.getMessage(),
                    Arrays.toString(e.getStackTrace()), 0, 0);
        }

    }

    Logger getLogger();

    RestConfigurationProperties getRestConfigurationProperties();

    GenericRestService getRestService();

    boolean isMILFlow();

    boolean isIdPayFlow();

    Configuration getHttpConfiguration(Map<String, Object> variables);

}
