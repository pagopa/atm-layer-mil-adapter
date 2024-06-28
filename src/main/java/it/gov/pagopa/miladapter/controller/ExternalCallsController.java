package it.gov.pagopa.miladapter.controller;

import it.gov.pagopa.miladapter.dto.CamundaWaitMessage;
import it.gov.pagopa.miladapter.dto.ProcessVariable;
import it.gov.pagopa.miladapter.engine.task.impl.MILRestServiceHandler;
import it.gov.pagopa.miladapter.services.CallbackCamundaService;
import it.gov.pagopa.miladapter.services.MILRestExternalService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/externalcall")
@Slf4j
public class ExternalCallsController {

    @Autowired
    MILRestExternalService milRestExternalService;

    @Autowired
    MILRestServiceHandler milRestServiceHandler;

    @Autowired
    CallbackCamundaService camundaService;

    @PostMapping
    public ResponseEntity<String> externalcall(@RequestBody Map<String, Object> body) {
        body.forEach((key, value) ->
                System.out.println("key: " + key + " value: " + value.toString())
        );

        // Log before starting async method
        log.info("Starting async execution");
        executeAsync(body);
        // Log after starting async method
        log.info("Async execution started");

        return new ResponseEntity<>("Headers processed successfully", HttpStatus.OK);
    }

    @Async
    public CompletableFuture<Void> executeAsync(Map<String, Object> body) {
        return milRestServiceHandler.execute(body, milRestExternalService)
                .thenAcceptAsync(response -> invokeCallback(body, response))
                .exceptionally(ex -> {
                    log.error("Error in async execution", ex);
                    return null;
                });
    }

    private void invokeCallback(Map<String, Object> body, VariableMap response) {
        try {
            CamundaWaitMessage camundaWaitMessage = createCallbackPayload(body, response);
            camundaService.callAdapter(camundaWaitMessage);
        } catch (Exception e) {
            log.error("Error while invoking callback", e);
        }
    }

    private CamundaWaitMessage createCallbackPayload(Map<String, Object> body, VariableMap response) {
        CamundaWaitMessage camundaWaitMessage = new CamundaWaitMessage();
        camundaWaitMessage.setMessageName(UUID.randomUUID().toString());
        camundaWaitMessage.setProcessInstanceId((String) body.get("responseEventMessage"));

        String responseBody = response.getValueTyped("response").getValue().toString();
        String statusCode = response.getValueTyped("statusCode").getValue().toString();

        camundaWaitMessage.setProcessVariables(Map.of(
                "statusCode", new ProcessVariable(statusCode, "String"),
                "response", new ProcessVariable(responseBody, "String")
        ));
        return camundaWaitMessage;
    }
}
