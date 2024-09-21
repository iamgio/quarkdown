package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.ast.base.block.BaseListItem
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.base.block.HorizontalRule
import eu.iamgio.quarkdown.ast.base.block.Html
import eu.iamgio.quarkdown.ast.base.block.LinkDefinition
import eu.iamgio.quarkdown.ast.base.block.ListBlock
import eu.iamgio.quarkdown.ast.base.block.Newline
import eu.iamgio.quarkdown.ast.base.block.OrderedList
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.block.Table
import eu.iamgio.quarkdown.ast.base.block.TaskListItem
import eu.iamgio.quarkdown.ast.base.block.UnorderedList
import eu.iamgio.quarkdown.ast.base.inline.Emphasis
import eu.iamgio.quarkdown.ast.base.inline.PlainTextNode
import eu.iamgio.quarkdown.ast.base.inline.Strong
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.quarkdown.FunctionCallNode
import eu.iamgio.quarkdown.ast.quarkdown.block.Math
import eu.iamgio.quarkdown.ast.quarkdown.block.PageBreak
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.util.toPlainText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Parsing tests.
 */
class BlockParserTest {
    /**
     * Tokenizes and parses a [source] code.
     * @param source source code
     * @param assertType if `true`, asserts each output node is of type [T]
     * @param flavor Markdown flavor to use
     * @param T type of the nodes to output
     * @return iterator of the parsed nodes
     */
    private inline fun <reified T : Node> blocksIterator(
        source: CharSequence,
        assertType: Boolean = true,
        flavor: MarkdownFlavor = QuarkdownFlavor,
    ): Iterator<T> {
        val context = MutableContext(flavor)
        context.attachMockPipeline()

        val lexer = flavor.lexerFactory.newBlockLexer(source)
        val parser = flavor.parserFactory.newParser(context)
        return nodesIterator(lexer, parser, assertType)
    }

    private val TextNode.rawText: String
        get() {
            (children.singleOrNull() as? PlainTextNode)?.let {
                return it.text
            }
            throw IllegalStateException("rawText requires a single PlainText node")
        }

    /**
     * @param node parent node
     * @param childIndex index of the text child
     * @return text content of the [childIndex]-th child
     */
    private fun rawText(
        node: NestableNode,
        childIndex: Int = 0,
    ): String = (node.children[childIndex] as TextNode).rawText

    @Test
    fun paragraph() {
        val nodes = blocksIterator<Paragraph>(readSource("/parsing/paragraph.md"))

        assertEquals("Paragraph 1", nodes.next().rawText)
        assertEquals("Paragraph 2", nodes.next().rawText)
        assertEquals("Paragraph 3", nodes.next().rawText)
        assertEquals("Paragraph 4\nwith lazy line", nodes.next().rawText)
    }

    @Test
    fun heading() {
        val nodes = blocksIterator<Heading>(readSource("/parsing/heading.md"), assertType = false)

        with(nodes.next()) {
            assertEquals("Title", rawText)
            assertNull(customId)
            assertEquals(1, depth)
        }
        with(nodes.next()) {
            assertEquals("Title", rawText)
            assertNull(customId)
            assertEquals(2, depth)
        }
        with(nodes.next()) {
            assertEquals("Title", rawText)
            assertNull(customId)
            assertEquals(3, depth)
        }
        with(nodes.next()) {
            assertTrue(text.isEmpty())
            assertNull(customId)
            assertEquals(1, depth)
        }
        with(nodes.next()) {
            assertEquals("Title with closing sequence", rawText)
            assertNull(customId)
            assertEquals(2, depth)
        }
        with(nodes.next()) {
            assertEquals("Title with custom ID", rawText)
            assertEquals("custom-id", customId)
            assertEquals(1, depth)
        }
        with(nodes.next()) {
            assertEquals("Title with custom ID", rawText)
            assertEquals("id", customId)
            assertEquals(3, depth)
        }
    }

    @Test
    fun setextHeading() {
        val nodes = blocksIterator<Heading>(readSource("/parsing/setextheading.md"))

        repeat(3) {
            with(nodes.next()) {
                assertEquals("Title 1", rawText)
                assertNull(customId)
                assertEquals(1, depth)
            }
        }
        repeat(3) {
            with(nodes.next()) {
                assertEquals("Title 2", rawText)
                assertNull(customId)
                assertEquals(2, depth)
            }
        }
        with(nodes.next()) {
            assertEquals("Title with ID", rawText)
            assertEquals("my-id", customId)
            assertEquals(1, depth)
        }
    }

