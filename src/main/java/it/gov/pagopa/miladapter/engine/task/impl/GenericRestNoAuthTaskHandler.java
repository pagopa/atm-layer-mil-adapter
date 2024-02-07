package it.gov.pagopa.miladapter.engine.task.impl;

import java.util.Map;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import it.gov.pagopa.miladapter.engine.task.RestExternalTaskHandler;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.GenericRestService;
import it.gov.pagopa.miladapter.services.GenericRestServiceNoAuth;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ExternalTaskSubscription(includeExtensionProperties = true, topicName = "rest-adapter-no-auth")
public class GenericRestNoAuthTaskHandler implements RestExternalTaskHandler {

    @Autowired
    GenericRestServiceNoAuth genericRestServiceNoAuth;

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
    public TaskExecutor getTaskRestExecutor(){
        return this.taskRestExecutor;
    }

    @Override
    public TaskExecutor getTaskComplExecutor(){
        return this.taskComplExecutor;
    }

    @Override
    public int getMaxTasks() {
        return this.maxTasksConfig;
    }

    public Logger getLogger() {
        return log;
    }

    @Override
    public RestConfigurationProperties getRestConfigurationProperties() {
        return restConfigurationProperties;
    }

    @Override
    public GenericRestService getRestService() {
        return genericRestServiceNoAuth;
    }

    @Override
    public boolean isMILFlow() {
        return false;
    }

    @Override
    public Configuration getHttpConfiguration(Map<String, Object> variables) {
        return EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCall(variables, isMILFlow());
    }

}
