package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.HTTPConfiguration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.impl.MILRestServiceImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MILRestServiceImplTest {
    @Mock
    private RestConfigurationProperties restConfigurationProperties;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private TokenService tokenService;
    @InjectMocks
    MILRestServiceImpl milRestService;
    private HTTPConfiguration httpConfiguration;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        httpConfiguration = new HTTPConfiguration();
        httpConfiguration.setEndpoint("/test");
        httpConfiguration.setHttpMethod(HttpMethod.GET);
        httpConfiguration.setHeaders(new HttpHeaders());
        httpConfiguration.setPathParams(new HashMap<>());
        httpConfiguration.setAuthParameters(new AuthParameters());
        when(restConfigurationProperties.getMilBasePath()).thenReturn("http://test-url:8080");
    }

    @Test
    public void executeMILRestCallTestOK() {
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(new ResponseEntity("test response", HttpStatus.OK));
        VariableMap output = milRestService.executeRestCall(httpConfiguration);
        assertEquals("test response", output.get("response"));
        assertEquals(200, output.get("statusCode"));
    }

    @Test
    public void executeMILRestCallTestKO() {
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(new ResponseEntity("BAD REQUEST", HttpStatus.BAD_REQUEST));
        VariableMap output = milRestService.executeRestCall(httpConfiguration);
        assertEquals("BAD REQUEST", output.get("response"));
        assertEquals(400, output.get("statusCode"));
    }
}
