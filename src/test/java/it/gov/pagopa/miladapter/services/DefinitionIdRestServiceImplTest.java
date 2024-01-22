package it.gov.pagopa.miladapter.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.DefinitionIdProperties;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.impl.DefinitionIdRestServiceImpl;

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
                .thenReturn(new ResponseEntity("test response", HttpStatus.OK));
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(restTemplate);
        VariableMap output = definitionIdRestService.executeRestCall(configuration);
        assertEquals("test response", output.get("response"));
        assertEquals(200, output.get("statusCode"));
    }

    @Test
    void executeDefinitionIdCallTestOKNoBranchNoTerminalId() {
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity("test response", HttpStatus.OK));
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(restTemplate);
        VariableMap output = definitionIdRestService.executeRestCall(configuration);
        assertEquals("test response", output.get("response"));
        assertEquals(200, output.get("statusCode"));
    }

    @Test
    void executeMILRestCallTestKO() {
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(new ResponseEntity("BAD REQUEST", HttpStatus.BAD_REQUEST));
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(restTemplate);
        VariableMap output = definitionIdRestService.executeRestCall(configuration);
        assertEquals("BAD REQUEST", output.get("response"));
        assertEquals(400, output.get("statusCode"));
    }
}
