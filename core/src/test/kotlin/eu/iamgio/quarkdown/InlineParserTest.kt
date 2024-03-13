package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.PlainText
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Parser tests for inline content.
 */
class InlineParserTest {
    /**
     * Tokenizes and parses a [source] code.
     * @param source source code
     * @param assertType if `true`, asserts each output node is of type [T]
     * @param flavor Markdown flavor to use
     * @param T type of the nodes to output
     * @return iterator of the parsed nodes
     */
    private inline fun <reified T : Node> inlineIterator(
        source: CharSequence,
        assertType: Boolean = true,
        flavor: MarkdownFlavor = QuarkdownFlavor,
    ): Iterator<T> {
        val lexer = flavor.lexerFactory.newInlineLexer(source)
        val parser = flavor.parserFactory.newParser()
        return nodesIterator(lexer, parser, assertType)
    }

    @Test
    fun escape() {
        // EscapeToken is parsed into PlainText.
        val nodes = inlineIterator<PlainText>(readSource("/parsing/inline/escape.md"))

        assertEquals("#", nodes.next().text)
        assertEquals("!", nodes.next().text)
        assertEquals(".", nodes.next().text)
        assertEquals(",", nodes.next().text)
        assertEquals("[", nodes.next().text)
        assertEquals("]", nodes.next().text)
    }
}
