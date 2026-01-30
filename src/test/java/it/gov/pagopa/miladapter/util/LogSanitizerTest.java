package it.gov.pagopa.miladapter.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LogSanitizerTest {

    static Stream<Arguments> sanitizeTestCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("Normal log message", "Normal log message"),
                Arguments.of("Log message\rwith CR", "Log message with CR"),
                Arguments.of("Log message\nwith LF", "Log message with LF"),
                Arguments.of("Log message\r\nwith CRLF", "Log message  with CRLF"),
                Arguments.of("Line1\nLine2\rLine3\r\nLine4", "Line1 Line2 Line3  Line4"),
                Arguments.of("", ""),
                Arguments.of("\r\n\r\n", "    ")
        );
    }

    @ParameterizedTest
    @MethodSource("sanitizeTestCases")
    void testSanitizeForLog(String input, String expected) {
        String result = LogSanitizer.sanitizeForLog(input);
        assertEquals(expected, result);
    }
}
