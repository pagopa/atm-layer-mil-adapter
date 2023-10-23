package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.model.HTTPConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public interface GenericRestService {

    default VariableMap executeRestCall(HTTPConfiguration configuration) {
        this.injectAuthToken(configuration);
        ResponseEntity<String> response = this.getRestTemplate()
                .exchange(this.prepareUri(configuration), configuration.getHttpMethod(), this.buildHttpEntity(configuration), String.class);
        VariableMap output = Variables.createVariables();
        output.putValue(HttpVariablesEnum.RESPONSE.getValue(), response.getBody());
        output.putValue(HttpVariablesEnum.STATUS_CODE.getValue(), response.getStatusCode().value());
        return output;
    }

    void injectAuthToken(HTTPConfiguration configuration);

    URI prepareUri(HTTPConfiguration configuration);

    <T> HttpEntity<T> buildHttpEntity(HTTPConfiguration configuration);

    RestTemplate getRestTemplate();
}
