package it.gov.pagopa.miladapter.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.miladapter.dto.CamundaWaitMessage;

public interface CallbackCamundaService {

     String callAdapter(CamundaWaitMessage camundaWaitMessage) throws JsonProcessingException;
}
