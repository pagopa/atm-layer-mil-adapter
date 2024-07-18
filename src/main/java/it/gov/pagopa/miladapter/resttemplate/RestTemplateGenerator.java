package it.gov.pagopa.miladapter.resttemplate;


import io.opentelemetry.api.OpenTelemetry;
import it.gov.pagopa.miladapter.config.HttpRequestInterceptor;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

    @Autowired
    OpenTelemetry openTelemetry;

    public RestTemplate generate(int connectionRequestTimeout, int connectionResponseTimeout, int retry, int retryDelay) {
        log.info("Generating restTemplate with connectionTimeout: {} milliseconds, responseTimeout: {} milliseconds, maxRetry: {}, retryInterval: {} milliseconds",
                connectionRequestTimeout, connectionResponseTimeout, retry, retryDelay);

        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.of(Duration.ofMillis(connectionResponseTimeout)))
                .setConnectionRequestTimeout(Timeout.of(Duration.ofMillis(connectionRequestTimeout)))
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(150) // Imposta il numero massimo di connessioni totali
                .setMaxConnPerRoute(150) // Imposta il numero massimo di connessioni per rotta
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryStrategy(new CustomHttpRequestRetryStrategy(retry, retryDelay))
                .evictIdleConnections(TimeValue.ofSeconds(20)) // Rilascia connessioni inattive dopo 30 secondi
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        BufferingClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(requestFactory);
        RestTemplate restTemplate = new RestTemplate(bufferingFactory);

        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(new HttpRequestInterceptor(openTelemetry, restConfigurationProperties));
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }
}