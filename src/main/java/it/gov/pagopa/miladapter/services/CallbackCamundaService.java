package it.gov.pagopa.miladapter.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.miladapter.dto.CamundaWaitMessage;
import org.springframework.web.client.RestTemplate;

public interface CallbackCamundaService {

     String callAdapter(RestTemplate restTemplate,CamundaWaitMessage camundaWaitMessage) throws JsonProcessingException;
}
