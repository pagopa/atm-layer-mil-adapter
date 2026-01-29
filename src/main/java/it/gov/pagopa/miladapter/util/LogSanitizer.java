package it.gov.pagopa.miladapter.util;

public final class LogSanitizer {
    /**
     * Simple sanitizer for log output to avoid log injection via control characters.
     *
     * @param input original string
     * @return string with CR and LF characters removed, or null if input is null
     */
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }
        return input.replace('\r', ' ').replace('\n', ' ');
    }

    /**
     * Sanitize and validate a user-provided endpoint to prevent SSRF.
     * Only allows relative paths starting with "/" and forbids protocol/host parts
     * or simple path traversal sequences.
     */
    public static String sanitizeEndpoint(String endpoint) {
        if (endpoint == null) {
            throw new RuntimeException("Endpoint cannot be null");
        }
        String trimmed = endpoint.trim();
        if (trimmed.isEmpty()) {
            throw new RuntimeException("Endpoint cannot be empty");
        }
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("//") || lower.contains("://")) {
            throw new RuntimeException("Absolute URLs are not allowed in endpoint");
        }
        if (!trimmed.startsWith("/")) {
            throw new RuntimeException("Endpoint must be a relative path starting with '/'");
        }
        if (trimmed.contains("..")) {
            throw new RuntimeException("Endpoint cannot contain path traversal sequences");
        }
        return trimmed;
    }
}
