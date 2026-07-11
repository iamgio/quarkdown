@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Clipped
import com.quarkdown.core.ast.quarkdown.block.Collapse
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.Landscape
import com.quarkdown.core.ast.quarkdown.block.Numbered
import com.quarkdown.core.ast.quarkdown.block.Stacked
import com.quarkdown.core.ast.quarkdown.inline.InlineCollapse
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData
import com.quarkdown.core.ast.quarkdown.inline.Whitespace
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.localization.localizeOrDefault
import com.quarkdown.core.context.localization.localizeOrNull
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.function.reflect.annotation.Body
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.value.MarkdownContentValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.log.Log
import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.util.node.toPlainText
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.processor.annotation.Spread

/**
 * @param foregroundColor text color. Default if unset
 * @param backgroundColor background color. Transparent if unset
 * @param borderColor border color. Default if unset and [borderWidth] is set
 * @param borderWidth border width. Default if unset and [borderColor] is set
 * @param borderStyle border style. Normal (solid) if unset and [borderColor] or [borderWidth] is set
 * @param alignment alignment of the content. Default if unset
 * @param textAlignment alignment of the text. [alignment] if unset
 * @param margin whitespace outside the content. None if unset
 * @param padding whitespace around the content. None if unset
 * @param cornerRadius corner (and border) radius. None if unset
 * @param fontSize relative font size of the text. Normal if unset
 * @param fontWeight font weight of the text. Normal if unset
 * @param fontStyle font style of the text. Normal if unset
 * @param fontVariant font variant of the text. Normal if unset
 * @param textDecoration text decoration of the text. None if unset
 * @param textCase text case of the text. Normal if unset
 */
data class StyleOptions(
    @Name("foreground") val foregroundColor: Color? = null,
    @Name("background") val backgroundColor: Color? = null,
    @Name("border") val borderColor: Color? = null,
    @Name("borderwidth") val borderWidth: Sizes? = null,
    @Name("borderstyle") val borderStyle: NodeStyle.BorderStyle? = null,
    @LikelyNamed val alignment: NodeStyle.Alignment? = null,
    @Name("textalignment") val textAlignment: NodeStyle.TextAlignment? = null,
    @Name("margin") val margin: Sizes? = null,
    @Name("padding") val padding: Sizes? = null,
    @Name("radius") val cornerRadius: Sizes? = null,
    @Name("fontsize") val fontSize: TextTransformData.Size? = null,
    @Name("fontweight") val fontWeight: TextTransformData.Weight? = null,
    @Name("fontstyle") val fontStyle: TextTransformData.Style? = null,
    @Name("fontvariant") val fontVariant: TextTransformData.Variant? = null,
    @Name("textdecoration") val textDecoration: TextTransformData.Decoration? = null,
    @Name("textcase") val textCase: TextTransformData.Case? = null,
) {
    /** @see com.quarkdown.core.ast.attributes.style.StylableNode */
    fun toNodeStyle(): NodeStyle {
        if (this == DEFAULT) return NodeStyle.DEFAULT
        return NodeStyle(
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            borderWidth = borderWidth,
            borderStyle = borderStyle,
            margin = margin,
            padding = padding,
            cornerRadius = cornerRadius,
            alignment = alignment,
            textAlignment = textAlignment ?: alignment?.let(NodeStyle.TextAlignment::fromAlignment),
            textTransform =
                TextTransformData(
                    size = fontSize,
                    weight = fontWeight,
                    style = fontStyle,
                    variant = fontVariant,
                    decoration = textDecoration,
                    case = textCase,
                ),
        )
    }

    companion object {
        val DEFAULT = StyleOptions()
    }
}

/**
 * A general-purpose container that groups content.
 *
 * Any active layout rules inherited by the parent (e.g. from [align], [row], [column], [grid]) are reset inside this container.
 *
 * @param width width of the container. No constraint if unset
 * @param height height of the container. No constraint if unset
 * @param fullWidth whether the container should take up the full width of the parent. Overridden by [width]. False if unset
 * @param float floating position of the container within the parent. Not floating if unset
 * @param fullColumnSpan whether the container should span across all columns in a multi-column layout. False if unset
 * @param className CSS class name to apply to the container, if supported by the renderer. None if unset
 * @param body content to group
 * @return the new [Container] node
 * @wiki container
 */
@QFunction
fun container(
    @LikelyNamed width: Size? = null,
    @LikelyNamed height: Size? = null,
    @Name("fullwidth") fullWidth: Boolean = false,
    @LikelyNamed float: Container.FloatAlignment? = null,
    @Name("fullspan") fullColumnSpan: Boolean = false,
    @Name("classname") className: String? = null,
    @Spread style: StyleOptions = StyleOptions.DEFAULT,
    @Body body: MarkdownContent? = null,
) = Container(
    width,
    height,
    fullWidth,
    float,
    fullColumnSpan,
    className,
    style.toNodeStyle(),
    body?.children ?: emptyList(),
).wrappedAsValue()

