package com.example.taskmanager.common;

/**
 * Very conservative HTML sanitizer for user-supplied text fields.
 * - Strips <script>...</script>
 * - Removes "javascript:" pseudo-URLs
 * - Removes common on*="" event attributes
 * - Optionally truncates overly long inputs
 *
 * NOTE: If you later add Jsoup/OWASP sanitizer to the pom, you can
 * swap the implementation here without touching callers.
 */
public final class HtmlSanitizer {

    private HtmlSanitizer() {}

    public static String sanitize(String input) {
        if (input == null || input.isBlank()) return input;

        String s = input;

        // Remove <script> blocks (very conservative, multiline)
        s = s.replaceAll("(?is)<script.*?>.*?</script>", "");

        // Remove inline event handlers like onclick="", onload="", onerror=""
        s = s.replaceAll("(?i)\\son[a-z]+\\s*=\\s*\"[^\"]*\"", "");
        s = s.replaceAll("(?i)\\son[a-z]+\\s*=\\s*'[^']*'", "");
        s = s.replaceAll("(?i)\\son[a-z]+\\s*=\\s*[^\\s>]*", "");

        // Remove javascript: pseudo-URLs in href/src etc.
        s = s.replaceAll("(?i)javascript\\s*:", "");

        // Optional hard cap to avoid huge payloads in DB/logs
        final int MAX = 10_000;
        if (s.length() > MAX) s = s.substring(0, MAX);

        return s.trim();
    }
}