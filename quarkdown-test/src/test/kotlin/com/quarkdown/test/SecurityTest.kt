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
