package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.model.HTTPConfiguration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.MILRestService;
import it.gov.pagopa.miladapter.services.TokenService;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
@Slf4j
public class MILRestServiceImpl implements MILRestService {

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    @Autowired
    RestTemplate restTemplate;


    @Autowired
    TokenService tokenService;

    @Override
    public VariableMap executeMILRestCall(HTTPConfiguration httpConfiguration) {

        tokenService.injectAuthToken(httpConfiguration.getHeaders(), httpConfiguration.getAuthParameters());
        URI url = HttpRequestUtils.buildURI(restConfigurationProperties.getMilBasePath(), httpConfiguration.getEndpoint(), httpConfiguration.getPathParams());
        HttpEntity<String> request = HttpRequestUtils.buildHttpEntity(httpConfiguration.getBody(), httpConfiguration.getHeaders());


        ResponseEntity<String> response = restTemplate
                .exchange(url, httpConfiguration.getHttpMethod(), request, String.class);

        VariableMap output = Variables.createVariables();
        output.putValue(HttpVariablesEnum.RESPONSE.getValue(), response.getBody());
        output.putValue(HttpVariablesEnum.STATUS_CODE.getValue(), response.getStatusCode().value());

        return output;
    }
}
