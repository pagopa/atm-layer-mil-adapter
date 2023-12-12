package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.impl.GenericRestServiceNoAuthImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

class GenericRestServiceTest {

    @Mock
    RestConfigurationProperties restConfigurationProperties;

    @Mock
    RestTemplateGenerator restTemplateGenerator;

    @Mock
    RestTemplate restTemplateMock;

    @InjectMocks
    GenericRestServiceNoAuthImpl genericRestService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecuteRestCallSuccess() {
        Configuration configuration = mock(Configuration.class);
        configuration.setHttpMethod(GET);

        when(restConfigurationProperties.getAsyncThreshold()).thenReturn(1000L);
        when(configuration.getDelayMilliseconds()).thenReturn(100);
        when(configuration.getEndpoint()).thenReturn("http://localtest");
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(restTemplateMock);

        ResponseEntity<String> successfulResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplateMock.exchange(any(URI.class), any(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(successfulResponse);

        VariableMap output = genericRestService.executeRestCall(configuration);
        assertEquals(200, output.get(HttpVariablesEnum.STATUS_CODE.getValue()));
        assertEquals("Success", output.get(HttpVariablesEnum.RESPONSE.getValue()));
    }


    @Test
    void testExecuteRestCallFailure() {
        Configuration configuration = mock(Configuration.class);

        when(configuration.getDelayMilliseconds()).thenReturn(600);
        when(restConfigurationProperties.getAsyncThreshold()).thenReturn(1000L);
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(restTemplateMock);
        when(restTemplateMock.exchange(any(URI.class), any(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        VariableMap output = genericRestService.executeRestCall(configuration);
        assertEquals(500 ,output.get(HttpVariablesEnum.STATUS_CODE.getValue()));
    }
}
