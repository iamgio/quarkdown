package com.quarkdown.test

import com.quarkdown.core.pipeline.error.StrictPipelineErrorHandler
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

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
    fun `'read' call from updated working directory`() {
        execute(
            """
            .include {include/include-8.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Line 1\nLine 2\n\nLine 3</p>",
                it,
            )
        }
    }

    @Test
    fun `relative-path image from updated working directory`() {
        execute(
            """
            .include {include/include-9.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>img: <img src=\"img/icon.png\" alt=\"img\" /></p>",
                it,
            )
        }
    }

    @Test
    fun `absolute-path image from updated working directory should not be updated`() {
        execute(
            """
            .include {include/include-10.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>img: <img src=\"/img/icon.png\" alt=\"img\" /></p>",
                it,
            )
        }
    }

    @Test
    fun `url image from updated working directory should not be updated`() {
        execute(
            """
            .include {include/include-11.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>img: <img src=\"https://example.com/img/icon.png\" alt=\"img\" /></p>",
                it,
            )
        }
    }

    @Test
    fun `relative-path reference image from updated working directory`() {
        execute(
            """
            .include {include/include-12.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>img: <img src=\"images/picture.png\" alt=\"img\" /></p>",
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

    @Test
    fun `bulk-include all from directory`() {
        execute(
            """
            .noautopagebreak
            .includeall {.listfiles {include} sortby:{name}}
            """.trimIndent(),
        ) {
            assertTrue(it.startsWith(OUTPUT_1 + OUTPUT_3))
        }
    }
}
