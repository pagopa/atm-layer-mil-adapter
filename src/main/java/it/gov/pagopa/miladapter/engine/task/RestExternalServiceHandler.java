package it.gov.pagopa.miladapter.engine.task;

import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.GenericRestExternalService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface RestExternalServiceHandler {

    TaskExecutor getTaskRestExecutor();

    TaskExecutor getTaskComplExecutor();

    int getMaxTasks();

    @Async
    default CompletableFuture<VariableMap> execute(Map<String, Object> variables, GenericRestExternalService externalService) {
        try {
            getLogger().info("MIL-Adapter invoked with headers: {}", variables.get("headers"));
            if (getRestConfigurationProperties().isLogEngineInputVariablesEnabled()) {
                getLogger().info("Input Headers Variables: {}", variables);
            }

            Executor restPoolExecutor = this.getTaskRestExecutor();
            Configuration configuration = getHttpConfiguration(variables);

            getLogger().info("Executing task with transactionId: {}", configuration.getHeaders() != null ? configuration.getHeaders().get(RequiredProcessVariables.TRANSACTION_ID.getEngineValue()) : "");
            // Async mode
            return CompletableFuture.supplyAsync(
                    () -> getRestService().executeRestCall(configuration), restPoolExecutor);

        } catch (Exception e) {
            getLogger().error("Error on MIL-Adapter execution: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    Logger getLogger();

    RestConfigurationProperties getRestConfigurationProperties();

    GenericRestExternalService getRestService();

    boolean isMILFlow();

    boolean isIdPayFlow();

    Configuration getHttpConfiguration(Map<String, Object> variables);
}

