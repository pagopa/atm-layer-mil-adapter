package it.gov.pagopa.miladapter.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ExternalCallService extends GenericRestService {
     ResponseEntity<String> executeExternalCall (Map<String, Object> body) throws JsonProcessingException;

}
