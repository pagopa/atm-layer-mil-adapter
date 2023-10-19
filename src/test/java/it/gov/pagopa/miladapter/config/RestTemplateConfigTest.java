package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class RestTemplateConfigTest {

    @InjectMocks
    private RestTemplateConfig restTemplateConfig;

    @Mock
    private RestConfigurationProperties restConfigurationProperties;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRestTemplateWithInterceptorEnabled() {
        when(restConfigurationProperties.isInterceptorLoggingEnabled()).thenReturn(true);
        when(restConfigurationProperties.getConnectionTimeout()).thenReturn(5555);
        when(restConfigurationProperties.getReadTimeout()).thenReturn(5555);
        RestTemplate restTemplate = restTemplateConfig.restTemplateWithInterceptor();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        assertEquals(1, interceptors.size());
    }

    @Test
    public void testRestTemplateWithInterceptorDisabled() {
        when(restConfigurationProperties.isInterceptorLoggingEnabled()).thenReturn(false);
        when(restConfigurationProperties.getConnectionTimeout()).thenReturn(5555);
        when(restConfigurationProperties.getReadTimeout()).thenReturn(5555);
        RestTemplate restTemplate = restTemplateConfig.restTemplateWithInterceptor();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        assertEquals(0, interceptors.size());
    }
}
