package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.model.HTTPConfiguration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.MILRestService;
import it.gov.pagopa.miladapter.services.TokenService;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@Slf4j
public class MILRestServiceImpl implements MILRestService {

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    @Autowired
    RestTemplateGenerator restTemplateGenerator;


    @Autowired
    TokenService tokenService;

    @Override
    public void injectAuthToken(HTTPConfiguration httpConfiguration) {
        tokenService.injectAuthToken(httpConfiguration.getHeaders(), httpConfiguration.getAuthParameters());
    }

    @Override
    public URI prepareUri(HTTPConfiguration httpConfiguration) {
        return HttpRequestUtils.buildURI(restConfigurationProperties.getMilBasePath(), httpConfiguration.getEndpoint(), httpConfiguration.getPathParams());
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
    public HttpEntity<String> buildHttpEntity(HTTPConfiguration httpConfiguration) {
        return HttpRequestUtils.buildHttpEntity(httpConfiguration.getBody(), httpConfiguration.getHeaders());
    }
}