/**
 * Aligns content and text within its parent.
 *
 * @param alignment content alignment anchor and text alignment
 * @param body content to center
 * @return the new aligned [Container] node
 * @see container
 * @wiki align
 */
@QFunction
fun align(
    alignment: NodeStyle.Alignment,
    @Body body: MarkdownContent,
) = container(
    fullWidth = true,
    style = StyleOptions(alignment = alignment),
    body = body,
)

/**
 * Centers content and text within its parent.
 *
 * @param body content to center
 * @return the new aligned [Container] node
 * @see align
 * @wiki align
 */
@QFunction
fun center(
    @Body body: MarkdownContent,
) = align(NodeStyle.Alignment.CENTER, body)

/**
 * Turns content into a floating element, allowing subsequent content to wrap around it.
 *
 * @param alignment floating position
 * @param body content to float
 * @return the new floating [Container] node
 * @wiki float
 */
@QFunction
fun float(
    @LikelyNamed alignment: Container.FloatAlignment,
    @Body body: MarkdownContent,
) = container(
    float = alignment,
    body = body,
)

/**
 * Stacks content together, according to the specified type.
 * @param layout stack type
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param rowGap blank space between rows. If omitted, the default value is used. Only applicable to [Stacked.Column] and [Stacked.Grid]
 * @param columnGap blank space between columns. If omitted, the default value is used. Only applicable to [Stacked.Row] and [Stacked.Grid]
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
    rowGap: Size? = null,
    columnGap: Size? = null,
    body: MarkdownContent,
) = Stacked(layout, mainAxisAlignment, crossAxisAlignment, rowGap, columnGap, body.children).wrappedAsValue()

/**
 * Stacks content horizontally.
 *
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between children. If omitted, the default value is used
 * @param body content to stack
 * @return the new [Stacked] node
 * @wiki stacks
 */
@QFunction
fun row(
    @Name("alignment") mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.START,
    @Name("cross") crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    @LikelyNamed gap: Size? = null,
    @Body body: MarkdownContent,
) = stack(Stacked.Row, mainAxisAlignment, crossAxisAlignment, null, gap, body)

/**
 * Stacks content vertically.
 *
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between children. If omitted, the default value is used
 * @param body content to stack
 * @return the new [Stacked] node
 * @wiki stacks
 */
@QFunction
fun column(
    @Name("alignment") mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.START,
    @Name("cross") crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    @LikelyNamed gap: Size? = null,
    @Body body: MarkdownContent,
) = stack(Stacked.Column, mainAxisAlignment, crossAxisAlignment, gap, null, body)

/**
 * Stacks content in a grid layout.
 *
 * Each child is placed in a cell, and a row of cells ends when its cell count reaches [columnCount].
 *
 * @param columnCount positive number of columns
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap blank space between rows and columns. If omitted, the default value is used
 * @param rowGap blank space between rows (overrides [gap] for rows). If omitted, the default value is used
 * @param columnGap blank space between columns (overrides [gap] for columns). If omitted, the default value is used
 * @param body content to stack
 * @return the new [Stacked] node
 * @throws IllegalArgumentException if [columnCount] is non-positive
 * @wiki stacks
 */
@QFunction
fun grid(
    @Name("columns") columnCount: Int,
    @Name("alignment") mainAxisAlignment: Stacked.MainAxisAlignment = Stacked.MainAxisAlignment.CENTER,
    @Name("cross") crossAxisAlignment: Stacked.CrossAxisAlignment = Stacked.CrossAxisAlignment.CENTER,
    @LikelyNamed gap: Size? = null,
    @Name("vgap") rowGap: Size? = gap,
    @Name("hgap") columnGap: Size? = gap,
    @Body body: MarkdownContent,
) = when {
    columnCount <= 0 -> {
        throw IllegalArgumentException("Column count must be at least 1")
    }

    else -> {
        stack(
            Stacked.Grid(columnCount),
            mainAxisAlignment,
            crossAxisAlignment,
            rowGap ?: gap,
            columnGap ?: gap,
            body,
        )
    }
}

/**
 * Transposes content to landscape orientation by rotating it 90 degrees counter-clockwise.
 * This is useful for wide content, such as diagrams, that does not fit in the normal page orientation.
 *
 * This feature is experimental and may render inconsistently.
 * @param body content to transpose
 * @return the new [Landscape] node
 * @wiki landscape-content
 */
@QFunction
fun landscape(
    @Body body: MarkdownContent,
) = Landscape(body.children).wrappedAsValue()

/**
 * Shorthand for [container] with `fullspan:{true}`.
 * Makes content span across all columns in a multi-column layout.
 *
 * If the document has a single-column layout, the effect is the same as [container].
 *
 * @param body content to span across all columns
 * @return the new [Container] node with [Container.fullColumnSpan] enabled
 * @wiki multi-column-layout
 */
@QFunction
@Name("fullspan")
fun fullColumnSpan(
    @Body body: MarkdownContent,
) = container(fullColumnSpan = true, body = body)

