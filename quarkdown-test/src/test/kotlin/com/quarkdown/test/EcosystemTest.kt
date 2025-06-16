package com.quarkdown.test

import com.quarkdown.core.pipeline.error.StrictPipelineErrorHandler
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

private const val OUTPUT_1 = "<h1>Title</h1><p>Some <em>text</em>.</p>"
private const val OUTPUT_3 = "<h2>Included</h2><pre><code>code\ncode</code></pre>"

/**
 * Tests for including files and libraries.
 */
class EcosystemTest {
    @Test
    fun `include source`() {
        execute(
            """
            .noautopagebreak
            .include {include/include-1.md}
            """.trimIndent(),
        ) {
            assertEquals(
                OUTPUT_1,
                it,
            )
        }
    }

    @Test
    fun `include function from source`() {
        execute(
            """
            .include {include/include-2.md}
            .hello {world}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello, world!</p>",
                it,
            )
        }

        execute(
            """
            .noautopagebreak
            # Main
            .include {include/include-3.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<h1>Main</h1>$OUTPUT_3",
                it,
            )
        }
    }

    @Test
    fun `share function with included files`() {
        execute(
            """
            .function {hello}
                x:
                Hello, .x!
            .include {include/include-4.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<h3>Hello, world!</h3>",
                it,
            )
        }
    }

    @Test
    fun `transitive inclusion`() {
        execute(
            """
            .noautopagebreak
            # Main
            .include {include/include-5.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<h1>Main</h1><h1>Included</h1><p>Hello, Gio!</p><h3>Hello, world!</h3>",
                it,
            )
        }
    }

    @Test
    fun `invalid usage as value`() {
        // Included file cannot be used as a dynamic value.
        assertFails {
            execute(
                """
                .sum {.include {include/include-6.md}} {3}
                """.trimIndent(),
            ) {}
        }
    }

    @Test
    fun `mutate included data`() {
        execute(
            """
            .include {include/include-7.md}
            
            .saygreeting
            
            .var {mygreeting} {Hello}
            
            .saygreeting
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hi</p><p>Hello</p>",
                it,
            )
        }
    }

    @Test
    fun `include library`() {
        // Load library named 'hello' from libraries/hello.qd
        execute(
            """
            .include {hello}
            .hellofromlib {world}
            """.trimIndent(),
            loadableLibraries = setOf("hello"),
            useDummyLibraryDirectory = true,
        ) {
            assertEquals(
                "<p>Hello, <em>world</em>!</p>",
                it,
            )
        }
    }

    @Test
    fun `invocation of unincluded library`() {
        assertFails {
            execute(
                """
                .hellofromlib {world}
                """.trimIndent(),
                loadableLibraries = setOf("hello"),
                useDummyLibraryDirectory = true,
                errorHandler = StrictPipelineErrorHandler(),
            ) {}
        }
    }

    @Test
    fun `inclusion of unexisting library`() {
        // Not available in the environment.
        assertFails {
            execute(
                """
                .include {hello}
                .hellofromlib {world}
                """.trimIndent(),
                errorHandler = StrictPipelineErrorHandler(),
                useDummyLibraryDirectory = true,
            ) {}
        }
    }

    @Test
    fun `include multiple sources`() {
        execute(
            """
            .noautopagebreak
            .include {include/include-1.md}
            .include {include/include-3.md}
            
            .hello {world}
            """.trimIndent(),
        ) {
            assertEquals(
                "$OUTPUT_1$OUTPUT_3<p>Hello, world!</p>",
                it,
            )
        }
    }

    @Test
    fun `include multiple sources via bulk`() {
        execute(
            """
            .noautopagebreak
            .includeall
                - include/include-1.md
                - include/include-3.md
            
            .hello {world}
            """.trimIndent(),
        ) {
            assertEquals(
                "$OUTPUT_1$OUTPUT_3<p>Hello, world!</p>",
                it,
            )
        }
    }
}
