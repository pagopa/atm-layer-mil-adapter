package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class IDPayRestServiceImplTest {
    @Mock
    RestConfigurationProperties restConfigurationProperties;
    @Mock
    RestTemplateGenerator restTemplateGenerator;
    @InjectMocks
    IDPayRestServiceImpl idPayRestService;

    private final Configuration configuration=new Configuration();

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        configuration.setHeaders(new HttpHeaders());
        configuration.setAuthParameters(new AuthParameters());
        configuration.setBody("body");
        configuration.setEndpoint("endpoint");
        configuration.setPathParams(new HashMap<>());
    }

    @Test
    void injectAuthToken() {
        when(restConfigurationProperties.getIdPayBasePath()).thenReturn("basepath/");
        assertEquals(idPayRestService.prepareUri(configuration), (UriComponentsBuilder.fromUriString("basepath/endpoint").build().toUri()));
    }

    @Test
    void prepareUriTest() {
        when(restConfigurationProperties.getIdPayBasePath()).thenReturn("basepath/");
        assertEquals(idPayRestService.prepareUri(configuration), (UriComponentsBuilder.fromUriString("basepath/endpoint").build().toUri()));
    }

    @Test
    void getLoggerTest() {
        assertInstanceOf(Logger.class,idPayRestService.getLogger());
    }

    @Test
    void getRestConfigurationPropertiesTest() {
        assertEquals(restConfigurationProperties,idPayRestService.getRestConfigurationProperties());
    }

    @Test
    void getRestTemplateGeneratorTest() {
        assertEquals(restTemplateGenerator,idPayRestService.getRestTemplateGenerator());
    }

    @Test
    void buildHttpEntityTest() {
        HttpEntity<String> result=idPayRestService.buildHttpEntity(configuration);
        assertEquals("body",result.getBody());
        assertEquals(configuration.getHeaders(),result.getHeaders());
    }
}