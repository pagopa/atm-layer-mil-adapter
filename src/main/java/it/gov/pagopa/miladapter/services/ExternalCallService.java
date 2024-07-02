package it.gov.pagopa.miladapter.services;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ExternalCallService extends GenericRestService {
    CompletableFuture<Void> externalCallAndCallback(Map<String, Object> body);

}
