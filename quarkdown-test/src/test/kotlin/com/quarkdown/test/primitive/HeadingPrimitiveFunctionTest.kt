package com.quarkdown.test.primitive

import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `.extend` applied to Markdown headings.
 */
class HeadingPrimitiveFunctionTest {
    @Test
    fun `no extension renders unchanged`() {
        execute(
            """
            .noautopagebreak

            # Hello

            ## Hi
            """.trimIndent(),
        ) {
            assertEquals("<h1>Hello</h1><h2>Hi</h2>", it)
        }
    }

    @Test
    fun `extension wraps every heading`() {
        execute(
            """
            .noautopagebreak

            .extend {heading}
                .container
                    .super

            # Hello
            """.trimIndent(),
        ) {
            assertEquals("<div class=\"container\"><h1>Hello</h1></div>", it)
        }
    }

    @Test
    fun `content can be matched and conditionally wrapped`() {
        execute(
            """
            .noautopagebreak

            .extend {heading}
                content:
                .if {.content::equals {Hi}}
                    .container
                        .super
                .ifnot {.content::equals {Hi}}
                    .super

            # Hello

            ## Hi

            ### Hey
            """.trimIndent(),
        ) {
            assertEquals(
                "<h1>Hello</h1><div class=\"container\"><h2>Hi</h2></div><h3>Hey</h3>",
                it,
            )
        }
    }

    @Test
    fun `content can be transformed`() {
        execute(
            """
            .noautopagebreak

            .extend {heading}
                content:
                .super content:{.content::plaintext::capitalize}

            # hello world
            """.trimIndent(),
        ) {
            assertEquals("<h1>Hello world</h1>", it)
        }
    }

    @Test
    fun `depth can be read and drive container styling`() {
        execute(
            """
            .noautopagebreak

            .extend {heading}
                depth:
                .container background:{hsl(.depth::multiply {20}, 50, 50)} fullwidth:{yes}
                    .super

            ## Hello
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container fullwidth\" style=\"background-color: rgba(191, 148, 63, 1.0);\"><h2>Hello</h2></div>",
                it,
            )
        }
    }

    @Test
    fun `depth can be overridden via super`() {
        execute(
            """
            .noautopagebreak

            .extend {heading}
                .super depth:{4}

            # Hello
            """.trimIndent(),
        ) {
            assertEquals("<h4>Hello</h4>", it)
        }
    }

    @Test
    fun `ref can be set via super`() {
        execute(
            """
            .noautopagebreak

            .extend {heading}
                .super ref:{my-id}

            ## Hello
            """.trimIndent(),
        ) {
            assertEquals("<h2 id=\"my-id\">Hello</h2>", it)
        }
    }

    @Test
    fun `breakpage can be disabled via super`() {
        execute(
            """
            .extend {heading}
                .super breakpage:{no}

            # Hello
            """.trimIndent(),
        ) {
            assertEquals("<h1>Hello</h1>", it)
        }
    }

    @Test
    fun `styling can be applied via super`() {
        execute(
            """
            .extend {heading}
                depth:
                .super foreground:{red} \
                       background:{.takeif {red} {@lambda .depth::islower than:{3}}} \
                       fontvariant:{smallcaps}

            ## Hello
            
            ### Hello
            """.trimIndent(),
        ) {
            assertEquals(
                "<h2 style=\"color: rgba(255, 0, 0, 1.0); background-color: rgba(255, 0, 0, 1.0); font-variant: small-caps;\">Hello</h2>" +
                    "<h3 style=\"color: rgba(255, 0, 0, 1.0); font-variant: small-caps;\">Hello</h3>",
                it,
            )
        }
    }

    @Test
    fun `extension reaches headings nested inside a blockquote`() {
        execute(
            """
            .noautopagebreak

            .extend {heading}
                .container
                    .super

            > ## Quoted heading
            """.trimIndent(),
        ) {
            assertEquals(
                "<blockquote><div class=\"container\"><h2>Quoted heading</h2></div></blockquote>",
                it,
            )
        }
    }

    @Test
    fun `extension reaches headings nested inside a list item`() {
        execute(
            """
            .noautopagebreak

            .extend {heading}
                .container
                    .super

            - ## Item heading
            """.trimIndent(),
        ) {
            assertEquals(
                "<ul><li><div class=\"container\"><h2>Item heading</h2></div></li></ul>",
                it,
            )
        }
    }

    @Test
    fun `extension reaches the generated nodes`() {
        execute(
            """
            .doclang {en}

            .extend {heading}
                content:
                .container
                    .super

            # a

            ## b

            .tableofcontents
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
        ) {
            assertEquals(
                "<div class=\"container\"><h1 class=\"page-break\" id=\"a\">a</h1></div>" +
                    "<div class=\"container\"><h2 id=\"b\">b</h2></div>" +
                    "<div class=\"container\">" +
                    "<h1 class=\"page-break\" id=\"table-of-contents\">Table of Contents</h1>" +
                    "</div>" +
                    "<nav role=\"table-of-contents\" data-role=\"table-of-contents\"><ol>" +
                    "<li data-target-id=\"a\" data-depth=\"1\"><a href=\"#a\">a</a>" +
                    "<ol><li data-target-id=\"b\" data-depth=\"2\"><a href=\"#b\">b</a></li></ol></li>" +
                    "</ol></nav>",
                it,
            )
        }
    }

    @Test
    fun `extension considers function call as content`() {
        execute(
            """
            .noautopagebreak
            
            .extend {heading}
                content:
                .container foreground:{.takeif {red} {@lambda .content::plaintext::equals {Hello}}}
                    .super
            
            # Hello
            
            # Hi
            
            # .capitalize {Hello}
            
            # .capitalize {Hi}
            
            .var {x} {Hello}
            
            # .x
            
            .x {Hi}
            
            # .x
            """.trimIndent(),
        ) {
            val hello = "<div class=\"container\" style=\"color: rgba(255, 0, 0, 1.0);\"><h1>Hello</h1></div>"
            val hi = "<div class=\"container\"><h1>Hi</h1></div>"
            assertEquals(
                hello + hi + hello + hi + hello + hi,
                it,
            )
        }
    }
}
