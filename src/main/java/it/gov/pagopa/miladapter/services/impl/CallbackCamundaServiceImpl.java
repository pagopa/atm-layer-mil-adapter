package it.gov.pagopa.miladapter.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.miladapter.dto.CamundaWaitMessage;
import it.gov.pagopa.miladapter.services.CallbackCamundaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class CallbackCamundaServiceImpl implements CallbackCamundaService {


    @Value("${camunda.bpm.client.base-url}")
    private String apiUrl;
    @Value("${camunda.bpm.client.basic-auth.username}")
    private String username;
    @Value("${camunda.bpm.client.basic-auth.password}")
    private String password;

    @Autowired
    private ObjectMapper objectMapper;

    private static String ENDPOINT = "/message";

    public String callAdapter(RestTemplate restTemplate,CamundaWaitMessage camundaWaitMessage) throws JsonProcessingException {

        // Convertire il body in JSON
        String jsonBody = objectMapper.writeValueAsString(camundaWaitMessage);

        // Impostare gli headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        String token = Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + token);

        // Creare l'entity con il body e gli headers
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl+ENDPOINT, HttpMethod.POST, entity, String.class);
        return response.getBody();

    }

}
