package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.CodeSpan
import eu.iamgio.quarkdown.ast.Comment
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.LineBreak
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.PlainText
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.rendering.html.HtmlNodeRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * HTML node rendering tests.
 */
class HtmlRendererTest {
    private fun readParts(path: String) =
        readSource("/rendering/$path")
            .split("\n---\n")
            .map { it.trim() }
            .iterator()

    private fun Node.render() = this.accept(HtmlNodeRenderer())

    // Inline

    @Test
    fun comment() {
        assertEquals("", Comment().render())
    }

    @Test
    fun lineBreak() {
        assertEquals("<br>", LineBreak().render())
    }

    @Test
    fun criticalContent() {
    }

    @Test
    fun link() {
        val out = readParts("inline/link.html")

        assertEquals(
            out.next(),
            Link(label = listOf(PlainText("Foo bar")), url = "https://google.com", title = null).render(),
        )
        assertEquals(
            out.next(),
            Link(label = listOf(Strong(listOf(PlainText("Foo bar")))), url = "/url", title = null).render(),
        )
        assertEquals(
            out.next(),
            Link(label = listOf(PlainText("Foo bar baz")), url = "url", title = "Title").render(),
        )
    }

    @Test
    fun referenceLink() {
    }

    @Test
    fun image() {
        val out = readParts("inline/image.html")

        /*
        TODO get correct alt text
        assertEquals(
            out.next(),
            Image(
                Link(label = listOf(), url = "/url", title = null),
            ).render(),
        )
        assertEquals(
            out.next(),
            Image(
                Link(label = listOf(), url = "/url", title = "Title"),
            ).render(),
        )*/
    }

    @Test
    fun referenceImage() {
    }

    @Test
    fun plainText() {
    }

    @Test
    fun codeSpan() {
        val out = readParts("inline/codespan.html")

        assertEquals(out.next(), CodeSpan("Foo bar").render())
    }

    @Test
    fun emphasis() {
        val out = readParts("inline/emphasis.html")

        assertEquals(out.next(), Emphasis(listOf(PlainText("Foo bar"))).render())
        assertEquals(out.next(), Emphasis(listOf(Emphasis(listOf(PlainText("Foo bar"))))).render())
    }

    @Test
    fun strong() {
        val out = readParts("inline/strong.html")

        assertEquals(out.next(), Strong(listOf(PlainText("Foo bar"))).render())
        assertEquals(out.next(), Strong(listOf(Strong(listOf(PlainText("Foo bar"))))).render())
    }

    @Test
    fun strongEmphasis() {
        val out = readParts("inline/strongemphasis.html")

        assertEquals(out.next(), StrongEmphasis(listOf(PlainText("Foo bar"))).render())
        assertEquals(out.next(), StrongEmphasis(listOf(StrongEmphasis(listOf(PlainText("Foo bar"))))).render())
    }
}
