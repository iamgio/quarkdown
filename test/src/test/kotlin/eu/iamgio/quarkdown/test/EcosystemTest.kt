package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.pipeline.error.StrictPipelineErrorHandler
import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

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
                "<h1>Title</h1><p>Some <em>text</em>.</p>",
                it,
            )
        }

        // Import functions from another source.
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
                "<h1>Main</h1><h2>Included</h2><pre><code>code\ncode</code></pre>",
                it,
            )
        }

        // Sharing functions with included files.
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

        // Transitive inclusion of files.
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
    fun `include library`() {
        // Load library named 'hello' from libraries/hello.qmd
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

        // Not included.
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

        // Included but not available.
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
}
