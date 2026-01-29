package it.gov.pagopa.miladapter.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogSanitizerTest {

    @Test
    void testSanitizeForLog_withNull_shouldReturnNull() {
        String result = LogSanitizer.sanitizeForLog(null);
        assertNull(result);
    }

    @Test
    void testSanitizeForLog_withNormalString_shouldReturnSameString() {
        String input = "Normal log message";
        String result = LogSanitizer.sanitizeForLog(input);
        assertEquals("Normal log message", result);
    }

    @Test
    void testSanitizeForLog_withCarriageReturn_shouldReplaceWithSpace() {
        String input = "Log message\rwith CR";
        String result = LogSanitizer.sanitizeForLog(input);
        assertEquals("Log message with CR", result);
    }

    @Test
    void testSanitizeForLog_withLineFeed_shouldReplaceWithSpace() {
        String input = "Log message\nwith LF";
        String result = LogSanitizer.sanitizeForLog(input);
        assertEquals("Log message with LF", result);
    }

    @Test
    void testSanitizeForLog_withBothCRLF_shouldReplaceWithSpaces() {
        String input = "Log message\r\nwith CRLF";
        String result = LogSanitizer.sanitizeForLog(input);
        assertEquals("Log message  with CRLF", result);
    }

    @Test
    void testSanitizeForLog_withMultipleControlChars_shouldReplaceAll() {
        String input = "Line1\nLine2\rLine3\r\nLine4";
        String result = LogSanitizer.sanitizeForLog(input);
        assertEquals("Line1 Line2 Line3  Line4", result);
    }

    @Test
    void testSanitizeForLog_withEmptyString_shouldReturnEmptyString() {
        String result = LogSanitizer.sanitizeForLog("");
        assertEquals("", result);
    }

    @Test
    void testSanitizeForLog_withOnlyControlChars_shouldReturnSpaces() {
        String input = "\r\n\r\n";
        String result = LogSanitizer.sanitizeForLog(input);
        assertEquals("    ", result);
    }
}

