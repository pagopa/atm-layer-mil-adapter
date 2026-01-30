package it.gov.pagopa.miladapter.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class EndpointValidator {

    private EndpointValidator() {}

    /**
     * Sanitize and validate a user-provided endpoint to prevent SSRF.
     * Only allows relative paths starting with "/" and forbids protocol/host parts
     * or simple path traversal sequences.
     */
    public static String sanitizeEndpoint(String endpoint) {
        if (endpoint == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endpoint cannot be null");
        }
        String trimmed = endpoint.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endpoint cannot be empty");
        }
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("//") || lower.contains("://")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Absolute URLs are not allowed in endpoint");
        }
        if (!trimmed.startsWith("/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endpoint must be a relative path starting with '/'");
        }
        if (trimmed.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endpoint cannot contain path traversal sequences");
        }
        return trimmed;
    }
}
