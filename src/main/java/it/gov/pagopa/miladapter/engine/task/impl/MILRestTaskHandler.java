package it.gov.pagopa.miladapter.engine.task.impl;

import java.util.Map;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.gov.pagopa.miladapter.engine.task.RestExternalTaskHandler;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.GenericRestService;
import it.gov.pagopa.miladapter.services.MILRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ExternalTaskSubscription(includeExtensionProperties = true, topicName = "mil-adapter")
public class MILRestTaskHandler implements RestExternalTaskHandler {

    @Autowired
    MILRestService milRestService;

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
        return this.milRestService;
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
