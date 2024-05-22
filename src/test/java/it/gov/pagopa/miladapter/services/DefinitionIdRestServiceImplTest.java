package it.gov.pagopa.miladapter.services;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.DefinitionIdProperties;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.impl.DefinitionIdRestServiceImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefinitionIdRestServiceImplTest {

    @Mock
    private RestConfigurationProperties restConfigurationProperties;
    @Mock
    private RestTemplate restTemplate;

    @Mock
    Tracer tracer;

    @Mock
    private TokenService tokenService;
    @Mock
    private RestTemplateGenerator restTemplateGenerator;
    @InjectMocks
    DefinitionIdRestServiceImpl definitionIdRestService;
    private Configuration configuration;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        when(tracer.spanBuilder(any())).thenReturn(spanBuilder);
        Span span = mock(Span.class);
        when(spanBuilder.startSpan()).thenReturn(span);
        configuration = new Configuration();
        configuration.setHttpMethod(HttpMethod.GET);
        configuration.setHeaders(new HttpHeaders());
        configuration.setPathParams(new HashMap<>());
        configuration.setAuthParameters(new AuthParameters());
        configuration.getAuthParameters().setAcquirerId("12345");
        DefinitionIdProperties dip = Mockito.mock(DefinitionIdProperties.class);
        when(restConfigurationProperties.getModelBasePath()).thenReturn("http://test-url:8080");
        when(restConfigurationProperties.getDefinitionIdProperties()).thenReturn(dip);
        when(restConfigurationProperties.getDefinitionIdProperties().getMethod()).thenReturn("GET");
        when(restConfigurationProperties.getDefinitionIdProperties().getUrl())
                .thenReturn("/bpmn/function/{functionType}/bank/{acquirerId}/branch/{branchId}/terminal/{terminalId}");
    }

    @Test
    void executeDefinitionIdCallTestOK() {
        configuration.getAuthParameters().setBranchId("1234");
        configuration.getAuthParameters().setTerminalId("12345678");
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity("{\"output\":\"test\"}", HttpStatus.OK));
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(restTemplate);
        VariableMap output = definitionIdRestService.executeRestCall(configuration);
        assertEquals("{\"output\":\"test\"}", output.get("response").toString());
        assertEquals(200, output.get("statusCode"));
    }

    @Test
    void executeDefinitionIdCallTestOKNoBranchNoTerminalId() {
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity("{\"output\":\"test\"}", HttpStatus.OK));
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(restTemplate);
        VariableMap output = definitionIdRestService.executeRestCall(configuration);
        assertEquals("{\"output\":\"test\"}", output.get("response").toString());
        assertEquals(200, output.get("statusCode"));
    }

    @Test
    void executeMILRestCallTestKO() {
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(new ResponseEntity("{\"output\":\"BAD REQUEST\"}", HttpStatus.BAD_REQUEST));
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(restTemplate);
        VariableMap output = definitionIdRestService.executeRestCall(configuration);
        assertEquals("{}", output.get("response").toString());
        assertEquals(400, output.get("statusCode"));
    }

    @Test
    void getLoggerTest(){
        assertInstanceOf(Logger.class,definitionIdRestService.getLogger());
    }
}
