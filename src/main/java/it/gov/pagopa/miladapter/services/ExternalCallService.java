package it.gov.pagopa.miladapter.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ExternalCallService extends GenericRestService {
     ResponseEntity<String> executeCallAndCallBack (Map<String, Object> body) throws JsonProcessingException;

     CompletableFuture<Void> executeAsyncTask(Map<String, Object> body);
}
