package it.gov.pagopa.miladapter.engine.task.impl;

import it.gov.pagopa.miladapter.engine.task.RestExternalTaskHandler;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.GenericRestService;
import it.gov.pagopa.miladapter.services.IDPayRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@ExternalTaskSubscription(includeExtensionProperties = true, topicName = "idpay-adapter")
public class IDPayRestTaskHandler implements RestExternalTaskHandler {
    @Autowired
    IDPayRestService idPayRestService;
    @Autowired
    RestConfigurationProperties restConfigurationProperties;
    @Value("${camunda.bpm.client.max-tasks}")
    private int maxTasksConfig;
    @Autowired
    @Qualifier("#{adapter-pool-configuration.rest.name}")
    private TaskExecutor taskRestExecutor;
    @Autowired
    @Qualifier("#{adapter-pool-configuration.completion.name}")
    private TaskExecutor taskComplExecutor;

    @Override
    public TaskExecutor getTaskRestExecutor() {
        return this.taskRestExecutor;
    }

    @Override
    public TaskExecutor getTaskComplExecutor() {
        return this.taskComplExecutor;
    }

    @Override
    public int getMaxTasks() {
        return this.maxTasksConfig;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public RestConfigurationProperties getRestConfigurationProperties() {
        return restConfigurationProperties;
    }

    @Override
    public GenericRestService getRestService() {
        return this.idPayRestService;
    }

    @Override
    public boolean isMILFlow() {
        return true;
    }

    @Override
    public Configuration getHttpConfiguration(Map<String, Object> variables) {
        return EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCall(variables, isMILFlow());
    }
}
