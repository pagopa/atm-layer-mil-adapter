package it.gov.pagopa.miladapter.engine.task.impl;

import it.gov.pagopa.miladapter.engine.task.RestExternalTaskHandler;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

@Service
@Slf4j
public abstract class GenericHandler implements RestExternalTaskHandler {

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


}
