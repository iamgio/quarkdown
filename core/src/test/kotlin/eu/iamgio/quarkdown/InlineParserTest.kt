package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.CodeSpan
import eu.iamgio.quarkdown.ast.CriticalContent
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.MathSpan
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.ast.Strikethrough
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

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
        val parser = flavor.parserFactory.newParser(MutableContext())
        return nodesIterator(lexer, parser, assertType)
    }

    @Test
    fun escape() {
        // EscapeToken is parsed into PlainText.
        val nodes = inlineIterator<Text>(readSource("/parsing/inline/escape.md"))

        assertEquals("#", nodes.next().text)
        assertEquals("!", nodes.next().text)
        assertEquals(".", nodes.next().text)
        assertEquals(",", nodes.next().text)
        assertEquals("[", nodes.next().text)
        assertEquals("]", nodes.next().text)
    }

    @Test
    fun entity() {
        val nodes = inlineIterator<CriticalContent>(readSource("/parsing/inline/entity.md"))

        // Decimal
        assertEquals(35.toChar().toString(), nodes.next().text)
        assertEquals(1234.toChar().toString(), nodes.next().text)
        assertEquals(992.toChar().toString(), nodes.next().text)
        assertEquals(65533.toChar().toString(), nodes.next().text)

        // Hexadecimal
        assertEquals(0x22.toChar().toString(), nodes.next().text)
        assertEquals(0xD06.toChar().toString(), nodes.next().text)
        assertEquals(0xCAB.toChar().toString(), nodes.next().text)

        // HTML
        assertEquals(" ", nodes.next().text)
        assertEquals("&", nodes.next().text)
        assertEquals("©", nodes.next().text)
        assertEquals("Æ", nodes.next().text)
    }

    @Test
    fun link() {
        val nodes = inlineIterator<Link>(readSource("/parsing/inline/link.md"))

        with(nodes.next()) {
            with(label.first()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
            assertEquals("https://google.com", url)
            assertNull(title)
        }

        repeat(2) {
            with(nodes.next()) {
                with(label.first()) {
                    assertIs<Text>(this)
                    assertEquals("foo", text)
                }
                assertEquals("https://google.com", url)
                assertEquals(title, "Title")
            }
        }

        // Autolink (first: diamond, second: URL)
        repeat(2) {
            with(nodes.next()) {
                assertEquals("https://google.com", url)
                with(label.first()) {
                    assertIs<Text>(this)
                    assertEquals(url, text)
                }
                assertNull(title)
            }
        }

        assertFalse(nodes.hasNext())
    }

    @Test
    fun referenceLink() {
        val nodes = inlineIterator<ReferenceLink>(readSource("/parsing/inline/reflink.md"))

        with(nodes.next()) {
            with(label.first()) {
                assertIs<Text>(this)
                assertEquals("label", text)
            }
            assertEquals(Text("ref"), reference.first())
        }

        repeat(2) {
            with(nodes.next()) {
                with(label.first()) {
                    assertIs<Text>(this)
                    assertEquals("ref", text)
                }
                assertEquals(Text("ref"), reference.first())
            }
        }
    }

    @Test
    fun image() {
        val nodes = inlineIterator<Image>(readSource("/parsing/inline/image.md"))

        with(nodes.next()) {
            with(link.label.first()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
            assertEquals("/img", link.url)
            assertNull(link.title)

            assertNull(width)
            assertNull(height)
        }

        repeat(2) {
            with(nodes.next()) {
                with(link.label.first()) {
                    assertIs<Text>(this)
                    assertEquals("foo", text)
                }
                assertEquals("/img", link.url)
                assertEquals(link.title, "Title")

                assertNull(width)
                assertNull(height)
            }
        }

        with(nodes.next()) {
            with(link.label.first()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
            assertEquals("/img", link.url)
            assertEquals(link.title, "Title")

            assertEquals(150, width)
            assertEquals(100, height)
        }

        with(nodes.next()) {
            assertEquals(150, width)
            assertNull(height)
        }

        with(nodes.next()) {
            assertNull(width)
            assertEquals(100, height)
        }

        with(nodes.next()) {
            assertNull(width)
            assertNull(height)
        }
    }

    @Test
    fun referenceImage() {
        val nodes = inlineIterator<ReferenceImage>(readSource("/parsing/inline/refimage.md"))

        with(nodes.next()) {
            with(link.label.first()) {
                assertIs<Text>(this)
                assertEquals("label", text)
            }
            assertEquals(Text("ref"), link.reference.first())

            assertNull(width)
            assertNull(height)
        }

        repeat(2) {
            with(nodes.next()) {
                with(link.label.first()) {
                    assertIs<Text>(this)
                    assertEquals("ref", text)
                }
                assertEquals(Text("ref"), link.reference.first())

                assertNull(width)
                assertNull(height)
            }
        }

        with(nodes.next()) {
            with(link.label.first()) {
                assertIs<Text>(this)
                assertEquals("ref", text)
            }
            assertEquals(Text("ref"), link.reference.first())

            assertEquals(150, width)
            assertEquals(100, height)
        }

        with(nodes.next()) {
            with(link.label.first()) {
                assertIs<Text>(this)
                assertEquals("ref", text)
            }
            assertEquals(Text("ref"), link.reference.first())

            assertEquals(150, width)
            assertNull(height)
        }
    }

    @Test
    fun codeSpan() {
        val nodes = inlineIterator<CodeSpan>(readSource("/parsing/inline/codespan.md"))

        assertEquals("foo", nodes.next().text)
        assertEquals("foo ` bar", nodes.next().text)
        assertEquals("``", nodes.next().text)
        assertEquals(" `` ", nodes.next().text)
        assertEquals(" a", nodes.next().text)
        assertEquals("b", nodes.next().text)
        assertEquals("foo bar   baz", nodes.next().text)
    }

    @Test
    fun strikethrough() {
        val nodes = inlineIterator<Strikethrough>(readSource("/parsing/inline/strikethrough.md"), assertType = false)

        assertEquals("foo", (nodes.next().children.first() as Text).text)
        assertEquals("Hi", (nodes.next().children.first() as Text).text)
    }

    @Test
    fun strong() {
        val nodes = inlineIterator<Strong>(readSource("/parsing/inline/strong.md"))

        with(nodes.next()) {
            with(children.first()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
        }

        with(nodes.next()) {
            val content = children.iterator()
            with(content.next()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
            with(content.next()) {
                assertIs<Emphasis>(this)
                assertIs<Text>(children.first())
                assertEquals("bar", (children.first() as Text).text)
            }
            with(content.next()) {
                assertIs<Text>(this)
                assertEquals("baz", text)
            }
        }

        with(nodes.next()) {
            with(children.first()) {
                assertIs<Text>(this)
                assertEquals("foo_bar_baz", text)
            }
        }

        /*
        TODO fix for **foo*bar***
        with(nodes.next()) {
            val content = children.iterator()
            with(content.next()) {
                assertIs<PlainText>(this)
                assertEquals("foo", text)
            }
            with(content.next()) {
                assertIs<Emphasis>(this)
                assertIs<PlainText>(children.first())
                assertEquals("bar", (children.first() as PlainText).text)
            }
        }
         */

        assertFalse(nodes.hasNext())
    }

    @Test
    fun emphasis() {
        val nodes = inlineIterator<Emphasis>(readSource("/parsing/inline/emphasis.md"))

        with(nodes.next()) {
            with(children.first()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
        }

        with(nodes.next()) {
            val content = children.iterator()
            with(content.next()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
            with(content.next()) {
                assertIs<Strong>(this)
                assertIs<Text>(children.first())
                assertEquals("bar", (children.first() as Text).text)
            }
            with(content.next()) {
                assertIs<Text>(this)
                assertEquals("baz", text)
            }
        }

        with(nodes.next()) {
            with(children.first()) {
                assertIs<Text>(this)
                assertEquals("foo_bar_baz", text)
            }
        }

        assertFalse(nodes.hasNext())
    }

    @Test
    fun strongEmphasis() {
        val nodes = inlineIterator<StrongEmphasis>(readSource("/parsing/inline/strongemphasis.md"))

        with(nodes.next()) {
            with(children.first()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
        }

        with(nodes.next()) {
            val content = children.iterator()
            with(content.next()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
            with(content.next()) {
                assertIs<Emphasis>(this)
                assertIs<Text>(children.first())
                assertEquals("bar", (children.first() as Text).text)
            }
            with(content.next()) {
                assertIs<Text>(this)
                assertEquals("baz", text)
            }
        }

        assertFalse(nodes.hasNext())
    }

    @Test
    fun mathSpan() {
        val nodes = inlineIterator<MathSpan>(readSource("/parsing/inline/mathspan.md"), assertType = false)

        repeat(3) {
            assertEquals("Math expression", nodes.next().expression)
        }
        assertEquals("Math \$expression", nodes.next().expression)
    }
}
