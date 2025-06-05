package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Clipped
import com.quarkdown.core.ast.quarkdown.block.Collapse
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.FullColumnSpan
import com.quarkdown.core.ast.quarkdown.block.Numbered
import com.quarkdown.core.ast.quarkdown.block.Stacked
import com.quarkdown.core.ast.quarkdown.inline.InlineCollapse
import com.quarkdown.core.ast.quarkdown.inline.Whitespace
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.localization.localizeOrDefault
import com.quarkdown.core.context.localization.localizeOrNull
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.MarkdownContentValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.log.Log
import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.util.toPlainText

/**
 * `Layout` stdlib module exporter.
 * This module handles position and shape of an element.
 */
val Layout: Module =
    moduleOf(
        ::container,
        ::align,
        ::center,
        ::float,
        ::row,
        ::column,
        ::grid,
        ::fullColumnSpan,
        ::whitespace,
        ::clip,
        ::box,
        ::toDo,
        ::collapse,
        ::inlineCollapse,
        ::figure,
        ::numbered,
        ::table,
    )

/**
 * A general-purpose container that groups content.
 * Any layout rules (e.g. from [align], [row], [column], [grid]) are ignored inside this container.
 * @param width width of the container. No constraint if unset
 * @param height height of the container. No constraint if unset
 * @param fullWidth whether the container should take up the full width of the parent. Overridden by [width]. False if unset
 * @param foregroundColor text color. Default if unset
 * @param backgroundColor background color. Transparent if unset
 * @param borderColor border color. Default if unset and [borderWidth] is set
 * @param borderWidth border width. Default if unset and [borderColor] is set
 * @param borderStyle border style. Normal (solid) if unset and [borderColor] or [borderWidth] is set
 * @param margin whitespace outside the content. None if unset
 * @param padding whitespace around the content. None if unset
 * @param cornerRadius corner (and border) radius. None if unset
 * @param alignment alignment of the content. Default if unset
 * @param textAlignment alignment of the text. [alignment] if unset
 * @param float floating position of the container within the parent. Not floating if unset
 * @param body content to group
 * @return the new [Container] node
 * @wiki Container
 */
fun container(
    width: Size? = null,
    height: Size? = null,
    @Name("fullwidth") fullWidth: Boolean = false,
    @Name("foreground") foregroundColor: Color? = null,
    @Name("background") backgroundColor: Color? = null,
    @Name("border") borderColor: Color? = null,
    @Name("borderwidth") borderWidth: Sizes? = null,
    @Name("borderstyle") borderStyle: Container.BorderStyle? = null,
    @Name("margin") margin: Sizes? = null,
    @Name("padding") padding: Sizes? = null,
    @Name("radius") cornerRadius: Sizes? = null,
    alignment: Container.Alignment? = null,
    @Name("textalignment") textAlignment: Container.TextAlignment? = alignment?.let(Container.TextAlignment::fromAlignment),
    float: Container.FloatAlignment? = null,
    @LikelyBody body: MarkdownContent? = null,
) = Container(
    width,
    height,
    fullWidth,
    foregroundColor,
    backgroundColor,
    borderColor,
    borderWidth,
    borderStyle,
    margin,
    padding,
    cornerRadius,
    alignment,
    textAlignment,
    float,
    body?.children ?: emptyList(),
).wrappedAsValue()

/**
 * Aligns content and text within its parent.
 * @param alignment content alignment anchor and text alignment
 * @param body content to center
 * @return the new aligned [Container] node
 * @see container
 * @wiki Align
 */
fun align(
    alignment: Container.Alignment,
    @LikelyBody body: MarkdownContent,
) = container(
    fullWidth = true,
    alignment = alignment,
    textAlignment = Container.TextAlignment.fromAlignment(alignment),
    body = body,
)

/**
 * Centers content and text within its parent.
 * @param body content to center
 * @return the new aligned [Container] node
 * @see align
 * @wiki Align
 */
fun center(
    @LikelyBody body: MarkdownContent,
) = align(Container.Alignment.CENTER, body)

/**
 * Turns content into a floating element, allowing subsequent content to wrap around it.
 * @param alignment floating position
 * @param body content to float
 * @return the new floating [Container] node
 * @wiki Float
 */
