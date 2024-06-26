package it.gov.pagopa.miladapter.resttemplate;

import io.opentelemetry.api.OpenTelemetry;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RestTemplateGenerator.class, RestConfigurationProperties.class})
@ExtendWith(SpringExtension.class)
class RestTemplateGeneratorTest {

    @Mock
    private RestConfigurationProperties restConfigurationProperties;

    @MockBean
    private OpenTelemetry openTelemetry;

    @InjectMocks
    private RestTemplateGenerator restTemplateGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateRestTemplateWithInterceptor() {
        when(restConfigurationProperties.isInterceptorLoggingEnabled()).thenReturn(true);

        RestTemplate restTemplate = restTemplateGenerator.generate(5000, 5000, 3, 1000);

        assertTrue(restTemplate.getInterceptors().stream().anyMatch(Objects::nonNull));
    }

}

