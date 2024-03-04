@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.*
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.lexer.NewlineToken
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
     * @param flavor Markdown flavor to use
     * @param T type of the nodes to output
     * @return iterator of the parsed nodes
     */
    private inline fun <reified T : Node> nodesIterator(
        source: CharSequence,
        assertType: Boolean = true,
        flavor: MarkdownFlavor = QuarkdownFlavor,
    ): Iterator<T> {
        val lexer = flavor.lexerFactory.newBlockLexer(source)
        val parser = flavor.parserFactory.newBlockParser()

        return lexer.tokenize().asSequence()
            .filterNot { it is NewlineToken }
            .map { it.accept(parser) }
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
    fun multilineMath() {
        val nodes = nodesIterator<Math>(readSource("/parsing/math_multiline.md"), assertType = false)

        repeat(3) {
            assertEquals("Math expression", nodes.next().text)
        }
        assertEquals("Line 1\nLine 2", nodes.next().text)
    }

    @Test
    fun onelineMath() {
        val nodes = nodesIterator<Math>(readSource("/parsing/math_oneline.md"), assertType = false)

        repeat(3) {
            assertEquals("Math expression", nodes.next().text)
        }
    }

    @Test
    fun horizontalRule() {
        val nodes = nodesIterator<HorizontalRule>(readSource("/parsing/hr.md"), assertType = false)
        assertEquals(6, nodes.asSequence().count())
    }

    @Test
    fun blockQuote() {
        val nodes = nodesIterator<BlockQuote>(readSource("/parsing/blockquote.md"), assertType = false)

        assertEquals("Text", text(nodes.next()))
        assertEquals("Text", text(nodes.next()))
        assertEquals("Line 1\nLine 2", text(nodes.next()))

        with(nodes.next()) {
            assertIs<Paragraph>(children[0])
            assertEquals("Paragraph 1", text(this, childIndex = 0))
            assertIs<Newline>(children[1])
            assertEquals("Paragraph 2", text(this, childIndex = 2))
        }

        with(nodes.next()) {
            assertEquals("Text", text(this))
            assertIs<BlockQuote>(children[1])
            assertEquals("Inner quote", text(children[1] as NestableNode))
        }

        with(nodes.next()) {
            assertEquals("Text\nwith lazy line", text(this))
        }

        with(nodes.next()) {
            assertEquals("Text", text(this))
            assertIs<BlockQuote>(children[1])
            assertEquals("Inner text\nwith lazy\nlines", text(children[1] as NestableNode))
        }

        repeat(3) {
            assertEquals("Text", text(nodes.next()))
        }

        assertIs<OrderedList>(nodes.next().children.first())

        assertFalse(nodes.hasNext())
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

    // This is shared by both unordered and ordered list tests.
    private inline fun <reified T : ListBlock> list(source: CharSequence) {
        val nodes = nodesIterator<T>(source, assertType = false)

        // First list
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)

            if (this is OrderedList) {
                assertEquals(1, startIndex)
            }

            val items = children.iterator()
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertEquals("A", text(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertEquals("B", text(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertEquals("C", text(this))
            }
        }

        // List after two blank lines
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)

            val items = children.iterator()
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertEquals("A", text(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertEquals("B", text(this))
            }
        }

        // Different list for different bullet character
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)

            val items = children.iterator()
            with(children.first()) {
                assertIs<BaseListItem>(this)
                assertEquals("C", text(this))
            }
        }

        // List after two blank lines
        with(nodes.next()) {
            assertIs<T>(this)
            assertTrue(isLoose)

            val items = children.iterator()

            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<Paragraph>(children[2])
                assertEquals("Some paragraph", text(this, childIndex = 2))
            }

            assertIs<Newline>(items.next())

            // Nested list
            with(items.next()) {
                // First list item
                assertIs<BaseListItem>(this)
                assertEquals("B", text(this))
                assertIs<Paragraph>(children[0])
                with(children[1]) {
                    assertIs<T>(this)
                    assertEquals(1, children.size)
                    with(children[0]) {
                        // Second list item
                        assertIs<BaseListItem>(this)
                        assertEquals("Nested 1", text(this))
                        assertIs<Paragraph>(children[0])
                        with(children[1]) {
                            assertIs<T>(this)
                            assertTrue(isLoose)
                            with(children[0]) {
                                // Third list item
                                assertIs<BaseListItem>(this)
                                assertIs<Paragraph>(children[0])
                                assertEquals("Nested A", text(this))
                                assertIs<Newline>(children[1])
                                assertIs<Paragraph>(children[2])
                                assertEquals("Some paragraph", text(this, childIndex = 2))
                            }

                            assertIs<Newline>(children[1])

                            with(children[2]) {
                                assertIs<BaseListItem>(this)
                                assertIs<Paragraph>(children[0])
                                assertEquals("Nested B", text(this))
                            }
                        }
                    }
                }
            }

            assertIs<Newline>(items.next())

            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("C", text(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<BlockQuote>(children[2])
                assertEquals("Some quote", text(children[2] as NestableNode, childIndex = 0))
            }

            assertIs<Newline>(items.next())

            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("D", text(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<Paragraph>(children[2])
                assertEquals("Some paragraph", text(this, childIndex = 2))
                with(children[3]) {
                    assertIs<T>(this)
                    with(children[0]) {
                        assertIs<BaseListItem>(this)
                        assertIs<Paragraph>(children[0])
                        assertEquals("E", text(this))
                        assertIs<Code>(children[1])
                    }
                }
            }

            assertIs<Newline>(items.next())

            with(items.next()) {
                assertIs<BaseListItem>(this)
                with(children[0]) {
                    assertIs<T>(this)
                    with(children[0]) {
                        assertIs<BaseListItem>(this)
                        assertIs<Paragraph>(children[0])
                        assertEquals("E", text(this))
                    }
                }
            }
        }

        // List after paragraph
        with(nodes.next()) {
            assertIs<T>(this)
            assertTrue(isLoose)

            val items = children.iterator()
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("Another list\nwith lazy line", text(this))
            }

            assertIs<Newline>(items.next())

            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("B", text(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<Paragraph>(children[2])
                assertEquals("Some paragraph\nwith lazy line", text(this, childIndex = 2))
            }

            assertIs<Newline>(items.next())

            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Heading>(children[0])
                assertEquals("Heading", text(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("C", text(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Heading>(children[0])
                assertEquals("Heading", text(this, childIndex = 0))
                assertIs<Paragraph>(children[1])
                assertEquals("Some paragraph", text(this, childIndex = 1))
            }
        }

        // List after heading
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)

            if (this is OrderedList) {
                assertEquals(9, startIndex)
            }

            val items = children.iterator()
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("B", text(this))
            }
        }

        // List after horizontal rule
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)
            with(children.iterator().next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this))
            }
        }

        // List after blockquote
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)

            val items = children.iterator()
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<BlockQuote>(children[0])
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this))
            }
        }

        // List after fence code
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)

            if (this is OrderedList) {
                assertEquals(3, startIndex)
            }

            val items = children.iterator()
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", text(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                with(children[0]) {
                    assertIs<Code>(this)
                    assertEquals("Some multiline\ncode", this.text)
                }
            }
        }

        // List after heading
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)

            val items = children.iterator()
            repeat(2) {
                with(items.next()) {
                    assertIs<TaskListItem>(this)
                    assertIs<Paragraph>(children[0])
                    assertEquals("Checked", text(this))
                    assertTrue(isChecked)
                }
            }
            with(items.next()) {
                assertIs<TaskListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("Unchecked", text(this))
                assertFalse(isChecked)
            }
        }

        // List after heading
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)
            assertEquals(2, children.size)

            with((children[1] as BaseListItem).children[1]) {
                assertIs<T>(this)

                if (this is OrderedList) {
                    assertEquals(1, startIndex)
                }

                with(children[0]) {
                    assertIs<TaskListItem>(this)
                    assertTrue(isChecked)

                    assertIs<Paragraph>(children[0])
                }
            }
        }

        assertFalse(nodes.hasNext())
    }

    @Test
    fun unorderedList() {
        list<UnorderedList>(readSource("/parsing/unorderedlist.md"))
    }

    @Test
    fun orderedList() {
        list<OrderedList>(readSource("/parsing/orderedlist.md"))
    }
}
