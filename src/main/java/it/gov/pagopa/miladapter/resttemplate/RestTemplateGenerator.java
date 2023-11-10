package it.gov.pagopa.miladapter.resttemplate;


import it.gov.pagopa.miladapter.config.HttpRequestInterceptor;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RestTemplateGenerator {

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    public RestTemplate generate(int connectionRequestTimeout, int connectionResponseTimeout, int retry, int retryDelay) {
        log.info("Generating restTemplate with connectionTimeout: {} milliseconds, responseTimeout: {} milliseconds, maxRetry: {}, retryInterval: {} milliseconds",
                connectionRequestTimeout, connectionResponseTimeout, retry, retryDelay);
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.of(Duration.ofMillis(connectionResponseTimeout)))
                .setConnectionRequestTimeout(Timeout.of(Duration.ofMillis(connectionRequestTimeout)))
                .build();

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);
        httpClientBuilder.setRetryStrategy(new CustomHttpRequestRetryStrategy(retry, retryDelay));
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
        BufferingClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(requestFactory);
        RestTemplate restTemplate = new RestTemplate(bufferingFactory);


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