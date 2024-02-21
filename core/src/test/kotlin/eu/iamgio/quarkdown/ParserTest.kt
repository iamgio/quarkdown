package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.BlockQuote
import eu.iamgio.quarkdown.ast.FencesCode
import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.HorizontalRule
import eu.iamgio.quarkdown.ast.Html
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.lexer.BlockLexer
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.parser.BlockTokenParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Parsing tests.
 */
class ParserTest {
    private val blockParser = BlockTokenParser()

    /**
     * Tokenizes and parses a [source] code, parses each token.
     * @param source source code
     * @param assertType if `true`, asserts each output node is of type [T]
     * @param T type of the nodes to output
     * @return iterator of the parsed nodes
     */
    private inline fun <reified T : Node> nodesIterator(
        source: CharSequence,
        assertType: Boolean = true,
    ): Iterator<T> {
        return BlockLexer(source).tokenize().asSequence()
            .filterNot { it is NewlineToken }
            .map { it.parse(blockParser) }
            .onEach {
                if (assertType) {
                    assertIs<T>(it)
                }
            }
            .filterIsInstance<T>()
            .iterator()
    }

    @Test
    fun paragraph() {
        val nodes = nodesIterator<Paragraph>(readSource("/parsing/paragraph.md"))

        assertEquals("Paragraph 1", nodes.next().text)
        assertEquals("Paragraph 2", nodes.next().text)
        assertEquals("Paragraph 3", nodes.next().text)
        assertEquals("Paragraph 4\nwith lazy line", nodes.next().text)
    }

    @Test
    fun heading() {
        val nodes = nodesIterator<Heading>(readSource("/parsing/heading.md"), assertType = false)

        with(nodes.next()) {
            assertEquals("Title", text)
            assertEquals(1, depth)
        }
        with(nodes.next()) {
            assertEquals("Title", text)
            assertEquals(2, depth)
        }
        with(nodes.next()) {
            assertEquals("Title", text)
            assertEquals(3, depth)
        }
        with(nodes.next()) {
            assertEquals("", text)
            assertEquals(1, depth)
        }
        with(nodes.next()) {
            assertEquals("Title with closing sequence", text)
            assertEquals(2, depth)
        }
    }

    @Test
    fun fencesCode() {
        val nodes = nodesIterator<FencesCode>(readSource("/parsing/fencescode.md"))

        with(nodes.next()) {
            assertEquals("Code", text)
            assertEquals(null, lang)
        }
        with(nodes.next()) {
            assertEquals("Code", text)
            assertEquals(null, lang)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\nCode line 2", text)
            assertEquals(null, lang)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\n    Code line 2", text)
            assertEquals(null, lang)
        }
        with(nodes.next()) {
            assertEquals("Code", text)
            assertEquals("text", lang)
        }
        with(nodes.next()) {
            assertEquals("Code", text)
            assertEquals("text", lang)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\nCode line 2", text)
            assertEquals("text", lang)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\n    Code line 2", text)
            assertEquals("text", lang)
        }
        with(nodes.next()) {
            assertEquals("let x;", text)
            assertEquals("ecmascript 6", lang)
        }
    }

    @Test
    fun horizontalRule() {
        val nodes = nodesIterator<HorizontalRule>(readSource("/parsing/hr.md"), assertType = false)
        assertEquals(6, nodes.asSequence().count())
    }

    @Test
    fun blockQuote() {
        val nodes = nodesIterator<BlockQuote>(readSource("/parsing/blockquote.md"))

        fun text(blockQuote: BlockQuote) = (blockQuote.children.first() as Paragraph).text

        assertEquals("Text", text(nodes.next()))
        assertEquals("Text", text(nodes.next()))
        assertEquals("Line 1\nLine 2", text(nodes.next()))
    }

    @Test
    fun html() {
        val nodes = nodesIterator<Html>(readSource("/parsing/html.md"))

        assertEquals("<p>Text</p>", nodes.next().content)
        assertEquals("<p><i>Text</i></p>", nodes.next().content)
        assertTrue { nodes.next().content.endsWith("</header>") }
        assertTrue { nodes.next().content.endsWith("</html>") }
    }
}
