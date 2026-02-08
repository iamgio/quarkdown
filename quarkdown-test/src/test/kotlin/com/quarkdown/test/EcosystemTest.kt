package com.quarkdown.test

import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.function.quarkdownName
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.error.StrictPipelineErrorHandler
import com.quarkdown.stdlib.ContextSandbox
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

private const val OUTPUT_BASIC_SOURCE = "<h1>Title</h1><p>Some <em>text</em>.</p>"
private const val OUTPUT_FUNCTION_WITH_CONTENT = "<h2>Included</h2><pre><code>code\ncode</code></pre>"
private const val OUTPUT_ABSOLUTE_IMAGE = "<p>img: <img src=\"/img/icon.png\" alt=\"img\" /></p>"

private fun forSandboxes(
    vararg sandboxes: ContextSandbox,
    expectOthersFail: Boolean = true,
    block: (String) -> Unit,
) {
    sandboxes.forEach {
        Log.info("Testing ecosystem success in sandbox: ${it.quarkdownName}")
        block(it.quarkdownName)
    }

    if (expectOthersFail) {
        val others = ContextSandbox.entries.toSet() - sandboxes.toSet()
        others.forEach { sandbox ->
            Log.info("Testing ecosystem failure in sandbox: ${sandbox.quarkdownName}")
            assertFails {
                block(sandbox.quarkdownName)
            }
        }
    }
}

/**
 * Tests for including files and libraries.
 */
class EcosystemTest {
    @Test
    fun `include source`() {
        forSandboxes(ContextSandbox.SHARE, ContextSandbox.SCOPE, ContextSandbox.SUBDOCUMENT) { sandbox ->
            execute(
                """
                .noautopagebreak
                .include {include/basic-source.md} sandbox:{$sandbox}
                """.trimIndent(),
            ) {
                assertEquals(
                    OUTPUT_BASIC_SOURCE,
                    it,
                )
            }
        }
    }

    @Test
    fun `include source with stdlib call`() {
        forSandboxes(ContextSandbox.SHARE, ContextSandbox.SCOPE, ContextSandbox.SUBDOCUMENT) { sandbox ->
            execute(
                """
                .include {include/stdlib-call.md} sandbox:{$sandbox}
                """.trimIndent(),
            ) {
                assertContains(it, "Lorem ipsum")
            }
        }
    }

    @Test
    fun `modify document info from included source`() {
        forSandboxes(ContextSandbox.SHARE, ContextSandbox.SCOPE) { sandbox ->
            execute(
                """
                .include {include/document-info-modification.md} sandbox:{$sandbox}
                """.trimIndent(),
            ) {
                assertEquals("Modified Title", documentInfo.name)
                assertEquals("it", documentInfo.locale?.shortTag)
            }
        }
    }

    @Test
    fun `include function from source`() {
        forSandboxes(ContextSandbox.SHARE) { sandbox ->
            execute(
                """
                .include {include/function-definition.md} sandbox:{$sandbox}
                .hello {world}
                """.trimIndent(),
            ) {
                assertEquals(
                    "<p>Hello, world!</p>",
                    it,
                )
            }
        }
    }

    @Test
    fun `include uncalled function plus content from source`() {
        forSandboxes(ContextSandbox.SHARE, ContextSandbox.SCOPE, ContextSandbox.SUBDOCUMENT) { sandbox ->
            execute(
                """
                .noautopagebreak
                # Main
                .include {include/function-with-content.md} sandbox:{$sandbox}
                """.trimIndent(),
            ) {
                assertEquals(
                    "<h1>Main</h1>$OUTPUT_FUNCTION_WITH_CONTENT",
                    it,
                )
            }
        }
    }

    @Test
    fun `share function with included files`() {
        forSandboxes(ContextSandbox.SHARE, ContextSandbox.SCOPE, ContextSandbox.SUBDOCUMENT) { sandbox ->
            execute(
                """
                .function {hello}
                    x:
                    Hello, .x!
                .include {include/shared-function-usage.md} sandbox:{$sandbox}
                """.trimIndent(),
            ) {
                assertEquals(
                    "<h3>Hello, world!</h3>",
                    it,
                )
            }
        }
    }

