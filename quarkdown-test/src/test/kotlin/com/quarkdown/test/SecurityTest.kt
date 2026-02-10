package com.quarkdown.test

import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter
import com.quarkdown.test.util.execute
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

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
    fun `mermaid injection`() {
        execute(
            """
            .mermaid
                graph TD
                    A --> B</pre>Hello
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure><pre class=\"mermaid fill-height\">" +
                    "graph TD\n    A --&gt; B&lt;/pre&gt;Hello</pre></figure>",
                it,
            )
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
