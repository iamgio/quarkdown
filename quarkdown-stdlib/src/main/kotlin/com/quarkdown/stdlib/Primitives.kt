package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.library.module.moduleOf
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
 *                                 Has no effect if [canTrackLocation] is disabled.
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
 * Inserts content in a figure block, with an optional caption.
 *
 * If either [caption] or [referenceId] is set, the figure will be numbered according to the `figures` [numbering] rule.
 *
 * @param caption optional caption of the figure
 * @param referenceId optional ID for cross-referencing via [reference]
 * @param body content of the figure
 * @return the new [Figure] node
 */
fun figure(
    @LikelyNamed caption: String? = null,
    @Name("ref") referenceId: String? = null,
    @LikelyBody body: MarkdownContent,
): NodeValue =
    Figure<MarkdownContent>(
        body,
        caption = caption,
        referenceId = referenceId,
    ).wrappedAsValue()
