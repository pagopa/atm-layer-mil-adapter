package it.gov.pagopa.miladapter.engine.task.impl;

import it.gov.pagopa.miladapter.engine.task.RestExternalServiceHandler;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.services.GenericRestExternalService;
import it.gov.pagopa.miladapter.services.MILRestExternalService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MILRestServiceHandler extends GenericServiceHandler implements RestExternalServiceHandler {

    @Autowired
    MILRestExternalService milRestService;

    @Override
    public GenericRestExternalService getRestService() {
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
        return EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCallNew(variables);
    }

}
