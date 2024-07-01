package it.gov.pagopa.miladapter.services;

import java.util.Map;

public interface ExternalCallService extends GenericRestService {
    public void executeCallAndCallBack (Map<String, Object> body);
}
