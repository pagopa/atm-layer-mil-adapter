package it.gov.pagopa.miladapter.engine.task.impl;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.gov.pagopa.miladapter.engine.task.RestExternalTaskHandler;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.GenericRestService;
import it.gov.pagopa.miladapter.services.ModelRestService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ExternalTaskSubscription(includeExtensionProperties = true, topicName = "model-adapter")
public class ModelRestTaskHandler implements RestExternalTaskHandler {

    @Autowired
    ModelRestService modelRestService;

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

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
        return this.modelRestService;
    }

    @Override
    public boolean isMILFlow() {
        return false;
    }

}
