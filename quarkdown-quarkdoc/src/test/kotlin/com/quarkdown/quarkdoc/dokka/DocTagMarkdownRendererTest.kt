package com.quarkdown.quarkdoc.dokka

import com.quarkdown.quarkdoc.dokka.index.DocTagMarkdownRenderer
import org.jetbrains.dokka.model.doc.A
import org.jetbrains.dokka.model.doc.B
import org.jetbrains.dokka.model.doc.Br
import org.jetbrains.dokka.model.doc.CodeBlock
import org.jetbrains.dokka.model.doc.CodeInline
import org.jetbrains.dokka.model.doc.H4
import org.jetbrains.dokka.model.doc.I
import org.jetbrains.dokka.model.doc.Li
import org.jetbrains.dokka.model.doc.P
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.doc.Ul
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [DocTagMarkdownRenderer].
 */
class DocTagMarkdownRendererTest {
    @Test
    fun `paragraphs and emphasis`() {
        val tags =
            listOf(
                P(children = listOf(Text(body = "Hello "), B(children = listOf(Text(body = "bold"))))),
                P(
                    children =
                        listOf(
                            I(children = listOf(Text(body = "italic"))),
                            Text(body = " and "),
                            CodeInline(children = listOf(Text(body = "code"))),
                        ),
                ),
            )
        assertEquals("Hello **bold**\n\n*italic* and `code`", DocTagMarkdownRenderer.render(tags))
    }

    @Test
    fun `loose inline run forms a paragraph`() {
        val tags =
            listOf(
                Text(body = "Likely a "),
                A(children = listOf(Text(body = "body argument")), params = mapOf("href" to "https://example.com")),
                H4(children = listOf(Text(body = "Values"))),
            )
        assertEquals("Likely a [body argument](https://example.com)\n\n#### Values", DocTagMarkdownRenderer.render(tags))
    }

    @Test
    fun `lists with nested blocks`() {
        val list =
            Ul(
                children =
                    listOf(
                        Li(children = listOf(P(children = listOf(Text(body = "first"))), P(children = listOf(Text(body = "more"))))),
                        Li(children = listOf(Text(body = "second"))),
                    ),
            )
        assertEquals("* first\n\n  more\n* second", DocTagMarkdownRenderer.render(listOf(list)))
    }

    @Test
    fun `code block keeps line breaks`() {
        val block =
            CodeBlock(
                children = listOf(Text(body = ".func {x}"), Br, Text(body = "    body")),
            )
        assertEquals("```\n.func {x}\n    body\n```", DocTagMarkdownRenderer.render(listOf(block)))
    }

    @Test
    fun `code block carries language`() {
        val block = CodeBlock(children = listOf(Text(body = ".func {x}")), params = mapOf("lang" to "markdown"))
        assertEquals("```markdown\n.func {x}\n```", DocTagMarkdownRenderer.render(listOf(block)))
    }
}
