package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate getRestTemplate(RestTemplateGenerator restTemplateGenerator, RestConfigurationProperties restConfigurationProperties) {
        return restTemplateGenerator.generate(
                restConfigurationProperties.getConnectionRequestTimeoutMilliseconds(),
                restConfigurationProperties.getConnectionResponseTimeoutMilliseconds(),
                restConfigurationProperties.getMaxRetry(),
                restConfigurationProperties.getRetryIntervalMilliseconds()
        );
    }
}
