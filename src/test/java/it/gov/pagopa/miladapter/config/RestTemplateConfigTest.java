package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class RestTemplateConfigTest {

    @InjectMocks
    private RestTemplateConfig restTemplateConfig;

    @Mock
    private RestConfigurationProperties restConfigurationProperties;

    @Mock
    private RestTemplateGenerator restTemplateGenerator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRestTemplateGeneration() {
        when(restConfigurationProperties.isInterceptorLoggingEnabled()).thenReturn(true);
        when(restConfigurationProperties.getConnectionResponseTimeoutMilliseconds()).thenReturn(5555);
        RestTemplate restTemplate = new RestTemplate();
        when(restTemplateGenerator.generate(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(restTemplate);
        RestTemplate restTemplateGenerated = restTemplateConfig.getRestTemplate(restTemplateGenerator, restConfigurationProperties);
        assertEquals(restTemplateGenerated, restTemplate);
    }
}