fun float(
    alignment: Container.FloatAlignment,
    @LikelyBody body: MarkdownContent,
) = container(
    float = alignment,
    body = body,
)

/**
 * Stacks content together, according to the specified type.
 * @param layout stack type
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between children. If omitted, the default value is used
 * @param body content to stack
 * @return the new [Stacked] node
 * @see row
 * @see column
 * @see grid
 */
private fun stack(
    layout: Stacked.Layout,
    mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.START,
    crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    gap: Size? = null,
    body: MarkdownContent,
) = Stacked(layout, mainAxisAlignment, crossAxisAlignment, gap, body.children).wrappedAsValue()

/**
 * Stacks content horizontally.
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between children. If omitted, the default value is used
 * @param body content to stack
 * @return the new [Stacked] node
 * @wiki Stacks
 */
fun row(
    @Name("alignment") mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.START,
    @Name("cross") crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    gap: Size? = null,
    @LikelyBody body: MarkdownContent,
) = stack(Stacked.Row, mainAxisAlignment, crossAxisAlignment, gap, body)

/**
 * Stacks content vertically.
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between children. If omitted, the default value is used
 * @param body content to stack
 * @return the new [Stacked] node
 * @wiki Stacks
 */
fun column(
    @Name("alignment") mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.START,
    @Name("cross") crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    gap: Size? = null,
    @LikelyBody body: MarkdownContent,
) = stack(Stacked.Column, mainAxisAlignment, crossAxisAlignment, gap, body)

/**
 * Stacks content in a grid layout.
 * Each child is placed in a cell in a row, and a row ends when its cell count reaches [columnCount].
 * @param columnCount positive number of columns
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between rows and columns. If omitted, the default value is used
 * @param body content to stack
 * @return the new [Stacked] node
 * @throws IllegalArgumentException if [columnCount] is non-positive
 * @wiki Stacks
 */
fun grid(
    @Name("columns") columnCount: Int,
    @Name("alignment") mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.CENTER,
    @Name("cross") crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    gap: Size? = null,
    @LikelyBody body: MarkdownContent,
) = when {
    columnCount <= 0 -> throw IllegalArgumentException("Column count must be at least 1")
    else -> stack(Stacked.Grid(columnCount), mainAxisAlignment, crossAxisAlignment, gap, body)
}

/**
 * If the document has a multi-column layout (set via [pageFormat]), makes content span across all columns in a multi-column layout.
 * If the document has a single-column layout, the effect is the same as [container].
 * @param body content to span across all columns
 * @return the new [FullColumnSpan] span node
 * @wiki Multi-column layout
 */
@Name("fullspan")
fun fullColumnSpan(
    @LikelyBody body: MarkdownContent,
) = FullColumnSpan(body.children).wrappedAsValue()

/**
 * An empty square that adds whitespace to the layout.
 * If at least one of the dimensions is set, the square will have a fixed size.
 * If both dimensions are unset, a blank character is used, which can be useful for spacing and adding line breaks.
 * @param width width of the square. If unset, it defaults to zero
 * @param height height of the square. If unset, it defaults to zero
 * @return the new [Whitespace] node
 */
fun whitespace(
    width: Size? = null,
    height: Size? = null,
) = Whitespace(width, height).wrappedAsValue()

/**
 * Applies a clipping path to its content.
 * @param clip clip type to apply
 * @return the new [Clipped] block
 * @wiki Clip
 */
fun clip(
    clip: Clipped.Clip,
    @LikelyBody body: MarkdownContent,
) = Clipped(clip, body.children).wrappedAsValue()

/**
 * Inserts content in a box.
 * @param title box title. If unset:
 * - If the locale ([docLanguage]) is set and supported, the title is localized according to the box [type]
 * - Otherwise, the box is untitled
 * @param type box type. If unset, it defaults to a callout box
 * @param padding padding around the box. If unset, the box uses the default padding
 * @param backgroundColor background color. If unset, the box uses the default color
 * @param foregroundColor foreground (text) color. If unset, the box uses the default color
 * @param body box content
 * @return the new [Box] node
 * @wiki Box
 */