    @Test
    fun blockCode() {
        val nodes = blocksIterator<Code>(readSource("/parsing/blockcode.md"))

        assertEquals("Code line 1\nCode line 2\n\nCode line 3", nodes.next().content)
        assertFalse(nodes.hasNext())
    }

    @Test
    fun fencesCode() {
        val nodes = blocksIterator<Code>(readSource("/parsing/fencescode.md"))

        with(nodes.next()) {
            assertEquals("Code", content)
            assertEquals(null, language)
        }
        with(nodes.next()) {
            assertEquals("Code", content)
            assertEquals(null, language)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\nCode line 2", content)
            assertEquals(null, language)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\n    Code line 2", content)
            assertEquals(null, language)
        }
        with(nodes.next()) {
            assertEquals("Code", content)
            assertEquals("text", language)
        }
        with(nodes.next()) {
            assertEquals("Code", content)
            assertEquals("text", language)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\nCode line 2", content)
            assertEquals("text", language)
        }
        with(nodes.next()) {
            assertEquals("Code line 1\n    Code line 2", content)
            assertEquals("text", language)
        }
        with(nodes.next()) {
            assertEquals("let x;", content)
            assertEquals("ecmascript 6", language)
        }
    }

    @Test
    fun multilineMath() {
        val nodes = blocksIterator<Math>(readSource("/parsing/math_multiline.md"), assertType = false)

        repeat(3) {
            assertEquals("Math expression", nodes.next().expression)
        }
        assertEquals("Line 1\nLine 2", nodes.next().expression)
    }

    @Test
    fun onelineMath() {
        val nodes = blocksIterator<Math>(readSource("/parsing/math_oneline.md"), assertType = false)

        repeat(2) {
            assertEquals("Math expression", nodes.next().expression)
        }
    }

    @Test
    fun horizontalRule() {
        val nodes = blocksIterator<HorizontalRule>(readSource("/parsing/hr.md"), assertType = false)
        assertEquals(6, nodes.asSequence().count())
    }

    @Test
    fun pageBreak() {
        val nodes = blocksIterator<PageBreak>(readSource("/parsing/pagebreak.md"), assertType = false)
        assertEquals(2, nodes.asSequence().count())
    }

    @Test
    fun blockQuote() {
        val nodes = blocksIterator<BlockQuote>(readSource("/parsing/blockquote.md"), assertType = false)

        assertEquals("Text", rawText(nodes.next()))
        assertEquals("Text", rawText(nodes.next()))
        assertEquals("Line 1\nLine 2", rawText(nodes.next()))

        with(nodes.next()) {
            assertIs<Paragraph>(children[0])
            assertEquals("Paragraph 1", rawText(this, childIndex = 0))
            assertIs<Newline>(children[1])
            assertEquals("Paragraph 2", rawText(this, childIndex = 2))
        }

        with(nodes.next()) {
            assertEquals("Text", rawText(this))
            assertIs<BlockQuote>(children[1])
            assertEquals("Inner quote", rawText(children[1] as NestableNode))
        }

        with(nodes.next()) {
            assertEquals("Text\nwith lazy line", rawText(this))
        }

        with(nodes.next()) {
            assertEquals("Text", rawText(this))
            assertIs<BlockQuote>(children[1])
            assertEquals("Inner text\nwith lazy\nlines", rawText(children[1] as NestableNode))
        }

        repeat(3) {
            assertEquals("Text", rawText(nodes.next()))
        }

        with(nodes.next().children.first()) {
            assertIs<OrderedList>(this)
            assertEquals(2, children.size)
        }

        with(nodes.next()) {
            assertEquals("A note.", rawText(this))
            assertEquals(BlockQuote.Type.NOTE, type)
        }

        with(nodes.next()) {
            assertEquals("This is a tip!", rawText(this))
            assertIs<UnorderedList>(children[1])
            assertEquals(BlockQuote.Type.TIP, type)
        }

        with(nodes.next()) {
            assertEquals("you should be\nmore careful.", rawText(this))
            assertEquals(BlockQuote.Type.WARNING, type)
        }

        with(nodes.next()) {
            assertEquals("Something: not a typed quote.", rawText(this))
            assertNull(type)
        }

        with(nodes.next()) {
            assertEquals("To be, or not to be, that is the question.", rawText(this))
            assertEquals(Text("William Shakespeare, Hamlet"), attribution!!.single())
        }

        with(nodes.next()) {
            assertEquals("Shopping list", rawText(this))
            assertIs<UnorderedList>(children[1])
            assertNull(attribution)
        }

        with(nodes.next()) {
            assertEquals(1, children.size)
            children.first().let { inner ->
                assertIs<BlockQuote>(inner)
                assertEquals("You miss 100% of the shots you don’t take.", inner.children.toPlainText())
                assertEquals(Text("Wayne Gretzky"), inner.attribution!!.single())
            }
            assertEquals(Emphasis(listOf(Text("Michael Scott"))), attribution!!.single())
        }

        with(nodes.next()) {
            assertEquals("Try Quarkdown.", rawText(this))
            assertEquals(Text("iamgio"), attribution!!.single())
            assertEquals(BlockQuote.Type.TIP, type)
        }

        assertFalse(nodes.hasNext())
    }

