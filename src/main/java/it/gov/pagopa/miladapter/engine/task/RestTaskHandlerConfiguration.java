package it.gov.pagopa.miladapter.engine.task;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@ExternalTaskSubscription(
        includeExtensionProperties = true,
        topicName = "mil-adapter")
public class RestTaskHandlerConfiguration implements ExternalTaskHandler {

    @SneakyThrows
    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.info("{}: The External Task {} has been invoked!", this.getClass().getName(), externalTask.getId());
        Map<String, Object> variables = externalTask.getAllVariablesTyped();
        String urlVariable = (String) variables.get("url");
        Map<String, String> pathParams = new HashMap<>();
        if (variables.containsKey("pathParams")) {
            pathParams = (Map<String, String>) variables.get("pathParams");
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(urlVariable);
        URI url = builder.buildAndExpand(pathParams).toUri();
        String body = externalTask.getVariable("body");
        Map<String, String> headersMap = externalTask.getVariable("headers");
        MultiValueMap<String, String> headersMultiValueMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : headersMap.entrySet())
            headersMultiValueMap.put(entry.getKey(), Collections.singletonList(entry.getValue()));

        HttpHeaders headers = new HttpHeaders(headersMultiValueMap);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate
                .exchange(url, HttpMethod.GET, request, String.class);

        log.info(response.toString());
        Thread.sleep(20000);
        VariableMap output = Variables.createVariables();
        output.putValue("response", response.getBody());
        output.putValue("statusCode", response.getStatusCodeValue());
        externalTaskService.complete(externalTask, output);
    }
}
