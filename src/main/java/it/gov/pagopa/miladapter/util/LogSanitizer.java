package it.gov.pagopa.miladapter.util;

public final class LogSanitizer {

    private LogSanitizer() {}

    /**
     * Simple sanitizer for log output to avoid log injection via control characters.
     *
     * @param input original string
     * @return string with CR and LF characters replaced with spaces, or null if input is null
     */
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\r", " ").replace("\n", " ");
    }
}
