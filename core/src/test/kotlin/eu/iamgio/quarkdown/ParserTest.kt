package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.lexer.BlockLexer
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.parser.BlockTokenParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Parsing tests.
 */
class ParserTest {
    private val blockParser = BlockTokenParser()

    /**
     * Tokenizes and parses a [source] code, parses each token and asserts each output node is of type [T].
     * @param source source code
     * @param T type of the nodes to output
     * @return iterator of the parsed nodes
     */
    private inline fun <reified T : Node> nodesIterator(source: CharSequence): Iterator<T> {
        return BlockLexer(source).tokenize().asSequence()
            .filterNot { it is NewlineToken }
            .map { it.parse(blockParser) }
            .onEach { assertIs<T>(it) }
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
}
