package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for possible security issues.
 */
class SecurityTest {
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
}
