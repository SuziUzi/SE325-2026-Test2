package nz.ac.auckland.se325.robolab;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Task 3 — user-typed text must be shown as text, never run as HTML.
 *
 * The cross-site-scripting hole is in the browser code: a booking's purpose is written straight
 * into the page's HTML. This cannot be exercised from Java, so this test inspects the front-end
 * source instead: it fails while a purpose is still assigned to innerHTML without escaping. A fix
 * that uses textContent, or an HTML-escaping helper (e.g. esc(...)), turns it GREEN. Don't edit
 * this file. (Also confirm by hand: a booking with purpose <img src=x onerror=alert(1)> must no
 * longer pop up a box.)
 */
@DisplayName("Task 3 — booking purpose is not rendered as raw HTML")
class Task3XssTest {

    @Test
    @DisplayName("no booking purpose is written to innerHTML without escaping")
    void purposeIsNotWrittenAsRawHtml() throws Exception {
        String js = Files.readString(Path.of("src/main/resources/static/app.js"));

        // Matches `innerHTML = ... purpose ...` on one statement, UNLESS an escaping helper is
        // applied on that same statement.
        Pattern rawSink = Pattern.compile(
                "innerHTML\\s*=\\s*(?![^;\\n]*(esc\\(|escape|encodeHTML|escapeHtml))[^;\\n]*purpose");

        assertFalse(rawSink.matcher(js).find(),
                "A booking purpose is still assigned to innerHTML without escaping. "
                + "Render it with textContent, or escape it before inserting it as HTML.");
    }
}
