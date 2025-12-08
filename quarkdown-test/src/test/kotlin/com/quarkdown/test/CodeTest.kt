package com.quarkdown.test

import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for code blocks and inline code.
 */
class CodeTest {
    @Test
    fun inline() {
        execute("`println(\"Hello, world!\")`") {
            assertEquals(
                "<p><span class=\"codespan-content\"><code>println(&quot;Hello, world!&quot;)</code></span></p>",
                it,
            )
            assertFalse(attributes.hasCode)
        }
    }

    @Test
    fun `hex color preview`() {
        execute("`#FF0000`") {
            assertEquals(
                "<p>" +
                    "<span class=\"codespan-content\">" +
                    "<code>#FF0000</code>" +
                    "<span style=\"background-color: rgba(255, 0, 0, 1.0);\" class=\"color-preview\"></span>" +
                    "</span>" +
                    "</p>",
                it,
            )
            assertFalse(attributes.hasCode)
        }
    }

    @Test
    fun `rgb color preview`() {
        execute("`rgba(200, 100, 50, 0.5)`") {
            assertEquals(
                "<p>" +
                    "<span class=\"codespan-content\">" +
                    "<code>rgba(200, 100, 50, 0.5)</code>" +
                    "<span style=\"background-color: rgba(200, 100, 50, 0.5);\" class=\"color-preview\"></span>" +
                    "</span>" +
                    "</p>",
                it,
            )
            assertFalse(attributes.hasCode)
        }
    }

    @Test
    fun block() {
        execute("```\nprintln(\"Hello, world!\")\n```") {
            assertEquals("<pre><code>println(&quot;Hello, world!&quot;)</code></pre>", it)
            assertTrue(attributes.hasCode)
        }
    }

    @Test
    fun `block with language`() {
        execute("```kotlin\nprintln(\"Hello, world!\")\n```") {
            assertEquals("<pre><code class=\"language-kotlin\">println(&quot;Hello, world!&quot;)</code></pre>", it)
            assertTrue(attributes.hasCode)
        }
    }

    @Test
    fun `block with language and indentation`() {
        execute("```kotlin\nfun hello() {\n    println(\"Hello, world!\")\n}\n```") {
            assertEquals(
                "<pre><code class=\"language-kotlin\">fun hello() {\n    println(&quot;Hello, world!&quot;)\n}</code></pre>",
                it,
            )
            assertTrue(attributes.hasCode)
        }
    }

    // #259
    @Test
    fun `indented block`() {
        execute(
            """
            - indented context .br
                ```
                aaaa
                bbbb
                cccc
                ```
            """.trimIndent(),
        ) {
            assertEquals(
                "<ul><li>indented context <br />" +
                    "<pre><code>aaaa\nbbbb\ncccc</code></pre>" +
                    "</li></ul>",
                it,
            )
        }
    }

    // #32
    @Test
    fun `long block`() {
        execute(
            """
            ```nohighlight
                 1/1       file.ext   - this_is_a_very_long_function_name_indeed()
                 59/59     file.ext   - this_is_a_very_long_function_name_indeed()
                 11/11     file.ext   - this_is_a_very_long_function_name_indeed()
                 11/11     file.ext   - this_is_a_very_long_function_name_indeed()
                 11/11     file.ext   - this_is_a_very_long_function_name_indeed()
                 15/15     file.ext   - this_is_a_very_long_function_name_indeed()
                 4/4       file.ext   - this_is_a_very_long_function_name_indeed()
                 13/13     file.ext   - this_is_a_very_long_function_name_indeed()
                 14/14     file.ext   - this_is_a_very_long_function_name_indeed()
                 4/4       file.ext   - this_is_a_very_long_function_name_indeed()
                 15/15     file.ext   - this_is_a_very_long_function_name_indeed()
                 19/19     file.ext   - this_is_a_very_long_function_name_indeed()
                 12/12     file.ext   - this_is_a_very_long_function_name_indeed()
                 4/4       file.ext   - this_is_a_very_long_function_name_indeed()
                 14/14     file.ext   - this_is_a_very_long_function_name_indeed()
                 2/2       file.ext   - this_is_a_very_long_function_name_indeed()
                 8/8       file.ext   - this_is_a_very_long_function_name_indeed()
                 13/13     file.ext   - this_is_a_very_long_function_name_indeed()
                 4/4       file.ext   - this_is_a_very_long_function_name_indeed()
                 16/16     file.ext   - this_is_a_very_long_function_name_indeed()
            ***  19/21     file.ext   - this_is_a_very_long_function_name_indeed()
                 2/2       file.ext   - this_is_a_very_long_function_name_indeed()
                 2/2       file.ext   - this_is_a_very_long_function_name_indeed()
                 2/2       file.ext   - this_is_a_very_long_function_name_indeed()
                 2/2       file.ext   - this_is_a_very_long_function_name_indeed()
                 2/2       file.ext   - this_is_a_very_long_function_name_indeed()
                 2/2       file.ext   - this_is_a_very_long_function_name_indeed()
                 16/16     file.ext   - this_is_a_very_long_function_name_indeed()
            ***  23/25     file.ext   - this_is_a_very_long_function_name_indeed()
                 1/1       file.ext   - this_is_a_very_long_function_name_indeed()
                 1/1       file.ext   - this_is_a_very_long_function_name_indeed()
                 7/7       file.ext   - this_is_a_very_long_function_name_indeed()
                 3/3       file.ext   - this_is_a_very_long_function_name_indeed()
            ***  81/82     file_1.ext - this_is_a_very_long_function_name_indeed()
            ***  0/87      file_1.ext - this_is_a_very_long_function_name_indeed()
            ```
            """.trimIndent(),
        ) {
            assertTrue(it.startsWith("<pre><code class=\"language-nohighlight\">"))
            assertTrue(it.endsWith("</code></pre>"))
            assertEquals(35, it.lines().size)
        }
    }
}
