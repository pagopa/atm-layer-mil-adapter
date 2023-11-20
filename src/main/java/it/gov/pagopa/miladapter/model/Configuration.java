package it.gov.pagopa.miladapter.model;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    AuthParameters authParameters;

}
