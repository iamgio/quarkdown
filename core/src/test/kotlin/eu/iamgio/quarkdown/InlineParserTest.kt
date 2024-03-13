package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.PlainText
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
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

    @Test
    fun link() {
        val nodes = inlineIterator<Link>(readSource("/parsing/inline/link.md"))

        with(nodes.next()) {
            with(label.first()) {
                assertIs<PlainText>(this)
                assertEquals("foo", text)
            }
            assertEquals("https://google.com", url)
            assertNull(title)
        }

        repeat(2) {
            with(nodes.next()) {
                with(label.first()) {
                    assertIs<PlainText>(this)
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
                    assertIs<PlainText>(this)
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
                assertIs<PlainText>(this)
                assertEquals("label", text)
            }
            assertEquals("ref", reference)
        }

        repeat(2) {
            with(nodes.next()) {
                with(label.first()) {
                    assertIs<PlainText>(this)
                    assertEquals("ref", text)
                }
                assertEquals("ref", reference)
            }
        }
    }

    @Test
    fun strong() {
        val nodes = inlineIterator<Strong>(readSource("/parsing/inline/strong.md"))

        with(nodes.next()) {
            with(children.first()) {
                assertIs<PlainText>(this)
                assertEquals("foo", text)
            }
        }

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
            with(content.next()) {
                assertIs<PlainText>(this)
                assertEquals("baz", text)
            }
        }

        with(nodes.next()) {
            with(children.first()) {
                assertIs<PlainText>(this)
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
                assertIs<PlainText>(this)
                assertEquals("foo", text)
            }
        }

        with(nodes.next()) {
            val content = children.iterator()
            with(content.next()) {
                assertIs<PlainText>(this)
                assertEquals("foo", text)
            }
            with(content.next()) {
                assertIs<Strong>(this)
                assertIs<PlainText>(children.first())
                assertEquals("bar", (children.first() as PlainText).text)
            }
            with(content.next()) {
                assertIs<PlainText>(this)
                assertEquals("baz", text)
            }
        }

        with(nodes.next()) {
            with(children.first()) {
                assertIs<PlainText>(this)
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
                assertIs<PlainText>(this)
                assertEquals("foo", text)
            }
        }

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
            with(content.next()) {
                assertIs<PlainText>(this)
                assertEquals("baz", text)
            }
        }

        assertFalse(nodes.hasNext())
    }
}
