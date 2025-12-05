package it.gov.pagopa.miladapter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.miladapter.services.ExternalCallService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/externalcall")
@Slf4j
public class ExternalCallsController {


    @Autowired
    ExternalCallService externalCallService;

    @PostMapping
    public ResponseEntity<String> externalcall(@RequestBody Map<String, Object> body) throws JsonProcessingException {
        // Log before starting async method
        log.info("Starting call external");
        return externalCallService.executeExternalCall(body);
    }

}
