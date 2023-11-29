package it.gov.pagopa.miladapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HttpVariablesEnum {
    BODY("body"),
    URL("url"),
    HEADERS("headers"),
    METHOD("method"),
    PATH_PARAMS("pathParams"),
    QUERY_PARAMS("queryParams"),
    RESPONSE("response"),
    STATUS_CODE("statusCode"),
    CLIENT_ID("client_id"),
    CLIENT_SECRET("client_secret"),
    CONNECTION_RESPONSE_TIMEOUT_MILLISECONDS("connectionResponseTimeoutMilliseconds"),
    CONNECTION_REQUEST_TIMEOUT_MILLISECONDS("connectionRequestTimeoutMilliseconds"),
    MAX_RETRY("maxRetry"),
    RETRY_INTERVAL_MILLISECONDS("retryIntervalMilliseconds"),
    DELAY_MILLISECONDS("delayMilliseconds");


    private String value;

}
