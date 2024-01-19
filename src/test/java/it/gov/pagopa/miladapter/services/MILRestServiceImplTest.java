package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class MILRestServiceImplTest {
    @Mock
    private RestConfigurationProperties restConfigurationProperties;
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TokenService tokenService;
    @Mock
    private RestTemplateGenerator restTemplateGenerator;
    @InjectMocks
    MILRestServiceImpl milRestService;
    private Configuration configuration;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        configuration = new Configuration();
        configuration.setEndpoint("/test");
        configuration.setHttpMethod(HttpMethod.GET);
        configuration.setHeaders(new HttpHeaders());
        configuration.setPathParams(new HashMap<>());
        configuration.setAuthParameters(new AuthParameters());
        when(restConfigurationProperties.getMilBasePath()).thenReturn("http://test-url:8080");
    }

    @Test
    void executeMILRestCallTestOK() {
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(new ResponseEntity("test response", HttpStatus.OK));
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(restTemplate);
        VariableMap output = milRestService.executeRestCall(configuration);
        assertEquals("test response", output.get("response"));
        assertEquals(200, output.get("statusCode"));
    }

    @Test
    void executeMILRestCallTestKO() {
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(new ResponseEntity("BAD REQUEST", HttpStatus.BAD_REQUEST));
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(restTemplate);
        VariableMap output = milRestService.executeRestCall(configuration);
        assertEquals("BAD REQUEST", output.get("response"));
        assertEquals(400, output.get("statusCode"));
    }
}
