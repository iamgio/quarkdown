@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.PageBreak
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.reflect.annotation.Body
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.data.EvaluableString
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.processor.annotation.Spread

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
 * As a [Heading] primitive, this function can be used in `.extend` to affect all headings in the document:
 *
 * ```markdown
 * .extend {heading} where:{depth: .depth::equals {1}}
 *     content:
 *     .super foreground:{blue}
 *         *.content*
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
@QFunction
fun heading(
    @Body content: InlineMarkdownContent,
    @LikelyNamed depth: Int,
    @Name("ref") customId: String? = null,
    @Name("numbered") canTrackLocation: Boolean = true,
    @Name("indexed") includeInTableOfContents: Boolean = true,
    @Name("breakpage") canBreakPage: Boolean = true,
    @Spread style: StyleOptions = StyleOptions.DEFAULT,
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
        style = style.toNodeStyle(),
    ).let(::NodeValue)
}

/**
 * Creates a paragraph with fine-grained control over its properties.
 *
 * As a [Paragraph] primitive, this function can be used in `.extend` to affect all paragraphs in the document:
 *
 * ```markdown
 * .extend {paragraph}
 *     .super foreground:{gray}
 * ```
 */
@QFunction
fun paragraph(
    @Body content: InlineMarkdownContent,
    @Spread style: StyleOptions = StyleOptions.DEFAULT,
) = Paragraph(
    text = content.children,
    style = style.toNodeStyle(),
).wrappedAsValue()

/**
 * Creates a link.
 *
 * As a [Link] primitive, this function can be used in `.extend` to affect all links in the document,
 * including those introduced by the standard `[text](url)` Markdown syntax:
 *
 * ```markdown
 * .extend {link} where:{url: .url::startswith {https://}}
 *     .super foreground:{blue}
 * ```
 *
 * @param content inline label of the link
 * @param url URL the link points to
 * @param title optional inline title used as the tooltip
 * @return a wrapped [Link] node
 */
@QFunction
fun link(
    @Injected context: Context,
    @Body content: InlineMarkdownContent,
    url: String,
    @LikelyNamed title: InlineMarkdownContent? = null,
    @Spread style: StyleOptions = StyleOptions.DEFAULT,
) = Link(
    label = content.children,
    url = url,
    title = title?.children,
    fileSystem = context.fileSystem,
    style = style.toNodeStyle(),
).wrappedAsValue()

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
 * @param usesMediaStorage whether this image should be registered in the media storage system.
 *                         When disabled, the image URL is used as-is without being copied to the output's media directory.
 *                         Useful for images that should reference a fixed relative path rather than a stored copy
 * @return a wrapped [Figure] or [Image] node, depending on [wrapInFigure]
 */
@QFunction
fun image(
    @Injected context: Context,
    url: String,
    @LikelyNamed label: InlineMarkdownContent,
    @LikelyNamed title: InlineMarkdownContent? = null,
    @LikelyNamed width: Size? = null,
    @LikelyNamed height: Size? = null,
    @Name("ref") referenceId: String? = null,
    @Name("figure") wrapInFigure: Boolean = true,
    @Name("mediastorage") usesMediaStorage: Boolean = true,
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
            usesMediaStorage = usesMediaStorage,
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
 * As a [PageBreak] primitive, this function can be used in `.extend` to affect all page breaks in the document,
 * including those introduced by the `<<<` syntax:
 *
 * ```markdown
 * .extend {pagebreak}
 *     .container border:{1px dashed gray}
 *
 *     .super
 * ```
 *
 * @return a [PageBreak] node
 */
@QFunction
@Name("pagebreak")
fun pageBreak() = PageBreak().wrappedAsValue()

/**
 * Creates a math (TeX) node, either inline or as a block.
 *
 * Example:
 * ```markdown
 * .math {2 + 2}
 *
 * .math {E = mc^2} block:{yes} ref:{einstein}
 * ```
 *
 * As a primitive backing both [Math] blocks and [MathSpan] inlines, this function can be used
 * in `.extend` to affect all math nodes in the document:
 *
 * ```markdown
 * .extend {math} where:{block: .block::not}
 *     .super foreground:{gray}
 * ```
 *
 * @param content TeX expression content
 * @param block whether the node is rendered as a block instead of inline. If unset, it's inferred from whether the source function call is block or inline
 * @param referenceId optional ID for cross-referencing via [reference], only applied to block math
 * @return the new [Math] block node or [MathSpan] inline node, depending on [block]
 */
@QFunction
fun math(
    @Injected call: FunctionCall<*>,
    @Body content: EvaluableString,
    @LikelyNamed block: Boolean? = null,
    @Name("ref") referenceId: String? = null,
    @Spread style: StyleOptions = StyleOptions.DEFAULT,
): NodeValue {
    val isBlock = block ?: call.sourceNode?.isBlock ?: false
    return when {
        isBlock -> {
            Math(
                expression = content.content,
                referenceId = referenceId,
                style = style.toNodeStyle(),
            ).wrappedAsValue()
        }

        else -> {
            MathSpan(
                expression = content.content,
                style = style.toNodeStyle(),
            ).wrappedAsValue()
        }
    }
}

/**
 * Inserts content in a figure block, with an optional caption.
 *
 * If either [caption] or [referenceId] is set, the figure will be numbered according to the `figures` [numbering] rule.
 *
 * As a [Figure] primitive, this function can be used in `.extend` to affect all figures in the document,
 * including standalone Markdown images (`![alt](url)`) that are automatically wrapped in a figure:
 *
 * ```markdown
 * .extend {figure} where:{ref: .ref::equals {logo}}
 *     .container border:{1px solid gray}
 *         .super
 * ```
 *
 * @param caption optional inline caption of the figure
 * @param referenceId optional ID for cross-referencing via [reference]
 * @param body content of the figure
 * @return the new [Figure] node
 */
@QFunction
fun figure(
    @LikelyNamed caption: InlineMarkdownContent? = null,
    @Name("ref") referenceId: String? = null,
    @Body body: MarkdownContent,
): NodeValue =
    Figure(
        body,
        caption = caption?.children,
        referenceId = referenceId,
    ).wrappedAsValue()