    @Test
    fun linkDefinition() {
        val nodes = blocksIterator<LinkDefinition>(readSource("/parsing/linkdefinition.md"))

        with(nodes.next()) {
            assertEquals("label", rawText)
            assertEquals("https://google.com", url)
            assertEquals(null, title)
        }
        with(nodes.next()) {
            assertEquals("label", rawText)
            assertEquals("url", url)
            assertEquals(null, title)
        }
        with(nodes.next()) {
            assertEquals("label", rawText)
            assertEquals("/url", url)
            assertEquals(null, title)
        }
        repeat(4) {
            with(nodes.next()) {
                assertEquals("label", rawText)
                assertEquals("https://google.com", url)
                assertEquals("Title", title)
            }
        }
        with(nodes.next()) {
            assertEquals("label", rawText)
            assertEquals("https://google.com", url)
            assertEquals("Multiline\ntitle", title)
        }
        with(nodes.next()) {
            assertEquals("label", rawText)
            assertEquals("https://google.com", url)
            assertEquals("Line 1\nLine 2\nLine 3", title)
        }
        with(nodes.next()) {
            assertEquals("label", rawText)
            assertEquals("/url", url)
            assertEquals("Title", title)
        }
    }

    @Test
    fun html() {
        val nodes = blocksIterator<Html>(readSource("/parsing/html.md"))

        assertEquals("<p>Text</p>", nodes.next().content)
        assertEquals("<p><i>Text</i></p>", nodes.next().content)
        assertTrue { nodes.next().content.endsWith("</header>") }
        assertTrue { nodes.next().content.endsWith("</html>") }
    }

    @Test
    fun table() {
        val nodes = blocksIterator<Table>(readSource("/parsing/table.md"))

        with(nodes.next().columns.iterator()) {
            with(next()) {
                assertEquals(Table.Alignment.NONE, alignment)
                assertEquals(Text("foo"), header.text.first())
                assertEquals(2, cells.size)
                assertEquals(Text("abc"), cells[0].text.first())
                assertEquals(Text("ghi"), cells[1].text.first())
            }
            with(next()) {
                assertEquals(Table.Alignment.NONE, alignment)
                assertEquals(Text("bar"), header.text.first())
                assertEquals(2, cells.size)
                assertEquals(Text("def"), cells[0].text.first())
                assertEquals(Text("jkl"), cells[1].text.first())
            }
        }

        with(nodes.next().columns.iterator()) {
            with(next()) {
                assertEquals(Table.Alignment.CENTER, alignment)
                assertEquals(Text("abc"), header.text.first())
                assertEquals(1, cells.size)
                assertEquals(Text("bar"), cells.first().text.first())
            }
            with(next()) {
                assertEquals(Table.Alignment.RIGHT, alignment)
                assertEquals(Text("defghi"), header.text.first())
                assertEquals(1, cells.size)
                assertEquals(Text("baz"), cells.first().text.first())
            }
        }

        with(nodes.next().columns.iterator()) {
            with(next()) {
                assertEquals(Table.Alignment.NONE, alignment)
                assertEquals(2, cells.size)
            }
            assertFalse(hasNext())
        }

        with(nodes.next().columns.iterator()) {
            with(next()) {
                assertEquals(Table.Alignment.NONE, alignment)
                assertEquals(Text("abc"), header.text.first())
                assertEquals(2, cells.size)
                assertEquals(Text("bar"), cells[0].text.first())
                assertEquals(Text("bar"), cells[1].text.first())
            }
            with(next()) {
                assertEquals(Table.Alignment.LEFT, alignment)
                assertEquals(Text("def"), header.text.first())
                assertEquals(2, cells.size)
                assertTrue(cells[0].text.isEmpty())
                assertEquals(Text("baz"), cells[1].text.first())
            }

            assertFalse(hasNext())
        }

        with(nodes.next().columns.iterator()) {
            with(next()) {
                assertEquals(Table.Alignment.LEFT, alignment)
                assertTrue(header.text.isEmpty())
                assertEquals(2, cells.size)
                assertEquals(Strong(listOf(Text("C"))), cells[0].text.first())
                assertEquals(Strong(listOf(Text("D"))), cells[1].text.first())
            }
            with(next()) {
                assertEquals(Table.Alignment.NONE, alignment)
                assertEquals(Text("A"), header.text.first())
                assertEquals(2, cells.size)
                assertEquals(Text("AC"), cells[0].text.first())
                assertEquals(Text("AD"), cells[1].text.first())
            }
            with(next()) {
                assertEquals(Table.Alignment.RIGHT, alignment)
                assertEquals(Text("B"), header.text.first())
                assertEquals(2, cells.size)
                assertEquals(Text("BC"), cells[0].text.first())
                assertEquals(Text("BD"), cells[1].text.first())
            }

            assertFalse(hasNext())
        }

        assertFalse(nodes.hasNext())
    }

