package it.gov.pagopa.miladapter.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EndpointValidatorTest {

    static Stream<Arguments> invalidEndpointProvider() {
        return Stream.of(
                Arguments.of(null, "Endpoint cannot be null"),
                Arguments.of("", "Endpoint cannot be empty"),
                Arguments.of("   ", "Endpoint cannot be empty"),
                Arguments.of("http://example.com/api", "Absolute URLs are not allowed in endpoint"),
                Arguments.of("https://example.com/api", "Absolute URLs are not allowed in endpoint"),
                Arguments.of("//example.com/api", "Absolute URLs are not allowed in endpoint"),
                Arguments.of("ftp://example.com/api", "Absolute URLs are not allowed in endpoint"),
                Arguments.of("HTTPS://example.com/api", "Absolute URLs are not allowed in endpoint"),
                Arguments.of("api/endpoint", "Endpoint must be a relative path starting with '/'"),
                Arguments.of("/api/../admin", "Endpoint cannot contain path traversal sequences"),
                Arguments.of("/api/..", "Endpoint cannot contain path traversal sequences"),
                Arguments.of("/../api", "Endpoint cannot contain path traversal sequences")
        );
    }

    static Stream<Arguments> validEndpointProvider() {
        return Stream.of(
                Arguments.of("/api/endpoint", "/api/endpoint"),
                Arguments.of("  /api/endpoint  ", "/api/endpoint"),
                Arguments.of("/api/v1/users/123/profile", "/api/v1/users/123/profile"),
                Arguments.of("/api/endpoint?param=value", "/api/endpoint?param=value")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidEndpointProvider")
    void testSanitizeEndpoint_withInvalidInput_shouldThrowException(String input, String expectedMessage) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            EndpointValidator.sanitizeEndpoint(input);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains(expectedMessage));
    }

    @ParameterizedTest
    @MethodSource("validEndpointProvider")
    void testSanitizeEndpoint_withValidInput_shouldReturnSanitizedPath(String input, String expected) {
        String result = EndpointValidator.sanitizeEndpoint(input);
        assertEquals(expected, result);
    }
}
