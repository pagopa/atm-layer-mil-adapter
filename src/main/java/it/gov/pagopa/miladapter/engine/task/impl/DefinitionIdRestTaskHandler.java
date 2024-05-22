package it.gov.pagopa.miladapter.engine.task.impl;

import it.gov.pagopa.miladapter.engine.task.RestExternalTaskHandler;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.services.DefinitionIdRestService;
import it.gov.pagopa.miladapter.services.GenericRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@ExternalTaskSubscription(includeExtensionProperties = true, topicName = "definition-id-adapter")
public class DefinitionIdRestTaskHandler extends GenericHandler implements RestExternalTaskHandler {

    @Autowired
    DefinitionIdRestService modelRestService;

    @Override
    public GenericRestService getRestService() {
        return this.modelRestService;
    }

    @Override
    public boolean isMILFlow() {
        return false;
    }

    @Override
    public boolean isIdPayFlow() {
        return false;
    }

    @Override
    public Configuration getHttpConfiguration(Map<String, Object> variables) {
        return EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationInternalCall(variables, isMILFlow());
    }

}
