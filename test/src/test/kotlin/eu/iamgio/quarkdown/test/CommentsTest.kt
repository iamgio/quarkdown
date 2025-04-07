package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for Markdown comments.
 */
class CommentsTest {
    @Test
    fun `only comment`() {
        execute("<!-- comment -->") {
            assertEquals("", it)
        }
    }

    @Test
    fun `comment as block`() {
        execute(
            """
            <!-- comment -->
            
            Hello
            
            <!-- comment -->
            
            World
            
            <!-- comment -->
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello</p><p>World</p>", it)
        }
    }

    @Test
    fun `comment as inline`() {
        execute(
            """
            Hello <!-- comment --> World
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello  World</p>", it)
        }

        execute(
            """
            Hello<!-- comment -->World
            """.trimIndent(),
        ) {
            assertEquals("<p>HelloWorld</p>", it)
        }
    }

    @Test
    fun `comment across lines`() {
        execute(
            """
            Hello <!-- comment
            with new lines --> World
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello  World</p>", it)
        }
    }

    @Test
    fun `comment in block markdown argument`() {
        execute(
            """
            .container
                <!-- comment -->
                Hello
            """.trimIndent(),
        ) {
            assertEquals("<div class=\"container\"><p>Hello</p></div>", it)
        }
    }

    @Test
    fun `comment in block lambda argument`() {
        execute(
            """
            .if {yes}
                <!-- comment -->
                Hello
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello</p>", it)
        }
    }

    @Test
    fun `comment in static-value block argument is not a comment`() {
        execute(
            """
            .uppercase
                <!-- comment -->
                Hi
            """.trimIndent(),
        ) {
            assertEquals("<p><!-- COMMENT -->\nHI</p>", it)
        }
    }

    @Test
    fun `comment in inline argument`() {
        execute(".sum {1} {<!-- comment -->3}") {
            assertEquals("<p>4</p>", it)
        }
    }
}
