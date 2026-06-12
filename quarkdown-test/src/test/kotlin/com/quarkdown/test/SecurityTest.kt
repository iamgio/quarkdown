package com.quarkdown.test

import com.quarkdown.core.pipeline.error.PipelineException
import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter
import com.quarkdown.test.util.execute
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for possible security issues.
 */
class SecurityTest {
    @Test
    fun `authors injection`() {
        execute(
            """
            .docauthors
                - "><script>alert('XSS')</script>
            """.trimIndent(),
            afterPostRenderingHook = {
                assertContains(it, "&lt;script&gt;")
                assertContains(it, "&lt;/script&gt;")
                assertContains(it, "name=\"author\"")
            },
        ) {}
    }

    @Test
    fun `plaintext injection`() {
        execute(".plaintext {\"><script>alert('XSS')</script>}") {
            assertContains(it, "&lt;script&gt;")
            assertContains(it, "&lt;/script&gt;")
        }
    }

    @Test
    fun `string manipulation injection`() {
        execute(".uppercase {\"><script>alert('XSS')</script>}") {
            assertContains(it, "&lt;SCRIPT&gt;")
            assertContains(it, "&lt;/SCRIPT&gt;")
        }
    }

    @Test
    fun `mermaid injection`() {
        execute(
            """
            .mermaid
                graph TD
                    A --> B</pre>Hello
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure><pre class=\"mermaid\">" +
                    "graph TD\n    A --&gt; B&lt;/pre&gt;Hello</pre></figure>",
                it,
            )
        }
    }

    @Test
    fun `file tree injection`() {
        execute(
            """
            .filetree
                - </div><script>alert('XSS')</script>
            """.trimIndent(),
        ) {
            val escaped = "&lt;/div&gt;&lt;script&gt;alert(&rsquo;XSS&rsquo;)&lt;/script&gt;"
            assertContains(it, ">$escaped</li>")
            assertContains(it, "data-name=\"$escaped\"")
        }
    }

    @Test
    fun `infinite recursion in strict mode`() {
        assertFailsWith<PipelineException> {
            execute(
                """
                .function {myfunc}
                    .myfunc
                .myfunc
                """.trimIndent(),
            ) {}
        }
    }

    @Test
    fun `cross-reference id injection (heading)`() {
        execute(
            """
            ## Title {#"><script>alert('XSS')</script>}

            See .ref {"><script>alert('XSS')</script>}.
            """.trimIndent(),
        ) {
            // Angle brackets, quotes, and ampersands are stripped from IDs by sanitizeId().
            val sanitizedId = "scriptalert(XSS)/script"
            assertContains(it, "id=\"$sanitizedId\"")
            assertContains(it, "href=\"#$sanitizedId\"")
            // No raw script tags in the output.
            assert("<script>" !in it) { "Raw <script> tag found in output: $it" }
        }
    }

    @Test
    fun `injection-payload heading ids that collapse to the same anchor are disambiguated`() {
        // Two raw customIds that differ only in characters sanitizeId() strips collapse to the same
        // anchor in the output. Without keying the deduplication hook on the sanitized form, both
        // headings would emit the same id and references could no longer disambiguate them.
        execute(
            """
            .noautopagebreak

            ## A {#"><script>alert(1)</script>}

            ## B {#<script>alert(1)</script>"}
            """.trimIndent(),
        ) {
            assertContains(it, "<h2 id=\"scriptalert(1)/script\">A</h2>")
            assertContains(it, "<h2 id=\"scriptalert(1)/script-2\">B</h2>")
        }

        // Sanity check: payloads that sanitize to *different* strings (1 vs 2) keep distinct ids.
        execute(
            """
            .noautopagebreak

            ## A {#"><script>alert(1)</script>}

            ## B {#"><script>alert(2)</script>}
            """.trimIndent(),
        ) {
            assertContains(it, "<h2 id=\"scriptalert(1)/script\">A</h2>")
            assertContains(it, "<h2 id=\"scriptalert(2)/script\">B</h2>")
        }
    }

    @Test
    fun `cross-reference id injection (figure)`() {
        execute(
            """
            ![Image](img.png) {#"><img src=x onerror=alert(1)>}

            See .ref {"><img src=x onerror=alert(1)>}.
            """.trimIndent(),
        ) {
            val sanitizedId = "imgsrc=xonerror=alert(1)"
            assertContains(it, "id=\"$sanitizedId\"")
            assertContains(it, "href=\"#$sanitizedId\"")
            // The reference ID fallback text is HTML-escaped, not rendered as raw HTML.
            assertContains(it, "&lt;img src=x onerror=alert(1)&gt;")
        }
    }

    @Test
    fun `cross-reference id injection (math)`() {
        execute(
            """
            .numbering
                - equations: 1

            ${'$'} E=mc^2 ${'$'} {#" onclick="alert(1)}
            """.trimIndent(),
        ) {
            // Quotes and spaces are stripped from IDs.
            val sanitizedId = "onclick=alert(1)"
            assertContains(it, "id=\"$sanitizedId\"")
            assert("onclick=\"alert" !in it) { "Injected event handler found in output: $it" }
        }
    }

    @Test
    fun `docname sanitization at file export time`() {
        execute(
            ".docname {../test.abc}",
            outputResourceHook = { group ->
                val file = group?.accept(FileResourceExporter(location = File("."), write = false))
                assertEquals("../test.abc", group?.name)
                assertEquals("-.-test.abc", file?.name)
            },
        ) {
            assertEquals("../test.abc", documentInfo.name)
        }
    }
}
