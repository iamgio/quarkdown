package com.quarkdown.core

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.CriticalContent
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.ReferenceDefinitionFootnote
import com.quarkdown.core.ast.base.inline.ReferenceFootnote
import com.quarkdown.core.ast.base.inline.ReferenceImage
import com.quarkdown.core.ast.base.inline.ReferenceLink
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.size.cm
import com.quarkdown.core.document.size.inch
import com.quarkdown.core.document.size.mm
import com.quarkdown.core.document.size.percent
import com.quarkdown.core.document.size.px
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.util.toPlainText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        val parser = flavor.parserFactory.newParser(MutableContext(flavor))
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
    fun subdocumentLink() {
        val nodes = inlineIterator<SubdocumentLink>(readSource("/parsing/inline/subdocumentlink.md"))

        repeat(2) {
            with(nodes.next()) {
                assertEquals("path/to/file.qd", url)
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
            assertNodeEquals(Text("ref"), reference.first())
        }

        repeat(2) {
            with(nodes.next()) {
                with(label.first()) {
                    assertIs<Text>(this)
                    assertEquals("ref", text)
                }
                assertNodeEquals(Text("ref"), reference.first())
            }
        }
    }

    @Test
    fun referenceFootnote() {
        val nodes = inlineIterator<ReferenceFootnote>(readSource("/parsing/inline/reffootnote.md"))

        assertEquals("label", nodes.next().label)
        assertEquals("1", nodes.next().label)
        assertNodeEquals(Text("[^2]"), nodes.next().fallback())
    }

    @Test
    fun `all-in-one reference footnote`() {
        val nodes = inlineIterator<ReferenceDefinitionFootnote>(readSource("/parsing/inline/reffootnote-all-in-one.md"))

        with(nodes.next()) {
            assertEquals("abc", label)
            assertEquals("this is a definition!", definition.toPlainText())
        }
        with(nodes.next()) {
            assertTrue(
                label.length ==
                    java.util.UUID
                        .randomUUID()
                        .toString()
                        .length,
            )
            assertEquals("this is an anonymous definition!", definition.toPlainText())
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
            assertNull(referenceId)
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
                assertNull(referenceId)
            }
        }

        with(nodes.next()) {
            with(link.label.first()) {
                assertIs<Text>(this)
                assertEquals("foo", text)
            }
            assertEquals("/img", link.url)
            assertEquals(link.title, "Title")

            assertEquals(150.px, width)
            assertEquals(100.px, height)
        }

        with(nodes.next()) {
            assertEquals(150.px, width)
            assertNull(height)
        }

        with(nodes.next()) {
            assertNull(width)
            assertEquals(100.px, height)
        }

        with(nodes.next()) {
            assertNull(width)
            assertNull(height)
        }

        with(nodes.next()) {
            assertEquals(140.px, width)
            assertNull(height)
        }

        with(nodes.next()) {
            assertEquals(2.0.cm, width)
            assertEquals(4.2.inch, height)
        }

        with(nodes.next()) {
            assertEquals(20.0.mm, width)
            assertEquals(3.0.cm, height)
        }

        with(nodes.next()) {
            assertEquals(2.px, width)
            assertEquals(3.px, height)
        }

        repeat(3) {
            with(nodes.next()) {
                assertEquals(50.percent, width)
                assertEquals(5.percent, height)
            }
        }

        with(nodes.next()) {
            assertEquals(70.percent, width)
            assertNull(height)
        }

        with(nodes.next()) {
            assertNull(width)
            assertNull(height)
            assertNull(link.title)
            assertEquals("custom-id", referenceId)
        }

        with(nodes.next()) {
            assertNotNull(width)
            assertNotNull(height)
            assertNotNull(link.title)
            assertEquals("custom-id", referenceId)
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
            assertNodeEquals(Text("ref"), link.reference.first())

            assertNull(width)
            assertNull(height)
            assertNull(referenceId)
        }

        repeat(2) {
            with(nodes.next()) {
                with(link.label.first()) {
                    assertIs<Text>(this)
                    assertEquals("ref", text)
                }
                assertNodeEquals(Text("ref"), link.reference.first())

                assertNull(width)
                assertNull(height)
            }
        }

        with(nodes.next()) {
            with(link.label.first()) {
                assertIs<Text>(this)
                assertEquals("ref", text)
            }
            assertNodeEquals(Text("ref"), link.reference.first())

            assertEquals(150.px, width)
            assertEquals(100.px, height)
        }

        with(nodes.next()) {
            with(link.label.first()) {
                assertIs<Text>(this)
                assertEquals("ref", text)
            }
            assertNodeEquals(Text("ref"), link.reference.first())

            assertEquals(150.px, width)
            assertNull(height)
        }

        with(nodes.next()) {
            assertNull(width)
            assertNull(height)
            assertEquals("custom-id", referenceId)
        }

        with(nodes.next()) {
            assertNotNull(width)
            assertNotNull(height)
            assertEquals("custom-id", referenceId)
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

        // Color content.
        with(nodes.next()) {
            assertEquals("#FF00FF", text)
            assertEquals(CodeSpan.ColorContent(Color(255, 0, 255)), content)
        }
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

        repeat(7) {
            assertEquals("Math expression", nodes.next().expression)
        }
        assertEquals("Math \$expression", nodes.next().expression)
    }
}
