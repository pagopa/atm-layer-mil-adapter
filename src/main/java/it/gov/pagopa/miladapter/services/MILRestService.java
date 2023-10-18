package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.model.HTTPConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;

import java.util.Map;

public interface MILRestService {

    VariableMap executeMILRestCall(HTTPConfiguration configuration);
}
