@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.BaseListItem
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.base.block.HorizontalRule
import eu.iamgio.quarkdown.ast.base.block.Html
import eu.iamgio.quarkdown.ast.base.block.LinkDefinition
import eu.iamgio.quarkdown.ast.base.block.ListItem
import eu.iamgio.quarkdown.ast.base.block.OrderedList
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.block.Table
import eu.iamgio.quarkdown.ast.base.block.TaskListItem
import eu.iamgio.quarkdown.ast.base.block.UnorderedList
import eu.iamgio.quarkdown.ast.base.inline.CodeSpan
import eu.iamgio.quarkdown.ast.base.inline.Comment
import eu.iamgio.quarkdown.ast.base.inline.CriticalContent
import eu.iamgio.quarkdown.ast.base.inline.Emphasis
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.ast.base.inline.LineBreak
import eu.iamgio.quarkdown.ast.base.inline.Link
import eu.iamgio.quarkdown.ast.base.inline.ReferenceImage
import eu.iamgio.quarkdown.ast.base.inline.ReferenceLink
import eu.iamgio.quarkdown.ast.base.inline.Strikethrough
import eu.iamgio.quarkdown.ast.base.inline.Strong
import eu.iamgio.quarkdown.ast.base.inline.StrongEmphasis
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.quarkdown.block.Aligned
import eu.iamgio.quarkdown.ast.quarkdown.block.Box
import eu.iamgio.quarkdown.ast.quarkdown.block.Clipped
import eu.iamgio.quarkdown.ast.quarkdown.block.Math
import eu.iamgio.quarkdown.ast.quarkdown.block.PageBreak
import eu.iamgio.quarkdown.ast.quarkdown.inline.MathSpan
import eu.iamgio.quarkdown.context.BaseContext
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.MutableContextOptions
import eu.iamgio.quarkdown.document.size.cm
import eu.iamgio.quarkdown.document.size.inch
import eu.iamgio.quarkdown.flavor.base.BaseMarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.misc.Color
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import eu.iamgio.quarkdown.pipeline.Pipelines
import eu.iamgio.quarkdown.rendering.NodeRenderer
import eu.iamgio.quarkdown.util.normalizeLineSeparators
import eu.iamgio.quarkdown.util.toPlainText
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * HTML node rendering tests.
 */
class HtmlNodeRendererTest {
    private fun readParts(path: String) =
        readSource("/rendering/$path")
            .normalizeLineSeparators()
            .split("\n---\n")
            .map { it.trim() }
            .iterator()

    private fun renderer(context: Context = MutableContext(QuarkdownFlavor)): NodeRenderer {
        if (context.attachedPipeline == null) {
            // Attach a mock pipeline to the context, allowing to render pretty output
            // (since its value is retrieved from the attached pipeline)
            Pipelines.attach(
                context,
                MutableContext(context.flavor).attachMockPipeline(PipelineOptions(prettyOutput = true)),
            )
        }

        return context.flavor.rendererFactory.html(context).nodeRenderer
    }

    private fun Node.render(context: Context = MutableContext(QuarkdownFlavor)) = this.accept(renderer(context))

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

        val context = BaseContext(attributes, QuarkdownFlavor)

        val fallback = { Emphasis(listOf(Text("fallback"))) }

