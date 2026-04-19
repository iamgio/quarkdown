package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.PageBreak
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.wrappedAsValue

/**
 * `Primitives` stdlib module exporter.
 * This module handles wrappers of primitive Markdown nodes with more granular control.
 */
val Primitives: QuarkdownModule =
    moduleOf(
        ::heading,
        ::image,
        ::pageBreak,
        ::figure,
    )

/**
 * Creates a heading with fine-grained control over its behavior.
 *
 * Unlike standard Markdown headings (`#`, `##`, etc.), this function allows explicit control
 * over numbering, page breaks, table of contents indexing, and custom identifiers.
 *
 * Example:
 * ```markdown
 * .heading {My heading} depth:{2} numbered:{no}
 * ```
 *
 * @param content inline content of the heading
 * @param depth importance level of the heading (1 for H1, 6 for H6). For 0-depth, see [marker] instead
 * @param customId optional custom identifier for cross-referencing. If unset, the ID is automatically generated
 * @param canTrackLocation whether the heading **can** be numbered and has its position tracked in the document hierarchy.
 *                         Actual numbering depends on [numbering].
 * @param includeInTableOfContents whether the heading should appear in the table of contents and navigation sidebar.
 *                                 Can be used independently from [canTrackLocation].
 * @param canBreakPage whether the heading triggers an automatic page break
 * @return a wrapped [Heading] node
 * @throws IllegalArgumentException if [depth] is not in the 1-6 range
 */
fun heading(
    content: InlineMarkdownContent,
    @LikelyNamed depth: Int,
    @Name("ref") customId: String? = null,
    @Name("numbered") canTrackLocation: Boolean = true,
    @Name("indexed") includeInTableOfContents: Boolean = true,
    @Name("breakpage") canBreakPage: Boolean = true,
): NodeValue {
    require(depth in Heading.MIN_DEPTH..Heading.MAX_DEPTH) {
        "Heading depth must be between ${Heading.MIN_DEPTH} and ${Heading.MAX_DEPTH}, but got $depth."
    }

    return Heading(
        depth = depth,
        text = content.children,
        customId = customId,
        canBreakPage = canBreakPage,
        canTrackLocation = canTrackLocation,
        excludeFromTableOfContents = !includeInTableOfContents,
    ).let(::NodeValue)
}

/**
 * Creates an image with fine-grained control over its properties,
 * compared to the standard Markdown image syntax (`![alt text](url "title")`).
 *
 * Example:
 * ```markdown
 * .image {image.png} label:{An image}
 *
 * .image {photo.jpg} label:{A photo} title:{A beautiful photo} width:{200px}
 * ```
 *
 * @param url path or URL to the image
 * @param label inline content used as the image's alt text
 * @param title optional inline content used as both the tooltip and figure caption
 * @param width optional width constraint for the image
 * @param height optional height constraint for the image
 * @param referenceId optional ID for cross-referencing via [reference]
 * @param wrapInFigure whether to wrap the image in a [Figure] block
 * @return a wrapped [Figure] or [Image] node, depending on [wrapInFigure]
 */
fun image(
    @Injected context: Context,
    url: String,
    @LikelyNamed label: InlineMarkdownContent,
    @LikelyNamed title: InlineMarkdownContent? = null,
    @LikelyNamed width: Size? = null,
    @LikelyNamed height: Size? = null,
    @Name("ref") referenceId: String? = null,
    @Name("figure") wrapInFigure: Boolean = true,
): NodeValue {
    val image =
        Image(
            Link(
                label = label.children,
                url = url,
                title = title?.children,
                fileSystem = context.fileSystem,
            ),
            width = width,
            height = height,
            referenceId = referenceId,
        )
    return if (wrapInFigure) {
        Figure(
            child = image,
            caption = title?.children,
            referenceId = referenceId,
        )
    } else {
        image
    }.wrappedAsValue()
}

/**
 * Creates a page break. In standard Quarkdown, this is also achievable with `<<<` on its own line,
 * but this function provides a more explicit way to insert a page break,
 * and can be used within other function calls.
 *
 * Example:
 * ```markdown
 * .pagebreak
 * ```
 *
 * @return a [PageBreak] node
 */
@Name("pagebreak")
fun pageBreak() = PageBreak().wrappedAsValue()

/**
 * Inserts content in a figure block, with an optional caption.
 *
 * If either [caption] or [referenceId] is set, the figure will be numbered according to the `figures` [numbering] rule.
 *
 * @param caption optional inline caption of the figure
 * @param referenceId optional ID for cross-referencing via [reference]
 * @param body content of the figure
 * @return the new [Figure] node
 */
fun figure(
    @LikelyNamed caption: InlineMarkdownContent? = null,
    @Name("ref") referenceId: String? = null,
    @LikelyBody body: MarkdownContent,
): NodeValue =
    Figure(
        body,
        caption = caption?.children,
        referenceId = referenceId,
    ).wrappedAsValue()
