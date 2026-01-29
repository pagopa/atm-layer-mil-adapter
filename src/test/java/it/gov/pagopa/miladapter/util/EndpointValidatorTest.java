package it.gov.pagopa.miladapter.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EndpointValidatorTest {

    @Test
    void testSanitizeEndpoint_withNull_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint(null);
        });
        assertEquals("Endpoint cannot be null", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withEmptyString_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("");
        });
        assertEquals("Endpoint cannot be empty", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withWhitespaceOnly_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("   ");
        });
        assertEquals("Endpoint cannot be empty", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withValidRelativePath_shouldReturnPath() {
        String result = EndpointValidator.sanitizeEndpoint("/api/endpoint");
        assertEquals("/api/endpoint", result);
    }

    @Test
    void testSanitizeEndpoint_withValidPathWithSpaces_shouldTrimAndReturn() {
        String result = EndpointValidator.sanitizeEndpoint("  /api/endpoint  ");
        assertEquals("/api/endpoint", result);
    }

    @Test
    void testSanitizeEndpoint_withHttpUrl_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("http://example.com/api");
        });
        assertEquals("Absolute URLs are not allowed in endpoint", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withHttpsUrl_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("https://example.com/api");
        });
        assertEquals("Absolute URLs are not allowed in endpoint", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withProtocolRelativeUrl_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("//example.com/api");
        });
        assertEquals("Absolute URLs are not allowed in endpoint", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withCustomProtocol_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("ftp://example.com/api");
        });
        assertEquals("Absolute URLs are not allowed in endpoint", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withoutLeadingSlash_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("api/endpoint");
        });
        assertEquals("Endpoint must be a relative path starting with '/'", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withPathTraversal_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("/api/../admin");
        });
        assertEquals("Endpoint cannot contain path traversal sequences", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withPathTraversalAtEnd_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("/api/..");
        });
        assertEquals("Endpoint cannot contain path traversal sequences", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withPathTraversalAtStart_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("/../api");
        });
        assertEquals("Endpoint cannot contain path traversal sequences", exception.getMessage());
    }

    @Test
    void testSanitizeEndpoint_withValidComplexPath_shouldReturnPath() {
        String result = EndpointValidator.sanitizeEndpoint("/api/v1/users/123/profile");
        assertEquals("/api/v1/users/123/profile", result);
    }

    @Test
    void testSanitizeEndpoint_withQueryString_shouldReturnPath() {
        String result = EndpointValidator.sanitizeEndpoint("/api/endpoint?param=value");
        assertEquals("/api/endpoint?param=value", result);
    }

    @Test
    void testSanitizeEndpoint_withUppercaseHttps_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            EndpointValidator.sanitizeEndpoint("HTTPS://example.com/api");
        });
        assertEquals("Absolute URLs are not allowed in endpoint", exception.getMessage());
    }
}