    // This is shared by both unordered and ordered list tests.
    private inline fun <reified T : ListBlock> list(source: CharSequence) {
        val nodes = blocksIterator<T>(source, assertType = false)

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
                assertEquals("A", rawText(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertEquals("B", rawText(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertEquals("C", rawText(this))
            }
        }

        // List after two blank lines
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)

            val items = children.iterator()
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertEquals("A", rawText(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertEquals("B", rawText(this))
            }
        }

        // Different list for different bullet character
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)

            with(children.first()) {
                assertIs<BaseListItem>(this)
                assertEquals("C", rawText(this))
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
                assertEquals("A", rawText(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<Paragraph>(children[2])
                assertEquals("Some paragraph", rawText(this, childIndex = 2))
            }

            assertIs<Newline>(items.next())

            // Nested list
            with(items.next()) {
                // First list item
                assertIs<BaseListItem>(this)
                assertEquals("B", rawText(this))
                assertIs<Paragraph>(children[0])
                with(children[1]) {
                    assertIs<T>(this)
                    assertEquals(1, children.size)
                    with(children[0]) {
                        // Second list item
                        assertIs<BaseListItem>(this)
                        assertEquals("Nested 1", rawText(this))
                        assertIs<Paragraph>(children[0])
                        with(children[1]) {
                            assertIs<T>(this)
                            assertTrue(isLoose)
                            with(children[0]) {
                                // Third list item
                                assertIs<BaseListItem>(this)
                                assertIs<Paragraph>(children[0])
                                assertEquals("Nested A", rawText(this))
                                assertIs<Newline>(children[1])
                                assertIs<Paragraph>(children[2])
                                assertEquals("Some paragraph", rawText(this, childIndex = 2))
                            }

                            assertIs<Newline>(children[1])

                            with(children[2]) {
                                assertIs<BaseListItem>(this)
                                assertIs<Paragraph>(children[0])
                                assertEquals("Nested B", rawText(this))
                            }
                        }
                    }
                }
            }

            assertIs<Newline>(items.next())

            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("C", rawText(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<BlockQuote>(children[2])
                assertEquals("Some quote", rawText(children[2] as NestableNode, childIndex = 0))
            }

            assertIs<Newline>(items.next())

            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("D", rawText(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<Paragraph>(children[2])
                assertEquals("Some paragraph", rawText(this, childIndex = 2))
                with(children[3]) {
                    assertIs<T>(this)
                    with(children[0]) {
                        assertIs<BaseListItem>(this)
                        assertIs<Paragraph>(children[0])
                        assertEquals("E", rawText(this))
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
                        assertEquals("E", rawText(this))
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
                assertEquals("Another list\nwith lazy line", rawText(this))
            }

            assertIs<Newline>(items.next())

            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("B", rawText(this, childIndex = 0))
                assertIs<Newline>(children[1])
                assertIs<Paragraph>(children[2])
                assertEquals("Some paragraph\nwith lazy line", rawText(this, childIndex = 2))
            }

            assertIs<Newline>(items.next())

            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Heading>(children[0])
                assertEquals("Heading", rawText(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("C", rawText(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Heading>(children[0])
                assertEquals("Heading", rawText(this, childIndex = 0))
                assertIs<Paragraph>(children[1])
                assertEquals("Some paragraph", rawText(this, childIndex = 1))
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
                assertEquals("A", rawText(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("B", rawText(this))
            }
        }

        // List after horizontal rule
        with(nodes.next()) {
            assertIs<T>(this)
            assertFalse(isLoose)
            with(children.iterator().next()) {
                assertIs<BaseListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("A", rawText(this))
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
                assertEquals("A", rawText(this))
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
                assertEquals("A", rawText(this))
            }
            with(items.next()) {
                assertIs<BaseListItem>(this)
                with(children[0]) {
                    assertIs<Code>(this)
                    assertEquals("Some multiline\ncode", this.content)
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
                    assertEquals("Checked", rawText(this))
                    assertTrue(isChecked)
                }
            }
            with(items.next()) {
                assertIs<TaskListItem>(this)
                assertIs<Paragraph>(children[0])
                assertEquals("Unchecked", rawText(this))
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

    @Test
    fun functionCall() {
        val nodes = blocksIterator<FunctionCallNode>(readSource("/parsing/functioncall.md"), assertType = false)

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(0, arguments.size)
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(2, arguments.size)
            assertEquals("arg1", arguments[0].value.unwrappedValue)
            assertEquals("arg2", arguments[1].value.unwrappedValue)
            assertFalse(arguments[0].isBody)
            assertFalse(arguments[1].isBody)
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(1, arguments.size)
            assertEquals("arg1}", arguments[0].value.unwrappedValue)
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(0, arguments.size)
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(1, arguments.size)
            assertTrue(arguments.first().isBody)

            assertEquals(
                "body content",
                arguments.first().value.unwrappedValue,
            )
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(1, arguments.size)
            assertTrue(arguments.first().isBody)

            assertEquals(
                "body content\nbody **content**",
                arguments.first().value.unwrappedValue,
            )
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(1, arguments.size)
            assertTrue(arguments.first().isBody)
            assertEquals(
                "  body content\nbody content",
                arguments.first().value.unwrappedValue,
            )
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(1, arguments.size)
            assertTrue(arguments.first().isBody)
            assertEquals(
                "body content\n\nbody content\n\nbody content",
                arguments.first().value.unwrappedValue,
            )
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(4, arguments.size)

            val args = arguments.iterator()
            with(args.next()) {
                assertEquals("arg1", value.unwrappedValue)
                assertFalse(this.isBody)
            }
            with(args.next()) {
                assertEquals("arg2", value.unwrappedValue)
                assertFalse(this.isBody)
            }
            with(args.next()) {
                assertEquals("arg3", value.unwrappedValue)
                assertFalse(this.isBody)
            }
            with(args.next()) {
                assertTrue(this.isBody)
                assertEquals(
                    "body content\n\n  body content\n\nbody content",
                    value.unwrappedValue,
                )
            }
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(2, arguments.size)

            val args = arguments.iterator()
            with(args.next()) {
                assertEquals("{{arg1}}", value.unwrappedValue)
                assertFalse(this.isBody)
            }
            with(args.next()) {
                assertEquals("{arg2}", value.unwrappedValue)
                assertFalse(this.isBody)
            }
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(2, arguments.size)

            val args = arguments.iterator()
            with(args.next()) {
                assertEquals("arg{1}", value.unwrappedValue)
                assertFalse(this.isBody)
            }
            with(args.next()) {
                assertEquals("arg2", value.unwrappedValue)
                assertFalse(this.isBody)
            }
        }

        with(nodes.next()) {
            assertEquals("function", name)
            assertEquals(3, arguments.size)

            val args = arguments.iterator()
            with(args.next()) {
                assertEquals("arg{1} arg", value.unwrappedValue)
                assertFalse(this.isBody)
            }
            with(args.next()) {
                assertEquals("{ arg2 }", value.unwrappedValue)
                assertFalse(this.isBody)
            }
            with(args.next()) {
                assertTrue(this.isBody)
                assertEquals(
                    "body content",
                    value.unwrappedValue,
                )
            }
        }
    }
}