/**
 * An empty rectangle that adds whitespace to the layout.
 *
 * If at least one of the dimensions is set, the rectangle will have a fixed size.
 * If both dimensions are unset, a blank character (`&nbsp;`) is used, which can be useful for spacing and adding line breaks.
 *
 * @param width width of the square. If unset, it defaults to zero
 * @param height height of the square. If unset, it defaults to zero
 * @return the new [Whitespace] node
 */
@QFunction
fun whitespace(
    @LikelyNamed width: Size? = null,
    @LikelyNamed height: Size? = null,
) = Whitespace(width, height).wrappedAsValue()

/**
 * Applies a clipping path to its content.
 *
 * @param clip clip type to apply
 * @param body content to clip
 * @return the new [Clipped] block
 * @wiki clip
 */
@QFunction
fun clip(
    clip: Clipped.Clip,
    @Body body: MarkdownContent,
) = Clipped(clip, body.children).wrappedAsValue()

/**
 * Inserts content in a styled box.
 *
 * @param title box title. If unset:
 * - If the locale ([docLanguage]) is set and supported, the title is localized according to the box [type]
 * - Otherwise, the box is untitled
 * @param type box type. If unset, it defaults to a callout box
 * @param padding padding around the box. If unset, the box uses the default padding
 * @param backgroundColor background color. If unset, the box uses the default color
 * @param foregroundColor foreground (text) color. If unset, the box uses the default color
 * @param body box content
 * @return the new [Box] node
 * @wiki box
 */
@QFunction
fun box(
    @Injected context: Context,
    title: InlineMarkdownContent? = null,
    @LikelyNamed type: Box.Type = Box.Type.CALLOUT,
    @LikelyNamed padding: Size? = null,
    @Name("background") backgroundColor: Color? = null,
    @Name("foreground") foregroundColor: Color? = null,
    @Body body: MarkdownContent,
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
 * Creates a _to do_ box, to mark content that needs to be done later, and also logs it to stdout.
 *
 * The title is localized according to the current locale ([docLanguage]), or English as a fallback.
 *
 * @param body content to show in the box
 * @return the new box node
 */
@QFunction
@Name("todo")
fun toDo(
    @Injected context: Context,
    @LikelyBody body: MarkdownContent,
): NodeValue {
    val title = context.localizeOrDefault(key = "todo")!!
    return Box(
        title = buildInline { text(title.uppercase()) },
        type = Box.Type.WARNING,
        content = body.children,
    ).wrappedAsValue().also {
        Log.warn("$title: ${body.children.toPlainText()}")
    }
}

/**
 * Inserts content in a collapsible block, whose content can be hidden or shown by interacting with it.
 *
 * @param title title of the block
 * @param open whether the block is open at the beginning
 * @param body content of the block when expanded
 * @return the new [Collapse] node
 * @wiki collapsible
 */
@QFunction
fun collapse(
    title: InlineMarkdownContent,
    @LikelyNamed open: Boolean = false,
    @Body body: MarkdownContent,
) = Collapse(title.children, open, body.children).wrappedAsValue()

/**
 * Inserts content in a collapsible text span, whose content can be expanded or collapsed by interacting with it.
 *
 * @param full content to show when the node is expanded
 * @param short content to show when the node is collapsed
 * @param open whether the block is open at the beginning
 * @return the new [InlineCollapse] node
 * @wiki collapsible
 */
@QFunction
@Name("textcollapse")
fun inlineCollapse(
    @LikelyNamed full: InlineMarkdownContent,
    @LikelyNamed short: InlineMarkdownContent,
    @LikelyNamed open: Boolean = false,
) = InlineCollapse(full.children, short.children, open).wrappedAsValue()

/**
 * Node that can be numbered depending on its location in the document
 * and the amount of occurrences according to its [key].
 *
 * The numbering format can be set via [numbering] by specifying a format for the given [key].
 *
 * ```
 * .numbering
 *     - headings: 1.1
 *     - greetings: 1.a
 *
 * # Title 1
 *
 * .numbered {greetings}
 *     number:
 *     Hello! This block has the number .number, which is `1.a`. The next one will be `1.b`.
 * ```
 *
 * @param key name to group (and count) numbered nodes
 * @param referenceId optional ID for cross-referencing via [reference]
 * @param body content, with the formatted location of this element (as a string) as an argument
 * @return the new [Numbered] node
 * @wiki numbering
 */
@QFunction
fun numbered(
    @LikelyNamed key: String,
    @Name("ref") referenceId: String? = null,
    @Body body: Lambda,
): NodeValue {
    val node =
        Numbered(
            key,
            referenceId = referenceId,
        ) { number ->
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
 * The following example joins 5 columns via [repeat]:
 *
 * ```
 * .table
 *     .repeat {5}
 *         | Header .1 |
 *         |-----------|
 *         |  Cell .1  |
 * ```
 *
 * @param subTables independent tables (as Markdown sources) that will be parsed and joined together into a single table
 * @return a new [Table] node
 * @wiki table-generation
 */
@QFunction
fun table(
    @Injected context: Context,
    @Body subTables: Iterable<Value<String>>,
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
