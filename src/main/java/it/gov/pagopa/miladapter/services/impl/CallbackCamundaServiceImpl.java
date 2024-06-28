package it.gov.pagopa.miladapter.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.miladapter.dto.CamundaWaitMessage;
import it.gov.pagopa.miladapter.services.CallbackCamundaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class CallbackCamundaServiceImpl implements CallbackCamundaService {


    @Value("${camunda.bpm.client.base-url}")
    private String apiUrl;
    @Autowired
    private ObjectMapper objectMapper;

    private static String ENDPOINT = "/message";

    public String callAdapter(CamundaWaitMessage camundaWaitMessage) throws JsonProcessingException {

        // Convertire il body in JSON
        String jsonBody = objectMapper.writeValueAsString(camundaWaitMessage);

        // Impostare gli headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Creare l'entity con il body e gli headers
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(apiUrl+ENDPOINT, HttpMethod.POST, entity, String.class);
        return response.getBody();

    }

}
