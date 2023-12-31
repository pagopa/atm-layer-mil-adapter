package it.gov.pagopa.miladapter.services;

import camundajar.impl.com.google.gson.JsonObject;
import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.json.SpinJsonNode;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.camunda.spin.Spin.JSON;

public interface GenericRestService {

    default VariableMap executeRestCall(Configuration configuration) {

        this.injectAuthToken(configuration);
        ResponseEntity<String> response;

        try {
            if (configuration.getDelayMilliseconds() != null) {
                try {
                    getLogger().info("Starting delay of {}, at {}",configuration.getDelayMilliseconds(),System.currentTimeMillis());
                    Thread.sleep(configuration.getDelayMilliseconds());
                    getLogger().info("End of delay at {}", System.currentTimeMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            response = this.getRestTemplate(configuration)
                    .exchange(this.prepareUri(configuration), configuration.getHttpMethod(), this.buildHttpEntity(configuration), String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            getLogger().error("Exception in HTTP request: ", e);
            response = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            getLogger().error("Exception in HTTP request: ", e);
            response = new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        VariableMap output = Variables.createVariables();
        if (response.getBody() == null) {
            response = new ResponseEntity<>(new JsonObject().toString(), response.getStatusCode());
        }
        output.putValue(HttpVariablesEnum.RESPONSE.getValue(), response.getBody());
        output.putValue(HttpVariablesEnum.STATUS_CODE.getValue(), response.getStatusCode().value());
        SpinJsonNode headersJsonNode = JSON(response.getHeaders());
        output.putValue(HttpVariablesEnum.RESPONSE_HEADERS.getValue(), headersJsonNode.toString());
        return output;
    }

    void injectAuthToken(Configuration configuration);

    URI prepareUri(Configuration configuration);

    Logger getLogger();

    RestConfigurationProperties getRestConfigurationProperties();

    RestTemplateGenerator getRestTemplateGenerator();

    <T> HttpEntity<T> buildHttpEntity(Configuration configuration);

    default RestTemplate getRestTemplate(Configuration configuration) {
        HttpRequestUtils.getRestFactoryConfigsOrDefault(configuration, getRestConfigurationProperties());
        return getRestTemplateGenerator().generate(configuration.getConnectionRequestTimeoutMilliseconds(), configuration.getConnectionResponseTimeoutMilliseconds(),
                configuration.getMaxRetry(), configuration.getRetryIntervalMilliseconds());
    }
}