        assertEquals(
            out.next(),
            ReferenceLink(label, label, fallback).render(context),
        )
        assertEquals(
            out.next(),
            ReferenceLink(listOf(Text("label")), label, fallback).render(context),
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
                width = null,
                height = null,
            ).render(),
        )
        assertEquals(
            out.next(),
            Image(
                Link(label = listOf(), url = "/url", title = "Title"),
                width = null,
                height = null,
            ).render(MutableContext(BaseMarkdownFlavor)),
        ) // The figcaption appears only with the Quarkdown rendering!
        assertEquals(
            out.next(),
            Image(
                Link(label = listOf(), url = "/url", title = "Title"),
                width = null,
                height = null,
            ).render(),
        )
        assertEquals(
            out.next(),
            Image(
                Link(label = listOf(Text("Foo bar")), url = "/url", title = "Title"),
                width = null,
                height = null,
            ).render(),
        )
        assertEquals(
            out.next(),
            Image(
                Link(label = listOf(Strong(listOf(Text("Foo"))), CodeSpan(" bar")), url = "/url", title = "Title"),
                width = null,
                height = null,
            ).render(),
        )
        assertEquals(
            out.next(),
            Image(
                Link(label = listOf(Text("Foo bar")), url = "/url", title = "Title"),
                width = 150,
                height = 100,
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

        val context = BaseContext(attributes, QuarkdownFlavor)

        val fallback = { Emphasis(listOf(Text("fallback"))) }

        assertEquals(
            out.next(),
            ReferenceImage(
                ReferenceLink(label, label, fallback),
                width = null,
                height = null,
            ).render(context),
        )
        assertEquals(
            out.next(),
            ReferenceImage(
                ReferenceLink(
                    listOf(Text("label")),
                    label,
                    fallback,
                ),
                width = null,
                height = null,
            ).render(context),
        )
        assertEquals(
            out.next(),
            ReferenceImage(
                ReferenceLink(
                    listOf(Text("label")),
                    label,
                    fallback,
                ),
                width = 150,
                height = 100,
            ).render(context),
        )
        assertEquals(
            out.next(),
            ReferenceImage(
                ReferenceLink(
                    listOf(Text("label")),
                    label,
                    fallback,
                ),
                width = null,
                height = null,
            ).render(),
        )
    }

    @Test
    fun text() {
        assertEquals("Foo bar", Text("Foo bar").render())
    }

    @Test
    fun codeSpan() {
        val out = readParts("inline/codespan.html")

        // The Quarkdown rendering wraps the content in a span which allows additional content, such as color.
        val base = MutableContext(BaseMarkdownFlavor)
        val quarkdown = MutableContext(QuarkdownFlavor)

        val spanWithColor =
            CodeSpan(
                "#FFFF00",
                CodeSpan.ColorContent(Color.fromHex("#FFFF00")!!),
            )

        assertEquals(out.next(), CodeSpan("Foo bar").render(base))
        assertEquals(out.next(), CodeSpan("<a href=\"#\">").render(base))
        assertEquals(out.next(), spanWithColor.render(quarkdown))
        assertEquals(out.next(), spanWithColor.render(base))
        assertEquals(out.next(), CodeSpan("Foo bar").render(quarkdown))
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

        assertEquals(out.next(), Code("Code", language = null, showLineNumbers = true).render())
        assertEquals(out.next(), Code("Code", language = null, showLineNumbers = false).render())
        assertEquals(out.next(), Code("class Point {\n    ...\n}", language = null, showLineNumbers = true).render())
        assertEquals(out.next(), Code("class Point {\n    ...\n}", language = "java", showLineNumbers = false).render())
        assertEquals(out.next(), Code("<a href=\"#\">", language = "html", showLineNumbers = true).render())
        assertEquals(
            out.next(),
            Code("class Point {\n    ...\n}", language = "java", focusedLines = Range(1, 2)).render(),
        )
        assertEquals(
            out.next(),
            Code("class Point {\n    ...\n}", language = "java", focusedLines = Range(2, null)).render(),
        )
        assertEquals(
            out.next(),
            Code("class Point {\n    ...\n}", language = "java", focusedLines = Range(null, 1)).render(),
        )
    }

    @Test
    fun horizontalRule() {
        assertEquals("<hr />", HorizontalRule().render())
    }

    @Test
    fun pageBreak() {
        assertEquals("<div class=\"page-break\">\n</div>", PageBreak().render())
    }

    @Test
    fun heading() {
        val out = readParts("block/heading.html")

        // No automatic ID, no automatic page break.
        val noIdNoPageBreak =
            MutableContext(
                QuarkdownFlavor,
                options = MutableContextOptions(autoPageBreakHeadingDepth = 0, enableAutomaticIdentifiers = false),
            )

        assertEquals(out.next(), Heading(1, listOf(Text("Foo bar"))).render(noIdNoPageBreak))
        assertEquals(out.next(), Heading(2, listOf(Text("Foo bar"))).render(noIdNoPageBreak))
        assertEquals(out.next(), Heading(3, listOf(Text("Foo bar")), customId = "my-id").render(noIdNoPageBreak))
        assertEquals(out.next(), Heading(3, listOf(Strong(listOf(Text("Foo bar"))))).render(noIdNoPageBreak))
        assertEquals(out.next(), Heading(4, listOf(Text("Foo"), Emphasis(listOf(Text("bar"))))).render(noIdNoPageBreak))

        // Automatic ID, no automatic page break.
        val idNoPageBreak =
            MutableContext(
                QuarkdownFlavor,
                options = MutableContextOptions(autoPageBreakHeadingDepth = 0),
            )

        assertEquals(out.next(), Heading(1, listOf(Text("Foo bar"))).render(idNoPageBreak))
        assertEquals(out.next(), Heading(1, listOf(Text("Foo bar")), customId = "custom-id").render(idNoPageBreak))
        assertEquals(out.next(), Heading(4, listOf(Text("Foo"), Emphasis(listOf(Text("bar"))))).render(idNoPageBreak))

        // Automatic ID, force page break on depth <= 2
        val autoPageBreak =
            MutableContext(QuarkdownFlavor, options = MutableContextOptions(autoPageBreakHeadingDepth = 2))

        assertEquals(out.next(), Heading(1, listOf(Text("Foo bar"))).render(autoPageBreak))
        assertEquals(out.next(), Heading(2, listOf(Text("Foo bar"))).render(autoPageBreak))
        assertEquals(out.next(), Heading(3, listOf(Text("Foo bar"))).render(autoPageBreak))
        assertEquals(out.next(), Heading(4, listOf(Text("Foo bar"))).render(autoPageBreak))
    }

    private fun listItems() =
        listOf(
            BaseListItem(
                children =
                    listOf(
                        Paragraph(listOf(Text("A1"))),
                        HorizontalRule(),
                        Paragraph(listOf(Text("A2"))),
                    ),
            ),
            BaseListItem(
                children =
                    listOf(
                        Paragraph(listOf(Text("B1"))),
                        HorizontalRule(),
                        Paragraph(listOf(Text("B2"))),
                    ),
            ),
            BaseListItem(
                children =
                    listOf(
                        Paragraph(listOf(Text("C1"))),
                        HorizontalRule(),
                        Paragraph(listOf(Text("C2"))),
                    ),
            ),
            TaskListItem(
                isChecked = true,
                listOf(
                    Paragraph(listOf(Text("D1"))),
                    HorizontalRule(),
                    Paragraph(listOf(Text("D2"))),
                ),
            ),
        )

    @Test
    fun orderedList() {
        val out = readParts("block/orderedlist.html")

        assertEquals(out.next(), OrderedList(startIndex = 1, isLoose = false, emptyList()).render())

        assertEquals(
            out.next(),
            OrderedList(
                startIndex = 1,
                isLoose = true,
                listItems(),
            ).render(),
        )

        assertEquals(
            out.next(),
            OrderedList(
                startIndex = 12,
                isLoose = true,
                listItems(),
            ).render(),
        )

        assertEquals(
            out.next(),
            OrderedList(
                startIndex = 1,
                isLoose = false,
                listItems(),
            )
                .also { list -> list.children.asSequence().filterIsInstance<ListItem>().forEach { it.owner = list } }
                .render(),
        )
    }

    @Test
    fun unorderedList() {
        val out = readParts("block/unorderedlist.html")

        assertEquals(out.next(), UnorderedList(isLoose = false, emptyList()).render())

        assertEquals(
            out.next(),
            UnorderedList(
                isLoose = true,
                listItems(),
            ).render(),
        )

        assertEquals(
            out.next(),
            UnorderedList(
                isLoose = false,
                listItems(),
            )
                .also { list -> list.children.asSequence().filterIsInstance<ListItem>().forEach { it.owner = list } }
                .render(),
        )
    }

    @Test
    fun html() {
        assertEquals("<p><strong>test</p></strong>", Html("<p><strong>test</p></strong>").render())
    }

    @Test
    fun paragraph() {
        val out = readParts("block/paragraph.html")

        assertEquals(out.next(), Paragraph(listOf(Text("Foo bar"))).render())
        assertEquals(out.next(), Paragraph(listOf(Text("Foo"), LineBreak(), Text("bar"))).render())
    }

    @Test
    fun blockquote() {
        val out = readParts("block/blockquote.html")

        assertEquals(
            out.next(),
            BlockQuote(
                listOf(
                    Paragraph(listOf(Text("Foo bar"))),
                    Paragraph(listOf(Text("Baz bim"))),
                ),
            ).render(),
        )
    }

    @Test
    fun table() {
        val out = readParts("block/table.html")

        assertEquals(
            out.next(),
            Table(
                listOf(
                    Table.Column(
                        Table.Alignment.NONE,
                        header = Table.Cell(listOf(Text("A"))),
                        cells =
                            listOf(
                                Table.Cell(listOf(Text("C"))),
                                Table.Cell(listOf(Text("E"))),
                            ),
                    ),
                    Table.Column(
                        Table.Alignment.NONE,
                        header = Table.Cell(listOf(Text("B"))),
                        cells =
                            listOf(
                                Table.Cell(listOf(Text("D"))),
                                Table.Cell(listOf(Text("F"))),
                            ),
                    ),
                ),
            ).render(),
        )

        assertEquals(
            out.next(),
            Table(
                listOf(
                    Table.Column(
                        Table.Alignment.CENTER,
                        header = Table.Cell(listOf(Text("A"))),
                        cells =
                            listOf(
                                Table.Cell(listOf(Text("C"))),
                                Table.Cell(listOf(Text("E"))),
                            ),
                    ),
                    Table.Column(
                        Table.Alignment.RIGHT,
                        header = Table.Cell(listOf(Text("B"))),
                        cells =
                            listOf(
                                Table.Cell(listOf(Text("D"))),
                                Table.Cell(listOf(Strong(listOf(Text("F"))))),
                            ),
                    ),
                ),
            ).render(),
        )
    }

    // Quarkdown

    @Test
    fun mathBlock() {
        assertEquals("__QD_BLOCK_MATH__\$some expression\$__QD_BLOCK_MATH__", Math("some expression").render())
        assertEquals(
            "__QD_BLOCK_MATH__\$\\lim_{x\\to\\infty}x\$__QD_BLOCK_MATH__",
            Math("\\lim_{x\\to\\infty}x").render(),
        )
    }

    @Test
    fun mathSpan() {
        assertEquals("__QD_INLINE_MATH__\$some expression\$__QD_INLINE_MATH__", MathSpan("some expression").render())
        assertEquals(
            "__QD_INLINE_MATH__\$\\lim_{x\\to\\infty}x\$__QD_INLINE_MATH__",
            MathSpan("\\lim_{x\\to\\infty}x").render(),
        )
    }

    @Test
    fun aligned() {
        val out = readParts("quarkdown/aligned.html")
        val paragraph = Paragraph(listOf(Text("Foo"), LineBreak(), Text("bar")))

        assertEquals(out.next(), Aligned(Aligned.Alignment.LEFT, listOf(paragraph)).render())
        assertEquals(out.next(), Aligned(Aligned.Alignment.CENTER, listOf(paragraph)).render())
        assertEquals(out.next(), Aligned(Aligned.Alignment.RIGHT, listOf(paragraph)).render())
    }

    @Test
    fun clipped() {
        val out = readParts("quarkdown/clipped.html")
        val paragraph = Paragraph(listOf(Text("Foo"), LineBreak(), Text("bar")))

        assertEquals(out.next(), Clipped(Clipped.Clip.CIRCLE, listOf(paragraph)).render())
    }

    @Test
    fun box() {
        val out = readParts("quarkdown/box.html")
        val paragraph = Paragraph(listOf(Text("Foo"), LineBreak(), Text("bar")))

        assertEquals(
            out.next(),
            Box(
                title = listOf(Text("Title")),
                type = Box.Type.CALLOUT,
                padding = null,
                backgroundColor = null,
                foregroundColor = null,
                listOf(paragraph),
            ).render(),
        )

        assertEquals(
            out.next(),
            Box(
                title = listOf(Text("Title"), Emphasis(listOf(Text("Title")))),
                type = Box.Type.CALLOUT,
                padding = null,
                backgroundColor = null,
                foregroundColor = null,
                listOf(paragraph),
            ).render(),
        )

        assertEquals(
            out.next(),
            Box(
                title = null,
                type = Box.Type.ERROR,
                padding = 4.0.cm,
                backgroundColor = null,
                foregroundColor = null,
                listOf(paragraph),
            ).render(),
        )

        assertEquals(
            out.next(),
            Box(
                title = listOf(Text("Title")),
                type = Box.Type.ERROR,
                padding = 3.0.inch,
                backgroundColor = Color(255, 0, 120),
                foregroundColor = Color(0, 10, 25),
                listOf(paragraph),
            ).render(),
        )
    }
}
