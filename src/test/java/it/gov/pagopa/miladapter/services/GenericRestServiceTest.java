package it.gov.pagopa.miladapter.services;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
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
import static org.mockito.ArgumentMatchers.*;
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

    @Mock
    Tracer tracer;

    @InjectMocks
    GenericRestServiceNoAuthImpl genericRestService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        when(tracer.spanBuilder(any())).thenReturn(spanBuilder);
        Span span = mock(Span.class);
        when(spanBuilder.startSpan()).thenReturn(span);
    }

    /*@Test
    void testExecuteRestCallInterruptedException(){
        Configuration configuration = mock(Configuration.class);
        when(configuration.getDelayMilliseconds()).thenReturn(2000);
        when(Thread.sleep(anyLong())).thenThrow(InterruptedException.class);
        assertThrows(RuntimeException.class, () -> genericRestService.executeRestCall(configuration));
    }
*/

    @Test
    void testExecuteRestCallWithBodyAttribute(){
        Configuration configuration = mock(Configuration.class);
        HttpEntity<String> entity = mock(HttpEntity.class);
        when(configuration.getDelayMilliseconds()).thenReturn(100);
        when(entity.hasBody()).thenReturn(true);
        when(entity.getBody()).thenReturn("TestBody");
        genericRestService.executeRestCall(configuration);
       // verify(serviceSpan).setAttribute(eq("http.body"), eq("TestBody"));
    }

    @Test
    void testSpanBuilderWithParentSpanContext(){
        Configuration configuration=mock(Configuration.class);
        when(configuration.getParentSpanContextString()).thenReturn("{\"traceId\":\"123\",\"spanId\":\"456\"}");
        genericRestService.spanBuilder(configuration);
        // verify(spanBuilder).setParent(any(Context.class));
    }

    @Test
    void testSpanBuilderWithoutParentSpanContext() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getParentSpanContextString()).thenReturn("");
        genericRestService.spanBuilder(configuration);
        // verify(spanBuilder, never()).setParent(any(Context.class));
    }

    @Test
    void testExecuteRestCallSuccess() {
        Configuration configuration = mock(Configuration.class);

        when(restConfigurationProperties.getAsyncThreshold()).thenReturn(1000L);
        when(configuration.getDelayMilliseconds()).thenReturn(100);
        when(configuration.getEndpoint()).thenReturn("http://localtest");
        when(configuration.getHttpMethod()).thenReturn(GET);
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(restTemplateMock);

        ResponseEntity<String> successfulResponse = new ResponseEntity<>("{\"output\":\"SUCCESS\"}", HttpStatus.OK);
        when(restTemplateMock.exchange(any(URI.class), any(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(successfulResponse);

        VariableMap output = genericRestService.executeRestCall(configuration);
        assertEquals(200, output.get(HttpVariablesEnum.STATUS_CODE.getValue()));
        assertEquals("{\"output\":\"SUCCESS\"}", output.get(HttpVariablesEnum.RESPONSE.getValue()).toString());
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
