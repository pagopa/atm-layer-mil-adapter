package it.gov.pagopa.miladapter.services;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ExternalCallService extends GenericRestService {
    public void executeCallAndCallBack (Map<String, Object> body);

    public CompletableFuture<Void> executeAsyncTask(Map<String, Object> body);
}
