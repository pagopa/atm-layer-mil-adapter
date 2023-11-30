package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.impl.GenericRestServiceNoAuthImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenericRestServiceTest {

    @Mock
    private RestTemplateGenerator restTemplateGenerator;

    @InjectMocks
    private GenericRestServiceNoAuthImpl genericRestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecuteRestCallSuccess() {
        Configuration configuration = new Configuration();

        RestTemplate restTemplateMock = mock(RestTemplate.class);
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(restTemplateMock);

        ResponseEntity<String> successfulResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplateMock.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(successfulResponse);

        VariableMap output = genericRestService.executeRestCall(configuration);
    }

    @Test
    public void testExecuteRestCallFailure() {
        Configuration configuration = new Configuration();

        RestTemplate restTemplateMock = mock(RestTemplate.class);
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(restTemplateMock);

        when(restTemplateMock.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        VariableMap output = genericRestService.executeRestCall(configuration);

    }

}
