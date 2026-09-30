package com.quarkdown.core

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Checks the emphasis nesting Quarkdown produces against the CommonMark 0.31.2 specification's own examples, vendored in `resources/commonmark/emphasis.txt` as `markdown TAB html TAB number`.
 *
 * Only the nesting is compared, never the text: both sides project to `em[...]`, `strong[...]` and `t`, with
 * adjacent text collapsing, so text replacements and entity handling cannot affect the result.
 */
class CommonMarkEmphasisTest {
    /**
     * Examples whose difference is permanent, with the reason.
     */
    private val knownDifferences =
        mapOf(
            475 to "Quarkdown has no raw HTML inline construct, so the delimiters inside a tag pair",
            476 to "Quarkdown has no raw HTML inline construct, so the delimiters inside a tag pair",
            477 to "Quarkdown has no raw HTML inline construct, so the delimiters inside a tag pair",
        )

    /**
     * Projects a node the way [htmlShape] projects the reference output: a link contributes a text unit for
     * each of its tags, so its label's own emphasis shows exactly as it does in the HTML.
     * @return the node's shape
     */
    private fun Node.shape(): String =
        when (this) {
            is StrongEmphasis -> "em[strong[${children.joinToString("") { it.shape() }}]]"
            is Emphasis -> "em[${children.joinToString("") { it.shape() }}]"
            is Strong -> "strong[${children.joinToString("") { it.shape() }}]"
            is Strikethrough -> "del[${children.joinToString("") { it.shape() }}]"
            is LinkNode -> "t${label.joinToString("") { it.shape() }}t"
            else -> "t"
        }

    private fun String.collapseText() = replace(Regex("t+"), "t")

    /**
     * @param html the specification's reference output
     * @return the same shape the token stream projects to
     */
    private fun htmlShape(html: String): String {
        val out = StringBuilder()
        var index = 0
        while (index < html.length) {
            when {
                html.startsWith("<em>", index) -> {
                    out.append("em[")
                    index += "<em>".length
                }

                html.startsWith("</em>", index) -> {
                    out.append("]")
                    index += "</em>".length
                }

                html.startsWith("<strong>", index) -> {
                    out.append("strong[")
                    index += "<strong>".length
                }

                html.startsWith("</strong>", index) -> {
                    out.append("]")
                    index += "</strong>".length
                }

                html.startsWith("<p>", index) -> index += "<p>".length
                html.startsWith("</p>", index) -> index += "</p>".length
                html[index] == '<' -> {
                    index = html.indexOf('>', index).let { if (it < 0) html.length else it + 1 }
                    out.append("t")
                }

                html[index] == '\n' -> index++
                else -> {
                    out.append("t")
                    index++
                }
            }
        }
        return out.toString().collapseText()
    }

    private fun unescape(text: String) = text.replace("\\n", "\n").replace("\\t", "\t").replace("\\\\", "\\")

    private fun shapeOf(source: String): String {
        val context = MutableContext(QuarkdownFlavor)
        context.attachMockPipeline()
        val parser = QuarkdownFlavor.parserFactory.newParser(context)
        return QuarkdownFlavor.lexerFactory
            .newInlineLexer(source)
            .tokenize()
            .joinToString("") { it.accept(parser).shape() }
            .collapseText()
    }

    @Test
    fun `emphasis nesting matches the specification`() {
        val failures = mutableListOf<String>()
        readSource("/commonmark/emphasis.txt").lineSequence().filter { it.isNotBlank() }.forEach { line ->
            val (rawMarkdown, rawHtml, rawNumber) = line.split("\t")
            val example = rawNumber.toInt()
            val markdown = unescape(rawMarkdown).trim()
            val expected = htmlShape(unescape(rawHtml))
            val actual = shapeOf(markdown)
            val reason = knownDifferences[example]
            when {
                expected == actual && reason != null ->
                    failures += "example $example <$markdown> now matches: remove it from knownDifferences ($reason)"

                expected != actual && reason == null ->
                    failures += "example $example <$markdown> expected $expected but was $actual"
            }
        }
        assertEquals(emptyList(), failures)
    }
}
