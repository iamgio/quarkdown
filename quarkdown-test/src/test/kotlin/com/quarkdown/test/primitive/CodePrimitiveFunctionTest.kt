package com.quarkdown.test.primitive

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `.extend` applied to [com.quarkdown.core.ast.base.block.Code] blocks.
 */
class CodePrimitiveFunctionTest {
    @Test
    fun `no extension renders unchanged`() {
        execute("```\nHello\n```") {
            assertEquals("<pre><code>Hello</code></pre>", it)
        }
    }

    @Test
    fun `extension wraps every fenced code block`() {
        execute(
            """
            .extend {code}
                .container
                    .super

            ```
            Hello
            ```
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><pre><code>Hello</code></pre></div>",
                it,
            )
        }
    }

    @Test
    fun `extension can disable line numbers via super`() {
        execute(
            """
            .extend {code}
                .super linenumbers:{no}

            ```kotlin
            println("Hello")
            println("Hello")
            ```
            """.trimIndent(),
        ) {
            assertEquals(
                "<pre><code class=\"language-kotlin nohljsln\">" +
                    "println(&quot;Hello&quot;)\nprintln(&quot;Hello&quot;)" +
                    "</code></pre>",
                it,
            )
        }
    }

    @Test
    fun `language can be matched and conditionally wrapped`() {
        execute(
            """
            .extend {code} where:{lang: .lang::equals {kotlin}}
                .container
                    .super

            ```kotlin
            println("Match")
            ```

            ```
            No match
            ```
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><pre><code class=\"language-kotlin\">println(&quot;Match&quot;)</code></pre></div>" +
                    "<pre><code>No match</code></pre>",
                it,
            )
        }
    }

    @Test
    fun `language can be set via super`() {
        execute(
            """
            .extend {code}
                .super lang:{kotlin}

            ```
            println("Hello")
            ```
            """.trimIndent(),
        ) {
            assertEquals(
                "<pre><code class=\"language-kotlin\">println(&quot;Hello&quot;)</code></pre>",
                it,
            )
        }
    }

    @Test
    fun `callouts can be attached to matching languages via super`() {
        execute(
            """
            .extend {code} where:{lang: .lang::equals {kotlin}}
                .super callouts:{
                    - 1: Prints a greeting
                }

            ```kotlin
            println("Hello")
            ```

            ```
            No callouts
            ```
            """.trimIndent(),
        ) {
            assertEquals(
                "<pre><code class=\"language-kotlin\" data-callouts=\"1\">println(&quot;Hello&quot;)</code></pre>" +
                    "<ul class=\"code-callouts\">" +
                    "<li class=\"code-callout\"><span class=\"code-callout-marker\">1</span>Prints a greeting</li>" +
                    "</ul>" +
                    "<pre><code>No callouts</code></pre>",
                it,
            )
        }
    }

    @Test
    fun `focus range can be set via super`() {
        execute(
            """
            .extend {code}
                .super focus:{2..3}

            ```
            a
            b
            c
            d
            ```
            """.trimIndent(),
        ) {
            assertEquals(
                "<pre><code class=\"focus-lines\" data-focus-start=\"2\" data-focus-end=\"3\">a\nb\nc\nd</code></pre>",
                it,
            )
        }
    }
}
