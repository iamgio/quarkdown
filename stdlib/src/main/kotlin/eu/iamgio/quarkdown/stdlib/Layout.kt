package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Aligned
import eu.iamgio.quarkdown.ast.Box
import eu.iamgio.quarkdown.ast.Clipped
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.function.value.wrappedAsValue

/**
 * `Layout` stdlib module exporter.
 * This module handles position and shape of an element.
 */
val Layout =
    setOf(
        ::align,
        ::center,
        ::clip,
        ::box,
    )

/**
 * Aligns content within its parent.
 * @param alignment content alignment anchor
 * @param body content to center
 * @return the new aligned block
 */
fun align(
    alignment: Aligned.Alignment,
    body: MarkdownContent,
) = Aligned(alignment, body.children).wrappedAsValue()

/**
 * Centers content within its parent.
 * @param body content to center
 * @return the new aligned block
 * @see align
 */
fun center(body: MarkdownContent) = align(Aligned.Alignment.CENTER, body)

/**
 * Applies a clipping path to its content.
 * @param clip clip type to apply
 * @return the new clipped block
 */
fun clip(
    clip: Clipped.Clip,
    body: MarkdownContent,
) = Clipped(clip, body.children).wrappedAsValue()

/**
 * Inserts content in a box.
 * @param title box title. The box is untitled if it is `null`
 * @param body box content
 * @return the new box node
 */
fun box(
    title: MarkdownContent? = null,
    body: MarkdownContent,
) = Box(title?.children, body.children).wrappedAsValue()
