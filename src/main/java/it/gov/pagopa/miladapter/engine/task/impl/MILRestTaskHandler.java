package it.gov.pagopa.miladapter.engine.task.impl;

import it.gov.pagopa.miladapter.engine.task.RestExternalTaskHandler;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.services.GenericRestService;
import it.gov.pagopa.miladapter.services.MILRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@ExternalTaskSubscription(includeExtensionProperties = true, topicName = "mil-adapter")
public class MILRestTaskHandler extends GenericHandler implements RestExternalTaskHandler {

    @Autowired
    MILRestService milRestService;

    @Override
    public GenericRestService getRestService() {
        return this.milRestService;
    }

    @Override
    public boolean isMILFlow() {
        return true;
    }

    @Override
    public boolean isIdPayFlow() {
        return false;
    }

    @Override
    public Configuration getHttpConfiguration(Map<String, Object> variables) {
        return EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCall(variables, isMILFlow(),isIdPayFlow());
    }

}
