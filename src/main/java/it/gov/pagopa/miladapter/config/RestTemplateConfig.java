package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    private RestTemplate getRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(restConfigurationProperties.getConnectionTimeout());
        factory.setReadTimeout(restConfigurationProperties.getReadTimeout());
        return new RestTemplate(new BufferingClientHttpRequestFactory(factory));
    }

    @Bean
    public RestTemplate restTemplateWithInterceptor() {
        RestTemplate restTemplate = getRestTemplate();

        if (restConfigurationProperties.isInterceptorLoggingEnabled()) {
            List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
            if (CollectionUtils.isEmpty(interceptors)) {
                interceptors = new ArrayList<>();
            }
            interceptors.add(new HttpRequestInterceptor());
            restTemplate.setInterceptors(interceptors);
        }
        return restTemplate;
    }
}
