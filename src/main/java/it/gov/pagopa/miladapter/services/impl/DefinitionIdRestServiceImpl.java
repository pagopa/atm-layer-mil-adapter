package it.gov.pagopa.miladapter.services.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.DefinitionIdRestService;
import it.gov.pagopa.miladapter.services.TokenService;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefinitionIdRestServiceImpl implements DefinitionIdRestService {

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    @Autowired
    RestTemplateGenerator restTemplateGenerator;

    @Autowired
    TokenService tokenService;

    @Override
    public void injectAuthToken(Configuration configuration) {
    }

    @Override
    public URI prepareUri(Configuration configuration) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put(RequiredProcessVariables.ACQUIRER_ID.getModelValue(),
                configuration.getAuthParameters().getAcquirerId());
        pathParams.put(RequiredProcessVariables.BRANCH_ID.getModelValue(),
                configuration.getAuthParameters().getBranchId() != null
                        ? configuration.getAuthParameters().getBranchId()
                        : "_");
        pathParams.put(RequiredProcessVariables.TERMINAL_ID.getModelValue(),
                configuration.getAuthParameters().getTerminalId() != null
                        ? configuration.getAuthParameters().getTerminalId()
                        : configuration.getAuthParameters().getAcquirerId()
                                + configuration.getAuthParameters().getCode());
        pathParams.put(RequiredProcessVariables.FUNCTION_ID.getModelValue(), configuration.getFunction());

        return HttpRequestUtils.buildURI(restConfigurationProperties.getModelBasePath(),
                restConfigurationProperties.getDefinitionIdProperties().getUrl(),
                pathParams);
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public RestConfigurationProperties getRestConfigurationProperties() {
        return this.restConfigurationProperties;
    }

    @Override
    public RestTemplateGenerator getRestTemplateGenerator() {
        return restTemplateGenerator;
    }

    @Override
    public HttpEntity<String> buildHttpEntity(Configuration configuration) {
        return HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
    }
}