    @Test
    fun `transitive inclusion`() {
        forSandboxes(ContextSandbox.SHARE, ContextSandbox.SCOPE, ContextSandbox.SUBDOCUMENT) { sandbox ->
            execute(
                """
                .noautopagebreak
                # Main
                .include {include/transitive-include.md} sandbox:{$sandbox}
                """.trimIndent(),
            ) {
                assertEquals(
                    "<h1>Main</h1><h1>Included</h1><p>Hello, Gio!</p><h3>Hello, world!</h3>",
                    it,
                )
            }
        }
    }

    @Test
    fun `invalid usage as value`() {
        // Included file cannot be used as a dynamic value.
        assertFails {
            execute(
                """
                .sum {.include {include/dynamic-value.md}} {3}
                """.trimIndent(),
            ) {}
        }
    }

    @Test
    fun `mutate included data`() {
        forSandboxes(ContextSandbox.SHARE) { sandbox ->
            execute(
                """
                .include {include/mutable-data.md} sandbox:{$sandbox}
                
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
    }

    @Test
    fun `'read' call from updated working directory`() {
        forSandboxes(ContextSandbox.SHARE, ContextSandbox.SCOPE, ContextSandbox.SUBDOCUMENT) { sandbox ->
            execute(
                """
                .include {include/read-relative-path.md}
                """.trimIndent(),
            ) {
                assertEquals(
                    "<p>Line 1\nLine 2\n\nLine 3</p>",
                    it,
                )
            }
        }
    }

    @Test
    fun `relative-path image from updated working directory`() {
        forSandboxes(ContextSandbox.SHARE, ContextSandbox.SCOPE, ContextSandbox.SUBDOCUMENT) { sandbox ->
            execute(
                """
                .include {include/relative-image.md}
                """.trimIndent(),
            ) {
                assertEquals(
                    "<p>img: <img src=\"img/icon.png\" alt=\"img\" /></p>",
                    it,
                )
            }
        }
    }

    @Test
    fun `absolute-path image from updated working directory should not be updated`() {
        execute(
            """
            .include {include/absolute-image.md}
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
            .include {include/url-image.md}
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
            .include {include/reference-image.md}
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
        forSandboxes(ContextSandbox.SHARE, ContextSandbox.SCOPE, ContextSandbox.SUBDOCUMENT) { sandbox ->
            // Load library named 'hello' from libraries/hello.qd
            execute(
                """
                .include {hello} sandbox:{$sandbox}
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
    fun `include library content`() {
        execute(
            ".include {content}",
            loadableLibraries = setOf("content"),
            useDummyLibraryDirectory = true,
        ) {
            assertEquals(
                "<h2>Title</h2><p>Content</p>",
                it,
            )
        }
    }

    @Test
    fun `include multiple sources`() {
        forSandboxes(ContextSandbox.SHARE) { sandbox ->
            execute(
                """
                .noautopagebreak
                .include {include/basic-source.md} sandbox:{$sandbox}
                .include {include/function-with-content.md} sandbox:{$sandbox}
                
                .hello {world}
                """.trimIndent(),
            ) {
                assertEquals(
                    "$OUTPUT_BASIC_SOURCE$OUTPUT_FUNCTION_WITH_CONTENT<p>Hello, world!</p>",
                    it,
                )
            }
        }
    }

    @Test
    fun `include multiple sources via bulk`() {
        execute(
            """
            .noautopagebreak
            .includeall
                - include/basic-source.md
                - include/function-with-content.md
            
            .hello {world}
            """.trimIndent(),
        ) {
            assertEquals(
                "$OUTPUT_BASIC_SOURCE$OUTPUT_FUNCTION_WITH_CONTENT<p>Hello, world!</p>",
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
            if (subdocument == Subdocument.Root) {
                assertTrue(it.startsWith(OUTPUT_ABSOLUTE_IMAGE + OUTPUT_BASIC_SOURCE))
            }
        }
    }
}
