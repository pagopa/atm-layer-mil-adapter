package it.gov.pagopa.miladapter.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
public class HttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private final RestConfigurationProperties restConfigurationProperties;
    private final OpenTelemetry openTelemetry;

    public HttpRequestInterceptor(OpenTelemetry openTelemetry,RestConfigurationProperties restConfigurationProperties) {
        this.openTelemetry=openTelemetry;
        this.restConfigurationProperties = restConfigurationProperties;

    }

    TextMapSetter<HttpRequest> setter =
            (request, key, value) -> request.getHeaders().add(key, value);


    private void logRequest(HttpRequest request, byte[] body) {
        Map<String, String> filteredHeaders = request.getHeaders().toSingleValueMap();
        filteredHeaders.remove("Ocp-Apim-Subscription-Key");
        filteredHeaders.remove("Authorization");
        log.info("===========================request begin================================================");
        log.info("URI         : {}", request.getURI());
        log.info("Method      : {}", request.getMethod());
        log.info("Headers     : {}", filteredHeaders);
        log.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
        log.info("==========================request end================================================");
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        log.info("============================response begin==========================================");
        log.info("Status code  : {}", response.getStatusCode());
        log.info("Status text  : {}", response.getStatusText());
        log.info("Headers      : {}", response.getHeaders());
        log.info("Response body: {}", StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
        log.info("=======================response end=================================================");
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        propagateTrace(request);
        LocalDateTime timestampStart = LocalDateTime.now();
        LocalDateTime timestampEnd = LocalDateTime.now();
        if (restConfigurationProperties.isInterceptorLoggingEnabled()) {
            logRequest(request, body);
            log.info("Request started at : {}", timestampStart);
        }
        ClientHttpResponse response = execution.execute(request, body);
        if (restConfigurationProperties.isInterceptorLoggingEnabled()) {
            logResponse(response);
            log.info("Request finished at : {}", timestampEnd);
            long duration = Duration.between(timestampStart, timestampEnd).toMillis();
            log.info("Request duration : {}", duration);
        }
        return response;
    }

    private void propagateTrace(HttpRequest request) {
        try {
            openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), request, setter);

        } catch (Exception e) {
            log.error("Unable to propagate tracing informations in HttpRequestInterceptor");
        }
    }
}
