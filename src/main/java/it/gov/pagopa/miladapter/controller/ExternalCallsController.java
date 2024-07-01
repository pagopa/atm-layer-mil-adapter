package it.gov.pagopa.miladapter.controller;

import it.gov.pagopa.miladapter.dto.CamundaWaitMessage;
import it.gov.pagopa.miladapter.dto.ProcessVariable;
import it.gov.pagopa.miladapter.engine.task.impl.MILRestServiceHandler;
import it.gov.pagopa.miladapter.services.CallbackCamundaService;
import it.gov.pagopa.miladapter.services.ExternalCallService;
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
    ExternalCallService externalCallService;

    @PostMapping
    public ResponseEntity<String> externalcall(@RequestBody Map<String, Object> body) {
        // Log before starting async method
        log.info("Starting async execution");
        externalCallService.executeCallAndCallBack(body);
        // Log after starting async method
        log.info("Async execution started");

        return new ResponseEntity<>("Headers processed successfully", HttpStatus.OK);
    }

}
