package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [com.quarkdown.core.ast.quarkdown.block.Box] nodes.
 */
class BoxesTest {
    private fun box(
        type: String,
        title: String?,
        content: String,
    ) = buildString {
        append("<div class=\"box $type\">")
        if (title != null) {
            append("<header><h4>$title</h4></header>")
        }
        append("<div class=\"box-content\"><p>")
        append(content)
        append("</p></div></div>")
    }

    @Test
    fun `titled callout`() {
        execute(".box {Hello}\n\tHello, **world**!") {
            assertEquals(
                box("callout", "Hello", "Hello, <strong>world</strong>!"),
                it,
            )
        }
    }

    @Test
    fun `titled tip`() {
        execute(".box {Hello} type:{tip}\n\tHello, world!") {
            assertEquals(
                box("tip", "Hello", "Hello, world!"),
                it,
            )
        }
    }

    @Test
    fun `titled warning`() {
        execute(".box {Hello, *world*} type:{warning}\n\tHello, world!") {
            assertEquals(
                box("warning", "Hello, <em>world</em>", "Hello, world!"),
                it,
            )
        }
    }

    @Test
    fun error() {
        execute(".box type:{error}\n\tHello, world!") {
            assertEquals(
                box("error", null, "Hello, world!"),
                it,
            )
        }
    }

    @Test
    fun `localized title`() {
        execute(
            """
            .doclang {english}
            .box type:{error}
              Hello, world!
            
            .box type:{tip}
               Hello, world!
               
            .box
              Hello, world!
            """.trimIndent(),
        ) {
            assertEquals(
                box("error", "Error", "Hello, world!") +
                    box("tip", "Tip", "Hello, world!") +
                    box("callout", null, "Hello, world!"),
                it,
            )
        }
    }

    @Test
    fun `unsupported localization`() {
        execute(
            """
            .doclang {japanese}
            .box type:{warning}
              Hello, world!
            """.trimIndent(),
        ) {
            assertEquals(
                box("warning", null, "Hello, world!"),
                it,
            )
        }
    }

    @Test
    fun `to-do`() {
        execute(
            """
            .doclang {italian}
            .todo {Hello, world!}
            """.trimIndent(),
        ) {
            assertEquals(
                box("warning", "DA FARE", "Hello, world!"),
                it,
            )
        }
    }

    @Test
    fun `to-do with fallback localization for missing locale`() {
        execute(
            """
            .todo {Hello, world!}
            """.trimIndent(),
        ) {
            assertEquals(
                box("warning", "TO DO", "Hello, world!"),
                it,
            )
        }
    }

    @Test
    fun `to-do with fallback localization for unsupported locale`() {
        execute(
            """
            .doclang {japanese}
            .todo {Hello, world!}
            """.trimIndent(),
        ) {
            assertEquals(
                box("warning", "TO DO", "Hello, world!"),
                it,
            )
        }
    }
}
