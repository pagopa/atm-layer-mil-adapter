package it.gov.pagopa.miladapter.services.impl;

import io.opentelemetry.api.trace.Tracer;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.GenericRestExternalService;
import it.gov.pagopa.miladapter.services.TokenService;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public abstract class GenericRestExternalServiceAbstract implements GenericRestExternalService {

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    @Autowired
    RestTemplateGenerator restTemplateGenerator;

    @Autowired
    TokenService tokenService;

    @Autowired
    Tracer tracer;

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public Tracer getTracer() {
        return this.tracer;
    }

    @Override
    public RestTemplateGenerator getRestTemplateGenerator() {
        return restTemplateGenerator;
    }

    @Override
    public HttpEntity<String> buildHttpEntity(Configuration configuration) {
        return HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
    }
    @Override
    public RestConfigurationProperties getRestConfigurationProperties() {
        return this.restConfigurationProperties;
    }
}
