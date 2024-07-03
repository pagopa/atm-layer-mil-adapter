package it.gov.pagopa.miladapter.services;

import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}




