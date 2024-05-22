package it.gov.pagopa.miladapter.model;

import lombok.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

    String endpoint;
    String function;
    HttpMethod httpMethod;
    String body;
    HttpHeaders headers;
    Map<String, String> pathParams;
    Integer connectionResponseTimeoutMilliseconds;
    Integer connectionRequestTimeoutMilliseconds;
    Integer maxRetry;
    Integer retryIntervalMilliseconds;
    Integer delayMilliseconds;
    String parentSpanContextString;

    AuthParameters authParameters;

}
