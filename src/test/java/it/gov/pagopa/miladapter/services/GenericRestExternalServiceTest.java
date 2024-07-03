package it.gov.pagopa.miladapter.services;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GenericRestExternalServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private Tracer tracer;

    @Mock
    private RestConfigurationProperties restConfigurationProperties;

    @Mock
    private RestTemplateGenerator restTemplateGenerator;

    private GenericRestExternalService genericRestExternalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        genericRestExternalService = new GenericRestExternalService() {
            @Override
            public URI prepareUri(Configuration configuration) {
                return URI.create("http://example.com");
            }

            @Override
            public Logger getLogger() {
                return logger;
            }

            @Override
            public Tracer getTracer() {
                return tracer;
            }

            @Override
            public RestConfigurationProperties getRestConfigurationProperties() {
                return restConfigurationProperties;
            }

            @Override
            public RestTemplateGenerator getRestTemplateGenerator() {
                return restTemplateGenerator;
            }

            @Override
            public <T> HttpEntity<T> buildHttpEntity(Configuration configuration) {
                return new HttpEntity<>(null);
            }
        };
    }

    @Test
    void testGetRestTemplate() {
        Configuration configuration = new Configuration();
        configuration.setConnectionRequestTimeoutMilliseconds(5000);
        configuration.setConnectionResponseTimeoutMilliseconds(3000);
        configuration.setMaxRetry(3);
        configuration.setRetryIntervalMilliseconds(1000);

        RestTemplate restTemplate = new RestTemplate();
        when(restTemplateGenerator.generate(5000, 3000, 3, 1000)).thenReturn(restTemplate);

        RestTemplate result = genericRestExternalService.getRestTemplate(configuration);

        assertEquals(restTemplate, result);
        verify(restTemplateGenerator).generate(5000, 3000, 3, 1000);
    }

    @Test
    void testSpanBuilderWithEmptyParentSpanContextString() {
        Configuration configuration = new Configuration();
        configuration.setParentSpanContextString(null);

        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        when(tracer.spanBuilder("MIL-Adapter-RestCall-Execution")).thenReturn(spanBuilder);

        SpanBuilder result = genericRestExternalService.spanBuilder(configuration);

        assertNotNull(result);
        verify(tracer).spanBuilder("MIL-Adapter-RestCall-Execution");
    }

    @Test
    void testSpanBuilderWithValidParentSpanContextString() {
        Configuration configuration = new Configuration();
        String parentSpanContextString = "{\"traceId\":\"traceId\",\"spanId\":\"spanId\"}";
        configuration.setParentSpanContextString(parentSpanContextString);

        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        SpanContext spanContext = SpanContext.createFromRemoteParent("traceId", "spanId", TraceFlags.getSampled(), TraceState.getDefault());
        Span parentSpan = Span.wrap(spanContext);
        when(tracer.spanBuilder("MIL-Adapter-RestCall-Execution")).thenReturn(spanBuilder);
        when(spanBuilder.setParent(any(Context.class))).thenReturn(spanBuilder);

        SpanBuilder result = genericRestExternalService.spanBuilder(configuration);

        assertNotNull(result);
        verify(tracer).spanBuilder("MIL-Adapter-RestCall-Execution");

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(spanBuilder).setParent(contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        Span capturedSpan = Span.fromContext(capturedContext);

        assertEquals(spanContext.getTraceId(), capturedSpan.getSpanContext().getTraceId());
        assertEquals(spanContext.getSpanId(), capturedSpan.getSpanContext().getSpanId());
    }

    @Test
    void testSpanBuilderWithInvalidParentSpanContextString() {
        Configuration configuration = new Configuration();
        String invalidParentSpanContextString = "invalid_json";
        configuration.setParentSpanContextString(invalidParentSpanContextString);

        try {
            genericRestExternalService.spanBuilder(configuration);
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }
}
