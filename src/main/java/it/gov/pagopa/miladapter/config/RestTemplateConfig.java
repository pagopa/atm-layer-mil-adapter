package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Autowired
    RestTemplateGenerator restTemplateGenerator;

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    @Bean
    public RestTemplate getRestTemplate() {
        return restTemplateGenerator.generate(
                restConfigurationProperties.getConnectionRequestTimeoutMilliseconds(),
                restConfigurationProperties.getConnectionResponseTimeoutMilliseconds(),
                restConfigurationProperties.getMaxRetry(),
                restConfigurationProperties.getRetryIntervalMilliseconds()
        );
    }
}
