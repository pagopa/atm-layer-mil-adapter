package it.gov.pagopa.miladapter.services.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public class ReportingServiceException extends RuntimeException {
    private final HttpStatusCode statusCode;
    private final String responseBody;

    public ReportingServiceException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = null;
    }

    public ReportingServiceException(String message, HttpStatusCode statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public ReportingServiceException(String message, Throwable cause, HttpStatusCode statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = null;
    }
}

