package it.gov.pagopa.miladapter.services.impl;

import io.opentelemetry.api.trace.Tracer;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.GenericRestServiceNoAuth;
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
public class GenericRestServiceNoAuthImpl implements GenericRestServiceNoAuth {

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    @Autowired
    RestTemplateGenerator restTemplateGenerator;

    @Autowired
    TokenService tokenService;

    @Autowired
    Tracer tracer;

    @Override
    public void injectAuthToken(Configuration configuration) {
    }

    @Override
    public URI prepareUri(Configuration configuration) {
        return HttpRequestUtils.buildURI(configuration.getEndpoint(), configuration.getPathParams());
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public io.opentelemetry.api.trace.Tracer getTracer() {
        return this.tracer;
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
