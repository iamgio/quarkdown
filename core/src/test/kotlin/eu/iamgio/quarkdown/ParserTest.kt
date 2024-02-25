package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.BlockQuote
import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.HorizontalRule
import eu.iamgio.quarkdown.ast.Html
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.ListItem
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Newline
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.ast.TextNode
import eu.iamgio.quarkdown.ast.UnorderedList
import eu.iamgio.quarkdown.lexer.BlockLexer
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.parser.BlockTokenParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Parsing tests.
 */
class ParserTest {
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
        val lexer = BlockLexer(source)
        val parser = BlockTokenParser(lexer)

        return lexer.tokenize().asSequence()
            .filterNot { it is NewlineToken }
            .map { it.parse(parser) }
            .onEach {
                if (assertType) {
                    assertIs<T>(it)
                }
            }
            .filterIsInstance<T>()
            .iterator()
    }

    /**
     * @param node parent node
     * @param childIndex index of the text child
     * @return text content of the [childIndex]-th child
     */
    private fun text(
        node: NestableNode,
        childIndex: Int = 0,
    ) = (node.children[childIndex] as TextNode).text

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
    fun setextHeading() {
        val nodes = nodesIterator<Heading>(readSource("/parsing/setextheading.md"))

        repeat(3) {
            with(nodes.next()) {
                assertEquals("Title 1", text)
                assertEquals(1, depth)
            }
        }
        repeat(3) {
            with(nodes.next()) {
                assertEquals("Title 2", text)
                assertEquals(2, depth)
            }
        }
    }

    @Test
    fun blockCode() {
        val nodes = nodesIterator<Code>(readSource("/parsing/blockcode.md"))

        assertEquals("Code line 1\nCode line 2\n\nCode line 3", nodes.next().text)
        assertFalse(nodes.hasNext())
    }

    @Test
    fun fencesCode() {
        val nodes = nodesIterator<Code>(readSource("/parsing/fencescode.md"))

        with(nodes.next()) {
            assertEquals("Code", text)
            assertEquals(null, language)
        }
        with(nodes.next()) {
            assertEquals("Code", text)
            assertEquals(null, language)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\nCode line 2", text)
            assertEquals(null, language)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\n    Code line 2", text)
            assertEquals(null, language)
        }
        with(nodes.next()) {
            assertEquals("Code", text)
            assertEquals("text", language)
        }
        with(nodes.next()) {
            assertEquals("Code", text)
            assertEquals("text", language)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\nCode line 2", text)
            assertEquals("text", language)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\n    Code line 2", text)
            assertEquals("text", language)
        }
        with(nodes.next()) {
            assertEquals("let x;", text)
            assertEquals("ecmascript 6", language)
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

        assertEquals("Text", text(nodes.next()))
        assertEquals("Text", text(nodes.next()))
        assertEquals("Line 1\nLine 2", text(nodes.next()))

        with(nodes.next()) {
            assertEquals("Paragraph 1", text(this, childIndex = 0))
            assertIs<Newline>(children[1])
            assertEquals("Paragraph 2", text(this, childIndex = 2))
        }

        with(nodes.next()) {
            assertEquals("Text", text(this))
            assertIs<BlockQuote>(children[1])
            assertEquals("Inner quote", text(children[1] as NestableNode))
        }
    }

    @Test
    fun linkDefinition() {
        val nodes = nodesIterator<LinkDefinition>(readSource("/parsing/linkdefinition.md"))

        with(nodes.next()) {
            assertEquals("label", text)
            assertEquals("https://google.com", url)
            assertEquals(null, title)
        }
        with(nodes.next()) {
            assertEquals("label", text)
            assertEquals("url", url)
            assertEquals(null, title)
        }
        with(nodes.next()) {
            assertEquals("label", text)
            assertEquals("/url", url)
            assertEquals(null, title)
        }
        repeat(3) {
            with(nodes.next()) {
                assertEquals("label", text)
                assertEquals("https://google.com", url)
                assertEquals("Title", title)
            }
        }
        with(nodes.next()) {
            assertEquals("label", text)
            assertEquals("https://google.com", url)
            assertEquals("Multiline\ntitle", title)
        }
        with(nodes.next()) {
            assertEquals("label", text)
            assertEquals("https://google.com", url)
            assertEquals("Line 1\nLine 2\nLine 3", title)
        }
        with(nodes.next()) {
            assertEquals("label", text)
            assertEquals("/url", url)
            assertEquals("Title", title)
        }
    }

    @Test
    fun html() {
        val nodes = nodesIterator<Html>(readSource("/parsing/html.md"))

        assertEquals("<p>Text</p>", nodes.next().content)
        assertEquals("<p><i>Text</i></p>", nodes.next().content)
        assertTrue { nodes.next().content.endsWith("</header>") }
        assertTrue { nodes.next().content.endsWith("</html>") }
    }

    @Test
    fun unorderedList() {
        val nodes = nodesIterator<UnorderedList>(readSource("/parsing/unorderedlist.md"), assertType = false)

        // First list
        with(nodes.next().children.iterator()) {
            with(next()) {
                assertIs<ListItem>(this)
                assertEquals("A", text(this))
            }
            with(next()) {
                assertIs<ListItem>(this)
                assertEquals("B", text(this))
            }
            with(next()) {
                assertIs<ListItem>(this)
                assertEquals("C", text(this))
            }
            // TODO this should be a separate list (divided by double empty line)
            // (done, just test) (should be ok, update tests and commit)
        }

        // List after two blank lines
        with(nodes.next().children.iterator()) {
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<Paragraph>(children[2])
                assertEquals("Some paragraph", text(this, childIndex = 2))
            }

            // Nested list
            with(next()) {
                // First list item
                assertIs<ListItem>(this)
                assertEquals("B", text(this))
                assertIs<Paragraph>(children[0])
                with(children[1]) {
                    assertIs<UnorderedList>(this)
                    assertEquals(1, children.size)
                    with(children[0]) {
                        // Second list item
                        assertIs<ListItem>(this)
                        assertEquals("Nested 1", text(this))
                        assertIs<Paragraph>(children[0])
                        with(children[1]) {
                            assertIs<UnorderedList>(this)
                            with(children[0]) {
                                // Third list item
                                assertIs<ListItem>(this)
                                assertIs<Paragraph>(children[0])
                                assertEquals("Nested 2", text(this))
                                assertIs<Newline>(children[1])
                                assertIs<Paragraph>(children[2])
                                assertEquals("Some paragraph", text(this, childIndex = 2))
                            }
                        }
                    }
                }
            }

            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("C", text(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<BlockQuote>(children[2])
                assertEquals("Some quote", text(children[2] as NestableNode, childIndex = 0))
            }

            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("D", text(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<Paragraph>(children[2])
                assertEquals("Some paragraph", text(this, childIndex = 2))
                with(children[3]) {
                    assertIs<UnorderedList>(this)
                    with(children[0]) {
                        assertIs<ListItem>(this)
                        assertIs<Paragraph>(children[0])
                        assertEquals("E", text(this))
                        assertIs<Code>(children[1])
                    }
                }
            }

            with(next()) {
                assertIs<ListItem>(this)
                with(children[0]) {
                    assertIs<UnorderedList>(this)
                    with(children[0]) {
                        assertIs<ListItem>(this)
                        assertIs<Paragraph>(children[0])
                        assertEquals("E", text(this))
                    }
                }
            }
        }

        // List after paragraph
        with(nodes.next().children.iterator()) {
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("Another list\nwith lazy line", text(this))
            }
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("B", text(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<Paragraph>(children[2])
                assertEquals("Some paragraph\n with lazy line", text(this, childIndex = 2))
            }
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Heading>(children[0])
                assertEquals("Heading", text(this))
            }
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("C", text(this))
            }
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Heading>(children[0])
                assertEquals("Heading", text(this, childIndex = 0))
                assertIs<Paragraph>(children[1])
                assertEquals("Some paragraph", text(this, childIndex = 1))
            }
        }

        // List after heading
        with(nodes.next().children.iterator()) {
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this))
            }
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("B", text(this))
            }
        }

        // List after horizontal rule
        with(nodes.next().children.iterator()) {
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this))
            }
        }

        // List after blockquote
        with(nodes.next().children.iterator()) {
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<BlockQuote>(children[0])
            }
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this))
            }
        }

        // List after fence code
        with(nodes.next().children.iterator()) {
            with(next()) {
                assertIs<ListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this))
            }
        }

        assertFalse(nodes.hasNext())
    }
}
