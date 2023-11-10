package it.gov.pagopa.miladapter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HTTPConfiguration {

    String endpoint;
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
