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
    CLIENT_SECRET("client_secret");


    private String value;

}
