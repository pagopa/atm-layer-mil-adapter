package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class HttpRequestUtils {

    public static HttpEntity<String> buildHttpEntity(String body, HttpHeaders headers) {
        return new HttpEntity<>(body, headers);
    }

    public static HttpHeaders createHttpHeaders(Map<String, String> headersMap) {
        if (CollectionUtils.isEmpty(headersMap)) {
            headersMap = new HashMap<>();
        }
        return HttpRequestUtils.fromMapToHeaders(headersMap);
    }

    public static URI buildURI(String basePath, String endpoint, Map<String, String> pathParams) {
        String url = basePath.concat(endpoint);
        return buildURI(url, pathParams);
    }

    public static URI buildURI(String endpoint, Map<String, String> pathParams) {
        Assert.notNull(pathParams, "pathParams can be empty but cannot be null");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endpoint);
        return builder.buildAndExpand(pathParams).toUri();
    }

    public static HttpHeaders fromMapToHeaders(Map<String, String> map) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        for (String key : map.keySet()) {
            if (StringUtils.isBlank(map.get(key))) {
                throw new RuntimeException("declared header param cannot be empty or null");
            }
            multiValueMap.put(key, Collections.singletonList(map.get(key)));
        }
        return new HttpHeaders(multiValueMap);
    }

    public static void checkNotNullPathParams(Map<String, String> map) {
        for (String key : map.keySet()) {
            if (StringUtils.isBlank(map.get(key))) {
                throw new RuntimeException("declared path param cannot be empty or null");
            }
        }
    }

    public static void getRestFactoryConfigsOrDefault(Configuration configuration,
            RestConfigurationProperties restConfigurationProperties) {
        configuration.setConnectionResponseTimeoutMilliseconds(
                configuration.getConnectionResponseTimeoutMilliseconds() == null
                        ? restConfigurationProperties.getConnectionResponseTimeoutMilliseconds()
                        : configuration.getConnectionResponseTimeoutMilliseconds());
        configuration
                .setConnectionRequestTimeoutMilliseconds(configuration.getConnectionRequestTimeoutMilliseconds() == null
                        ? restConfigurationProperties.getConnectionRequestTimeoutMilliseconds()
                        : configuration.getConnectionRequestTimeoutMilliseconds());
        configuration.setMaxRetry(configuration.getMaxRetry() == null ? restConfigurationProperties.getMaxRetry()
                : configuration.getMaxRetry());
        configuration.setRetryIntervalMilliseconds(configuration.getRetryIntervalMilliseconds() == null
                ? restConfigurationProperties.getRetryIntervalMilliseconds()
                : configuration.getRetryIntervalMilliseconds());
    }

    public static HttpMethod httpMethodFromValue(String httpMethodString) {
        return switch (httpMethodString.toUpperCase()) {
            case "GET" -> HttpMethod.GET;
            case "HEAD" -> HttpMethod.HEAD;
            case "POST" -> HttpMethod.POST;
            case "PUT" -> HttpMethod.PUT;
            case "PATCH" -> HttpMethod.PATCH;
            case "DELETE" -> HttpMethod.DELETE;
            case "OPTIONS" -> HttpMethod.OPTIONS;
            case "TRACE" -> HttpMethod.TRACE;
            default -> throw new RuntimeException(String.format("Http method %s does not exist", httpMethodString));
        };
    }
}