fun box(
    @Injected context: Context,
    title: InlineMarkdownContent? = null,
    type: Box.Type = Box.Type.CALLOUT,
    padding: Size? = null,
    @Name("background") backgroundColor: Color? = null,
    @Name("foreground") foregroundColor: Color? = null,
    @LikelyBody body: MarkdownContent,
): NodeValue {
    // Localizes the title according to the box type,
    // if the title is not manually set.
    fun localizedTitle(): InlineContent? =
        context.localizeOrNull(key = type.name)?.let {
            buildInline { text(it) }
        }

    return Box(
        title?.children ?: localizedTitle(),
        type,
        padding,
        backgroundColor,
        foregroundColor,
        body.children,
    ).wrappedAsValue()
}

/**
 * Creates a _to do_ box, to mark content that needs to be done later, and logs it.
 * The title is localized according to the current locale ([docLanguage]), or English as a fallback.
 * @param body content to show in the box
 * @return the new box node
 */
@Name("todo")
fun toDo(
    @Injected context: Context,
    @LikelyBody body: MarkdownContent,
): NodeValue {
    val title = context.localizeOrDefault(key = "todo")!!
    return Box(
        title = buildInline { text(title.uppercase()) },
        type = Box.Type.WARNING,
        children = body.children,
    ).wrappedAsValue().also {
        Log.warn("$title: ${body.children.toPlainText()}")
    }
}

/**
 * Inserts content in a collapsible block, whose content can be hidden or shown by interacting with it.
 * @param title title of the block
 * @param open whether the block is open at the beginning
 * @return the new [Collapse] node
 * @wiki Collapsible
 */
fun collapse(
    title: InlineMarkdownContent,
    open: Boolean = false,
    @LikelyBody body: MarkdownContent,
) = Collapse(title.children, open, body.children).wrappedAsValue()

/**
 * Inserts content in a collapsible text span, whose content can be expanded or collapsed by interacting with it.
 * @param full content to show when the node is expanded
 * @param short content to show when the node is collapsed
 * @param open whether the block is open at the beginning
 * @return the new [InlineCollapse] node
 * @wiki Collapsible
 */
@Name("textcollapse")
fun inlineCollapse(
    full: InlineMarkdownContent,
    short: InlineMarkdownContent,
    open: Boolean = false,
) = InlineCollapse(full.children, short.children, open).wrappedAsValue()

/**
 * Inserts content in a figure block, which features an optional caption and can be numbered according to the `figures` numbering rules.
 * @param caption optional caption of the figure
 * @param body content of the figure
 * @return the new [Figure] node
 */
fun figure(
    caption: String? = null,
    @LikelyBody body: MarkdownContent,
): NodeValue =
    object : Figure<MarkdownContent>(body) {
        override val caption: String? = caption
    }.wrappedAsValue()

/**
 * Node that can be numbered depending on its location in the document
 * and the amount of occurrences according to its [key].
 * @param key name to group (and count) numbered nodes
 * @param body content, with the formatted location of this element (as a string) as an argument
 * @return the new [Numbered] node
 * @wiki Numbering
 */
fun numbered(
    key: String,
    @LikelyBody body: Lambda,
): NodeValue {
    val node =
        Numbered(key) { number ->
            body
                .invoke<MarkdownContent, MarkdownContentValue>(number.wrappedAsValue())
                .unwrappedValue
                .children
        }
    return node.wrappedAsValue()
}

/**
 * Creates a table out of a collection of columns.
 *
 * The following example joins 5 columns:
 * ```
 * .table
 *     .foreach {1..5}
 *         | Header .1 |
 *         |-----------|
 *         |  Cell .1  |
 * ```
 *
 * @param subTables independent tables (as Markdown sources) that will be parsed and joined together into a single table
 * @return a new [Table] node
 * @wiki Table generator
 */
fun table(
    @Injected context: Context,
    @LikelyBody subTables: Iterable<Value<String>>,
): NodeValue {
    val columns =
        subTables
            .asSequence()
            .map { it.unwrappedValue }
            .map { ValueFactory.blockMarkdown(it, context).unwrappedValue }
            .map { it.children.first() }
            .filterIsInstance<Table>()
            .flatMap { it.columns }

    return Table(columns.toList()).wrappedAsValue()
}
