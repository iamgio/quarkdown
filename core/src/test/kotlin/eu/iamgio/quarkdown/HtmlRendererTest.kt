package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.AstAttributes
import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.CodeSpan
import eu.iamgio.quarkdown.ast.Comment
import eu.iamgio.quarkdown.ast.CriticalContent
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.HorizontalRule
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.LineBreak
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.ast.Strikethrough
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.rendering.RendererFactory
import eu.iamgio.quarkdown.util.toPlainText
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

    private fun renderer(attributes: AstAttributes = MutableAstAttributes()) = RendererFactory.html(attributes)

    private fun Node.render(attributes: AstAttributes = MutableAstAttributes()) = this.accept(renderer(attributes))

    // Inline

    @Test
    fun comment() {
        assertEquals("", Comment().render())
    }

    @Test
    fun lineBreak() {
        assertEquals("<br />", LineBreak().render())
    }

    @Test
    fun criticalContent() {
        assertEquals("&amp;", CriticalContent("&").render())
        assertEquals("&gt;", CriticalContent(">").render())
        assertEquals("~", CriticalContent("~").render())
    }

    @Test
    fun link() {
        val out = readParts("inline/link.html")

        assertEquals(
            out.next(),
            Link(label = listOf(Text("Foo bar")), url = "https://google.com", title = null).render(),
        )
        assertEquals(
            out.next(),
            Link(label = listOf(Strong(listOf(Text("Foo bar")))), url = "/url", title = null).render(),
        )
        assertEquals(
            out.next(),
            Link(label = listOf(Text("Foo bar baz")), url = "url", title = "Title").render(),
        )
    }

    @Test
    fun referenceLink() {
        val out = readParts("inline/reflink.html")

        val label = listOf(Strong(listOf(Text("Foo"))))

        val attributes =
            MutableAstAttributes(
                linkDefinitions =
                    mutableListOf(
                        LinkDefinition(
                            label,
                            url = "/url",
                            title = "Title",
                        ),
                    ),
            )

        val fallback = { Emphasis(listOf(Text("fallback"))) }

        assertEquals(
            out.next(),
            ReferenceLink(label, label, fallback).render(attributes),
        )
        assertEquals(
            out.next(),
            ReferenceLink(listOf(Text("label")), label, fallback).render(attributes),
        )
        assertEquals(
            out.next(),
            ReferenceLink(listOf(Text("label")), label, fallback).render(),
        )
    }

    @Test
    fun image() {
        val out = readParts("inline/image.html")

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
        )
        assertEquals(
            out.next(),
            Image(
                Link(label = listOf(Text("Foo bar")), url = "/url", title = "Title"),
            ).render(),
        )
        assertEquals(
            out.next(),
            Image(
                Link(label = listOf(Strong(listOf(Text("Foo"))), CodeSpan(" bar")), url = "/url", title = "Title"),
            ).render(),
        )
    }

    @Test
    fun referenceImage() {
        val out = readParts("inline/refimage.html")

        val label = listOf(Text("Foo"))

        val attributes =
            MutableAstAttributes(
                linkDefinitions =
                    mutableListOf(
                        LinkDefinition(
                            label,
                            url = "/url",
                            title = "Title",
                        ),
                    ),
            )

        val fallback = { Emphasis(listOf(Text("fallback"))) }

        assertEquals(
            out.next(),
            ReferenceImage(ReferenceLink(label, label, fallback)).render(attributes),
        )
        assertEquals(
            out.next(),
            ReferenceImage(ReferenceLink(listOf(Text("label")), label, fallback)).render(attributes),
        )
        assertEquals(
            out.next(),
            ReferenceImage(ReferenceLink(listOf(Text("label")), label, fallback)).render(),
        )
    }

    @Test
    fun text() {
    }

    @Test
    fun codeSpan() {
        val out = readParts("inline/codespan.html")

        assertEquals(out.next(), CodeSpan("Foo bar").render())
    }

    @Test
    fun emphasis() {
        val out = readParts("inline/emphasis.html")

        assertEquals(out.next(), Emphasis(listOf(Text("Foo bar"))).render())
        assertEquals(out.next(), Emphasis(listOf(Emphasis(listOf(Text("Foo bar"))))).render())
    }

    @Test
    fun strong() {
        val out = readParts("inline/strong.html")

        assertEquals(out.next(), Strong(listOf(Text("Foo bar"))).render())
        assertEquals(out.next(), Strong(listOf(Strong(listOf(Text("Foo bar"))))).render())
    }

    @Test
    fun strongEmphasis() {
        val out = readParts("inline/strongemphasis.html")

        assertEquals(out.next(), StrongEmphasis(listOf(Text("Foo bar"))).render())
        assertEquals(out.next(), StrongEmphasis(listOf(StrongEmphasis(listOf(Text("Foo bar"))))).render())
    }

    @Test
    fun strikethrough() {
        val out = readParts("inline/strikethrough.html")

        assertEquals(out.next(), Strikethrough(listOf(Text("Foo bar"))).render())
        assertEquals(out.next(), Strikethrough(listOf(Strong(listOf(Text("Foo bar"))))).render())
    }

    @Test
    fun plainTextConversion() {
        val inline: InlineContent =
            listOf(
                Text("abc"),
                Strong(
                    listOf(
                        Emphasis(
                            listOf(
                                Text("def"),
                                CodeSpan("ghi"),
                            ),
                        ),
                        CodeSpan("jkl"),
                    ),
                ),
                Text("mno"),
                CriticalContent("&"),
            )

        assertEquals("abcdefghijklmno&", inline.toPlainText())
        // Critical content is rendered differently
        assertEquals("abcdefghijklmno&amp;", inline.toPlainText(renderer()))
    }

    // Block

    @Test
    fun code() {
        val out = readParts("block/code.html")

        assertEquals(out.next(), Code("Code", language = null).render())
        assertEquals(out.next(), Code("class Point {\n    ...\n}", language = null).render())
        assertEquals(out.next(), Code("class Point {\n    ...\n}", language = "java").render())
    }

    @Test
    fun horizontalRule() {
        assertEquals("<hr />", HorizontalRule().render())
    }

    @Test
    fun paragraph() {
        val out = readParts("block/paragraph.html")

        assertEquals(out.next(), Paragraph(listOf(Text("Foo bar"))).render())
        assertEquals(out.next(), Paragraph(listOf(Text("Foo"), LineBreak(), Text("bar"))).render())
    }
}
